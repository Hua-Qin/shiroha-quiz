package com.yiqiu.readingquiz.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.Option
import com.yiqiu.readingquiz.data.model.Question
import com.yiqiu.readingquiz.data.model.QuestionType
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

private const val TAG = "QuestionEditor"

/**
 * 单题编辑器（修复之前的"编辑器无法打开"问题）。
 *
 * - 题型下拉（5 种）
 * - 题干输入
 * - 选项列表（动态增减；JUDGE 固定 A/B）
 * - 答案输入（多选用逗号分隔）
 * - 填空答案（仅 BLANK 显示）
 * - 解析输入
 * - 章节 ID 输入（关联到 ArticleBlock.Section）
 * - 保存 / 删除 / 返回
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
    // 选项使用 SnapshotStateList 实现动态编辑
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
            Text(
                text = "编辑题目",
                style = CafeType.Heading,
                color = CafeColors.Fg,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "删除",
                    tint = CafeColors.Wrong
                )
            }
        }

        // ===== 编辑区 =====
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = CafeSpacing.ContainerPad,
                vertical = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 题型选择
            item {
                Column {
                    SectionLabel("题型")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    // 切题型时调整选项
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
                Column {
                    SectionLabel("题干")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        placeholder = { Text("请输入题干…") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 选项（仅当 SINGLE/MULTIPLE/JUDGE 显示）
            if (type == QuestionType.SINGLE || type == QuestionType.MULTIPLE || type == QuestionType.JUDGE) {
                item {
                    Column {
                        SectionLabel(if (type == QuestionType.JUDGE) "选项（固定）" else "选项")
                        Spacer(modifier = Modifier.height(8.dp))
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
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // 答案
            item {
                Column {
                    SectionLabel("答案" + if (type == QuestionType.MULTIPLE) "（多选用英文逗号分隔，如 A,C）" else "")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = answerText,
                        onValueChange = { answerText = it },
                        placeholder = { Text(if (type == QuestionType.JUDGE) "A 或 B" else "如 A 或 A,C") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // 填空答案（仅 BLANK）
            if (type == QuestionType.BLANK) {
                item {
                    Column {
                        SectionLabel("填空答案（按题干 ⬚ 顺序，逗号分隔）")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = blankAnswerText,
                            onValueChange = { blankAnswerText = it },
                            placeholder = { Text("如 答案1,答案2") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // 章节 ID
            item {
                Column {
                    SectionLabel("章节 ID（可选，如 S#01）")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sectionIdText,
                        onValueChange = { sectionIdText = it },
                        placeholder = { Text("S#01") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // 解析
            item {
                Column {
                    SectionLabel("解析")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = analysisText,
                        onValueChange = { analysisText = it },
                        placeholder = { Text("解释答案，可引用文章原句…") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 错误提示
            errorMsg?.let { msg ->
                item {
                    Text(
                        text = msg,
                        style = CafeType.Caption,
                        color = CafeColors.Wrong
                    )
                }
            }
        }

        // ===== 底部保存按钮 =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CafeColors.Surface)
                .padding(CafeSpacing.ContainerPad),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("取消", color = CafeColors.Muted)
            }
            TextButton(
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
                        return@TextButton
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
                modifier = Modifier.weight(2f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Save,
                    contentDescription = null,
                    tint = CafeColors.Accent
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("保存修改", color = CafeColors.Accent, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // ===== 删除确认 =====
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除这道题？") },
            text = { Text("该操作不可撤销。") },
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
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }
}

/**
 * 题型 Chip（圆角矩形，单选效果）
 */
@Composable
private fun TypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) CafeColors.Accent else CafeColors.Surface
    val fg = if (selected) Color.White else CafeColors.Fg
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(CafeRadius.Pill))
            .border(
                width = 1.dp,
                color = if (selected) CafeColors.Accent else CafeColors.Border,
                shape = RoundedCornerShape(CafeRadius.Pill)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = CafeType.Caption.copy(fontWeight = FontWeight.Medium),
            color = fg
        )
    }
}

/**
 * 单个选项编辑行
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(CafeColors.Accent.copy(alpha = 0.12f), RoundedCornerShape(CafeRadius.Sm)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = key,
                style = CafeType.Caption.copy(fontWeight = FontWeight.Bold),
                color = CafeColors.Accent
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("选项内容") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = enabled
        )
        IconButton(
            onClick = onDelete,
            enabled = enabled,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "删除选项",
                tint = if (enabled) CafeColors.Muted else CafeColors.Border,
                modifier = Modifier.size(16.dp)
            )
        }
        IconButton(
            onClick = onAdd,
            enabled = enabled,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Save,
                contentDescription = "新增选项",
                tint = if (enabled) CafeColors.Accent else CafeColors.Border,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 字段标签
 */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = CafeType.Caption.copy(fontWeight = FontWeight.SemiBold),
        color = CafeColors.Muted
    )
}

/**
 * 找不到题目的兜底
 */
@Composable
private fun NotFoundState(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(CafeColors.Bg).padding(CafeSpacing.ContainerPad)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "返回",
                    tint = CafeColors.Fg
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "编辑题目", style = CafeType.Heading, color = CafeColors.Fg)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "题目不存在或已被删除", style = CafeType.Heading, color = CafeColors.Wrong)
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