package com.yiqiu.readingquiz.data.model

/**
 * 文章主模型。
 * blocks 顺序渲染（顶层可为 Section / Paragraph / Image）；highlights 标注核心术语；notes 由用户在阅读页"记笔记"添加。
 */
data class Article(
    val id: String,
    val title: String,
    val author: String,
    val source: String,
    val category: String,
    val coverSummary: String,
    val blocks: List<ArticleBlock>,
    val notes: List<ReadingNote>,
    val favorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 章节分组：一篇文章对应一个主章节组（按业务领域归类）。
 * 当前以独立文章为粒度，保留扩展位以便后续做"教程集合 → 多篇文章"的容器。
 */
data class ChapterGroup(
    val title: String,
    val chapters: List<Article>
)