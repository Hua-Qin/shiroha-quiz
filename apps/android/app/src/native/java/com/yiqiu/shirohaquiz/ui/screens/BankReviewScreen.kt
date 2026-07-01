package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.BorderStroke
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.importer.model.MultiBlankSupport
import com.yiqiu.shirohaquiz.importer.model.Option
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.AiAnalysisFillPanel
import com.yiqiu.shirohaquiz.ui.components.EmptyStateIllustration
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.MultiBlankAnswerEditor
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.QuestionImagesBlock
import com.yiqiu.shirohaquiz.ui.components.ShirohaDangerConfirmDialog
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BankReviewScreen(
    bankId: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val bank = QuizRepository.banks.firstOrNull { it.id == bankId } ?: QuizRepository.activeBank()

    if (bank == null) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            ShirohaHeader(
                kicker = "Review",
                title = "棰樺簱鏍稿",
                subtitle = "娌℃湁鎵惧埌闇€瑕佹牳瀵圭殑棰樺簱銆"
            )
            EmptyStateIllustration(
                title = "棰樺簱涓嶅瓨鍦",
                message = "杩欎唤棰樺簱鍙兘宸茬粡琚垹闄ゆ垨鍒囨崲銆",
                imageRes = R.drawable.illus_empty_state_webp
            )
            ActionPillButton(
                icon = Icons.Rounded.ArrowBack,
                text = "杩斿洖璇︽儏",
                primary = false,
                onClick = onBack
            )
        }
        return
    }

    val editableQuestions = remember(bank.id) { bank.questions.toMutableStateList() }
    var currentIndex by rememberSaveable(bank.id) { mutableStateOf(0) }
    var filterName by rememberSaveable(bank.id) { mutableStateOf(BankReviewFilter.ALL.name) }
    var query by rememberSaveable(bank.id) { mutableStateOf("") }
    var showRemoveImagesConfirm by rememberSaveable(bank.id) { mutableStateOf(false) }
    var showDeleteLastOptionConfirm by rememberSaveable(bank.id) { mutableStateOf(false) }
    var showDeleteQuestionConfirm by rememberSaveable(bank.id) { mutableStateOf(false) }
    var showInsertQuestionMenu by rememberSaveable(bank.id) { mutableStateOf(false) }
    val filter = bankReviewFilterFromName(filterName)

    val allIndices = editableQuestions.indices.toList()
    val filteredByType = editableQuestions.indices.filter { index ->
        bankQuestionMatchesFilter(editableQuestions[index], filter)
    }
    val searchText = query.trim()
    val visibleIndices = (if (filter == BankReviewFilter.ALL) allIndices else filteredByType).filter { index ->
        if (searchText.isBlank()) true else bankQuestionMatchesQuery(editableQuestions[index], searchText)
    }

    val safeIndex = when {
        editableQuestions.isEmpty() -> 0
        visibleIndices.isEmpty() -> currentIndex.coerceIn(0, editableQuestions.lastIndex)
        currentIndex in visibleIndices -> currentIndex
        else -> visibleIndices.first()
    }
    if (editableQuestions.isNotEmpty() && safeIndex != currentIndex) currentIndex = safeIndex

    if (showRemoveImagesConfirm && editableQuestions.isNotEmpty()) {
        ShirohaDangerConfirmDialog(
            title = "纭绉婚櫎鏈鍥剧墖锛",
            message = "杩欎細浠庡綋鍓嶆牳瀵归鐩腑绉婚櫎宸茬粦瀹氬浘鐗囥€傞渶瑕佺偣鍑讳繚瀛樿繑鍥炲悗鎵嶄細鍐欏叆棰樺簱銆",
            confirmText = "纭绉婚櫎",
            onDismiss = { showRemoveImagesConfirm = false },
            onConfirm = {
                val target = editableQuestions.getOrNull(safeIndex)
                if (target != null) {
                    editableQuestions[safeIndex] = target.copy(images = emptyList())
                }
                showRemoveImagesConfirm = false
            }
        )
    }

    if (showDeleteLastOptionConfirm && editableQuestions.isNotEmpty()) {
        ShirohaDangerConfirmDialog(
            title = "纭鍒犻櫎鏈€鍚庝竴涓€夐」锛",
            message = "杩欎細鍒犻櫎褰撳墠棰樼洰鐨勬渶鍚庝竴涓€夐」銆傞渶瑕佺偣鍑讳繚瀛樿繑鍥炲悗鎵嶄細鍐欏叆棰樺簱銆",
            confirmText = "纭鍒犻櫎",
            onDismiss = { showDeleteLastOptionConfirm = false },
            onConfirm = {
                val target = editableQuestions.getOrNull(safeIndex)
                if (target != null && target.options.isNotEmpty()) {
                    editableQuestions[safeIndex] = target.copy(options = target.options.dropLast(1))
                }
                showDeleteLastOptionConfirm = false
            }
        )
    }

    if (showDeleteQuestionConfirm && editableQuestions.isNotEmpty()) {
        ShirohaDangerConfirmDialog(
            title = "纭鍒犻櫎鏈锛",
            message = "杩欎細浠庡綋鍓嶆牳瀵瑰垪琛ㄤ腑鍒犻櫎鏈銆傞渶瑕佺偣鍑讳繚瀛樿繑鍥炲悗鎵嶄細鐪熸鍐欏叆棰樺簱銆",
            confirmText = "纭鍒犻櫎",
            onDismiss = { showDeleteQuestionConfirm = false },
            onConfirm = {
                if (safeIndex in editableQuestions.indices) {
                    editableQuestions.removeAt(safeIndex)
                    currentIndex = safeIndex.coerceAtMost((editableQuestions.size - 1).coerceAtLeast(0))
                }
                showDeleteQuestionConfirm = false
            }
        )
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Review",
            title = "棰樺簱鏍稿",
            subtitle = "${bank.name} 路 ${editableQuestions.size} 棰樸€"
        )

        GlassCard {
            Text(
                text = "鏍稿绛涢€",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BankReviewFilter.values().forEach { item ->
                    ReviewTypeChip(
                        text = "${bankReviewFilterLabel(item)} ${bankReviewFilterCount(editableQuestions, item)}",
                        selected = filter == item,
                        onClick = {
                            filterName = item.name
                            currentIndex = firstMatchingQuestionIndex(editableQuestions, item, query) ?: 0
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { value ->
                    query = value
                    currentIndex = firstMatchingQuestionIndex(editableQuestions, filter, value) ?: currentIndex
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("鎼滅储棰樺共 / 閫夐」 / 绛旀") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true
            )
            if (visibleIndices.isEmpty()) {
                Spacer(Modifier.height(12.dp))
                NoticeCard("褰撳墠绛涢€変笅娌℃湁棰樼洰銆傚彲浠ュ垏鍥炩€滃叏閮ㄢ€濇垨娓呯┖鎼滅储鍏抽敭璇嶃€", warning = false)
            }
        }

        if (editableQuestions.isEmpty()) {
            GlassCard {
                NoticeCard("褰撳墠棰樺簱宸茬粡娌℃湁棰樼洰銆備繚瀛樺悗杩欎唤棰樺簱浼氬彉鎴愮┖棰樺簱銆", warning = true)
                Spacer(Modifier.height(12.dp))
                ActionPillButton(
                    icon = Icons.Rounded.Done,
                    text = "淇濆瓨骞惰繑鍥",
                    primary = true,
                    onClick = {
                        QuizRepository.replaceBankQuestions(context, bank.id, editableQuestions)
                        onBack()
                    }
                )
            }
            return@Column
        }

        if (visibleIndices.isNotEmpty()) {
            val question = editableQuestions[safeIndex]
            val visiblePosition = visibleIndices.indexOf(safeIndex).takeIf { it >= 0 } ?: 0
            val anomalyIndices = editableQuestions.indices.filter { index ->
                bankQuestionMatchesFilter(editableQuestions[index], BankReviewFilter.ANOMALY)
            }

            fun insertQuestion(position: InsertQuestionPosition) {
                val anchorIndex = editableQuestions.indexOfFirst { item -> item.id == question.id }
                if (anchorIndex < 0) return

                val newQuestion = newQuestionFromAnchor(question)
                val insertIndex = when (position) {
                    InsertQuestionPosition.BEFORE -> anchorIndex
                    InsertQuestionPosition.AFTER -> anchorIndex + 1
                }
                editableQuestions.add(insertIndex, newQuestion)

                if (!bankQuestionMatchesFilter(newQuestion, filter) || !bankQuestionMatchesQuery(newQuestion, searchText)) {
                    filterName = BankReviewFilter.ALL.name
                    query = ""
                }
                currentIndex = insertIndex
                showInsertQuestionMenu = false
            }

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "绗?${safeIndex + 1} / ${editableQuestions.size} 棰",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = buildString {
                                append(typeLabel(question.type))
                                append(" 路 绛旀锛")
                                append(answerDisplayText(question))
                                if (filter != BankReviewFilter.ALL || searchText.isNotBlank()) {
                                    append(" 路 褰撳墠绛涢€?${visiblePosition + 1}/${visibleIndices.size}")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ReviewCompactButton(
                        icon = Icons.Rounded.CheckCircle,
                        text = "淇濆瓨杩斿洖",
                        primary = true,
                        onClick = {
                            QuizRepository.replaceBankQuestions(context, bank.id, editableQuestions)
                            onBack()
                        }
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReviewCompactButton(
                        icon = Icons.Rounded.ArrowBack,
                        text = "鏀惧純",
                        modifier = Modifier.weight(1f),
                        onClick = onBack
                    )
                    ReviewCompactButton(
                        icon = Icons.Rounded.ArrowBack,
                        text = if (filter == BankReviewFilter.ALL && searchText.isBlank()) "涓婁竴棰" else "涓婁竴鏉",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val target = previousIndexInList(visibleIndices, safeIndex) ?: (safeIndex - 1).coerceAtLeast(0)
                            currentIndex = target
                        }
                    )
                    ReviewCompactButton(
                        icon = Icons.Rounded.ArrowForward,
                        text = if (filter == BankReviewFilter.ALL && searchText.isBlank()) "涓嬩竴棰" else "涓嬩竴鏉",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val target = nextIndexInList(visibleIndices, safeIndex) ?: (safeIndex + 1).coerceAtMost(editableQuestions.lastIndex)
                            currentIndex = target
                        }
                    )
                }
                if (anomalyIndices.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReviewCompactButton(
                            icon = Icons.Rounded.ArrowBack,
                            text = "涓婁竴寮傚父",
                            modifier = Modifier.weight(1f),
                            onClick = { previousIndexInList(anomalyIndices, safeIndex)?.let { currentIndex = it } }
                        )
                        ReviewCompactButton(
                            icon = Icons.Rounded.ArrowForward,
                            text = "涓嬩竴寮傚父",
                            modifier = Modifier.weight(1f),
                            onClick = { nextIndexInList(anomalyIndices, safeIndex)?.let { currentIndex = it } }
                        )
                    }
                }
            }

            if ((filter != BankReviewFilter.ALL || searchText.isNotBlank()) && visibleIndices.size > 1) {
                ReviewFilteredJumpList(
                    questions = editableQuestions,
                    indices = visibleIndices,
                    currentIndex = safeIndex,
                    onIndexChange = { currentIndex = it }
                )
            }

            if (bankReviewTips(question).isNotEmpty()) {
                GlassCard {
                    Text(
                        text = "鏈鎻愮ず",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))
                    bankReviewTips(question).forEach { tip ->
                        NoticeCard(tip, warning = true)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "棰樼洰鍐呭",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box {
                        ReviewCompactButton(
                            icon = Icons.Rounded.Add,
                            text = "鎻掑叆鏂伴",
                            onClick = { showInsertQuestionMenu = true }
                        )
                        DropdownMenu(
                            expanded = showInsertQuestionMenu,
                            onDismissRequest = { showInsertQuestionMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("鍦ㄥ綋鍓嶉鍓嶆彃鍏") },
                                onClick = { insertQuestion(InsertQuestionPosition.BEFORE) }
                            )
                            DropdownMenuItem(
                                text = { Text("鍦ㄥ綋鍓嶉鍚庢彃鍏") },
                                onClick = { insertQuestion(InsertQuestionPosition.AFTER) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = question.number,
                        onValueChange = { value ->
                            editableQuestions[safeIndex] = question.copy(number = value)
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("棰樺彿") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = question.category,
                        onValueChange = { value ->
                            editableQuestions[safeIndex] = question.copy(category = value)
                        },
                        modifier = Modifier.weight(1.4f),
                        label = { Text("鍒嗗尯/鏉ユ簮") },
                        singleLine = true
                    )
                }
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuestionType.values().forEach { type ->
                        ReviewTypeChip(
                            text = typeLabel(type),
                            selected = question.type == type,
                            onClick = { editableQuestions[safeIndex] = normalizeAfterTypeChange(question, type) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = question.question,
                    onValueChange = { value ->
                        editableQuestions[safeIndex] = question.copy(question = value)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    minLines = 6,
                    label = { Text("棰樺共") },
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }

            if (question.images.isNotEmpty()) {
                GlassCard {
                    Text(
                        text = "棰樼洰鍥剧墖",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    NoticeCard("鍥剧墖鏉ヨ嚜鍘熼搴撹祫婧愩€傝纭鍥剧墖鏄惁灞炰簬鏈銆", warning = false)
                    Spacer(Modifier.height(12.dp))
                    QuestionImagesBlock(question.images, maxPreviewHeight = 360.dp, showMeta = true)
                    Spacer(Modifier.height(12.dp))
                    ActionPillButton(
                        icon = Icons.Rounded.Delete,
                        text = "绉婚櫎鏈鍥剧墖",
                        primary = false,
                        onClick = { showRemoveImagesConfirm = true }
                    )
                }
            }

            GlassCard {
                Text(
                    text = "閫夐」",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                if (question.options.isEmpty()) {
                    NoticeCard("褰撳墠棰樼洰娌℃湁閫夐」銆傚垽鏂鍙互琛ラ綈鍒ゆ柇閫夐」锛岄€夋嫨棰樺彲浠ユ坊鍔犻€夐」銆", warning = false)
                    Spacer(Modifier.height(12.dp))
                }
                question.options.forEachIndexed { optionIndex, option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = option.key,
                            onValueChange = { value ->
                                val updated = question.options.mapIndexed { currentOptionIndex, item ->
                                    if (currentOptionIndex == optionIndex) item.copy(key = value.uppercase().take(2)) else item
                                }
                                editableQuestions[safeIndex] = question.copy(options = updated)
                            },
                            modifier = Modifier.width(74.dp),
                            label = { Text("椤") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = option.text,
                            onValueChange = { value ->
                                val updated = question.options.mapIndexed { currentOptionIndex, item ->
                                    if (currentOptionIndex == optionIndex) item.copy(text = value) else item
                                }
                                editableQuestions[safeIndex] = question.copy(options = updated)
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("閫夐」鍐呭") }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionPillButton(
                        icon = Icons.Rounded.Add,
                        text = "娣诲姞閫夐」",
                        primary = false,
                        onClick = {
                            val key = nextOptionKey(question.options)
                            editableQuestions[safeIndex] = question.copy(options = question.options + Option(key, ""))
                        }
                    )
                    ActionPillButton(
                        icon = Icons.Rounded.RemoveCircle,
                        text = "鍒犻櫎鏈€鍚庨€夐」",
                        primary = false,
                        onClick = {
                            if (question.options.isNotEmpty()) {
                                showDeleteLastOptionConfirm = true
                            }
                        }
                    )
                    ActionPillButton(
                        icon = Icons.Rounded.CheckCircle,
                        text = "琛ラ綈鍒ゆ柇閫夐」",
                        primary = false,
                        onClick = {
                            editableQuestions[safeIndex] = question.copy(
                                type = QuestionType.JUDGE,
                                options = defaultJudgeOptions(),
                                answer = if (question.answer.isEmpty()) listOf("A") else question.answer,
                                blankAnswers = emptyList()
                            )
                        }
                    )
                }
            }

            GlassCard {
                Text(
                    text = "绛旀涓庤В鏋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                val detectedBlankCount = MultiBlankSupport.countExplicitBlanks(question.question)
                if (question.type == QuestionType.BLANK && question.blankAnswers.isNotEmpty()) {
                    MultiBlankAnswerEditor(
                        blankAnswers = question.blankAnswers,
                        detectedBlankCount = detectedBlankCount,
                        onChange = { groups ->
                            editableQuestions[safeIndex] = MultiBlankSupport.withBlankAnswers(question, groups)
                        },
                        onDisable = {
                            editableQuestions[safeIndex] = question.copy(
                                answer = MultiBlankSupport.compatibilityAnswer(question.blankAnswers),
                                blankAnswers = emptyList()
                            )
                        }
                    )
                } else {
                    OutlinedTextField(
                        value = answerInputText(question),
                        onValueChange = { value ->
                            editableQuestions[safeIndex] = question.copy(
                                answer = parseReviewAnswer(value, question.type),
                                blankAnswers = emptyList()
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("绛旀锛氬崟閫夊～ A锛屽閫夊～ ABC 鎴?A,B,C锛屽垽鏂～ 姝ｇ‘/閿欒") },
                        singleLine = true
                    )
                    if (question.type == QuestionType.BLANK && detectedBlankCount > 1) {
                        Spacer(Modifier.height(10.dp))
                        ActionPillButton(
                            icon = Icons.Rounded.Add,
                            text = "鍚敤澶氱┖绛旀",
                            primary = false,
                            onClick = {
                                editableQuestions[safeIndex] = MultiBlankSupport.withBlankAnswers(
                                    question,
                                    MultiBlankSupport.initialGroups(question.question, question.answer)
                                )
                            }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = question.analysis,
                    onValueChange = { value ->
                        editableQuestions[safeIndex] = question.copy(analysis = value)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    minLines = 5,
                    label = { Text("瑙ｆ瀽") },
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(10.dp))
                AiAnalysisFillPanel(
                    question = question,
                    currentAnalysis = question.analysis,
                    onApplyAnalysis = { value ->
                        editableQuestions[safeIndex] = question.copy(analysis = value)
                    }
                )
            }

            GlassCard {
                Text(
                    text = "鍗遍櫓鎿嶄綔",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "濡傛灉瑙ｆ瀽鍣ㄦ妸璇存槑鏂囧瓧銆侀〉鐪夐〉鑴氭垨纰庣墖娈佃瘑鍒垚棰樼洰锛屽彲浠ュ垹闄ゆ湰棰樸€傚垹闄ゅ悗闇€瑕佺偣鍑讳繚瀛樿繑鍥炴墠浼氱湡姝ｅ啓鍏ラ搴撱€",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                ActionPillButton(
                    icon = Icons.Rounded.Delete,
                    text = "鍒犻櫎鏈",
                    primary = false,
                    onClick = { showDeleteQuestionConfirm = true }
                )
            }
        }
    }
}

private enum class BankReviewFilter {
    ALL,
    ANOMALY,
    NO_ANSWER,
    IMAGE,
    HARD_ERROR
}

private enum class InsertQuestionPosition {
    BEFORE,
    AFTER
}

private fun newQuestionFromAnchor(anchor: Question): Question {
    return Question(
        type = anchor.type,
        question = "",
        category = anchor.category,
        score = anchor.score
    )
}

private fun bankReviewFilterFromName(name: String): BankReviewFilter {
    return runCatching { BankReviewFilter.valueOf(name) }.getOrDefault(BankReviewFilter.ALL)
}

private fun bankReviewFilterLabel(filter: BankReviewFilter): String = when (filter) {
    BankReviewFilter.ALL -> "鍏ㄩ儴"
    BankReviewFilter.ANOMALY -> "鍙枒椤"
    BankReviewFilter.NO_ANSWER -> "鏃犵瓟妗"
    BankReviewFilter.IMAGE -> "鍥剧墖棰"
    BankReviewFilter.HARD_ERROR -> "纭敊璇"
}

private fun bankReviewFilterCount(
    questions: List<Question>,
    filter: BankReviewFilter
): Int {
    if (filter == BankReviewFilter.ALL) return questions.size
    return questions.count { bankQuestionMatchesFilter(it, filter) }
}

private fun firstMatchingQuestionIndex(
    questions: List<Question>,
    filter: BankReviewFilter,
    query: String
): Int? {
    return questions.indices.firstOrNull { index ->
        bankQuestionMatchesFilter(questions[index], filter) && bankQuestionMatchesQuery(questions[index], query.trim())
    }
}

private fun bankQuestionMatchesFilter(question: Question, filter: BankReviewFilter): Boolean {
    return when (filter) {
        BankReviewFilter.ALL -> true
        BankReviewFilter.ANOMALY -> bankReviewTips(question).isNotEmpty()
        BankReviewFilter.NO_ANSWER -> question.answer.isEmpty()
        BankReviewFilter.IMAGE -> question.images.isNotEmpty()
        BankReviewFilter.HARD_ERROR -> bankHasHardReviewError(question)
    }
}

private fun bankQuestionMatchesQuery(question: Question, query: String): Boolean {
    if (query.isBlank()) return true
    val normalized = query.lowercase()
    return question.question.lowercase().contains(normalized) ||
        question.number.lowercase().contains(normalized) ||
        question.category.lowercase().contains(normalized) ||
        question.answer.joinToString(" ").lowercase().contains(normalized) ||
        question.blankAnswers.flatten().joinToString(" ").lowercase().contains(normalized) ||
        question.options.any { option ->
            option.key.lowercase().contains(normalized) || option.text.lowercase().contains(normalized)
        }
}

private fun bankReviewTips(question: Question): List<String> {
    val tips = mutableListOf<String>()
    if (question.question.isBlank()) tips += "棰樺共涓虹┖銆"
    if (question.answer.isEmpty()) tips += "鏈瘑鍒埌绛旀銆"
    if (question.type in listOf(QuestionType.SINGLE, QuestionType.MULTIPLE) && question.options.size < 2) {
        tips += "閫夋嫨棰橀€夐」鏁伴噺鍋忓皯銆"
    }
    if (bankHasHardReviewError(question)) tips += "瀛樺湪纭敊璇紝寤鸿浼樺厛澶勭悊銆"
    return tips.distinct()
}

private fun bankHasHardReviewError(question: Question): Boolean {
    return question.question.isBlank() ||
        (question.type in listOf(QuestionType.SINGLE, QuestionType.MULTIPLE) && question.options.isEmpty())
}

private fun previousIndexInList(indices: List<Int>, currentIndex: Int): Int? {
    if (indices.isEmpty()) return null
    return indices.lastOrNull { it < currentIndex } ?: indices.lastOrNull()
}

private fun nextIndexInList(indices: List<Int>, currentIndex: Int): Int? {
    if (indices.isEmpty()) return null
    return indices.firstOrNull { it > currentIndex } ?: indices.firstOrNull()
}

@Composable
private fun ReviewFilteredJumpList(
    questions: List<Question>,
    indices: List<Int>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit
) {
    GlassCard {
        Text(
            text = "褰撳墠绛涢€夊垪琛",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        indices.take(12).forEach { index ->
            val question = questions[index]
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shirohaNoRippleClickable { onIndexChange(index) },
                shape = RoundedCornerShape(ShirohaRadius.Md),
                color = if (index == currentIndex) ShirohaColors.BrandPrimarySoft else Color.White.copy(alpha = 0.72f),
                border = BorderStroke(
                    1.dp,
                    if (index == currentIndex) ShirohaColors.LineSelected else ShirohaColors.LineStrong
                )
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = "绗?${index + 1} 棰?路 ${typeLabel(question.type)} 路 绛旀锛${answerDisplayText(question)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = question.question.ifBlank { "棰樺共涓虹┖" }.take(70),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (indices.size > 12) {
            NoticeCard("褰撳墠绛涢€夊叡 ${indices.size} 棰橈紝杩欓噷鍏堟樉绀哄墠 12 棰橈紱鍙互鐢ㄢ€滀笂涓€鏉?/ 涓嬩竴鏉♀€濈户缁牳瀵广€", warning = false)
        }
    }
}

@Composable
private fun ReviewTypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.shirohaNoRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = if (selected) ShirohaColors.BrandPrimarySoft else Color.White.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, if (selected) ShirohaColors.LineSelected else ShirohaColors.LineStrong)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ReviewCompactButton(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.shirohaNoRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = if (primary) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.82f),
        border = if (primary) null else BorderStroke(1.dp, ShirohaColors.LineStrong)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(15.dp),
                tint = if (primary) Color.White else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = text,
                color = if (primary) Color.White else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun normalizeAfterTypeChange(question: Question, type: QuestionType): Question {
    return when (type) {
        QuestionType.JUDGE -> question.copy(
            type = type,
            options = if (question.options.isEmpty()) defaultJudgeOptions() else question.options,
            answer = normalizeJudgeAnswer(question.answer),
            blankAnswers = emptyList()
        )

        QuestionType.SINGLE,
        QuestionType.MULTIPLE -> question.copy(type = type, blankAnswers = emptyList())

        QuestionType.BLANK -> question.copy(type = type)
        QuestionType.SHORT -> question.copy(type = type, blankAnswers = emptyList())
    }
}

private fun normalizeJudgeAnswer(answer: List<String>): List<String> {
    if (answer.isEmpty()) return emptyList()
    return answer.mapNotNull { value ->
        when (value.trim().uppercase()) {
            "A", "姝ｇ‘", "瀵", "鏄", "TRUE", "T", "鈭", "鉁", "鉁", "鈽" -> "A"
            "B", "閿欒", "閿", "鍚", "FALSE", "F", "脳", "X", "鉁", "鉂" -> "B"
            else -> value.trim().takeIf { it.isNotBlank() }
        }
    }
}

private fun defaultJudgeOptions(): List<Option> = listOf(
    Option("A", "姝ｇ‘"),
    Option("B", "閿欒")
)

private fun nextOptionKey(options: List<Option>): String {
    val used = options.map { it.key.uppercase() }.toSet()
    return ('A'..'H').firstOrNull { it.toString() !in used }?.toString() ?: "${options.size + 1}"
}

private fun parseReviewAnswer(text: String, type: QuestionType): List<String> {
    val clean = text.trim()
    if (clean.isBlank()) return emptyList()

    if (type == QuestionType.BLANK || type == QuestionType.SHORT) return listOf(clean)

    if (type == QuestionType.JUDGE) {
        normalizeJudgeAnswer(listOf(clean)).takeIf { it.isNotEmpty() }?.let { return it }
    }

    val compactLetters = clean.uppercase().replace(Regex("[\\s,锛屮€?锛?]+"), "")
    if (compactLetters.matches(Regex("^[A-H]{1,8}$"))) {
        return compactLetters.map { it.toString() }.distinct()
    }

    return clean
        .replace("锛", ",")
        .replace("銆", ",")
        .replace("/", ",")
        .replace("锛", ",")
        .replace(";", ",")
        .split(Regex("[\\s,]+"))
        .map { token -> token.trim() }
        .filter { it.isNotBlank() }
        .flatMap { token ->
            if (token.uppercase().matches(Regex("^[A-H]{2,8}$"))) {
                token.uppercase().map { it.toString() }
            } else {
                normalizeJudgeAnswer(listOf(token)).ifEmpty { listOf(token.uppercase()) }
            }
        }
        .distinct()
}

private fun answerInputText(question: Question): String {
    if (question.type == QuestionType.JUDGE && question.answer.size == 1) {
        return when (question.answer.first().trim().uppercase()) {
            "A", "姝ｇ‘", "瀵", "鏄", "TRUE", "T", "鈭", "鉁", "鉁", "鈽" -> "姝ｇ‘"
            "B", "閿欒", "閿", "鍚", "FALSE", "F", "脳", "X", "鉁", "鉂" -> "閿欒"
            else -> question.answer.first()
        }
    }
    return question.answer.joinToString(",")
}

private fun answerDisplayText(question: Question): String {
    if (MultiBlankSupport.hasStructuredAnswers(question)) {
        return MultiBlankSupport.expectedAnswerText(question.blankAnswers)
    }
    val value = answerInputText(question)
    return value.ifBlank { "鏈瘑鍒瓟妗" }
}

private fun typeLabel(type: QuestionType): String = when (type) {
    QuestionType.SINGLE -> "鍗曢€夐"
    QuestionType.MULTIPLE -> "澶氶€夐"
    QuestionType.JUDGE -> "鍒ゆ柇棰"
    QuestionType.BLANK -> "濉┖棰"
    QuestionType.SHORT -> "绠€绛旈"
}