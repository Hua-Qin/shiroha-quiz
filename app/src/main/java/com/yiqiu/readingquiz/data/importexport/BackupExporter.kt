package com.yiqiu.readingquiz.data.importexport

import com.yiqiu.readingquiz.data.ReadingRepository
import org.json.JSONArray
import org.json.JSONObject

/**
 * 备份导出（参考 native buildBackupPayloadV23 结构）。
 * 注意：AI API Key 不包含在导出中。
 */
object BackupExporter {

    fun exportFullBackupJson(): String {
        val articlesArr = JSONArray()
        ReadingRepository.articles.forEach { a ->
            articlesArr.put(JSONObject()
                .put("id", a.id)
                .put("title", a.title)
                .put("author", a.author)
                .put("source", a.source)
                .put("category", a.category)
                .put("coverSummary", a.coverSummary)
                .put("favorite", a.favorite)
                .put("createdAt", a.createdAt)
                .put("updatedAt", a.updatedAt))
        }
        val notesArr = JSONArray()
        ReadingRepository.notes.forEach { n ->
            notesArr.put(JSONObject()
                .put("id", n.id)
                .put("articleId", n.articleId)
                .put("content", n.content)
                .put("anchorText", n.anchorText)
                .put("createdAt", n.createdAt))
        }
        val sessionsArr = JSONArray()
        ReadingRepository.sessions.forEach { s ->
            sessionsArr.put(JSONObject()
                .put("id", s.id)
                .put("articleId", s.articleId)
                .put("currentIndex", s.currentIndex)
                .put("durationMs", s.durationMs)
                .put("completed", s.completed)
                .put("startedAt", s.startedAt))
        }
        val payload = JSONObject()
            .put("app", "ReadingQuiz")
            .put("kind", "full-backup")
            .put("appVersion", "0.2.0-alpha")
            .put("exportedAt", System.currentTimeMillis())
            .put("articles", articlesArr)
            .put("notes", notesArr)
            .put("sessions", sessionsArr)
            // questionsList: 扁平 JSON array，每条自带 articleId（统一题库）
            .put("questionsList", JSONArray().apply {
                ReadingRepository.allQuestions().forEach { q ->
                    put(JSONObject()
                        .put("id", q.id)
                        .put("type", q.type.name)
                        .put("question", q.question)
                        .put("answer", JSONArray(q.answer))
                        .put("blankAnswers", JSONArray(q.blankAnswers))
                        .put("explanation", q.explanation)
                        .put("articleId", q.articleId)
                        // 新增字段：章节绑定（如为 null 则 JSON 中省略，反序列化时兼容）
                        .put("sectionId", q.sectionId ?: JSONObject.NULL)
                        .put("anchorText", q.anchorText))
                }
            })
            // ⚠️ AI API Key 故意不在导出中
        return payload.toString(2)
    }

    fun exportArticleJson(articleId: String): String? {
        val article = ReadingRepository.articles.firstOrNull { it.id == articleId } ?: return null
        val obj = JSONObject()
            .put("id", article.id)
            .put("title", article.title)
            .put("author", article.author)
            .put("source", article.source)
            .put("category", article.category)
            .put("coverSummary", article.coverSummary)
            .put("favorite", article.favorite)
            .put("createdAt", article.createdAt)
            .put("updatedAt", article.updatedAt)
        return obj.toString(2)
    }
}