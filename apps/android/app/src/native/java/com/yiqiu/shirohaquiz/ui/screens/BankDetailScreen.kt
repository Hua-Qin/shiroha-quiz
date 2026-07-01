package com.yiqiu.shirohaquiz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.importer.model.MultiBlankSupport
import com.yiqiu.shirohaquiz.importer.model.Option
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.DEFAULT_BANK_GROUP_NAME
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.AiAnalysisFillPanel
import com.yiqiu.shirohaquiz.ui.components.EmptyStateIllustration
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.MultiBlankAnswerEditor
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaDangerConfirmDialog
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.util.bankDisplayPath

@Composable
fun BankDetailScreen(
    bankId: String?,
    onBack: () -> Unit,
    onGoPractice: () -> Unit,
    onGoExam: () -> Unit,
    onOpenReview: () -> Unit
) {
    val context = LocalContext.current
    val bank = if (bankId == null) {
        QuizRepository.activeBank()
    } else {
        QuizRepository.banks.firstOrNull { it.id == bankId }
    }
    val isActive = bank?.id == QuizRepository.activeBank()?.id
    var showSlashedList by remember(bank?.id) { mutableStateOf(false) }
    var showDeleteBankConfirm by remember(bank?.id) { mutableStateOf(false) }

    BackHandler(enabled = showSlashedList) { showSlashedList = false }

    if (showDeleteBankConfirm) {
        bank?.let { targetBank ->
            ShirohaDangerConfirmDialog(
                title = "纭鍒犻櫎棰樺簱锛",
                message = "灏嗗垹闄も€${targetBank.name}鈥濓紝骞舵竻鐞嗚繖浠介搴撳叧鑱旂殑閿欓銆佹柀棰樺拰瀛︿範璁板綍銆傛搷浣滀笉鍙挙閿€銆",
                confirmText = "纭鍒犻櫎",
                onDismiss = { showDeleteBankConfirm = false },
                onConfirm = {
                    QuizRepository.deleteBank(context, targetBank.id)
                    showDeleteBankConfirm = false
                    onBack()
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Bank Detail",
            title = bank?.let { bankDisplayPath(it.groupName, it.name) } ?: "棰樺簱璇︽儏",
            subtitle = "棰樺簱鎽樿銆侀鍨嬪垎甯冨拰蹇€熸搷浣溿€"
        )

        if (bank == null) {
            EmptyStateIllustration(
                title = "娌℃湁鎵惧埌瀵瑰簲棰樺簱",
                message = "杩欓€氬父璇存槑棰樺簱宸茬粡琚垏鎹㈡垨鍒犻櫎銆傝繑鍥炰笂涓€椤甸噸鏂伴€夋嫨涓€浠介搴撳嵆鍙€",
                imageRes = R.drawable.illus_empty_state_webp,
                action = {
                    Spacer(Modifier.height(12.dp))
                }
            )
            GlassCard {
                ActionPillButton(
                    icon = Icons.Rounded.Done,
                    text = "杩斿洖",
                    primary = true,
                    onClick = onBack
                )
            }
            return
        }

        if (showSlashedList) {
            SlashedQuestionListCard(
                bank = bank,
                onBack = { showSlashedList = false },
                onRestore = { question -> QuizRepository.restoreSlashedQuestion(context, bank.id, question) }
            )
            return
        }

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "棰樺簱鎽樿",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                ActionPillButton(
                    icon = Icons.Rounded.Done,
                    text = if (isActive) "褰撳墠棰樺簱" else "璁句负褰撳墠",
                    primary = true,
                    modifier = Modifier.height(44.dp),
                    onClick = {
                        if (!isActive) {
                            QuizRepository.setActiveBank(context, bank.id)
                        }
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip("${bank.questions.size} 棰", selected = true)
                StatusChip(bank.groupName.ifBlank { DEFAULT_BANK_GROUP_NAME }, selected = false)
                StatusChip(if (isActive) "娲诲姩棰樺簱" else "鍙垏鎹㈤搴", selected = isActive)
                Spacer(Modifier.weight(1f))
                SlashedBankChip(
                    count = QuizRepository.slashedQuestionCount(bank.id),
                    onClick = { showSlashedList = true }
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "鍗曢€?${bank.questions.count { it.type == QuestionType.SINGLE }} 路 澶氶€?${bank.questions.count { it.type == QuestionType.MULTIPLE }} 路 鍒ゆ柇 ${bank.questions.count { it.type == QuestionType.JUDGE }} 路 涓昏 ${bank.questions.count { it.type == QuestionType.BLANK || it.type == QuestionType.SHORT }}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionPillButton(
                    icon = Icons.Rounded.PlayArrow,
                    text = "杩涘叆缁冧範",
                    primary = false,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    fillWidthContent = true,
                    onClick = onGoPractice
                )
                ActionPillButton(
                    icon = Icons.Rounded.Timer,
                    text = "杩涘叆鑰冭瘯",
                    primary = false,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    fillWidthContent = true,
                    onClick = onGoExam
                )
            }
        }

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "棰樼洰棰勮",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                ActionPillButton(
                    icon = Icons.Rounded.Edit,
                    text = "浜屾鏍稿",
                    primary = false,
                    modifier = Modifier.height(42.dp),
                    onClick = onOpenReview
                )
            }
            Spacer(Modifier.height(12.dp))
            NoticeCard("杩欓噷鍙樉绀哄墠 5 棰樸€傜偣鍑烩€滀簩娆℃牳瀵光€濆彲杩涘叆瀹屾暣娌夋蹈鏍稿椤碉紝閫愰鏌ョ湅鍜屼慨鏀规暣浠介搴撱€", warning = false)
            Spacer(Modifier.height(12.dp))
            bank.questions.take(5).forEach { question ->
                QuestionPreviewBlock(
                    question = question,
                    editable = false,
                    onEdit = {}
                )
            }
        }

        if (bank.id != "demo-bank") {
            GlassCard {
                Text(
                    text = "鍗遍櫓鎿嶄綔",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                NoticeCard("鍒犻櫎棰樺簱鍚庯紝浼氫竴璧锋竻鐞嗚繖浠藉師鐢熼搴撳叧鑱旂殑鏈湴璁板綍銆")
                Spacer(Modifier.height(12.dp))
                ActionPillButton(
                    icon = Icons.Rounded.DeleteOutline,
                    text = "鍒犻櫎杩欎唤棰樺簱",
                    primary = false,
                    onClick = { showDeleteBankConfirm = true }
                )
            }
        }
    }
}

@Composable
private fun SlashedBankChip(
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(32.dp)
            .shirohaNoRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = ShirohaColors.BrandPrimarySoft,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSelected)
    ) {
        Text(
            text = "鏂?$count",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SlashedQuestionListCard(
    bank: com.yiqiu.shirohaquiz.state.QuizBank,
    onBack: () -> Unit,
    onRestore: (Question) -> Unit
) {
    val slashed = QuizRepository.slashedQuestionsForBank(bank.id)
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "褰撳墠棰樺簱鏂╅鏈",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${bank.name} 路 鍏?${slashed.size} 棰",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            ActionPillButton(
                icon = Icons.Rounded.Done,
                text = "杩斿洖",
                primary = false,
                modifier = Modifier.height(42.dp),
                onClick = onBack
            )
        }
        Spacer(Modifier.height(12.dp))
        if (slashed.isEmpty()) {
            NoticeCard("鏆傛棤宸叉柀棰樸€傚紑鍚柀棰樺姛鑳藉悗锛岀粌涔犳椂鐐瑰嚮棰樼洰鍙充笂瑙掆€滄柀鈥濇寜閽紝鍙皢涓€鐪间細鐨勯绉诲嚭鍚庣画缁冧範銆", warning = false)
        } else {
            NoticeCard("鎭㈠鍚庯紝杩欓亾棰樹細閲嶆柊杩涘叆鍚庣画缁冧範銆", warning = false)
            Spacer(Modifier.height(12.dp))
            slashed.forEach { question ->
                QuestionPreviewBlock(
                    question = question,
                    editable = false,
                    onEdit = {}
                )
                ActionPillButton(
                    icon = Icons.Rounded.Done,
                    text = "鎭㈠鏈",
                    primary = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    fillWidthContent = true,
                    onClick = { onRestore(question) }
                )
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun QuestionPreviewBlock(
    question: Question,
    editable: Boolean,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusChip(typeLabel(question.type))
        if (editable) {
            Spacer(Modifier.weight(1f))
            ActionPillButton(
                icon = Icons.Rounded.Edit,
                text = "淇敼",
                primary = false,
                modifier = Modifier.height(38.dp),
                onClick = onEdit
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        text = "${question.number}. ${question.question}",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold
    )
    if (question.options.isNotEmpty()) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = question.options.joinToString("  ") { "${it.key}. ${it.text}" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (editable) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (MultiBlankSupport.hasStructuredAnswers(question)) {
                "绛旀锛歕n${MultiBlankSupport.expectedAnswerText(question.blankAnswers)}"
            } else {
                "绛旀锛${question.answer.joinToString(" / ").ifBlank { "鏈瘑鍒" }}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun QuestionEditDialog(
    question: Question,
    onDismiss: () -> Unit,
    onSave: (Question) -> Unit
) {
    var stem by remember(question.id) { mutableStateOf(question.question) }
    var optionsText by remember(question.id) { mutableStateOf(formatOptions(question.options)) }
    var answerText by remember(question.id) { mutableStateOf(question.answer.joinToString(" ")) }
    var blankAnswerDrafts by remember(question.id) { mutableStateOf(question.blankAnswers) }
    var analysisText by remember(question.id) { mutableStateOf(question.analysis) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("淇敼棰樼洰") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "棰樺瀷锛${typeLabel(question.type)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = stem,
                    onValueChange = { stem = it },
                    label = { Text("棰樺共") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = optionsText,
                    onValueChange = { optionsText = it },
                    label = { Text("閫夐」锛屾瘡琛屼竴涓紝渚嬪 A. 閫夐」鍐呭") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                val isStructuredBlank = question.type == QuestionType.BLANK && blankAnswerDrafts.isNotEmpty()
                val detectedBlankCount = MultiBlankSupport.countExplicitBlanks(stem)
                if (isStructuredBlank) {
                    MultiBlankAnswerEditor(
                        blankAnswers = blankAnswerDrafts,
                        detectedBlankCount = detectedBlankCount,
                        onChange = { groups ->
                            blankAnswerDrafts = groups
                            answerText = MultiBlankSupport.compatibilityAnswer(groups).firstOrNull().orEmpty()
                        },
                        onDisable = {
                            answerText = MultiBlankSupport.compatibilityAnswer(blankAnswerDrafts).firstOrNull().orEmpty()
                            blankAnswerDrafts = emptyList()
                        }
                    )
                } else {
                    OutlinedTextField(
                        value = answerText,
                        onValueChange = { answerText = it },
                        label = { Text("绛旀锛屼緥濡$A 鎴$A B") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (question.type == QuestionType.BLANK && detectedBlankCount > 1) {
                        TextButton(
                            onClick = {
                                blankAnswerDrafts = MultiBlankSupport.initialGroups(stem, parseAnswer(answerText, QuestionType.BLANK))
                            }
                        ) { Text("鍚敤澶氱┖绛旀") }
                    }
                }
                OutlinedTextField(
                    value = analysisText,
                    onValueChange = { analysisText = it },
                    label = { Text("瑙ｆ瀽") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                AiAnalysisFillPanel(
                    question = question.copy(
                        question = stem.trim(),
                        options = parseOptions(optionsText),
                        answer = if (question.type == QuestionType.BLANK && blankAnswerDrafts.isNotEmpty()) {
                            MultiBlankSupport.compatibilityAnswer(blankAnswerDrafts)
                        } else {
                            parseAnswer(answerText, question.type)
                        },
                        blankAnswers = if (question.type == QuestionType.BLANK) blankAnswerDrafts else emptyList(),
                        analysis = analysisText.trim()
                    ),
                    currentAnalysis = analysisText,
                    onApplyAnalysis = { analysisText = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        question.copy(
                            question = stem.trim(),
                            options = parseOptions(optionsText),
                            answer = if (question.type == QuestionType.BLANK && blankAnswerDrafts.isNotEmpty()) {
                                MultiBlankSupport.compatibilityAnswer(blankAnswerDrafts)
                            } else {
                                parseAnswer(answerText, question.type)
                            },
                            blankAnswers = if (question.type == QuestionType.BLANK) blankAnswerDrafts else emptyList(),
                            analysis = analysisText.trim()
                        )
                    )
                }
            ) { Text("淇濆瓨") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("鍙栨秷") }
        }
    )
}

private fun formatOptions(options: List<Option>): String {
    return options.joinToString("\n") { "${it.key}. ${it.text}" }
}

private fun parseOptions(raw: String): List<Option> {
    return raw.lines().mapNotNull { line ->
        val trimmed = line.trim()
        if (trimmed.isBlank()) return@mapNotNull null
        val match = Regex("^([A-Ga-g])\\s*[.锛庛€?锛歖?\\s*(.+)$").find(trimmed)
        if (match != null) {
            Option(
                key = match.groupValues[1].uppercase(),
                text = match.groupValues[2].trim()
            )
        } else {
            null
        }
    }
}

private fun parseAnswer(raw: String, type: QuestionType): List<String> {
    val clean = raw.trim()
    if (clean.isBlank()) return emptyList()
    if (type == QuestionType.BLANK || type == QuestionType.SHORT) return listOf(clean)
    return clean.split(Regex("[\\s,锛屻€?]+"))
        .map { it.trim().uppercase() }
        .filter { it.isNotBlank() }
        .distinct()
}

private fun typeLabel(type: QuestionType): String = when (type) {
    QuestionType.SINGLE -> "鍗曢€夐"
    QuestionType.MULTIPLE -> "澶氶€夐"
    QuestionType.JUDGE -> "鍒ゆ柇棰"
    QuestionType.BLANK -> "濉┖棰"
    QuestionType.SHORT -> "绠€绛旈"
}