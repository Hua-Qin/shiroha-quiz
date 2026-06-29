package com.yiqiu.shirohaquiz.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.state.KnowledgeCourse
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EmptyStateIllustration
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 学习主页：显示已导入的课程列表
 * 支持导入课程包（JSON 文件，含 course + questions 节点）
 */
@Composable
fun StudyScreen(
    onOpenCourse: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var statusText by remember { mutableStateOf("") }
    var isStatusWarn by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }

    val courses = QuizRepository.knowledgeCourses

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        if (isImporting) return@rememberLauncherForActivityResult
        isImporting = true
        statusText = "正在解析课程包…"
        isStatusWarn = false
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: return@runCatching "无法读取文件"
                    val text = String(bytes, Charsets.UTF_8)
                    QuizRepository.importCoursePackage(context, text)
                }.getOrElse { e -> "导入失败：${e.message ?: e.javaClass.simpleName}" }
            }
            statusText = result
            isStatusWarn = result.startsWith("导入失败") || result.startsWith("未检测到") || result.startsWith("课程包")
            isImporting = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Study",
            title = "边学边答",
            subtitle = "导入教程课程包，学完每个知识点立即答题巩固"
        )

        // 导入按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
        ) {
            ActionPillButton(
                icon = Icons.Rounded.FileOpen,
                text = "导入课程包",
                primary = true,
                fillWidthContent = true,
                enabled = !isImporting,
                onClick = {
                    statusText = ""
                    filePicker.launch(arrayOf("*/*"))
                },
                modifier = Modifier.weight(1f)
            )
        }

        if (statusText.isNotBlank()) {
            NoticeCard(text = statusText, warning = isStatusWarn)
        }

        if (courses.isEmpty()) {
            Spacer(modifier = Modifier.height(ShirohaSpacing.Lg))
            EmptyStateIllustration(
                title = "还没有课程",
                message = "请导入课程包 JSON（包含 course 节点和 questions 数组）。\n点击上方「导入课程包」按钮选择文件。"
            )
        } else {
            Text(
                text = "已导入 ${courses.size} 门课程",
                style = MaterialTheme.typography.titleMedium,
                color = ShirohaColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            courses.forEach { course ->
                StudyCourseCard(course = course, onClick = { onOpenCourse(course.courseId) })
            }
        }

        Spacer(modifier = Modifier.height(ShirohaSpacing.Xl))
    }
}

@Composable
private fun StudyCourseCard(
    course: KnowledgeCourse,
    onClick: () -> Unit
) {
    val summary = QuizRepository.courseProgressSummary(course.courseId)
    GlassCard(modifier = Modifier
        .fillMaxWidth()
        .shirohaNoRippleClickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(ShirohaSpacing.Lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.MenuBook,
                    contentDescription = null,
                    tint = ShirohaColors.BrandPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(ShirohaSpacing.Sm))
                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ShirohaColors.TextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "进入课程",
                    tint = ShirohaColors.TextSecondary
                )
            }
            if (course.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(ShirohaSpacing.Xs))
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ShirohaColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
            Row(
                horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProgressStat(label = "章节", value = "${summary.studiedSections}/${summary.totalSections}")
                ProgressStat(label = "题目", value = "${summary.totalQuestions}")
                ProgressStat(
                    label = "正确率",
                    value = if (summary.practicedSections > 0) "${(summary.averageAccuracy * 100).toInt()}%" else "—"
                )
            }
        }
    }
}

@Composable
private fun ProgressStat(label: String, value: String) {
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