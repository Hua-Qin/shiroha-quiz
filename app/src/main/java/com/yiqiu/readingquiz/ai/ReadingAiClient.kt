package com.yiqiu.readingquiz.ai

import android.util.Log
import com.yiqiu.readingquiz.data.AiConfig
import com.yiqiu.readingquiz.data.model.Article
import com.yiqiu.readingquiz.data.model.Option
import com.yiqiu.readingquiz.data.model.Question
import com.yiqiu.readingquiz.data.model.QuestionType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * AI HTTP 客户端（参考 native ShirohaAiClient 移植并精简）。
 * - HttpURLConnection + OpenAI 兼容 chat/completions
 * - response_format: json_object 优先，失败回退不带该字段
 * - 结构化 JSON 提取（extractBalancedJson）兼容代码围栏与噪声文本
 * - 错误分类：超时 / 域名解析 / HTTP 401/404/429/5xx / JSON 解析失败
 */
object ReadingAiClient {

    private const val TAG = "ReadingAi"

    sealed class AiResult<out T> {
        data class Success<T>(val value: T) : AiResult<T>()
        data class Failure(val category: String, val message: String) : AiResult<Nothing>()
    }

    /**
     * HTTP 异常（携带状态码 + 服务端错误信息），用于精确区分鉴权失败 / 资源不存在 / 限流等。
     * 之前版本把所有 IOException 吞成 null 再误判为 Success，现改为抛出异常由调用方分类处理。
     */
    class AiHttpException(val code: Int, override val message: String) : IOException(message)

    fun testConnection(config: AiConfig): AiResult<String> {
        Log.d(TAG, "testConnection: baseUrl=${config.apiBaseUrl}, model=${config.modelName}")
        val err = validateConfigOrError(config)
        if (err != null) {
            Log.w(TAG, "testConnection config invalid: $err")
            ErrorLogStore.log(TAG, "testConnection 配置无效：$err", "W")
            return AiResult.Failure("config", err)
        }
        val content = try {
            requestChatCompletion(
                config = config,
                systemPrompt = AiPrompts.TEST_CONNECTION_SYSTEM_PROMPT,
                userPayload = "请只回复 JSON：{\"ok\":true}",
                maxTokens = 64
            )
        } catch (e: AiHttpException) {
            Log.w(TAG, "testConnection HTTP ${e.code}: ${e.message}")
            ErrorLogStore.log(TAG, "testConnection HTTP ${e.code}：${e.message}", "E", e)
            val hint = when (e.code) {
                401, 403 -> "（API Key 无效或权限不足）"
                404 -> "（endpoint 不存在，请检查 Base URL）"
                429 -> "（请求频率超限）"
                in 500..599 -> "（服务端错误）"
                else -> ""
            }
            return AiResult.Failure("http", "HTTP ${e.code}${hint}：${e.message}")
        } catch (e: Throwable) {
            Log.w(TAG, "testConnection FAILED: ${e.message}", e)
            ErrorLogStore.log(TAG, "testConnection 网络异常：${e.message}", "E", e)
            return AiResult.Failure("exception", e.message ?: "未知错误")
        }
        // 关键修复：content == null 意味着请求失败（网络错误/鉴权失败/timeout），必须返回 Failure 而非 Success
        return if (content.isNullOrBlank()) {
            Log.w(TAG, "testConnection FAILED: null/blank content")
            ErrorLogStore.log(TAG, "testConnection 失败：请求未返回内容（请检查 API Key 与网络）", "E")
            AiResult.Failure("network", "请求失败：无法连接到 ${config.apiBaseUrl}，请检查 API Key 与网络")
        } else {
            Log.i(TAG, "testConnection SUCCESS: ${content.take(40)}")
            AiResult.Success("连接成功：${content.take(40)}")
        }
    }

    /**
     * 拉取远端模型列表。
     * 调 OpenAI 兼容的 GET ${apiBaseUrl}/models，返回 {"data":[{"id":"..."}]} 中的 id 列表。
     * 不强制要求 modelName（仅需 apiBaseUrl + apiKey）。
     */
    fun fetchModels(config: AiConfig): AiResult<List<String>> {
        if (config.apiBaseUrl.isBlank()) {
            ErrorLogStore.log(TAG, "fetchModels: API Base URL 为空", "W")
            return AiResult.Failure("config", "API Base URL 不能为空")
        }
        if (config.apiKey.isBlank()) {
            ErrorLogStore.log(TAG, "fetchModels: API Key 为空", "W")
            return AiResult.Failure("config", "API Key 不能为空")
        }
        // URL 合法性校验
        val endpoint = "${config.apiBaseUrl.trimEnd('/')}/models"
        try {
            URL(endpoint)
        } catch (e: Throwable) {
            Log.w(TAG, "fetchModels invalid URL: $endpoint", e)
            ErrorLogStore.log(TAG, "fetchModels URL 不合法：$endpoint", "W", e)
            return AiResult.Failure("config", "API Base URL 格式不合法：$endpoint")
        }
        Log.d(TAG, "fetchModels: endpoint=$endpoint")
        val timeoutMs = config.timeoutSeconds.coerceIn(15, 180) * 1000
        val body = try {
            getRaw(endpoint, config.apiKey, timeoutMs)
        } catch (e: AiHttpException) {
            Log.w(TAG, "fetchModels HTTP ${e.code}: ${e.message}")
            ErrorLogStore.log(TAG, "fetchModels HTTP ${e.code}：${e.message}", "E", e)
            val hint = when (e.code) {
                401, 403 -> "（API Key 无效或权限不足）"
                404 -> "（endpoint 不存在，请检查 Base URL）"
                else -> ""
            }
            return AiResult.Failure("http", "HTTP ${e.code}${hint}：${e.message}")
        } catch (e: Throwable) {
            Log.w(TAG, "fetchModels network error: ${e.message}", e)
            ErrorLogStore.log(TAG, "fetchModels 网络异常：${e.message}", "E", e)
            return AiResult.Failure("exception", "${e.javaClass.simpleName}: ${e.message ?: "未知错误"}")
        }
        return try {
            val obj = JSONObject(body)
            val arr = obj.optJSONArray("data")
            if (arr == null) {
                Log.w(TAG, "fetchModels parse: missing 'data' array, bodyPreview='${body.take(200)}'")
                ErrorLogStore.log(TAG, "fetchModels 响应缺少 data 数组：${body.take(200)}", "W")
                return AiResult.Success(emptyList())
            }
            val ids = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val id = item.optString("id", "")
                if (id.isNotBlank()) ids.add(id)
            }
            if (ids.isEmpty()) {
                Log.w(TAG, "fetchModels parse: no model ids found, count=${arr.length()}")
                ErrorLogStore.log(TAG, "fetchModels 未解析到模型 id", "W")
                AiResult.Failure("parse", "未解析到任何模型 id")
            } else {
                Log.i(TAG, "fetchModels SUCCESS: ${ids.size} models")
                AiResult.Success(ids)
            }
        } catch (e: JSONException) {
            Log.w(TAG, "fetchModels parse failed: bodyPreview='${body.take(200)}'", e)
            ErrorLogStore.log(TAG, "fetchModels JSON 解析失败：${e.message}", "E", e)
            AiResult.Failure("parse", "JSON 解析失败：${e.message}")
        }
    }

    fun generateQuestionsFromArticle(
        config: AiConfig,
        article: Article,
        questionCount: Int = 5
    ): AiResult<List<Question>> {
        Log.d(TAG, "generateQuestions: articleId=${article.id}, count=$questionCount")
        val err = validateConfigOrError(config)
        if (err != null) {
            Log.w(TAG, "generateQuestions config invalid: $err")
            ErrorLogStore.log(TAG, "generateQuestions 配置无效：$err", "W")
            return AiResult.Failure("config", err)
        }
        val userPayload = buildArticlePayload(article, questionCount)
        val content = try {
            requestChatCompletion(
                config = config,
                systemPrompt = AiPrompts.ARTICLE_QUIZ_GENERATION_SYSTEM_PROMPT,
                userPayload = userPayload
            )
        } catch (e: AiHttpException) {
            Log.w(TAG, "generateQuestions HTTP ${e.code}: ${e.message}")
            ErrorLogStore.log(TAG, "generateQuestions HTTP ${e.code}：${e.message}", "E", e)
            val hint = when (e.code) {
                401, 403 -> "（API Key 无效或权限不足）"
                429 -> "（请求频率超限）"
                else -> ""
            }
            return AiResult.Failure("http", "HTTP ${e.code}${hint}：${e.message}")
        } catch (e: Throwable) {
            Log.w(TAG, "generateQuestions FAILED: ${e.message}", e)
            ErrorLogStore.log(TAG, "generateQuestions 网络异常：${e.message}", "E", e)
            return AiResult.Failure("exception", "${e.javaClass.simpleName}: ${e.message ?: "未知错误"}")
        }
        if (content == null) {
            Log.w(TAG, "generateQuestions: null content (empty choices)")
            ErrorLogStore.log(TAG, "generateQuestions：AI 返回空内容", "W")
            return AiResult.Failure("parse", "AI 返回了空内容，请重试或更换模型")
        }
        val result = parseQuestions(content)
        Log.i(TAG, "generateQuestions result: ${(result as? AiResult.Success)?.value?.size ?: "failure ${(result as AiResult.Failure).message}"}")
        return result
    }

    // ----------------- HTTP -----------------

    private fun requestChatCompletion(
        config: AiConfig,
        systemPrompt: String,
        userPayload: String,
        maxTokens: Int? = null
    ): String? {
        val endpoint = "${config.apiBaseUrl.trimEnd('/')}/chat/completions"
        val timeoutMs = config.timeoutSeconds.coerceIn(15, 180) * 1000

        val payload = JSONObject().apply {
            put("model", config.modelName)
            put("temperature", 0.1)
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", systemPrompt))
                put(JSONObject().put("role", "user").put("content", userPayload))
            })
            if (maxTokens != null) put("max_tokens", maxTokens)
        }

        // 第一次：带 response_format:json_object；400（不支持）时降级重试
        val first = try {
            postWithJsonFormat(endpoint, config.apiKey, timeoutMs, payload)
        } catch (e: AiHttpException) {
            if (e.code == 400) null else throw e
        }
        if (first != null) return first
        // 第二次：不带 response_format
        return postWithoutJsonFormat(endpoint, config.apiKey, timeoutMs, payload)
    }

    private fun postWithJsonFormat(
        endpoint: String,
        apiKey: String,
        timeoutMs: Int,
        payload: JSONObject
    ): String? {
        payload.put("response_format", JSONObject().put("type", "json_object"))
        return execute(endpoint, apiKey, timeoutMs, payload)
    }

    private fun postWithoutJsonFormat(
        endpoint: String,
        apiKey: String,
        timeoutMs: Int,
        payload: JSONObject
    ): String? {
        payload.remove("response_format")
        return execute(endpoint, apiKey, timeoutMs, payload)
    }

    /**
     * 简单的 GET 请求（用于 /models 端点）。
     * 返回原始响应体；HTTP 非 2xx 时抛出 [AiHttpException]。
     */
    private fun getRaw(endpoint: String, apiKey: String, timeoutMs: Int): String {
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "ReadingQuiz/0.2 (Android)")
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) {
                val errBody = conn.errorStream?.bufferedReader()?.use { it.readText() }?.take(300) ?: ""
                Log.w(TAG, "getRaw $endpoint: HTTP $code, errBody='$errBody'")
                throw AiHttpException(code, parseErrorMessage(errBody, "HTTP $code"))
            }
            return conn.inputStream?.let {
                BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use(BufferedReader::readText)
            } ?: ""
        } finally {
            conn.disconnect()
        }
    }

    private fun execute(
        endpoint: String,
        apiKey: String,
        timeoutMs: Int,
        payload: JSONObject
    ): String? {
        val start = System.currentTimeMillis()
        Log.d(TAG, "POST $endpoint timeoutMs=$timeoutMs")
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "ReadingQuiz/0.2 (Android)")
        }
        try {
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload.toString()) }
            val code = conn.responseCode
            val elapsed = System.currentTimeMillis() - start
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.let {
                BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use(BufferedReader::readText)
            } ?: ""
            if (code !in 200..299) {
                Log.w(TAG, "POST $endpoint: HTTP $code, elapsed=${elapsed}ms, bodyPreview='${body.take(200)}'")
                throw AiHttpException(code, parseErrorMessage(body, "HTTP $code"))
            }
            Log.d(TAG, "POST $endpoint: HTTP $code, elapsed=${elapsed}ms")
            return extractContent(JSONObject(body))
        } finally {
            conn.disconnect()
        }
    }

    /**
     * 从 HTTP 错误响应体中提取可读错误信息。
     * 兼容 OpenAI 风格 {"error":{"message":"..."}} 和纯文本。
     */
    private fun parseErrorMessage(body: String, fallback: String): String {
        if (body.isBlank()) return fallback
        return try {
            val obj = JSONObject(body)
            val err = obj.optJSONObject("error")
            err?.optString("message", "")?.ifBlank { null } ?: obj.optString("message", fallback)
        } catch (e: JSONException) {
            body.take(150)
        }
    }

    private fun extractContent(payload: JSONObject): String? {
        val choices = payload.optJSONArray("choices") ?: return null
        if (choices.length() == 0) return null
        val first = choices.getJSONObject(0)
        val message = first.optJSONObject("message")
        if (message != null) {
            val c = message.optString("content", "")
            if (c.isNotBlank()) return c
        }
        val txt = first.optString("text", "")
        if (txt.isNotBlank()) return txt
        return payload.optString("output_text", "").ifBlank { null }
    }

    // ----------------- JSON 提取 -----------------

    /**
     * 提取首个平衡的 JSON 对象。处理代码围栏、前后噪声、字符串转义。
     */
    fun extractBalancedJson(text: String): String? {
        if (text.isBlank()) return null
        val stripped = text
            .replace(Regex("^\\s*```(?:json)?\\s*", RegexOption.MULTILINE), "")
            .replace(Regex("```\\s*$", RegexOption.MULTILINE), "")
        var start = -1
        var depth = 0
        var inString = false
        var escape = false
        for (i in stripped.indices) {
            val c = stripped[i]
            if (escape) { escape = false; continue }
            if (c == '\\') { escape = true; continue }
            if (c == '"') { inString = !inString; continue }
            if (inString) continue
            when (c) {
                '{' -> {
                    if (depth == 0) start = i
                    depth++
                }
                '}' -> {
                    if (depth > 0) depth--
                    if (depth == 0 && start >= 0) {
                        return stripped.substring(start, i + 1)
                    }
                }
            }
        }
        return null
    }

    private fun parseQuestions(content: String): AiResult<List<Question>> {
        val json = extractBalancedJson(content)
            ?: return AiResult.Failure("parse", "AI 返回中找不到合法 JSON")
        return try {
            val obj = JSONObject(json)
            val arr = obj.optJSONArray("questions")
                ?: return AiResult.Failure("parse", "AI 返回缺少 questions 数组")
            val questions = mutableListOf<Question>()
            for (i in 0 until arr.length()) {
                val q = arr.getJSONObject(i)
                questions.add(questionFromJson(q))
            }
            AiResult.Success(questions)
        } catch (e: JSONException) {
            AiResult.Failure("parse", "JSON 解析失败：${e.message}")
        }
    }

    private fun questionFromJson(o: JSONObject): Question {
        val oArr = o.optJSONArray("options") ?: JSONArray()
        val options = mutableListOf<Option>()
        for (i in 0 until oArr.length()) {
            val op = oArr.getJSONObject(i)
            options.add(Option(key = op.optString("key", ""), text = op.optString("text", "")))
        }
        val typeStr = o.optString("type", "SINGLE").uppercase()
        val type = runCatching { QuestionType.valueOf(typeStr) }.getOrDefault(QuestionType.SINGLE)
        val answer = o.optJSONArray("answer").toStringListFromJson()
        val blanks = o.optJSONArray("blankAnswers").toStringListFromJson()
        return Question(
            id = java.util.UUID.randomUUID().toString(),
            type = type,
            question = o.optString("question", ""),
            options = options,
            answer = answer,
            blankAnswers = blanks,
            analysis = o.optString("analysis", ""),
            category = o.optString("category", "")
        )
    }

    private fun JSONArray?.toStringListFromJson(): List<String> {
        if (this == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until length()) list.add(optString(i, ""))
        return list
    }

    // ----------------- Payload -----------------

    /**
     * 递归展平 Section 为纯文本（AI 输入不需要层级标记）。
     */
    private fun flattenSection(section: com.yiqiu.readingquiz.data.model.ArticleBlock.Section): String {
        val sb = StringBuilder()
        sb.append(section.title).append('\n')
        section.children.forEach { child ->
            when (child) {
                is com.yiqiu.readingquiz.data.model.ArticleBlock.Paragraph -> sb.append(child.text).append('\n')
                is com.yiqiu.readingquiz.data.model.ArticleBlock.Image -> sb.append("[图片：${child.caption}]").append('\n')
                is com.yiqiu.readingquiz.data.model.ArticleBlock.Section -> sb.append(flattenSection(child)).append('\n')
            }
        }
        return sb.toString().trim()
    }

    private fun buildArticlePayload(article: Article, questionCount: Int): String {
        // Task 3：扁平化所有 block（Section 递归展平为子块的纯文本拼接）
        val plainText = article.blocks.joinToString("\n\n") { block ->
            when (block) {
                is com.yiqiu.readingquiz.data.model.ArticleBlock.Paragraph -> block.text
                is com.yiqiu.readingquiz.data.model.ArticleBlock.Image -> "[图片：${block.caption}]"
                is com.yiqiu.readingquiz.data.model.ArticleBlock.Section -> flattenSection(block)
            }
        }
        return JSONObject()
            .put("task", "generate_questions_from_article")
            .put("questionCount", questionCount)
            .put("article", JSONObject()
                .put("id", article.id)
                .put("title", article.title)
                .put("author", article.author)
                .put("source", article.source)
                .put("category", article.category)
                .put("content", plainText)
            )
            .toString()
    }

    private fun validateConfigOrError(config: AiConfig): String? {
        val err: String? = when {
            config.apiBaseUrl.isBlank() -> "API Base URL 不能为空"
            config.apiKey.isBlank() -> "API Key 不能为空"
            config.modelName.isBlank() -> "模型名不能为空"
            else -> null
        }
        Log.d(TAG, "validateConfig: ok=${err == null}")
        return err
    }
}