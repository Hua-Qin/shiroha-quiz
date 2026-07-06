package com.yiqiu.readingquiz.data

/**
 * AI 配置（OpenAI 兼容 chat/completions）。
 * 字段命名与 native flavor 保持一致。
 */
data class AiConfig(
    val apiBaseUrl: String,
    val apiKey: String,
    val modelName: String,
    val timeoutSeconds: Int
) {
    companion object {
        val DEFAULT: AiConfig = AiConfig(
            apiBaseUrl = "https://api.openai.com/v1",
            apiKey = "",
            modelName = "gpt-4o-mini",
            timeoutSeconds = 60
        )

        val EMPTY: AiConfig = AiConfig(
            apiBaseUrl = "",
            apiKey = "",
            modelName = "",
            timeoutSeconds = 60
        )
    }
}