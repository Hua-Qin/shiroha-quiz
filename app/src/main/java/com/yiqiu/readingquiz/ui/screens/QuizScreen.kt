package com.yiqiu.readingquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.Question
import com.yiqiu.readingquiz.data.model.QuestionType
import com.yiqiu.readingquiz.data.model.UserAnswer
import com.yiqiu.readingquiz.ui.components.CafeButton
import com.yiqiu.readingquiz.ui.components.CafeButtonVariant
import com.yiqiu.readingquiz.ui.components.CafeCard
import com.yiqiu.readingquiz.ui.components.CafeCardVariant
import com.yiqiu.readingquiz.ui.components.CafeEyebrow
import com.yiqiu.readingquiz.ui.components.CafeProgressDots
import com.yiqiu.readingquiz.ui.components.CafeTopBar
import com.yiqiu.readingquiz.ui.components.EditArticleDialog
import com.yiqiu.readingquiz.ui.components.NoteActionMenu
import com.yiqiu.readingquiz.ui.components.NotePadWindow
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType
import kotlinx.coroutines.delay

// 文件级私有间距 / 尺寸常量（无对应全局 token 时使用，避免在屏幕上散落 .dp 字面量）
private val TopBarIconBtnSize = 40.dp        // 顶部 TopBar 图标按钮尺寸
private val OptionStatusIconSize = 20.dp     // 选项行状态图标尺寸
private val FeedbackIconSize = 22.dp         // 判题反馈区图标尺寸

/**
 * 答题页（cafe-ui 风格重写）。
 * - TopBar 用 CafeTopBar（subtitle 显示 "3/10" 进度 eyebrow）。
 * - 题干用 CafeType.displaySection 居左。
 * - OptionRow 用 CafeListRow 风格（surface + 1px border + 圆角 + 选中/正确/错误态）。
 * - 判题反馈用 CafeCard 包裹（icon + 解析）。
 * - 底部导航三按钮：上一题 Ghost / 提交 Primary / 下一题 Primary。
 * - 顶部进度条用 CafeProgressDots。
 */
@Composable
fun QuizScreen(
    articleId: String,
    onBack: () -> Unit,
    onViewScore: (String) -> Unit,
    sectionId: String? = null
) {
    // 章节过滤：当传入 sectionId 时，仅显示该 sectionId 下的题目
    val session = remember(articleId, sectionId) {
        if (sectionId != null) ReadingRepository.sessionForArticleAndSection(articleId, sectionId)
        else ReadingRepository.sessionFor(articleId)
    }
    if (session == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CafeColors.Bg)
                .padding(CafeSpacing.containerPad)
        ) {
            Text(
                text = "未找到答题会话，请返回文章后点击「进入答题」",
                style = CafeType.body,
                color = CafeColors.Wrong
            )
            Spacer(modifier = Modifier.height(CafeSpacing.md))
            CafeButton(text = "返回", onClick = onBack)
        }
        return
    }

    var currentIndex by remember { mutableStateOf(session.currentIndex) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedKeys by remember { mutableStateOf(listOf<String>()) }
    var blankInputs by remember { mutableStateOf(listOf<String>()) }
    var shortText by remember { mutableStateOf("") }
    var judged by remember { mutableStateOf(false) }
    var correct by remember { mutableStateOf(false) }
    var marked by remember {
        mutableStateOf(session.markedForReview.contains(session.questions[currentIndex].id))
    }
    // 笔记操作三态：菜单 → 文档编辑 / 浮窗
    var showActionMenu by remember { mutableStateOf(false) }
    var showEditArticle by remember { mutableStateOf(false) }
    var showNotePad by remember { mutableStateOf(false) }

    LaunchedEffect(session.id) {
        while (true) {
            delay(1000)
            nowMs = System.currentTimeMillis()
        }
    }

    val question = session.questions.getOrNull(currentIndex)
    if (question == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CafeColors.Bg)
                .padding(CafeSpacing.containerPad)
        ) {
            Text(text = "题目为空", style = CafeType.body, color = CafeColors.Wrong)
        }
        return
    }

    val isLast = currentIndex == session.questions.size - 1
    val answeredCorrect = remember(session.answers) {
        session.answers.filter { it.judged && it.correct }.map { it.questionId }.toSet()
    }
    val answeredWrong = remember(session.answers) {
        session.answers.filter { it.judged && !it.correct }.map { it.questionId }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeColors.Bg)
    ) {
        // 顶部状态区：cafe-ui CafeTopBar + subtitle 显示进度 eyebrow
        CafeTopBar(
            title = "答题",
            subtitle = "${currentIndex + 1} / ${session.questions.size}",
            onBack = onBack,
            actions = {
                // 标记疑问 IconButton
                IconButton(
                    onClick = {
                        marked = !marked
                        ReadingRepository.toggleMarked(session.id, question.id)
                    },
                    modifier = Modifier.size(TopBarIconBtnSize)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bookmark,
                        contentDescription = "标记疑问",
                        tint = if (marked) CafeColors.Accent2 else CafeColors.Muted
                    )
                }
                // 笔记 IconButton
                IconButton(
                    onClick = { showActionMenu = true },
                    modifier = Modifier.size(TopBarIconBtnSize)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "记笔记",
                        tint = CafeColors.Fg
                    )
                }
                // 计时（mono）
                Text(
                    text = formatDuration(nowMs - session.startedAt),
                    style = CafeType.meta,
                    color = CafeColors.Muted,
                    modifier = Modifier.padding(end = CafeSpacing.xs)
                )
            }
        )

        // 进度条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CafeSpacing.containerPad, vertical = CafeSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CafeSpacing.sm)
        ) {
            CafeProgressDots(
                total = session.questions.size,
                current = currentIndex,
                answeredCorrect = answeredCorrect.mapNotNull { id ->
                    session.questions.indexOfFirst { it.id == id }.takeIf { it >= 0 }
                }.toSet(),
                answeredWrong = answeredWrong.mapNotNull { id ->
                    session.questions.indexOfFirst { it.id == id }.takeIf { it >= 0 }
                }.toSet()
            )
            if (sectionId != null) {
                val answeredCount = session.answers.count { a ->
                    session.questions.any { it.id == a.questionId }
                }
                Spacer(modifier = Modifier.weight(1f))
                CafeEyebrow(
                    text = "Section $answeredCount / ${session.questions.size}",
                    showLeadingDot = true,
                    textColor = CafeColors.Accent2
                )
            }
        }

        // 题目展示区
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = CafeSpacing.containerPad),
            verticalArrangement = Arrangement.spacedBy(CafeSpacing.md)
        ) {
            item {
                // 题型 eyebrow + 题干
                CafeEyebrow(
                    text = question.type.name,
                    showLeadingDot = true,
                    textColor = CafeColors.Accent
                )
                Spacer(modifier = Modifier.size(CafeSpacing.xs))
                Text(text = question.question, style = CafeType.displaySection, color = CafeColors.Fg)
            }
            when (question.type) {
                QuestionType.SINGLE -> items(question.options) { option ->
                    val selected = selectedKeys.contains(option.key)
                    // 判题后展示正确/错误反馈
                    val isAnswerKey = question.answer.contains(option.key)
                    OptionRow(
                        label = option.key,
                        text = option.text,
                        selected = selected,
                        judged = judged,
                        isCorrectOption = isAnswerKey,
                        onClick = {
                            if (!judged) selectedKeys = listOf(option.key)
                        }
                    )
                }
                QuestionType.MULTIPLE -> items(question.options) { option ->
                    val selected = selectedKeys.contains(option.key)
                    val isAnswerKey = question.answer.contains(option.key)
                    OptionRow(
                        label = option.key,
                        text = option.text,
                        selected = selected,
                        judged = judged,
                        isCorrectOption = isAnswerKey,
                        onClick = {
                            if (!judged) {
                                selectedKeys = if (selected) selectedKeys - option.key
                                else selectedKeys + option.key
                            }
                        }
                    )
                }
                QuestionType.JUDGE -> {
                    val pickedTrue = selectedKeys.contains("A")
                    item {
                        OptionRow(
                            label = "A",
                            text = "正确",
                            selected = pickedTrue,
                            judged = judged,
                            isCorrectOption = question.answer.contains("A"),
                            onClick = { if (!judged) selectedKeys = listOf("A") }
                        )
                    }
                    item {
                        OptionRow(
                            label = "B",
                            text = "错误",
                            selected = !pickedTrue && selectedKeys.isNotEmpty(),
                            judged = judged,
                            isCorrectOption = question.answer.contains("B"),
                            onClick = { if (!judged) selectedKeys = listOf("B") }
                        )
                    }
                }
                QuestionType.BLANK -> {
                    item {
                        Column {
                            question.blankAnswers.forEachIndexed { idx, _ ->
                                OutlinedTextField(
                                    value = blankInputs.getOrElse(idx) { "" },
                                    onValueChange = { v ->
                                        if (!judged) {
                                            blankInputs = blankInputs.toMutableList().apply {
                                                if (idx >= size) while (size <= idx) add("")
                                                this[idx] = v
                                            }
                                        }
                                    },
                                    label = { Text("空 ${idx + 1}") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(CafeSpacing.xs))
                            }
                        }
                    }
                }
                QuestionType.SHORT -> item {
                    OutlinedTextField(
                        value = shortText,
                        onValueChange = { if (!judged) shortText = it },
                        label = { Text("请输入你的回答") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(CafeSpacing.sectionY)
                    )
                }
            }

            // 判题后显示反馈卡（CafeCard 包裹：icon + 解析）
            if (judged) {
                item {
                    FeedbackCard(
                        correct = correct,
                        analysis = question.analysis.ifBlank { "暂无解析" }
                    )
                }
            }
        }

        // 底部导航：上一题 Ghost / 提交 Primary / 下一题 Primary
        BottomNavBar(
            judged = judged,
            isLast = isLast,
            correct = correct,
            canSubmit = when (question.type) {
                QuestionType.SINGLE, QuestionType.MULTIPLE, QuestionType.JUDGE -> selectedKeys.isNotEmpty()
                QuestionType.BLANK -> blankInputs.size >= question.blankAnswers.size &&
                    blankInputs.all { it.isNotBlank() }
                QuestionType.SHORT -> shortText.isNotBlank()
            },
            onPrev = {
                if (currentIndex > 0) {
                    currentIndex--
                    ReadingRepository.advanceToNext(session.id) // advanceToNext 在题目导航里复用为上一题调整（语义上保持不变）
                    selectedKeys = emptyList()
                    blankInputs = emptyList()
                    shortText = ""
                    judged = false
                    correct = false
                    marked = session.markedForReview.contains(session.questions[currentIndex].id)
                }
            },
            canPrev = currentIndex > 0,
            onSubmit = {
                val isValid = when (question.type) {
                    QuestionType.SINGLE, QuestionType.MULTIPLE, QuestionType.JUDGE ->
                        selectedKeys.isNotEmpty()
                    QuestionType.BLANK ->
                        blankInputs.size >= question.blankAnswers.size &&
                            blankInputs.all { it.isNotBlank() }
                    QuestionType.SHORT -> shortText.isNotBlank()
                }
                if (!isValid) return@BottomNavBar
                val ans = UserAnswer(
                    questionId = question.id,
                    selectedKeys = selectedKeys,
                    blankInputs = blankInputs,
                    shortAnswer = shortText,
                    judged = question.type != QuestionType.SHORT,
                    correct = judge(question, selectedKeys, blankInputs),
                    submittedAt = System.currentTimeMillis()
                )
                ReadingRepository.updateAnswer(session.id, ans)
                judged = true
                correct = ans.correct
                // 累计错题到章节进度
                if (!ans.correct) {
                    val targetSectionId = sectionId ?: question.sectionId
                    if (targetSectionId != null) {
                        ReadingRepository.incrementSectionWrong(articleId, targetSectionId)
                    }
                }
                if (isLast) {
                    ReadingRepository.completeSession(session.id, nowMs - session.startedAt)
                    // 章节完成标记：当传入 sectionId 时，最后一题完成后标记该章节已完成
                    val targetSectionId = sectionId ?: question.sectionId
                    if (targetSectionId != null) {
                        ReadingRepository.markSectionCompleted(articleId, targetSectionId)
                    }
                }
            },
            onNext = {
                if (isLast) {
                    onViewScore(session.articleId)
                } else {
                    currentIndex++
                    ReadingRepository.advanceToNext(session.id)
                    selectedKeys = emptyList()
                    blankInputs = emptyList()
                    shortText = ""
                    judged = false
                    correct = false
                    marked = session.markedForReview.contains(session.questions[currentIndex].id)
                }
            }
        )
    }

    // 笔记操作菜单
    if (showActionMenu) {
        NoteActionMenu(
            onEditArticle = { showEditArticle = true },
            onCreateNote = { showNotePad = true },
            onDismiss = { showActionMenu = false }
        )
    }

    // 直接编辑文档（占位对话框）
    if (showEditArticle) {
        EditArticleDialog(
            articleTitle = session.articleId,
            onDismiss = { showEditArticle = false }
        )
    }

    // 便笺浮窗
    if (showNotePad) {
        NotePadWindow(
            articleId = session.articleId,
            onDismiss = { showNotePad = false }
        )
    }
}

/**
 * 选项行（cafe-ui CafeListRow 风格 + 选中/正确/错误态）。
 *
 * - 默认：surface + 1px border + 圆角
 * - 选中（未判题）：Accent border + Accent.copy(alpha=0.1f) 浅色背景
 * - 正确（判题后且是正确答案）：Correct 绿 border + ✓ icon
 * - 错误（判题后选了非正确答案）：Wrong 红 border + ✗ icon
 */
@Composable
private fun OptionRow(
    label: String,
    text: String,
    selected: Boolean,
    judged: Boolean,
    isCorrectOption: Boolean,
    onClick: () -> Unit
) {
    val (containerColor, borderColor, contentColor) = when {
        judged && isCorrectOption -> Triple(CafeColors.Surface, CafeColors.Correct, CafeColors.Correct)
        judged && selected && !isCorrectOption -> Triple(CafeColors.Surface, CafeColors.Wrong, CafeColors.Wrong)
        selected -> Triple(
            CafeColors.Accent.copy(alpha = 0.1f),
            CafeColors.Accent,
            CafeColors.Accent
        )
        else -> Triple(CafeColors.Surface, CafeColors.Border, CafeColors.Fg)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CafeRadius.rCard))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(CafeRadius.rCard))
            .clickable(enabled = !judged, onClick = onClick)
            .padding(horizontal = CafeSpacing.listRowPadH, vertical = CafeSpacing.listRowPadV),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
    ) {
        // 状态图标
        val (icon, iconTint) = when {
            judged && isCorrectOption -> Icons.Rounded.CheckCircle to CafeColors.Correct
            judged && selected && !isCorrectOption -> Icons.Rounded.Cancel to CafeColors.Wrong
            selected -> Icons.Rounded.CheckCircle to CafeColors.Accent
            else -> Icons.Rounded.RadioButtonUnchecked to CafeColors.Neutral
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(OptionStatusIconSize)
        )
        // 选项 key（eyebrow mono 风格）
        Text(
            text = label,
            style = CafeType.eyebrow.copy(fontWeight = FontWeight.Bold),
            color = contentColor
        )
        Text(
            text = text,
            style = CafeType.bodyCompact,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 判题反馈卡（CafeCard Default 包裹：icon + 解析）。
 */
@Composable
private fun FeedbackCard(correct: Boolean, analysis: String) {
    val (borderColor, accentText) = if (correct) {
        CafeColors.Correct to CafeColors.Correct
    } else {
        CafeColors.Wrong to CafeColors.Wrong
    }
    CafeCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CafeCardVariant.Default,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = CafeSpacing.cardPad,
            vertical = CafeSpacing.cardPadSm
        )
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
        ) {
            Icon(
                imageVector = if (correct) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                contentDescription = null,
                tint = accentText,
                modifier = Modifier.size(FeedbackIconSize)
            )
            Column(modifier = Modifier.weight(1f)) {
                CafeEyebrow(
                    text = if (correct) "Correct" else "Wrong",
                    showLeadingDot = true,
                    textColor = accentText
                )
                Spacer(modifier = Modifier.size(CafeSpacing.xs))
                Text(
                    text = analysis,
                    style = CafeType.bodyCompact,
                    color = CafeColors.Fg
                )
            }
        }
    }
}

/**
 * 底部导航：三按钮（上一题 Ghost / 提交 Primary / 下一题 Primary）。
 */
@Composable
private fun BottomNavBar(
    judged: Boolean,
    isLast: Boolean,
    correct: Boolean,
    canSubmit: Boolean,
    canPrev: Boolean,
    onPrev: () -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val primaryText = when {
        !judged -> "提交答案"
        isLast -> "查看成绩"
        correct -> "下一题"
        else -> "下一题"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CafeSpacing.containerPad, vertical = CafeSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
    ) {
        CafeButton(
            text = "上一题",
            onClick = onPrev,
            variant = CafeButtonVariant.Ghost,
            enabled = canPrev
        )
        Spacer(modifier = Modifier.weight(1f))
        if (!judged) {
            CafeButton(
                text = primaryText,
                onClick = onSubmit,
                variant = CafeButtonVariant.Primary,
                enabled = canSubmit
            )
        } else {
            CafeButton(
                text = primaryText,
                onClick = onNext,
                variant = CafeButtonVariant.Primary
            )
        }
    }
}

private fun judge(q: Question, selectedKeys: List<String>, blankInputs: List<String>): Boolean {
    return when (q.type) {
        QuestionType.SINGLE, QuestionType.JUDGE -> q.answer == selectedKeys
        QuestionType.MULTIPLE -> q.answer.toSet() == selectedKeys.toSet()
        QuestionType.BLANK -> {
            if (blankInputs.size != q.blankAnswers.size) return false
            q.blankAnswers.zip(blankInputs).all { (ans, inp) -> ans.trim() == inp.trim() }
        }
        QuestionType.SHORT -> false
    }
}

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return "%02d:%02d:%02d".format(h, m, s)
}