package com.yiqiu.readingquiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * 笔记操作选择菜单（点击「记笔记」图标后弹出）。
 *
 * 提供两个选项：
 * 1. **直接编辑文档**：跳转文章级块编辑（占位为 EditArticleDialog）
 * 2. **新建笔记**：弹出便笺浮窗（NotePadWindow）
 *
 * 设计风格：cafe-ui + Material DropdownMenu 视觉规范
 * - 圆角卡片（CafeRadius.rFrame = 18dp）
 * - 选项行高 56dp，左侧 Icon（24dp）+ 标题 + 副标题
 * - 半透明遮罩 + 点击遮罩关闭
 */
@Composable
fun NoteActionMenu(
    onEditArticle: () -> Unit,
    onCreateNote: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(CafeColors.Surface, RoundedCornerShape(CafeRadius.rFrame))
                    .border(1.dp, CafeColors.Border, RoundedCornerShape(CafeRadius.rFrame))
                    .padding(vertical = 8.dp)
            ) {
                // 标题
                Text(
                    text = "选择操作",
                    style = CafeType.bodyXSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = CafeColors.Muted,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // 选项 1：直接编辑文档
                NoteActionItem(
                    icon = Icons.Rounded.Article,
                    title = "直接编辑文档",
                    subtitle = "修改文章的章节、段落、图片",
                    onClick = {
                        onEditArticle()
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 选项 2：新建笔记
                NoteActionItem(
                    icon = Icons.Rounded.NoteAdd,
                    title = "新建笔记",
                    subtitle = "在便笺浮窗中记录想法（支持加粗、可拖拽）",
                    onClick = {
                        onCreateNote()
                        onDismiss()
                    }
                )
            }
        }
    }
}

/**
 * 菜单中的单个选项行。
 */
@Composable
private fun NoteActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(CafeColors.Bg, RoundedCornerShape(CafeRadius.rMd)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CafeColors.Accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = CafeType.body.copy(fontWeight = FontWeight.Medium),
                color = CafeColors.Fg
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = CafeType.bodyXSmall,
                color = CafeColors.Muted
            )
        }
    }
}