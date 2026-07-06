package com.yiqiu.readingquiz.data.importexport

import com.yiqiu.readingquiz.data.model.Article
import com.yiqiu.readingquiz.data.model.ArticleBlock
import com.yiqiu.readingquiz.data.model.HighlightSpan
import org.json.JSONObject
import java.util.UUID

/**
 * 文章导入（JSON / Markdown / TXT）。
 */
object ArticleImporter {

    fun importArticleJson(raw: String): Article? = try {
        val obj = JSONObject(raw)
        val now = System.currentTimeMillis()
        Article(
            id = obj.optString("id", UUID.randomUUID().toString()),
            title = obj.optString("title", "未命名文章"),
            author = obj.optString("author", ""),
            source = obj.optString("source", ""),
            category = obj.optString("category", ""),
            coverSummary = obj.optString("coverSummary", ""),
            blocks = buildBlocks(obj),
            notes = emptyList(),
            favorite = obj.optBoolean("favorite", false),
            createdAt = obj.optLong("createdAt", now),
            updatedAt = obj.optLong("updatedAt", now)
        )
    } catch (_: Exception) {
        null
    }

    fun importMarkdown(raw: String, fileName: String): Article {
        val lines = raw.lines()
        val title = lines.firstOrNull()?.removePrefix("#")?.trim()?.ifBlank { null }
            ?: fileName.substringBeforeLast('.')
        val body = if (title != null && lines.firstOrNull()?.startsWith("#") == true) {
            lines.drop(1)
        } else {
            lines
        }
        val paragraphs = body.joinToString("\n").split(Regex("\\n\\s*\\n"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { ArticleBlock.Paragraph(text = it, highlights = emptyList<HighlightSpan>()) }
        return Article(
            id = UUID.randomUUID().toString(),
            title = title,
            author = "",
            source = fileName,
            category = "",
            coverSummary = "",
            blocks = paragraphs,
            notes = emptyList(),
            favorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun importPlainText(raw: String, fileName: String): Article {
        val paragraphs = raw.split(Regex("\\n\\s*\\n"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { ArticleBlock.Paragraph(text = it, highlights = emptyList<HighlightSpan>()) }
        val title = fileName.substringBeforeLast('.')
        return Article(
            id = UUID.randomUUID().toString(),
            title = title,
            author = "",
            source = fileName,
            category = "",
            coverSummary = "",
            blocks = paragraphs,
            notes = emptyList(),
            favorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun buildBlocks(obj: JSONObject): List<ArticleBlock> {
        val arr = obj.optJSONArray("blocks") ?: return emptyList()
        val blocks = mutableListOf<ArticleBlock>()
        for (i in 0 until arr.length()) {
            val b = arr.getJSONObject(i)
            when (b.optString("type", "paragraph")) {
                "image" -> blocks.add(
                    ArticleBlock.Image(
                        path = b.optString("path", ""),
                        caption = b.optString("caption", "")
                    )
                )
                else -> {
                    val hl = b.optJSONArray("highlights")
                    val highlights = mutableListOf<HighlightSpan>()
                    if (hl != null) {
                        for (j in 0 until hl.length()) {
                            val h = hl.getJSONObject(j)
                            highlights.add(
                                HighlightSpan(
                                    text = h.optString("text", ""),
                                    startIndex = h.optInt("startIndex", 0),
                                    endIndex = h.optInt("endIndex", 0),
                                    explanation = h.optString("explanation", "")
                                )
                            )
                        }
                    }
                    blocks.add(
                        ArticleBlock.Paragraph(
                            text = b.optString("text", ""),
                            highlights = highlights
                        )
                    )
                }
            }
        }
        return blocks
    }
}