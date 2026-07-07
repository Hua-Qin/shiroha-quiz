package com.yiqiu.readingquiz.data.importexport

import com.yiqiu.readingquiz.data.model.ArticleBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ArticleImporter 单元测试（覆盖 JSON / Markdown / TXT 三个解析路径 + 损坏容错）。
 *
 * 运行命令：./gradlew :app:testDebugUnitTest --tests com.yiqiu.readingquiz.data.importexport.ArticleImporterTest
 */
class ArticleImporterTest {

    @Test
    fun `valid JSON parses correctly`() {
        val raw = """
            {
              "title": "Kotlin 入门",
              "author": "JetBrains",
              "category": "编程",
              "blocks": [
                { "type": "section", "title": "走进新语言", "level": 1, "id": "S#01", "children": [
                  { "type": "paragraph", "text": "Kotlin 是一种现代的编程语言。", "highlights": [] }
                ]}
              ]
            }
        """.trimIndent()
        val article = ArticleImporter.importArticleJson(raw)
        assertNotNull("合法 JSON 应解析成功", article)
        article!!
        assertEquals("Kotlin 入门", article.title)
        assertEquals("JetBrains", article.author)
        assertEquals(1, article.blocks.size)
        val section = article.blocks[0] as ArticleBlock.Section
        assertEquals("走进新语言", section.title)
        assertEquals(1, section.level)
        assertEquals("S#01", section.id)
        assertEquals(1, section.children.size)
    }

    @Test
    fun `valid Markdown assigns section ids in order`() {
        val raw = """
            # Kotlin 入门

            欢迎大家进入到 Kotlin 程序设计的学习中。

            ## 走进新语言

            Kotlin 是一种现代但已经成熟的编程语言。

            ### 开发环境配置

            要开发 Kotlin 程序，我们首先需要安装 Java 环境。
        """.trimIndent()
        val article = ArticleImporter.importMarkdown(raw, "kotlin-basics.md")
        // 顶层 1 个 L1 section「Kotlin 入门」
        assertEquals(1, article.blocks.size)
        val l1 = article.blocks[0] as ArticleBlock.Section
        assertEquals(1, l1.level)
        assertEquals("Kotlin 入门", l1.title)
        assertEquals("S#01", l1.id)

        // L1 包含：1 个 L2 section「走进新语言」+ 1 个 paragraph「欢迎大家...」
        val l2List = l1.children.filterIsInstance<ArticleBlock.Section>()
        assertEquals(1, l2List.size)
        val l2 = l2List[0]
        assertEquals(2, l2.level)
        assertEquals("S#02", l2.id)

        // L2 包含：1 个 L3 section「开发环境配置」+ 1 个 paragraph「Kotlin 是...」
        val l3List = l2.children.filterIsInstance<ArticleBlock.Section>()
        assertEquals(1, l3List.size)
        val l3 = l3List[0]
        assertEquals(3, l3.level)
        assertEquals("S#03", l3.id)
    }

    @Test
    fun `plain text imports as paragraphs with filename title`() {
        val raw = "第一段内容。\n\n第二段内容。\n\n第三段内容。"
        val article = ArticleImporter.importPlainText(raw, "sample.txt")
        assertEquals("sample", article.title)  // substringBeforeLast('.')
        assertEquals(3, article.blocks.size)
        article.blocks.forEach { block ->
            assertTrue(block is ArticleBlock.Paragraph)
        }
        assertEquals("第一段内容。", (article.blocks[0] as ArticleBlock.Paragraph).text)
    }

    @Test
    fun `broken JSON returns null without crashing`() {
        val raw = """
            { "title": "缺失字段", "blocks": [ { "type": "paragraph", "text": "未闭合
        """.trimIndent()
        val article = ArticleImporter.importArticleJson(raw)
        assertNull("损坏 JSON 应返回 null", article)
    }

    @Test
    fun `empty plain text returns empty blocks`() {
        val article = ArticleImporter.importPlainText("", "empty.txt")
        assertEquals("empty", article.title)
        assertTrue("空文件应无 blocks", article.blocks.isEmpty())
    }
}