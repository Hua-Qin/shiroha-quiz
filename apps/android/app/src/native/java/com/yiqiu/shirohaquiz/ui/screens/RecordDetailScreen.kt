package com.yiqiu.shirohaquiz.ui.screens

import com.yiqiu.shirohaquiz.ui.theme.shirohaEditorialBackground

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.importer.model.MultiBlankSupport
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.state.StudyQuestionResult
import com.yiqiu.shirohaquiz.state.StudyRecord
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EditorialDivider
import com.yiqiu.shirohaquiz.ui.components.EditorialFigure
import com.yiqiu.shirohaquiz.ui.components.EditorialSection
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.QuestionImagesBlock
import com.yiqiu.shirohaquiz.ui.components.ShirohaDangerConfirmDialog
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.editorialScaleFor
import com.yiqiu.shirohaquiz.ui.theme.screenClassFor
import com.yiqiu.shirohaquiz.ui.theme.uiScaleFor

private enum class RecordQuestionFilter(val label: String) {
    ALL("鍏ㄩ儴棰樼洰"),
    WRONG_ONLY("鍙湅閿欓")
}

@Composable
fun RecordDetailScreen(
    recordId: String?,
    onBack: () -> Unit
) {
    val record = QuizRepository.findStudyRecord(recordId)
    var questionFilter by remember { mutableStateOf(RecordQuestionFilter.ALL) }
    var pendingDelete by remember { mutableStateOf(false) }
    var wrongAddedResultId by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shirohaEditorialBackground()
    ) {
        val screenClass = screenClassFor(maxWidth)
        val scale = editorialScaleFor(screenClass)
        val uiScale = uiScaleFor(screenClass)

        if (record == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
                verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xxl)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
                    ) {
                        ShirohaHeader(
                            kicker = "Record Detail",
                            title = "璁板綍璇︽儏",
                            subtitle = "娌℃湁鎵惧埌杩欐潯瀛︿範璁板綍銆",
                            scale = scale
                        )
                    }
                }
                item {
                    GlassCard { NoticeCard("娌℃湁鎵惧埌杩欐潯瀛︿範璁板綍銆", warning = true) }
                }
                item {
                    ActionPillButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        text = "杩斿洖璁板綍",
                        primary = false,
                        onClick = onBack
                    )
                }
            }
            return@BoxWithConstraints
        }

        val indexedResults = remember(record.questionResults, questionFilter) {
            record.questionResults
                .mapIndexed { index, result -> index + 1 to result }
                .filter { (_, result) -> questionFilter == RecordQuestionFilter.ALL || !result.correct }
        }

        if (pendingDelete) {
            ShirohaDangerConfirmDialog(
                title = "鍒犻櫎杩欐潯瀛︿範璁板綍锛",
                message = "鍒犻櫎鍚庢棤娉曟仮澶嶏紝浣嗕笉浼氬奖鍝嶉搴撱€侀敊棰樻湰鍜屾敹钘忓す銆",
                confirmText = "鍒犻櫎",
                onDismiss = { pendingDelete = false },
                onConfirm = {
                    QuizRepository.deleteStudyRecord(record.id)
                    pendingDelete = false
                    onBack()
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xxl)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
                ) {
                    ShirohaHeader(
                        kicker = formatRecordTime(record.timestamp),
                        title = "璁板綍璇︽儏",
                        subtitle = record.title.ifBlank { record.bankName.ifBlank { "瀛︿範璁板綍" } },
                        scale = scale
                    )
                }
            }

            item {
                EditorialSection(
                    kicker = "Overview",
                    title = "鏈姒傝",
                    scale = scale
                ) {
                    RecordSummaryFigures(record = record, scale = scale)
                }
            }

            item {
                EditorialSection(
                    kicker = "Info",
                    title = "鍏冧俊鎭",
                    scale = scale
                ) {
                    RecordSummaryCard(record = record)
                }
            }

            if (record.questionResults.isEmpty()) {
                item {
                    GlassCard {
                        NoticeCard("杩欐槸涓€鏉℃棫璁板綍锛屽綋鏃舵病鏈変繚瀛橀€愰璇︽儏锛屽彧鑳芥煡鐪嬫憳瑕併€")
                    }
                }
            } else {
                item {
                    EditorialSection(
                        kicker = "Details",
                        title = "绛旈鏄庣粏",
                        scale = scale
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm)
                        ) {
                            RecordQuestionFilter.entries.forEach { item ->
                                ActionPillButton(
                                    icon = if (item == RecordQuestionFilter.WRONG_ONLY) Icons.Rounded.Close else Icons.Rounded.CheckCircle,
                                    text = item.label,
                                    primary = questionFilter == item,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height((44 * uiScale).coerceAtLeast(36f).dp),
                                    fillWidthContent = true,
                                    onClick = { questionFilter = item }
                                )
                            }
                        }
                    }
                }

                if (indexedResults.isEmpty()) {
                    item {
                        GlassCard { NoticeCard("杩欐潯璁板綍閲屾病鏈夐敊棰樸€") }
                    }
                } else {
                    items(
                        items = indexedResults,
                        key = { (index, result) ->
                            "${result.sourceBankId.orEmpty()}#${result.question.id}#$index"
                        }
                    ) { (index, result) ->
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            QuestionResultCard(index = index, result = result)
                            if (!result.correct) {
                                WrongQuestionActionRow(
                                    result = result,
                                    justAdded = wrongAddedResultId == result.question.id,
                                    onAddToWrongBook = {
                                        val bankId = result.sourceBankId
                                        val bank = bankId?.let { id ->
                                            QuizRepository.banks.firstOrNull { it.id == id }
                                        }
                                        if (bank != null) {
                                            QuizRepository.addWrongContext(
                                                bank = bank,
                                                question = result.question,
                                                userAnswer = result.userAnswer,
                                                source = "璇︽儏椤垫墜鍔ㄦ坊鍔",
                                                addedManually = true
                                            )
                                            wrongAddedResultId = result.question.id
                                        }
                                    }
                                )
                            }
                        }
                        EditorialDivider()
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm)
                ) {
                    ActionPillButton(
                        icon = Icons.Rounded.DeleteOutline,
                        text = "鍒犻櫎璁板綍",
                        primary = false,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { pendingDelete = true }
                    )
                    ActionPillButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        text = "杩斿洖",
                        primary = true,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onBack
                    )
                }
            }

            item {
                Spacer(Modifier.height(ShirohaSpacing.Md))
            }
        }
    }
}

@Composable
private fun RecordSummaryFigures(record: StudyRecord, scale: Float) {
    val wrong = (record.total - record.correct).coerceAtLeast(0)
    val accuracy = if (record.total == 0) 0 else record.correct * 100 / record.total

    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = record.total.toString(),
                label = "鎬婚鏁",
                unit = "棰"
            )
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = record.correct.toString(),
                label = "绛斿",
                unit = "棰"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = wrong.toString(),
                label = "绛旈敊",
                unit = "棰"
            )
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = accuracy.toString(),
                label = "姝ｇ‘鐜",
                unit = "%"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = record.durationSeconds?.let { formatDuration(it) } ?: "鈥",
                label = "鐢ㄦ椂"
            )
            if (record.totalScore != null && record.earnedScore != null) {
                EditorialFigure(
                    modifier = Modifier.weight(1f),
                    scale = scale,
                    value = "${record.earnedScore.trimScore()}/${record.totalScore.trimScore()}",
                    label = "寰楀垎",
                    unit = "鍒"
                )
            } else {
                EditorialFigure(
                    modifier = Modifier.weight(1f),
                    scale = scale,
                    value = "鈥",
                    label = "寰楀垎"
                )
            }
        }
    }
}

@Composable
private fun RecordSummaryCard(
    record: StudyRecord,
    onBack: () -> Unit
) {
    val startedAt = record.startedAt ?: record.durationSeconds?.let { record.timestamp - it * 1000L } ?: record.timestamp

    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip(record.source, selected = true)
            StatusChip(record.bankName)
        }
        Text(
            text = record.title.ifBlank { "瀛︿範璁板綍" },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        InfoLine("棰樺簱", record.bankName)
        InfoLine("妯″紡", record.source)
        InfoLine("寮€濮嬫椂闂", formatRecordTime(startedAt))
        InfoLine("缁撴潫鏃堕棿", formatRecordTime(record.timestamp))
        if (record.autoSubmitted) {
            InfoLine("浜ゅ嵎鏂瑰紡", "鑷姩浜ゅ嵎")
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = ShirohaColors.TextSecondary,
            modifier = Modifier
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WrongQuestionActionRow(
    result: StudyQuestionResult,
    justAdded: Boolean,
    onAddToWrongBook: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = ShirohaSpacing.Sm, bottom = ShirohaSpacing.Sm),
        horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionPillButton(
            icon = if (justAdded) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
            text = if (justAdded) "宸插姞鍏ラ敊棰樻湰" else "鍔犲叆閿欓鏈",
            primary = !justAdded,
            onClick = { if (!justAdded) onAddToWrongBook() }
        )
    }
}

@Composable
private fun QuestionResultCard(
    index: Int,
    result: StudyQuestionResult
) {
    val question = result.question
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusChip("绗?$index 棰", selected = true)
                StatusChip(typeLabelForRecord(question.type))
            }
            StatusChip(if (result.correct) "姝ｇ‘" else "閿欒", selected = result.correct)
        }
        result.sourceBankName?.takeIf { it.isNotBlank() }?.let { sourceBankName ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = "鏉ユ簮锛?sourceBankName",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = question.question,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (question.images.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            QuestionImagesBlock(question.images, maxPreviewHeight = 260.dp, showMeta = false)
        }
        if (question.options.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            question.options.forEach { option ->
                Text(
                    text = "${option.key}. ${option.text}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (result.correct) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
                contentDescription = null,
                tint = if (result.correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text(
                text = if (result.correct) "鏈鍥炵瓟姝ｇ‘" else "鏈鍥炵瓟閿欒",
                color = if (result.correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(8.dp))
        val structuredBlank = MultiBlankSupport.hasStructuredAnswers(question)
        val shownUserBlankAnswers = result.userBlankAnswers.ifEmpty {
            if (structuredBlank) result.userAnswer else emptyList()
        }
        Text(
            text = if (structuredBlank) {
                "浣犵殑绛旀锛歕n${MultiBlankSupport.userAnswerText(shownUserBlankAnswers)}"
            } else {
                "浣犵殑绛旀锛?{result.userAnswer.joinToString(" / ").ifBlank { "鏈綔绛" }}"
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (structuredBlank) {
                "姝ｇ‘绛旀锛歕n${result.answerText.ifBlank { MultiBlankSupport.expectedAnswerText(question.blankAnswers) }}"
            } else {
                "姝ｇ‘绛旀锛?{result.answerText.ifBlank { question.answer.joinToString(" / ").ifBlank { "鏈瘑鍒瓟妗" } }}"
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (result.maxScore != null && result.earnedScore != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "寰楀垎锛?{result.earnedScore.trimScore()} / ${result.maxScore.trimScore()} 鍒",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (question.analysis.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "瑙ｆ瀽锛?{question.analysis}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun typeLabelForRecord(type: QuestionType): String = when (type) {
    QuestionType.SINGLE -> "鍗曢€夐"
    QuestionType.MULTIPLE -> "澶氶€夐"
    QuestionType.JUDGE -> "鍒ゆ柇棰"
    QuestionType.BLANK -> "濉┖棰"
    QuestionType.SHORT -> "绠€绛旈"
}


