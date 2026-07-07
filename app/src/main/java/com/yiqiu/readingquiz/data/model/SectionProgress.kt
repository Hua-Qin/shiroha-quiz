package com.yiqiu.readingquiz.data.model

/**
 * 章节学习进度（按 articleId + sectionId 维度统计）。
 *
 * - completed：当前章节所有题目答完且通过标记
 * - wrongCount：累计答错题数（多次答错同一题累加）
 * - unansweredCount：累计未答题数
 * - lastUpdated：最近一次更新时间（毫秒时间戳）
 *
 * key 格式：`"${articleId}#${sectionId}"`
 */
data class SectionProgress(
    val articleId: String,
    val sectionId: String,
    val completed: Boolean = false,
    val wrongCount: Int = 0,
    val unansweredCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)