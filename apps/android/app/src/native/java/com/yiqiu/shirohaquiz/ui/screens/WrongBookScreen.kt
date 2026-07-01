package com.yiqiu.shirohaquiz.ui.screens

import com.yiqiu.shirohaquiz.ui.theme.shirohaEditorialBackground

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.importer.model.MultiBlankSupport
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.DEFAULT_BANK_GROUP_NAME
import com.yiqiu.shirohaquiz.state.QuizBank
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.state.WrongQuestionEntry
import com.yiqiu.shirohaquiz.state.WrongStatus
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EditorialDivider
import com.yiqiu.shirohaquiz.ui.components.EditorialFigure
import com.yiqiu.shirohaquiz.ui.components.EditorialSection
import com.yiqiu.shirohaquiz.ui.components.EmptyStateIllustration
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.QuestionAiAnalysisButton
import com.yiqiu.shirohaquiz.ui.components.QuestionImagesBlock
import com.yiqiu.shirohaquiz.ui.components.ShirohaDangerConfirmDialog
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.editorialScaleFor
import com.yiqiu.shirohaquiz.ui.theme.screenClassFor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class WrongBookFilter(val label: String) {
    NOT_MASTERED("鏈帉鎻"),
    MASTERED("宸叉帉鎻"),
    ALL("鍏ㄩ儴")
}

private enum class WrongBookSort(val label: String) {
    RECENT_WRONG("鏈€杩戦敊"),
    WRONG_COUNT("閿欒娆℃暟"),
    MASTERY("鎺屾彙绋嬪害")
}

private enum class WrongBookReviewCountMode(val label: String) {
    TEN("10棰"),
    TWENTY("20棰"),
    CUSTOM("鑷畾涔"),
    ALL("鍏ㄩ儴")
}

private const val WRONG_BOOK_PAGE_SCOPE_ALL = "all"
private const val WRONG_BOOK_PAGE_SCOPE_BANK_PREFIX = "bank:"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WrongBookScreen(
    onBack: () -> Unit,
    onGoPractice: () -> Unit
) {
    val banks = QuizRepository.banks.toList()
    val allWrongBook = QuizRepository.wrongBook.toList()
    var selectedScopeKey by rememberSaveable { mutableStateOf(WRONG_BOOK_PAGE_SCOPE_ALL) }
    var showScopeDialog by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(WrongBookFilter.NOT_MASTERED) }
    var sort by remember { mutableStateOf(WrongBookSort.RECENT_WRONG) }
    var selectedTypes by remember { mutableStateOf(QuestionType.entries.toSet()) }
    var reviewCountMode by remember { mutableStateOf(WrongBookReviewCountMode.ALL) }
    var customReviewCount by remember { mutableStateOf(10) }
    var customReviewCountText by remember { mutableStateOf("10") }
    var showCustomReviewCountDialog by remember { mutableStateOf(false) }
    var showClearWrongBookConfirm by remember { mutableStateOf(false) }
    // 鎵嬪姩娣诲姞閿欓娴佺▼鐩稿叧鐘舵€?    var showManualAddDialog by remember { mutableStateOf(false) }
    var manualAddBankId by remember { mutableStateOf<String?>(null) }
    var manualAddQuestionId by remember { mutableStateOf("") }
    var manualAddErrorReason by remember { mutableStateOf("") }
    var manualAddFeedback by remember { mutableStateOf<String?>(null) }

    val selectedBankId = selectedScopeKey
        .takeIf { it.startsWith(WRONG_BOOK_PAGE_SCOPE_BANK_PREFIX) }
        ".removePrefix(WRONG_BOOK_PAGE_SCOPE_BANK_PREFIX)
    val selectedBank = selectedBankId?.let { id -> banks.firstOrNull { it.id == id } }
    val effectiveScopeKey = if (selectedBankId == null || selectedBank != null) {
        selectedScopeKey
    } else {
        WRONG_BOOK_PAGE_SCOPE_ALL
    }
    val wrongBook = selectedBank?.let { bank ->
        allWrongBook.filter { it.bankId == bank.id }
    } ?: allWrongBook
    val scopeLabel = selectedBank?.name ?: "鍏ㄩ儴棰樺簱"
    val isSingleBankScope = selectedBank != null

    val advancedReviewSettingsEnabled = QuizRepository.wrongBookAdvancedReviewSettingsEnabled
    val masteryFilteredEntries = remember(wrongBook, filter) {
        wrongBook.filterBy(filter)
    }
    val availableTypes = remember(masteryFilteredEntries) {
        QuestionType.entries.filter { type -> masteryFilteredEntries.any { it.question.type == type } }
    }
    val filteredEntries = remember(masteryFilteredEntries, selectedTypes, sort, advancedReviewSettingsEnabled) {
        masteryFilteredEntries
            .filter { entry -> !advancedReviewSettingsEnabled || entry.question.type in selectedTypes }
            .sortBy(sort)
    }
    val reviewCandidates = filteredEntries.filter {
        it.status != WrongStatus.MASTERED.label && !QuizRepository.isQuestionSlashed(it.bankId, it.question)
    }
    val selectedReviewCount = if (advancedReviewSettingsEnabled) {
        resolveWrongBookReviewCount(
            mode = reviewCountMode,
            customCount = customReviewCount,
            availableCount = reviewCandidates.size
        )
    } else {
        reviewCandidates.size
    }
    val reviewEntries = reviewCandidates.take(selectedReviewCount)
    val notMasteredCount = wrongBook.count { it.status != WrongStatus.MASTERED.label }
    val masteredCount = wrongBook.count { it.status == WrongStatus.MASTERED.label }
    val smartReviewEnabled = QuizRepository.wrongBookSmartReviewEnabled
    val smartReviewEntries = remember(wrongBook, smartReviewEnabled) {
        if (!smartReviewEnabled) {
            emptyList()
        } else {
            val now = System.currentTimeMillis()
            wrongBook
                .filterNot { QuizRepository.isQuestionSlashed(it.bankId, it.question) }
                .filter { entry -> isWrongEntryDueForPageReview(entry, now) }
                .sortedWith(
                    compareBy<WrongQuestionEntry> { if (it.status == WrongStatus.MASTERED.label) 1 else 0 }
                        .thenBy { it.nextReviewAt ?: 0L }
                        .thenByDescending { it.wrongCount }
                        .thenByDescending { it.lastWrongAt }
                )
        }
    }
    val smartReviewNotMastered = smartReviewEntries.count { it.status != WrongStatus.MASTERED.label }
    val smartReviewMastered = smartReviewEntries.count { it.status == WrongStatus.MASTERED.label }

    if (showClearWrongBookConfirm) {
        ShirohaDangerConfirmDialog(
            title = if (isSingleBankScope) "纭娓呯┖鈥${selectedBank?.name.orEmpty()}鈥濋敊棰橈紵" else "纭娓呯┖鍏ㄩ儴閿欓锛",
            message = if (isSingleBankScope) {
                "杩欎細绉婚櫎璇ラ搴撶殑閿欓璁板綍锛屽寘鎷敊棰樻鏁般€佹帉鎻＄姸鎬佸拰澶嶄範缁熻銆傚叾浠栭搴撻敊棰樹笉浼氬彈褰卞搷銆"
            } else {
                "杩欎細绉婚櫎鍏ㄩ儴棰樺簱鐨勯敊棰樿褰曪紝鍖呮嫭閿欓娆℃暟銆佹帉鎻＄姸鎬佸拰澶嶄範缁熻銆傛搷浣滀笉鍙挙閿€銆"
            },
            confirmText = if (isSingleBankScope) "娓呯┖褰撳墠鑼冨洿" else "娓呯┖鍏ㄩ儴",
            onDismiss = { showClearWrongBookConfirm = false },
            onConfirm = {
                if (selectedBank == null) {
                    QuizRepository.clearWrongBook()
                } else {
                    wrongBook.toList().forEach(QuizRepository::removeWrongQuestion)
                }
                showClearWrongBookConfirm = false
            }
        )
    }

    if (showCustomReviewCountDialog) {
        WrongBookReviewCountDialog(
            value = customReviewCountText,
            maxCount = reviewCandidates.size.coerceAtLeast(1),
            onValueChange = { customReviewCountText = it },
            onDismiss = { showCustomReviewCountDialog = false },
            onConfirm = { count ->
                customReviewCount = count
                reviewCountMode = WrongBookReviewCountMode.CUSTOM
                showCustomReviewCountDialog = false
            }
        )
    }

    if (showScopeDialog) {
        WrongBookScopeDialog(
            banks = banks,
            wrongBook = allWrongBook,
            selectedScopeKey = effectiveScopeKey,
            onSelect = { key ->
                selectedScopeKey = key
                selectedTypes = QuestionType.entries.toSet()
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
        val uiScale = com.yiqiu.shirohaquiz.ui.theme.uiScaleFor(screenClass)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
                ) {
                    // 缂栬緫寮$Header:kicker + 琛嚎澶ф爣棰"                    ShirohaHeader(
                        kicker = "Wrong Book",
                        title = "閿欓鏈",
                        subtitle = "鎶婇敊棰樺彉鎴愭彁鍒嗛樁姊",
                        scale = scale
                    )

                    if (allWrongBook.isEmpty()) {
                        EmptyStateIllustration(
                            title = "閿欓鏈繕鏄┖鐨",
                            message = "缁х画缁冧範鎴栬€冭瘯鍚庯紝閿欓浼氳嚜鍔ㄨ繘鍏ヨ繖閲屻€",
                            imageRes = R.drawable.illus_wrongbook_hint_webp,
                            action = {
                                Spacer(Modifier.height((12 * uiScale).dp))
                                ActionPillButton(
                                    icon = Icons.AutoMirrored.Rounded.Undo,
                                    text = "杩斿洖棣栭〉",
                                    primary = false,
                                    onClick = onBack
                                )
                            }
                        )
                    }

                    if (allWrongBook.isNotEmpty()) {
                        IllustrationHeroCard(
                            title = "閿欓闇€瑕佹參鎱㈡秷鍖栥€",
                            subtitle = "绛涢敊棰橈紝闆嗕腑澶嶇洏",
                            imageRes = R.drawable.illus_wrongbook_hint_webp,
                            modifier = Modifier.height(ShirohaDimens.HeroCardHeight),
                            imageSize = ShirohaDimens.HeroImageSize,
                            scale = scale
                        )

                        // === 椤堕儴缂栬緫寮忔暟鎹尯:琛嚎澶ф暟瀛?===
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md),
                            verticalAlignment = Alignment.Top
                        ) {
                            EditorialFigure(
                                value = wrongBook.size.toString(),
                                label = "閿欓鎬绘暟",
                                unit = "鏉",
                                scale = scale,
                                modifier = Modifier.weight(1f)
                            )
                            EditorialFigure(
                                value = notMasteredCount.toString(),
                                label = "鏈帉鎻",
                                unit = "棰",
                                scale = scale,
                                modifier = Modifier.weight(1f)
                            )
                            EditorialFigure(
                                value = masteredCount.toString(),
                                label = "宸叉帉鎻",
                                unit = "棰",
                                scale = scale,
                                modifier = Modifier.weight(1f)
                            )
                            EditorialFigure(
                                value = (if (smartReviewEnabled) smartReviewEntries.size else 0).toString(),
                                label = if (smartReviewEnabled) "浠婃棩寰呭涔" else "鏅鸿兘澶嶄範",
                                unit = "棰",
                                scale = scale,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height((4 * uiScale).dp))
                        EditorialDivider(label = "鑼冨洿")
                        Spacer(Modifier.height((4 * uiScale).dp))

                        // 椤堕儴鎿嶄綔 Row:鎵嬪姩娣诲姞 + 杩斿洖
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy((10 * uiScale).dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ActionPillButton(
                                icon = Icons.Rounded.Add,
                                text = "鎵嬪姩娣诲姞閿欓",
                                primary = true,
                                enabled = banks.isNotEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height((46 * uiScale).dp),
                                fillWidthContent = true,
                                onClick = {
                                    manualAddFeedback = null
                                    manualAddQuestionId = ""
                                    manualAddErrorReason = ""
                                    manualAddBankId = selectedBank?.id ?: banks.firstOrNull()?.id
                                    showManualAddDialog = true
                                }
                            )
                            ActionPillButton(
                                icon = Icons.AutoMirrored.Rounded.Undo,
                                text = "杩斿洖",
                                primary = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .height((46 * uiScale).dp),
                                fillWidthContent = true,
                                onClick = onBack
                            )
                        }

                        if (showManualAddDialog) {
                            WrongBookManualAddDialog(
                                banks = banks,
                                selectedBankId = manualAddBankId,
                                onSelectBank = { manualAddBankId = it },
                                questionId = manualAddQuestionId,
                                onQuestionIdChange = { manualAddQuestionId = it },
                                errorReason = manualAddErrorReason,
                                onErrorReasonChange = { manualAddErrorReason = it },
                                feedback = manualAddFeedback,
                                onConfirm = {
                                    val bankId = manualAddBankId
                                    val qid = manualAddQuestionId.trim()
                                    if (bankId.isNullOrBlank()) {
                                        manualAddFeedback = "璇烽€夋嫨棰樺簱"
                                    } else if (qid.isBlank()) {
                                        manualAddFeedback = "璇疯緭鍏ユ垨閫夋嫨棰樼洰 ID"
                                    } else {
                                        val result = QuizRepository.addWrongContext(
                                            questionId = qid,
                                            bankId = bankId,
                                            userAnswer = emptyList(),
                                            errorReason = manualAddErrorReason.trim(),
                                            addedManually = true
                                        )
                                        if (result != null) {
                                            manualAddFeedback = "宸插姞鍏ラ敊棰樻湰"
                                            manualAddQuestionId = ""
                                            manualAddErrorReason = ""
                                        } else {
                                            manualAddFeedback = "鏈壘鍒拌棰樼洰锛岃妫€鏌ラ搴撲笌棰樼洰 ID"
                                        }
                                    }
                                },
                                onDismiss = { showManualAddDialog = false }
                            )
                        }

                        // 鑼冨洿鎽樿(Scope Header)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy((10 * uiScale).dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "閿欓 ${wrongBook.size} 鏉",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    StatusChip(if (isSingleBankScope) "鍗曢搴" else "鍏ㄩ儴棰樺簱", selected = isSingleBankScope)
                                }
                                Spacer(Modifier.height((6 * uiScale).dp))
                                Text(
                                    text = "鏈帉鎻?$notMasteredCount 路 宸叉帉鎻?$masteredCount",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            ActionPillButton(
                                icon = Icons.Rounded.DeleteOutline,
                                text = "娓呯┖",
                                primary = false,
                                enabled = wrongBook.isNotEmpty(),
                                modifier = Modifier.height((42 * uiScale).dp),
                                onClick = { showClearWrongBookConfirm = true }
                            )
                        }

                        // 閿欓鑼冨洿閫夋嫨
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shirohaNoRippleClickable { showScopeDialog = true },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(ShirohaRadius.Md),
                            color = ShirohaColors.CardWhite86,
                            border = BorderStroke(1.dp, ShirohaColors.LineStrong)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = (14 * uiScale).dp, vertical = (12 * uiScale).dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "閿欓鑼冨洿",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ShirohaColors.TextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height((2 * uiScale).dp))
                                    Text(
                                        text = scopeLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = selectedBank?.let { bank ->
                                            val groupName = bank.groupName.ifBlank { DEFAULT_BANK_GROUP_NAME }
                                            "$groupName 路 閿欓 ${wrongBook.size} 鏉"
                                        } ?: "褰撳墠鏄剧ず鍏ㄩ儴棰樺簱鐨?${allWrongBook.size} 閬撻敊棰",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.ExpandMore,
                                    contentDescription = "閫夋嫨閿欓鑼冨洿",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size((22 * uiScale).dp)
                                )
                            }
                        }

                        if (wrongBook.isEmpty()) {
                            NoticeCard("褰撳墠棰樺簱鏆傛棤閿欓銆傚彲浠ュ垏鎹㈠埌鍏朵粬棰樺簱鎴栧叏閮ㄩ搴撱€")
                        } else {
                            // 鏅鸿兘澶嶄範鍖?寮€鍚椂鏄剧ず)
                            if (smartReviewEnabled) {
                                EditorialSection(
                                    kicker = "浠婃棩",
                                    title = "鏅鸿兘澶嶄範",
                                    scale = scale
                                ) {
                                    WrongBookSmartReviewSection(
                                        total = smartReviewEntries.size,
                                        notMastered = smartReviewNotMastered,
                                        masteredReview = smartReviewMastered,
                                        onStart = {
                                            if (
                                                smartReviewEntries.isNotEmpty() &&
                                                QuizRepository.startWrongBookPractice(
                                                    entries = smartReviewEntries,
                                                    includeMastered = true,
                                                    sourceLabel = "浠婃棩澶嶄範"
                                                )
                                            ) {
                                                onGoPractice()
                                            }
                                        }
                                    )
                                }
                            }

                            // 澶嶄範璁剧疆:绛涢€?+ 棰樺瀷 + 鍗曟澶嶄範鏁伴噺
                            EditorialSection(
                                kicker = "绛涢€",
                                title = "澶嶄範璁剧疆",
                                scale = scale
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)) {
                                    // 鎺屾彙绛涢€?                                    Column(verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)) {
                                        Text(
                                            text = "鎺屾彙绛涢€",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                                        ) {
                                            WrongBookFilter.entries.forEach { item ->
                                                ActionPillButton(
                                                    icon = Icons.Rounded.CheckCircle,
                                                    text = item.label,
                                                    primary = filter == item,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height((44 * uiScale).dp),
                                                    fillWidthContent = true,
                                                    onClick = { filter = item }
                                                )
                                            }
                                        }
                                    }

                                    if (advancedReviewSettingsEnabled) {
                                        Column(verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)) {
                                            Text(
                                                text = "棰樺瀷",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (availableTypes.isEmpty()) {
                                                NoticeCard("褰撳墠鎺屾彙绛涢€変笅娌℃湁鍙€夋嫨鐨勯鍨嬨€")
                                            } else {
                                                FlowRow(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp),
                                                    verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                                                ) {
                                                    val allAvailableSelected = availableTypes.all { it in selectedTypes }
                                                    ActionPillButton(
                                                        icon = Icons.Rounded.CheckCircle,
                                                        text = "鍏ㄩ儴棰樺瀷",
                                                        primary = allAvailableSelected,
                                                        modifier = Modifier.height((42 * uiScale).dp),
                                                        onClick = { selectedTypes = QuestionType.entries.toSet() }
                                                    )
                                                    availableTypes.forEach { type ->
                                                        val count = masteryFilteredEntries.count { it.question.type == type }
                                                        ActionPillButton(
                                                            icon = Icons.Rounded.CheckCircle,
                                                            text = "${typeLabel(type)} $count",
                                                            primary = type in selectedTypes,
                                                            modifier = Modifier.height((42 * uiScale).dp),
                                                            onClick = {
                                                                selectedTypes = if (type in selectedTypes) {
                                                                    selectedTypes - type
                                                                } else {
                                                                    selectedTypes + type
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy((6 * uiScale).dp)) {
                                            Text(
                                                text = "鍗曟澶嶄範鏁伴噺",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "褰撳墠鍙涔?${reviewCandidates.size} 棰",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp),
                                                verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                                            ) {
                                                WrongBookReviewCountMode.entries.forEach { item ->
                                                    val label = when (item) {
                                                        WrongBookReviewCountMode.CUSTOM -> {
                                                            if (reviewCountMode == WrongBookReviewCountMode.CUSTOM) "鑷畾涔?${selectedReviewCount}棰" else item.label
                                                        }
                                                        WrongBookReviewCountMode.ALL -> "鍏ㄩ儴 ${reviewCandidates.size}棰"
                                                        else -> item.label
                                                    }
                                                    ActionPillButton(
                                                        icon = Icons.Rounded.PlayArrow,
                                                        text = label,
                                                        primary = reviewCountMode == item,
                                                        enabled = reviewCandidates.isNotEmpty(),
                                                        modifier = Modifier.height((42 * uiScale).dp),
                                                        onClick = {
                                                            if (item == WrongBookReviewCountMode.CUSTOM) {
                                                                customReviewCountText = customReviewCount
                                                                    .coerceIn(1, reviewCandidates.size.coerceAtLeast(1))
                                                                    .toString()
                                                                showCustomReviewCountDialog = true
                                                            } else {
                                                                reviewCountMode = item
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // 鎺掑簭
                                    Column(verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)) {
                                        Text(
                                            text = "鎺掑簭",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                                        ) {
                                            WrongBookSort.entries.forEach { item ->
                                                ActionPillButton(
                                                    icon = Icons.Rounded.PlayArrow,
                                                    text = item.label,
                                                    primary = sort == item,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .heightIn(min = (44 * uiScale).dp),
                                                    fillWidthContent = true,
                                                    textMaxLines = 2,
                                                    onClick = { sort = item }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 搴曢儴鎿嶄綔:寮€濮嬪涔?+ 杩斿洖
                            Spacer(Modifier.height((4 * uiScale).dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy((10 * uiScale).dp)
                            ) {
                                ActionPillButton(
                                    icon = Icons.Rounded.PlayArrow,
                                    text = when {
                                        reviewEntries.isEmpty() -> "鏆傛棤鍙涔犻鐩"
                                        advancedReviewSettingsEnabled -> "寮€濮嬪涔?${reviewEntries.size} 棰"
                                        else -> "鍒烽敊棰"
                                    },
                                    primary = reviewEntries.isNotEmpty(),
                                    enabled = reviewEntries.isNotEmpty(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height((50 * uiScale).dp),
                                    fillWidthContent = true,
                                    onClick = {
                                        if (reviewEntries.isNotEmpty() && QuizRepository.startWrongBookPractice(reviewEntries)) {
                                            onGoPractice()
                                        }
                                    }
                                )
                                ActionPillButton(
                                    icon = Icons.AutoMirrored.Rounded.Undo,
                                    text = "杩斿洖",
                                    primary = false,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height((50 * uiScale).dp),
                                    fillWidthContent = true,
                                    onClick = onBack
                                )
                            }

                            if (reviewEntries.isEmpty()) {
                                NoticeCard(
                                    text = when {
                                        advancedReviewSettingsEnabled && selectedTypes.none { it in availableTypes } -> "璇疯嚦灏戦€夋嫨涓€绉嶆湁閿欓鐨勯鍨嬨€"
                                        filter == WrongBookFilter.MASTERED -> "宸叉帉鎻￠涓嶄細杩涘叆鎵嬪姩澶嶄範銆傞渶瑕佸涔犳椂鍙厛鏍囦负鏈帉鎻°€"
                                        else -> "褰撳墠绛涢€変笅娌℃湁闇€瑕佸涔犵殑閿欓銆"
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (wrongBook.isNotEmpty()) {
                if (filteredEntries.isEmpty()) {
                    item {
                        EditorialDivider(label = "鍒楄〃")
                        NoticeCard("褰撳墠绛涢€変笅娌℃湁閿欓銆")
                    }
                } else {
                    item {
                        EditorialDivider(label = "鍒楄〃")
                    }
                    items(
                        items = filteredEntries,
                        key = { entry -> "${entry.bankId}#${entry.question.id}" }
                    ) { entry ->
                        WrongQuestionPreview(entry, uiScale = uiScale, scale = scale)
                    }
                }
            }
        }
    }
}

@Composable
private fun WrongBookScopeDialog(
    banks: List<QuizBank>,
    wrongBook: List<WrongQuestionEntry>,
    selectedScopeKey: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val wrongCountByBank = wrongBook.groupingBy { it.bankId }.eachCount()
    val groupedBanks = banks
        .groupBy { it.groupName.ifBlank { DEFAULT_BANK_GROUP_NAME } }
        .entries
        .sortedBy { entry -> if (entry.key == DEFAULT_BANK_GROUP_NAME) "" else entry.key }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("閫夋嫨閿欓鑼冨洿") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WrongBookScopeOption(
                    title = "鍏ㄩ儴棰樺簱",
                    desc = "鍏?${wrongBook.size} 閬撻敊棰",
                    selected = selectedScopeKey == WRONG_BOOK_PAGE_SCOPE_ALL,
                    onClick = { onSelect(WRONG_BOOK_PAGE_SCOPE_ALL) }
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
                        val key = WRONG_BOOK_PAGE_SCOPE_BANK_PREFIX + bank.id
                        WrongBookScopeOption(
                            title = bank.name,
                            desc = "閿欓 ${wrongCountByBank[bank.id] ?: 0} 棰$路 鍏?${bank.questions.size} 棰",
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
private fun WrongBookScopeOption(
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

@Composable
private fun WrongBookReviewCountDialog(
    value: String,
    maxCount: Int,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("鑷畾涔夊崟娆″涔犳暟閲") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "璇疯緭鍏$1锝$maxCount 涔嬮棿鐨勯鏁般€",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { onValueChange(it.filter { ch -> ch.isDigit() }.take(4)) },
                    singleLine = true,
                    label = { Text("澶嶄範棰橀噺") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val count = value.toIntOrNull()?.coerceIn(1, maxCount) ?: 1
                    onConfirm(count)
                }
            ) { Text("纭畾") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("鍙栨秷") } }
    )
}

@Composable
private fun WrongBookSmartReviewSection(
    total: Int,
    notMastered: Int,
    masteredReview: Int,
    onStart: () -> Unit
) {
    Text(
        text = "浠婃棩澶嶄範",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "鏈帉鎻?$notMastered 路 宸叉帉鎻″洖椤?$masteredReview",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(10.dp))
    ActionPillButton(
        icon = Icons.Rounded.PlayArrow,
        text = if (total > 0) "寮€濮嬩粖鏃ュ涔" else "浠婃棩鏆傛棤鍒版湡",
        primary = total > 0,
        modifier = Modifier
            .fillMaxWidth(0.58f)
            .height(44.dp),
        fillWidthContent = true,
        onClick = {
            if (total > 0) onStart()
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WrongQuestionPreview(
    entry: WrongQuestionEntry,
    uiScale: Float = 1f,
    scale: Float = 1f
) {
    var showRemoveConfirm by remember(entry.bankId, entry.question.id) { mutableStateOf(false) }
    var showDetail by remember(entry.bankId, entry.question.id) { mutableStateOf(false) }
    var analysisExpanded by remember(entry.bankId, entry.question.id) { mutableStateOf(false) }
    var reasonExpanded by remember(entry.bankId, entry.question.id) { mutableStateOf(false) }

    if (showRemoveConfirm) {
        ShirohaDangerConfirmDialog(
            title = "纭绉诲嚭杩欓亾閿欓锛",
            message = "杩欎細浠庨敊棰樻湰涓Щ鍑烘湰棰橈紝骞舵竻闄よ繖閬撻褰撳墠鐨勯敊棰樺涔犵姸鎬併€傚師棰樺簱涓殑棰樼洰涓嶄細琚垹闄ゃ€",
            confirmText = "纭绉诲嚭",
            onDismiss = { showRemoveConfirm = false },
            onConfirm = {
                QuizRepository.removeWrongQuestion(entry)
                showRemoveConfirm = false
            }
        )
    }

    if (showDetail) {
        WrongQuestionDetailDialog(
            entry = entry,
            analysisExpanded = analysisExpanded,
            reasonExpanded = reasonExpanded,
            onToggleAnalysis = { analysisExpanded = !analysisExpanded },
            onToggleReason = { reasonExpanded = !reasonExpanded },
            onDismiss = { showDetail = false }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ShirohaRadius.Lg),
        color = ShirohaColors.CardWhite86,
        border = BorderStroke(1.dp, ShirohaColors.LineSoft)
    ) {
        Column(modifier = Modifier.padding((16 * uiScale).dp.coerceAtLeast(12.dp))) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp),
            verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
        ) {
            StatusChip(displayWrongStatus(entry.status), selected = entry.status != WrongStatus.MASTERED.label)
            StatusChip(typeLabel(entry.question.type))
            StatusChip(entry.bankName)
            if (entry.addedManually) {
                StatusChip("鎵嬪姩娣诲姞", selected = true)
            } else {
                StatusChip("鑷姩璁板綍")
            }
        }
        Spacer(Modifier.height((12 * uiScale).dp))
        Text(
            text = wrongQuestionDisplayTitle(entry),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (entry.question.images.isNotEmpty()) {
            Spacer(Modifier.height((10 * uiScale).dp))
            QuestionImagesBlock(
                images = entry.question.images,
                maxPreviewHeight = 260.dp,
                showMeta = false
            )
        }
        if (entry.question.options.isNotEmpty()) {
            Spacer(Modifier.height((10 * uiScale).dp))
            entry.question.options.forEach { option ->
                Text(
                    text = "${option.key}. ${option.text}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height((4 * uiScale).dp))
            }
        }
        // 姝ｇ‘绛旀锛堢豢鑹查珮浜級
        Spacer(Modifier.height((10 * uiScale).dp))
        WrongBookAnswerLine(
            label = "姝ｇ‘绛旀",
            text = formatCorrectAnswerText(entry),
            highlightColor = ShirohaColors.StateSuccess
        )
        // 浣犵殑绛旀锛堢孩鑹查珮浜紝NEW 鏍囩锛?        WrongBookAnswerLine(
            label = "浣犵殑绛旀",
            text = formatUserAnswerText(entry),
            highlightColor = ShirohaColors.StateDanger,
            showNewBadge = true
        )
        // AI 瑙ｆ瀽鎶樺彔
        if (entry.aiAnalysis.isNotBlank()) {
            Spacer(Modifier.height((10 * uiScale).dp))
            WrongBookExpandableSection(
                title = "AI 瑙ｆ瀽",
                expanded = analysisExpanded,
                onToggle = { analysisExpanded = !analysisExpanded },
                content = entry.aiAnalysis
            )
        }
        // 閿欒鍘熷洜鎶樺彔
        if (entry.errorReason.isNotBlank()) {
            Spacer(Modifier.height((8 * uiScale).dp))
            WrongBookExpandableSection(
                title = "閿欒鍘熷洜",
                expanded = reasonExpanded,
                onToggle = { reasonExpanded = !reasonExpanded },
                content = entry.errorReason
            )
        }
        Spacer(Modifier.height((8 * uiScale).dp))
        Text(
            text = "閿?${entry.wrongCount} 娆$路 瀵?${entry.rightCount} 娆$路 鏈€杩戦敊璇?${formatTimestamp(entry.lastWrongAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height((12 * uiScale).dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy((10 * uiScale).dp)
        ) {
            ActionPillButton(
                icon = Icons.Rounded.CheckCircle,
                text = if (entry.status == WrongStatus.MASTERED.label) "閲嶆柊澶嶄範" else "鏍囪鎺屾彙",
                primary = entry.status != WrongStatus.MASTERED.label,
                modifier = Modifier
                    .weight(1f)
                    .height((46 * uiScale).dp),
                fillWidthContent = true,
                onClick = {
                    QuizRepository.markWrongQuestionMastered(
                        entry = entry,
                        mastered = entry.status != WrongStatus.MASTERED.label
                    )
                }
            )
            ActionPillButton(
                icon = Icons.Rounded.Visibility,
                text = "璇︽儏",
                primary = false,
                modifier = Modifier
                    .weight(1f)
                    .height((46 * uiScale).dp),
                fillWidthContent = true,
                onClick = { showDetail = true }
            )
            ActionPillButton(
                icon = Icons.Rounded.DeleteOutline,
                text = "绉诲嚭",
                primary = false,
                modifier = Modifier
                    .weight(1f)
                    .height((46 * uiScale).dp),
                fillWidthContent = true,
                onClick = { showRemoveConfirm = true }
            )
        }
        Spacer(Modifier.height((10 * uiScale).dp))
        QuestionAiAnalysisButton(
            question = entry.question,
            userAnswer = entry.lastAnswer
        )
        }
    }
}

@Composable
private fun WrongBookAnswerLine(
    label: String,
    text: String,
    highlightColor: Color,
    showNewBadge: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label锛",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = highlightColor,
            fontWeight = FontWeight.SemiBold
        )
        if (showNewBadge) {
            Spacer(Modifier.width(6.dp))
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(ShirohaRadius.Pill),
                color = ShirohaColors.StateDangerSoft
            ) {
                Text(
                    text = "NEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShirohaColors.StateDanger,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WrongBookExpandableSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shirohaNoRippleClickable(onClick = onToggle),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ShirohaRadius.Md),
        color = ShirohaColors.CardWhite86,
        border = BorderStroke(1.dp, ShirohaColors.LineSoft)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "鏀惰捣" else "灞曞紑",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WrongQuestionDetailDialog(
    entry: WrongQuestionEntry,
    analysisExpanded: Boolean,
    reasonExpanded: Boolean,
    onToggleAnalysis: () -> Unit,
    onToggleReason: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("閿欓璇︽儏") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(displayWrongStatus(entry.status), selected = entry.status != WrongStatus.MASTERED.label)
                    StatusChip(typeLabel(entry.question.type))
                    StatusChip(entry.bankName)
                    if (entry.addedManually) {
                        StatusChip("鎵嬪姩娣诲姞", selected = true)
                    } else {
                        StatusChip("鑷姩璁板綍")
                    }
                }
                Text(
                    text = wrongQuestionDisplayTitle(entry),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (entry.question.images.isNotEmpty()) {
                    QuestionImagesBlock(
                        images = entry.question.images,
                        maxPreviewHeight = 220.dp,
                        showMeta = false
                    )
                }
                if (entry.question.options.isNotEmpty()) {
                    entry.question.options.forEach { option ->
                        Text(
                            text = "${option.key}. ${option.text}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                WrongBookAnswerLine(
                    label = "姝ｇ‘绛旀",
                    text = formatCorrectAnswerText(entry),
                    highlightColor = ShirohaColors.StateSuccess
                )
                WrongBookAnswerLine(
                    label = "浣犵殑绛旀",
                    text = formatUserAnswerText(entry),
                    highlightColor = ShirohaColors.StateDanger,
                    showNewBadge = true
                )
                if (entry.aiAnalysis.isNotBlank()) {
                    WrongBookExpandableSection(
                        title = "AI 瑙ｆ瀽",
                        expanded = analysisExpanded,
                        onToggle = onToggleAnalysis,
                        content = entry.aiAnalysis
                    )
                } else {
                    NoticeCard("鏆傛棤 AI 瑙ｆ瀽缂撳瓨銆")
                }
                if (entry.errorReason.isNotBlank()) {
                    WrongBookExpandableSection(
                        title = "閿欒鍘熷洜",
                        expanded = reasonExpanded,
                        onToggle = onToggleReason,
                        content = entry.errorReason
                    )
                } else {
                    NoticeCard("灏氭湭濉啓閿欒鍘熷洜銆")
                }
                Text(
                    text = "閿?${entry.wrongCount} 娆$路 瀵?${entry.rightCount} 娆$路 鏈€杩戦敊璇?${formatTimestamp(entry.lastWrongAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("鍏抽棴") } }
    )
}

private fun formatUserAnswerText(entry: WrongQuestionEntry): String {
    val fromNew = entry.userAnswer.joinToString(" / ").trim()
    if (fromNew.isNotBlank()) return fromNew
    // 鍏煎鏃ф暟鎹細浣跨敤 lastAnswer
    val fromLegacy = entry.lastAnswer.joinToString(" / ").trim()
    return fromLegacy.ifBlank { "鏈綔绛" }
}

private fun formatCorrectAnswerText(entry: WrongQuestionEntry): String {
    val fromNew = entry.correctAnswer.joinToString(" / ").trim()
    if (fromNew.isNotBlank()) return fromNew
    // 鍏煎鏃ф暟鎹細浠?question.answer 鍏滃簳
    return if (MultiBlankSupport.hasStructuredAnswers(entry.question)) {
        MultiBlankSupport.expectedAnswerText(entry.question.blankAnswers)
    } else {
        entry.question.answer.joinToString(" / ").ifBlank { "鏈瘑鍒瓟妗" }
    }
}

private fun wrongQuestionDisplayTitle(entry: WrongQuestionEntry): String {
    val number = entry.question.number.trim()
    val stem = entry.question.question.trim()
    val safeNumber = number.takeUnless(::isSuspiciousWrongQuestionNumber)

    return when {
        safeNumber.isNullOrBlank() -> stem
        stem.isBlank() -> safeNumber
        else -> "$safeNumber. $stem"
    }
}

private fun isSuspiciousWrongQuestionNumber(number: String): Boolean {
    if (number.isBlank()) return false
    val compact = number.replace(Regex("""\s+"""), "")
    if (compact.length > 24) return true

    val numericParts = Regex("""\d+""").findAll(compact).count()
    val dashCount = compact.count { it == '-' || it == '锛? || it == '鈥" || it == '鈥" }
    return numericParts >= 4 && dashCount >= 3
}

private fun List<WrongQuestionEntry>.filterBy(filter: WrongBookFilter): List<WrongQuestionEntry> {
    return when (filter) {
        WrongBookFilter.NOT_MASTERED -> filter { it.status != WrongStatus.MASTERED.label }
        WrongBookFilter.MASTERED -> filter { it.status == WrongStatus.MASTERED.label }
        WrongBookFilter.ALL -> this
    }
}

private fun List<WrongQuestionEntry>.sortBy(sort: WrongBookSort): List<WrongQuestionEntry> {
    return when (sort) {
        WrongBookSort.RECENT_WRONG -> sortedByDescending { it.lastWrongAt }
        WrongBookSort.WRONG_COUNT -> sortedWith(compareByDescending<WrongQuestionEntry> { it.wrongCount }.thenByDescending { it.lastWrongAt })
        WrongBookSort.MASTERY -> sortedWith(compareBy<WrongQuestionEntry> { statusRank(it.status) }.thenByDescending { it.wrongCount })
    }
}

private fun resolveWrongBookReviewCount(
    mode: WrongBookReviewCountMode,
    customCount: Int,
    availableCount: Int
): Int {
    if (availableCount <= 0) return 0
    return when (mode) {
        WrongBookReviewCountMode.TEN -> 10.coerceAtMost(availableCount)
        WrongBookReviewCountMode.TWENTY -> 20.coerceAtMost(availableCount)
        WrongBookReviewCountMode.CUSTOM -> customCount.coerceIn(1, availableCount)
        WrongBookReviewCountMode.ALL -> availableCount
    }
}

private fun isWrongEntryDueForPageReview(entry: WrongQuestionEntry, now: Long): Boolean {
    val dueAt = entry.nextReviewAt ?: when (entry.status) {
        WrongStatus.MASTERED.label -> return false
        else -> startOfPageDay(now)
    }
    return dueAt <= now
}

private fun startOfPageDay(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun statusRank(status: String): Int = when (status) {
    WrongStatus.MASTERED.label -> 1
    else -> 0
}

private fun displayWrongStatus(status: String): String =
    if (status == WrongStatus.MASTERED.label) "宸叉帉鎻" else "鏈帉鎻"

private fun typeLabel(type: QuestionType): String = when (type) {
    QuestionType.SINGLE -> "鍗曢€夐"
    QuestionType.MULTIPLE -> "澶氶€夐"
    QuestionType.JUDGE -> "鍒ゆ柇棰"
    QuestionType.BLANK -> "濉┖棰"
    QuestionType.SHORT -> "绠€绛旈"
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}


@Composable
private fun WrongBookManualAddDialog(
    banks: List<QuizBank>,
    selectedBankId: String?,
    onSelectBank: (String) -> Unit,
    questionId: String,
    onQuestionIdChange: (String) -> Unit,
    errorReason: String,
    onErrorReasonChange: (String) -> Unit,
    feedback: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentBank = banks.firstOrNull { it.id == selectedBankId }
    val questionsInBank = currentBank?.questions.orEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("鎵嬪姩娣诲姞閿欓") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "閫夋嫨棰樺簱鍚庯紝浠庨搴撳垪琛ㄤ腑鐐归€夎鍔犲叆閿欓鏈殑棰樼洰銆",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 棰樺簱涓嬫媺
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shirohaNoRippleClickable { },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(ShirohaRadius.Md),
                    color = ShirohaColors.CardWhite86,
                    border = BorderStroke(1.dp, ShirohaColors.LineStrong)
                ) {
                    var bankMenuExpanded by remember { mutableStateOf(false) }
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shirohaNoRippleClickable { bankMenuExpanded = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentBank?.name ?: "閫夋嫨棰樺簱",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = currentBank?.let { "${it.questions.size} 棰$路 ${it.groupName.ifBlank { DEFAULT_BANK_GROUP_NAME }}" }
                                        ?: "灏氭湭閫夋嫨",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.ExpandMore,
                                contentDescription = "閫夋嫨棰樺簱",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = bankMenuExpanded,
                            onDismissRequest = { bankMenuExpanded = false },
                            modifier = Modifier.heightIn(max = 320.dp)
                        ) {
                            banks.forEach { bank ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(bank.name, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                text = "${bank.questions.size} 棰$路 ${bank.groupName.ifBlank { DEFAULT_BANK_GROUP_NAME }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        bankMenuExpanded = false
                                        onSelectBank(bank.id)
                                    }
                                )
                            }
                        }
                    }
                }

                // 棰樼洰 ID 杈撳叆锛堝悓鏃跺彲浣滀负鎵嬪姩杈撳叆锛?                OutlinedTextField(
                    value = questionId,
                    onValueChange = onQuestionIdChange,
                    label = { Text("棰樼洰 ID锛堟墜鍔ㄨ緭鍏ユ垨浠庝笅鏂归€夋嫨锛") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 褰撳墠棰樺簱棰樼洰鍒楄〃锛堢簿绠€灞曠ず锛?                if (questionsInBank.isNotEmpty()) {
                    Text(
                        text = "棰樺簱棰樼洰锛堢偣鍑诲嵆閫変腑锛",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShirohaColors.TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    questionsInBank.take(80).forEach { question ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shirohaNoRippleClickable { onQuestionIdChange(question.id) },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(ShirohaRadius.Sm),
                            color = if (questionId == question.id) ShirohaColors.BrandPrimarySoft else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (questionId == question.id) ShirohaColors.LineSelected else ShirohaColors.LineSoft
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Text(
                                    text = wrongQuestionDisplayTitle(WrongQuestionEntry(bankId = currentBank!!.id, bankName = currentBank.name, question = question, lastAnswer = emptyList(), source = "", timestamp = 0L)),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "ID: ${question.id}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (questionsInBank.size > 80) {
                        Text(
                            text = "浠呭睍绀哄墠 80 棰橈紝鍓╀綑璇蜂娇鐢ㄤ笂鏂$ID 杈撳叆銆",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (currentBank != null) {
                    NoticeCard("鎵€閫夐搴撴殏鏃犻鐩€")
                }

                // 閿欒鍘熷洜
                OutlinedTextField(
                    value = errorReason,
                    onValueChange = onErrorReasonChange,
                    label = { Text("閿欒鍘熷洜锛堝彲閫夛級") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                if (!feedback.isNullOrBlank()) {
                    NoticeCard(feedback)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("鍔犲叆閿欓鏈") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("鍙栨秷") }
        }
    )
}
