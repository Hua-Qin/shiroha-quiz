package com.yiqiu.shirohaquiz.ui.screens

import com.yiqiu.shirohaquiz.ui.theme.shirohaEditorialBackground

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.MultiBlankSupport
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.DEFAULT_BANK_GROUP_NAME
import com.yiqiu.shirohaquiz.state.QuestionSearchEngine
import com.yiqiu.shirohaquiz.state.QuestionSearchMatchedField
import com.yiqiu.shirohaquiz.state.QuestionSearchResult
import com.yiqiu.shirohaquiz.state.QuestionSearchScope
import com.yiqiu.shirohaquiz.state.QuizBank
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EditorialDivider
import com.yiqiu.shirohaquiz.ui.components.EditorialSection
import com.yiqiu.shirohaquiz.ui.components.EmptyStateIllustration
import com.yiqiu.shirohaquiz.ui.components.QuestionImagesBlock
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import com.yiqiu.shirohaquiz.ui.text.LatexDisplayFormatter
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.editorialScaleFor
import com.yiqiu.shirohaquiz.ui.theme.screenClassFor
import com.yiqiu.shirohaquiz.ui.theme.uiScaleFor
import com.yiqiu.shirohaquiz.ui.util.bankDisplayPath

private const val SCOPE_ACTIVE = "active"
private const val SCOPE_ALL = "all"
private const val SCOPE_BANK_PREFIX = "bank:"

@Composable
fun QuestionSearchScreen(
    onBack: () -> Unit,
    onOpenBankDetail: (String) -> Unit
) {
    val banks = QuizRepository.banks
    val activeBank = QuizRepository.activeBank()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedScopeKey by rememberSaveable {
        mutableStateOf(if (activeBank != null) SCOPE_ACTIVE else SCOPE_ALL)
    }
    var showScopeDialog by remember { mutableStateOf(false) }
    var expandedResultKeys by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    val scope = selectedScopeKey.toSearchScope(activeBank?.id)
    val scopeLabel = selectedScopeKey.scopeLabel(banks, activeBank)
    val results = QuestionSearchEngine.search(
        banks = banks,
        activeBankId = activeBank?.id,
        query = query,
        scope = scope
    )

    if (showScopeDialog) {
        SearchScopeDialog(
            banks = banks,
            activeBank = activeBank,
            selectedScopeKey = selectedScopeKey,
            onSelect = { key ->
                selectedScopeKey = key
                showScopeDialog = false
            },
            onDismiss = { showScopeDialog = false }
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            ShirohaHeader(
                kicker = "Search",
                title = "棰樼洰鎼滅储",
                subtitle = "鎼滈骞层€侀€夐」銆佺瓟妗堟垨瑙ｆ瀽銆",
                scale = scale
            )

            // === 鎼滅储妗$EditorialSection 鍖呰９ ===
            EditorialSection(
                kicker = "Search",
                title = "鎼滅储",
                scale = scale
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)) {
                    // 鑼冨洿閫夋嫨
                    Text(
                        text = "鎼滅储鑼冨洿",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShirohaColors.TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shirohaNoRippleClickable { showScopeDialog = true },
                        shape = RoundedCornerShape(ShirohaRadius.Md),
                        color = ShirohaColors.CardWhite86,
                        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineStrong)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = scopeLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "鍙垏鎹㈠綋鍓嶉搴撱€佸叏閮ㄩ搴撴垨鎸囧畾棰樺簱",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.ExpandMore,
                                contentDescription = "閫夋嫨鎼滅储鑼冨洿",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    // 鎼滅储杈撳叆妗?                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "鎼滅储"
                            )
                        },
                        placeholder = { Text("鎼滈骞层€侀€夐」銆佺瓟妗堟垨瑙ｆ瀽") },
                        singleLine = true
                    )
                }
            }

            // === 楂樼骇绛涢€?棰樺簱):EditorialSection 鍖呰９ ===
            EditorialSection(
                kicker = "Filter",
                title = "绛涢€",
                scale = scale
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm)) {
                    Text(
                        text = "棰樺簱鑼冨洿",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShirohaColors.TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm)
                    ) {
                        StatusChip(
                            text = if (activeBank != null) "褰撳墠棰樺簱" else "鏈€変腑",
                            selected = selectedScopeKey == SCOPE_ACTIVE,
                            modifier = Modifier.shirohaNoRippleClickable {
                                if (activeBank != null) selectedScopeKey = SCOPE_ACTIVE
                            }
                        )
                        StatusChip(
                            text = "鍏ㄩ儴棰樺簱",
                            selected = selectedScopeKey == SCOPE_ALL,
                            modifier = Modifier.shirohaNoRippleClickable {
                                selectedScopeKey = SCOPE_ALL
                            }
                        )
                    }
                    Text(
                        text = "宸查€夛細${scopeLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ShirohaColors.TextSecondary
                    )
                }
            }

            // === 鎼滅储缁撴灉:EditorialSection 鍖呰９ ===
            when {
                query.isBlank() -> EditorialSection(
                    kicker = "Results",
                    title = "缁撴灉",
                    scale = scale
                ) {
                    EmptyStateIllustration(
                        title = "杈撳叆鍏抽敭璇嶅紑濮嬫悳绱",
                        message = "鍙互杈撳叆棰樺共鐗囨銆侀€夐」鍐呭銆佺瓟妗堟垨瑙ｆ瀽閲岀殑鍏抽敭璇嶃€"
                    )
                }

                results.isEmpty() -> EditorialSection(
                    kicker = "Results",
                    title = "缁撴灉",
                    scale = scale
                ) {
                    EmptyStateIllustration(
                        title = "娌℃湁鎵惧埌鐩稿叧棰樼洰",
                        message = "鍙互鎹竴涓叧閿瘝锛屾垨鍒囨崲鍒板叏閮ㄩ搴撴悳绱€"
                    )
                }

                else -> EditorialSection(
                    kicker = "Results",
                    title = "缁撴灉锛${results.size}锛",
                    scale = scale
                ) {
                    results.forEachIndexed { index, result ->
                        val expanded = result.key in expandedResultKeys
                        QuestionSearchResultCard(
                            result = result,
                            expanded = expanded,
                            onToggleExpanded = {
                                expandedResultKeys = if (expanded) {
                                    expandedResultKeys - result.key
                                } else {
                                    (expandedResultKeys + result.key).distinct()
                                }
                            },
                            onOpenBankDetail = onOpenBankDetail
                        )
                        if (index < results.lastIndex) {
                            EditorialDivider()
                        }
                    }
                }
            }

            // 杩斿洖鎸夐挳
            Row(modifier = Modifier.fillMaxWidth()) {
                ActionPillButton(
                    icon = Icons.Rounded.ArrowBack,
                    text = "杩斿洖棰樺簱绠＄悊",
                    primary = false,
                    onClick = onBack
                )
            }

            Spacer(Modifier.height(ShirohaSpacing.Xl))
        }
    }
}

@Composable
private fun SearchScopeDialog(
    banks: List<QuizBank>,
    activeBank: QuizBank?,
    selectedScopeKey: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val groupedBanks = banks
        .groupBy { it.groupName.ifBlank { DEFAULT_BANK_GROUP_NAME } }
        .entries
        .sortedBy { entry -> if (entry.key == DEFAULT_BANK_GROUP_NAME) "" else entry.key }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("閫夋嫨鎼滅储鑼冨洿") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeBank?.let { bank ->
                    SearchScopeOption(
                        title = "褰撳墠棰樺簱锛${bank.name}",
                        desc = bankDisplayPath(bank),
                        selected = selectedScopeKey == SCOPE_ACTIVE,
                        onClick = { onSelect(SCOPE_ACTIVE) }
                    )
                }
                SearchScopeOption(
                    title = "鍏ㄩ儴棰樺簱",
                    desc = "鎼滅储鎵€鏈夊垎缁勫拰棰樺簱",
                    selected = selectedScopeKey == SCOPE_ALL,
                    onClick = { onSelect(SCOPE_ALL) }
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
                        val key = bank.scopeKey()
                        SearchScopeOption(
                            title = bank.name,
                            desc = "${bank.questions.size} 棰",
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
private fun SearchScopeOption(
    title: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shirohaNoRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(ShirohaRadius.Md),
        color = if (selected) ShirohaColors.BrandPrimarySoft else Color.Transparent,
        border = BorderStroke(1.dp, if (selected) ShirohaColors.LineSelected else ShirohaColors.LineSoft)
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
                if (desc.isNotBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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

@Composable
private fun QuestionSearchResultCard(
    result: QuestionSearchResult,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onOpenBankDetail: (String) -> Unit
) {
    val question = result.question

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shirohaNoRippleClickable(onClick = onToggleExpanded)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${result.groupName} / ${result.bankName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ShirohaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // 鍒楄〃椤?棰樺彿 + 棰樺共棰勮
                Text(
                    text = "绗?${result.questionIndex} 棰$路 ${question.type.label()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            StatusChip(if (expanded) "鏀惰捣" else "灞曞紑", selected = expanded)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = LatexDisplayFormatter.format(question.question).ifBlank { "锛堢┖棰樺共锛" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        // 鍛戒腑瀛楁 Chip
        Text(
            text = "鍛戒腑锛${result.matchedFields.joinToString("銆") { it.label }}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        val answerText = if (MultiBlankSupport.hasStructuredAnswers(question)) {
            MultiBlankSupport.expectedAnswerText(question.blankAnswers)
        } else {
            question.answer.joinToString("銆")
        }
        if (answerText.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            // 绛旀棰勮
            Text(
                text = "绛旀锛${LatexDisplayFormatter.format(answerText)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis
            )
        }

        if (expanded) {
            Spacer(Modifier.height(12.dp))
            EditorialDivider()
            Spacer(Modifier.height(12.dp))
            FullQuestionInfo(question)
            Spacer(Modifier.height(14.dp))
            ActionPillButton(
                icon = Icons.Rounded.Visibility,
                text = "鏌ョ湅鎵€鍦ㄩ搴",
                primary = false,
                modifier = Modifier.fillMaxWidth(),
                fillWidthContent = true,
                onClick = { onOpenBankDetail(result.bankId) }
            )
        } else if (question.analysis.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "瑙ｆ瀽锛${LatexDisplayFormatter.format(question.analysis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FullQuestionInfo(question: Question) {
    SearchInfoBlock(label = "棰樺共", text = question.question.ifBlank { "锛堢┖棰樺共锛" })
    if (question.images.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        QuestionImagesBlock(question.images, maxPreviewHeight = 260.dp, showMeta = true)
    }
    if (question.options.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "閫夐」",
            style = MaterialTheme.typography.labelLarge,
            color = ShirohaColors.TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        question.options.forEach { option ->
            Text(
                text = "${option.key}. ${LatexDisplayFormatter.format(option.text)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
        }
    }
    SearchInfoBlock(
        label = "姝ｇ‘绛旀",
        text = if (MultiBlankSupport.hasStructuredAnswers(question)) {
            MultiBlankSupport.expectedAnswerText(question.blankAnswers)
        } else {
            question.answer.joinToString("銆").ifBlank { "锛堟湭濉啓锛" }
        }
    )
    if (question.analysis.isNotBlank()) {
        SearchInfoBlock(label = "瑙ｆ瀽", text = question.analysis)
    }
    if (question.category.isNotBlank()) {
        SearchInfoBlock(label = "鍒嗙被", text = question.category)
    }
}

@Composable
private fun SearchInfoBlock(label: String, text: String) {
    Spacer(Modifier.height(12.dp))
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = ShirohaColors.TextSecondary,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = LatexDisplayFormatter.format(text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

private fun String.toSearchScope(activeBankId: String?): QuestionSearchScope {
    return when {
        this == SCOPE_ACTIVE && activeBankId != null -> QuestionSearchScope.ActiveBank
        this == SCOPE_ALL -> QuestionSearchScope.AllBanks
        startsWith(SCOPE_BANK_PREFIX) -> QuestionSearchScope.Bank(removePrefix(SCOPE_BANK_PREFIX))
        else -> QuestionSearchScope.AllBanks
    }
}

private fun String.scopeLabel(banks: List<QuizBank>, activeBank: QuizBank?): String {
    return when {
        this == SCOPE_ACTIVE && activeBank != null -> "褰撳墠棰樺簱锛${activeBank.name}"
        this == SCOPE_ALL -> "鍏ㄩ儴棰樺簱"
        startsWith(SCOPE_BANK_PREFIX) -> {
            val bankId = removePrefix(SCOPE_BANK_PREFIX)
            banks.firstOrNull { it.id == bankId }".let { "鎸囧畾棰樺簱锛${it.name}" } ?: "鎸囧畾棰樺簱"
        }
        else -> "鍏ㄩ儴棰樺簱"
    }
}

private fun QuizBank.scopeKey(): String = "$SCOPE_BANK_PREFIX$id"

private fun QuestionType.label(): String = when (this) {
    QuestionType.SINGLE -> "鍗曢€夐"
    QuestionType.MULTIPLE -> "澶氶€夐"
    QuestionType.JUDGE -> "鍒ゆ柇棰"
    QuestionType.BLANK -> "濉┖棰"
    QuestionType.SHORT -> "绠€绛旈"
}

