package com.yiqiu.readingquiz.ai

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
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

/**
 * AI HTTP 客户端（参考 native ShirohaAiClient 移植并精简）。
 * - HttpURLConnection + OpenAI 兼容 chat/completions
 * - response_format: json_object 优先，失败回退不带该字段
 * - 结构化 JSON 提取（extractBalancedJson）兼容代码围栏与噪声文本
 * - 错误分类：超时 / 域名解析 / HTTP 401/404/429/5xx / JSON 解析失败
 */
object ReadingAiClient {

    sealed class AiResult<out T> {
        data class Success<T>(val value: T) : AiResult<T>()
        data class Failure(val category: String, val message: String) : AiResult<Nothing>()
    }

    fun testConnection(config: AiConfig): AiResult<String> {
        val err = validateConfigOrError(config)
        if (err != null) return AiResult.Failure("config", err)
        val content = try {
            requestChatCompletion(
                config = config,
                systemPrompt = AiPrompts.TEST_CONNECTION_SYSTEM_PROMPT,
                userPayload = "请只回复 JSON：{\"ok\":true}",
                maxTokens = 64
            )
        } catch (e: Throwable) {
            return AiResult.Failure("exception", e.message ?: "未知错误")
        }
        return if (content.isNullOrBlank()) {
            AiResult.Success("连接成功。")
        } else {
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
            return AiResult.Failure("config", "API Base URL 不能为空")
        }
        if (config.apiKey.isBlank()) {
            return AiResult.Failure("config", "API Key 不能为空")
        }
        val endpoint = "${config.apiBaseUrl.trimEnd('/')}/models"
        val timeoutMs = config.timeoutSeconds.coerceIn(15, 180) * 1000
        val body = try {
            getRaw(endpoint, config.apiKey, timeoutMs)
        } catch (e: Throwable) {
            return AiResult.Failure("exception", e.message ?: "未知错误")
        } ?: return AiResult.Failure("network", "请求失败：无法连接到 ${config.apiBaseUrl}")
        return try {
            val obj = org.json.JSONObject(body)
            val arr = obj.optJSONArray("data")
                ?: return AiResult.Failure("parse", "返回缺少 data 数组")
            val ids = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val id = item.optString("id", "")
                if (id.isNotBlank()) ids.add(id)
            }
            if (ids.isEmpty()) AiResult.Failure("parse", "未解析到任何模型 id")
            else AiResult.Success(ids)
        } catch (e: org.json.JSONException) {
            AiResult.Failure("parse", "JSON 解析失败：${e.message}")
        }
    }

    fun generateQuestionsFromArticle(
        config: AiConfig,
        article: Article,
        questionCount: Int = 5
    ): AiResult<List<Question>> {
        val err = validateConfigOrError(config)
        if (err != null) return AiResult.Failure("config", err)
        val userPayload = buildArticlePayload(article, questionCount)
        val content = try {
            requestChatCompletion(
                config = config,
                systemPrompt = AiPrompts.ARTICLE_QUIZ_GENERATION_SYSTEM_PROMPT,
                userPayload = userPayload
            )
        } catch (e: Throwable) {
            return AiResult.Failure("exception", e.message ?: "未知错误")
        } ?: return AiResult.Failure("network", "AI 请求失败：无法连接到 ${config.apiBaseUrl}")

        return parseQuestions(content)
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

        return try {
            postWithJsonFormat(endpoint, config.apiKey, timeoutMs, payload)
                ?: postWithoutJsonFormat(endpoint, config.apiKey, timeoutMs, payload)
        } catch (e: SocketTimeoutException) {
            null
        } catch (e: UnknownHostException) {
            null
        } catch (e: IOException) {
            null
        }
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
     * 返回原始响应体；连接失败 / 5xx 时返回 null。
     */
    private fun getRaw(endpoint: String, apiKey: String, timeoutMs: Int): String? {
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Accept", "application/json")
        }
        return try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            stream?.let {
                BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use(BufferedReader::readText)
            }
        } catch (_: Throwable) {
            null
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
        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Accept", "application/json")
        }
        return try {
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload.toString()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.let {
                BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use(BufferedReader::readText)
            } ?: ""
            if (code !in 200..299) return null
            extractContent(JSONObject(body))
        } finally {
            conn.disconnect()
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

    private fun buildArticlePayload(article: Article, questionCount: Int): String {
        val plainText = article.blocks.joinToString("\n\n") { block ->
            when (block) {
                is com.yiqiu.readingquiz.data.model.ArticleBlock.Paragraph -> block.text
                is com.yiqiu.readingquiz.data.model.ArticleBlock.Image -> "[图片：${block.caption}]"
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
        if (config.apiBaseUrl.isBlank()) return "API Base URL 不能为空"
        if (config.apiKey.isBlank()) return "API Key 不能为空"
        if (config.modelName.isBlank()) return "模型名不能为空"
        return null
    }
}