package com.yiqiu.readingquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.ui.components.CafeCard
import com.yiqiu.readingquiz.ui.components.CafePrimaryButton
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

@Composable
fun ScoreScreen(
    articleId: String,
    onBack: () -> Unit,
    onBackToArticle: (String) -> Unit
) {
    val session = remember(articleId) { ReadingRepository.sessionFor(articleId) }
    if (session == null) {
        Column(modifier = Modifier.fillMaxSize().padding(CafeSpacing.ContainerPad)) {
            Text(text = "未找到成绩", style = CafeType.Body, color = CafeColors.Wrong)
        }
        return
    }

    val total = session.questions.size
    val judged = session.answers.filter { it.judged }
    val correctCount = judged.count { it.correct }
    val wrongCount = judged.count { !it.correct }
    val rate = if (total > 0) (correctCount * 100 / total) else 0

    Column(modifier = Modifier.fillMaxSize().background(CafeColors.Bg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(CafeSpacing.ContainerPad),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "返回",
                    tint = CafeColors.Fg
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text = "成绩汇总", style = CafeType.Heading, color = CafeColors.Fg)
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = CafeSpacing.ContainerPad),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CafeCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(text = "总题数：$total", style = CafeType.Body, color = CafeColors.Fg)
                        Text(text = "答对：$correctCount", style = CafeType.Body, color = CafeColors.Correct)
                        Text(text = "答错：$wrongCount", style = CafeType.Body, color = CafeColors.Wrong)
                        Text(text = "正确率：$rate%", style = CafeType.Body, color = CafeColors.Accent)
                        Text(
                            text = "用时：${session.durationMs / 1000} 秒",
                            style = CafeType.Caption,
                            color = CafeColors.Muted
                        )
                    }
                }
            }
            items(session.questions) { q ->
                val ans = session.answers.firstOrNull { it.questionId == q.id }
                CafeCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${session.questions.indexOf(q) + 1}. ${q.question}",
                            style = CafeType.Body,
                            color = CafeColors.Fg,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = when {
                                ans == null -> Icons.Rounded.RadioButtonUnchecked
                                ans.judged && ans.correct -> Icons.Rounded.CheckCircle
                                else -> Icons.Rounded.CheckCircle
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
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                CafePrimaryButton(
                    text = "返回文章",
                    onClick = { onBackToArticle(session.articleId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}