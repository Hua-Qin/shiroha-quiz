package com.yiqiu.readingquiz.data.model

/**
 * 正文片段密封类。
 * Paragraph 携带若干 HighlightSpan（核心术语 + 释义，用于点击弹气泡）。
 * Image 携带资产路径和图注。
 */
sealed class ArticleBlock {
    data class Paragraph(
        val text: String,
        val highlights: List<HighlightSpan>
    ) : ArticleBlock()

    data class Image(
        val path: String,
        val caption: String
    ) : ArticleBlock()
}

data class HighlightSpan(
    val text: String,
    val startIndex: Int,
    val endIndex: Int,
    val explanation: String
)