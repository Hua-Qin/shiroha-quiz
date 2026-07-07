package com.yiqiu.readingquiz.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.ReadingNote
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeType
import java.util.UUID
import kotlin.math.roundToInt

private const val TAG = "NotePad"

/**
 * 一条带格式（加粗）的笔记片段，用 [SpanStyle.Bold] 标记加粗段。
 * 比 ReadingNote 多了 boldRanges 字段；保存到 Repository 时只持久化 content（Markdown **xx** 语法）。
 */
data class NoteSegment(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val boldRanges: List<IntRange> = emptyList()
) {
    fun isEmpty(): Boolean = text.isBlank()
}

/**
 * 笔记浮窗（独立的悬浮窗口形式）。
 *
 * 核心特性：
 * - **可拖拽移动**：顶部拖拽手柄 + detectDragGestures 实现（屏幕内边界限制）
 * - **支持字体加粗**：选中文本 → 加粗按钮 → 应用 SpanStyle.Bold 到所选区间
 * - **基础文本编辑**：BasicTextField + 自由换行 + 退格删除
 * - **视觉设计**：浅黄背景（纸张质感）+ 顶角撕边效果（细线分隔）+ 圆角卡片 + 阴影
 *
 * 设计参数：
 * - 默认尺寸 280dp × 320dp
 * - 屏幕内边界自适应（拖拽时自动 clamp 到屏幕范围）
 *
 * @param articleId 当前文章 ID（笔记归属）
 * @param onDismiss 关闭浮窗回调
 */
@Composable
fun NotePadWindow(
    articleId: String,
    onDismiss: () -> Unit
) {
    // 屏幕尺寸（用于拖拽边界计算）
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val padWidthPx = with(density) { 280.dp.toPx() }
    val padHeightPx = with(density) { 320.dp.toPx() }

    // 浮窗位置（屏幕坐标系），初始位置：右下角
    var offsetX by remember { mutableStateOf(screenWidthPx - padWidthPx - with(density) { 24.dp.toPx() }) }
    var offsetY by remember { mutableStateOf(screenHeightPx - padHeightPx - with(density) { 120.dp.toPx() }) }

    // 笔记内容：用 AnnotatedString 支持加粗 span
    var contentValue by remember { mutableStateOf(TextFieldValue(AnnotatedString(""))) }

    // 现有笔记列表（用于展示历史）
    val existingNotes = remember { mutableStateListOf<ReadingNote>() }
    LaunchedEffect(articleId) {
        existingNotes.clear()
        existingNotes.addAll(ReadingRepository.notesForArticle(articleId))
        Log.d(TAG, "NotePadWindow open: articleId=$articleId, existing=${existingNotes.size}")
    }

    Dialog(
        onDismissRequest = onDismiss,
        // 使用全屏 Dialog 但只渲染一个浮窗卡片
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        // 全屏透明背景 + 浮窗卡片（绝对定位）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // 浮窗主体（带拖拽偏移）
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(width = 280.dp, height = 320.dp)
            ) {
                NotePadCard(
                    contentValue = contentValue,
                    onContentChange = { contentValue = it },
                    onSave = {
                        val text = contentValue.text.trim()
                        if (text.isNotBlank()) {
                            val annotated = contentValue.annotatedString
                            val note = ReadingNote(
                                id = UUID.randomUUID().toString(),
                                articleId = articleId,
                                content = annotatedToMarkdown(annotated),
                                anchorText = "",
                                createdAt = System.currentTimeMillis()
                            )
                            ReadingRepository.addNote(note)
                            existingNotes.add(0, note)
                            contentValue = TextFieldValue(AnnotatedString(""))
                            Log.d(TAG, "note saved: articleId=$articleId, len=${text.length}, boldSpans=${annotated.spanStyles.size}")
                        }
                    },
                    onDeleteExisting = { noteId ->
                        ReadingRepository.deleteNote(noteId)
                        existingNotes.removeAll { it.id == noteId }
                        Log.d(TAG, "note deleted: id=$noteId")
                    },
                    existingNotes = existingNotes,
                    onClose = onDismiss,
                    onDrag = { delta ->
                        val newX = (offsetX + delta.x).coerceIn(0f, screenWidthPx - padWidthPx)
                        val newY = (offsetY + delta.y).coerceIn(0f, screenHeightPx - padHeightPx)
                        offsetX = newX
                        offsetY = newY
                    }
                )
            }
        }
    }
}

/**
 * 浮窗卡片 UI（拖拽手柄 + 编辑器 + 加粗按钮 + 历史列表 + 操作栏）。
 */
@Composable
private fun NotePadCard(
    contentValue: TextFieldValue,
    onContentChange: (TextFieldValue) -> Unit,
    onSave: () -> Unit,
    onDeleteExisting: (String) -> Unit,
    existingNotes: SnapshotStateList<ReadingNote>,
    onClose: () -> Unit,
    onDrag: (Offset) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // 纸张质感：浅米黄背景 + 淡边框 + 圆角 + 微阴影
            .background(NotePadPaperBg, shape = RoundedCornerShape(CafeRadius.Frame))
            .border(1.dp, NotePadPaperBorder, RoundedCornerShape(CafeRadius.Frame))
            .padding(8.dp)
    ) {
        // 顶栏：拖拽手柄 + 标题 + 关闭按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                // 拖拽手柄区（仅前 40dp 区域响应拖拽，避免误触关闭按钮）
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.DragIndicator,
                contentDescription = "拖拽移动笔记",
                tint = CafeColors.Muted,
                modifier = Modifier.padding(horizontal = 4.dp).size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "便笺",
                style = CafeType.Caption.copy(fontWeight = FontWeight.SemiBold),
                color = CafeColors.Fg
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "关闭便笺",
                    tint = CafeColors.Muted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 撕边分隔线（双线效果）
        Row(
            modifier = Modifier.fillMaxWidth().height(2.dp).padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(CafeColors.Border)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(CafeColors.Border)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 编辑器（带加粗支持的 BasicTextField）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .weight(1f)
                .background(NotePadEditorBg, RoundedCornerShape(CafeRadius.Sm))
                .border(0.5.dp, CafeColors.Border, RoundedCornerShape(CafeRadius.Sm))
                .padding(8.dp)
        ) {
            BasicTextField(
                value = contentValue,
                onValueChange = onContentChange,
                modifier = Modifier.fillMaxSize(),
                textStyle = CafeType.Body.copy(color = CafeColors.Fg),
                cursorBrush = SolidColor(CafeColors.Accent),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (contentValue.text.isEmpty()) {
                            Text(
                                text = "在这里输入笔记…",
                                style = CafeType.Body,
                                color = CafeColors.Muted
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 工具栏：加粗按钮 + 保存按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 加粗按钮（切换选中文本的 Bold span）
            IconButton(
                onClick = { onContentChange(applyBoldToSelection(contentValue)) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FormatBold,
                    contentDescription = "加粗选中文本",
                    tint = CafeColors.Accent
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 现有笔记计数
            if (existingNotes.isNotEmpty()) {
                Text(
                    text = "已存 ${existingNotes.size} 条",
                    style = CafeType.Caption,
                    color = CafeColors.Muted
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // 保存按钮
            TextButton(
                onClick = onSave,
                enabled = contentValue.text.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Save,
                    contentDescription = null,
                    tint = if (contentValue.text.isNotBlank()) CafeColors.Accent else CafeColors.Neutral
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "保存",
                    style = CafeType.Caption,
                    color = if (contentValue.text.isNotBlank()) CafeColors.Accent else CafeColors.Neutral
                )
            }
        }

        // 现有笔记列表（精简展示，最多 3 条 + 横向滚动）
        if (existingNotes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 70.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(existingNotes.take(5)) { note ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = note.content.take(40) + if (note.content.length > 40) "…" else "",
                                style = CafeType.Caption,
                                color = CafeColors.Muted,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteExisting(note.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "删除笔记",
                                    tint = CafeColors.Wrong,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 对选中文本应用加粗 span。
 * - 无选区：忽略
 * - 选区已有 Bold span：切换为非 Bold（取消加粗）
 * - 选区无 Bold span：添加 Bold span
 */
private fun applyBoldToSelection(value: TextFieldValue): TextFieldValue {
    val selection = value.selection
    if (selection.collapsed) return value  // 无选区

    val start = selection.min
    val end = selection.max
    val annotated = value.annotatedString
    // 与选区重叠的 span 列表（用手动构造的 IntRange，避免 1.9.x AnnotatedString.Range.range 缺失问题）
    val existingOverlaps: List<IntRange> = annotated.spanStyles
        .filter { span -> span.start < end && span.end > start }
        .map { span -> IntRange(span.start, span.end - 1) }

    // 判断选区是否已全部加粗
    val isAllBold = (start until end).all { idx ->
        existingOverlaps.any { range -> idx >= range.first && idx <= range.last }
    }

    val newStyles = mutableListOf<AnnotatedString.Range<SpanStyle>>()
    newStyles.addAll(annotated.spanStyles)
    if (isAllBold) {
        // 取消加粗
        newStyles.removeAll { span -> span.start < end && span.end > start }
    } else {
        // 添加加粗
        newStyles.add(
            AnnotatedString.Range(
                item = SpanStyle(fontWeight = FontWeight.Bold),
                start = start,
                end = end
            )
        )
    }

    // 合并重叠的 Bold span（保持视觉一致）
    val merged = mergeBoldSpans(newStyles)

    val newAnnotated = AnnotatedString(value.text, merged)
    return TextFieldValue(
        annotatedString = newAnnotated,
        selection = TextRange(start, end)
    )
}

/**
 * 合并重叠 / 相邻的 Bold span。
 */
private fun mergeBoldSpans(spans: List<AnnotatedString.Range<SpanStyle>>): List<AnnotatedString.Range<SpanStyle>> {
    val boldSpans = spans.filter { it.item.fontWeight == FontWeight.Bold }
        .sortedBy { it.start }
    if (boldSpans.isEmpty()) return spans.filter { it.item.fontWeight != FontWeight.Bold }

    val merged = mutableListOf<AnnotatedString.Range<SpanStyle>>()
    var current = boldSpans[0]
    for (i in 1 until boldSpans.size) {
        val next = boldSpans[i]
        if (next.start <= current.end) {
            // 合并
            current = AnnotatedString.Range(
                item = SpanStyle(fontWeight = FontWeight.Bold),
                start = current.start,
                end = maxOf(current.end, next.end)
            )
        } else {
            merged.add(current)
            current = next
        }
    }
    merged.add(current)

    return merged + spans.filter { it.item.fontWeight != FontWeight.Bold }
}

/**
 * 将 AnnotatedString 中的 Bold span 序列化为 Markdown **xx** 语法（持久化到 ReadingNote.content）。
 * 解析时反向转换在 ReadingRepository / Reader 端按需实现（本版本仅持久化）。
 */
private fun annotatedToMarkdown(annotated: AnnotatedString): String {
    val text = annotated.text
    if (annotated.spanStyles.isEmpty()) return text

    // 提取所有 Bold span 并按 start 排序
    val boldSpans = annotated.spanStyles
        .filter { it.item.fontWeight == FontWeight.Bold }
        .sortedBy { it.start }

    val sb = StringBuilder()
    var cursor = 0
    for (span in boldSpans) {
        val safeStart = span.start.coerceAtLeast(0).coerceAtMost(text.length)
        val safeEnd = span.end.coerceAtLeast(safeStart).coerceAtMost(text.length)
        if (cursor < safeStart) sb.append(text.substring(cursor, safeStart))
        sb.append("**").append(text.substring(safeStart, safeEnd)).append("**")
        cursor = safeEnd
    }
    if (cursor < text.length) sb.append(text.substring(cursor))
    return sb.toString()
}

// 笔记浮窗主题色（独立于 CafeColors，体现纸张质感）
private val NotePadPaperBg: Color = Color(0xFFFFF8E7)        // 浅米黄纸张
private val NotePadPaperBorder: Color = Color(0xFFE6D9B8)   // 纸张淡边框
private val NotePadEditorBg: Color = Color(0xFFFFFBEF)       // 编辑区略浅

/**
 * 便利函数：在 Composable 中直接弹窗显示笔记浮窗（封装 Dialog 状态管理）。
 * 用于 ReadingScreen / QuizScreen 调用。
 *
 * 用法：
 * ```
 * var showNotePad by remember { mutableStateOf(false) }
 * if (showNotePad) {
 *     NotePadDialog(articleId = article.id, onDismiss = { showNotePad = false })
 * }
 * ```
 */
@Composable
fun NotePadDialog(articleId: String, onDismiss: () -> Unit) {
    NotePadWindow(articleId = articleId, onDismiss = onDismiss)
}

/**
 * 「直接编辑文档」入口（占位 - 完整功能可后续接入文章块编辑）。
 * 当前实现：弹出占位 AlertDialog，提示用户「文章编辑功能即将上线」。
 *
 * 设计意图：让 Edit 按钮的二选一菜单（"直接编辑文档" / "新建笔记"）有完整实现，
 * 其中「新建笔记」调用 NotePadWindow，「直接编辑文档」调用本函数。
 */
@Composable
fun EditArticleDialog(
    articleTitle: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("直接编辑文档") },
        text = {
            Column {
                Text(
                    text = "正在打开「$articleTitle」的文档编辑器…",
                    style = CafeType.Body,
                    color = CafeColors.Fg
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "（提示：文档块编辑功能正在开发中，当前可使用「新建笔记」记录片段想法。）",
                    style = CafeType.Caption,
                    color = CafeColors.Muted
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了")
            }
        }
    )
}