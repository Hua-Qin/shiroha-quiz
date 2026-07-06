package com.yiqiu.readingquiz.data.model

data class ReadingNote(
    val id: String,
    val articleId: String,
    val content: String,
    val anchorText: String,
    val createdAt: Long
)