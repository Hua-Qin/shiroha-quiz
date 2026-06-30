package com.yiqiu.shirohaquiz.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.ai.PersonalizedAdvice
import com.yiqiu.shirohaquiz.ai.ShirohaAiClient
import com.yiqiu.shirohaquiz.state.CategoryCount
import com.yiqiu.shirohaquiz.state.DailyTrendPoint
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.state.StudyStatistics
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 首页 / 学习看板。
 *
 * 布局自上而下：
 *  1. ShirohaHeader
 *  2. OverviewSection：6 个 MetricCell 卡片
 *  3. TodayLearningCard：今日练题、待复习、继续练习 / 模拟考试
 *  4. TrendChart：近 14 天自绘折线图
 *  5. WrongBookBarChart：错题分类横向柱状图
 *  6. ShortcutGrid（2x2）：错题本 / 收藏夹 / 边学边答 / 学习记录
 *  7. AiAdviceCard：AI 学习建议按钮
 */
@Composable
fun HomeDashboardScreen(
    onBack: () -> Unit = {},
    onGoImport: () -> Unit,
    onGoStudy: () -> Unit,
    onGoExam: () -> Unit = {},
    onOpenBankList: () -> Unit = {},
    onOpenBankDetail: (String) -> Unit = {},
    onOpenWrongBook: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenAiSettings: () -> Unit = {}
) {
    val studyRecords = QuizRepository.studyRecords
    val wrongBook = QuizRepository.wrongBook
    val studyProgress = QuizRepository.studyProgress
    val knowledgeCourses = QuizRepository.knowledgeCourses
    val favoriteQuestions = QuizRepository.favoriteQuestions

    val stats by remember(
        studyRecords.size,
        wrongBook.size,
        studyProgress.size,
        knowledgeCourses.size,
        favoriteQuestions.size
    ) {
        mutableStateOf(QuizRepository.computeStudyStatistics())
    }

    val todayPracticeCount = remember(studyRecords.size) {
        computeTodayPracticeCount(studyRecords)
    }
    val wrongBookActiveCount = remember(wrongBook.size) {
        QuizRepository.wrongBookActiveCount()
    }
    val smartReviewEnabled = QuizRepository.wrongBookSmartReviewEnabled
    val pendingReviewCount = if (smartReviewEnabled) {
        QuizRepository.todayWrongBookSmartReviewCount()
    } else {
        wrongBookActiveCount
    }
    val pendingReviewTitle = if (smartReviewEnabled) "今日待复习" else "待复习"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "学习看板",
            title = "首页",
            subtitle = "总览你的学习进度"
        )

        OverviewSection(stats = stats)

        TodayLearningCard(
            todayPracticeCount = todayPracticeCount,
            pendingReviewTitle = pendingReviewTitle,
            pendingReviewCount = pendingReviewCount,
            onGoPractice = onGoStudy,
            onGoExam = onGoExam
        )

        TrendChartCard(stats = stats)

        WrongBookBarChartCard(stats = stats)

        ShortcutGrid(
            wrongBookActiveCount = wrongBookActiveCount,
            favoriteCount = favoriteQuestions.size,
            knowledgeCoursesCount = knowledgeCourses.size,
            studyRecordsCount = studyRecords.size,
            onOpenWrongBook = onOpenWrongBook,
            onOpenFavorites = onOpenFavorites,
            onOpenStudy = onGoStudy,
            onOpenRecords = onOpenRecords
        )

        AiAdviceCard(
            stats = stats,
            isAiConfigured = QuizRepository.isAiConfigured(),
            onOpenAiSettings = onOpenAiSettings
        )

        Spacer(Modifier.height(ShirohaSpacing.Sm))
    }
}

@Composable
private fun OverviewSection(stats: StudyStatistics) {
    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
        ) {
            MetricCell(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.QuestionAnswer,
                value = "${stats.totalQuestionsAnswered}",
                label = "累计答题",
                unit = "题"
            )
            MetricCell(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Schedule,
                value = stats.totalStudyMinutesFormatted,
                label = "累计学习",
                unit = ""
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
        ) {
            MetricCell(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.QuestionMark,
                value = "${(stats.overallAccuracy * 100).toInt()}",
                label = "平均正确率",
                unit = "%"
            )
            MetricCell(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.School,
                value = "${stats.knowledgePointsStudied} / ${stats.totalKnowledgePoints}",
                label = "已学知识点",
                unit = ""
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
        ) {
            MetricCell(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.School,
                value = "${stats.practiceCount}",
                label = "练习次数",
                unit = "次"
            )
            MetricCell(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.School,
                value = "${stats.examCount}",
                label = "考试次数",
                unit = "次"
            )
        }
    }
}

@Composable
private fun MetricCell(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    unit: String
) {
    Surface(
        modifier = modifier.heightIn(min = 96.dp),
        shape = RoundedCornerShape(ShirohaRadius.Md),
        color = ShirohaColors.CardWhite86,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
    ) {
        Column(
            modifier = Modifier.padding(ShirohaSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = ShirohaColors.BrandPrimarySoft,
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = ShirohaColors.TextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (unit.isNotBlank()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShirohaColors.TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayLearningCard(
    todayPracticeCount: Int,
    pendingReviewTitle: String,
    pendingReviewCount: Int,
    onGoPractice: () -> Unit,
    onGoExam: () -> Unit
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "今日学习",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            DashboardStatusChip("今日")
        }
        Spacer(Modifier.height(ShirohaSpacing.Sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
        ) {
            MiniMetric(
                modifier = Modifier.weight(1f),
                title = "今日练题",
                value = "${todayPracticeCount} 题"
            )
            MiniMetric(
                modifier = Modifier.weight(1f),
                title = pendingReviewTitle,
                value = "${pendingReviewCount} 题"
            )
        }
        Spacer(Modifier.height(ShirohaSpacing.Md))
        ActionPillButton(
            icon = Icons.Rounded.School,
            text = "继续练习",
            primary = true,
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoPractice
        )
        Spacer(Modifier.height(ShirohaSpacing.Sm))
        ActionPillButton(
            icon = Icons.Rounded.Timer,
            text = "模拟考试",
            primary = false,
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoExam
        )
    }
}

@Composable
private fun MiniMetric(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(ShirohaRadius.Md),
        color = ShirohaColors.CardWhite78,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = ShirohaColors.TextSecondary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TrendChartCard(stats: StudyStatistics) {
    GlassCard {
        Text(
            text = "近 14 天学习趋势",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(ShirohaSpacing.Sm))
        Text(
            text = "蓝线为每日答题量，紫线为每日正确率",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(ShirohaSpacing.Md))
        DailyTrendChart(points = stats.dailyTrend)
        Spacer(Modifier.height(ShirohaSpacing.Md))
        DailyTrendLegend()
    }
}

@Composable
private fun WrongBookBarChartCard(stats: StudyStatistics) {
    GlassCard {
        Text(
            text = "错题分类分布",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(ShirohaSpacing.Sm))
        Text(
            text = "展示错题数量最多的前 6 个分类",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(ShirohaSpacing.Md))
        if (stats.wrongBookByCategory.isEmpty()) {
            NoticeCard("当前没有错题数据，完成练习后会在这里统计。", warning = false)
        } else {
            CategoryBarChart(categories = stats.wrongBookByCategory.take(6))
        }
    }
}

@Composable
private fun ShortcutGrid(
    wrongBookActiveCount: Int,
    favoriteCount: Int,
    knowledgeCoursesCount: Int,
    studyRecordsCount: Int,
    onOpenWrongBook: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenStudy: () -> Unit,
    onOpenRecords: () -> Unit
) {
    GlassCard {
        Text(
            text = "快捷入口",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(ShirohaSpacing.Sm))
        Text(
            text = "点击卡片快速跳转学习数据",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(ShirohaSpacing.Md))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 480.dp
            if (isWide) {
                Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
                    ) {
                        DashboardShortcutCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Warning,
                            label = "错题本",
                            value = "$wrongBookActiveCount",
                            desc = "复习错题",
                            onClick = onOpenWrongBook
                        )
                        DashboardShortcutCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Star,
                            label = "收藏夹",
                            value = "$favoriteCount",
                            desc = "查看收藏",
                            onClick = onOpenFavorites
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
                    ) {
                        DashboardShortcutCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.AutoStories,
                            label = "边学边答",
                            value = "$knowledgeCoursesCount",
                            desc = "课程学习",
                            onClick = onOpenStudy
                        )
                        DashboardShortcutCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Timer,
                            label = "学习记录",
                            value = "$studyRecordsCount",
                            desc = "查看记录",
                            onClick = onOpenRecords
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
                    ) {
                        DashboardShortcutCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Warning,
                            label = "错题本",
                            value = "$wrongBookActiveCount",
                            desc = "复习错题",
                            onClick = onOpenWrongBook
                        )
                        DashboardShortcutCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Star,
                            label = "收藏夹",
                            value = "$favoriteCount",
                            desc = "查看收藏",
                            onClick = onOpenFavorites
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
                    ) {
                        DashboardShortcutCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.AutoStories,
                            label = "边学边答",
                            value = "$knowledgeCoursesCount",
                            desc = "课程学习",
                            onClick = onOpenStudy
                        )
                        DashboardShortcutCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Timer,
                            label = "学习记录",
                            value = "$studyRecordsCount",
                            desc = "查看记录",
                            onClick = onOpenRecords
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardShortcutCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    desc: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.shirohaNoRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(ShirohaRadius.Md),
        color = ShirohaColors.CardWhite78,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = ShirohaColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AiAdviceCard(
    stats: StudyStatistics,
    isAiConfigured: Boolean,
    onOpenAiSettings: () -> Unit
) {
    var adviceState by remember { mutableStateOf<AdviceUiState>(AdviceUiState.Idle) }
    val scope = rememberCoroutineScope()

    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI 学习建议",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            DashboardStatusChip("AI")
        }
        Spacer(Modifier.height(ShirohaSpacing.Sm))
        Text(
            text = "基于你的答题数据生成专属学习建议。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(ShirohaSpacing.Md))
        when (val state = adviceState) {
            is AdviceUiState.Idle -> {
                ActionPillButton(
                    icon = Icons.Rounded.AutoAwesome,
                    text = if (isAiConfigured) "获取学习建议" else "配置 AI 后获取建议",
                    primary = true,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (!isAiConfigured) {
                            onOpenAiSettings()
                            return@ActionPillButton
                        }
                        adviceState = AdviceUiState.Loading
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                runCatching {
                                    ShirohaAiClient.generatePersonalizedAdvice(
                                        apiBaseUrl = QuizRepository.aiApiBaseUrl,
                                        apiKey = QuizRepository.aiApiKey,
                                        modelName = QuizRepository.aiModelName,
                                        recordsSummary = buildRecordsSummary(stats),
                                        wrongQuestionsSummary = buildWrongQuestionsSummary()
                                    )
                                }
                            }
                            adviceState = result.fold(
                                onSuccess = { AdviceUiState.Loaded(it) },
                                onFailure = { AdviceUiState.Failed(it.message ?: "未知错误") }
                            )
                        }
                    }
                )
            }
            is AdviceUiState.Loading -> {
                ActionPillButton(
                    icon = Icons.Rounded.AutoAwesome,
                    text = "生成中…",
                    primary = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {}
                )
            }
            is AdviceUiState.Loaded -> {
                AdviceContentBlock(advice = state.advice)
                Spacer(Modifier.height(ShirohaSpacing.Md))
                ActionPillButton(
                    icon = Icons.Rounded.AutoAwesome,
                    text = "重新生成",
                    primary = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { adviceState = AdviceUiState.Idle }
                )
            }
            is AdviceUiState.Failed -> {
                NoticeCard("生成失败：${state.message}", warning = true)
                Spacer(Modifier.height(ShirohaSpacing.Md))
                ActionPillButton(
                    icon = Icons.Rounded.AutoAwesome,
                    text = "重试",
                    primary = true,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { adviceState = AdviceUiState.Idle }
                )
            }
        }
    }
}

@Composable
private fun AdviceContentBlock(advice: PersonalizedAdvice) {
    if (advice.overallAssessment.isNotBlank()) {
        Text(
            text = advice.overallAssessment,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(ShirohaSpacing.Sm))
    }
    if (advice.weakPoints.isNotEmpty()) {
        Text(
            text = "薄弱点",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        advice.weakPoints.forEach { point ->
            Text(
                text = "· $point",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(ShirohaSpacing.Sm))
    }
    if (advice.suggestions.isNotEmpty()) {
        Text(
            text = "提升建议",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        advice.suggestions.forEach { item ->
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "【${item.priority}】${item.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (item.detail.isNotBlank()) {
                    Text(
                        text = item.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = ShirohaColors.TextSecondary
                    )
                }
            }
        }
        Spacer(Modifier.height(ShirohaSpacing.Sm))
    }
    if (advice.motivationalMessage.isNotBlank()) {
        Text(
            text = advice.motivationalMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private sealed class AdviceUiState {
    data object Idle : AdviceUiState()
    data object Loading : AdviceUiState()
    data class Loaded(val advice: PersonalizedAdvice) : AdviceUiState()
    data class Failed(val message: String) : AdviceUiState()
}

@Composable
private fun DashboardStatusChip(text: String) {
    Surface(
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = ShirohaColors.BrandPrimarySoft,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSelected)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 近 14 天学习趋势自绘图表：
 * - 浅色网格线 + X 轴底线
 * - 蓝线：每日答题量（实线 + 数据点）
 * - 紫线：每日正确率（虚线 + 数据点）
 * - X 轴日期标签（按密度间隔采样）
 */
@Composable
private fun DailyTrendChart(
    points: List<DailyTrendPoint>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val gridColor = ShirohaColors.LineSoft
    val totalLineColor = ShirohaColors.BrandPrimary
    val accuracyLineColor = ShirohaColors.BrandSecondary
    val axisColor = ShirohaColors.LineStrong
    val labelColor = ShirohaColors.TextSecondary

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        if (points.isEmpty()) {
            drawEmptyHint(density, this, labelColor)
            return@Canvas
        }
        val padding = with(density) { 16.dp.toPx() }
        val bottomLabelHeight = with(density) { 18.dp.toPx() }
        val leftAxisWidth = with(density) { 24.dp.toPx() }
        val w = size.width - padding * 2 - leftAxisWidth
        val h = size.height - padding * 2 - bottomLabelHeight
        val originX = padding + leftAxisWidth
        val originY = padding + h

        // 网格线（4 条）
        for (i in 0..3) {
            val y = originY - h * i / 3f
            drawLine(
                color = gridColor,
                start = Offset(originX, y),
                end = Offset(originX + w, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // X 轴底线
        drawLine(
            color = axisColor,
            start = Offset(originX, originY),
            end = Offset(originX + w, originY),
            strokeWidth = 1.dp.toPx()
        )

        val maxTotal = points.maxOf { it.total }.coerceAtLeast(1)
        val stepX = if (points.size > 1) w / (points.size - 1f) else 0f
        val pointRadius = with(density) { 3.dp.toPx() }

        // 每日答题量折线
        val totalPath = Path()
        val totalPoints = points.mapIndexed { i, p ->
            val x = originX + stepX * i
            val y = originY - h * (p.total.toFloat() / maxTotal)
            Offset(x, y)
        }
        if (totalPoints.isNotEmpty()) {
            totalPath.moveTo(totalPoints.first().x, totalPoints.first().y)
            for (i in 1 until totalPoints.size) {
                totalPath.lineTo(totalPoints[i].x, totalPoints[i].y)
            }
            drawPath(
                path = totalPath,
                color = totalLineColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
            totalPoints.forEach { offset ->
                drawCircle(
                    color = totalLineColor,
                    radius = pointRadius,
                    center = offset
                )
            }
        }

        // 每日正确率折线（0-1 映射到 0-h）
        val accuracyPath = Path()
        val accuracyPoints = points.mapIndexed { i, p ->
            val x = originX + stepX * i
            val y = originY - h * p.accuracy
            Offset(x, y)
        }
        if (accuracyPoints.isNotEmpty()) {
            accuracyPath.moveTo(accuracyPoints.first().x, accuracyPoints.first().y)
            for (i in 1 until accuracyPoints.size) {
                accuracyPath.lineTo(accuracyPoints[i].x, accuracyPoints[i].y)
            }
            drawPath(
                path = accuracyPath,
                color = accuracyLineColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
            )
            accuracyPoints.forEach { offset ->
                drawCircle(
                    color = accuracyLineColor,
                    radius = pointRadius * 0.85f,
                    center = offset
                )
            }
        }

        // 日期标签
        val labelStep = if (points.size > 7) (points.size + 6) / 7 else 1
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                200,
                (labelColor.red * 255).toInt(),
                (labelColor.green * 255).toInt(),
                (labelColor.blue * 255).toInt()
            )
            textSize = with(density) { 11.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val labelOffsetY = with(density) { 14.dp.toPx() }
        points.forEachIndexed { i, p ->
            if (i % labelStep == 0 || i == points.lastIndex) {
                val x = originX + stepX * i
                drawContext.canvas.nativeCanvas.drawText(p.date, x, originY + labelOffsetY, textPaint)
            }
        }
    }
}

@Composable
private fun DailyTrendLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(color = ShirohaColors.BrandPrimary, label = "答题量")
        LegendDot(color = ShirohaColors.BrandSecondary, label = "正确率")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = ShirohaColors.TextSecondary
        )
    }
}

/**
 * 错题分类横向柱状图
 * 每行：分类名 + 长度条 + 数字
 */
@Composable
fun CategoryBarChart(
    categories: List<CategoryCount>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val barColor = ShirohaColors.BrandPrimary
    val trackColor = ShirohaColors.LineSoft

    if (categories.isEmpty()) {
        NoticeCard("没有可显示的错题分类。", warning = false)
        return
    }
    val maxCount = categories.maxOf { it.count }.coerceAtLeast(1)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
    ) {
        categories.forEach { entry ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${entry.count} 题",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                ) {
                    val cornerRadius = with(density) { 6.dp.toPx() }
                    val trackHeight = size.height
                    // 背景轨道
                    drawRoundRect(
                        color = trackColor,
                        size = Size(size.width, trackHeight),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                    // 进度条
                    val progress = entry.count.toFloat() / maxCount.toFloat()
                    val barWidth = size.width * progress
                    if (barWidth > 0f) {
                        drawRoundRect(
                            color = barColor,
                            size = Size(barWidth, trackHeight),
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                        )
                    }
                }
            }
        }
    }
}

private fun drawEmptyHint(
    density: androidx.compose.ui.unit.Density,
    scope: androidx.compose.ui.graphics.drawscope.DrawScope,
    color: Color
) {
    scope.drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.argb(
                160,
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt()
            )
            textSize = with(density) { 12.sp.toPx() }
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        drawText(
            "暂无趋势数据",
            scope.size.width / 2f,
            scope.size.height / 2f,
            paint
        )
    }
}

private fun computeTodayPracticeCount(records: List<com.yiqiu.shirohaquiz.state.StudyRecord>): Int {
    if (records.isEmpty()) return 0
    val now = Calendar.getInstance()
    return records
        .filter { isToday(it.timestamp, now) }
        .sumOf { it.total }
}

private fun isToday(timestamp: Long, now: Calendar): Boolean {
    if (timestamp <= 0L) return false
    val target = Calendar.getInstance().apply { timeInMillis = timestamp }
    return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
}

private fun buildRecordsSummary(stats: StudyStatistics): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val recent = QuizRepository.studyRecords
        .sortedByDescending { it.timestamp }
        .take(10)
        .joinToString("\n") { record ->
            val date = dateFormat.format(Date(record.timestamp))
            val accuracy = if (record.total > 0) {
                (record.correct * 100.0 / record.total).toInt()
            } else 0
            "$date ${record.source} 《${record.bankName.ifBlank { record.title }}》 " +
                "${record.correct}/${record.total} 正确率 $accuracy%"
        }
    return buildString {
        append("累计答题 ${stats.totalQuestionsAnswered} 题，累计正确 ${stats.totalCorrect} 题，")
        append("平均正确率 ${(stats.overallAccuracy * 100).toInt()}%。")
        if (recent.isNotEmpty()) {
            append("\n最近记录：\n")
            append(recent)
        }
    }
}

private fun buildWrongQuestionsSummary(): String {
    val wrongBook = QuizRepository.wrongBook
    if (wrongBook.isEmpty()) return "暂无错题数据。"
    val recent = wrongBook
        .sortedByDescending { it.updatedAt.takeIf { updated -> updated > 0 } ?: it.timestamp }
        .take(10)
    return recent.joinToString("\n") { entry ->
        val cat = entry.question.category?.ifBlank { "未分类" } ?: "未分类"
        "分类 $cat：${entry.question.text.take(60)}"
    }
}