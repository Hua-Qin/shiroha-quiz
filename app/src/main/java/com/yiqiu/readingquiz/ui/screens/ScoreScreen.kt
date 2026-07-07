package com.yiqiu.readingquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.QuestionType
import com.yiqiu.readingquiz.ui.components.CafeBadge
import com.yiqiu.readingquiz.ui.components.CafeBadgeVariant
import com.yiqiu.readingquiz.ui.components.CafeButton
import com.yiqiu.readingquiz.ui.components.CafeButtonVariant
import com.yiqiu.readingquiz.ui.components.CafeCard
import com.yiqiu.readingquiz.ui.components.CafeCtaBanner
import com.yiqiu.readingquiz.ui.components.CafeEyebrow
import com.yiqiu.readingquiz.ui.components.CafeKpiCard
import com.yiqiu.readingquiz.ui.components.CafeListRow
import com.yiqiu.readingquiz.ui.components.CafeTopBar
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

// 文件级私有间距常量（无对应全局 token 时使用，避免在屏幕上散落 .dp 字面量）
private val AnalysisHeaderTopPad = 4.dp   // 题卡内「解析」小标题顶部 padding

@Composable
fun ScoreScreen(
    articleId: String,
    onBack: () -> Unit,
    onBackToArticle: (String) -> Unit
) {
    val session = remember(articleId) { ReadingRepository.sessionFor(articleId) }
    if (session == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CafeColors.Bg)
                .padding(CafeSpacing.containerPad)
        ) {
            CafeTopBar(title = "本次成绩", onBack = onBack)
            Text(
                text = "未找到成绩",
                style = CafeType.body,
                color = CafeColors.Wrong,
                modifier = Modifier.padding(top = CafeSpacing.md)
            )
        }
        return
    }

    val total = session.questions.size
    val judged = session.answers.filter { it.judged }
    val correctCount = judged.count { it.correct }
    val wrongCount = judged.count { !it.correct }
    val rate = if (total > 0) (correctCount * 100 / total) else 0

    // 错题列表（用于错题区段）
    val wrongQuestions = session.questions.filter { q ->
        val ans = session.answers.firstOrNull { it.questionId == q.id }
        ans != null && ans.judged && !ans.correct
    }

    // 展开/收起状态（按 questionId 记录）
    val expanded = remember(articleId) { mutableStateMapOf<String, Boolean>() }

    Column(modifier = Modifier.fillMaxSize().background(CafeColors.Bg)) {
        CafeTopBar(title = "本次成绩", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = CafeSpacing.containerPad,
                vertical = CafeSpacing.md
            ),
            verticalArrangement = Arrangement.spacedBy(CafeSpacing.md)
        ) {
            // 顶部成绩 hero banner
            item(key = "hero-banner") {
                CafeCtaBanner(
                    title = "$correctCount / $total",
                    body = "本次成绩 $rate 分 · 答对 $correctCount 道，答错 $wrongCount 道，用时 ${formatDuration(session.durationMs)}",
                    primaryButton = {
                        CafeButton(
                            text = "重新作答",
                            onClick = onBack,
                            variant = CafeButtonVariant.OnDark
                        )
                    },
                    secondaryButton = {
                        CafeButton(
                            text = "返回阅读",
                            onClick = { onBackToArticle(session.articleId) },
                            variant = CafeButtonVariant.OnDark
                        )
                    }
                )
            }

            // KPI 行：答对 / 答错 / 用时
            item(key = "kpi-row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CafeSpacing.sm)
                ) {
                    CafeKpiCard(
                        label = "答对",
                        value = correctCount.toString(),
                        delta = "${rate}%",
                        modifier = Modifier.weight(1f)
                    )
                    CafeKpiCard(
                        label = "答错",
                        value = wrongCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    CafeKpiCard(
                        label = "用时",
                        value = formatDuration(session.durationMs),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 题目列表
            item(key = "questions-eyebrow") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                ) {
                    CafeEyebrow(text = "题目明细", showLeadingDot = true)
                }
            }

            items(session.questions, key = { it.id }) { q ->
                val index = session.questions.indexOf(q) + 1
                val ans = session.answers.firstOrNull { it.questionId == q.id }
                val isExpanded = expanded[q.id] == true

                Column(
                    verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                ) {
                    CafeListRow(
                        name = "题 $index · ${questionTypeLabel(q.type)}",
                        meta = answerMeta(ans),
                        showTopBorder = index > 1,
                        onClick = {
                            expanded[q.id] = !(expanded[q.id] ?: false)
                        },
                        trailing = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                        ) {
                                if (ans != null) {
                                    CafeBadge(
                                        text = if (ans.judged && ans.correct) "正确" else "错误",
                                        variant = if (ans.judged && ans.correct)
                                            CafeBadgeVariant.Correct
                                        else
                                            CafeBadgeVariant.Wrong
                                    )
                                }
                                Icon(
                                    imageVector = when {
                                        ans == null -> Icons.Rounded.HelpOutline
                                        ans.judged && ans.correct -> Icons.Rounded.CheckCircle
                                        else -> Icons.Rounded.Cancel
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        ans == null -> CafeColors.Neutral
                                        ans.judged && ans.correct -> CafeColors.Correct
                                        else -> CafeColors.Wrong
                                    }
                                )
                            }
                        }
                    )

                    if (isExpanded) {
                        CafeCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
                                Text(
                                    text = q.question,
                                    style = CafeType.cardTitle,
                                    color = CafeColors.Fg
                                )
                                q.options.forEach { opt ->
                                    Text(
                                        text = "${opt.key}. ${opt.text}",
                                        style = CafeType.bodyCompact,
                                        color = CafeColors.Muted
                                    )
                                }
                                if (q.analysis.isNotBlank()) {
                                    Text(
                                        text = "解析",
                                        style = CafeType.eyebrow,
                                        color = CafeColors.Muted,
                                        modifier = Modifier.padding(top = AnalysisHeaderTopPad)
                                    )
                                    Text(
                                        text = q.analysis,
                                        style = CafeType.bodyCompact,
                                        color = CafeColors.Fg
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 错题区段
            if (wrongQuestions.isNotEmpty()) {
                item(key = "wrong-eyebrow") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                    ) {
                        CafeEyebrow(text = "错题复盘", showLeadingDot = true, textColor = CafeColors.Wrong)
                    }
                }

                item(key = "wrong-card") {
                    CafeCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
                            wrongQuestions.forEach { q ->
                                val idx = session.questions.indexOf(q) + 1
                                val ans = session.answers.firstOrNull { it.questionId == q.id }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                                ) {
                                    CafeBadge(
                                        text = "题 $idx",
                                        variant = CafeBadgeVariant.Wrong
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = q.question,
                                            style = CafeType.bodyCompact,
                                            color = CafeColors.Fg
                                        )
                                        if (q.answer.isNotEmpty()) {
                                            Text(
                                                text = "正确答案：${q.answer.joinToString(" / ")}",
                                                style = CafeType.meta,
                                                color = CafeColors.Correct
                                            )
                                        }
                                        ans?.let {
                                            val userKeys = it.selectedKeys.joinToString(" / ")
                                            if (userKeys.isNotBlank()) {
                                                Text(
                                                    text = "你的答案：$userKeys",
                                                    style = CafeType.meta,
                                                    color = CafeColors.Wrong
                                                )
                                            }
                                        }
                                        if (q.analysis.isNotBlank()) {
                                            Text(
                                                text = q.analysis,
                                                style = CafeType.bodyCompact,
                                                color = CafeColors.Muted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes} 分 ${seconds} 秒" else "${seconds} 秒"
}

private fun questionTypeLabel(type: QuestionType): String = when (type) {
    QuestionType.SINGLE -> "单选"
    QuestionType.MULTIPLE -> "多选"
    QuestionType.JUDGE -> "判断"
    QuestionType.BLANK -> "填空"
    QuestionType.SHORT -> "简答"
}

private fun answerMeta(ans: com.yiqiu.readingquiz.data.model.UserAnswer?): String {
    if (ans == null) return "未作答"
    val userKeys = ans.selectedKeys.joinToString(" / ")
    return if (userKeys.isBlank()) "已作答" else "你的答案 $userKeys"
}