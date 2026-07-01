package com.yiqiu.shirohaquiz.ui.screens

import com.yiqiu.shirohaquiz.ui.theme.shirohaEditorialBackground

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EditorialDivider
import com.yiqiu.shirohaquiz.ui.components.EditorialFigure
import com.yiqiu.shirohaquiz.ui.components.EditorialSection
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.editorialScaleFor
import com.yiqiu.shirohaquiz.ui.theme.screenClassFor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 璇剧▼绔犺妭鍒楄〃椤碉紙鏆栬壊缂栬緫鏉傚織椋庨噸鍐欙級
 *
 * 甯冨眬鑷笂鑰屼笅锛? *  1. ShirohaHeader锛坘icker=璇剧▼鍚?/ title=璇剧▼鍚?/ subtitle=鎻忚堪 / 鎬荤珷鑺傛暟锛? *  2. 璇剧▼绠€浠?EditorialSection锛氭弿杩?+ 鍒涘缓鏃堕棿
 *  3. 杩涘害缁熻 EditorialSection锛欵ditorialFigure 脳N 鍛堢幇 宸插绔犺妭 / 宸茬粌涔?/ 宸叉帉鎻?/ 鎬婚鏁?/ 姝ｇ‘鐜? *  4. 绔犺妭鍒楄〃 EditorialSection锛坘icker=Chapters / title=绔犺妭锛夛細姣忕珷鑺?CardWhite86 + EditorialDivider
 *
 * 鏄剧ず鍗曢棬璇剧▼鐨勬墍鏈夌珷鑺傚強瀛︿範杩涘害
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shirohaEditorialBackground()
    ) {
        val screenClass = screenClassFor(maxWidth)
        val scale = editorialScaleFor(screenClass)

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
                subtitle = course.description.ifBlank { "鍏?${sections.size} 涓珷鑺" },
                scale = scale
            )

            // === 璇剧▼绠€浠?EditorialSection锛氭弿杩?+ 鍒涘缓鏃堕棿 ===
            if (course.description.isNotBlank() || course.createdAt > 0L) {
                EditorialSection(
                    kicker = "Overview",
                    title = "璇剧▼绠€浠",
                    scale = scale
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm)) {
                        if (course.description.isNotBlank()) {
                            Text(
                                text = course.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ShirohaColors.TextPrimary
                            )
                        }
                        if (course.createdAt > 0L) {
                            val dateText = rememberOrFormatDate(course.createdAt)
                            Text(
                                text = "鍒涘缓浜?$dateText",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShirohaColors.TextSecondary
                            )
                        }
                    }
                }
            }

            // === 杩涘害缁熻 EditorialSection锛欵ditorialFigure 脳N ===
            EditorialSection(
                kicker = "Progress",
                title = "瀛︿範杩涘害",
                scale = scale
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
                    ) {
                        EditorialFigure(
                            modifier = Modifier.weight(1f),
                            scale = scale,
                            value = "${summary.studiedSections}",
                            label = "宸插绔犺妭",
                            unit = "/ ${summary.totalSections}"
                        )
                        EditorialFigure(
                            modifier = Modifier.weight(1f),
                            scale = scale,
                            value = "${summary.practicedSections}",
                            label = "宸茬粌涔"
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
                    ) {
                        EditorialFigure(
                            modifier = Modifier.weight(1f),
                            scale = scale,
                            value = "${summary.masteredSections}",
                            label = "宸叉帉鎻",
                            unit = "鑺"
                        )
                        EditorialFigure(
                            modifier = Modifier.weight(1f),
                            scale = scale,
                            value = "${summary.totalQuestions}",
                            label = "鎬婚鏁",
                            unit = "棰"
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
                    ) {
                        EditorialFigure(
                            modifier = Modifier.weight(1f),
                            scale = scale,
                            value = if (summary.practicedSections > 0) {
                                "${(summary.averageAccuracy * 100).toInt()}"
                            } else "鈥",
                            label = "姝ｇ‘鐜",
                            unit = if (summary.practicedSections > 0) "%" else ""
                        )
                    }
                }
            }

            // === 绔犺妭鍒楄〃 EditorialSection锛氭瘡绔犺妭 CardWhite86 + EditorialDivider ===
            EditorialSection(
                kicker = "Chapters",
                title = "绔犺妭",
                scale = scale
            ) {
                sections.forEachIndexed { index, section ->
                    SectionCard(
                        index = index + 1,
                        section = section,
                        onClick = { onOpenSection(section.id) }
                    )
                    if (index < sections.lastIndex) {
                        EditorialDivider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(ShirohaSpacing.Xl))
        }
    }
}

/**
 * 绔犺妭鍒楄〃椤癸細CardWhite86 鍗＄墖 + 鐘舵€$chip + 鏍囬 + 鎻忚堪 + ActionPillButton
 */
@Composable
private fun SectionCard(
    index: Int,
    section: KnowledgeSection,
    onClick: () -> Unit
) {
    val progress = QuizRepository.sectionProgress(
        // 娉ㄦ剰锛氶€氳繃褰撳墠 courseId 璋冪敤锛屼娇鐢$stack 璇诲彇
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shirohaNoRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(ShirohaRadius.Md),
        color = ShirohaColors.CardWhite86,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
    ) {
        Column(modifier = Modifier.padding(ShirohaSpacing.Lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 搴忓彿鍦"                Box(
                    modifier = Modifier
                        .size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(32.dp)) {
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
            // 鐘舵€$chip 琛岋細闅惧害 + 棰樼洰鏁?+ 鎺屾彙搴?            Row(
                horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(text = difficultyLabel(section.difficulty), selected = false)
                StatusChip(text = "$questionCount 棰", selected = false)
                StatusChip(text = mastery.label, selected = mastery == MasteryLevel.MASTERED)
            }
            if (progress.practiced) {
                Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
                Text(
                    text = "鏈€杩戞纭巼 ${(progress.lastAccuracy * 100).toInt()}%锛堟渶浣?${(progress.bestAccuracy * 100).toInt()}%锛",
                    style = MaterialTheme.typography.labelSmall,
                    color = masteryColor(mastery)
                )
            }
            Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
            // 杩涘叆绔犺妭鎸夐挳
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = "寮€濮嬪涔",
                primary = true,
                enabled = true,
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EmptyCourseView(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "璇剧▼涓嶅瓨鍦", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
        Text(text = "鐐瑰嚮杩斿洖", modifier = Modifier.shirohaNoRippleClickable(onClick = onBack))
    }
}

private fun difficultyLabel(d: String): String = when (d.lowercase()) {
    "easy" -> "鍏ラ棬"
    "medium" -> "杩涢樁"
    "hard" -> "娣卞叆"
    else -> d
}

private fun masteryColor(level: MasteryLevel): Color = when (level) {
    MasteryLevel.MASTERED -> Color(0xFF10B981)
    MasteryLevel.NEED_REVIEW -> Color(0xFFF59E0B)
    MasteryLevel.NEED_RELEARN -> Color(0xFFEF4444)
    else -> ShirohaColors.TextSecondary
}

private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

/**
 * 绠€鍗曟棩鏈熸牸寮忓寲锛堜笉浣跨敤 remember锛屽洜涓?createdAt 鍦ㄥ閮$BoxWithConstraints 閲嶇粍鏃跺凡鏄ǔ瀹氬€硷級
 */
private fun rememberOrFormatDate(timestamp: Long): String {
    return dateFormat.format(Date(timestamp))
}
