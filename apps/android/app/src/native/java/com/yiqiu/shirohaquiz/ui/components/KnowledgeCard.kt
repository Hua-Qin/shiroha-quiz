package com.yiqiu.shirohaquiz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.state.KnowledgeSection
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

/**
 * 知识点讲解卡片
 * 用于「边学边答」中展示章节的知识点内容
 * 可折叠：折叠态只显示标题与摘要，展开态显示完整讲解+代码示例
 */
@Composable
fun KnowledgeCard(
    section: KnowledgeSection,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(ShirohaSpacing.Lg)) {
            // 头部：章节标题 + 难度标签 + 展开按钮
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp)) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(color = androidx.compose.ui.graphics.Color(0xFF4F7CFF))
                    }
                }
                Spacer(modifier = Modifier.size(ShirohaSpacing.Sm))
                Text(
                    text = section.sectionTitle.ifBlank { section.id },
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ShirohaColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(text = difficultyLabel(section.difficulty), selected = false)
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (expanded) "收起" else "展开",
                        tint = ShirohaColors.TextSecondary
                    )
                }
            }

            // 章节大标题（如果存在）
            if (section.chapterTitle.isNotBlank() && section.chapterTitle != section.sectionTitle) {
                Text(
                    text = section.chapterTitle,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = ShirohaColors.TextTertiary
                )
            }

            // 折叠态：仅显示摘要
            if (!expanded) {
                if (section.summary.isNotBlank()) {
                    Text(
                        text = section.summary,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = ShirohaColors.TextSecondary,
                        maxLines = 2
                    )
                }
                return@Column
            }

            // 展开态：完整内容
            Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))

            if (section.summary.isNotBlank()) {
                Text(
                    text = section.summary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = ShirohaColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
            }

            // 详细讲解（按 \n 拆行）
            if (section.content.isNotBlank()) {
                section.content.split("\n").forEach { line ->
                    if (line.isBlank()) {
                        Spacer(modifier = Modifier.height(ShirohaSpacing.Xs))
                    } else {
                        Text(
                            text = line,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = ShirohaColors.TextPrimary,
                            lineHeight = 22.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
            }

            // 代码示例
            AnimatedVisibility(visible = section.codeExample.isNotBlank()) {
                Column {
                    Text(
                        text = "代码示例",
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                        color = ShirohaColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
                    CodeBlock(code = section.codeExample)
                    Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
                }
            }

            // 代码说明
            AnimatedVisibility(visible = section.codeExplanation.isNotBlank()) {
                Column {
                    NoticeCard(text = section.codeExplanation, warning = false)
                }
            }
        }
    }
}

private fun difficultyLabel(d: String): String = when (d.lowercase()) {
    "easy" -> "入门"
    "medium" -> "进阶"
    "hard" -> "深入"
    else -> d
}