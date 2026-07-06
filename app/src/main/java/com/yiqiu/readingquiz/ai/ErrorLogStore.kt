package com.yiqiu.readingquiz.ai

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 内存错误日志存储（环形缓冲，最多 [MAX_SIZE] 条）。
 *
 * 定位：内置调试日志，供 AiSettingsScreen 的「查看错误日志」弹窗读取。
 * 仅内存，App 重启后清空（符合调试定位，无需持久化）。
 * 同时保留 android.util.Log 输出到 logcat 供 adb 抓取。
 */
object ErrorLogStore {

    private const val MAX_SIZE = 50

    data class Entry(
        val timestamp: Long,
        val tag: String,
        val level: String,   // "D" / "I" / "W" / "E"
        val message: String,
        val throwable: String? = null
    )

    val entries: SnapshotStateList<Entry> = mutableStateListOf()

    fun log(tag: String, message: String, level: String = "W", throwable: Throwable? = null) {
        val entry = Entry(
            timestamp = System.currentTimeMillis(),
            tag = tag,
            level = level,
            message = message,
            throwable = throwable?.let { "${it.javaClass.name}: ${it.message}" }
        )
        entries.add(0, entry)  // 倒序：最新的在最前
        while (entries.size > MAX_SIZE) entries.removeAt(entries.size - 1)
    }

    fun clear() {
        entries.clear()
    }

    /**
     * 拼接为可复制的纯文本（带格式化时间戳），用于一键复制到剪贴板。
     */
    fun toClipboardText(): String {
        val sdf = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())
        return entries.joinToString("\n") { e ->
            buildString {
                append("[")
                append(sdf.format(Date(e.timestamp)))
                append("] ")
                append(e.level)
                append("/")
                append(e.tag)
                append(": ")
                append(e.message)
                e.throwable?.let { append("\n    ").append(it) }
            }
        }
    }
}