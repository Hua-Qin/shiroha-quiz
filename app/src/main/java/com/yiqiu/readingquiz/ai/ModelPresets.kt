package com.yiqiu.readingquiz.ai

import android.util.Log

/**
 * 国产 / 国际主流大模型预设（OpenAI 兼容）。
 *
 * - 智谱清言 (Zhipu / GLM)：base_url = https://open.bigmodel.cn/api/paas/v4
 *   文档：https://docs.bigmodel.cn/cn/guide/start/quick-start
 *   API Key：Bearer 形式（id.secret 也支持）
 *   模型：glm-4.5-flash（免费）/ glm-4-plus / glm-4-flash
 *
 * - 阶跃星辰 (StepFun)：base_url = https://api.stepfun.com/v1
 *   文档：https://platform.stepfun.com/docs/zh/llm/text
 *   API Key：Bearer 形式
 *   模型：step-3.7-flash / step-3.5-flash / step-1 / step-2
 *
 * 两个平台都提供 GET /models 与 POST /chat/completions，与现有 ReadingAiClient 完全兼容。
 * 无需修改 HTTP 层。
 */
object ModelPresets {

    private const val TAG = "ModelPresets"

    data class Preset(
        val id: String,            // 内部 ID
        val displayName: String,    // 显示名
        val baseUrl: String,        // API Base URL
        val defaultModel: String,   // 默认模型名
        val modelsPath: String = "/models",
        val chatPath: String = "/chat/completions",
        val authHeader: String = "Authorization",
        val authPrefix: String = "Bearer",
        val notes: String = ""      // 备注
    )

    val ZHIPU_GLM = Preset(
        id = "zhipu",
        displayName = "智谱清言（GLM）",
        baseUrl = "https://open.bigmodel.cn/api/paas/v4",
        defaultModel = "glm-4.5-flash",
        notes = "免费模型：glm-4.5-flash；推荐：glm-4-plus；API Key 为 Bearer 格式（id.secret 也可）"
    )

    val STEPFUN = Preset(
        id = "stepfun",
        displayName = "阶跃星辰（StepFun）",
        baseUrl = "https://api.stepfun.com/v1",
        defaultModel = "step-3.5-flash",
        notes = "推荐：step-3.5-flash / step-3.7-flash；OpenAI Chat Completions 完全兼容"
    )

    val ALL: List<Preset> = listOf(ZHIPU_GLM, STEPFUN)

    /**
     * 按 baseUrl 前缀猜测匹配的 preset（用于历史配置迁移 / 自动识别）。
     */
    fun detectByBaseUrl(baseUrl: String): Preset? {
        val normalized = baseUrl.trimEnd('/').lowercase()
        return ALL.firstOrNull { normalized.contains(it.id) || normalized.startsWith(it.baseUrl.trimEnd('/').lowercase()) }
    }

    /**
     * 调用 preset 的 /models 端点拉取模型列表。
     * 复用 ReadingAiClient 的 getRaw 逻辑，但作为独立方法便于扩展不同鉴权方式。
     */
    fun fetchModels(preset: Preset, apiKey: String, timeoutSeconds: Int = 60): ReadingAiClient.AiResult<List<String>> {
        val endpoint = "${preset.baseUrl.trimEnd('/')}${preset.modelsPath}"
        Log.d(TAG, "fetchModels(${preset.id}): endpoint=$endpoint")
        val timeoutMs = timeoutSeconds.coerceIn(15, 180) * 1000
        val body = try {
            // 智谱 / 阶跃的 API Key 都是 Bearer 格式；如有差异未来扩展
            httpGet(endpoint, "${preset.authPrefix} $apiKey", timeoutMs)
        } catch (e: Throwable) {
            Log.w(TAG, "fetchModels(${preset.id}) network error: ${e.message}", e)
            return ReadingAiClient.AiResult.Failure("exception", e.message ?: "未知错误")
        }
        if (body == null) {
            return ReadingAiClient.AiResult.Failure("network", "请求失败：无法连接到 ${preset.baseUrl}")
        }
        return try {
            val obj = org.json.JSONObject(body)
            val arr = obj.optJSONArray("data")
                ?: return ReadingAiClient.AiResult.Success(emptyList())
            val ids = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val id = item.optString("id", "")
                if (id.isNotBlank()) ids.add(id)
            }
            Log.i(TAG, "fetchModels(${preset.id}) SUCCESS: ${ids.size} models")
            ReadingAiClient.AiResult.Success(ids)
        } catch (e: org.json.JSONException) {
            Log.w(TAG, "fetchModels(${preset.id}) parse failed", e)
            ReadingAiClient.AiResult.Failure("parse", "JSON 解析失败：${e.message}")
        }
    }

    private fun httpGet(endpoint: String, authValue: String, timeoutMs: Int): String? {
        val url = java.net.URL(endpoint)
        val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            setRequestProperty("Authorization", authValue)
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "ReadingQuiz/0.2 (Android)")
        }
        return try {
            val code = conn.responseCode
            if (code !in 200..299) {
                Log.w(TAG, "GET $endpoint: HTTP $code")
                null
            } else {
                conn.inputStream?.use {
                    java.io.BufferedReader(java.io.InputStreamReader(it, Charsets.UTF_8)).use(java.io.BufferedReader::readText)
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "GET $endpoint exception: ${e.message}", e)
            null
        } finally {
            conn.disconnect()
        }
    }
}