package com.yiqiu.shirohaquiz.ui.screens

import com.yiqiu.shirohaquiz.ui.theme.shirohaEditorialBackground

import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.state.DEFAULT_BANK_GROUP_NAME
import com.yiqiu.shirohaquiz.state.QuizBank
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.state.StudyRecord
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EditorialDivider
import com.yiqiu.shirohaquiz.ui.components.EditorialFigure
import com.yiqiu.shirohaquiz.ui.components.EditorialSection
import com.yiqiu.shirohaquiz.ui.components.EmptyStateIllustration
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaDangerConfirmDialog
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.editorialScaleFor
import com.yiqiu.shirohaquiz.ui.theme.screenClassFor
import com.yiqiu.shirohaquiz.ui.theme.uiScaleFor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val RECORD_SCOPE_ALL = "all"
private const val RECORD_SCOPE_BANK_PREFIX = "bank:"

@Composable
fun RecordsScreen(
    onBack: () -> Unit,
    onOpenRecord: (String) -> Unit = {}
) {
    val records = QuizRepository.studyRecords.toList()
    val banks = QuizRepository.banks.toList()
    var selectedScopeKey by rememberSaveable { mutableStateOf(RECORD_SCOPE_ALL) }
    var showScopeDialog by remember { mutableStateOf(false) }
    var pendingDeleteRecord by remember { mutableStateOf<StudyRecord?>(null) }

    val selectedBankId = selectedScopeKey
        .takeIf { it.startsWith(RECORD_SCOPE_BANK_PREFIX) }
        ?.removePrefix(RECORD_SCOPE_BANK_PREFIX)
    val selectedBank = selectedBankId?.let { id -> banks.firstOrNull { it.id == id } }
    val effectiveScopeKey = if (selectedBankId == null || selectedBank != null) {
        selectedScopeKey
    } else {
        RECORD_SCOPE_ALL
    }
    val bankNameCounts = banks
        .map { it.name.trim() }
        .filter { it.isNotBlank() }
        .groupingBy { it }
        .eachCount()
    val visibleRecords = selectedBank?.let { bank ->
        val bankNameIsUnique = bankNameCounts[bank.name.trim()] == 1
        records.filter { record -> recordMatchesBank(record, bank, bankNameIsUnique) }
    } ?: records

    if (showScopeDialog) {
        RecordScopeDialog(
            banks = banks,
            records = records,
            selectedScopeKey = effectiveScopeKey,
            bankNameCounts = bankNameCounts,
            onSelect = { key ->
                selectedScopeKey = key
                showScopeDialog = false
            },
            onDismiss = { showScopeDialog = false }
        )
    }

    pendingDeleteRecord?.let { targetRecord ->
        ShirohaDangerConfirmDialog(
            title = "鍒犻櫎杩欐潯瀛︿範璁板綍锛",
            message = "鍒犻櫎鍚庢棤娉曟仮澶嶏紝浣嗕笉浼氬奖鍝嶉搴撱€侀敊棰樻湰鍜屾敹钘忓す銆",
            confirmText = "鍒犻櫎",
            onDismiss = { pendingDeleteRecord = null },
            onConfirm = {
                QuizRepository.deleteStudyRecord(targetRecord.id)
                pendingDeleteRecord = null
            }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shirohaEditorialBackground()
    ) {
        val screenClass = screenClassFor(maxWidth)
        val scale = editorialScaleFor(screenClass)
        val uiScale = uiScaleFor(screenClass)

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
                        kicker = "History",
                        title = "瀛︿範璁板綍",
                        subtitle = if (records.isEmpty()) {
                            "瀹屾垚缁冧範鎴栬€冭瘯鍚庯紝璁板綍浼氳嚜鍔ㄥ嚭鐜板湪杩欓噷銆"
                        } else {
                            "鍏?${records.size} 鏉″涔犺褰曘€"
                        },
                        scale = scale
                    )
                }
            }

            if (records.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
                    ) {
                        EmptyStateIllustration(
                            title = "杩欓噷杩樻病鏈夊涔犺褰",
                            message = "瀹屾垚缁冧範鎴栬€冭瘯鍚庯紝璁板綍浼氳嚜鍔ㄥ嚭鐜板湪杩欓噷銆",
                            imageRes = R.drawable.illus_rest_state_webp,
                            action = { Spacer(Modifier.height((12 * uiScale).dp)) }
                        )
                        GlassCard {
                            ActionPillButton(
                                icon = Icons.AutoMirrored.Rounded.Undo,
                                text = "杩斿洖",
                                primary = false,
                                onClick = onBack
                            )
                        }
                    }
                }
            }

            if (records.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
                    ) {
                        IllustrationHeroCard(
                            title = "瀛︿範璁板綍浼氬湪杩欓噷鎱㈡參绉疮",
                            subtitle = "缁冧範鍜岃€冭瘯閮戒細鏀跺綍鍒拌繖閲屻€",
                            imageRes = R.drawable.illus_rest_state_webp,
                            imageSize = 88.dp,
                            scale = scale
                        )

                        EditorialSection(
                            kicker = "Overview",
                            title = "鏁版嵁姒傝",
                            scale = scale
                        ) {
                            RecordsOverviewFigures(
                                records = records,
                                scale = scale
                            )
                        }

                        EditorialSection(
                            kicker = "Filter",
                            title = "绛涢€",
                            scale = scale
                        ) {
                            RecordsFilterBlock(
                                selectedBank = selectedBank,
                                records = records,
                                visibleRecords = visibleRecords,
                                uiScale = uiScale,
                                onPickBank = { showScopeDialog = true }
                            )
                        }

                        EditorialSection(
                            kicker = "Records",
                            title = "璁板綍",
                            scale = scale
                        ) {
                            if (visibleRecords.isEmpty()) {
                                GlassCard {
                                    NoticeCard("璇ラ搴撴殏鏃犲涔犺褰曘€傚彲浠ュ垏鎹㈠埌鍏朵粬棰樺簱鎴栧叏閮ㄨ褰曘€")
                                }
                            }
                        }
                    }
                }

                if (visibleRecords.isNotEmpty()) {
                    items(
                        items = visibleRecords,
                        key = { record -> record.id }
                    ) { record ->
                        RecordCard(
                            record = record,
                            onClick = { onOpenRecord(record.id) },
                            onDelete = { pendingDeleteRecord = record }
                        )
                        EditorialDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordsOverviewFigures(
    records: List<StudyRecord>,
    scale: Float
) {
    val totalCount = records.size
    val totalDurationSeconds = records.sumOf { it.durationSeconds ?: 0 }
    val totalMinutes = totalDurationSeconds / 60
    val totalQuestions = records.sumOf { it.total }
    val totalCorrect = records.sumOf { it.correct }
    val averageScore = if (totalQuestions == 0) 0 else totalCorrect * 100 / totalQuestions

    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = totalCount.toString(),
                label = "鎬昏褰曟暟",
                unit = "鏉"
            )
            EditorialFigure(
                modifier = Modifier.weight(1f),
                scale = scale,
                value = if (totalMinutes >= 60) {
                    "${totalMinutes / 60}h${(totalMinutes % 60).toString().padStart(2, '0')}"
                } else {
                    totalMinutes.toString()
                },
                label = "鎬荤敤鏃",
                unit = if (totalMinutes >= 60) "" else "鍒"
            )
        }
        EditorialFigure(
            modifier = Modifier.fillMaxWidth(),
            scale = scale,
            value = averageScore.toString(),
            label = "骞冲潎姝ｇ‘鐜",
            unit = "%"
        )
    }
}

@Composable
private fun RecordsFilterBlock(
    selectedBank: QuizBank?,
    records: List<StudyRecord>,
    visibleRecords: List<StudyRecord>,
    uiScale: Float,
    onPickBank: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)) {
        Text(
            text = "鏃ユ湡鑼冨洿 / 绫诲瀷 / 棰樺簱",
            style = MaterialTheme.typography.bodyMedium,
            color = ShirohaColors.TextSecondary
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shirohaNoRippleClickable { onPickBank() },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(ShirohaRadius.Md),
            color = ShirohaColors.CardWhite86,
            border = BorderStroke(1.dp, ShirohaColors.LineStrong)
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = (14 * uiScale).coerceAtLeast(10f).dp,
                    vertical = (12 * uiScale).coerceAtLeast(8f).dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedBank?.name ?: "鍏ㄩ儴璁板綍",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = selectedBank?.let { bank ->
                            val groupName = bank.groupName.ifBlank { DEFAULT_BANK_GROUP_NAME }
                            "$groupName 路 鏄剧ず ${visibleRecords.size} / ${records.size} 鏉"
                        } ?: "褰撳墠鏄剧ず鍏ㄩ儴 ${records.size} 鏉″涔犺褰",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = "閫夋嫨璁板綍鑼冨洿",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        if (selectedBank != null) {
            Text(
                text = "鍖呮嫭灞炰簬璇ラ搴撶殑璁板綍锛屼互鍙婅法棰樺簱缁冧範涓寘鍚棰樺簱棰樼洰鐨勮褰曘€",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecordScopeDialog(
    banks: List<QuizBank>,
    records: List<StudyRecord>,
    selectedScopeKey: String,
    bankNameCounts: Map<String, Int>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val groupedBanks = banks
        .groupBy { it.groupName.ifBlank { DEFAULT_BANK_GROUP_NAME } }
        .entries
        .sortedBy { entry -> if (entry.key == DEFAULT_BANK_GROUP_NAME) "" else entry.key }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("閫夋嫨璁板綍鑼冨洿") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RecordScopeOption(
                    title = "鍏ㄩ儴璁板綍",
                    desc = "鍏?${records.size} 鏉″涔犺褰",
                    selected = selectedScopeKey == RECORD_SCOPE_ALL,
                    onClick = { onSelect(RECORD_SCOPE_ALL) }
                )
                groupedBanks.forEach { entry ->
                    Text(
                        text = entry.key,
                        style = MaterialTheme.typography.labelLarge,
                        color = ShirohaColors.TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    entry.value.forEach { bank ->
                        val key = RECORD_SCOPE_BANK_PREFIX + bank.id
                        val bankNameIsUnique = bankNameCounts[bank.name.trim()] == 1
                        val count = records.count { record ->
                            recordMatchesBank(record, bank, bankNameIsUnique)
                        }
                        RecordScopeOption(
                            title = bank.name,
                            desc = "$count 鏉＄浉鍏宠褰?路 鍏?${bank.questions.size} 棰",
                            selected = selectedScopeKey == key,
                            onClick = { onSelect(key) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("鍏抽棴") }
        }
    )
}

@Composable
private fun RecordScopeOption(
    title: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shirohaNoRippleClickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ShirohaRadius.Md),
        color = if (selected) ShirohaColors.BrandPrimarySoft else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (selected) ShirohaColors.LineSelected else ShirohaColors.LineSoft
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Done,
                    contentDescription = "宸查€夋嫨",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun recordMatchesBank(
    record: StudyRecord,
    bank: QuizBank,
    bankNameIsUnique: Boolean
): Boolean {
    if (record.bankId == bank.id) return true
    if (record.questionResults.any { it.sourceBankId == bank.id }) return true

    if (!bankNameIsUnique) return false

    val bankName = bank.name.trim()
    if (bankName.isBlank()) return false
    if (record.bankId.isNullOrBlank() && record.bankName.trim() == bankName) return true
    return record.questionResults.any { result ->
        result.sourceBankId.isNullOrBlank() && result.sourceBankName?.trim() == bankName
    }
}

@Composable
private fun RecordCard(
    record: StudyRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val wrong = (record.total - record.correct).coerceAtLeast(0)
    val accuracy = if (record.total == 0) 0 else record.correct * 100 / record.total
    val finishTime = record.timestamp
    val isExam = record.source.contains("鑰冭瘯")
    val meaningfulTitle = meaningfulRecordTitle(record)
    val footerText = recordFooterText(record)

    GlassCard(
        modifier = Modifier.shirohaNoRippleClickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(record.source, selected = true)
                Text(
                    text = record.bankName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = formatRecordTime(finishTime),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "鍒犻櫎璁板綍",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (meaningfulTitle != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = meaningfulTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
        } else {
            Spacer(Modifier.height(10.dp))
        }

        Text(
            text = "${record.total} 棰?路 瀵?${record.correct} 路 閿?$wrong",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = footerText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            Text(
                text = if (isExam && record.totalScore != null && record.earnedScore != null) {
                    "${record.earnedScore.trimScore()} / ${record.totalScore.trimScore()} 鍒"
                } else {
                    "姝ｇ‘鐜?$accuracy%"
                },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}

internal fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

internal fun formatRecordTime(timestamp: Long): String {
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private fun meaningfulRecordTitle(record: StudyRecord): String? {
    val title = record.title.trim()
    if (title.isBlank()) return null
    val bankName = record.bankName.trim()
    val placeholders = setOf(
        "褰撳墠棰樺簱",
        "鍘熺敓鑰冭瘯",
        "缁冧範璁板綍",
        "鑰冭瘯璁板綍",
        "褰撳墠缁冧範",
        "褰撳墠鑰冭瘯"
    )
    if (title in placeholders) return null
    if (bankName.isNotBlank() && title == bankName) return null
    return title
}

private fun recordFooterText(record: StudyRecord): String {
    val duration = record.durationSeconds?.let { "鐢ㄦ椂 ${formatDuration(it)}" }
    val detail = if (record.questionResults.isNotEmpty()) "鐐瑰嚮鏌ョ湅璇︽儏" else "浠呬繚鐣欐憳瑕"
    return listOfNotNull(duration, detail).joinToString(" 路 ")
}

internal fun Double.trimScore(): String {
    return if (this % 1.0 == 0.0) this.toInt().toString() else "%.1f".format(this)
}


