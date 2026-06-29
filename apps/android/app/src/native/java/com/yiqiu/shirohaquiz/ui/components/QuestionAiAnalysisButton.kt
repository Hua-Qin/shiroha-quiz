package com.yiqiu.shirohaquiz.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.ai.ShirohaAiClient
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 通用"AI 解析本题"按钮：
 * - 用于 StudySessionScreen 答题结果区
 * - 用于 WrongBookScreen 错题卡片
 *
 * 行为：
 * 1. 未配置 AI → 提示并可引导到设置
 * 2. 未开启单题分析 → 提示
 * 3. 已配置 + 开启 → 调用 ShirohaAiClient.analyzeSingleQuestion，渲染三段结果
 */
@Composable
fun QuestionAiAnalysisButton(
    question: Question,
    userAnswer: List<String>? = null,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var loading by remember(question.id) { mutableStateOf(false) }
    var result by remember(question.id) { mutableStateOf<ShirohaAiClient.AiSingleQuestionAnalysis?>(null) }
    var errorText by remember(question.id) { mutableStateOf<String?>(null) }
    var showSettingsHint by remember { mutableStateOf(false) }
    var showEnableHint by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionPillButton(
            icon = Icons.Rounded.AutoAwesome,
            text = when {
                loading -> "AI 解析中"
                result != null -> "重新解析本题"
                else -> "AI 解析本题"
            },
            primary = false,
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            fillWidthContent = true,
            onClick = {
                when {
                    !QuizRepository.isAiConfigured() -> {
                        showSettingsHint = true
                        errorText = null
                        result = null
                    }
                    !QuizRepository.aiSingleQuestionAnalysisEnabled -> {
                        showEnableHint = true
                        errorText = null
                        result = null
                    }
                    else -> {
                        loading = true
                        errorText = null
                        scope.launch {
                            val outcome = runCatching {
                                withContext(Dispatchers.IO) {
                                    ShirohaAiClient.analyzeSingleQuestion(
                                        apiBaseUrl = QuizRepository.aiApiBaseUrl,
                                        apiKey = QuizRepository.aiApiKey,
                                        modelName = QuizRepository.aiModelName,
                                        question = question,
                                        userAnswer = userAnswer.orEmpty(),
                                        timeoutSeconds = QuizRepository.aiTimeoutSeconds
                                    )
                                }
                            }
                            outcome.onSuccess { analysis ->
                                result = analysis
                                errorText = null
                            }.onFailure { error ->
                                result = null
                                errorText = error.message ?: "请检查接口配置或网络。"
                            }
                            loading = false
                        }
                    }
                }
            }
        )

        if (loading) {
            NoticeCard("AI 正在解析本题，请稍候。", warning = false)
        }
        errorText?.takeIf { it.isNotBlank() }?.let { message ->
            NoticeCard("AI 解析失败：$message，请稍后重试", warning = true)
        }
        result?.let { analysis ->
            QuestionAiAnalysisResultCard(analysis)
        }
    }

    if (showSettingsHint) {
        AlertDialog(
            onDismissRequest = { showSettingsHint = false },
            title = { Text("AI 尚未配置") },
            text = { Text("AI 尚未配置，请先在「我的 → AI 设置」中配置 API 地址、API Key 和模型名称。") },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsHint = false
                    onNavigateToSettings()
                }) { Text("去设置") }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsHint = false }) { Text("取消") }
            }
        )
    }

    if (showEnableHint) {
        AlertDialog(
            onDismissRequest = { showEnableHint = false },
            title = { Text("未开启单题 AI 分析") },
            text = { Text("请在 AI 设置中开启「单题 AI 分析」。") },
            confirmButton = {
                TextButton(onClick = {
                    showEnableHint = false
                    onNavigateToSettings()
                }) { Text("去设置") }
            },
            dismissButton = {
                TextButton(onClick = { showEnableHint = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun QuestionAiAnalysisResultCard(result: ShirohaAiClient.AiSingleQuestionAnalysis) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShirohaRadius.Lg),
        color = ShirohaColors.CardWhite68,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "AI 参考分析",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            // 1. 正确答案解释
            AnalysisSection(
                title = "正确答案解释",
                content = "参考答案：${result.suggestedAnswer}"
            )
            result.matchesLocalAnswer?.let { matched ->
                Text(
                    text = if (matched) "与题库答案：一致" else "与题库答案：可能不一致，建议人工确认",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (matched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            // 2. 相关知识点拓展 / 3. 解题思路分析 都由 analysis 字段承担（与现有 SingleQuestionAiResultCard 一致）
            AnalysisSection(
                title = "相关知识点拓展",
                content = result.analysis.takeIf { it.isNotBlank() } ?: "AI 未返回拓展内容。"
            )
            AnalysisSection(
                title = "解题思路分析",
                content = result.analysis.takeIf { it.isNotBlank() } ?: "AI 未返回思路分析。"
            )
            if (result.needsReview) {
                Text(
                    text = "需要人工确认",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
            result.warning.takeIf { it.isNotBlank() }?.let { warning ->
                Text(
                    text = "提示：$warning",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "AI 结果仅供参考，不会自动修改题库答案或解析。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnalysisSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 23.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
