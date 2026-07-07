package com.yiqiu.readingquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.ReadingNote
import com.yiqiu.readingquiz.ui.components.CafeBadge
import com.yiqiu.readingquiz.ui.components.CafeBadgeVariant
import com.yiqiu.readingquiz.ui.components.CafeButton
import com.yiqiu.readingquiz.ui.components.CafeButtonVariant
import com.yiqiu.readingquiz.ui.components.CafeCard
import com.yiqiu.readingquiz.ui.components.CafeEyebrow
import com.yiqiu.readingquiz.ui.components.CafeTopBar
import com.yiqiu.readingquiz.ui.components.NotePadDialog
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * 全局笔记列表页（统一入口，随时查看所有笔记）。
 *
 * - 顶部 CafeTopBar 返回 + 标题
 * - 按文章分组展示笔记（空态提示）
 * - 点击笔记卡片 → 打开对应文章的 NotePadDialog 编辑
 * - 长按 / 删除按钮 → 确认删除
 */
@Composable
fun NotesScreen(
    onBack: () -> Unit,
    onOpenArticle: (String) -> Unit = {}
) {
    val allNotes = remember { ReadingRepository.notes.toList() }
    var pendingDelete by remember { mutableStateOf<ReadingNote?>(null) }
    var editingNoteId by remember { mutableStateOf<String?>(null) }
    var targetArticleId by remember { mutableStateOf<String?>(null) }

    // 按文章分组：Map<articleId, List<ReadingNote>>
    val grouped = remember(allNotes) {
        allNotes.groupBy { it.articleId }
            .toList()
            .sortedByDescending { (_, notes) -> notes.maxOfOrNull { it.createdAt } ?: 0L }
    }

    // 笔记编辑浮窗（复用现有 NotePadDialog）
    if (editingNoteId != null && targetArticleId != null) {
        NotePadDialog(
            articleId = targetArticleId!!,
            onDismiss = {
                editingNoteId = null
                targetArticleId = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeColors.Bg)
    ) {
        CafeTopBar(title = "我的笔记", onBack = onBack)

        if (allNotes.isEmpty()) {
            EmptyNotesState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = CafeSpacing.containerPad,
                    vertical = CafeSpacing.md
                ),
                verticalArrangement = Arrangement.spacedBy(CafeSpacing.cardPadSm)
            ) {
                grouped.forEach { (articleId, notes) ->
                    val article = ReadingRepository.getArticle(articleId)
                    val articleTitle = article?.title ?: "未知文章"
                    item(key = "header-$articleId") {
                        Column(
                            modifier = Modifier.padding(vertical = CafeSpacing.xs),
                            verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                        ) {
                            CafeEyebrow(
                                text = articleTitle,
                                showLeadingDot = true
                            )
                            CafeBadge(
                                text = "${notes.size} 条笔记",
                                variant = CafeBadgeVariant.Up
                            )
                        }
                    }
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onEdit = {
                                editingNoteId = note.id
                                targetArticleId = note.articleId
                            },
                            onDelete = { pendingDelete = note },
                            onOpenArticle = onOpenArticle
                        )
                    }
                }
            }
        }
    }

    // 删除确认
    pendingDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除这条笔记？", style = CafeType.cardTitle, color = CafeColors.Fg) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
                    Text(
                        text = note.content.take(120) + if (note.content.length > 120) "…" else "",
                        style = CafeType.body,
                        color = CafeColors.Fg
                    )
                    Text(
                        text = "该操作不可撤销。",
                        style = CafeType.meta,
                        color = CafeColors.Muted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    ReadingRepository.deleteNote(note.id)
                    pendingDelete = null
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = CafeColors.Wrong
                    )
                    Text("删除", color = CafeColors.Wrong)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("取消", color = CafeColors.Muted)
                }
            }
        )
    }
}

/**
 * 单条笔记卡片（内容预览 + 操作）。
 */
@Composable
private fun NoteCard(
    note: ReadingNote,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenArticle: (String) -> Unit
) {
    CafeCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onOpenArticle(note.articleId) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
        ) {
            // 顶部：时间 + 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
            ) {
                Text(
                    text = note.anchorText.ifBlank { "笔记 · ${note.content.take(20)}" },
                    style = CafeType.meta,
                    color = CafeColors.Muted,
                    modifier = Modifier.weight(1f)
                )
                CafeButton(
                    text = "编辑",
                    onClick = onEdit,
                    variant = CafeButtonVariant.Ghost,
                    modifier = Modifier.padding(end = CafeSpacing.xs)
                )
                CafeButton(
                    text = "删除",
                    onClick = onDelete,
                    variant = CafeButtonVariant.Ghost
                )
            }
            // 内容
            Text(
                text = note.content,
                style = CafeType.body,
                color = CafeColors.Fg
            )
        }
    }
}

/**
 * 空态提示。
 */
@Composable
private fun EmptyNotesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CafeSpacing.containerPad),
        verticalArrangement = Arrangement.spacedBy(CafeSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CafeCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "暂无笔记",
                    style = CafeType.cardTitle,
                    color = CafeColors.Fg
                )
                Text(
                    text = "进入文章阅读页，点击编辑按钮即可创建笔记。",
                    style = CafeType.bodySmall,
                    color = CafeColors.Muted
                )
            }
        }
    }
}
