package com.yiqiu.readingquiz.data.model

/**
 * 文章主模型。
 * blocks 顺序渲染；highlights 标注核心术语；notes 由用户在阅读页"记笔记"添加。
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