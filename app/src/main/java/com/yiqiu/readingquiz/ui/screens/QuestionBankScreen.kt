package com.yiqiu.readingquiz.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import com.yiqiu.readingquiz.ai.ReadingAiClient
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.importexport.QuestionImportParser
import com.yiqiu.readingquiz.data.model.Question
import com.yiqiu.readingquiz.data.model.QuestionType
import com.yiqiu.readingquiz.ui.components.CafeBadge
import com.yiqiu.readingquiz.ui.components.CafeBadgeVariant
import com.yiqiu.readingquiz.ui.components.CafeButton
import com.yiqiu.readingquiz.ui.components.CafeButtonAi
import com.yiqiu.readingquiz.ui.components.CafeButtonVariant
import com.yiqiu.readingquiz.ui.components.CafeCard
import com.yiqiu.readingquiz.ui.components.CafeCtaBanner
import com.yiqiu.readingquiz.ui.components.CafeTopBar
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "QuestionBank"

/**
 * 题库管理页（cafe-ui 风格 + AI 智能出题 + 导入 + 编辑器入口 + 长按删除）。
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
    var questions by remember(articleId) { mutableStateOf(ReadingRepository.getQuestions(articleId)) }
    var pendingDelete by remember { mutableStateOf<Question?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(false) }
    var generating by remember { mutableStateOf(false) }
    var showImportHint by remember { mutableStateOf(false) }

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
        // ===== 顶部栏（cafe-ui TopBar）=====
        CafeTopBar(
            title = "题目库",
            subtitle = article?.title ?: "未找到文章",
            onBack = onBack,
            actions = {
                CafeBadge(
                    text = "共 ${questions.size} 题",
                    variant = CafeBadgeVariant.Up
                )
            }
        )

        // ===== 醒目的 AI 智能出题 CTA Banner =====
        AiGenerateCta(
            enabled = !generating && article != null,
            loading = generating,
            onClick = { triggerAiGenerate() }
        )

        // ===== 操作栏（导入 / 手动新增 / 答题）=====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CafeSpacing.containerPad),
            horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
        ) {
            CafeButton(
                text = "导入题目",
                onClick = {
                    importLauncher.launch(arrayOf("application/json", "text/markdown", "text/plain", "text/*"))
                    showImportHint = true
                },
                enabled = !generating,
                variant = CafeButtonVariant.Ghost,
                leadingIcon = Icons.Rounded.FileUpload,
                modifier = Modifier.weight(1f),
                fullWidth = true
            )
            CafeButton(
                text = "手动新增",
                onClick = {
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
                variant = CafeButtonVariant.Ghost,
                leadingIcon = Icons.Rounded.Add,
                modifier = Modifier.weight(1f),
                fullWidth = true
            )
            CafeButton(
                text = "答题",
                onClick = { onEnterQuiz(articleId) },
                enabled = questions.isNotEmpty(),
                variant = CafeButtonVariant.Primary,
                leadingIcon = Icons.Rounded.PlayArrow,
                modifier = Modifier.weight(1f),
                fullWidth = true
            )
        }

        // ===== 状态提示 =====
        status?.let { msg ->
            StatusMessage(text = msg, isError = statusIsError)
        }
        if (showImportHint && status == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CafeSpacing.containerPad, vertical = CafeSpacing.xs)
            ) {
                Text(
                    text = "支持 .json / .md / .txt 三种格式",
                    style = CafeType.meta,
                    color = CafeColors.Muted
                )
            }
        }

        // ===== 题目列表 =====
        if (questions.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = CafeSpacing.containerPad,
                    end = CafeSpacing.containerPad,
                    top = CafeSpacing.cardPadSm,
                    bottom = CafeSpacing.sectionY
                ),
                verticalArrangement = Arrangement.spacedBy(CafeSpacing.cardPadSm)
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
            title = { Text("删除这道题？", style = CafeType.cardTitle, color = CafeColors.Fg) },
            text = {
                Column {
                    Text(
                        text = target.question.take(80) + if (target.question.length > 80) "…" else "",
                        style = CafeType.bodyCompact,
                        color = CafeColors.Fg
                    )
                    Spacer(modifier = Modifier.size(CafeSpacing.xs))
                    Text(
                        text = "该操作不可撤销。",
                        style = CafeType.meta,
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
                    Spacer(modifier = Modifier.size(CafeSpacing.xs))
                    Text("删除", color = CafeColors.Wrong)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("取消", color = CafeColors.Muted)
                }
            }
        )
    }
}

/**
 * 醒目的 AI 智能出题 CTA 区（cafe-ui banner 风格，保留私有函数签名）。
 */
@Composable
private fun AiGenerateButton(
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    AiGenerateCta(enabled = enabled, loading = loading, onClick = onClick)
}

/**
 * AI 智能出题 CTA：使用 CafeCtaBanner 风格 + CafeButtonAi 主按钮。
 */
@Composable
private fun AiGenerateCta(
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    CafeCtaBanner(
        title = if (loading) "AI 正在生成…" else "AI 智能出题",
        body = if (loading) "请稍候，约 10-30 秒" else "资深教研员模板 · 数量按文章自主决定",
        modifier = Modifier.padding(
            horizontal = CafeSpacing.containerPad,
            vertical = CafeSpacing.cardPadSm
        ),
        primaryButton = {
            CafeButtonAi(
                text = if (loading) "生成中" else "开始生成",
                onClick = onClick,
                enabled = enabled,
                loading = loading,
                onBanner = true
            )
        },
        secondaryButton = {
            // 通过 leadingIcon 保留 AI 图标语义
            CafeButton(
                text = "Auto",
                onClick = onClick,
                enabled = enabled,
                variant = CafeButtonVariant.OnDark,
                leadingIcon = Icons.Rounded.AutoAwesome
            )
        }
    )
}

/**
 * 状态提示条：成功用 Correct badge 风格，失败用 Wrong 风格。
 */
@Composable
private fun StatusMessage(text: String, isError: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CafeSpacing.containerPad, vertical = CafeSpacing.xs)
    ) {
        CafeBadge(
            text = text,
            variant = if (isError) CafeBadgeVariant.Wrong else CafeBadgeVariant.Correct
        )
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
        Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
            // 顶部：序号 + 题型 + 章节 + 已答状态
            Row(verticalAlignment = Alignment.CenterVertically) {
                CafeBadge(
                    text = "#$index",
                    variant = CafeBadgeVariant.Filled
                )
                Spacer(modifier = Modifier.size(CafeSpacing.xs))
                CafeBadge(
                    text = typeLabel(question.type),
                    variant = CafeBadgeVariant.Default
                )
                if (question.sectionId != null) {
                    Spacer(modifier = Modifier.size(CafeSpacing.xs))
                    CafeBadge(
                        text = "§ ${question.sectionId}",
                        variant = CafeBadgeVariant.Default
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (question.answer.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "已配置答案",
                            tint = CafeColors.Accent2,
                            modifier = Modifier.size(iconSm)
                        )
                        Spacer(modifier = Modifier.size(CafeSpacing.xs))
                        Text(
                            text = "已配置答案",
                            style = CafeType.meta,
                            color = CafeColors.Accent2
                        )
                    }
                }
            }

            // 中部：题干
            Text(
                text = question.question.ifBlank { "（题干为空）" },
                style = CafeType.body,
                color = if (question.question.isBlank()) CafeColors.Muted else CafeColors.Fg,
                maxLines = 3
            )

            // 底部：选项数 meta + 编辑入口
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${question.options.size} OPTIONS · ${typeMeta(question.type)}",
                    style = CafeType.meta,
                    color = CafeColors.Muted,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "编辑",
                    tint = CafeColors.Muted,
                    modifier = Modifier.size(iconSm)
                )
            }
        }
    }
}

/**
 * 空态：cafe-ui 卡片风格 + icon + 文案
 */
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(CafeSpacing.containerPad)
    ) {
        CafeCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FileUpload,
                    contentDescription = null,
                    tint = CafeColors.Muted,
                    modifier = Modifier.size(CafeSpacing.iconTileSize)
                )
                Text(
                    text = "暂无题目",
                    style = CafeType.cardTitle,
                    color = CafeColors.Fg
                )
                Text(
                    text = "点击上方「AI 智能出题」或「导入题目」开始",
                    style = CafeType.bodyXSmall,
                    color = CafeColors.Muted
                )
            }
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
 * 题型 → meta 描述（用于卡片底栏）。
 */
private fun typeMeta(type: QuestionType): String = when (type) {
    QuestionType.SINGLE -> "SINGLE CHOICE"
    QuestionType.MULTIPLE -> "MULTIPLE CHOICE"
    QuestionType.JUDGE -> "TRUE / FALSE"
    QuestionType.BLANK -> "FILL IN BLANKS"
    QuestionType.SHORT -> "SHORT ANSWER"
}

/**
 * 内部 icon 尺寸 token：派生自 CafeRadius.rBtn（10dp）作为小图标尺寸。
 */
private val iconSm = com.yiqiu.readingquiz.ui.theme.CafeRadius.rBtn