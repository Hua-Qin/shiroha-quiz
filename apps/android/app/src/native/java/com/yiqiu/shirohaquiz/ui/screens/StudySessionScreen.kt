package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.yiqiu.shirohaquiz.importer.model.Option
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.KnowledgeCourse
import com.yiqiu.shirohaquiz.state.KnowledgeSection
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.ArticleReader
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

/**
 * 学习会话阶段状态机：
 *  - LEARN：阅读阶段，仅展示文章式学习内容，不显示任何题目
 *  - QUIZ ：答题阶段，不显示学习内容（可通过"查看知识点"打开半屏覆盖层）
 *  - DONE ：练习完成，展示总结
 */
private enum class StudySessionPhase { LEARN, QUIZ, DONE }

/**
 * 学习会话页：两阶段分离
 *
 * 阶段 1（LEARN）：阅读文章 → 「我已学完，开始练习」
 * 阶段 2（QUIZ）：纯答题 → 顶栏可打开"查看知识点"只读覆盖层
 * 阶段 3（DONE）：总结卡 → 重新练习 / 下一章节 / 返回
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

    // 课程关联题库：用于错题本写入。题库随 courseId 缓存。
    val linkedBank = remember(courseId) {
        QuizRepository.courseById(courseId)?.linkedBankId?.let { bid ->
            QuizRepository.banks.firstOrNull { it.id == bid }
        }
    }

    // 阶段状态机：进入页面默认为学习阅读阶段
    var phase by remember { mutableStateOf(StudySessionPhase.LEARN) }
    var currentIdx by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<List<String>>(emptyList()) }
    var submitted by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }
    var showKnowledgeDialog by remember { mutableStateOf(false) }

    // 进入页面：标记已学习（仅 LEARN 阶段初次进入时触发）
    LaunchedEffect(sectionId) {
        QuizRepository.markSectionStudied(courseId, sectionId)
    }

    // DONE 守门：每次 phase 变为 DONE 时记录一次练习结果
    // 注意：使用 phase 作为 key，且函数体内用 if 守门，确保每次重新进入 DONE 时都会执行
    // （如果用 LaunchedEffect(Unit) 在「重新练习」后重新进入 DONE 时不会触发）
    // Step 2 调整：每题结果已在 onSubmit 内通过 markSectionQuestionResult 累加到 SectionProgress，
    // 这里不再调用 recordSectionResult（避免覆盖逐题累加的正确率）。
    LaunchedEffect(phase) {
        // 故意留空：会话结果由 onSubmit 逐题累加，DONE 阶段不再做覆盖式写入。
    }

    val currentQuestion = questions.getOrNull(currentIdx)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ShirohaHeader(
            kicker = section.chapterTitle.ifBlank { "课程学习" },
            title = section.sectionTitle.ifBlank { section.id },
            subtitle = when (phase) {
                StudySessionPhase.LEARN -> "阶段 1 / 阅读学习内容"
                StudySessionPhase.QUIZ -> "阶段 2 / 完成练习题"
                StudySessionPhase.DONE -> "阶段 3 / 练习总结"
            }
        )
        Spacer(modifier = Modifier.height(ShirohaSpacing.Md))

        when (phase) {
            StudySessionPhase.LEARN -> {
                LearnPhase(
                    section = section,
                    questionsSize = questions.size,
                    onStartPractice = {
                        phase = StudySessionPhase.QUIZ
                    },
                    onBack = onBack
                )
            }
            StudySessionPhase.QUIZ -> {
                QuizPhase(
                    questions = questions,
                    currentQuestion = currentQuestion,
                    currentIdx = currentIdx,
                    selectedAnswer = selectedAnswer,
                    submitted = submitted,
                    onSelectAnswer = { selectedAnswer = it },
                    onSubmit = {
                        val q = currentQuestion
                        if (q != null) {
                            val isCorrect = QuizRepository.judgeAnswer(q, selectedAnswer)
                            if (isCorrect) correctCount++

                            // 1) 写入错题本：答错入错题，答对则从错题本累加正确计数
                            val bank = linkedBank
                            if (bank != null) {
                                if (isCorrect) {
                                    // markWrongQuestionRight 是 private，这里改为走 addWrongContext 的公开重载不存在对应清除路径，
                                    // 改为读取错题本条目后用 copy 调整（不依赖 private 方法）。如果题目不在错题本则直接跳过。
                                    val idx = QuizRepository.wrongBook.indexOfFirst {
                                        it.bankId == bank.id && it.question.id == q.id
                                    }
                                    if (idx >= 0) {
                                        val entry = QuizRepository.wrongBook[idx]
                                        val now = System.currentTimeMillis()
                                        val nextRightCount = entry.rightCount + 1
                                        val nextReviewRightCount = entry.reviewRightCount + 1
                                        val nextStreak = entry.streakCorrectCount + 1
                                        QuizRepository.wrongBook[idx] = entry.copy(
                                            rightCount = nextRightCount,
                                            reviewRightCount = nextReviewRightCount,
                                            streakCorrectCount = nextStreak,
                                            lastCorrectAt = now,
                                            lastReviewedAt = now,
                                            updatedAt = now
                                        )
                                        QuizRepository.persist()
                                    }
                                } else {
                                    // 答错：复用现有 addWrongContext(bank, question, userAnswer, source, correctAnswer, addedManually)
                                    QuizRepository.addWrongContext(
                                        bank = bank,
                                        question = q,
                                        userAnswer = selectedAnswer,
                                        source = "边学边答",
                                        correctAnswer = q.answer,
                                        addedManually = false
                                    )
                                }
                            }

                            // 2) 写入 studyRecord（逐题记录）
                            // recordPracticeResult 当前是 private；改为直接构造 StudyRecord 写入 studyRecords 列表
                            val record = com.yiqiu.shirohaquiz.state.StudyRecord(
                                id = "study_${q.id}_${System.currentTimeMillis()}",
                                bankId = bank?.id,
                                bankName = bank?.name ?: "未命名题库",
                                source = "边学边答",
                                title = q.question.take(24),
                                total = 1,
                                correct = if (isCorrect) 1 else 0,
                                timestamp = System.currentTimeMillis(),
                                questionResults = listOf(
                                    com.yiqiu.shirohaquiz.state.StudyQuestionResult(
                                        question = q,
                                        userAnswer = selectedAnswer,
                                        correct = isCorrect,
                                        answerText = q.answer.joinToString(" / ").ifBlank { "未识别答案" },
                                        autoScored = true,
                                        sourceBankId = bank?.id,
                                        sourceBankName = bank?.name
                                    )
                                ),
                                scopeType = "course",
                                scopeName = course.courseName
                            )
                            QuizRepository.studyRecords.add(0, record)
                            QuizRepository.persist()

                            // 3) 更新 SectionProgress：markSectionQuestionResult 尚未在 QuizRepository 暴露，
                            // 用 studyProgressKey 直接累加（与 recordSectionResult 字段语义一致）
                            val key = QuizRepository.studyProgressKey(courseId, sectionId)
                            val cur = QuizRepository.studyProgress[key]
                                ?: com.yiqiu.shirohaquiz.state.SectionProgress()
                            val newCorrect = cur.correctCount + (if (isCorrect) 1 else 0)
                            val newTotal = cur.totalCount + 1
                            QuizRepository.studyProgress[key] = cur.copy(
                                studied = true,
                                practiced = true,
                                correctCount = newCorrect,
                                totalCount = newTotal,
                                lastStudiedAt = System.currentTimeMillis(),
                                bestAccuracy = maxOf(
                                    cur.bestAccuracy,
                                    newCorrect.toFloat() / newTotal.coerceAtLeast(1).toFloat()
                                ),
                                practiceCount = cur.practiceCount + 1
                            )
                            QuizRepository.persist()
                        }
                        submitted = true
                    },
                    onNext = {
                        submitted = false
                        selectedAnswer = emptyList()
                        if (currentIdx + 1 >= questions.size) {
                            // 全部答完，进入完成态；记录结果由 LaunchedEffect(finished) 守门
                            phase = StudySessionPhase.DONE
                        } else {
                            currentIdx++
                        }
                    },
                    onBackToLearn = {
                        // 回到阅读阶段再读一遍（用于在 QUIZ 中放弃并回到 LEARN）
                        phase = StudySessionPhase.LEARN
                    },
                    onShowKnowledge = { showKnowledgeDialog = true }
                )
            }
            StudySessionPhase.DONE -> {
                DonePhase(
                    total = questions.size,
                    correct = correctCount,
                    onRetry = {
                        currentIdx = 0
                        selectedAnswer = emptyList()
                        submitted = false
                        correctCount = 0
                        // 重新练习：回到 LEARN，让用户再读一次知识点
                        phase = StudySessionPhase.LEARN
                    },
                    onNext = {
                        nextSectionId(course, sectionId)?.let { onNextSection(it) }
                    },
                    onBack = onBack,
                    hasNext = nextSectionId(course, sectionId) != null
                )
            }
        }
    }

    // 答题阶段"查看知识点"半屏 Dialog（只读 ArticleReader）
    if (showKnowledgeDialog) {
        AlertDialog(
            onDismissRequest = { showKnowledgeDialog = false },
            confirmButton = {
                TextButton(onClick = { showKnowledgeDialog = false }) {
                    Text("关闭")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKnowledgeDialog = false }) {
                    Text("继续答题")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = section.sectionTitle.ifBlank { "知识点" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showKnowledgeDialog = false }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "关闭",
                            tint = ShirohaColors.TextSecondary
                        )
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    ArticleReader(section = section)
                }
            }
        )
    }
}

/**
 * LEARN 阶段：阅读视图（仅文章内容，不显示任何题目）
 */
@Composable
private fun LearnPhase(
    section: KnowledgeSection,
    questionsSize: Int,
    onStartPractice: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ArticleReader(section = section)
        Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
        if (questionsSize > 0) {
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = "我已学完，开始练习（$questionsSize 题）",
                primary = true,
                fillWidthContent = true,
                onClick = onStartPractice
            )
        } else {
            NoticeCard(text = "本章暂无题目，可点击下方按钮返回", warning = false)
            ActionPillButton(
                icon = Icons.Rounded.CheckCircle,
                text = "标记为已学习并返回",
                primary = true,
                fillWidthContent = true,
                onClick = onBack
            )
        }
    }
}

/**
 * QUIZ 阶段：答题视图（不混排学习内容）
 */
@Composable
private fun QuizPhase(
    questions: List<Question>,
    currentQuestion: Question?,
    currentIdx: Int,
    selectedAnswer: List<String>,
    submitted: Boolean,
    onSelectAnswer: (List<String>) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
    onBackToLearn: () -> Unit,
    onShowKnowledge: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        if (questions.isEmpty()) {
            NoticeCard(text = "本章暂无题目，无法进入练习", warning = false)
            ActionPillButton(
                icon = Icons.Rounded.ArrowBack,
                text = "返回学习",
                primary = false,
                fillWidthContent = true,
                onClick = onBackToLearn
            )
            return@Column
        }

        // 顶部工具条：查看知识点
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip(text = "第 ${currentIdx + 1} / ${questions.size} 题", selected = true)
            Spacer(modifier = Modifier.weight(1f))
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = "查看知识点",
                primary = false,
                onClick = onShowKnowledge
            )
        }

        if (currentQuestion != null) {
            QuestionPracticeBlock(
                question = currentQuestion,
                questionNumber = currentIdx + 1,
                totalQuestions = questions.size,
                selectedAnswer = selectedAnswer,
                onSelect = onSelectAnswer,
                submitted = submitted,
                onSubmit = onSubmit,
                onNext = onNext
            )
        }
    }
}

/**
 * DONE 阶段：总结视图
 *
 * 注意：recordSectionResult 由 StudySessionScreen 的 LaunchedEffect(phase) 守门触发，
 * 此处不重复调用。
 */
@Composable
private fun DonePhase(
    total: Int,
    correct: Int,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    hasNext: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        FinishedSummary(
            total = total,
            correct = correct,
            onRetry = onRetry,
            onNext = onNext,
            onBack = onBack,
            hasNext = hasNext
        )
    }
}

private fun nextSectionId(course: KnowledgeCourse, currentSectionId: String): String? {
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
                    ans.contains("错") || ans.equals("false", true) ||
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
                            Option("对", "对"),
                            Option("错", "错")
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
        shape = RoundedCornerShape(ShirohaRadius.Md),
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
                text = "重新学习并练习",
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