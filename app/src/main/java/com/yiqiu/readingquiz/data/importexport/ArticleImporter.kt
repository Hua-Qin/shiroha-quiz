package com.yiqiu.readingquiz.data.importexport

import android.util.Log
import com.yiqiu.readingquiz.data.model.Article
import com.yiqiu.readingquiz.data.model.ArticleBlock
import com.yiqiu.readingquiz.data.model.HighlightSpan
import org.json.JSONObject
import java.util.UUID

/**
 * 文章导入（JSON / Markdown / TXT）。
 */
object ArticleImporter {

    private const val TAG = "ArticleImport"

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
        ).also { Log.d(TAG, "importArticleJson: bytes=${raw.length}, id=${it.id}") }
    } catch (e: Exception) {
        Log.w(TAG, "importArticleJson FAILED: ${e.message}", e)
        null
    }

    fun importMarkdown(raw: String, fileName: String): Article = parseMarkdownSections(raw, fileName)

    /**
     * 解析 markdown 文件，按 # / ## / ### 三级构建 ArticleBlock.Section 嵌套树。
     * - level=1（#）作为 Article 顶层 children
     * - level=N (N>1) 嵌套进最近的 level=N-1 Section
     * - 普通段落折叠为 ArticleBlock.Paragraph
     * - 图片 markdown `![alt](url)` → ArticleBlock.Image
     */
    fun parseMarkdownSections(raw: String, fileName: String): Article {
        val lines = raw.lines()
        val titleFromH1 = lines.firstOrNull { it.trimStart().startsWith("# ") }
            ?.removePrefix("#")?.trim()?.ifBlank { null }
        val title = titleFromH1 ?: fileName.substringBeforeLast('.')
        Log.d(TAG, "importMarkdown: file=$fileName, lines=${lines.size}, title='$title'")
        val now = System.currentTimeMillis()
        val topLevel = mutableListOf<ArticleBlock>()
        // 栈：(level, 当前层 container)
        val stack = ArrayDeque<Pair<Int, MutableList<ArticleBlock>>>()
        stack.addLast(1 to topLevel)
        val paragraphBuf = StringBuilder()
        // 章节计数器：为每个 Section 生成稳定 ID（S#01, S#02, ...）
        val sectionCounter = intArrayOf(1)
        fun flushParagraph() {
            if (paragraphBuf.isNotBlank()) {
                val text = paragraphBuf.toString().trim()
                if (text.isNotBlank()) {
                    stack.last().second.add(
                        ArticleBlock.Paragraph(text = text, highlights = emptyList())
                    )
                }
                paragraphBuf.clear()
            }
        }
        for (rawLine in lines) {
            val line = rawLine.trimEnd()
            when {
                line.matches(Regex("^#{1,6}\\s+.*")) -> {
                    flushParagraph()
                    val level = line.takeWhile { it == '#' }.length.coerceAtMost(3)
                    val t = line.removePrefix("#".repeat(line.takeWhile { it == '#' }.length)).trim()
                    // 弹栈直到栈顶 level < level
                    while (stack.size > 1 && stack.last().first >= level) stack.removeLast()
                    val children = mutableListOf<ArticleBlock>()
                    val sectionId = "S#${"%02d".format(sectionCounter[0])}"
                    sectionCounter[0]++
                    val section = ArticleBlock.Section(
                        title = t,
                        level = level,
                        children = children,
                        id = sectionId
                    )
                    stack.last().second.add(section)
                    stack.addLast(level to children)
                    Log.d(TAG, "section L$level: '$t', id=$sectionId")
                }
                line.matches(Regex("^!\\[([^\\]]*)]\\(([^)]+)\\)\\s*$")) -> {
                    flushParagraph()
                    val m = Regex("^!\\[([^\\]]*)]\\(([^)]+)\\)\\s*$").find(line)!!
                    val (alt, url) = m.destructured
                    stack.last().second.add(ArticleBlock.Image(path = url, caption = alt))
                }
                line.isBlank() -> flushParagraph()
                else -> {
                    if (paragraphBuf.isNotEmpty()) paragraphBuf.append('\n')
                    paragraphBuf.append(line)
                }
            }
        }
        flushParagraph()
        return Article(
            id = UUID.randomUUID().toString(),
            title = title,
            author = "",
            source = fileName,
            category = "",
            coverSummary = "",
            blocks = topLevel,
            notes = emptyList(),
            favorite = false,
            createdAt = now,
            updatedAt = now
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
                "section" -> blocks.add(
                    ArticleBlock.Section(
                        title = b.optString("title", ""),
                        level = b.optInt("level", 1),
                        children = buildBlocks(b),  // 递归解析 children
                        id = b.optString("id", "")   // 兼容旧 JSON 无 id
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