package com.yiqiu.shirohaquiz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.state.KnowledgeSection
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

/**
 * 文章式学习阅读组件
 *
 * 用于「边学边答」学习阶段，以连续文章流（而非卡片堆叠）展示章节内容：
 *  - 顶部主标题（章节标题）
 *  - 章节上下文（chapterTitle 小字）
 *  - 难度标签
 *  - 摘要块
 *  - 正文（按 \n 分段）
 *  - 代码示例（复用 CodeBlock）
 *  - 代码要点说明（复用 NoticeCard）
 *
 * 不使用 GlassCard 外框，仅内容垂直堆叠，与其他页面风格保持一致。
 */
@Composable
fun ArticleReader(
    section: KnowledgeSection,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ArticleHeader(section = section)
        if (section.summary.isNotBlank()) {
            ArticleSummary(section.summary)
        }
        if (section.content.isNotBlank()) {
            ArticleBody(section.content)
        }
        if (section.codeExample.isNotBlank()) {
            ArticleCodeExample(section.codeExample)
        }
        if (section.codeExplanation.isNotBlank()) {
            ArticleCodeExplanation(section.codeExplanation)
        }
    }
}

@Composable
private fun ArticleHeader(section: KnowledgeSection) {
    Column {
        // 章节上下文（chapterTitle 小字）
        if (section.chapterTitle.isNotBlank() && section.chapterTitle != section.sectionTitle) {
            Text(
                text = section.chapterTitle,
                style = MaterialTheme.typography.bodySmall,
                color = ShirohaColors.TextTertiary
            )
        }
        // 顶部主标题：sectionTitle（字号约 24sp）
        Spacer(modifier = Modifier.height(ShirohaSpacing.Xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = section.sectionTitle.ifBlank { section.id },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    lineHeight = 32.sp
                ),
                fontWeight = FontWeight.SemiBold,
                color = ShirohaColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            // 难度标签（右对齐）
            StatusChip(text = difficultyLabel(section.difficulty), selected = false)
        }
    }
}

@Composable
private fun ArticleSummary(summary: String) {
    // 摘要块：粗体短句，16sp，lineHeight 26sp
    Text(
        text = summary,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 16.sp,
            lineHeight = 26.sp
        ),
        fontWeight = FontWeight.SemiBold,
        color = ShirohaColors.TextPrimary
    )
}

@Composable
private fun ArticleBody(content: String) {
    // 正文：按 \n 拆分多段，bodyLarge，lineHeight 28sp
    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)) {
        content.split("\n").forEach { line ->
            if (line.isNotBlank()) {
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 28.sp
                    ),
                    color = ShirohaColors.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun ArticleCodeExample(code: String) {
    Column {
        // 代码块小标题
        Text(
            text = "代码示例",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = ShirohaColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
        // 复用 CodeBlock
        CodeBlock(code = code)
    }
}

@Composable
private fun ArticleCodeExplanation(explanation: String) {
    Column {
        Text(
            text = "代码要点",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = ShirohaColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
        NoticeCard(text = explanation, warning = false)
    }
}

/**
 * 难度标签本地化
 */
private fun difficultyLabel(d: String): String = when (d.lowercase()) {
    "easy" -> "入门"
    "medium" -> "进阶"
    "hard" -> "深入"
    else -> d
}