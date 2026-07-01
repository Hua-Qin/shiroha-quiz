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
import com.yiqiu.shirohaquiz.ui.components.EditorialFigure
import com.yiqiu.shirohaquiz.ui.components.EditorialSection
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.GlassCard
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 棣栭〉 / 瀛︿範鐪嬫澘(鏆栬壊缂栬緫鏉傚織椋庨噸鍐?
 *
 * 甯冨眬鑷笂鑰屼笅锛?
 *  1. EditorialHero(Shiroha 妯″紡甯︽诞鍔ㄦ彃鐢?+ 鏆栬壊 kicker
 *  2. EditorialFigure 脳6 缃戞牸(琛嚎澶ф暟瀛?+ 灏忔爣绛?+ 鍙戜笣涓嬪垝绾?
 *  3. 浠婃棩瀛︿範 EditorialSection(澶ф暟瀛?+ ActionPillButton)
 *  4. 瓒嬪娍鍥?EditorialSection
 *  5. 閿欓鍒嗙被 EditorialSection
 *  6. 蹇嵎鍏ュ彛 EditorialSection(2x2)
 *  7. AI 寤鸿 EditorialSection
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
    val pendingReviewTitle = if (smartReviewEnabled) "浠婃棩寰呭涔" else "寰呭涔"

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shirohaEditorialBackground()
    ) {
        val screenClass = screenClassFor(maxWidth)
        val scale = editorialScaleFor(screenClass)
        val warmBg = ShirohaColors.warmThemeEnabled

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xxl)
        ) {
            // === Hero 鍖$Shiroha 妯″紡鏃跺甫娴姩鎻掔敾 ===
            EditorialHeroSection(
                scale = scale,
                onGoStudy = onGoStudy,
                onGoExam = onGoExam
            )

            // === 缂栬緫寮忔暟鎹尯:6 涓$EditorialFigure ===
            EditorialFiguresSection(stats = stats, scale = scale)

            // === 浠婃棩瀛︿範 ===
            TodayLearningSection(
                todayPracticeCount = todayPracticeCount,
                pendingReviewTitle = pendingReviewTitle,
                pendingReviewCount = pendingReviewCount,
                scale = scale,
                onGoPractice = onGoStudy,
                onGoExam = onGoExam
            )

            // === 瓒嬪娍鍥?===
            EditorialSection(
                kicker = "杩$14 澶",
                title = "瀛︿範瓒嬪娍",
                scale = scale
            ) {
                DailyTrendChart(points = stats.dailyTrend)
                DailyTrendLegend()
            }

            // === 閿欓鍒嗙被 ===
            EditorialSection(
                kicker = "閿欓",
                title = "鍒嗙被鍒嗗竷",
                scale = scale
            ) {
                if (stats.wrongBookByCategory.isEmpty()) {
                    NoticeCard("褰撳墠娌℃湁閿欓鏁版嵁锛屽畬鎴愮粌涔犲悗浼氬湪杩欓噷缁熻銆", warning = false)
                } else {
                    com.yiqiu.shirohaquiz.ui.screens.CategoryBarChart(
                        categories = stats.wrongBookByCategory.take(6)
                    )
                }
            }

            // === 蹇嵎鍏ュ彛 ===
            EditorialSection(
                kicker = "瀵艰埅",
                title = "蹇嵎鍏ュ彛",
                scale = scale
            ) {
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
            }

            // === AI 寤鸿 ===
            EditorialSection(
                kicker = "AI",
                title = "瀛︿範寤鸿",
                scale = scale
            ) {
                AiAdviceCard(
                    stats = stats,
                    isAiConfigured = QuizRepository.isAiConfigured(),
                    onOpenAiSettings = onOpenAiSettings
                )
            }

            Spacer(Modifier.height(ShirohaSpacing.Sm))
        }
    }
}


/**
 * 棣栭〉 hero 鍖?琛嚎澶ф爣棰?+ 鍓枃 + Shiroha 娴姩鎻掔敾(鍙$Shiroha 妯″紡鎺у埗)
 * Shiroha 妯″紡鍏抽棴鏃?鐗堝績灞呬腑,鏃犳彃鐢汇€"
 */
@Composable
private fun EditorialHeroSection(
    scale: Float = 1f,
    onGoStudy: () -> Unit,
    onGoExam: () -> Unit
) {
    IllustrationHeroCard(
        title = "浠婃棩鐨勭粌涔?\n鏄庡ぉ浼氭劅璋綘銆",
        subtitle = "鎶婃瘡涓€娆＄瓟棰?閮藉綋浣滀竴娆＄簿杩涖€",
        imageRes = com.yiqiu.shirohaquiz.R.drawable.illus_home_welcome,
        scale = scale
    ) {
        Spacer(Modifier.height(ShirohaSpacing.Md))
        Row(horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm)) {
            ActionPillButton(
                icon = Icons.Rounded.School,
                text = "寮€濮嬬粌涔",
                primary = true,
                onClick = onGoStudy
            )
            ActionPillButton(
                icon = Icons.Rounded.Timer,
                text = "妯℃嫙鑰冭瘯",
                primary = false,
                onClick = onGoExam
            )
        }
    }
}

/**
 * 缂栬緫寮?6 澶ф暟鎹?琛嚎澶ф暟瀛?+ 灏忔爣绛?+ 鍙戜笣涓嬪垝绾?
 * 2 鍒?脳 3 琛?鍛堢幇鏉傚織灏侀潰绾ф暟鎹?
 */
@Composable
private fun EditorialFiguresSection(stats: StudyStatistics, scale: Float = 1f) {
    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
        ) {
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = "${stats.totalQuestionsAnswered}",
                label = "绱绛旈",
                unit = "棰"
            )
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = stats.totalStudyMinutesFormatted,
                label = "绱瀛︿範"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
        ) {
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = "${(stats.overallAccuracy * 100).toInt()}",
                label = "骞冲潎姝ｇ‘鐜",
                unit = "%"
            )
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = "${stats.knowledgePointsStudied} / ${stats.totalKnowledgePoints}",
                label = "宸插鐭ヨ瘑鐐"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
        ) {
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = "${stats.practiceCount}",
                label = "缁冧範娆℃暟",
                unit = "娆"
            )
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = "${stats.examCount}",
                label = "鑰冭瘯娆℃暟",
                unit = "娆"
            )
        }
    }
}

/**
 * 浠婃棩瀛︿範 EditorialSection:澶ф暟瀛椾粖鏃ョ粌棰樻暟 + 寰呭涔犳暟 + ActionPillButton
 */
@Composable
private fun TodayLearningSection(
    todayPracticeCount: Int,
    pendingReviewTitle: String,
    pendingReviewCount: Int,
    scale: Float = 1f,
    onGoPractice: () -> Unit,
    onGoExam: () -> Unit
) {
    EditorialSection(
        kicker = "浠婃棩",
        title = "瀛︿範鑺傚",
        scale = scale
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
        ) {
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = "${todayPracticeCount}",
                label = "浠婃棩缁冮",
                unit = "棰"
            )
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = "${pendingReviewCount}",
                label = pendingReviewTitle,
                unit = "棰"
            )
        }
        Spacer(Modifier.height(ShirohaSpacing.Sm))
        Row(horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm)) {
            ActionPillButton(
                icon = Icons.Rounded.School,
                text = "缁х画缁冧範",
                primary = true,
                onClick = onGoPractice
            )
            ActionPillButton(
                icon = Icons.Rounded.Timer,
                text = "妯℃嫙鑰冭瘯",
                primary = false,
                onClick = onGoExam
            )
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
    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
        ) {
            DashboardShortcutCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Warning,
                label = "閿欓鏈",
                value = "$wrongBookActiveCount",
                desc = "澶嶄範閿欓",
                onClick = onOpenWrongBook
            )
            DashboardShortcutCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Star,
                label = "鏀惰棌澶",
                value = "$favoriteCount",
                desc = "鏌ョ湅鏀惰棌",
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
                label = "杈瑰杈圭瓟",
                value = "$knowledgeCoursesCount",
                desc = "璇剧▼瀛︿範",
                onClick = onOpenStudy
            )
            DashboardShortcutCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Timer,
                label = "瀛︿範璁板綍",
                value = "$studyRecordsCount",
                desc = "鏌ョ湅璁板綍",
                onClick = onOpenRecords
            )
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

    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)) {
        Text(
            text = "鍩轰簬浣犵殑绛旈鏁版嵁鐢熸垚涓撳睘瀛︿範寤鸿銆",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        when (val state = adviceState) {
            is AdviceUiState.Idle -> {
                ActionPillButton(
                    icon = Icons.Rounded.AutoAwesome,
                    text = if (isAiConfigured) "鑾峰彇瀛︿範寤鸿" else "閰嶇疆 AI 鍚庤幏鍙栧缓璁",
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
                                ShirohaAiClient.generatePersonalizedAdvice(
                                    apiBaseUrl = QuizRepository.aiApiBaseUrl,
                                    apiKey = QuizRepository.aiApiKey,
                                    modelName = QuizRepository.aiModelName,
                                    recordsSummary = buildRecordsSummary(stats),
                                    wrongQuestionsSummary = buildWrongQuestionsSummary()
                                )
                            }
                            adviceState = result.fold(
                                onSuccess = { AdviceUiState.Loaded(it) },
                                onFailure = { AdviceUiState.Failed(it.message ?: "鏈煡閿欒") }
                            )
                        }
                    }
                )
            }
            is AdviceUiState.Loading -> {
                ActionPillButton(
                    icon = Icons.Rounded.AutoAwesome,
                    text = "鐢熸垚涓€",
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
                    text = "閲嶆柊鐢熸垚",
                    primary = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { adviceState = AdviceUiState.Idle }
                )
            }
            is AdviceUiState.Failed -> {
                NoticeCard("鐢熸垚澶辫触锛${state.message}", warning = true)
                Spacer(Modifier.height(ShirohaSpacing.Md))
                ActionPillButton(
                    icon = Icons.Rounded.AutoAwesome,
                    text = "閲嶈瘯",
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
            text = "钖勫急鐐",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        advice.weakPoints.forEach { point ->
            Text(
                text = "路 $point",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(ShirohaSpacing.Sm))
    }
    if (advice.suggestions.isNotEmpty()) {
        Text(
            text = "鎻愬崌寤鸿",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        advice.suggestions.forEach { item ->
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "銆${item.priority}銆${item.title}",
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
 * 杩?14 澶╁涔犺秼鍔胯嚜缁樺浘琛細
 * - 娴呰壊缃戞牸绾?+ X 杞村簳绾?
 * - 钃濈嚎锛氭瘡鏃ョ瓟棰橀噺锛堝疄绾?+ 鏁版嵁鐐癸級
 * - 绱嚎锛氭瘡鏃ユ纭巼锛堣櫄绾?+ 鏁版嵁鐐癸級
 * - X 杞存棩鏈熸爣绛撅紙鎸夊瘑搴﹂棿闅旈噰鏍凤級
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

        // 缃戞牸绾匡紙4 鏉★級
        for (i in 0..3) {
            val y = originY - h * i / 3f
            drawLine(
                color = gridColor,
                start = Offset(originX, y),
                end = Offset(originX + w, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // X 杞村簳绾?
        drawLine(
            color = axisColor,
            start = Offset(originX, originY),
            end = Offset(originX + w, originY),
            strokeWidth = 1.dp.toPx()
        )

        val maxTotal = points.maxOf { it.total }.coerceAtLeast(1)
        val stepX = if (points.size > 1) w / (points.size - 1f) else 0f
        val pointRadius = with(density) { 3.dp.toPx() }

        // 姣忔棩绛旈閲忔姌绾?
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

        // 姣忔棩姝ｇ‘鐜囨姌绾匡紙0-1 鏄犲皠鍒?0-h锛?
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

        // 鏃ユ湡鏍囩
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
        LegendDot(color = ShirohaColors.BrandPrimary, label = "绛旈閲")
        LegendDot(color = ShirohaColors.BrandSecondary, label = "姝ｇ‘鐜")
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
            "鏆傛棤瓒嬪娍鏁版嵁",
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
            "$date ${record.source} 銆${record.bankName.ifBlank { record.title }}銆?" +
                "${record.correct}/${record.total} 姝ｇ‘鐜?$accuracy%"
        }
    return buildString {
        append("绱绛旈 ${stats.totalQuestionsAnswered} 棰橈紝绱姝ｇ‘ ${stats.totalCorrect} 棰橈紝")
        append("骞冲潎姝ｇ‘鐜?${(stats.overallAccuracy * 100).toInt()}%銆")
        if (recent.isNotEmpty()) {
            append("\n鏈€杩戣褰曪細\n")
            append(recent)
        }
    }
}

private fun buildWrongQuestionsSummary(): String {
    val wrongBook = QuizRepository.wrongBook
    if (wrongBook.isEmpty()) return "鏆傛棤閿欓鏁版嵁銆"
    val recent = wrongBook
        .sortedByDescending { it.updatedAt.takeIf { updated -> updated > 0 } ?: it.timestamp }
        .take(10)
    return recent.joinToString("\n") { entry ->
        val cat = entry.question.category?.ifBlank { "鏈垎绫" } ?: "鏈垎绫"
        "鍒嗙被 ${cat}锛${entry.question.question.take(60)}"
    }
}
