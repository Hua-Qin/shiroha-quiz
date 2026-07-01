package com.yiqiu.shirohaquiz.ui.screens

import com.yiqiu.shirohaquiz.ui.theme.shirohaEditorialBackground

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.ShirohaMotion
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TextSnippet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.ai.AiSingleQuestionAnalysis
import com.yiqiu.shirohaquiz.ai.ShirohaAiClient
import com.yiqiu.shirohaquiz.importer.model.MultiBlankSupport
import com.yiqiu.shirohaquiz.importer.model.Option
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.QuestionCheckResult
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.AiAnalysisFillPanel
import com.yiqiu.shirohaquiz.ui.components.EditorialFigure
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.MultiBlankAnswerEditor
import com.yiqiu.shirohaquiz.ui.components.MultiBlankAnswerInputs
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.QuizOptionCard
import com.yiqiu.shirohaquiz.ui.components.QuizOptionResultStyle
import com.yiqiu.shirohaquiz.ui.components.QuestionImagesBlock
import com.yiqiu.shirohaquiz.ui.components.QuizSessionExitIconButton
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.editorialScaleFor
import com.yiqiu.shirohaquiz.ui.theme.screenClassFor
import com.yiqiu.shirohaquiz.ui.text.LatexDisplayFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeScreen(
    onGoExam: () -> Unit = {},
    onOpenRecords: () -> Unit = {},
    onOpenQuickEdit: () -> Unit = {}
) {
    val context = LocalContext.current
    val practiceScopeKey = QuizRepository.currentPracticeScopeKey()
    val practiceScopeLabel = QuizRepository.currentPracticeScopeLabel()
    val practiceScopeSummary = QuizRepository.currentPracticeScopeSummary()
    val autoNextScope = rememberCoroutineScope()
    val practiceQuestions = QuizRepository.activePracticeQuestions()
    val question = QuizRepository.currentPracticeQuestion()
    val result = QuizRepository.practiceLastResult
    val isReciteMode = QuizRepository.practiceReciteModeEnabled
    val slashedVersion = QuizRepository.slashedQuestions.joinToString("|") { "${it.bankId}:${it.questionKey}" }
    val practiceCandidateQuestions = remember(practiceScopeKey, QuizRepository.banks.toList(), slashedVersion) {
        QuizRepository.activePracticePoolQuestions()
    }
    val availableCounts = remember(practiceCandidateQuestions) {
        QuizRepository.questionTypeCounts(practiceCandidateQuestions)
    }
    val availableTypes = practiceTypeOrder.filter { (availableCounts[it] ?: 0) > 0 }
    val defaultPracticeTypes = remember(availableTypes) {
        availableTypes
            .filter { it in QuizRepository.objectiveQuestionTypes() }
            .toSet()
            .ifEmpty { availableTypes.toSet() }
            .ifEmpty { QuizRepository.objectiveQuestionTypes() }
    }
    val rememberedPracticeTypes = remember(
        practiceScopeKey,
        availableTypes,
        QuizRepository.rememberPracticeSettingsEnabled
    ) {
        if (QuizRepository.rememberPracticeSettingsEnabled) {
            QuizRepository.preferredPracticeTypes().filter { it in availableTypes }.toSet()
        } else {
            emptySet()
        }
    }
    val initialPracticeTypes = rememberedPracticeTypes.ifEmpty { defaultPracticeTypes }
    val initialAvailableCount = remember(practiceScopeKey, practiceCandidateQuestions, initialPracticeTypes) {
        practiceCandidateQuestions.count { it.type in initialPracticeTypes }
    }
    val initialQuestionCountMode = remember(
        practiceScopeKey,
        initialAvailableCount,
        QuizRepository.rememberPracticeSettingsEnabled,
        QuizRepository.preferredPracticeQuestionCountMode
    ) {
        if (QuizRepository.rememberPracticeSettingsEnabled) {
            normalizeVisiblePracticeCountMode(QuizRepository.preferredPracticeQuestionCountMode, initialAvailableCount)
        } else {
            "custom"
        }
    }
    var selectedQuestionCount by remember(practiceScopeKey, initialAvailableCount, initialQuestionCountMode) {
        mutableIntStateOf(
            resolvePracticeQuestionCount(
                mode = initialQuestionCountMode,
                customCount = QuizRepository.preferredPracticeCustomQuestionCount,
                availableCount = initialAvailableCount
            )
        )
    }
    var selectedQuestionCountMode by remember(practiceScopeKey, initialQuestionCountMode) { mutableStateOf(initialQuestionCountMode) }
    var selectedTypes by remember(practiceScopeKey, initialPracticeTypes) { mutableStateOf(initialPracticeTypes) }
    var practiceOrderMode by rememberSaveable(practiceScopeKey) {
        mutableStateOf(
            if (QuizRepository.rememberPracticeSettingsEnabled) {
                QuizRepository.preferredPracticeOrderMode
            } else {
                QuizRepository.PRACTICE_ORDER_RANDOM
            }
        )
    }
    var sequentialStartMode by rememberSaveable(practiceScopeKey) {
        mutableStateOf(QuizRepository.SEQUENTIAL_START_LAST)
    }
    var sequentialCustomStartNumber by rememberSaveable(practiceScopeKey) {
        mutableIntStateOf(1)
    }
    var selectedPracticeMode by rememberSaveable(practiceScopeKey, QuizRepository.preferredPracticeMode) {
        mutableStateOf(QuizRepository.preferredPracticeMode)
    }
    var selectedBatchGroupSizeMode by rememberSaveable(practiceScopeKey, QuizRepository.preferredPracticeBatchSizeMode) {
        mutableStateOf(QuizRepository.preferredPracticeBatchSizeMode)
    }
    var selectedBatchGroupSize by remember(practiceScopeKey, selectedBatchGroupSizeMode, selectedQuestionCount) {
        mutableIntStateOf(
            QuizRepository.resolvePracticeBatchGroupSize(
                mode = selectedBatchGroupSizeMode,
                customSize = QuizRepository.preferredPracticeBatchCustomSize
            ).coerceIn(1, selectedQuestionCount.coerceAtLeast(1))
        )
    }

    val effectiveSelectedTypes = selectedTypes.ifEmpty { defaultPracticeTypes }
    val selectedAvailable = remember(availableCounts, effectiveSelectedTypes) {
        availableCounts.entries.sumOf { (type, count) -> if (type in effectiveSelectedTypes) count else 0 }
    }
    val sequentialProgressSnapshot = QuizRepository.practiceSequentialProgress[practiceScopeKey] ?: 0
    val sequentialProgressStartNumber = remember(
        practiceScopeKey,
        selectedTypes,
        selectedAvailable,
        sequentialProgressSnapshot
    ) {
        QuizRepository.sequentialPracticeProgressIndex(null, effectiveSelectedTypes) + 1
    }
    val sequentialRangePreview = remember(
        practiceScopeKey,
        selectedQuestionCount,
        selectedTypes,
        sequentialStartMode,
        sequentialCustomStartNumber,
        selectedAvailable,
        sequentialProgressSnapshot
    ) {
        QuizRepository.sequentialPracticeRangePreview(
            questionCount = selectedQuestionCount,
            allowedTypes = effectiveSelectedTypes,
            startMode = sequentialStartMode,
            customStartNumber = sequentialCustomStartNumber,
            bank = null
        )
    }
    val sequentialRangeText = sequentialRangePreview?.let { (start, end) ->
        if (start == end) "鏈鑼冨洿锛氱 ${start} 棰" else "鏈鑼冨洿锛氱 ${start} - ${end} 棰"
    }
    val startPracticeWithSettings = {
        val safeTypes = selectedTypes.ifEmpty { QuizRepository.objectiveQuestionTypes() }
        val available = QuizRepository.activePracticePoolQuestions().count { it.type in safeTypes }
        if (available > 0) {
            val count = selectedQuestionCount.coerceIn(1, available)
            QuizRepository.rememberPracticeSettings(
                context = context,
                questionCountMode = selectedQuestionCountMode,
                customQuestionCount = if (selectedQuestionCountMode == "custom") count else null,
                orderMode = practiceOrderMode,
                types = safeTypes,
                practiceMode = selectedPracticeMode,
                batchSizeMode = selectedBatchGroupSizeMode,
                customBatchSize = if (selectedBatchGroupSizeMode == "custom") selectedBatchGroupSize.coerceIn(1, count) else null
            )
            if (practiceOrderMode == QuizRepository.PRACTICE_ORDER_SEQUENTIAL) {
                QuizRepository.startSequentialPracticeSession(
                    questionCount = count,
                    allowedTypes = safeTypes,
                    startMode = sequentialStartMode,
                    customStartNumber = sequentialCustomStartNumber,
                    practiceMode = if (QuizRepository.practiceReciteModeEnabled) QuizRepository.PRACTICE_MODE_INSTANT else selectedPracticeMode,
                    batchGroupSize = selectedBatchGroupSize.coerceIn(1, count)
                )
            } else {
                QuizRepository.startPracticeSession(
                    questionCount = count,
                    allowedTypes = safeTypes,
                    sourceLabel = practiceScopeLabel,
                    randomize = true,
                    practiceMode = if (QuizRepository.practiceReciteModeEnabled) QuizRepository.PRACTICE_MODE_INSTANT else selectedPracticeMode,
                    batchGroupSize = selectedBatchGroupSize.coerceIn(1, count)
                )
            }
        }
    }

    val isPracticeRunning = QuizRepository.practiceQuestions.isNotEmpty()
    var isPracticeProgressExpanded by rememberSaveable { mutableStateOf(true) }
    val practiceAnsweredCount = QuizRepository.practiceAnsweredCount()
    val practiceAutoScoredAnsweredCount = QuizRepository.practiceAutoScoredAnsweredCount()
    val practiceCorrectCount = QuizRepository.practiceCorrectCount()
    val practiceAccuracy = if (practiceAutoScoredAnsweredCount == 0) 0 else practiceCorrectCount * 100 / practiceAutoScoredAnsweredCount

    val screenScrollState = rememberScrollState()
    LaunchedEffect(isPracticeRunning, practiceScopeKey) {
        if (!isPracticeRunning) {
            screenScrollState.scrollTo(0)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shirohaEditorialBackground()
    ) {
        val scale = editorialScaleFor(screenClassFor(maxWidth))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(screenScrollState)
                .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                ShirohaHeader(
                    kicker = "Practice",
                    title = "缁冧範妯″紡",
                    subtitle = if (isPracticeRunning) "姝ｅ湪鎸夊綋鍓嶈缃户缁埛棰樸€" else "閫夋嫨鑼冨洿涓庤妭濂忥紝寮€濮嬩竴杞笓娉ㄧ殑缁冧範銆",
                    scale = scale,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (isPracticeRunning && !isPracticeProgressExpanded) {
                        PracticeAccuracyCapsule(
                            accuracy = practiceAccuracy,
                            modifier = Modifier.height(34.dp),
                            onClick = { isPracticeProgressExpanded = true }
                        )
                    }
                    ActionPillButton(
                        icon = Icons.Rounded.Timer,
                        text = "鍒囨崲鑰冭瘯",
                        primary = false,
                        modifier = Modifier.height(44.dp),
                        onClick = onGoExam
                    )
                }
            }

            if (!isPracticeRunning) {
                CompactPracticeSetupHero()
            }

            if (isPracticeRunning) {
                PracticeEditorialFiguresRow(
                    scale = scale,
                    practiceQuestionsSize = practiceQuestions.size,
                    practiceAutoScoredAnsweredCount = practiceAutoScoredAnsweredCount,
                    practiceCorrectCount = practiceCorrectCount,
                    practiceAccuracy = practiceAccuracy
                )
            }

        if (practiceCandidateQuestions.isEmpty()) {
            GlassCard {
                NoticeCard("杩樻病鏈夊彲缁冧範棰樼洰銆傝鍏堝湪瀵煎叆椤靛垱寤洪搴撱€")
            }
            return
        }

        if (QuizRepository.practiceQuestions.isEmpty()) {
            PracticeSetupPanel(
                bankName = practiceScopeLabel,
                scopeSummary = practiceScopeSummary,
                totalQuestions = practiceCandidateQuestions.size,
                availableCounts = availableCounts,
                selectedTypes = effectiveSelectedTypes,
                selectedQuestionCount = selectedQuestionCount.coerceAtMost(selectedAvailable.coerceAtLeast(1)),
                selectedQuestionCountMode = selectedQuestionCountMode,
                practiceOrderMode = practiceOrderMode,
                sequentialStartMode = sequentialStartMode,
                sequentialProgressStartNumber = sequentialProgressStartNumber,
                sequentialCustomStartNumber = sequentialCustomStartNumber,
                sequentialRangeText = sequentialRangeText,
                selectedPracticeMode = selectedPracticeMode,
                selectedBatchGroupSize = selectedBatchGroupSize.coerceIn(1, selectedQuestionCount.coerceAtLeast(1)),
                selectedBatchGroupSizeMode = selectedBatchGroupSizeMode,
                showInlineAnswerSettings = QuizRepository.practiceInlineAnswerSettingsEnabled,
                onSelectPracticeMode = { mode ->
                    selectedPracticeMode = mode
                    QuizRepository.rememberPracticeSettings(context, practiceMode = mode)
                },
                onSelectPracticeOrderMode = { mode ->
                    practiceOrderMode = mode
                    QuizRepository.rememberPracticeSettings(context, orderMode = mode)
                },
                onSelectSequentialStartMode = { mode ->
                    sequentialStartMode = mode
                },
                onSelectSequentialCustomStart = { startNumber ->
                    sequentialCustomStartNumber = startNumber.coerceAtLeast(1)
                    sequentialStartMode = QuizRepository.SEQUENTIAL_START_CUSTOM
                },
                onToggleType = { type ->
                    val currentTypes = selectedTypes.ifEmpty { defaultPracticeTypes }
                    val updated = if (currentTypes.contains(type)) {
                        if (currentTypes.size <= 1) currentTypes else currentTypes - type
                    } else {
                        currentTypes + type
                    }
                    selectedTypes = updated
                    val newAvailable = availableCounts.entries.sumOf { (itemType, count) -> if (itemType in updated) count else 0 }
                    var savedCountMode = selectedQuestionCountMode
                    var savedCustomCount: Int? = null
                    if (newAvailable > 0) {
                        val boundedCount = selectedQuestionCount.coerceAtMost(newAvailable)
                        if (boundedCount != selectedQuestionCount) {
                            selectedQuestionCount = boundedCount
                            selectedQuestionCountMode = "custom"
                            savedCountMode = "custom"
                            savedCustomCount = boundedCount
                        }
                    }
                    QuizRepository.rememberPracticeSettings(
                        context = context,
                        questionCountMode = savedCountMode,
                        customQuestionCount = savedCustomCount,
                        types = updated
                    )
                },
                onSelectQuestionCount = { count, mode ->
                    selectedQuestionCount = count
                    selectedQuestionCountMode = mode
                    if (selectedBatchGroupSize > count) {
                        selectedBatchGroupSize = count.coerceAtLeast(1)
                        selectedBatchGroupSizeMode = "custom"
                    }
                    QuizRepository.rememberPracticeSettings(
                        context = context,
                        questionCountMode = mode,
                        customQuestionCount = if (mode == "custom") count else null,
                        batchSizeMode = selectedBatchGroupSizeMode,
                        customBatchSize = if (selectedBatchGroupSizeMode == "custom") selectedBatchGroupSize else null
                    )
                },
                onSelectBatchGroupSize = { count, mode ->
                    selectedBatchGroupSize = count.coerceIn(1, selectedQuestionCount.coerceAtLeast(1))
                    selectedBatchGroupSizeMode = mode
                    QuizRepository.rememberPracticeSettings(
                        context = context,
                        batchSizeMode = mode,
                        customBatchSize = if (mode == "custom") selectedBatchGroupSize else null
                    )
                },
                onStartPractice = startPracticeWithSettings
            )
            return
        }

        if (question == null) {
            GlassCard { NoticeCard("褰撳墠缁冧範娌℃湁鍙樉绀虹殑棰樼洰锛岃閲嶆柊寮€濮嬬粌涔犮€") }
            return
        }

        val currentSessionKey = QuizRepository.currentPracticeSessionKey().orEmpty()
        val savedResult = QuizRepository.practiceAnswerResults[currentSessionKey]
        val effectiveResult = result ?: savedResult?.let { saved ->
            QuestionCheckResult(
                question = question,
                userAnswer = saved.userAnswer,
                userBlankAnswers = saved.userBlankAnswers,
                correct = saved.correct,
                answerText = saved.answerText,
                autoScored = saved.autoScored
            )
        }
        val isSubmitted = effectiveResult != null
        val isBatchPractice = QuizRepository.practiceMode == QuizRepository.PRACTICE_MODE_BATCH
        val isBatchSubmitted = isBatchPractice && QuizRepository.practiceBatchSubmitted
        val isBatchBeforeSubmit = isBatchPractice && !QuizRepository.practiceBatchSubmitted
        val batchGroupStart = if (isBatchPractice) QuizRepository.practiceCurrentBatchStartIndex() else 0
        val batchGroupEnd = if (isBatchPractice) QuizRepository.practiceCurrentBatchEndIndex() else practiceQuestions.lastIndex
        val batchGroupIndexes = if (isBatchPractice) QuizRepository.practiceCurrentBatchIndexes() else practiceQuestions.indices.toList()
        val batchGroupTotal = if (isBatchPractice) QuizRepository.practiceCurrentBatchTotal() else practiceQuestions.size
        val batchGroupNumber = if (isBatchPractice) QuizRepository.practiceBatchGroupNumber() else 0
        val batchGroupCount = if (isBatchPractice) QuizRepository.practiceBatchGroupCount() else 0
        val canGoNext = if (isReciteMode) {
            if (isBatchPractice) QuizRepository.practiceIndex < batchGroupEnd else QuizRepository.practiceIndex < practiceQuestions.lastIndex
        } else if (isBatchPractice) {
            QuizRepository.practiceIndex < batchGroupEnd
        } else {
            QuizRepository.practiceIndex < practiceQuestions.lastIndex &&
                (isBatchBeforeSubmit || !QuizRepository.practiceNextRequiresResult || isSubmitted)
        }
        val displayedSelection = if (isReciteMode) emptyList() else effectiveResult?.userAnswer ?: QuizRepository.selectedAnswer
        val displayOptions = remember(
            currentSessionKey,
            question.options,
            QuizRepository.practiceOptionShuffleEnabled,
            QuizRepository.practiceOptionShuffleSeed
        ) {
            practiceDisplayOptions(question, QuizRepository.practiceOptionShuffleEnabled, QuizRepository.practiceOptionShuffleSeed, currentSessionKey)
        }
        val displayAnswerMap = remember(displayOptions) {
            displayOptions.associate { option -> option.originalKey.trim().uppercase() to option.displayKey }
        }
        val isCurrentQuestionFavorited = QuizRepository.isCurrentPracticeQuestionFavorited()
        val batchDraftAnsweredCount = QuizRepository.practiceDraftAnsweredCount()
        var showBatchSubmitConfirm by rememberSaveable(practiceScopeKey) { mutableStateOf(false) }
        var showExitPracticeConfirm by rememberSaveable(practiceScopeKey) { mutableStateOf(false) }
        var showBatchAnswerSheet by rememberSaveable(practiceScopeKey) { mutableStateOf(false) }
        var showUnsubmittedCompleteConfirm by rememberSaveable(practiceScopeKey) { mutableStateOf(false) }
        var isUnsubmittedReviewMode by rememberSaveable(practiceScopeKey) { mutableStateOf(false) }

        BackHandler(
            enabled = !showExitPracticeConfirm && !showBatchSubmitConfirm && !showBatchAnswerSheet && !showUnsubmittedCompleteConfirm
        ) {
            showExitPracticeConfirm = true
        }

        var batchReviewWrongOnly by rememberSaveable(practiceScopeKey) { mutableStateOf(false) }
        val batchWrongIndexes = if (isBatchSubmitted) QuizRepository.practiceWrongQuestionIndexes() else emptyList()
        if (!isBatchSubmitted && batchReviewWrongOnly) batchReviewWrongOnly = false
        if (batchReviewWrongOnly && batchWrongIndexes.isEmpty()) batchReviewWrongOnly = false
        val batchReviewIndexes = if (isBatchSubmitted && batchReviewWrongOnly) batchWrongIndexes else if (isBatchPractice) batchGroupIndexes else practiceQuestions.indices.toList()
        val currentReviewPosition = batchReviewIndexes.indexOf(QuizRepository.practiceIndex)
        val goPreviousPractice = {
            if (isBatchSubmitted && batchReviewWrongOnly) {
                if (currentReviewPosition > 0) QuizRepository.goToPracticeQuestion(batchReviewIndexes[currentReviewPosition - 1])
            } else {
                QuizRepository.previousQuestion()
            }
        }
        val goNextPractice = {
            if (isBatchSubmitted && batchReviewWrongOnly) {
                if (currentReviewPosition >= 0 && currentReviewPosition < batchReviewIndexes.lastIndex) {
                    QuizRepository.goToPracticeQuestion(batchReviewIndexes[currentReviewPosition + 1])
                }
            } else {
                QuizRepository.nextQuestion()
            }
        }
        val canGoPreviousPractice = if (isBatchSubmitted && batchReviewWrongOnly) currentReviewPosition > 0 else if (isBatchPractice) QuizRepository.practiceIndex > batchGroupStart else QuizRepository.practiceIndex > 0
        val canGoNextPractice = if (isBatchSubmitted && batchReviewWrongOnly) {
            currentReviewPosition >= 0 && currentReviewPosition < batchReviewIndexes.lastIndex
        } else {
            canGoNext
        }
        val canStartNextBatchGroup = isBatchSubmitted && QuizRepository.canStartNextPracticeBatchGroup()
        val unsubmittedIndexes = if (!isBatchPractice && !isReciteMode) QuizRepository.practiceUnsubmittedQuestionIndexes() else emptyList()
        val unsubmittedCount = unsubmittedIndexes.size
        val isAtLastInstantQuestion = !isBatchPractice && !isReciteMode && QuizRepository.practiceIndex >= practiceQuestions.lastIndex
        val shouldOfferUnsubmittedCompletion = isAtLastInstantQuestion && unsubmittedCount > 0
        val isResolvingUnsubmitted = isUnsubmittedReviewMode && unsubmittedCount > 0
        val firstUnsubmittedIndex = unsubmittedIndexes.firstOrNull { it != QuizRepository.practiceIndex }
            ?: unsubmittedIndexes.firstOrNull()
        val nextUnsubmittedIndex = unsubmittedIndexes.firstOrNull { it > QuizRepository.practiceIndex }
            ?: unsubmittedIndexes.firstOrNull { it < QuizRepository.practiceIndex }
        val startUnsubmittedReview = {
            isUnsubmittedReviewMode = true
            firstUnsubmittedIndex?.let(QuizRepository::goToPracticeQuestion)
        }
        val goToNextUnsubmittedQuestion = {
            nextUnsubmittedIndex?.let(QuizRepository::goToPracticeQuestion)
        }
        val scheduleInstantAutoNextAfterSubmit: (String, Int, Boolean) -> Unit = { autoNextQuestionId, autoNextIndex, correct ->
            if (correct &&
                !isBatchPractice &&
                !isReciteMode &&
                QuizRepository.practiceAutoNextEnabled &&
                autoNextIndex < practiceQuestions.lastIndex
            ) {
                autoNextScope.launch {
                    delay(320)
                    if (QuizRepository.practiceIndex == autoNextIndex &&
                        QuizRepository.currentPracticeSessionKey() == autoNextQuestionId
                    ) {
                        QuizRepository.nextQuestion()
                    }
                }
            }
        }
        val scheduleBatchAutoNextAfterSelect: (String, Int) -> Unit = { autoNextQuestionId, autoNextIndex ->
            if (isBatchBeforeSubmit &&
                !isReciteMode &&
                QuizRepository.practiceBatchAutoNextEnabled &&
                autoNextIndex < batchGroupEnd
            ) {
                autoNextScope.launch {
                    delay(180)
                    if (QuizRepository.practiceIndex == autoNextIndex &&
                        QuizRepository.currentPracticeSessionKey() == autoNextQuestionId
                    ) {
                        QuizRepository.nextQuestion()
                    }
                }
            }
        }
        val submitCurrentPracticeQuestion = {
            val autoNextQuestionId = currentSessionKey
            val autoNextIndex = QuizRepository.practiceIndex
            val wasResolvingUnsubmitted = isResolvingUnsubmitted
            val submitted = QuizRepository.submitPracticeQuestion()
            if (submitted != null) {
                if (wasResolvingUnsubmitted) {
                    if (QuizRepository.practiceUnsubmittedQuestionIndexes().isEmpty()) {
                        isUnsubmittedReviewMode = false
                        isPracticeProgressExpanded = true
                        autoNextScope.launch { screenScrollState.scrollTo(0) }
                    }
                } else {
                    scheduleInstantAutoNextAfterSubmit(autoNextQuestionId, autoNextIndex, submitted.correct)
                }
            }
            submitted
        }
        val isPracticeComplete = !isReciteMode &&
            practiceQuestions.isNotEmpty() &&
            if (isBatchPractice) QuizRepository.isAllPracticeBatchGroupsSubmitted() else QuizRepository.practiceAnsweredCount() >= practiceQuestions.size
        val canShowSingleQuestionAiAnalysis = QuizRepository.aiSingleQuestionAnalysisEnabled && (isReciteMode || effectiveResult != null)
        var singleQuestionAiAnalysis by remember(currentSessionKey) { mutableStateOf<AiSingleQuestionAnalysis?>(null) }
        var singleQuestionAiError by remember(currentSessionKey) { mutableStateOf<String?>(null) }
        var isSingleQuestionAiLoading by remember(currentSessionKey) { mutableStateOf(false) }
        val runSingleQuestionAiAnalysis = {
            if (!QuizRepository.isAiConfigured()) {
                singleQuestionAiError = "璇峰厛鍦?鎴戠殑 鈫?AI 璁剧疆 涓～鍐$API 鍦板潃銆丄PI Key 鍜屾ā鍨嬪悕绉般€"
                singleQuestionAiAnalysis = null
            } else if (!isSingleQuestionAiLoading) {
                isSingleQuestionAiLoading = true
                singleQuestionAiError = null
                autoNextScope.launch {
                    val requestUserAnswer = effectiveResult?.userAnswer ?: displayedSelection
                    val aiQuestion = practiceQuestionForDisplay(question, displayOptions)
                    val aiUserAnswer = practiceAnswersForDisplay(requestUserAnswer, displayAnswerMap)
                    val result = runCatching {
                        withContext(Dispatchers.IO) {
                            ShirohaAiClient.analyzeSingleQuestion(
                                apiBaseUrl = QuizRepository.aiApiBaseUrl,
                                apiKey = QuizRepository.aiApiKey,
                                modelName = QuizRepository.aiModelName,
                                question = aiQuestion,
                                userAnswer = aiUserAnswer,
                                timeoutSeconds = QuizRepository.aiTimeoutSeconds
                            )
                        }
                    }
                    result.onSuccess { analysis ->
                        singleQuestionAiAnalysis = analysis
                        singleQuestionAiError = null
                    }.onFailure { error ->
                        singleQuestionAiAnalysis = null
                        singleQuestionAiError = error.message ?: "AI 鍒嗘瀽澶辫触锛岃妫€鏌ユ帴鍙ｉ厤缃垨缃戠粶銆"
                    }
                    isSingleQuestionAiLoading = false
                }
            }
        }

        val questionCardModifier = if (QuizRepository.swipeNavigationEnabled) {
            Modifier.questionSwipeNavigation(
                onSwipeLeft = {
                    when {
                        isResolvingUnsubmitted && nextUnsubmittedIndex != null -> goToNextUnsubmittedQuestion()
                        canGoNextPractice -> goNextPractice()
                        shouldOfferUnsubmittedCompletion -> startUnsubmittedReview()
                    }
                },
                onSwipeRight = { if (canGoPreviousPractice) goPreviousPractice() }
            )
        } else {
            Modifier
        }

        Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)) {
            if (isPracticeProgressExpanded) {
                PracticeProgressCard(
                    total = if (isBatchPractice) batchGroupTotal else practiceQuestions.size,
                    answered = if (isBatchBeforeSubmit) batchDraftAnsweredCount else if (isBatchSubmitted) QuizRepository.practiceCurrentBatchSubmittedCount() else practiceAnsweredCount,
                    scoredAnswered = if (isBatchSubmitted) QuizRepository.practiceCurrentBatchAutoScoredSubmittedCount() else practiceAutoScoredAnsweredCount,
                    correct = if (isBatchSubmitted) QuizRepository.practiceCurrentBatchCorrectCount() else practiceCorrectCount,
                    batchBeforeSubmit = isBatchBeforeSubmit,
                    batchSubmitted = isBatchSubmitted,
                    batchGroupNumber = batchGroupNumber,
                    batchGroupCount = batchGroupCount,
                    wrongCount = batchWrongIndexes.size,
                    wrongOnly = batchReviewWrongOnly,
                    expanded = true,
                    reciteMode = isReciteMode,
                    reciteIndex = QuizRepository.practiceIndex + 1,
                    onOpenAnswerSheet = if (isBatchPractice && !isReciteMode) { { showBatchAnswerSheet = true } } else null,
                    onToggleWrongOnly = if (isBatchSubmitted && !isReciteMode) {
                        {
                            if (batchReviewWrongOnly) {
                                batchReviewWrongOnly = false
                            } else if (batchWrongIndexes.isNotEmpty()) {
                                batchReviewWrongOnly = true
                                if (QuizRepository.practiceIndex !in batchWrongIndexes) {
                                    QuizRepository.goToPracticeQuestion(batchWrongIndexes.first())
                                }
                            }
                        }
                    } else null,
                    onToggle = { isPracticeProgressExpanded = false }
                )
            }

            if (isPracticeComplete) {
                PracticeCompletionCard(
                    total = practiceQuestions.size,
                    answered = QuizRepository.practiceAnsweredCount(),
                    scoredAnswered = QuizRepository.practiceAutoScoredAnsweredCount(),
                    correct = QuizRepository.practiceCorrectCount(),
                    onRestart = {
                        QuizRepository.completePracticeSession()
                        startPracticeWithSettings()
                    },
                    onOpenRecords = {
                        QuizRepository.completePracticeSession()
                        onOpenRecords()
                    },
                    onExit = { QuizRepository.completePracticeSession() }
                )
            }

            GlassCard(modifier = questionCardModifier) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CompactPracticeChip(
                        if (isBatchPractice) "绗?$batchGroupNumber 缁?路 ${QuizRepository.practiceIndex - batchGroupStart + 1} / $batchGroupTotal 棰" else "绗?${QuizRepository.practiceIndex + 1} / ${practiceQuestions.size} 棰",
                        selected = true
                    )
                    CompactPracticeChip(typeLabel(question.type))
                    if (isReciteMode) CompactPracticeChip("鑳岄妯″紡", selected = true)
                    if (isBatchSubmitted && batchReviewWrongOnly) CompactPracticeChip("鍙湅閿欓", selected = true)
                }
                FavoriteQuestionIconButton(
                    favorited = isCurrentQuestionFavorited,
                    onClick = { QuizRepository.toggleCurrentPracticeFavorite(context) }
                )
                if (QuizRepository.practiceQuickEditEnabled) {
                    QuickEditQuestionIconButton(onClick = onOpenQuickEdit)
                }
                if (QuizRepository.canSlashCurrentPracticeQuestion()) {
                    SlashQuestionRoundButton(
                        onClick = { QuizRepository.slashCurrentPracticeQuestion(context) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = LatexDisplayFormatter.format(question.question),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = QuizRepository.questionFontSizeSp().sp,
                    lineHeight = QuizRepository.questionLineHeightSp().sp
                ),
                fontWeight = FontWeight.SemiBold
            )
            if (question.images.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                QuestionImagesBlock(question.images, maxPreviewHeight = 360.dp, showMeta = true)
            }
            Spacer(Modifier.height(18.dp))

            when (question.type) {
                QuestionType.SINGLE,
                QuestionType.MULTIPLE,
                QuestionType.JUDGE -> {
                    displayOptions.forEach { option ->
                        QuizOptionCard(
                            label = option.displayKey,
                            text = option.text,
                            selected = displayedSelection.any { it.trim().equals(option.originalKey, ignoreCase = true) },
                            resultStyle = practiceOptionResultStyle(
                                optionKey = option.originalKey,
                                correctAnswers = question.answer,
                                result = effectiveResult,
                                revealAnswer = isReciteMode
                            ),
                            onClick = {
                                if (!isSubmitted && !isReciteMode) {
                                    val isInstantAutoSubmitQuestion = question.type == QuestionType.SINGLE || question.type == QuestionType.JUDGE
                                    val shouldAutoSubmitInstant = !isBatchPractice &&
                                        QuizRepository.practiceAutoSubmitEnabled &&
                                        isInstantAutoSubmitQuestion
                                    val shouldBatchAutoNext = isBatchBeforeSubmit &&
                                        QuizRepository.practiceBatchAutoNextEnabled &&
                                        isInstantAutoSubmitQuestion
                                    QuizRepository.toggleAnswer(
                                        key = option.originalKey,
                                        multiple = question.type == QuestionType.MULTIPLE
                                    )
                                    if (shouldAutoSubmitInstant) {
                                        submitCurrentPracticeQuestion()
                                    } else if (shouldBatchAutoNext) {
                                        scheduleBatchAutoNextAfterSelect(currentSessionKey, QuizRepository.practiceIndex)
                                    }
                                }
                            }
                        )
                        Spacer(Modifier.height(if (QuizRepository.compactOptionsEnabled) 8.dp else 10.dp))
                    }
                }

                QuestionType.BLANK -> {
                    if (isReciteMode) {
                        NoticeCard("鑳岄妯″紡涓嬬洿鎺ユ煡鐪嬪弬鑰冪瓟妗堝拰瑙ｆ瀽銆")
                    } else if (MultiBlankSupport.hasStructuredAnswers(question)) {
                        MultiBlankAnswerInputs(
                            blankCount = question.blankAnswers.size,
                            values = displayedSelection,
                            enabled = !isSubmitted && !isBatchSubmitted,
                            onValueChange = QuizRepository::updatePracticeBlankAnswer
                        )
                    } else {
                        SubjectiveAnswerEditor(
                            type = question.type,
                            value = displayedSelection.firstOrNull().orEmpty(),
                            enabled = !isSubmitted && !isBatchSubmitted,
                            onValueChange = { QuizRepository.updatePracticeTextAnswer(it) }
                        )
                    }
                }

                QuestionType.SHORT -> {
                    if (isReciteMode) {
                        NoticeCard("鑳岄妯″紡涓嬬洿鎺ユ煡鐪嬪弬鑰冪瓟妗堝拰瑙ｆ瀽銆")
                    } else {
                        SubjectiveAnswerEditor(
                            type = question.type,
                            value = displayedSelection.firstOrNull().orEmpty(),
                            enabled = !isSubmitted && !isBatchSubmitted,
                            onValueChange = { QuizRepository.updatePracticeTextAnswer(it) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            if (!isReciteMode && isBatchBeforeSubmit) {
                ActionPillButton(
                    Icons.Rounded.CheckCircle,
                    "鎻愪氦鏈粍",
                    primary = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    fillWidthContent = true,
                    onClick = {
                        if (batchDraftAnsweredCount < batchGroupTotal) {
                            showBatchSubmitConfirm = true
                        } else {
                            QuizRepository.submitPracticeBatch()
                        }
                    }
                )
            } else if (!isReciteMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionPillButton(
                        Icons.AutoMirrored.Rounded.TextSnippet,
                        "鏌ョ湅瑙ｆ瀽",
                        primary = false,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        fillWidthContent = true,
                        onClick = {
                            if (!isSubmitted) {
                                submitCurrentPracticeQuestion()
                            }
                        }
                    )
                    ActionPillButton(
                        Icons.Rounded.CheckCircle,
                        if (isSubmitted) "宸叉彁浜" else "鎻愪氦绛旀",
                        primary = !isSubmitted,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        fillWidthContent = true,
                        onClick = {
                            if (!isSubmitted) {
                                submitCurrentPracticeQuestion()
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionPillButton(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    "涓婁竴棰",
                    primary = false,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    fillWidthContent = true,
                    enabled = canGoPreviousPractice,
                    onClick = { goPreviousPractice() }
                )
                ActionPillButton(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    when {
                        isResolvingUnsubmitted && nextUnsubmittedIndex != null -> "涓嬩竴閬撴湭鎻愪氦锛$unsubmittedCount锛"
                        isResolvingUnsubmitted -> "褰撳墠棰樻湭鎻愪氦"
                        shouldOfferUnsubmittedCompletion -> "琛ョ瓟鏈彁浜わ紙$unsubmittedCount锛"
                        else -> "涓嬩竴棰"
                    },
                    primary = false,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    fillWidthContent = true,
                    enabled = when {
                        isResolvingUnsubmitted -> nextUnsubmittedIndex != null
                        shouldOfferUnsubmittedCompletion -> firstUnsubmittedIndex != null
                        else -> canGoNextPractice
                    },
                    onClick = {
                        when {
                            isResolvingUnsubmitted -> goToNextUnsubmittedQuestion()
                            shouldOfferUnsubmittedCompletion -> startUnsubmittedReview()
                            else -> goNextPractice()
                        }
                    }
                )
            }

            if (shouldOfferUnsubmittedCompletion || isResolvingUnsubmitted) {
                Spacer(Modifier.height(10.dp))
                ActionPillButton(
                    Icons.Rounded.CheckCircle,
                    "瀹屾垚缁冧範",
                    primary = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    fillWidthContent = true,
                    onClick = { showUnsubmittedCompleteConfirm = true }
                )
            }

            if (isBatchSubmitted) {
                Spacer(Modifier.height(10.dp))
                ActionPillButton(
                    Icons.Rounded.PlayArrow,
                    if (canStartNextBatchGroup) "杩涘叆涓嬩竴缁" else "瀹屾垚缁冧範",
                    primary = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    fillWidthContent = true,
                    onClick = {
                        if (canStartNextBatchGroup) {
                            QuizRepository.startNextPracticeBatchGroup()
                            batchReviewWrongOnly = false
                            isPracticeProgressExpanded = true
                        } else {
                            QuizRepository.completePracticeSession()
                        }
                    }
                )
            }


            if (showUnsubmittedCompleteConfirm) {
                UnsubmittedPracticeConfirmDialog(
                    unansweredCount = unsubmittedCount,
                    onDismiss = { showUnsubmittedCompleteConfirm = false },
                    onReturnToAnswer = {
                        showUnsubmittedCompleteConfirm = false
                        startUnsubmittedReview()
                    },
                    onComplete = {
                        QuizRepository.submitUnansweredPracticeQuestionsAsBlank()
                        showUnsubmittedCompleteConfirm = false
                        isUnsubmittedReviewMode = false
                        isPracticeProgressExpanded = true
                        autoNextScope.launch { screenScrollState.scrollTo(0) }
                    }
                )
            }

            if (showExitPracticeConfirm) {
                PracticeExitConfirmDialog(
                    canSaveProgress = !isPracticeComplete && QuizRepository.canSaveSequentialProgressOnPracticeExit(),
                    onDismiss = { showExitPracticeConfirm = false },
                    onDirectExit = {
                        showExitPracticeConfirm = false
                        if (isPracticeComplete) {
                            QuizRepository.completePracticeSession()
                        } else {
                            QuizRepository.endPracticeSession()
                        }
                    },
                    onSaveAndExit = {
                        showExitPracticeConfirm = false
                        QuizRepository.endPracticeSessionSavingSequentialProgress()
                    }
                )
            }

            if (isReciteMode || effectiveResult != null) {
                val answerText = when (question.type) {
                    QuestionType.SINGLE,
                    QuestionType.MULTIPLE -> practiceAnswersForDisplay(question.answer, displayAnswerMap)
                        .joinToString(" / ")
                        .ifBlank { "鏈瘑鍒瓟妗" }
                    QuestionType.BLANK -> effectiveResult?.answerText ?: if (MultiBlankSupport.hasStructuredAnswers(question)) {
                        MultiBlankSupport.expectedAnswerText(question.blankAnswers)
                    } else {
                        question.answer.joinToString(" / ").ifBlank { "鏈瘑鍒瓟妗" }
                    }
                    else -> effectiveResult?.answerText ?: question.answer.joinToString(" / ").ifBlank { "鏈瘑鍒瓟妗" }
                }
                Spacer(Modifier.height(16.dp))
                if (!isReciteMode && effectiveResult != null) {
                    if (effectiveResult.autoScored) {
                        AnswerResultCapsule(correct = effectiveResult.correct)
                    } else {
                        SubjectiveSubmittedCapsule()
                    }
                    Spacer(Modifier.height(8.dp))
                }
                val answerLabel = if (question.type == QuestionType.SHORT) "鍙傝€冪瓟妗" else "姝ｇ‘绛旀"
                NoticeCard("$answerLabel锛$answerText", warning = false)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "瑙ｆ瀽",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = question.analysis.takeIf { it.isNotBlank() }
                        ?.let(::formatAnalysisForDisplay)
                        ?.let(LatexDisplayFormatter::format)
                        ?: "鏆傛棤瑙ｆ瀽",
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 23.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (canShowSingleQuestionAiAnalysis) {
                    Spacer(Modifier.height(14.dp))
                    SingleQuestionAiAnalysisPanel(
                        analysis = singleQuestionAiAnalysis,
                        error = singleQuestionAiError,
                        loading = isSingleQuestionAiLoading,
                        onAnalyze = runSingleQuestionAiAnalysis
                    )
                }
            }

            if (showBatchSubmitConfirm) {
                BatchSubmitConfirmDialog(
                    unansweredCount = (batchGroupTotal - batchDraftAnsweredCount).coerceAtLeast(0),
                    onDismiss = { showBatchSubmitConfirm = false },
                    onConfirm = {
                        QuizRepository.submitPracticeBatch()
                        showBatchSubmitConfirm = false
                    }
                )
            }

            if (showBatchAnswerSheet) {
                BatchPracticeAnswerSheetDialog(
                    groupNumber = batchGroupNumber,
                    groupCount = batchGroupCount,
                    indexes = batchGroupIndexes,
                    currentIndex = QuizRepository.practiceIndex,
                    submitted = isBatchSubmitted,
                    isAnswered = QuizRepository::isPracticeDraftAnswered,
                    isCorrect = QuizRepository::practiceResultCorrectAt,
                    onJump = { index ->
                        QuizRepository.goToPracticeQuestion(index)
                        showBatchAnswerSheet = false
                    },
                    onDismiss = { showBatchAnswerSheet = false }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            QuizSessionExitIconButton(
                contentDescription = "閫€鍑虹粌涔",
                onClick = { showExitPracticeConfirm = true }
            )
        }
    }
    }
}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeQuickEditScreen(
    onBack: () -> Unit
) {
    val question = QuizRepository.currentPracticeQuestion()
    val currentSessionKey = QuizRepository.currentPracticeSessionKey().orEmpty()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Quick Edit",
            title = "蹇€熺紪杈戦鐩",
            subtitle = "淇褰撳墠缁冧範棰樺悗锛屽彲鐩存帴杩斿洖缁х画鍒烽銆"
        )

        if (question == null) {
            GlassCard { NoticeCard("褰撳墠娌℃湁鍙紪杈戠殑缁冧範棰樸€") }
            ActionPillButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                text = "杩斿洖缁冧範",
                primary = false,
                modifier = Modifier.height(44.dp),
                onClick = onBack
            )
            return
        }

        var questionText by remember(currentSessionKey) { mutableStateOf(question.question) }
        var answerText by remember(currentSessionKey) { mutableStateOf(question.answer.joinToString(" / ")) }
        var blankAnswerDrafts by remember(currentSessionKey) { mutableStateOf(question.blankAnswers) }
        var analysisText by remember(currentSessionKey) { mutableStateOf(question.analysis) }
        var optionDrafts by remember(currentSessionKey) { mutableStateOf(initialQuickEditOptions(question)) }
        var savedNotice by remember(currentSessionKey) { mutableStateOf("") }
        val isObjective = question.type == QuestionType.SINGLE ||
            question.type == QuestionType.MULTIPLE ||
            question.type == QuestionType.JUDGE
        val isStructuredBlank = question.type == QuestionType.BLANK && blankAnswerDrafts.isNotEmpty()
        val detectedBlankCount = MultiBlankSupport.countExplicitBlanks(questionText)

        GlassCard {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip("绗?${question.number.ifBlank { "-" }} 棰")
                StatusChip(typeLabel(question.type))
                StatusChip("淇濈暀棰樺瀷")
            }
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = questionText,
                onValueChange = {
                    questionText = it
                    savedNotice = ""
                },
                label = { Text("棰樺共") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
            )

            if (question.images.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                NoticeCard("鍥剧墖棰樺浘鏆備笉鍦ㄥ揩閫熺紪杈戜腑淇敼锛屼繚瀛樺悗浼氱户缁繚鐣欏師棰樺浘鐗囥€")
            }

            if (isObjective) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "閫夐」",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                optionDrafts.forEachIndexed { index, option ->
                    OutlinedTextField(
                        value = option.text,
                        onValueChange = { value ->
                            optionDrafts = optionDrafts.toMutableList().also { drafts ->
                                drafts[index] = option.copy(text = value)
                            }
                            savedNotice = ""
                        },
                        label = { Text("閫夐」 ${option.key}") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionPillButton(
                        icon = Icons.Rounded.Add,
                        text = "鏂板閫夐」",
                        primary = false,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        fillWidthContent = true,
                        onClick = {
                            nextQuickEditOptionKey(optionDrafts)?.let { key ->
                                optionDrafts = optionDrafts + Option(key = key, text = "")
                                savedNotice = ""
                            }
                        }
                    )
                    ActionPillButton(
                        icon = Icons.Rounded.DeleteOutline,
                        text = "鍒犻櫎鏈€鍚",
                        primary = false,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        fillWidthContent = true,
                        enabled = optionDrafts.size > minimumOptionCount(question.type),
                        onClick = {
                            if (optionDrafts.size > minimumOptionCount(question.type)) {
                                optionDrafts = optionDrafts.dropLast(1)
                                savedNotice = ""
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            if (isStructuredBlank) {
                MultiBlankAnswerEditor(
                    blankAnswers = blankAnswerDrafts,
                    detectedBlankCount = detectedBlankCount,
                    onChange = {
                        blankAnswerDrafts = it
                        answerText = MultiBlankSupport.compatibilityAnswer(it).firstOrNull().orEmpty()
                        savedNotice = ""
                    },
                    onDisable = {
                        answerText = MultiBlankSupport.compatibilityAnswer(blankAnswerDrafts).firstOrNull().orEmpty()
                        blankAnswerDrafts = emptyList()
                        savedNotice = "宸查€€鍑哄绌烘ā寮忥紝淇濆瓨鍚庢寜鏃х増鏁翠綋绛旀澶勭悊銆"
                    }
                )
            } else {
                OutlinedTextField(
                    value = answerText,
                    onValueChange = {
                        answerText = it
                        savedNotice = ""
                    },
                    label = { Text(if (isObjective) "绛旀锛屼緥濡?A 鎴?A/B" else "鍙傝€冪瓟妗") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = if (isObjective) 1 else 2,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                if (question.type == QuestionType.BLANK && detectedBlankCount > 1) {
                    Spacer(Modifier.height(8.dp))
                    ActionPillButton(
                        icon = Icons.Rounded.Add,
                        text = "鍚敤澶氱┖绛旀",
                        primary = false,
                        onClick = {
                            blankAnswerDrafts = MultiBlankSupport.initialGroups(questionText, parseQuickEditAnswer(answerText, question.type, emptyList()))
                            savedNotice = "璇锋寜棰樼┖椤哄簭濉啓姣忎竴绌虹瓟妗堛€"
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = analysisText,
                onValueChange = {
                    analysisText = it
                    savedNotice = ""
                },
                label = { Text("瑙ｆ瀽") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
            )
            Spacer(Modifier.height(10.dp))
            AiAnalysisFillPanel(
                question = question.copy(
                    question = questionText.trim(),
                    options = if (isObjective) optionDrafts.map { it.copy(text = it.text.trim()) } else emptyList(),
                    answer = if (isStructuredBlank) MultiBlankSupport.compatibilityAnswer(blankAnswerDrafts) else parseQuickEditAnswer(answerText, question.type, optionDrafts),
                    blankAnswers = if (isStructuredBlank) blankAnswerDrafts else emptyList(),
                    analysis = analysisText.trim()
                ),
                currentAnalysis = analysisText,
                onApplyAnalysis = { value ->
                    analysisText = value
                    savedNotice = "AI 寤鸿瑙ｆ瀽宸插啓鍏ョ紪杈戞锛屼繚瀛樺悗鎵嶄細鏇存柊棰樺簱銆"
                }
            )

            if (savedNotice.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                NoticeCard(savedNotice)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionPillButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                text = "鍙栨秷",
                primary = false,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                fillWidthContent = true,
                onClick = onBack
            )
            ActionPillButton(
                icon = Icons.Rounded.CheckCircle,
                text = "淇濆瓨淇敼",
                primary = questionText.isNotBlank(),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                fillWidthContent = true,
                enabled = questionText.isNotBlank(),
                onClick = {
                    val updatedQuestion = question.copy(
                        question = questionText.trim(),
                        options = if (isObjective) optionDrafts.map { it.copy(text = it.text.trim()) } else emptyList(),
                        answer = if (isStructuredBlank) MultiBlankSupport.compatibilityAnswer(blankAnswerDrafts) else parseQuickEditAnswer(answerText, question.type, optionDrafts),
                        blankAnswers = if (isStructuredBlank) blankAnswerDrafts else emptyList(),
                        analysis = analysisText.trim()
                    )
                    if (QuizRepository.updateCurrentPracticeQuestion(updatedQuestion)) {
                        savedNotice = "宸蹭繚瀛樹慨鏀癸紝褰撳墠缁冧範棰樺凡鍒锋柊銆"
                        onBack()
                    } else {
                        savedNotice = "淇濆瓨澶辫触锛氭湭鎵惧埌杩欓亾棰樼殑婧愰搴撱€"
                    }
                }
            )
        }
    }
}

/**
 * 缂栬緫寮忔暟鎹:琛嚎澶ф暟瀛?+ 灏忔爣绛?+ 鍙戜笣涓嬪垝绾裤€? * 杩愯涓椂灞曠ず涓や釜鏍稿績鏁版嵁:褰撳墠棰樺彿(甯︽€绘暟)+ 姝ｇ‘鐜囥€? * 鏁版嵁浠$QuizRepository 鍐呴儴璇诲彇,閬垮厤鎻愬墠渚濊禆涓婂眰灏氭湭瑙ｇ畻鐨勫彉閲忋€? */
@Composable
private fun PracticeEditorialFiguresRow(
    scale: Float,
    practiceQuestionsSize: Int,
    practiceAutoScoredAnsweredCount: Int,
    practiceCorrectCount: Int,
    practiceAccuracy: Int
) {
    val isBatchPractice = QuizRepository.practiceMode == QuizRepository.PRACTICE_MODE_BATCH
    val currentPosition = if (isBatchPractice) {
        val start = QuizRepository.practiceCurrentBatchStartIndex()
        (QuizRepository.practiceIndex - start + 1).coerceAtLeast(0)
    } else {
        QuizRepository.practiceIndex + 1
    }
    val totalQuestions = if (isBatchPractice) {
        QuizRepository.practiceCurrentBatchTotal()
    } else {
        practiceQuestionsSize
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        EditorialFigure(
            value = currentPosition.toString(),
            label = if (isBatchPractice) "缁勫唴浣嶇疆" else "褰撳墠棰樺彿",
            unit = "/ $totalQuestions 棰",
            scale = scale,
            modifier = Modifier.weight(1f)
        )
        EditorialFigure(
            value = "$practiceAccuracy",
            label = if (isBatchPractice) "鏈粍姝ｇ‘鐜" else "姝ｇ‘鐜",
            unit = "% 路 ${practiceCorrectCount}/${practiceAutoScoredAnsweredCount}",
            scale = scale,
            modifier = Modifier.weight(1f)
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PracticeSetupPanel(
    bankName: String,
    scopeSummary: String,
    totalQuestions: Int,
    availableCounts: Map<QuestionType, Int>,
    selectedTypes: Set<QuestionType>,
    selectedQuestionCount: Int,
    selectedQuestionCountMode: String,
    practiceOrderMode: String,
    sequentialStartMode: String,
    sequentialProgressStartNumber: Int,
    sequentialCustomStartNumber: Int,
    sequentialRangeText: String?,
    selectedPracticeMode: String,
    selectedBatchGroupSize: Int,
    selectedBatchGroupSizeMode: String,
    showInlineAnswerSettings: Boolean,
    onSelectPracticeMode: (String) -> Unit,
    onSelectPracticeOrderMode: (String) -> Unit,
    onSelectSequentialStartMode: (String) -> Unit,
    onSelectSequentialCustomStart: (Int) -> Unit,
    onToggleType: (QuestionType) -> Unit,
    onSelectQuestionCount: (Int, String) -> Unit,
    onSelectBatchGroupSize: (Int, String) -> Unit,
    onStartPractice: () -> Unit
) {
    val selectedAvailable = availableCounts.entries.sumOf { (type, count) -> if (type in selectedTypes) count else 0 }
    var showCustomCountDialog by remember { mutableStateOf(false) }
    var customQuestionCountText by remember(selectedAvailable) {
        mutableStateOf(selectedQuestionCount.coerceIn(1, selectedAvailable.coerceAtLeast(1)).toString())
    }
    var showCustomBatchGroupDialog by remember { mutableStateOf(false) }
    var customBatchGroupText by remember(selectedQuestionCount) {
        mutableStateOf(selectedBatchGroupSize.coerceIn(1, selectedQuestionCount.coerceAtLeast(1)).toString())
    }
    var showCustomSequentialStartDialog by remember { mutableStateOf(false) }
    var customSequentialStartText by remember(sequentialCustomStartNumber, selectedAvailable) {
        mutableStateOf(sequentialCustomStartNumber.coerceIn(1, selectedAvailable.coerceAtLeast(1)).toString())
    }

    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bankName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$scopeSummary 路 閫夎寖鍥村悗寮€濮",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = "寮€濮嬬粌涔",
                primary = selectedAvailable > 0,
                modifier = Modifier.height(46.dp),
                onClick = { if (selectedAvailable > 0) onStartPractice() }
            )
        }
        Spacer(Modifier.height(12.dp))

        Text("缁冧範鑼冨洿", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(7.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = "闅忔満鎶介",
                primary = practiceOrderMode == QuizRepository.PRACTICE_ORDER_RANDOM,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                fillWidthContent = true,
                onClick = { onSelectPracticeOrderMode(QuizRepository.PRACTICE_ORDER_RANDOM) }
            )
            ActionPillButton(
                icon = Icons.AutoMirrored.Rounded.TextSnippet,
                text = "椤哄簭鍒烽",
                primary = practiceOrderMode == QuizRepository.PRACTICE_ORDER_SEQUENTIAL,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                fillWidthContent = true,
                onClick = { onSelectPracticeOrderMode(QuizRepository.PRACTICE_ORDER_SEQUENTIAL) }
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (practiceOrderMode == QuizRepository.PRACTICE_ORDER_RANDOM) "浠庡凡閫夐鍨嬩腑闅忔満鎶介銆" else "鎸夊綋鍓嶉搴撻『搴忎粠鎸囧畾璧风偣鍙栭銆",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (practiceOrderMode == QuizRepository.PRACTICE_ORDER_SEQUENTIAL) {
            Spacer(Modifier.height(10.dp))
            Text("椤哄簭璧风偣", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(7.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionPillButton(
                    icon = Icons.Rounded.PlayArrow,
                    text = "缁х画涓婃",
                    primary = sequentialStartMode == QuizRepository.SEQUENTIAL_START_LAST,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    fillWidthContent = true,
                    onClick = { onSelectSequentialStartMode(QuizRepository.SEQUENTIAL_START_LAST) }
                )
                ActionPillButton(
                    icon = Icons.AutoMirrored.Rounded.TextSnippet,
                    text = "浠庡ご寮€濮",
                    primary = sequentialStartMode == QuizRepository.SEQUENTIAL_START_FIRST,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    fillWidthContent = true,
                    onClick = { onSelectSequentialStartMode(QuizRepository.SEQUENTIAL_START_FIRST) }
                )
                ActionPillButton(
                    icon = Icons.Rounded.EditNote,
                    text = "鑷€夐鍙",
                    primary = sequentialStartMode == QuizRepository.SEQUENTIAL_START_CUSTOM,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    fillWidthContent = true,
                    onClick = {
                        customSequentialStartText = sequentialCustomStartNumber.coerceIn(1, selectedAvailable.coerceAtLeast(1)).toString()
                        showCustomSequentialStartDialog = true
                    }
                )
            }
            sequentialRangeText?.let { range ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = range,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        if (showInlineAnswerSettings) {
            Text("绛旈鏂瑰紡", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(7.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionPillButton(
                    icon = Icons.Rounded.CheckCircle,
                    text = "鍗虫椂鍙嶉",
                    primary = selectedPracticeMode == QuizRepository.PRACTICE_MODE_INSTANT,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    fillWidthContent = true,
                    onClick = { onSelectPracticeMode(QuizRepository.PRACTICE_MODE_INSTANT) }
                )
                ActionPillButton(
                    icon = Icons.AutoMirrored.Rounded.TextSnippet,
                    text = "鎵归噺鍋氶",
                    primary = selectedPracticeMode == QuizRepository.PRACTICE_MODE_BATCH,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    fillWidthContent = true,
                    onClick = { onSelectPracticeMode(QuizRepository.PRACTICE_MODE_BATCH) }
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (selectedPracticeMode == QuizRepository.PRACTICE_MODE_BATCH) "鎸夋瘡缁勯鏁拌繛缁仛棰橈紝鎻愪氦鏈粍鍚庣粺涓€鐪嬭В鏋愩€" else "姣忛鎻愪氦鍚庣珛鍗虫煡鐪嬬粨鏋滃拰瑙ｆ瀽銆",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (selectedPracticeMode == QuizRepository.PRACTICE_MODE_BATCH) {
                Spacer(Modifier.height(10.dp))
                Text("姣忕粍棰樻暟", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(7.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    val safeMaxGroupSize = selectedQuestionCount.coerceAtLeast(1)
                    ActionPillButton(
                        icon = Icons.Rounded.PlayArrow,
                        text = "10棰",
                        primary = selectedBatchGroupSizeMode == "10",
                        modifier = Modifier.height(44.dp),
                        enabled = safeMaxGroupSize >= 10,
                        onClick = { onSelectBatchGroupSize(10.coerceAtMost(safeMaxGroupSize), "10") }
                    )
                    ActionPillButton(
                        icon = Icons.Rounded.PlayArrow,
                        text = "20棰",
                        primary = selectedBatchGroupSizeMode == "20",
                        modifier = Modifier.height(44.dp),
                        enabled = safeMaxGroupSize >= 20,
                        onClick = { onSelectBatchGroupSize(20.coerceAtMost(safeMaxGroupSize), "20") }
                    )
                    ActionPillButton(
                        icon = Icons.Rounded.PlayArrow,
                        text = "鑷畾涔",
                        primary = selectedBatchGroupSizeMode == "custom",
                        modifier = Modifier.height(44.dp),
                        onClick = {
                            customBatchGroupText = selectedBatchGroupSize.coerceIn(1, safeMaxGroupSize).toString()
                            showCustomBatchGroupDialog = true
                        }
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "褰撳墠姣忕粍 ${selectedBatchGroupSize.coerceIn(1, selectedQuestionCount.coerceAtLeast(1))} 棰橈紝鎻愪氦鏈粍鍚庡啀杩涘叆涓嬩竴缁勩€",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(10.dp))
        }
        Text("棰樺瀷", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        val visibleTypes = practiceTypeOrder.filter { (availableCounts[it] ?: 0) > 0 }
        val objectiveVisibleTypes = visibleTypes.filter { it in QuizRepository.objectiveQuestionTypes() }
        if (visibleTypes.size == objectiveVisibleTypes.size && visibleTypes.size in 2..3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                visibleTypes.forEach { type ->
                    ActionPillButton(
                        icon = Icons.Rounded.CheckCircle,
                        text = "${compactTypeLabel(type)} ${availableCounts[type] ?: 0}",
                        primary = type in selectedTypes,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        fillWidthContent = true,
                        onClick = { onToggleType(type) }
                    )
                }
            }
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                visibleTypes.forEach { type ->
                    ActionPillButton(
                        icon = Icons.Rounded.CheckCircle,
                        text = "${compactTypeLabel(type)} ${availableCounts[type] ?: 0}",
                        primary = type in selectedTypes,
                        modifier = Modifier.height(44.dp),
                        onClick = { onToggleType(type) }
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("棰橀噺", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = "鍙敤 $selectedAvailable 棰",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(6.dp))
        if (selectedAvailable > 0) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                val safeAvailable = selectedAvailable
                val halfCount = (safeAvailable / 2).coerceAtLeast(1)
                ActionPillButton(
                    icon = Icons.Rounded.PlayArrow,
                    text = "鑷畾涔",
                    primary = selectedQuestionCountMode == "custom",
                    modifier = Modifier.height(44.dp),
                    onClick = {
                        customQuestionCountText = selectedQuestionCount.coerceIn(1, safeAvailable).toString()
                        showCustomCountDialog = true
                    }
                )
                buildList {
                    if (safeAvailable >= 50) add(Triple(50, "50 棰", "50"))
                    if (safeAvailable >= 100) add(Triple(100, "100 棰", "100"))
                    if (safeAvailable > 1) add(Triple(halfCount, "涓€鍗?$halfCount 棰", "half"))
                    add(Triple(safeAvailable, "鍏ㄩ儴 $safeAvailable 棰", "all"))
                }
                    .distinctBy { it.first }
                    .forEach { (count, label, mode) ->
                        ActionPillButton(
                            icon = Icons.Rounded.PlayArrow,
                            text = label,
                            primary = selectedQuestionCountMode == mode,
                            modifier = Modifier.height(44.dp),
                            onClick = { onSelectQuestionCount(count, mode) }
                        )
                    }
            }
        }
        if (selectedAvailable <= 0) {
            Spacer(Modifier.height(10.dp))
            val emptyTip = if (totalQuestions > 0) {
                "褰撳墠绛涢€夎寖鍥村唴娌℃湁鍙粌涔犻鐩€傝嫢棰樼洰宸茶鏂╅锛屽彲鍒伴搴撹鎯呯殑鏂╅鏈仮澶嶅悗缁х画缁冧範銆"
            } else {
                "褰撳墠绛涢€夋病鏈夊彲缁冧範棰樼洰锛岃鑷冲皯閫夋嫨涓€绉嶆湁棰樼洰鐨勯鍨嬨€"
            }
            NoticeCard(emptyTip, warning = true)
        }
    }

    if (showCustomSequentialStartDialog) {
        CustomQuestionCountDialog(
            title = "鑷€夐『搴忚捣鐐",
            value = customSequentialStartText,
            maxCount = selectedAvailable.coerceAtLeast(1),
            onValueChange = { customSequentialStartText = it },
            onDismiss = { showCustomSequentialStartDialog = false },
            onConfirm = { startNumber ->
                onSelectSequentialCustomStart(startNumber)
                showCustomSequentialStartDialog = false
            }
        )
    }
    if (showCustomCountDialog) {
        CustomQuestionCountDialog(
            title = "鑷畾涔夌粌涔犻閲",
            value = customQuestionCountText,
            maxCount = selectedAvailable.coerceAtLeast(1),
            onValueChange = { customQuestionCountText = it },
            onDismiss = { showCustomCountDialog = false },
            onConfirm = { count ->
                onSelectQuestionCount(count, "custom")
                showCustomCountDialog = false
            }
        )
    }
    if (showCustomBatchGroupDialog) {
        CustomQuestionCountDialog(
            title = "鑷畾涔夋瘡缁勯鏁",
            value = customBatchGroupText,
            maxCount = selectedQuestionCount.coerceAtLeast(1),
            onValueChange = { customBatchGroupText = it },
            onDismiss = { showCustomBatchGroupDialog = false },
            onConfirm = { count ->
                onSelectBatchGroupSize(count, "custom")
                showCustomBatchGroupDialog = false
            }
        )
    }
}

@Composable
private fun CompactPracticeSetupHero() {
    val density = LocalDensity.current
    val floatDistancePx = with(density) { ShirohaMotion.HeroFloatDistance.toPx() }
    val heroFloat = rememberInfiniteTransition(label = "practice_illustration_float")
    val imageOffsetY by heroFloat.animateFloat(
        initialValue = -floatDistancePx,
        targetValue = floatDistancePx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = ShirohaMotion.HeroFloatMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "practice_illustration_float_y"
    )

    GlassCard(
        modifier = Modifier.height(ShirohaDimens.HeroCardHeight),
        contentPadding = ShirohaSpacing.Xl
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PracticeSetupStepCard(index = "1", text = "閫夊ソ鍙傛暟", selected = true)
                PracticeSetupStepCard(index = "2", text = "寮€濮嬬粌涔", selected = false)
                PracticeSetupStepCard(index = "3", text = "璁板綍缁撴灉", selected = false)
            }
            if (QuizRepository.shirohaModeEnabled) {
                Box(
                    modifier = Modifier.size(ShirohaDimens.HeroImageFrameSize),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.illus_practice_hint_webp),
                        contentDescription = "缁冧範鎻愮ず",
                        modifier = Modifier
                            .size(ShirohaDimens.HeroImageFrameSize)
                            .graphicsLayer { translationY = imageOffsetY }
                            .alpha(ShirohaDimens.HeroImageAlpha),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeSetupStepCard(
    index: String,
    text: String,
    selected: Boolean
) {
    Surface(
        modifier = Modifier
            .width(ShirohaDimens.StepPillWidth)
            .defaultMinSize(minHeight = ShirohaDimens.StepPillMinHeight),
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = if (selected) ShirohaColors.BrandPrimarySoft else ShirohaColors.CardMuted,
        border = BorderStroke(ShirohaDimens.Hairline, if (selected) ShirohaColors.LineSelected else ShirohaColors.LineSoft)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ShirohaDimens.StepPillHorizontalPadding, vertical = ShirohaDimens.StepPillVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$index  $text",
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.primary else ShirohaColors.TextSecondary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}




@Composable
private fun FavoriteQuestionIconButton(
    favorited: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (favorited) Icons.Rounded.Star else Icons.Rounded.StarBorder,
            contentDescription = if (favorited) "鍙栨秷鏀惰棌褰撳墠棰樼洰" else "鏀惰棌褰撳墠棰樼洰",
            tint = if (favorited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun QuickEditQuestionIconButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.EditNote,
            contentDescription = "蹇€熺紪杈戝綋鍓嶉鐩",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SlashQuestionRoundButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        color = ShirohaColors.BrandPrimarySoft,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSelected)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "鏂",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CompactPracticeChip(
    text: String,
    selected: Boolean = false
) {
    Surface(
        modifier = Modifier.defaultMinSize(minHeight = 32.dp),
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = if (selected) ShirohaColors.BrandPrimarySoft else ShirohaColors.CardMuted,
        border = BorderStroke(ShirohaDimens.Hairline, if (selected) ShirohaColors.LineSelected else ShirohaColors.LineSoft)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            color = if (selected) MaterialTheme.colorScheme.primary else ShirohaColors.TextSecondary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Modifier.practiceNoRipplePillClick(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val shape = RoundedCornerShape(ShirohaRadius.Pill)
    return this
        .clip(shape)
        .clickable(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
}

@Composable
private fun CompactPracticeActionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .defaultMinSize(minHeight = 32.dp)
            .practiceNoRipplePillClick(onClick = onClick),
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = ShirohaColors.CardWhite86,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineStrong)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
private fun PracticeCompletionCard(
    total: Int,
    answered: Int,
    scoredAnswered: Int,
    correct: Int,
    onRestart: () -> Unit,
    onOpenRecords: () -> Unit,
    onExit: () -> Unit
) {
    val accuracy = if (scoredAnswered == 0) 0 else correct * 100 / scoredAnswered
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = "鏈疆缁冧範瀹屾垚",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (scoredAnswered == answered) {
                        "鍏?$total 棰?路 宸叉彁浜?$answered 棰?路 姝ｇ‘ $correct 棰?路 姝ｇ‘鐜?$accuracy%"
                    } else {
                        "鍏?$total 棰?路 宸叉彁浜?$answered 棰?路 鑷姩鍒ゅ垎 $scoredAnswered 棰?路 姝ｇ‘鐜?$accuracy%"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = "鍐嶇粌涓€缁",
                primary = true,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                fillWidthContent = true,
                onClick = onRestart
            )
            ActionPillButton(
                icon = Icons.AutoMirrored.Rounded.TextSnippet,
                text = "鏌ョ湅璁板綍",
                primary = false,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                fillWidthContent = true,
                onClick = onOpenRecords
            )
            ActionPillButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                text = "杩斿洖璁剧疆",
                primary = false,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                fillWidthContent = true,
                onClick = onExit
            )
        }
    }
}


private fun practiceOptionResultStyle(
    optionKey: String,
    correctAnswers: List<String>,
    result: QuestionCheckResult?,
    revealAnswer: Boolean = false
): QuizOptionResultStyle {
    if (result == null && !revealAnswer) return QuizOptionResultStyle.Neutral
    val normalizedKey = optionKey.trim().uppercase()
    val isCorrectAnswer = correctAnswers.any { it.trim().uppercase() == normalizedKey }
    val isUserSelected = result?.userAnswer.orEmpty().any { it.trim().uppercase() == normalizedKey }
    return when {
        isCorrectAnswer -> QuizOptionResultStyle.Correct
        isUserSelected -> QuizOptionResultStyle.Wrong
        else -> QuizOptionResultStyle.Neutral
    }
}

@Composable
private fun SubjectiveAnswerEditor(
    type: QuestionType,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    val isShortAnswer = type == QuestionType.SHORT
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = if (isShortAnswer) 132.dp else 58.dp),
            label = { Text(if (isShortAnswer) "浣犵殑浣滅瓟" else "浣犵殑绛旀") },
            placeholder = { Text(if (isShortAnswer) "鍐欎笅浣犵殑浣滅瓟锛屾彁浜ゅ悗瀵圭収鍙傝€冪瓟妗堛€" else "璇疯緭鍏ュ～绌哄唴瀹") },
            singleLine = !isShortAnswer,
            minLines = if (isShortAnswer) 4 else 1,
            maxLines = if (isShortAnswer) 8 else 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = if (isShortAnswer) ImeAction.Default else ImeAction.Done
            )
        )
        Text(
            text = if (isShortAnswer) "绠€绛旈鎻愪氦鍚庡彧灞曠ず鍙傝€冪瓟妗堝拰瑙ｆ瀽锛屼笉鑷姩鍒ゅ垎銆" else "濉┖棰樻彁浜ゅ悗浼氫笌鍙傝€冪瓟妗堣嚜鍔ㄦ瘮瀵广€",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SubjectiveSubmittedCapsule() {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = ShirohaColors.BrandPrimarySoft.copy(alpha = if (ShirohaColors.isDarkMode) 0.82f else 0.72f),
        border = BorderStroke(ShirohaDimens.Hairline, accent.copy(alpha = 0.34f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.EditNote,
                contentDescription = "宸叉彁浜や綔绛",
                modifier = Modifier.size(15.dp),
                tint = accent
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = "宸叉彁浜や綔绛",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
        }
    }
}

@Composable
private fun AnswerResultCapsule(correct: Boolean) {
    val accent = if (correct) ShirohaColors.StateSuccess else ShirohaColors.StateDanger
    val background = if (correct) ShirohaColors.StateSuccessSoft else ShirohaColors.StateDangerSoft
    val text = if (correct) "鍥炵瓟姝ｇ‘" else "鍥炵瓟閿欒"
    Surface(
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = background.copy(alpha = if (ShirohaColors.isDarkMode) 0.9f else 0.76f),
        border = BorderStroke(ShirohaDimens.Hairline, accent.copy(alpha = 0.42f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (correct) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = text,
                    modifier = Modifier.size(15.dp),
                    tint = accent
                )
            } else {
                Text(
                    text = "脳",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
            Spacer(Modifier.width(5.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
        }
    }
}

@Composable
private fun PracticeProgressCard(
    total: Int,
    answered: Int,
    scoredAnswered: Int,
    correct: Int,
    batchBeforeSubmit: Boolean,
    batchSubmitted: Boolean,
    batchGroupNumber: Int,
    batchGroupCount: Int,
    wrongCount: Int,
    wrongOnly: Boolean,
    expanded: Boolean,
    reciteMode: Boolean = false,
    reciteIndex: Int = 0,
    onOpenAnswerSheet: (() -> Unit),
    onToggleWrongOnly: (() -> Unit),
    onToggle: () -> Unit
) {
    val accuracy = if (scoredAnswered == 0) 0 else correct * 100 / scoredAnswered
    if (!expanded) return

    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (reciteMode) {
                    Text(
                        text = "鑳岄妯″紡",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "娴忚 ${reciteIndex.coerceIn(1, total.coerceAtLeast(1))} / $total 棰?路 涓嶈鍏ユ纭巼",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (batchSubmitted) {
                    Text(
                        text = "鎵归噺澶嶇洏",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "绗?$batchGroupNumber / $batchGroupCount 缁",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = if (batchBeforeSubmit) "鎵归噺鍋氶 路 绗?$batchGroupNumber / $batchGroupCount 缁" else "姝ｇ‘鐜",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!batchBeforeSubmit) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (scoredAnswered == answered) {
                                "宸叉彁浜?$answered / $total 棰?路 姝ｇ‘鐜?$accuracy%"
                            } else {
                                "宸叉彁浜?$answered / $total 棰?路 鑷姩鍒ゅ垎 $scoredAnswered 棰?路 姝ｇ‘鐜?$accuracy%"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onOpenAnswerSheet != null) {
                    PracticePanelCapsule(text = "绛旈鍗", onClick = onOpenAnswerSheet)
                }
                if (onToggleWrongOnly != null) {
                    PracticePanelCapsule(
                        text = if (wrongOnly) "鐪嬪叏閮" else "鍙湅閿欓",
                        enabled = wrongCount > 0,
                        onClick = onToggleWrongOnly
                    )
                }
                PracticePanelCapsule(
                    text = "鏀惰捣",
                    onClick = onToggle
                )
            }
        }
    }
}

@Composable
private fun PracticePanelCapsule(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .defaultMinSize(minHeight = 32.dp)
            .practiceNoRipplePillClick(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = if (enabled) ShirohaColors.CardWhite86 else ShirohaColors.CardMuted,
        border = BorderStroke(ShirohaDimens.Hairline, if (enabled) ShirohaColors.LineStrong else ShirohaColors.LineSoft)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else ShirohaColors.TextSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun PracticeAccuracyCapsule(
    accuracy: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .width(38.dp)
            .practiceNoRipplePillClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$accuracy%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Modifier.questionSwipeNavigation(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
): Modifier {
    val thresholdPx = with(LocalDensity.current) { 62.dp.toPx() }
    val maxOffsetPx = with(LocalDensity.current) { 34.dp.toPx() }
    val swipeOffset = remember { Animatable(0f) }
    val swipeScope = rememberCoroutineScope()
    var dragAmount by remember { mutableStateOf(0f) }
    val dragState = rememberDraggableState { dragDelta ->
        dragAmount += dragDelta
        val visualOffset = (dragAmount * 0.42f).coerceIn(-maxOffsetPx, maxOffsetPx)
        swipeScope.launch { swipeOffset.snapTo(visualOffset) }
    }

    fun resetSwipeOffset() {
        swipeScope.launch { swipeOffset.animateTo(0f, animationSpec = tween(durationMillis = 140)) }
    }

    val offsetFraction = if (maxOffsetPx > 0f) {
        (abs(swipeOffset.value) / maxOffsetPx).coerceIn(0f, 1f)
    } else {
        0f
    }

    return this
        .graphicsLayer {
            translationX = swipeOffset.value
            alpha = 1f - offsetFraction * 0.05f
        }
        .draggable(
            state = dragState,
            orientation = Orientation.Horizontal,
            onDragStarted = {
                dragAmount = 0f
                swipeScope.launch { swipeOffset.stop() }
            },
            onDragStopped = {
                when {
                    dragAmount <= -thresholdPx -> onSwipeLeft()
                    dragAmount >= thresholdPx -> onSwipeRight()
                }
                dragAmount = 0f
                resetSwipeOffset()
            }
        )
}


@Composable
private fun BatchPracticeAnswerSheetDialog(
    groupNumber: Int,
    groupCount: Int,
    indexes: List<Int>,
    currentIndex: Int,
    submitted: Boolean,
    isAnswered: (Int) -> Boolean,
    isCorrect: (Int) -> Boolean?,
    onJump: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (submitted) "绗?$groupNumber / $groupCount 缁勫鐩樼瓟棰樺崱" else "绗?$groupNumber / $groupCount 缁勭瓟棰樺崱") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BatchLegendChip("褰撳墠", selected = true)
                    if (submitted) {
                        BatchLegendChip("姝ｇ‘", correct = true)
                        BatchLegendChip("閿欒", correct = false)
                    } else {
                        BatchLegendChip("宸茬瓟")
                        BatchLegendChip("鏈瓟", muted = true)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    indexes.chunked(5).forEach { rowIndexes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            rowIndexes.forEach { index ->
                                val current = index == currentIndex
                                val correct = if (submitted) isCorrect(index) else null
                                val answered = isAnswered(index)
                                BatchAnswerNumberChip(
                                    number = index - indexes.first() + 1,
                                    current = current,
                                    answered = answered,
                                    submitted = submitted,
                                    correct = correct,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onJump(index) }
                                )
                            }
                            repeat(5 - rowIndexes.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("鍏抽棴") } }
    )
}

@Composable
private fun BatchLegendChip(
    text: String,
    selected: Boolean = false,
    correct: Boolean? = null,
    muted: Boolean = false
) {
    val background = when {
        selected -> ShirohaColors.BrandPrimarySoft
        correct == true -> ShirohaColors.StateSuccessSoft
        correct == false -> ShirohaColors.StateDangerSoft
        muted -> ShirohaColors.CardMuted
        else -> ShirohaColors.CardWhite86
    }
    val borderColor = when {
        selected -> ShirohaColors.LineSelected
        correct == true -> ShirohaColors.StateSuccess
        correct == false -> ShirohaColors.StateDanger
        else -> ShirohaColors.LineSoft
    }
    val textColor = when {
        selected -> MaterialTheme.colorScheme.primary
        correct == true -> ShirohaColors.StateSuccess
        correct == false -> ShirohaColors.StateDanger
        else -> ShirohaColors.TextSecondary
    }
    Surface(
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = background,
        border = BorderStroke(ShirohaDimens.Hairline, borderColor)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
private fun BatchAnswerNumberChip(
    number: Int,
    current: Boolean,
    answered: Boolean,
    submitted: Boolean,
    correct: Boolean?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = when {
        current -> ShirohaColors.BrandPrimarySoft
        submitted && correct == true -> ShirohaColors.StateSuccessSoft
        submitted && correct == false -> ShirohaColors.StateDangerSoft
        answered -> ShirohaColors.CardWhite86
        else -> ShirohaColors.CardMuted
    }
    val borderColor = when {
        current -> ShirohaColors.LineSelected
        submitted && correct == true -> ShirohaColors.StateSuccess
        submitted && correct == false -> ShirohaColors.StateDanger
        answered -> ShirohaColors.LineStrong
        else -> ShirohaColors.LineSoft
    }
    val textColor = when {
        current -> MaterialTheme.colorScheme.primary
        submitted && correct == true -> ShirohaColors.StateSuccess
        submitted && correct == false -> ShirohaColors.StateDanger
        else -> MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = modifier
            .height(36.dp)
            .practiceNoRipplePillClick(onClick = onClick),
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = background,
        border = BorderStroke(ShirohaDimens.Hairline, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun UnsubmittedPracticeConfirmDialog(
    unansweredCount: Int,
    onDismiss: () -> Unit,
    onReturnToAnswer: () -> Unit,
    onComplete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("杩樻湁 $unansweredCount 棰樻湭鎻愪氦") },
        text = { Text("鍙互杩斿洖琛ョ瓟锛屾垨灏嗘湭鎻愪氦棰樻寜鏈綔绛斿鐞嗗悗瀹屾垚缁冧範銆") },
        confirmButton = {
            TextButton(onClick = onComplete) {
                Text("鎸夋湭浣滅瓟澶勭悊骞跺畬鎴")
            }
        },
        dismissButton = {
            TextButton(onClick = onReturnToAnswer) {
                Text("杩斿洖琛ョ瓟")
            }
        }
    )
}

@Composable
private fun PracticeExitConfirmDialog(
    canSaveProgress: Boolean,
    onDismiss: () -> Unit,
    onDirectExit: () -> Unit,
    onSaveAndExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("閫€鍑虹粌涔狅紵") },
        text = {
            Text(
                text = if (canSaveProgress) {
                    "淇濆瓨閫€鍑哄悗锛屼笅娆″彲浠庡綋鍓嶄綅缃户缁€傜洿鎺ラ€€鍑轰笉浼氭洿鏂伴『搴忚繘搴︺€"
                } else {
                    "閫€鍑哄悗灏嗙粨鏉熷綋鍓嶇粌涔犮€"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            if (canSaveProgress) {
                TextButton(onClick = onSaveAndExit) { Text("淇濆瓨閫€鍑") }
            } else {
                TextButton(onClick = onDirectExit) { Text("閫€鍑") }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onDismiss) { Text("鍙栨秷") }
                if (canSaveProgress) {
                    TextButton(onClick = onDirectExit) { Text("鐩存帴閫€鍑") }
                }
            }
        }
    )
}

@Composable
private fun BatchSubmitConfirmDialog(
    unansweredCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("鎻愪氦鏈粍锛") },
        text = {
            Text(
                text = if (unansweredCount > 0) {
                    "杩樻湁 $unansweredCount 棰樻湭浣滅瓟锛屾彁浜ゅ悗浼氭寜閿欒澶勭悊銆傜‘瀹氫粛鐒舵彁浜ゅ悧锛"
                } else {
                    "鎻愪氦鍚庡皢缁熶竴鍒ゅ垎锛屽苟杩涘叆瑙ｆ瀽澶嶇洏銆"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("鎻愪氦") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("鍙栨秷") } }
    )
}


@Composable
private fun CustomQuestionCountDialog(
    title: String,
    value: String,
    maxCount: Int,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
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
                    label = { Text("棰橀噺") },
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


private fun normalizeVisiblePracticeCountMode(mode: String, availableCount: Int): String {
    return when {
        mode == "50" && availableCount >= 50 -> "50"
        mode == "100" && availableCount >= 100 -> "100"
        mode == "half" -> "half"
        mode == "all" -> "all"
        else -> "custom"
    }
}

private fun resolvePracticeQuestionCount(
    mode: String,
    customCount: Int,
    availableCount: Int
): Int {
    val safeAvailable = availableCount.coerceAtLeast(1)
    return when (mode) {
        "50" -> 50.coerceAtMost(safeAvailable)
        "100" -> 100.coerceAtMost(safeAvailable)
        "half" -> (safeAvailable / 2).coerceAtLeast(1)
        "all" -> safeAvailable
        else -> customCount.coerceIn(1, safeAvailable)
    }
}

@Composable
private fun SingleQuestionAiAnalysisPanel(
    analysis: AiSingleQuestionAnalysis?,
    error: String?,
    loading: Boolean,
    onAnalyze: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ActionPillButton(
            icon = Icons.Rounded.AutoAwesome,
            text = when {
                loading -> "AI 鍒嗘瀽涓"
                analysis != null -> "閲嶆柊鍒嗘瀽鏈"
                else -> "AI 鍒嗘瀽鏈"
            },
            primary = false,
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            fillWidthContent = true,
            onClick = onAnalyze
        )
        if (loading) {
            NoticeCard("AI 姝ｅ湪鍒嗘瀽褰撳墠棰樼洰锛岃绋嶅€欍€", warning = false)
        }
        error?.takeIf { it.isNotBlank() }?.let { message ->
            NoticeCard("AI 鍒嗘瀽澶辫触锛$message", warning = true)
        }
        analysis?.let { result ->
            SingleQuestionAiResultCard(result)
        }
    }
}

@Composable
private fun SingleQuestionAiResultCard(result: AiSingleQuestionAnalysis) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ShirohaColors.CardWhite68,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI 鍙傝€冨垎鏋",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusChip(text = aiConfidenceLabel(result.confidence))
            }
            Text(
                text = "鍙傝€冪瓟妗堬細${result.suggestedAnswer}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            result.matchesLocalAnswer?.let { matched ->
                Text(
                    text = if (matched) "涓庨搴撶瓟妗堬細涓€鑷" else "涓庨搴撶瓟妗堬細鍙兘涓嶄竴鑷达紝寤鸿浜哄伐纭",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (matched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            if (result.needsReview) {
                Text(
                    text = "闇€瑕佷汉宸ョ‘璁",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = LatexDisplayFormatter.format(formatAnalysisForDisplay(result.analysis)),
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 23.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            result.warning.takeIf { it.isNotBlank() }?.let { warning ->
                Text(
                    text = "鎻愮ず锛$warning",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Text(
                text = "AI 缁撴灉浠呬緵鍙傝€冿紝涓嶄細鑷姩淇敼棰樺簱绛旀鎴栬В鏋愩€",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun aiConfidenceLabel(confidence: String): String {
    return when (confidence.trim().uppercase()) {
        "HIGH" -> "鍙俊搴?楂"
        "LOW" -> "鍙俊搴?浣"
        else -> "鍙俊搴?涓"
    }
}

private fun formatAnalysisForDisplay(analysis: String): String {
    return analysis.trim()
        .replace(Regex("\\s*(?=([A-G锛?锛)椤筟锛?锛?])"), "\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}

private data class PracticeDisplayOption(
    val displayKey: String,
    val originalKey: String,
    val text: String
)

private fun practiceDisplayOptions(
    question: Question,
    shuffleEnabled: Boolean,
    sessionSeed: Long,
    sessionKey: String
): List<PracticeDisplayOption> {
    val canShuffle = shuffleEnabled &&
        (question.type == QuestionType.SINGLE || question.type == QuestionType.MULTIPLE) &&
        question.options.size > 1
    val orderedOptions = if (canShuffle) {
        val seedSource = "$sessionKey|${question.number}|${question.question}"
        val seed = sessionSeed xor seedSource.hashCode().toLong()
        question.options.toMutableList().also { options ->
            java.util.Collections.shuffle(options, java.util.Random(seed))
        }
    } else {
        question.options
    }
    return orderedOptions.mapIndexed { index, option ->
        PracticeDisplayOption(
            displayKey = if (canShuffle) practiceDisplayKey(index) else option.key,
            originalKey = option.key,
            text = option.text
        )
    }
}

private fun practiceDisplayKey(index: Int): String {
    return if (index in 0..25) ('A'.code + index).toChar().toString() else (index + 1).toString()
}

private fun practiceAnswersForDisplay(
    answers: List<String>,
    originalToDisplay: Map<String, String>
): List<String> {
    return answers.map { answer ->
        val normalized = answer.trim().uppercase()
        originalToDisplay[normalized] ?: answer
    }
}

private fun practiceQuestionForDisplay(
    question: Question,
    displayOptions: List<PracticeDisplayOption>
): Question {
    if (question.type != QuestionType.SINGLE && question.type != QuestionType.MULTIPLE) return question
    val originalToDisplay = displayOptions.associate { it.originalKey.trim().uppercase() to it.displayKey }
    return question.copy(
        options = displayOptions.map { Option(key = it.displayKey, text = it.text) },
        answer = practiceAnswersForDisplay(question.answer, originalToDisplay)
    )
}

private val practiceTypeOrder = listOf(
    QuestionType.SINGLE,
    QuestionType.MULTIPLE,
    QuestionType.JUDGE,
    QuestionType.BLANK,
    QuestionType.SHORT
)


private fun initialQuickEditOptions(question: Question): List<Option> {
    if (question.options.isNotEmpty()) return question.options
    return when (question.type) {
        QuestionType.JUDGE -> listOf(
            Option("A", "姝ｇ‘"),
            Option("B", "閿欒")
        )
        QuestionType.SINGLE,
        QuestionType.MULTIPLE -> listOf("A", "B", "C", "D").map { key -> Option(key, "") }
        else -> emptyList()
    }
}

private fun minimumOptionCount(type: QuestionType): Int = when (type) {
    QuestionType.JUDGE -> 2
    QuestionType.SINGLE,
    QuestionType.MULTIPLE -> 2
    else -> 0
}

private fun nextQuickEditOptionKey(options: List<Option>): String? {
    val used = options.map { it.key.uppercase() }.toSet()
    return ('A'..'Z').firstOrNull { it.toString() !in used }?.toString()
}

private fun parseQuickEditAnswer(raw: String, type: QuestionType, options: List<Option>): List<String> {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return emptyList()
    return when (type) {
        QuestionType.SINGLE,
        QuestionType.MULTIPLE,
        QuestionType.JUDGE -> {
            val upper = trimmed.uppercase()
            val normalized = when (upper) {
                "姝ｇ‘", "瀵", "鏄", "TRUE", "T", "鈭" -> "A"
                "閿欒", "閿", "鍚", "FALSE", "F", "脳", "X" -> "B"
                else -> upper
            }
            val optionKeys = options.map { it.key.uppercase() }.toSet()
            val tokens = normalized
                .split(Regex("""[\s,锛屻€"|;锛沒+"""))
                .flatMap { token ->
                    if (token.length > 1 && token.all { it in 'A'..'Z' }) {
                        token.map { it.toString() }
                    } else {
                        listOf(token)
                    }
                }
                .map { it.trim().uppercase() }
                .filter { it.isNotBlank() && it in optionKeys }
                .distinct()
            tokens
        }
        QuestionType.BLANK,
        QuestionType.SHORT -> listOf(trimmed)
    }
}


private fun compactTypeLabel(type: QuestionType): String = when (type) {
    QuestionType.SINGLE -> "鍗曢€"
    QuestionType.MULTIPLE -> "澶氶€"
    QuestionType.JUDGE -> "鍒ゆ柇"
    QuestionType.BLANK -> "濉┖"
    QuestionType.SHORT -> "绠€绛"
}

private fun typeLabel(type: QuestionType): String = when (type) {
    QuestionType.SINGLE -> "鍗曢€夐"
    QuestionType.MULTIPLE -> "澶氶€夐"
    QuestionType.JUDGE -> "鍒ゆ柇棰"
    QuestionType.BLANK -> "濉┖棰"
    QuestionType.SHORT -> "绠€绛旈"
}