package com.yiqiu.readingquiz.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.Question
import com.yiqiu.readingquiz.data.model.QuestionType
import com.yiqiu.readingquiz.data.model.UserAnswer
import com.yiqiu.readingquiz.ui.components.CafePrimaryButton
import com.yiqiu.readingquiz.ui.components.CafeProgressDots
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType
import kotlinx.coroutines.delay

@Composable
fun QuizScreen(
    articleId: String,
    onBack: () -> Unit,
    onViewScore: (String) -> Unit
) {
    val session = remember(articleId) { ReadingRepository.sessionFor(articleId) }
    if (session == null) {
        Column(modifier = Modifier.fillMaxSize().padding(CafeSpacing.ContainerPad)) {
            Text(text = "未找到答题会话，请返回文章后点击「进入答题」",
                style = CafeType.Body, color = CafeColors.Wrong)
            Spacer(modifier = Modifier.height(12.dp))
            CafePrimaryButton(text = "返回", onClick = onBack)
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
    var marked by remember { mutableStateOf(session.markedForReview.contains(session.questions[currentIndex].id)) }

    LaunchedEffect(session.id) {
        while (true) {
            delay(1000)
            nowMs = System.currentTimeMillis()
        }
    }

    val question = session.questions.getOrNull(currentIndex)
    if (question == null) {
        Column(modifier = Modifier.fillMaxSize().padding(CafeSpacing.ContainerPad)) {
            Text(text = "题目为空", style = CafeType.Body, color = CafeColors.Wrong)
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

    Column(modifier = Modifier.fillMaxSize().background(CafeColors.Bg)) {
        // 顶部状态区
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CafeSpacing.ContainerPad),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "返回",
                    tint = CafeColors.Fg
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "第 ${currentIndex + 1} / ${session.questions.size} 题",
                style = CafeType.Heading,
                color = CafeColors.Fg
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = formatDuration(nowMs - session.startedAt),
                style = CafeType.Caption,
                color = CafeColors.Muted
            )
        }
        CafeProgressDots(
            total = session.questions.size,
            current = currentIndex,
            answeredCorrect = answeredCorrect.mapNotNull { id ->
                session.questions.indexOfFirst { it.id == id }.takeIf { it >= 0 }
            }.toSet(),
            answeredWrong = answeredWrong.mapNotNull { id ->
                session.questions.indexOfFirst { it.id == id }.takeIf { it >= 0 }
            }.toSet(),
            modifier = Modifier.padding(horizontal = CafeSpacing.ContainerPad)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 题目展示区
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = CafeSpacing.ContainerPad),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(text = question.question, style = CafeType.Heading, color = CafeColors.Fg)
            }
            when (question.type) {
                QuestionType.SINGLE -> items(question.options) { option ->
                    val selected = selectedKeys.contains(option.key)
                    OptionRow(
                        label = option.key,
                        text = option.text,
                        selected = selected,
                        onClick = {
                            if (!judged) selectedKeys = listOf(option.key)
                        }
                    )
                }
                QuestionType.MULTIPLE -> items(question.options) { option ->
                    val selected = selectedKeys.contains(option.key)
                    OptionRow(
                        label = option.key,
                        text = option.text,
                        selected = selected,
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
                            onClick = { if (!judged) selectedKeys = listOf("A") }
                        )
                    }
                    item {
                        OptionRow(
                            label = "B",
                            text = "错误",
                            selected = !pickedTrue && selectedKeys.isNotEmpty(),
                            onClick = { if (!judged) selectedKeys = listOf("B") }
                        )
                    }
                }
                QuestionType.BLANK -> {
                    item {
                        Column {
                            question.blankAnswers.forEachIndexed { idx, _ ->
                                androidx.compose.material3.OutlinedTextField(
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
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                QuestionType.SHORT -> item {
                    androidx.compose.material3.OutlinedTextField(
                        value = shortText,
                        onValueChange = { if (!judged) shortText = it },
                        label = { Text("请输入你的回答") },
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                    )
                }
            }
        }

        // 底部操作区
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CafeSpacing.ContainerPad),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                marked = !marked
                ReadingRepository.toggleMarked(session.id, question.id)
            }) {
                Icon(
                    imageVector = Icons.Rounded.Bookmark,
                    contentDescription = "标记疑问",
                    tint = if (marked) CafeColors.Accent2 else CafeColors.Fg
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            val btnText = when {
                !judged -> "提交答案"
                isLast -> "查看成绩"
                correct -> "下一题"
                else -> "查看解析"
            }
            CafePrimaryButton(
                text = btnText,
                onClick = {
                    if (!judged) {
                        val isValid = when (question.type) {
                            QuestionType.SINGLE, QuestionType.MULTIPLE, QuestionType.JUDGE ->
                                selectedKeys.isNotEmpty()
                            QuestionType.BLANK ->
                                blankInputs.size >= question.blankAnswers.size &&
                                    blankInputs.all { it.isNotBlank() }
                            QuestionType.SHORT -> shortText.isNotBlank()
                        }
                        if (!isValid) return@CafePrimaryButton
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
                        if (isLast) {
                            ReadingRepository.completeSession(session.id, nowMs - session.startedAt)
                        }
                    } else {
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
                }
            )
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (selected) Icons.Rounded.CheckCircle
                else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) CafeColors.Accent else CafeColors.Neutral
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = "$label. $text", style = CafeType.Body, color = CafeColors.Fg)
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