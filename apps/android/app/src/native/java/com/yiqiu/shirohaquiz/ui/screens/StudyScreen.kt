package com.yiqiu.shirohaquiz.ui.screens

import com.yiqiu.shirohaquiz.ui.theme.shirohaEditorialBackground

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.yiqiu.shirohaquiz.ui.components.EditorialDivider
import com.yiqiu.shirohaquiz.ui.components.EditorialFigure
import com.yiqiu.shirohaquiz.ui.components.EditorialSection
import com.yiqiu.shirohaquiz.ui.components.EmptyStateIllustration
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.editorialScaleFor
import com.yiqiu.shirohaquiz.ui.theme.screenClassFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 瀛︿範涓婚〉锛氭樉绀哄凡瀵煎叆鐨勮绋嬪垪琛紙鏆栬壊缂栬緫鏉傚織椋庨噸鍐欙級
 *
 * 甯冨眬鑷笂鑰屼笅锛? *  1. ShirohaHeader锛坘icker=Study / title=杈瑰杈圭瓟 / subtitle锛? *  2. 瀵煎叆鎸夐挳 + 鐘舵€佹彁绀? *  3. EditorialFigure 脳N 椤堕儴缁熻锛堟€昏繘搴?/ 宸插畬鎴?/ 杩涜涓級
 *  4. EditorialSection锛坘icker=Courses / title=璇剧▼锛夊寘瑁硅绋嬪垪琛紙CardWhite86 + EditorialDivider 鍒嗛殧锛? *  5. 绌虹姸鎬佺敤 EmptyStateIllustration
 *
 * 鏀寔瀵煎叆璇剧▼鍖咃紙JSON 鏂囦欢锛屽惈 course + questions 鑺傜偣锛夈€? */
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
        statusText = "姝ｅ湪瑙ｆ瀽璇剧▼鍖呪€"
        isStatusWarn = false
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: return@runCatching "鏃犳硶璇诲彇鏂囦欢"
                    val text = String(bytes, Charsets.UTF_8)
                    QuizRepository.importCoursePackage(context, text)
                }.getOrElse { e -> "瀵煎叆澶辫触锛${e.message ?: e.javaClass.simpleName}" }
            }
            statusText = result
            isStatusWarn = result.startsWith("瀵煎叆澶辫触") || result.startsWith("鏈娴嬪埌") || result.startsWith("璇剧▼鍖")
            isImporting = false
        }
    }

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
                kicker = "Study",
                title = "杈瑰杈圭瓟",
                subtitle = "瀵煎叆鏁欑▼璇剧▼鍖咃紝瀛﹀畬姣忎釜鐭ヨ瘑鐐圭珛鍗崇瓟棰樺珐鍥",
                scale = scale
            )

            // 瀵煎叆鎸夐挳
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
            ) {
                ActionPillButton(
                    icon = Icons.Rounded.FileOpen,
                    text = "瀵煎叆璇剧▼鍖",
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
                    title = "杩樻病鏈夎绋",
                    message = "璇峰鍏ヨ绋嬪寘 JSON锛堝寘鍚$course 鑺傜偣鍜$questions 鏁扮粍锛夈€俓n鐐瑰嚮涓婃柟銆屽鍏ヨ绋嬪寘銆嶆寜閽€夋嫨鏂囦欢銆"
                )
            } else {
                // 椤堕儴缁熻锛欵ditorialFigure 脳N锛堣‖绾垮ぇ鏁板瓧 + 灏忔爣绛?+ 鍙戜笣涓嬪垝绾匡級
                StudyOverviewFigures(
                    courses = courses,
                    scale = scale
                )

                // 璇剧▼鍒楄〃锛欵ditorialSection 鍖呰９
                EditorialSection(
                    kicker = "Courses",
                    title = "璇剧▼",
                    scale = scale
                ) {
                    courses.forEachIndexed { index, course ->
                        StudyCourseCard(
                            course = course,
                            onClick = { onOpenCourse(course.courseId) }
                        )
                        if (index < courses.lastIndex) {
                            EditorialDivider()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(ShirohaSpacing.Xl))
        }
    }
}

/**
 * 椤堕儴瀛︿範缁熻锛氳‖绾垮ぇ鏁板瓧 脳3
 *  - 鎬昏繘搴︼紙宸插涔犵珷鑺?/ 鎬荤珷鑺傦紝鐧惧垎姣旓級
 *  - 宸插涔犵珷鑺傛暟锛堝甫 / 鎬荤珷鑺?鍗曚綅娉ㄨВ锛? *  - 宸插畬鎴愮珷鑺傛暟锛堣妭锛? *  - 杩涜涓珷鑺傛暟锛堣妭锛? */
@Composable
private fun StudyOverviewFigures(
    courses: List<KnowledgeCourse>,
    scale: Float = 1f
) {
    val totalSections = courses.sumOf { it.sections.size }
    var studiedSections = 0
    var masteredSections = 0
    courses.forEach { c ->
        val summary = QuizRepository.courseProgressSummary(c.courseId)
        studiedSections += summary.studiedSections
        masteredSections += summary.masteredSections
    }
    val inProgressSections = (studiedSections - masteredSections).coerceAtLeast(0)
    val progressPercent = if (totalSections > 0) {
        (studiedSections * 100 / totalSections)
    } else 0

    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
        ) {
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = "$progressPercent",
                label = "鎬昏繘搴",
                unit = "%"
            )
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = "$studiedSections",
                label = "宸插涔犵珷鑺",
                unit = if (totalSections > 0) "/ $totalSections" else ""
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
        ) {
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = "$masteredSections",
                label = "宸插畬鎴",
                unit = "鑺"
            )
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = "$inProgressSections",
                label = "杩涜涓",
                unit = "鑺"
            )
        }
    }
}

@Composable
private fun StudyCourseCard(
    course: KnowledgeCourse,
    onClick: () -> Unit
) {
    val summary = QuizRepository.courseProgressSummary(course.courseId)
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
                    contentDescription = "杩涘叆璇剧▼",
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
                ProgressStat(label = "绔犺妭", value = "${summary.studiedSections}/${summary.totalSections}")
                ProgressStat(label = "棰樼洰", value = "${summary.totalQuestions}")
                ProgressStat(
                    label = "姝ｇ‘鐜",
                    value = if (summary.practicedSections > 0) "${(summary.averageAccuracy * 100).toInt()}%" else "鈥"
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

