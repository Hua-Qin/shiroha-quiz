package com.yiqiu.readingquiz.data.importexport

import android.content.Context
import android.net.Uri
import android.util.Log
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.Article

/**
 * 文件导入统一入口。
 * 接收系统文件选择器返回的 Uri，按扩展名分发到 ArticleImporter。
 */
object FileImporter {

    private const val TAG = "FileImport"

    sealed class Result {
        data class Success(val article: Article) : Result()
        data class Failure(val reason: String) : Result()
    }

    fun importFromUri(context: Context, uri: Uri): Result {
        val name = queryDisplayName(context, uri) ?: uri.lastPathSegment ?: "article.txt"
        Log.d(TAG, "importFromUri: uri=$uri, name=$name")
        val raw = readText(context, uri)
            ?: return Result.Failure("无法读取文件：$name").also {
                Log.w(TAG, "import failed: readText returned null for $name")
            }
        val ext = name.substringAfterLast('.', "").lowercase()
        val article = when (ext) {
            "json" -> ArticleImporter.importArticleJson(raw)
            "md", "markdown" -> ArticleImporter.importMarkdown(raw, name)
            else -> ArticleImporter.importPlainText(raw, name)
        } ?: return Result.Failure("解析失败：$name").also {
            Log.w(TAG, "import failed: parse returned null for $name (ext=$ext)")
        }
        ReadingRepository.addArticle(article)
        Log.i(TAG, "imported: '${article.title}' (ext=$ext)")
        return Result.Success(article)
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? = try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) cursor.getString(idx) else null
            } else null
        }
    } catch (e: Throwable) {
        Log.w(TAG, "queryDisplayName exception: ${e.message}", e)
        null
    }

    private fun readText(context: Context, uri: Uri): String? = try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
        }
    } catch (e: Throwable) {
        Log.w(TAG, "readText exception for $uri: ${e.message}", e)
        null
    }
}