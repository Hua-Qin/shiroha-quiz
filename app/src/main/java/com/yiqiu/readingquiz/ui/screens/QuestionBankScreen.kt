package com.yiqiu.readingquiz.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ai.ReadingAiClient
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.importexport.QuestionImportParser
import com.yiqiu.readingquiz.data.model.Question
import com.yiqiu.readingquiz.data.model.QuestionType
import com.yiqiu.readingquiz.ui.components.CafeCard
import com.yiqiu.readingquiz.ui.components.CafeGhostButton
import com.yiqiu.readingquiz.ui.components.CafePrimaryButton
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "QuestionBank"

/**
 * 题库管理页（题目列表 UI 优化 + AI 智能出题 + 导入 + 编辑器入口 + 长按删除）。
 *
 * @param articleId 当前文章 ID
 * @param onBack 返回
 * @param onEnterQuiz 进入答题（已生成题目时）
 * @param onEditQuestion 打开编辑器（编辑单题）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestionBankScreen(
    articleId: String,
    onBack: () -> Unit,
    onEnterQuiz: (articleId: String) -> Unit,
    onEditQuestion: (articleId: String, questionId: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val article = remember(articleId) { ReadingRepository.getArticle(articleId) }
    // 题目列表用可变状态，Repository 变化时同步刷新
    var questions by remember(articleId) { mutableStateOf(ReadingRepository.getQuestions(articleId)) }
    var pendingDelete by remember { mutableStateOf<Question?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(false) }
    var generating by remember { mutableStateOf(false) }
    var showImportHint by remember { mutableStateOf(false) }

    // 加载题目（每次进入页面从 Repository 刷新一次）
    LaunchedEffect(articleId) {
        questions = ReadingRepository.getQuestions(articleId)
        Log.d(TAG, "open: articleId=$articleId, count=${questions.size}")
    }

    /**
     * 文件导入 launcher
     */
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            status = "已取消导入"
            statusIsError = false
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val raw = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader(Charsets.UTF_8).readText()
                    }
                }.getOrNull()
            } ?: run {
                status = "读取文件失败"
                statusIsError = true
                return@launch
            }
            val name = uri.lastPathSegment ?: "questions.json"
            val result = withContext(Dispatchers.Default) {
                QuestionImportParser.importFromText(raw, name)
            }
            when (result) {
                is QuestionImportParser.Result.Success -> {
                    ReadingRepository.addQuestions(articleId, result.questions)
                    questions = ReadingRepository.getQuestions(articleId)
                    status = "已导入 ${result.questions.size} 道题"
                    statusIsError = false
                    Log.i(TAG, "imported: ${result.questions.size} questions")
                }
                is QuestionImportParser.Result.Failure -> {
                    status = "导入失败：${result.reason}"
                    statusIsError = true
                    Log.w(TAG, "import failed: ${result.reason}")
                }
            }
        }
    }

    /**
     * 一键生成专业题库（协程异步）。
     * 不传 count → AI 根据文章字数/章节数自主决定题目数量。
     */

    fun triggerAiGenerate(count: Int = 0) {
        if (article == null) {
            status = "文章不存在"
            statusIsError = true
            return
        }
        generating = true
        status = null
        scope.launch {
            val config = ReadingRepository.aiConfig.value
            val result = withContext(Dispatchers.IO) {
                ReadingAiClient.generateQuestionBank(config, article, count)
            }
            when (result) {
                is ReadingAiClient.AiResult.Success -> {
                    ReadingRepository.addQuestions(articleId, result.value)
                    questions = ReadingRepository.getQuestions(articleId)
                    status = "AI 已生成 ${result.value.size} 道题"
                    statusIsError = false
                }
                is ReadingAiClient.AiResult.Failure -> {
                    status = "AI 生成失败：${result.message}"
                    statusIsError = true
                }
            }
            generating = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeColors.Bg)
    ) {
        // ===== 顶部栏 =====
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
            Spacer(modifier = Modifier.size(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "题目库", style = CafeType.Heading, color = CafeColors.Fg)
                Text(
                    text = article?.title ?: "未找到文章",
                    style = CafeType.Caption,
                    color = CafeColors.Muted,
                    maxLines = 1
                )
            }
            // 总数徽章
            Box(
                modifier = Modifier
                    .background(CafeColors.Accent.copy(alpha = 0.12f), RoundedCornerShape(CafeRadius.Pill))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "共 ${questions.size} 题",
                    style = CafeType.Caption.copy(fontWeight = FontWeight.SemiBold),
                    color = CafeColors.Accent
                )
            }
        }

        // ===== 醒目的 AI 智能出题按钮 =====
        AiGenerateButton(
            enabled = !generating && article != null,
            loading = generating,
            onClick = { triggerAiGenerate() }
        )

        // ===== 操作栏（导入 / 进入答题 / 新增空题）=====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CafeSpacing.ContainerPad, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CafeGhostButton(
                text = "导入题目",
                onClick = {
                    importLauncher.launch(arrayOf("application/json", "text/markdown", "text/plain", "text/*"))
                    showImportHint = true
                },
                enabled = !generating,
                modifier = Modifier.weight(1f)
            )
            CafeGhostButton(
                text = "手动新增",
                onClick = {
                    // 新建空题 + 立即跳转编辑器
                    val newQ = Question(
                        id = java.util.UUID.randomUUID().toString(),
                        type = QuestionType.SINGLE,
                        question = "",
                        options = listOf(
                            com.yiqiu.readingquiz.data.model.Option("A", ""),
                            com.yiqiu.readingquiz.data.model.Option("B", "")
                        ),
                        answer = emptyList(),
                        blankAnswers = emptyList(),
                        analysis = ""
                    )
                    ReadingRepository.addQuestions(articleId, listOf(newQ))
                    onEditQuestion(articleId, newQ.id)
                },
                enabled = !generating,
                modifier = Modifier.weight(1f)
            )
            CafePrimaryButton(
                text = "答题",
                onClick = { onEnterQuiz(articleId) },
                enabled = questions.isNotEmpty(),
                modifier = Modifier.weight(1f)
            )
        }

        // ===== 状态提示 =====
        status?.let { msg ->
            Text(
                text = msg,
                style = CafeType.Caption,
                color = if (statusIsError) CafeColors.Wrong else CafeColors.Accent2,
                modifier = Modifier.padding(horizontal = CafeSpacing.ContainerPad, vertical = 4.dp)
            )
        }
        if (showImportHint && status == null) {
            Text(
                text = "支持 .json / .md / .txt 三种格式",
                style = CafeType.Caption,
                color = CafeColors.Muted,
                modifier = Modifier.padding(horizontal = CafeSpacing.ContainerPad)
            )
        }

        // ===== 题目列表 =====
        if (questions.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = CafeSpacing.ContainerPad,
                    end = CafeSpacing.ContainerPad,
                    top = 8.dp,
                    bottom = 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(questions, key = { it.id }) { q ->
                    QuestionRow(
                        question = q,
                        index = questions.indexOf(q) + 1,
                        onClick = { onEditQuestion(articleId, q.id) },
                        onLongClick = { pendingDelete = q }
                    )
                }
            }
        }
    }

    // ===== 删除确认对话框 =====
    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除这道题？") },
            text = {
                Column {
                    Text(
                        text = target.question.take(80) + if (target.question.length > 80) "…" else "",
                        style = CafeType.Body,
                        color = CafeColors.Fg
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "该操作不可撤销。",
                        style = CafeType.Caption,
                        color = CafeColors.Muted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    ReadingRepository.deleteQuestion(articleId, target.id)
                    questions = ReadingRepository.getQuestions(articleId)
                    Log.i(TAG, "deleted question: ${target.id}")
                    pendingDelete = null
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = CafeColors.Wrong
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("删除", color = CafeColors.Wrong)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            }
        )
    }
}

/**
 * 醒目的 AI 智能出题按钮（带渐变 + 大图标 + 副标题）
 */
@Composable
private fun AiGenerateButton(
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (enabled) CafeColors.Accent else CafeColors.Neutral
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CafeSpacing.ContainerPad, vertical = 8.dp)
            .background(bgColor, RoundedCornerShape(CafeRadius.Hero))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(CafeRadius.Card)),
                contentAlignment = Alignment.Center
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (loading) "AI 正在生成…" else "AI 智能出题",
                    style = CafeType.Heading.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
                Text(
                    text = if (loading) "请稍候，约 10-30 秒" else "资深教研员模板 · 数量按文章自主决定",
                    style = CafeType.Caption,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 单条题目行（点击编辑 + 长按删除）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuestionRow(
    question: Question,
    index: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    CafeCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // 序号 + 题型徽章
            Box(
                modifier = Modifier
                    .background(typeColor(question.type), RoundedCornerShape(CafeRadius.Md))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    style = CafeType.Caption.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                // 题型 + 章节
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = typeLabel(question.type),
                        style = CafeType.Caption.copy(fontWeight = FontWeight.SemiBold),
                        color = typeColor(question.type)
                    )
                    if (question.sectionId != null) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "📍 ${question.sectionId}",
                            style = CafeType.Caption,
                            color = CafeColors.Muted
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (question.answer.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "已答",
                                tint = CafeColors.Accent2,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.size(2.dp))
                            Text(
                                text = "已配置答案",
                                style = CafeType.Caption,
                                color = CafeColors.Accent2
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = question.question.ifBlank { "（题干为空）" },
                    style = CafeType.Body,
                    color = if (question.question.isBlank()) CafeColors.Muted else CafeColors.Fg,
                    maxLines = 3
                )
                if (question.options.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = question.options.take(4).joinToString("  ") { "${it.key}.${it.text.take(20)}" },
                        style = CafeType.Caption,
                        color = CafeColors.Muted,
                        maxLines = 2
                    )
                }
            }
            Spacer(modifier = Modifier.size(4.dp))
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "编辑",
                tint = CafeColors.Muted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.FileUpload,
                contentDescription = null,
                tint = CafeColors.Muted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "暂无题目",
                style = CafeType.Heading,
                color = CafeColors.Muted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "点击上方「AI 智能出题」或「导入题目」开始",
                style = CafeType.Caption,
                color = CafeColors.Muted
            )
        }
    }
}

/**
 * 题型 → 中文标签
 */
private fun typeLabel(type: QuestionType): String = when (type) {
    QuestionType.SINGLE -> "单选"
    QuestionType.MULTIPLE -> "多选"
    QuestionType.JUDGE -> "判断"
    QuestionType.BLANK -> "填空"
    QuestionType.SHORT -> "简答"
}

/**
 * 题型 → 主题色
 */
private fun typeColor(type: QuestionType): Color = when (type) {
    QuestionType.SINGLE -> Color(0xFF5D4432)        // Accent
    QuestionType.MULTIPLE -> Color(0xFF16A34A)      // Accent2
    QuestionType.JUDGE -> Color(0xFF2563EB)
    QuestionType.BLANK -> Color(0xFFD97706)
    QuestionType.SHORT -> Color(0xFF7C3AED)
}