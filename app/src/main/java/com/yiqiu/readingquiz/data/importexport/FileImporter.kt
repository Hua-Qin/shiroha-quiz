package com.yiqiu.readingquiz.data.importexport

import android.content.Context
import android.net.Uri
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.Article

/**
 * 文件导入统一入口。
 * 接收系统文件选择器返回的 Uri，按扩展名分发到 ArticleImporter。
 */
object FileImporter {

    sealed class Result {
        data class Success(val article: Article) : Result()
        data class Failure(val reason: String) : Result()
    }

    fun importFromUri(context: Context, uri: Uri): Result {
        val name = queryDisplayName(context, uri) ?: uri.lastPathSegment ?: "article.txt"
        val raw = readText(context, uri)
            ?: return Result.Failure("无法读取文件：$name")
        val ext = name.substringAfterLast('.', "").lowercase()
        val article = when (ext) {
            "json" -> ArticleImporter.importArticleJson(raw)
            "md", "markdown" -> ArticleImporter.importMarkdown(raw, name)
            else -> ArticleImporter.importPlainText(raw, name)
        } ?: return Result.Failure("解析失败：$name")
        ReadingRepository.addArticle(article)
        return Result.Success(article)
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? = try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) cursor.getString(idx) else null
            } else null
        }
    } catch (_: Throwable) {
        null
    }

    private fun readText(context: Context, uri: Uri): String? = try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
        }
    } catch (_: Throwable) {
        null
    }
}