package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.state.KnowledgeSection
import com.yiqiu.shirohaquiz.state.MasteryLevel
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

/**
 * 课程章节列表页
 * 显示单门课程的所有章节及学习进度
 */
@Composable
fun StudyCourseScreen(
    courseId: String,
    onOpenSection: (String) -> Unit,
    onBack: () -> Unit
) {
    val course = QuizRepository.courseById(courseId)
    if (course == null) {
        EmptyCourseView(onBack = onBack)
        return
    }
    val sections = QuizRepository.sectionsForCourse(courseId)
    val summary = QuizRepository.courseProgressSummary(courseId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Course",
            title = course.courseName,
            subtitle = course.description.ifBlank { "共 ${sections.size} 个章节" }
        )

        // 总体进度卡片
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(ShirohaSpacing.Lg)) {
                Text(
                    text = "学习进度",
                    style = MaterialTheme.typography.titleSmall,
                    color = ShirohaColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
                Row(horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)) {
                    StatItem(label = "已学章节", value = "${summary.studiedSections}/${summary.totalSections}")
                    StatItem(label = "已练习", value = "${summary.practicedSections}")
                    StatItem(label = "已掌握", value = "${summary.masteredSections}")
                }
                Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
                Row(horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)) {
                    StatItem(label = "总题数", value = "${summary.totalQuestions}")
                    StatItem(
                        label = "正确率",
                        value = if (summary.practicedSections > 0) "${(summary.averageAccuracy * 100).toInt()}%" else "—"
                    )
                }
            }
        }

        // 章节列表
        sections.forEachIndexed { index, section ->
            SectionCard(
                index = index + 1,
                section = section,
                onClick = { onOpenSection(section.id) }
            )
        }

        Spacer(modifier = Modifier.height(ShirohaSpacing.Xl))
    }
}

@Composable
private fun SectionCard(
    index: Int,
    section: KnowledgeSection,
    onClick: () -> Unit
) {
    val progress = QuizRepository.sectionProgress(
        // 注意：通过当前 courseId 调用，使用 stack 读取
        QuizRepository.knowledgeCourses.firstOrNull { c ->
            c.sections.any { it.id == section.id }
        }?.courseId ?: "", section.id
    )
    val mastery = progress.masteryLevel
    val questionCount = QuizRepository.knowledgeCourses
        .firstOrNull { c -> c.sections.any { it.id == section.id } }
        ?.linkedBankId?.let { bankId ->
            QuizRepository.banks.firstOrNull { it.id == bankId }?.questions?.count {
                it.knowledgePoints.contains(section.id) || it.knowledgePoints.contains(section.questionTag)
            }
        } ?: 0

    GlassCard(modifier = Modifier
        .fillMaxWidth()
        .shirohaNoRippleClickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(ShirohaSpacing.Lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 序号圆
                Box(
                    modifier = Modifier
                        .size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(32.dp)) {
                        drawCircle(color = ShirohaColors.BrandPrimarySoft)
                    }
                    Text(
                        text = "$index",
                        style = MaterialTheme.typography.titleSmall,
                        color = ShirohaColors.BrandPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.size(ShirohaSpacing.Md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.sectionTitle.ifBlank { section.id },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ShirohaColors.TextPrimary,
                        maxLines = 2
                    )
                    if (section.chapterTitle.isNotBlank() && section.chapterTitle != section.sectionTitle) {
                        Text(
                            text = section.chapterTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = ShirohaColors.TextTertiary
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "进入章节",
                    tint = ShirohaColors.TextSecondary
                )
            }
            if (section.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
                Text(
                    text = section.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = ShirohaColors.TextSecondary,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
            Row(
                horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(text = difficultyLabel(section.difficulty), selected = false)
                StatusChip(text = "${questionCount} 题", selected = false)
                StatusChip(text = mastery.label, selected = mastery == MasteryLevel.MASTERED)
            }
            if (progress.practiced) {
                Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
                Text(
                    text = "最近正确率 ${(progress.lastAccuracy * 100).toInt()}%（最佳 ${(progress.bestAccuracy * 100).toInt()}%）",
                    style = MaterialTheme.typography.labelSmall,
                    color = masteryColor(mastery)
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ShirohaColors.TextTertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = ShirohaColors.TextPrimary
        )
    }
}

@Composable
private fun EmptyCourseView(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "课程不存在", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
        Text(text = "点击返回", modifier = Modifier.shirohaNoRippleClickable(onClick = onBack))
    }
}

private fun difficultyLabel(d: String): String = when (d.lowercase()) {
    "easy" -> "入门"
    "medium" -> "进阶"
    "hard" -> "深入"
    else -> d
}

private fun masteryColor(level: MasteryLevel): Color = when (level) {
    MasteryLevel.MASTERED -> Color(0xFF10B981)
    MasteryLevel.NEED_REVIEW -> Color(0xFFF59E0B)
    MasteryLevel.NEED_RELEARN -> Color(0xFFEF4444)
    else -> ShirohaColors.TextSecondary
}