package com.yiqiu.readingquiz.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.Option
import com.yiqiu.readingquiz.data.model.Question
import com.yiqiu.readingquiz.data.model.QuestionType
import com.yiqiu.readingquiz.ui.components.CafeBadge
import com.yiqiu.readingquiz.ui.components.CafeBadgeVariant
import com.yiqiu.readingquiz.ui.components.CafeButton
import com.yiqiu.readingquiz.ui.components.CafeButtonVariant
import com.yiqiu.readingquiz.ui.components.CafeCard
import com.yiqiu.readingquiz.ui.components.CafeEyebrow
import com.yiqiu.readingquiz.ui.components.CafeTopBar
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

private const val TAG = "QuestionEditor"

/**
 * 单题编辑器（cafe-ui 风格）。
 *
 * - 题型 chip 组（CafeBadge Up/Default 切换）
 * - 题干输入
 * - 选项编辑（CafeListRow 风格行 + OutlinedTextField）
 * - 答案 / 解析 / 填空答案 / 章节 ID（CafeCard + CafeEyebrow 标签）
 * - 底部固定操作条（CafeButton Primary + Ghost 删除）
 */
@Composable
fun QuestionEditorScreen(
    articleId: String,
    questionId: String,
    onBack: () -> Unit
) {
    val allQuestions = remember(articleId) { ReadingRepository.getQuestions(articleId) }
    val original = allQuestions.firstOrNull { it.id == questionId }
    if (original == null) {
        NotFoundState(onBack)
        return
    }

    var type by remember { mutableStateOf(original.type) }
    var questionText by remember { mutableStateOf(original.question) }
    var analysisText by remember { mutableStateOf(original.analysis) }
    var sectionIdText by remember { mutableStateOf(original.sectionId ?: "") }
    var answerText by remember { mutableStateOf(original.answer.joinToString(",")) }
    var blankAnswerText by remember { mutableStateOf(original.blankAnswers.joinToString(",")) }
    val options: SnapshotStateList<Option> = remember {
        mutableStateListOf<Option>().apply {
            if (original.options.isNotEmpty()) {
                addAll(original.options)
            } else when (type) {
                QuestionType.SINGLE, QuestionType.MULTIPLE -> {
                    add(Option("A", "")); add(Option("B", ""))
                }
                QuestionType.JUDGE -> {
                    add(Option("A", "正确")); add(Option("B", "错误"))
                }
                else -> { /* no default */ }
            }
        }
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeColors.Bg)
    ) {
        // ===== 顶部栏（cafe-ui TopBar + 右上保存按钮）=====
        CafeTopBar(
            title = if (questionText.isBlank() && original.question.isBlank()) "新建题目" else "编辑题目",
            onBack = onBack,
            actions = {
                CafeButton(
                    text = "保存",
                    onClick = {
                        val err = validate(
                            type = type,
                            question = questionText,
                            options = options.toList(),
                            answerText = answerText,
                            blankAnswerText = blankAnswerText
                        )
                        if (err != null) {
                            errorMsg = err
                            return@CafeButton
                        }
                        val answerList = answerText.split(Regex("[,，\\s]+"))
                            .filter { it.isNotBlank() }
                        val blankList = blankAnswerText.split(Regex("[,，;；]\\s*"))
                            .filter { it.isNotBlank() }
                        val updated = original.copy(
                            type = type,
                            question = questionText.trim(),
                            options = options.toList(),
                            answer = answerList,
                            blankAnswers = if (type == QuestionType.BLANK) blankList else emptyList(),
                            analysis = analysisText.trim(),
                            sectionId = sectionIdText.trim().ifBlank { null },
                            anchorText = original.anchorText
                        )
                        ReadingRepository.updateQuestion(articleId, updated)
                        Log.i(TAG, "saved question: id=${updated.id}")
                        onBack()
                    },
                    enabled = true,
                    variant = CafeButtonVariant.Primary,
                    leadingIcon = Icons.Rounded.Save
                )
            }
        )

        // ===== 编辑区 =====
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = CafeSpacing.containerPad,
                vertical = CafeSpacing.cardPadSm
            ),
            verticalArrangement = Arrangement.spacedBy(CafeSpacing.md)
        ) {
            // 题型选择
            item {
                EditorSectionCard(
                    eyebrow = "题型",
                    title = "QUESTION TYPE"
                ) {
                    Spacer(modifier = Modifier.size(CafeSpacing.xs))
                    Row(horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
                        listOf(
                            QuestionType.SINGLE to "单选",
                            QuestionType.MULTIPLE to "多选",
                            QuestionType.JUDGE to "判断",
                            QuestionType.BLANK to "填空",
                            QuestionType.SHORT to "简答"
                        ).forEach { (t, label) ->
                            TypeChip(
                                text = label,
                                selected = type == t,
                                onClick = {
                                    type = t
                                    when (t) {
                                        QuestionType.JUDGE -> {
                                            options.clear()
                                            options.add(Option("A", "正确"))
                                            options.add(Option("B", "错误"))
                                        }
                                        QuestionType.SINGLE, QuestionType.MULTIPLE -> {
                                            if (options.size < 2) {
                                                options.clear()
                                                options.add(Option("A", ""))
                                                options.add(Option("B", ""))
                                            }
                                        }
                                        QuestionType.BLANK, QuestionType.SHORT -> {
                                            // 选项对填空/简答无意义，但保留以便用户切换回去
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 题干
            item {
                EditorSectionCard(
                    eyebrow = "题干",
                    title = "QUESTION"
                ) {
                    Spacer(modifier = Modifier.size(CafeSpacing.xs))
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        placeholder = { Text("请输入题干…", style = CafeType.bodyCompact, color = CafeColors.Muted) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = CafeType.bodyCompact
                    )
                }
            }

            // 选项
            if (type == QuestionType.SINGLE || type == QuestionType.MULTIPLE || type == QuestionType.JUDGE) {
                item {
                    EditorSectionCard(
                        eyebrow = if (type == QuestionType.JUDGE) "选项（固定）" else "选项",
                        title = "OPTIONS"
                    ) {
                        Spacer(modifier = Modifier.size(CafeSpacing.xs))
                        Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
                            options.forEachIndexed { idx, opt ->
                                OptionEditRow(
                                    key = opt.key,
                                    text = opt.text,
                                    enabled = type != QuestionType.JUDGE,
                                    onTextChange = { newText ->
                                        options[idx] = opt.copy(text = newText)
                                    },
                                    onDelete = {
                                        if (options.size > 2) options.removeAt(idx)
                                    },
                                    onAdd = {
                                        val nextKey = ('A' + options.size).toString()
                                        options.add(Option(nextKey, ""))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 答案
            item {
                EditorSectionCard(
                    eyebrow = "答案" + if (type == QuestionType.MULTIPLE) "（多选用英文逗号分隔，如 A,C）" else "",
                    title = "ANSWER"
                ) {
                    Spacer(modifier = Modifier.size(CafeSpacing.xs))
                    OutlinedTextField(
                        value = answerText,
                        onValueChange = { answerText = it },
                        placeholder = {
                            Text(
                                if (type == QuestionType.JUDGE) "A 或 B" else "如 A 或 A,C",
                                style = CafeType.bodyCompact,
                                color = CafeColors.Muted
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = CafeType.bodyCompact
                    )
                }
            }

            // 填空答案
            if (type == QuestionType.BLANK) {
                item {
                    EditorSectionCard(
                        eyebrow = "填空答案（按题干 ⬚ 顺序，逗号分隔）",
                        title = "BLANK ANSWERS"
                    ) {
                        Spacer(modifier = Modifier.size(CafeSpacing.xs))
                        OutlinedTextField(
                            value = blankAnswerText,
                            onValueChange = { blankAnswerText = it },
                            placeholder = {
                                Text(
                                    "如 答案1,答案2",
                                    style = CafeType.bodyCompact,
                                    color = CafeColors.Muted
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = CafeType.bodyCompact
                        )
                    }
                }
            }

            // 章节 ID
            item {
                EditorSectionCard(
                    eyebrow = "章节 ID（可选，如 S#01）",
                    title = "SECTION"
                ) {
                    Spacer(modifier = Modifier.size(CafeSpacing.xs))
                    OutlinedTextField(
                        value = sectionIdText,
                        onValueChange = { sectionIdText = it },
                        placeholder = {
                            Text(
                                "S#01",
                                style = CafeType.bodyCompact,
                                color = CafeColors.Muted
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = CafeType.bodyCompact
                    )
                }
            }

            // 解析
            item {
                EditorSectionCard(
                    eyebrow = "解析",
                    title = "ANALYSIS"
                ) {
                    Spacer(modifier = Modifier.size(CafeSpacing.xs))
                    OutlinedTextField(
                        value = analysisText,
                        onValueChange = { analysisText = it },
                        placeholder = {
                            Text(
                                "解释答案，可引用文章原句…",
                                style = CafeType.bodyCompact,
                                color = CafeColors.Muted
                            )
                        },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = CafeType.bodyCompact
                    )
                }
            }

            // 错误提示
            errorMsg?.let { msg ->
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CafeBadge(
                            text = msg,
                            variant = CafeBadgeVariant.Wrong
                        )
                    }
                }
            }
        }

        // ===== 底部操作条 =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CafeColors.Surface)
                .padding(CafeSpacing.containerPad),
            horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CafeButton(
                text = "删除",
                onClick = { showDeleteConfirm = true },
                variant = CafeButtonVariant.Ghost,
                leadingIcon = Icons.Rounded.Delete,
                modifier = Modifier.weight(1f),
                fullWidth = true
            )
            CafeButton(
                text = "取消",
                onClick = onBack,
                variant = CafeButtonVariant.Ghost,
                modifier = Modifier.weight(1f),
                fullWidth = true
            )
            CafeButton(
                text = "保存修改",
                onClick = {
                    val err = validate(
                        type = type,
                        question = questionText,
                        options = options.toList(),
                        answerText = answerText,
                        blankAnswerText = blankAnswerText
                    )
                    if (err != null) {
                        errorMsg = err
                        return@CafeButton
                    }
                    val answerList = answerText.split(Regex("[,，\\s]+"))
                        .filter { it.isNotBlank() }
                    val blankList = blankAnswerText.split(Regex("[,，;；]\\s*"))
                        .filter { it.isNotBlank() }
                    val updated = original.copy(
                        type = type,
                        question = questionText.trim(),
                        options = options.toList(),
                        answer = answerList,
                        blankAnswers = if (type == QuestionType.BLANK) blankList else emptyList(),
                        analysis = analysisText.trim(),
                        sectionId = sectionIdText.trim().ifBlank { null },
                        anchorText = original.anchorText
                    )
                    ReadingRepository.updateQuestion(articleId, updated)
                    Log.i(TAG, "saved question: id=${updated.id}")
                    onBack()
                },
                variant = CafeButtonVariant.Primary,
                leadingIcon = Icons.Rounded.Save,
                modifier = Modifier.weight(2f),
                fullWidth = true
            )
        }
    }

    // ===== 删除确认 =====
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除这道题？", style = CafeType.cardTitle, color = CafeColors.Fg) },
            text = { Text("该操作不可撤销。", style = CafeType.bodyCompact, color = CafeColors.Muted) },
            confirmButton = {
                TextButton(onClick = {
                    ReadingRepository.deleteQuestion(articleId, original.id)
                    Log.i(TAG, "deleted question from editor: ${original.id}")
                    showDeleteConfirm = false
                    onBack()
                }) {
                    Text("删除", color = CafeColors.Wrong)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消", color = CafeColors.Muted)
                }
            }
        )
    }
}

/**
 * 编辑器卡片（CafeCard + CafeEyebrow 标签）：统一包裹题干/选项/答案等区段。
 */
@Composable
private fun EditorSectionCard(
    eyebrow: String,
    title: String,
    content: @Composable () -> Unit
) {
    CafeCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
            ) {
                CafeEyebrow(text = title, showLeadingDot = true)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = eyebrow,
                    style = CafeType.meta,
                    color = CafeColors.Muted
                )
            }
            content()
        }
    }
}

/**
 * 题型 Chip：使用 CafeBadge Up/Default 切换（保留原签名）。
 */
@Composable
private fun TypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    CafeBadge(
        text = text,
        variant = if (selected) CafeBadgeVariant.Up else CafeBadgeVariant.Default,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/**
 * 单个选项编辑行：cafe-ui 风格的 key badge + 输入 + 操作图标。
 */
@Composable
private fun OptionEditRow(
    key: String,
    text: String,
    enabled: Boolean,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
    ) {
        // 选项 key 圆形徽章
        Box(
            modifier = Modifier
                .size(optionKeySize)
                .clip(RoundedCornerShape(CafeRadius.rPill))
                .background(CafeColors.Accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = key,
                style = CafeType.bodyCompact.copy(fontWeight = FontWeight.Bold),
                color = CafeColors.Accent
            )
        }
        // 文本输入
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    "选项内容",
                    style = CafeType.bodyCompact,
                    color = CafeColors.Muted
                )
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = enabled,
            textStyle = CafeType.bodyCompact
        )
        // 删除按钮
        Box(
            modifier = Modifier
                .size(optionKeySize)
                .clip(RoundedCornerShape(CafeRadius.rBtn))
                .background(
                    if (enabled) CafeColors.Wrong.copy(alpha = 0.10f) else CafeColors.Border.copy(alpha = 0.4f)
                )
                .clickable(enabled = enabled, onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "删除选项",
                tint = if (enabled) CafeColors.Wrong else CafeColors.Muted,
                modifier = Modifier.size(iconSm)
            )
        }
        // 新增按钮
        Box(
            modifier = Modifier
                .size(optionKeySize)
                .clip(RoundedCornerShape(CafeRadius.rBtn))
                .background(
                    if (enabled) CafeColors.Accent.copy(alpha = 0.12f) else CafeColors.Border.copy(alpha = 0.4f)
                )
                .clickable(enabled = enabled, onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Save,
                contentDescription = "新增选项",
                tint = if (enabled) CafeColors.Accent else CafeColors.Muted,
                modifier = Modifier.size(iconSm)
            )
        }
    }
}

/**
 * 找不到题目：cafe-ui 卡片风格 + 错误 badge。
 */
@Composable
private fun NotFoundState(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeColors.Bg)
    ) {
        CafeTopBar(
            title = "编辑题目",
            onBack = onBack
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(CafeSpacing.containerPad)
        ) {
            CafeCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(CafeSpacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                    ) {
                        CafeEyebrow(text = "NOT FOUND", showLeadingDot = true, textColor = CafeColors.Wrong)
                    }
                    Text(
                        text = "题目不存在或已被删除",
                        style = CafeType.cardTitle,
                        color = CafeColors.Wrong
                    )
                    Text(
                        text = "返回题库列表后重新选择，或新建一道题目。",
                        style = CafeType.bodyCompact,
                        color = CafeColors.Muted
                    )
                    Spacer(modifier = Modifier.size(CafeSpacing.xs))
                    CafeButton(
                        text = "返回",
                        onClick = onBack,
                        variant = CafeButtonVariant.Ghost,
                        fullWidth = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * 表单验证
 */
private fun validate(
    type: QuestionType,
    question: String,
    options: List<Option>,
    answerText: String,
    blankAnswerText: String
): String? {
    if (question.isBlank()) return "题干不能为空"
    when (type) {
        QuestionType.SINGLE -> {
            if (options.size < 2) return "单选题至少 2 个选项"
            if (answerText.isBlank()) return "请填写答案（单选）"
        }
        QuestionType.MULTIPLE -> {
            if (options.size < 2) return "多选题至少 2 个选项"
            if (answerText.isBlank()) return "请填写答案（多选）"
        }
        QuestionType.JUDGE -> {
            if (answerText.isBlank()) return "请填写答案（A=正确 / B=错误）"
        }
        QuestionType.BLANK -> {
            val blanks = blankAnswerText.split(Regex("[,，;；]\\s*"))
                .filter { it.isNotBlank() }
            if (blanks.isEmpty()) return "请填写填空答案"
        }
        QuestionType.SHORT -> {
            if (answerText.isBlank()) return "请提供参考答案"
        }
    }
    return null
}

/**
 * 内部派生尺寸 token：从 CafeSpacing/icon tile 派生，避免硬编码。
 */
private val optionKeySize: Dp = CafeSpacing.kpiPadH
// iconSm 派生自 CafeRadius 的 rBtn（10dp）做小图标尺寸
private val iconSm: Dp = CafeRadius.rBtn