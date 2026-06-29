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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.KnowledgeCard
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

/**
 * 学习会话页：先展示知识点，再答题
 */
@Composable
fun StudySessionScreen(
    courseId: String,
    sectionId: String,
    onBack: () -> Unit,
    onNextSection: (String) -> Unit
) {
    val course = QuizRepository.courseById(courseId)
    val section = course?.sections?.firstOrNull { it.id == sectionId }
    if (course == null || section == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "章节不存在", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
            ActionPillButton(
                icon = Icons.Rounded.ArrowBack,
                text = "返回",
                primary = false,
                onClick = onBack
            )
        }
        return
    }

    val questions = remember(sectionId, courseId) {
        QuizRepository.questionsForSection(courseId, sectionId)
    }

    var knowledgeExpanded by remember { mutableStateOf(true) }
    var started by remember { mutableStateOf(false) }
    var currentIdx by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<List<String>>(emptyList()) }
    var submitted by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    // 进入页面：标记已学习
    LaunchedEffect(sectionId) {
        QuizRepository.markSectionStudied(courseId, sectionId)
    }

    val currentQuestion = questions.getOrNull(currentIdx)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        // 知识点卡片（可折叠）
        KnowledgeCard(
            section = section,
            expanded = knowledgeExpanded,
            onToggleExpand = { knowledgeExpanded = !knowledgeExpanded }
        )

        if (questions.isEmpty()) {
            NoticeCard(text = "本章暂无题目，可点击下方「标记为已学习」返回", warning = false)
            ActionPillButton(
                icon = Icons.Rounded.CheckCircle,
                text = "标记为已学习",
                primary = true,
                fillWidthContent = true,
                onClick = {
                    QuizRepository.markSectionStudied(courseId, sectionId)
                    onBack()
                }
            )
            return@Column
        }

        if (!started) {
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = "开始练习（${questions.size} 题）",
                primary = true,
                fillWidthContent = true,
                onClick = { started = true }
            )
            return@Column
        }

        if (finished) {
            FinishedSummary(
                total = questions.size,
                correct = correctCount,
                onRetry = {
                    currentIdx = 0
                    selectedAnswer = emptyList()
                    submitted = false
                    correctCount = 0
                    finished = false
                },
                onNext = { onNextSection(nextSectionId(course, sectionId)) },
                onBack = onBack,
                hasNext = nextSectionId(course, sectionId) != null
            )
            // 记录进度：每次完成时记录一次
            LaunchedEffect(finished) {
                if (finished) {
                    QuizRepository.recordSectionResult(courseId, sectionId, correctCount, questions.size)
                }
            }
            return@Column
        }

        if (currentQuestion != null) {
            QuestionPracticeBlock(
                question = currentQuestion,
                questionNumber = currentIdx + 1,
                totalQuestions = questions.size,
                selectedAnswer = selectedAnswer,
                onSelect = { selectedAnswer = it },
                submitted = submitted,
                onSubmit = {
                    if (QuizRepository.judgeAnswer(currentQuestion, selectedAnswer)) {
                        correctCount++
                    }
                    submitted = true
                },
                onNext = {
                    submitted = false
                    selectedAnswer = emptyList()
                    if (currentIdx + 1 >= questions.size) {
                        finished = true
                    } else {
                        currentIdx++
                    }
                }
            )
        }
    }
}

private fun nextSectionId(course: com.yiqiu.shirohaquiz.state.KnowledgeCourse, currentSectionId: String): String? {
    val sections = course.sections.sortedBy { it.order }
    val idx = sections.indexOfFirst { it.id == currentSectionId }
    return if (idx in 0 until sections.size - 1) sections[idx + 1].id else null
}

@Composable
private fun QuestionPracticeBlock(
    question: Question,
    questionNumber: Int,
    totalQuestions: Int,
    selectedAnswer: List<String>,
    onSelect: (List<String>) -> Unit,
    submitted: Boolean,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val isCorrect = remember(submitted, selectedAnswer, question) {
        if (!submitted) null else QuizRepository.judgeAnswer(question, selectedAnswer)
    }
    val correctAnswers = remember(question) {
        when (question.type) {
            QuestionType.JUDGE -> {
                val ans = question.answer.firstOrNull().orEmpty().trim()
                when {
                    ans.contains("错") || ans.contains("错") || ans.equals("false", true) ||
                    ans.equals("×", true) || ans.equals("f", true) -> listOf("错")
                    else -> listOf("对")
                }
            }
            else -> question.answer
        }
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(ShirohaSpacing.Lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(text = "第 $questionNumber / $totalQuestions 题", selected = true)
                Spacer(modifier = Modifier.weight(1f))
                StatusChip(text = typeLabel(question.type.name), selected = false)
            }
            Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = ShirohaColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(ShirohaSpacing.Md))

            when (question.type) {
                QuestionType.SINGLE, QuestionType.MULTIPLE, QuestionType.JUDGE -> {
                    val optionsToShow = when (question.type) {
                        QuestionType.JUDGE -> listOf(
                            com.yiqiu.shirohaquiz.importer.model.Option("对", "对"),
                            com.yiqiu.shirohaquiz.importer.model.Option("错", "错")
                        )
                        else -> question.options
                    }
                    optionsToShow.forEach { opt ->
                        val isSelected = selectedAnswer.contains(opt.key)
                        val isCorrectOpt = correctAnswers.contains(opt.key)
                        val state = when {
                            !submitted -> {
                                if (isSelected) OptionState.SELECTED else OptionState.NEUTRAL
                            }
                            isCorrectOpt -> OptionState.CORRECT
                            isSelected -> OptionState.WRONG
                            else -> OptionState.NEUTRAL
                        }
                        OptionButton(
                            label = opt.key,
                            text = opt.text,
                            state = state,
                            onClick = {
                                if (submitted) return@OptionButton
                                if (question.type == QuestionType.SINGLE || question.type == QuestionType.JUDGE) {
                                    onSelect(listOf(opt.key))
                                } else {
                                    if (isSelected) {
                                        onSelect(selectedAnswer - opt.key)
                                    } else {
                                        onSelect(selectedAnswer + opt.key)
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
                    }
                }
                QuestionType.BLANK -> {
                    Text(
                        text = "填空题：${question.question}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShirohaColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
                    Text(
                        text = "参考答案：${question.answer.joinToString(" / ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ShirohaColors.TextSecondary
                    )
                }
                QuestionType.SHORT -> {
                    Text(
                        text = "简答题：${question.question}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShirohaColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
                    Text(
                        text = "参考答案：${question.answer.joinToString("；")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ShirohaColors.TextSecondary
                    )
                }
            }

            if (submitted && question.analysis.isNotBlank()) {
                Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
                NoticeCard(
                    text = (if (isCorrect == true) "✓ 回答正确" else "✗ 回答错误") + "\n\n解析：${question.analysis}",
                    warning = isCorrect != true
                )
            }

            Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
            if (!submitted) {
                ActionPillButton(
                    icon = Icons.Rounded.CheckCircle,
                    text = "提交答案",
                    primary = true,
                    fillWidthContent = true,
                    enabled = selectedAnswer.isNotEmpty() || question.type == QuestionType.BLANK || question.type == QuestionType.SHORT,
                    onClick = onSubmit
                )
            } else {
                ActionPillButton(
                    icon = Icons.Rounded.ArrowForward,
                    text = if (currentIsLast(questionNumber, totalQuestions)) "完成练习" else "下一题",
                    primary = true,
                    fillWidthContent = true,
                    onClick = onNext
                )
            }
        }
    }
}

private fun currentIsLast(current: Int, total: Int): Boolean = current >= total

private enum class OptionState { NEUTRAL, SELECTED, CORRECT, WRONG }

@Composable
private fun OptionButton(
    label: String,
    text: String,
    state: OptionState,
    onClick: () -> Unit
) {
    val (bg, border, fg) = when (state) {
        OptionState.NEUTRAL -> Triple(
            ShirohaColors.CardWhite78,
            ShirohaColors.LineSoft,
            ShirohaColors.TextPrimary
        )
        OptionState.SELECTED -> Triple(
            ShirohaColors.BrandPrimarySoft,
            ShirohaColors.LineSelected,
            ShirohaColors.BrandPrimary
        )
        OptionState.CORRECT -> Triple(
            Color(0xFFDCFCE7),
            Color(0xFF10B981),
            Color(0xFF047857)
        )
        OptionState.WRONG -> Triple(
            Color(0xFFFEE2E2),
            Color(0xFFEF4444),
            Color(0xFFB91C1C)
        )
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = Modifier
            .fillMaxWidth()
            .shirohaNoRippleClickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = fg
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = fg
            )
        }
    }
}

@Composable
private fun FinishedSummary(
    total: Int,
    correct: Int,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    hasNext: Boolean
) {
    val accuracy = if (total > 0) correct.toFloat() / total else 0f
    val percent = (accuracy * 100).toInt()
    val color = when {
        accuracy >= 0.8f -> Color(0xFF10B981)
        accuracy >= 0.6f -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ShirohaSpacing.Lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (accuracy >= 0.6f) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Text(
                text = "正确 $correct / $total",
                style = MaterialTheme.typography.titleMedium,
                color = ShirohaColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(ShirohaSpacing.Lg))
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = "重新练习",
                primary = false,
                fillWidthContent = true,
                onClick = onRetry
            )
            Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
            if (hasNext) {
                ActionPillButton(
                    icon = Icons.Rounded.ArrowForward,
                    text = "下一章节",
                    primary = true,
                    fillWidthContent = true,
                    onClick = onNext
                )
                Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
            }
            ActionPillButton(
                icon = Icons.Rounded.ArrowBack,
                text = "返回章节列表",
                primary = false,
                fillWidthContent = true,
                onClick = onBack
            )
        }
    }
}

private fun typeLabel(type: String): String = when (type) {
    "SINGLE" -> "单选"
    "MULTIPLE" -> "多选"
    "JUDGE" -> "判断"
    "BLANK" -> "填空"
    "SHORT" -> "简答"
    else -> type
}