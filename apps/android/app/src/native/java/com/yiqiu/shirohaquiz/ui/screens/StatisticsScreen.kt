package com.yiqiu.shirohaquiz.ui.screens

import com.yiqiu.shirohaquiz.ui.theme.shirohaEditorialBackground

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
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
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.editorialScaleFor
import com.yiqiu.shirohaquiz.ui.theme.screenClassFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 瀛︿範缁熻(鏆栬壊缂栬緫鏉傚織椋庨噸鍐?
 *
 * 甯冨眬鑷笂鑰屼笅:
 *  1. ShirohaHeader(kicker/title/subtitle) 鈥?椤堕儴甯?Serif 澶ф爣棰? *  2. EditorialFiguresSection 鈥?6 涓$EditorialFigure 琛嚎澶ф暟瀛楃綉鏍? *  3. 瓒嬪娍鍥?EditorialSection(DailyTrendChart + Legend)
 *  4. 閿欓鍒嗙被 EditorialSection(CategoryBarChart)
 *  5. AI 寤鸿 EditorialSection(AdviceContentBlock + ActionPillButton)
 */
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    onOpenWrongBook: () -> Unit = {},
    onOpenRecords: () -> Unit = {},
    onOpenFavorites: () -> Unit = {}
) {
    val studyRecords = QuizRepository.studyRecords
    val wrongBook = QuizRepository.wrongBook
    val studyProgress = QuizRepository.studyProgress
    val knowledgeCourses = QuizRepository.knowledgeCourses
    val favoriteQuestions = QuizRepository.favoriteQuestions
    val data by remember(
        studyRecords.size,
        wrongBook.size,
        studyProgress.size,
        knowledgeCourses.size,
        favoriteQuestions.size
    ) {
        mutableStateOf(QuizRepository.computeStudyStatistics())
    }
    var adviceState by remember { mutableStateOf<StatisticsAdviceUiState>(StatisticsAdviceUiState.Idle) }
    val scope = rememberCoroutineScope()

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
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xxl)
        ) {
            ShirohaHeader(
                kicker = "鏁版嵁",
                title = "瀛︿範缁熻",
                subtitle = "鎬昏浣犵殑瀛︿範杩涘害",
                scale = scale
            )

            // === 6 涓$EditorialFigure 琛嚎澶ф暟瀛楃綉鏍?===
            EditorialFiguresSection(stats = data, scale = scale)

            // === 瓒嬪娍鍥?===
            EditorialSection(
                kicker = "杩$14 澶",
                title = "瀛︿範瓒嬪娍",
                scale = scale
            ) {
                DailyTrendChart(points = data.dailyTrend)
                Spacer(Modifier.height(ShirohaSpacing.Sm))
                DailyTrendLegend()
            }

            // === 閿欓鍒嗙被 ===
            EditorialSection(
                kicker = "閿欓",
                title = "鍒嗙被鍒嗗竷",
                scale = scale
            ) {
                if (data.wrongBookByCategory.isEmpty()) {
                    NoticeCard("褰撳墠娌℃湁閿欓鏁版嵁锛屽畬鎴愮粌涔犲悗浼氬湪杩欓噷缁熻銆", warning = false)
                } else {
                    CategoryBarChart(categories = data.wrongBookByCategory.take(6))
                }
            }

            // === AI 寤鸿 ===
            EditorialSection(
                kicker = "AI",
                title = "瀛︿範寤鸿",
                scale = scale
            ) {
                AiAdviceCard(
                    data = data,
                    adviceState = adviceState,
                    onAdviceStateChange = { adviceState = it },
                    onBack = onBack,
                    scope = scope
                )
            }

            Spacer(Modifier.height(ShirohaSpacing.Sm))
        }
    }
}


 * 6 澶ф暟鎹"琛嚎澶ф暟瀛"+ 灏忔爣绛"+ 鍙戜笣涓嬪垝绾" * 2 鍒"脳 3 琛$鍛堢幇鏉傚織灏侀潰绾ф暟鎹" */
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
 * AI 寤鸿鍗＄墖:鍦?EditorialSection 鍐呬娇鐢?娓叉煋寤鸿鍐呭 + ActionPillButton
 * 鎶藉嚭璇ュ瓙缁勪欢浠ヤ繚鎸佷富鍑芥暟缁撴瀯娓呮櫚,骞堕伩鍏$adviceState 鎻愬崌鍒拌繃澶氬眰绾с€? */
@Composable
private fun AiAdviceCard(
    data: StudyStatistics,
    adviceState: StatisticsAdviceUiState,
    onAdviceStateChange: (StatisticsAdviceUiState) -> Unit,
    onBack: () -> Unit,
    scope: kotlinx.coroutines.CoroutineScope
) {
    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)) {
        Text(
            text = "鍩轰簬浣犵殑绛旈鏁版嵁鐢熸垚涓撳睘瀛︿範寤鸿銆",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        when (val state = adviceState) {
            is StatisticsAdviceUiState.Idle -> {
                val isAiConfigured = QuizRepository.isAiConfigured()
                ActionPillButton(
                    icon = Icons.Rounded.AutoAwesome,
                    text = if (isAiConfigured) "鑾峰彇瀛︿範寤鸿" else "璇峰厛鍦$AI 璁剧疆涓厤缃",
                    primary = true,
                    onClick = {
                        if (!isAiConfigured) {
                            onBack()
                            return@ActionPillButton
                        }
                        onAdviceStateChange(StatisticsAdviceUiState.Loading)
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                ShirohaAiClient.generatePersonalizedAdvice(
                                    apiBaseUrl = QuizRepository.aiApiBaseUrl,
                                    apiKey = QuizRepository.aiApiKey,
                                    modelName = QuizRepository.aiModelName,
                                    recordsSummary = buildStatisticsRecordsSummary(data),
                                    wrongQuestionsSummary = buildStatisticsWrongSummary()
                                )
                            }
                            onAdviceStateChange(
                                result.fold(
                                    onSuccess = { StatisticsAdviceUiState.Loaded(it) },
                                    onFailure = {
                                        StatisticsAdviceUiState.Failed(it.message ?: "鏈煡閿欒")
                                    }
                                )
                            )
                        }
                    }
                )
            }
            is StatisticsAdviceUiState.Loading -> {
                ActionPillButton(
                    icon = Icons.Rounded.AutoAwesome,
                    text = "鐢熸垚涓€",
                    primary = true,
                    enabled = false,
                    onClick = {}
                )
            }
            is StatisticsAdviceUiState.Loaded -> {
                StatisticsAdviceBlock(advice = state.advice)
                Spacer(Modifier.height(ShirohaSpacing.Sm))
                ActionPillButton(
                    icon = Icons.Rounded.AutoAwesome,
                    text = "閲嶆柊鐢熸垚",
                    primary = false,
                    onClick = { onAdviceStateChange(StatisticsAdviceUiState.Idle) }
                )
            }
            is StatisticsAdviceUiState.Failed -> {
                NoticeCard("鐢熸垚澶辫触:${state.message}", warning = true)
                Spacer(Modifier.height(ShirohaSpacing.Sm))
                ActionPillButton(
                    icon = Icons.Rounded.AutoAwesome,
                    text = "閲嶈瘯",
                    primary = true,
                    onClick = { onAdviceStateChange(StatisticsAdviceUiState.Idle) }
                )
            }
        }
        Spacer(Modifier.height(ShirohaSpacing.Sm))
        ActionPillButton(
            icon = Icons.Rounded.AutoAwesome,
            text = "杩斿洖棣栭〉",
            primary = false,
            onClick = onBack
        )
    }
}

/**
 * 杩?14 澶╁涔犺秼鍔胯嚜缁樺浘琛?
 * - 娴呰壊缃戞牸绾?+ X 杞村簳绾? * - 钃濈嚎:姣忔棩绛旈閲?瀹炵嚎 + 鏁版嵁鐐?
 * - 绱嚎:姣忔棩姝ｇ‘鐜?铏氱嚎 + 鏁版嵁鐐?
 * - X 杞存棩鏈熸爣绛?鎸夊瘑搴﹂棿闅旈噰鏍?
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

        // 缃戞牸绾?4 鏉?
        for (i in 0..3) {
            val y = originY - h * i / 3f
            drawLine(
                color = gridColor,
                start = Offset(originX, y),
                end = Offset(originX + w, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // X 杞村簳绾?        drawLine(
            color = axisColor,
            start = Offset(originX, originY),
            end = Offset(originX + w, originY),
            strokeWidth = 1.dp.toPx()
        )

        val maxTotal = points.maxOf { it.total }.coerceAtLeast(1)
        val stepX = if (points.size > 1) w / (points.size - 1f) else 0f
        val pointRadius = with(density) { 3.dp.toPx() }

        // 姣忔棩绛旈閲忔姌绾?        val totalPath = Path()
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
                style = Stroke(width = 2.5.dp.toPx())
            )
            totalPoints.forEach { offset ->
                drawCircle(
                    color = totalLineColor,
                    radius = pointRadius,
                    center = offset
                )
            }
        }

        // 姣忔棩姝ｇ‘鐜囨姌绾?0-1 鏄犲皠鍒?0-h)
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

/**
 * 閿欓鍒嗙被妯悜鏌辩姸鍥? * 姣忚:鍒嗙被鍚?+ 闀垮害鏉?+ 鏁板瓧
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
        NoticeCard("娌℃湁鍙樉绀虹殑閿欓鍒嗙被銆", warning = false)
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
                        text = "${entry.count} 棰",
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
                    // 鑳屾櫙杞ㄩ亾
                    drawRoundRect(
                        color = trackColor,
                        size = Size(size.width, trackHeight),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                    // 杩涘害鏉?                    val progress = entry.count.toFloat() / maxCount.toFloat()
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
            "鏆傛棤瓒嬪娍鏁版嵁",
            scope.size.width / 2f,
            scope.size.height / 2f,
            paint
        )
    }
}

@Composable
private fun StatisticsAdviceBlock(advice: PersonalizedAdvice) {
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

private sealed class StatisticsAdviceUiState {
    data object Idle : StatisticsAdviceUiState()
    data object Loading : StatisticsAdviceUiState()
    data class Loaded(val advice: PersonalizedAdvice) : StatisticsAdviceUiState()
    data class Failed(val message: String) : StatisticsAdviceUiState()
}

private fun buildStatisticsRecordsSummary(stats: StudyStatistics): String {
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
        append("绱绛旈 ${stats.totalQuestionsAnswered} 棰$绱姝ｇ‘ ${stats.totalCorrect} 棰?")
        append("骞冲潎姝ｇ‘鐜?${(stats.overallAccuracy * 100).toInt()}%銆")
        if (recent.isNotEmpty()) {
            append("\n鏈€杩戣褰?\n")
            append(recent)
        }
    }
}

private fun buildStatisticsWrongSummary(): String {
    val wrongBook = QuizRepository.wrongBook
    if (wrongBook.isEmpty()) return "鏆傛棤閿欓鏁版嵁銆"
    return wrongBook
        .sortedByDescending { it.updatedAt.takeIf { updated -> updated > 0 } ?: it.timestamp }
        .take(10)
        .joinToString("\n") { entry ->
            val cat = entry.question.category?.ifBlank { "鏈垎绫" } ?: "鏈垎绫"
            "鍒嗙被 $cat:${entry.question.question.take(60)}"
        }
}
