package com.yiqiu.shirohaquiz.ui.screens

import com.yiqiu.shirohaquiz.ui.theme.shirohaEditorialBackground

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.ai.AiRefactorResult
import com.yiqiu.shirohaquiz.ai.AiReviewSuggestion
import com.yiqiu.shirohaquiz.ai.ShirohaAiClient
import com.yiqiu.shirohaquiz.importer.model.ImportDiagnostics
import com.yiqiu.shirohaquiz.importer.model.ImportResult
import com.yiqiu.shirohaquiz.importer.model.ImportWarning
import com.yiqiu.shirohaquiz.importer.model.MultiBlankSupport
import com.yiqiu.shirohaquiz.importer.model.Option
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.importer.assets.QuestionImageBinder
import com.yiqiu.shirohaquiz.importer.assets.QuestionImportAssetExtractor
import com.yiqiu.shirohaquiz.importer.model.WarningLevel
import com.yiqiu.shirohaquiz.importer.parser.QuizImportParser
import com.yiqiu.shirohaquiz.importer.parser.TextImportDecoder
import com.yiqiu.shirohaquiz.importer.validate.ImportValidator
import com.yiqiu.shirohaquiz.state.DEFAULT_BANK_GROUP_NAME
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.AiAnalysisFillPanel
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.ui.components.EditorialFigure
import com.yiqiu.shirohaquiz.ui.components.EditorialSection
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.LoadingIllustration
import com.yiqiu.shirohaquiz.ui.components.MultiBlankAnswerEditor
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.QuestionImagesBlock
import com.yiqiu.shirohaquiz.ui.components.ShirohaDangerConfirmDialog
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.ShirohaMotion
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.editorialScaleFor
import com.yiqiu.shirohaquiz.ui.theme.screenClassFor
import com.yiqiu.shirohaquiz.ui.theme.uiScaleFor
import com.yiqiu.shirohaquiz.ui.util.bankDisplayPath
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportScreen(
    onImportSaved: () -> Unit,
    onOpenPreference: () -> Unit
) {
    val context = LocalContext.current
    val importScope = rememberCoroutineScope()
    var rawText by remember { mutableStateOf("") }
    var answerText by remember { mutableStateOf("") }
    var importedImages by remember { mutableStateOf<List<QuestionImportAssetExtractor.ExtractedImportImage>>(emptyList()) }
    var selectedFileName by rememberSaveable { mutableStateOf("鏈€夋嫨鏂囦欢") }
    var selectedAnswerFileName by rememberSaveable { mutableStateOf("鏈€夋嫨绛旀鏂囦欢") }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }
    var editableQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var reviewMode by rememberSaveable { mutableStateOf(false) }
    var reviewIndex by rememberSaveable { mutableStateOf(0) }
    var reviewEditingIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var reviewEditFromFilterList by rememberSaveable { mutableStateOf(false) }
    var reviewFilterListFocusTick by rememberSaveable { mutableStateOf(0) }
    var reviewFilterName by rememberSaveable { mutableStateOf(ReviewFilter.ALL.name) }
    var statusText by rememberSaveable {
        mutableStateOf("璇烽€夋嫨棰樺簱鏂囦欢銆")
    }
    var isStatusWarn by rememberSaveable { mutableStateOf(false) }
    var useDualImport by rememberSaveable { mutableStateOf(false) }
    var isImportBusy by rememberSaveable { mutableStateOf(false) }
    var busyText by rememberSaveable { mutableStateOf("") }
    var rawTextEditorExpanded by rememberSaveable { mutableStateOf(true) }
    var answerTextEditorExpanded by rememberSaveable { mutableStateOf(true) }
    var previewOnlyAnomaly by rememberSaveable { mutableStateOf(false) }
    var showAiConfigPrompt by rememberSaveable { mutableStateOf(false) }
    var rawFullEditorMode by rememberSaveable { mutableStateOf(false) }
    var answerFullEditorMode by rememberSaveable { mutableStateOf(false) }
    var aiReviewedQuestionIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var aiAnalyzedQuestionIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var aiAnalysisAppliedQuestionIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var aiReviewSuggestions by remember { mutableStateOf<List<AiReviewSuggestion>>(emptyList()) }
    var saveMode by rememberSaveable { mutableStateOf(ImportSaveMode.NEW_BANK.name) }
    var newBankGroupName by rememberSaveable { mutableStateOf(DEFAULT_BANK_GROUP_NAME) }
    var newBankName by rememberSaveable { mutableStateOf("瀵煎叆棰樺簱") }
    var appendTargetBankId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedFileName) {
        val defaultBankName = defaultImportBankName(selectedFileName)
        if (newBankName.isBlank() || newBankName == "瀵煎叆棰樺簱" || newBankName == "鏈€夋嫨鏂囦欢") {
            newBankName = defaultBankName
        }
    }

    fun clearParsedResult(clearImages: Boolean = false) {
        importResult = null
        editableQuestions = emptyList()
        reviewMode = false
        reviewIndex = 0
        reviewEditingIndex = null
        reviewFilterName = ReviewFilter.ALL.name
        previewOnlyAnomaly = false
        aiReviewedQuestionIds = emptyList()
        aiAnalyzedQuestionIds = emptyList()
        aiAnalysisAppliedQuestionIds = emptyList()
        aiReviewSuggestions = emptyList()
        if (clearImages) importedImages = emptyList()
    }

    fun applyParsedResult(result: ImportResult) {
        val resultWithExtraWarnings = result.copy(
            warnings = refreshImportWarningsForQuestions(result.warnings, result.questions)
        )
        importResult = resultWithExtraWarnings
        editableQuestions = resultWithExtraWarnings.questions
        reviewIndex = 0
        reviewEditingIndex = null
        reviewFilterName = ReviewFilter.ALL.name
        aiReviewedQuestionIds = emptyList()
        aiAnalyzedQuestionIds = emptyList()
        aiAnalysisAppliedQuestionIds = emptyList()
        aiReviewSuggestions = emptyList()
        val hardCount = resultWithExtraWarnings.warnings.count { it.level == WarningLevel.ERROR }
        val softCount = resultWithExtraWarnings.warnings.count { it.level == WarningLevel.WARNING }
        statusText = "宸插畬鎴${if (useDualImport) "鍙屾枃浠? else "鍘熺敓"}瑙ｆ瀽锛${resultWithExtraWarnings.questions.size} 棰橈紝纭敊璇?$hardCount 鏉★紝鍙‘璁ゆ彁绀?$softCount 鏉°€?
        isStatusWarn = hardCount > 0 || softCount > 0
    }

    fun syncEditableQuestions(
        nextQuestions: List<Question>,
        baseWarnings: List<ImportWarning> = importResult?.warnings.orEmpty()
    ) {
        editableQuestions = nextQuestions
        importResult = importResult?.copy(
            questions = nextQuestions,
            warnings = refreshImportWarningsForQuestions(baseWarnings, nextQuestions)
        )
    }

    if (rawFullEditorMode) {
        BackHandler { rawFullEditorMode = false }
        FullImportTextEditorScreen(
            title = if (useDualImport) "缂栬緫棰樼洰鏂囨湰" else "缂栬緫鍘熷鏂囨湰",
            value = rawText,
            placeholder = "鎶婃爣鍑嗛搴撴枃鏈矘璐村埌杩欓噷锛屾垨閫氳繃鏂囦欢瀵煎叆鍚庡湪杩欓噷璋冩暣銆",
            onValueChange = {
                rawText = it
                if (importResult != null || editableQuestions.isNotEmpty() || reviewMode || importedImages.isNotEmpty()) {
                    clearParsedResult(clearImages = true)
                }
            },
            onBack = { rawFullEditorMode = false }
        )
        return
    }

    if (answerFullEditorMode) {
        BackHandler { answerFullEditorMode = false }
        FullImportTextEditorScreen(
            title = "缂栬緫绛旀鏂囨湰",
            value = answerText,
            placeholder = "绮樿创绛旀鏂囨湰锛屾垨閫氳繃涓婃柟鎸夐挳閫夋嫨绛旀鏂囦欢銆",
            onValueChange = {
                answerText = it
                if (importResult != null || editableQuestions.isNotEmpty() || reviewMode) {
                    clearParsedResult()
                }
            },
            onBack = { answerFullEditorMode = false }
        )
        return
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null || isImportBusy) return@rememberLauncherForActivityResult
        selectedFileName = queryFileName(context, uri)
        val fileSizeCheck = checkImportFileSize(context, uri, selectedFileName)
        if (fileSizeCheck.blockMessage != null) {
            statusText = fileSizeCheck.blockMessage
            isStatusWarn = true
            return@rememberLauncherForActivityResult
        }
        isImportBusy = true
        busyText = "姝ｅ湪璇诲彇棰樺簱鏂囦欢鈥︹€"
        statusText = fileSizeCheck.warnMessage ?: "姝ｅ湪璇诲彇锛$selectedFileName"
        isStatusWarn = fileSizeCheck.warnMessage != null
        clearParsedResult(clearImages = true)
        importScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    readImportedContent(context, uri, selectedFileName)
                }
            }
            isImportBusy = false
            when (val decodeResult = result.getOrElse {
                QuestionImportAssetExtractor.DecodeResult.Failure("鏂囦欢鏃犳硶璇诲彇锛屽彲鑳藉凡鎹熷潖鎴栨病鏈夎闂潈闄愩€")
            }) {
                is QuestionImportAssetExtractor.DecodeResult.Success -> {
                    val content = decodeResult.content
                    if (content.text.isBlank()) {
                        statusText = "宸茶鍙栨枃浠讹紝浣嗘病鏈夊彂鐜板彲鐢ㄦ枃鏈唴瀹广€傝纭鏂囨。涓嶆槸绌虹櫧銆佹壂鎻忓浘鐗囨垨鏃х増 xls銆"
                        isStatusWarn = true
                    } else {
                        rawText = content.text
                        importedImages = content.images
                        rawTextEditorExpanded = content.text.length <= LARGE_TEXT_PREVIEW_THRESHOLD
                        statusText = if (content.images.isNotEmpty()) {
                            "宸茶鍙栵細$selectedFileName锛屽惈 ${content.images.size} 寮犲浘鐗囥€"
                        } else {
                            "宸茶鍙栵細$selectedFileName銆"
                        }
                        isStatusWarn = false
                    }
                }
                is QuestionImportAssetExtractor.DecodeResult.Failure -> {
                    statusText = decodeResult.userMessage
                    isStatusWarn = true
                }
            }
        }
    }

    val answerFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null || isImportBusy) return@rememberLauncherForActivityResult
        selectedAnswerFileName = queryFileName(context, uri)
        val fileSizeCheck = checkImportFileSize(context, uri, selectedAnswerFileName)
        if (fileSizeCheck.blockMessage != null) {
            statusText = fileSizeCheck.blockMessage
            isStatusWarn = true
            return@rememberLauncherForActivityResult
        }
        isImportBusy = true
        busyText = "姝ｅ湪璇诲彇绛旀鏂囦欢鈥︹€"
        statusText = fileSizeCheck.warnMessage ?: "姝ｅ湪璇诲彇绛旀鏂囦欢锛$selectedAnswerFileName"
        isStatusWarn = fileSizeCheck.warnMessage != null
        importScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    readImportedText(context, uri, selectedAnswerFileName)
                }
            }
            isImportBusy = false
            when (val decodeResult = result.getOrElse {
                TextImportDecoder.DecodeResult.Failure("绛旀鏂囦欢鏃犳硶璇诲彇锛屽彲鑳藉凡鎹熷潖鎴栨病鏈夎闂潈闄愩€")
            }) {
                is TextImportDecoder.DecodeResult.Success -> {
                    val text = decodeResult.text
                    if (text.isBlank()) {
                        statusText = "宸茶鍙栫瓟妗堟枃浠讹紝浣嗘病鏈夊彂鐜板彲鐢ㄦ枃鏈唴瀹广€傝纭鏂囦欢涓嶆槸绌虹櫧銆佹壂鎻忓浘鐗囨垨鏃х増 xls銆"
                        isStatusWarn = true
                    } else {
                        answerText = text
                        answerTextEditorExpanded = text.length <= LARGE_TEXT_PREVIEW_THRESHOLD
                        clearParsedResult()
                        statusText = "宸茶鍙栫瓟妗堟枃浠讹細$selectedAnswerFileName銆"
                        isStatusWarn = false
                    }
                }
                is TextImportDecoder.DecodeResult.Failure -> {
                    statusText = decodeResult.userMessage
                    isStatusWarn = true
                }
            }
        }
    }

    fun startParse() {
        if (isImportBusy) return
        if (rawText.isBlank() || (useDualImport && answerText.isBlank())) {
            statusText = if (useDualImport) {
                "璇峰悓鏃舵彁渚涢鐩枃鏈拰绛旀鏂囨湰锛屽啀寮€濮嬪弻鏂囦欢瑙ｆ瀽銆"
            } else {
                "璇峰厛鎻愪緵棰樺簱鏂囨湰锛屽啀寮€濮嬪師鐢熻В鏋愩€"
            }
            isStatusWarn = true
            return
        }

        val rawSnapshot = rawText
        val answerSnapshot = answerText
        val imagesSnapshot = importedImages
        val dualSnapshot = useDualImport
        isImportBusy = true
        busyText = if (dualSnapshot) "姝ｅ湪鍚堝苟棰樼洰鍜岀瓟妗堚€︹€? else "姝ｅ湪瑙ｆ瀽棰樺簱鏂囨湰鈥︹€?
        statusText = busyText
        isStatusWarn = false
        clearParsedResult()
        importScope.launch {
            val result = runCatching {
                withContext(Dispatchers.Default) {
                    val jsonPreview = if (!dualSnapshot) QuizRepository.parseImportJsonPreview(rawSnapshot) else null
                    if (jsonPreview != null && jsonPreview.banks.size > 1) {
                        throw IllegalArgumentException("${jsonPreview.message} 璇峰湪 鎴戠殑 鈫?鏁版嵁绠＄悊 鈫?瀵煎叆棰樺簱 涓鍏ュ棰樺簱 JSON / 澶囦唤鍖呫€")
                    }
                    val jsonBank = jsonPreview?.banks?.singleOrNull()
                    val parsedResult = if (jsonBank != null) {
                        ImportResult(
                            questions = jsonBank.questions,
                            strategyName = "JSON棰樺簱瀵煎叆",
                            warnings = emptyList(),
                            diagnostics = ImportDiagnostics(
                                normalizedLength = rawSnapshot.length,
                                blockCount = jsonBank.questions.size,
                                answeredCount = jsonBank.questions.count { it.answer.isNotEmpty() },
                                candidateCount = 1,
                                notes = listOf(jsonPreview.message)
                            )
                        )
                    } else if (dualSnapshot) {
                        QuizImportParser.parseDualText(rawSnapshot, answerSnapshot)
                    } else {
                        QuizImportParser.parseStandardText(rawSnapshot)
                    }
                    val finalResult = if (jsonBank == null && imagesSnapshot.isNotEmpty()) {
                        QuestionImageBinder.attach(parsedResult, imagesSnapshot)
                    } else {
                        parsedResult
                    }
                    finalResult to jsonPreview
                }
            }
            isImportBusy = false
            result.onSuccess { (finalResult, jsonPreview) ->
                val jsonBank = jsonPreview?.banks?.singleOrNull()
                if (jsonBank != null) {
                    newBankName = jsonBank.name.ifBlank { defaultImportBankName(selectedFileName) }
                    newBankGroupName = jsonBank.groupName.ifBlank { DEFAULT_BANK_GROUP_NAME }
                }
                applyParsedResult(finalResult)
                if (jsonPreview != null && jsonBank != null) {
                    statusText = jsonPreview.message
                    isStatusWarn = false
                }
            }.onFailure { error ->
                statusText = "瑙ｆ瀽澶辫触锛${error.message ?: "璇锋鏌ラ搴撴枃浠舵牸寮"}"
                isStatusWarn = true
            }
        }
    }

    fun deleteReviewQuestionAt(index: Int) {
        val deletedQuestionId = editableQuestions.getOrNull(index)?.id
        val nextQuestions = editableQuestions.filterIndexed { currentIndex, _ -> currentIndex != index }
        val baseWarnings = importResult?.warnings.orEmpty().filterNot { warning ->
            deletedQuestionId != null && (importWarningQuestionId(warning) ?: aiWarningQuestionId(warning)) == deletedQuestionId
        }
        syncEditableQuestions(nextQuestions, baseWarnings)
        if (deletedQuestionId != null) {
            aiReviewSuggestions = aiReviewSuggestions.filterNot { it.questionId == deletedQuestionId }
            aiReviewedQuestionIds = aiReviewedQuestionIds.filterNot { it == deletedQuestionId }
            aiAnalyzedQuestionIds = aiAnalyzedQuestionIds.filterNot { it == deletedQuestionId }
            aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds.filterNot { it == deletedQuestionId }
        }
        reviewEditingIndex = null
        reviewEditFromFilterList = false
        reviewIndex = index.coerceAtMost((nextQuestions.size - 1).coerceAtLeast(0))
        firstMatchingQuestionIndex(
            questions = nextQuestions,
            warnings = importResult?.warnings.orEmpty(),
            aiSuggestions = aiReviewSuggestions,
            aiReviewedQuestionIds = aiReviewedQuestionIds,
            aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
            aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds,
            filter = reviewFilterFromName(reviewFilterName)
        )?.let { reviewIndex = it }
        statusText = "宸蹭粠鏍稿鍒楄〃涓Щ闄?1 棰樸€備繚瀛橀搴撴椂浼氫娇鐢ㄥ綋鍓嶆牳瀵瑰悗鐨勯鐩€"
        isStatusWarn = false
    }

    val editingReviewIndex = reviewEditingIndex
    if (reviewMode && importResult != null && editingReviewIndex != null && editableQuestions.isNotEmpty()) {
        BackHandler {
            reviewEditingIndex = null
            if (reviewEditFromFilterList) {
                reviewFilterListFocusTick += 1
            }
            reviewEditFromFilterList = false
        }
        val safeEditingIndex = editingReviewIndex.coerceIn(0, editableQuestions.lastIndex)
        val editingQuestion = editableQuestions[safeEditingIndex]
        ReviewQuestionEditScreen(
            question = editingQuestion,
            questionIndex = safeEditingIndex,
            totalCount = editableQuestions.size,
            questionWarnings = warningsForQuestion(editingQuestion, importResult?.warnings.orEmpty()),
            questionAiSuggestions = aiReviewSuggestions.filter { it.questionId == editingQuestion.id && isActionableAiSuggestion(it) },
            aiReviewedQuestionIds = aiReviewedQuestionIds,
            aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
            aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds,
            onQuestionChange = { question ->
                syncEditableQuestions(
                    editableQuestions.mapIndexed { currentIndex, item ->
                        if (currentIndex == safeEditingIndex) question else item
                    }
                )
            },
            onApplyAiSuggestion = { suggestion ->
                val currentQuestion = editableQuestions.getOrNull(safeEditingIndex)
                if (currentQuestion != null && canApplyAiSuggestion(suggestion)) {
                    val nextQuestion = applyAiReviewSuggestion(currentQuestion, suggestion)
                    val nextQuestions = editableQuestions.mapIndexed { currentIndex, item ->
                        if (currentIndex == safeEditingIndex) nextQuestion else item
                    }
                    val baseWarnings = importResult?.warnings.orEmpty().filterNot { warning ->
                        isAiImportWarning(warning) && aiWarningBelongsToQuestion(warning, currentQuestion)
                    }
                    syncEditableQuestions(nextQuestions, baseWarnings)
                    aiReviewSuggestions = aiReviewSuggestions.filterNot { it.questionId == suggestion.questionId }
                    statusText = "宸查噰绾?1 鏉?AI 鏍稿寤鸿锛屼繚瀛橀搴撳墠浠嶅彲缁х画浜哄伐璋冩暣銆"
                    isStatusWarn = false
                }
            },
            onDeleteQuestion = { deleteReviewQuestionAt(safeEditingIndex) },
            onBack = {
                reviewEditingIndex = null
                if (reviewEditFromFilterList) {
                    reviewFilterListFocusTick += 1
                }
                reviewEditFromFilterList = false
            }
        )
        return
    }

    if (reviewMode && importResult != null) {
        BackHandler {
            reviewEditingIndex = null
            reviewMode = false
        }
        val warnings = importResult?.warnings.orEmpty()
        val reviewFilter = reviewFilterFromName(reviewFilterName)
        NativeQuestionReviewScreen(
            questions = editableQuestions,
            warnings = warnings,
            aiSuggestions = aiReviewSuggestions,
            aiReviewedQuestionIds = aiReviewedQuestionIds,
            aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
            aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds,
            filter = reviewFilter,
            currentIndex = reviewIndex.coerceIn(0, (editableQuestions.size - 1).coerceAtLeast(0)),
            focusFilterListTick = reviewFilterListFocusTick,
            onFilterChange = { filter ->
                reviewFilterName = filter.name
                firstMatchingQuestionIndex(
                    questions = editableQuestions,
                    warnings = warnings,
                    aiSuggestions = aiReviewSuggestions,
                    aiReviewedQuestionIds = aiReviewedQuestionIds,
                    aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
                    aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds,
                    filter = filter
                )?.let { reviewIndex = it }
            },
            onIndexChange = { index ->
                if (editableQuestions.isNotEmpty()) {
                    reviewIndex = index.coerceIn(0, editableQuestions.lastIndex)
                }
            },
            onQuestionChange = { index, question ->
                syncEditableQuestions(
                    editableQuestions.mapIndexed { currentIndex, item ->
                        if (currentIndex == index) question else item
                    }
                )
            },
            onApplyAiSuggestion = { index, suggestion ->
                val currentQuestion = editableQuestions.getOrNull(index)
                if (currentQuestion != null && canApplyAiSuggestion(suggestion)) {
                    val nextQuestion = applyAiReviewSuggestion(currentQuestion, suggestion)
                    val nextQuestions = editableQuestions.mapIndexed { currentIndex, item ->
                        if (currentIndex == index) nextQuestion else item
                    }
                    val baseWarnings = importResult?.warnings.orEmpty().filterNot { warning ->
                        isAiImportWarning(warning) && aiWarningBelongsToQuestion(warning, currentQuestion)
                    }
                    syncEditableQuestions(nextQuestions, baseWarnings)
                    aiReviewSuggestions = aiReviewSuggestions.filterNot { it.questionId == suggestion.questionId }
                    statusText = "宸查噰绾?1 鏉?AI 鏍稿寤鸿锛屼繚瀛橀搴撳墠浠嶅彲缁х画浜哄伐璋冩暣銆"
                    isStatusWarn = false
                }
            },
            onDeleteQuestion = { index -> deleteReviewQuestionAt(index) },
            onEditQuestion = { index, focusFilterListOnBack ->
                if (editableQuestions.isNotEmpty()) {
                    reviewIndex = index.coerceIn(0, editableQuestions.lastIndex)
                    reviewEditingIndex = index.coerceIn(0, editableQuestions.lastIndex)
                    reviewEditFromFilterList = focusFilterListOnBack
                }
            },
            onBack = {
                reviewEditingIndex = null
                reviewMode = false
            }
        )
        return
    }

    val shouldPickAnswerFile = useDualImport && selectedFileName != "鏈€夋嫨鏂囦欢"

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shirohaEditorialBackground()
    ) {
        val screenClass = screenClassFor(maxWidth)
        val scale = editorialScaleFor(screenClass)
        val uiScale = uiScaleFor(screenClass)
        val importedBankCount = QuizRepository.banks.size
        val totalImportedQuestions = QuizRepository.banks.sumOf { it.questions.size }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            ShirohaHeader(
                kicker = "Import",
                title = "瀵煎叆棰樺簱",
                subtitle = "閫夋嫨棰樺簱鏂囦欢銆佺矘璐存枃鏈垨浣跨敤绀轰緥銆",
                scale = scale
            )

            // === 椤堕儴缁熻:宸插鍏ラ搴撴暟 / 鎬婚鐩暟 ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
            ) {
                EditorialFigure(
                    modifier = Modifier.weight(1f),
                    scale = scale,
                    value = "$importedBankCount",
                    label = "宸插鍏ラ搴",
                    unit = "涓"
                )
                EditorialFigure(
                    modifier = Modifier.weight(1f),
                    scale = scale,
                    value = "$totalImportedQuestions",
                    label = "鎬婚鐩暟",
                    unit = "棰"
                )
            }

            // === 寮曞鏂囨。閾炬帴 ===
            NoticeCard(
                text = "棣栨瀵煎叆锛熸煡鐪嬨€屾爣鍑嗘牸寮忚鏄庛€嶄簡瑙ｅ瓧娈靛惈涔夈€佽瘑鍒鍒欎笌绀轰緥銆",
                warning = false
            )

            // === 瀵煎叆鏂瑰紡:EditorialSection 鍖呰９ ===
            EditorialSection(
                    kicker = "Methods",
                    title = "瀵煎叆鏂瑰紡",
                    scale = scale
                ) {
                    GlassCard {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = ShirohaColors.CardWhite62,
                            border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
                        ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FileOpen,
                        contentDescription = "閫夋嫨棰樺簱鏂囦欢",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "閫夋嫨棰樺簱鏂囦欢",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "鏀寔 docx / txt / json / xlsx / csv",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (useDualImport) {
                    "褰撳墠棰樺簱鏂囦欢锛$selectedFileName\n褰撳墠绛旀鏂囦欢锛$selectedAnswerFileName"
                } else {
                    "褰撳墠鏂囦欢锛$selectedFileName"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionPillButton(
                    icon = Icons.Rounded.FileOpen,
                    text = if (shouldPickAnswerFile) "閫夋嫨绛旀鏂囦欢" else "閫夋嫨棰樺簱鏂囦欢",
                    primary = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    fillWidthContent = true,
                    onClick = {
                        if (!isImportBusy) {
                            if (shouldPickAnswerFile) {
                                answerFilePicker.launch(arrayOf("*/*"))
                            } else {
                                filePicker.launch(arrayOf("*/*"))
                            }
                        }
                    }
                )
                ActionPillButton(
                    icon = Icons.Rounded.Refresh,
                    text = "濉叆绀轰緥",
                    primary = false,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    fillWidthContent = true,
                    onClick = {
                        if (!isImportBusy) {
                            useDualImport = false
                            selectedFileName = "绀轰緥棰樺簱"
                            rawText = sampleImportText()
                            rawTextEditorExpanded = true
                            answerTextEditorExpanded = true
                            importedImages = emptyList()
                            clearParsedResult()
                            statusText = "宸插～鍏ョず渚嬮搴撱€"
                            isStatusWarn = false
                        }
                    }
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ImportModeChip(
                    icon = Icons.Rounded.Description,
                    text = "鏍囧噯瀵煎叆",
                    selected = !useDualImport,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    onClick = {
                        if (!isImportBusy) {
                            useDualImport = false
                            clearParsedResult()
                            statusText = "宸插垏鎹㈠埌鏍囧噯瀵煎叆銆"
                            isStatusWarn = false
                        }
                    }
                )
                ImportModeChip(
                    icon = Icons.Rounded.AutoAwesome,
                    text = "鍙屾枃浠跺鍏",
                    selected = useDualImport,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    onClick = {
                        if (!isImportBusy) {
                            useDualImport = true
                            clearParsedResult()
                            statusText = "宸插垏鎹㈠埌鍙屾枃浠跺鍏ャ€傚厛閫夋嫨棰樺簱鏂囦欢锛屽啀閫夋嫨绛旀鏂囦欢銆"
                            isStatusWarn = false
                        }
                    }
                )
            }
        }

        val generalStatusText = statusText.takeIf { !shouldShowAiStatusInImport(it) }
        if (generalStatusText != null && (isStatusWarn || !isImportBusy)) {
            NoticeCard(generalStatusText, warning = isStatusWarn)
        }

        if (isImportBusy) {
            LoadingIllustration(
                text = busyText.ifBlank { "姝ｅ湪澶勭悊瀵煎叆浠诲姟鈥︹€" },
                imageRes = R.drawable.illus_loading_state_webp
            )
        } else {
            GlassCard {
                val hasRawText = rawText.isNotBlank()
                val hasSelectedFile = selectedFileName != "鏈€夋嫨鏂囦欢"
                val rawTextTitle = when {
                    useDualImport -> "棰樼洰鏂囨湰"
                    hasRawText || hasSelectedFile -> "鍘熷鏂囨湰"
                    else -> "绮樿创瀵煎叆"
                }
                val rawTextHint = when {
                    useDualImport -> "棰樺簱鏂囦欢鍐呭锛屽彲鍦ㄨВ鏋愬墠鏍稿璋冩暣銆"
                    hasRawText || hasSelectedFile -> "鍙湪瑙ｆ瀽鍓嶆牳瀵规垨璋冩暣瀵煎叆鏂囨湰銆"
                    else -> "绮樿创棰樺簱鏂囨湰鍚庤В鏋愩€"
                }
                val rawTextActionButtonWidth = 128.dp
                val rawTextActionButtonHeight = ShirohaDimens.ActionButtonMinHeight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = rawTextTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = rawTextHint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    ActionPillButton(
                        icon = Icons.Rounded.PlayArrow,
                        text = "寮€濮嬭В鏋",
                        primary = true,
                        modifier = Modifier
                            .width(rawTextActionButtonWidth)
                            .height(rawTextActionButtonHeight),
                        fillWidthContent = true,
                        onClick = { startParse() }
                    )
                }
                Spacer(Modifier.height(12.dp))
                if (!rawTextEditorExpanded && rawText.length > LARGE_TEXT_PREVIEW_THRESHOLD) {
                    LargeImportTextPreview(
                        text = rawText,
                        label = "棰樼洰鏂囨湰杈冮暱锛屽凡鏀惰捣鍏ㄦ枃缂栬緫浠ュ噺灏戝崱椤裤€",
                        showEditButton = false,
                        onEditFullText = { rawFullEditorMode = true }
                    )
                } else {
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = {
                            rawText = it
                            rawTextEditorExpanded = it.length <= LARGE_TEXT_PREVIEW_THRESHOLD
                            if (importResult != null || editableQuestions.isNotEmpty() || reviewMode || importedImages.isNotEmpty()) {
                                clearParsedResult(clearImages = true)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                when {
                                    useDualImport -> 160.dp
                                    hasRawText || hasSelectedFile -> 190.dp
                                    else -> 104.dp
                                }
                            ),
                        enabled = true,
                        minLines = when {
                            useDualImport -> 7
                            hasRawText || hasSelectedFile -> 9
                            else -> 4
                        },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        placeholder = { Text("鎶婃爣鍑嗛搴撴枃鏈矘璐村埌杩欓噷锛屾垨閫氳繃涓婃柟閫夋嫨鏂囦欢瀵煎叆銆") }
                    )
                }
                if (hasRawText) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        ActionPillButton(
                            icon = Icons.Rounded.Edit,
                            text = "缂栬緫鍏ㄦ枃",
                            primary = false,
                            modifier = Modifier
                                .width(rawTextActionButtonWidth)
                                .height(rawTextActionButtonHeight),
                            fillWidthContent = true,
                            onClick = { rawFullEditorMode = true }
                        )
                    }
                }

                if (useDualImport) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "绛旀鏂囨湰",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))
                    if (!answerTextEditorExpanded && answerText.length > LARGE_TEXT_PREVIEW_THRESHOLD) {
                        LargeImportTextPreview(
                            text = answerText,
                            label = "绛旀鏂囨湰杈冮暱锛屽凡鏀惰捣鍏ㄦ枃缂栬緫浠ュ噺灏戝崱椤裤€",
                            onEditFullText = { answerFullEditorMode = true }
                        )
                    } else {
                        OutlinedTextField(
                            value = answerText,
                            onValueChange = {
                                answerText = it
                                answerTextEditorExpanded = it.length <= LARGE_TEXT_PREVIEW_THRESHOLD
                                if (importResult != null || editableQuestions.isNotEmpty() || reviewMode) {
                                    clearParsedResult()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            enabled = true,
                            minLines = 7,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            placeholder = { Text("绮樿创绛旀鏂囨湰锛屾垨閫氳繃涓婃柟鎸夐挳閫夋嫨绛旀鏂囦欢銆") }
                        )
                    }
                }
            }

            importResult?.let { result ->
                val displayResult = result.copy(questions = editableQuestions)
                NativeImportSummary(
                    result = displayResult,
                    aiSuggestions = aiReviewSuggestions,
                    aiReviewedQuestionIds = aiReviewedQuestionIds,
                    aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
                    aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds
                )

                val warnings = importResult?.warnings.orEmpty()
                val anomalyQuestions = editableQuestions.filter { question ->
                    questionMatchesFilter(question, warningsForQuestion(question, warnings), ReviewFilter.ANOMALY)
                }
                val aiSuggestionCount = editableQuestions.count { question ->
                    aiSuggestionsForQuestion(question, aiReviewSuggestions).any(::isActionableAiSuggestion)
                }
                val aiApplicableCount = editableQuestions.count { question ->
                    aiSuggestionsForQuestion(question, aiReviewSuggestions).any(::canApplyAiSuggestion)
                }
                val missingAnalysisCount = editableQuestions.count(::shouldApplyAiAnalysis)
                val aiAnalysisAppliedCount = editableQuestions.count { it.id in aiAnalysisAppliedQuestionIds.toSet() }
                val previewQuestions = if (previewOnlyAnomaly) anomalyQuestions else editableQuestions

                GlassCard {
                    Text(
                        text = "鏍稿涓庡啓鍏",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "浜哄伐鏍稿锛屽彲鐢ˋI杈呭姪锛屾渶鍚庣‘璁や繚瀛",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "浜哄伐鏍稿",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionPillButton(
                            icon = Icons.Rounded.Edit,
                            text = "杩涘叆娌夋蹈鏍稿",
                            primary = false,
                            onClick = {
                                if (editableQuestions.isNotEmpty()) {
                                    reviewFilterName = ReviewFilter.ALL.name
                                    reviewIndex = reviewIndex.coerceIn(0, editableQuestions.lastIndex)
                                    reviewMode = true
                                }
                            }
                        )
                        ActionPillButton(
                            icon = Icons.Rounded.CheckCircle,
                            text = if (previewOnlyAnomaly) "鏄剧ず鍏ㄩ儴棰? else "浠呯湅寮傚父棰?,
                            primary = previewOnlyAnomaly,
                            onClick = {
                                val nextOnlyAnomaly = !previewOnlyAnomaly
                                previewOnlyAnomaly = nextOnlyAnomaly
                                statusText = if (nextOnlyAnomaly) {
                                    "蹇€熼瑙堝凡鍒囨崲涓轰粎鏄剧ず寮傚父棰橈紝鍏?${anomalyQuestions.size} 棰樸€"
                                } else {
                                    "蹇€熼瑙堝凡鍒囨崲涓哄叏閮ㄩ鐩€"
                                }
                                isStatusWarn = false
                            }
                        )
                        ActionPillButton(
                            icon = Icons.Rounded.Description,
                            text = "鐪嬬己瑙ｆ瀽 $missingAnalysisCount",
                            primary = false,
                            enabled = missingAnalysisCount > 0,
                            onClick = {
                                reviewFilterName = ReviewFilter.MISSING_ANALYSIS.name
                                firstMatchingQuestionIndex(
                                    questions = editableQuestions,
                                    warnings = warnings,
                                    aiSuggestions = aiReviewSuggestions,
                                    aiReviewedQuestionIds = aiReviewedQuestionIds,
                                    aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
                                    aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds,
                                    filter = ReviewFilter.MISSING_ANALYSIS
                                )?.let { reviewIndex = it }
                                reviewMode = true
                            }
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "AI 杈呭姪",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionPillButton(
                            icon = Icons.Rounded.AutoAwesome,
                            text = "AI閲嶆瀯",
                            primary = QuizRepository.isAiConfigured() && QuizRepository.aiRefactorEnabled,
                            modifier = Modifier.alpha(if (QuizRepository.isAiConfigured() && QuizRepository.aiRefactorEnabled) 1f else ShirohaDimens.DisabledAlpha),
                            enabled = editableQuestions.isNotEmpty() && rawText.isNotBlank() && !isImportBusy,
                            onClick = {
                                if (!QuizRepository.isAiConfigured()) {
                                    showAiConfigPrompt = true
                                    statusText = "AI 閲嶆瀯锛氳鍏堝湪涓汉鍋忓ソ 鈫?AI 璁剧疆涓厤缃帴鍙ｃ€"
                                    isStatusWarn = true
                                    return@ActionPillButton
                                }
                                if (!QuizRepository.aiRefactorEnabled) {
                                    statusText = "AI 閲嶆瀯鏈惎鐢紝璇峰厛鍦ㄤ釜浜哄亸濂?鈫?AI 璁剧疆涓紑鍚€"
                                    isStatusWarn = true
                                    return@ActionPillButton
                                }
                                val sourceText = rawText.trim()
                                val answerSourceText = answerText.trim()
                                if (sourceText.isBlank()) {
                                    statusText = "AI 閲嶆瀯闇€瑕佸師濮嬫枃鏈紝璇峰厛瀵煎叆鎴栫矘璐撮搴撴枃鏈€"
                                    isStatusWarn = true
                                    return@ActionPillButton
                                }
                                val sourceLength = sourceText.length + answerSourceText.length
                                if (sourceLength > QuizRepository.aiRefactorMaxChars) {
                                    statusText = "AI 閲嶆瀯锛氬師鏂囩害 ${sourceLength} 瀛楋紝瓒呰繃褰撳墠涓婇檺 ${QuizRepository.aiRefactorMaxChars} 瀛椼€傝鍦?AI 璁剧疆涓皟澶т笂闄愶紝鎴栨媶鍒嗛搴撳悗澶勭悊銆"
                                    isStatusWarn = true
                                    return@ActionPillButton
                                }
                                val warningTexts = displayResult.warnings.map { warning ->
                                    val numberText = warning.questionNumber?.takeIf { it.isNotBlank() }?.let { "棰樺彿$it锛" }.orEmpty()
                                    "${warning.level.name}锛?numberText${warning.message}"
                                }.distinct().take(120)
                                val beforeCount = editableQuestions.size
                                statusText = "AI 閲嶆瀯涓細浼樺厛娓呮礂鍘熸枃骞堕噸鏂版湰鍦拌В鏋愶紝蹇呰鏃跺啀浣跨敤 AI 鐩存帴閲嶆瀯缁撴灉銆"
                                isStatusWarn = false
                                importScope.launch {
                                    isImportBusy = true
                                    busyText = "AI 閲嶆瀯涓€︹€"
                                    runCatching {
                                        withContext(Dispatchers.IO) {
                                            val refactorResult = ShirohaAiClient.refactorQuestions(
                                                apiBaseUrl = QuizRepository.aiApiBaseUrl,
                                                apiKey = QuizRepository.aiApiKey,
                                                modelName = QuizRepository.aiModelName,
                                                rawText = sourceText,
                                                answerText = answerSourceText,
                                                currentQuestions = editableQuestions,
                                                warnings = warningTexts,
                                                timeoutSeconds = QuizRepository.aiTimeoutSeconds
                                            )
                                            val cleanedText = refactorResult.cleanedText.orEmpty().trim()
                                            val cleanedAnswerText = refactorResult.cleanedAnswerText.orEmpty().trim()
                                            val reparsedResult = if (cleanedText.isNotBlank()) {
                                                val parsed = if (cleanedAnswerText.isNotBlank()) {
                                                    QuizImportParser.parseDualText(cleanedText, cleanedAnswerText)
                                                } else {
                                                    QuizImportParser.parseStandardText(cleanedText)
                                                }
                                                if (importedImages.isNotEmpty()) {
                                                    QuestionImageBinder.attach(parsed, importedImages)
                                                } else {
                                                    parsed
                                                }
                                            } else {
                                                null
                                            }
                                            AiRefactorApplyResult(refactorResult = refactorResult, reparsedResult = reparsedResult)
                                        }
                                    }.onSuccess { applyResult ->
                                        val refactorResult = applyResult.refactorResult
                                        val reparsedResult = applyResult.reparsedResult
                                        val directQuestions = refactorResult.questions
                                        val shouldUseReparsed = reparsedResult != null && reparsedResult.questions.isNotEmpty() &&
                                            (directQuestions.isEmpty() || reparsedResult.questions.size >= directQuestions.size || reparsedResult.questions.size >= beforeCount)

                                        if (shouldUseReparsed && reparsedResult != null) {
                                            val refactoredQuestions = reparsedResult.questions
                                            val nextWarnings = refreshImportWarningsForQuestions(
                                                aiRefactorImportWarnings(refactorResult.notes, "AI閲嶆瀯宸叉竻娲楀師鏂囧苟閲嶆柊鏈湴瑙ｆ瀽锛岃浜哄伐纭棰橀噺銆侀骞层€侀€夐」銆佺瓟妗堝拰瑙ｆ瀽鍚庡啀淇濆瓨銆") +
                                                    reparsedResult.warnings,
                                                refactoredQuestions
                                            )
                                            val nextResult = reparsedResult.copy(
                                                strategyName = "AI閲嶆瀯閲嶈В鏋?+ ${reparsedResult.strategyName}",
                                                warnings = nextWarnings,
                                                diagnostics = reparsedResult.diagnostics.copy(
                                                    notes = (reparsedResult.diagnostics.notes + "AI閲嶆瀯锛氬凡娓呮礂鍘熸枃骞堕噸鏂版湰鍦拌В鏋愶紝鐢?$beforeCount 棰樿В鏋愪负 ${refactoredQuestions.size} 棰樸€" + refactorResult.notes).distinct()
                                                )
                                            )
                                            importResult = nextResult
                                            editableQuestions = refactoredQuestions
                                            reviewIndex = 0
                                            reviewFilterName = ReviewFilter.ALL.name
                                            previewOnlyAnomaly = false
                                            aiReviewedQuestionIds = emptyList()
                                            aiAnalyzedQuestionIds = emptyList()
                                            aiAnalysisAppliedQuestionIds = emptyList()
                                            aiReviewSuggestions = emptyList()
                                            statusText = "AI 閲嶆瀯瀹屾垚锛氬凡娓呮礂鍘熸枃骞堕噸鏂版湰鍦拌В鏋愶紝鐢?$beforeCount 棰樺緱鍒?${refactoredQuestions.size} 棰樸€傝鍏堜汉宸ユ牳瀵癸紝鍐嶇户缁?AI 鏍稿鎴?AI 瑙ｆ瀽銆"
                                            isStatusWarn = nextWarnings.isNotEmpty()
                                        } else if (directQuestions.isNotEmpty()) {
                                            val refactoredQuestions = directQuestions
                                            val nextWarnings = refreshImportWarningsForQuestions(
                                                aiRefactorImportWarnings(refactorResult.notes, "AI閲嶆瀯宸茬敓鎴愭柊鐨勫緟鏍稿缁撴灉锛岃浜哄伐纭棰橀噺銆侀骞层€侀€夐」銆佺瓟妗堝拰瑙ｆ瀽鍚庡啀淇濆瓨銆"),
                                                refactoredQuestions
                                            )
                                            val nextResult = displayResult.copy(
                                                questions = refactoredQuestions,
                                                strategyName = "${displayResult.strategyName} + AI閲嶆瀯",
                                                warnings = nextWarnings,
                                                diagnostics = displayResult.diagnostics.copy(
                                                    notes = (displayResult.diagnostics.notes + "AI閲嶆瀯锛氱敱 $beforeCount 棰橀噸鏁翠负 ${refactoredQuestions.size} 棰樸€" + refactorResult.notes).distinct()
                                                )
                                            )
                                            importResult = nextResult
                                            editableQuestions = refactoredQuestions
                                            reviewIndex = 0
                                            reviewFilterName = ReviewFilter.ALL.name
                                            previewOnlyAnomaly = false
                                            aiReviewedQuestionIds = emptyList()
                                            aiAnalyzedQuestionIds = emptyList()
                                            aiAnalysisAppliedQuestionIds = emptyList()
                                            aiReviewSuggestions = emptyList()
                                            statusText = "AI 閲嶆瀯瀹屾垚锛氱敱 $beforeCount 棰橀噸鏁翠负 ${refactoredQuestions.size} 棰樸€傝鍏堜汉宸ユ牳瀵癸紝鍐嶇户缁?AI 鏍稿鎴?AI 瑙ｆ瀽銆"
                                            isStatusWarn = nextWarnings.isNotEmpty()
                                        } else if (reparsedResult != null) {
                                            statusText = "AI 閲嶆瀯宸茶繑鍥炴竻娲楁枃鏈紝浣嗘湰鍦伴噸瑙ｆ瀽鏈緱鍒板彲鐢ㄩ鐩紝褰撳墠寰呮牳瀵圭粨鏋滄湭鏀瑰姩銆"
                                            isStatusWarn = true
                                        } else {
                                            statusText = "AI 閲嶆瀯瀹屾垚浣嗘病鏈夎繑鍥炲彲鐢ㄦ竻娲楁枃鏈垨棰樼洰锛屽綋鍓嶅緟鏍稿缁撴灉鏈敼鍔ㄣ€"
                                            isStatusWarn = true
                                        }
                                    }.onFailure { error ->
                                        statusText = "AI 閲嶆瀯澶辫触锛${error.message ?: "璇锋鏌ユ帴鍙ｉ厤缃"}"
                                        isStatusWarn = true
                                    }
                                    isImportBusy = false
                                    busyText = ""
                                }
                            }
                        )
                        ActionPillButton(
                            icon = Icons.Rounded.AutoAwesome,
                            text = "AI鏍稿",
                            primary = QuizRepository.isAiConfigured() && QuizRepository.aiReviewEnabled,
                            modifier = Modifier.alpha(if (QuizRepository.isAiConfigured() && QuizRepository.aiReviewEnabled) 1f else ShirohaDimens.DisabledAlpha),
                            enabled = editableQuestions.isNotEmpty() && !isImportBusy,
                            onClick = {
                                if (!QuizRepository.isAiConfigured()) {
                                    showAiConfigPrompt = true
                                    statusText = "AI 鏍稿锛氳鍏堝湪涓汉鍋忓ソ 鈫?AI 璁剧疆涓厤缃帴鍙ｃ€"
                                    isStatusWarn = true
                                    return@ActionPillButton
                                }
                                if (!QuizRepository.aiReviewEnabled) {
                                    statusText = "AI 鏍稿鏈惎鐢紝璇峰厛鍦ㄤ釜浜哄亸濂?鈫?AI 璁剧疆涓紑鍚€"
                                    isStatusWarn = true
                                    return@ActionPillButton
                                }
                                val baseReviewQuestions = if (QuizRepository.aiOnlyAnomaly) anomalyQuestions else editableQuestions
                                val reviewedIdSet = aiReviewedQuestionIds.toSet()
                                val remainingReviewQuestions = baseReviewQuestions.filterNot { it.id in reviewedIdSet }
                                val limitedQuestions = remainingReviewQuestions.take(QuizRepository.aiMaxQuestions)
                                if (baseReviewQuestions.isEmpty()) {
                                    statusText = if (QuizRepository.aiOnlyAnomaly) {
                                        "AI 鏍稿锛氬綋鍓嶅紑鍚簡浠呭鐞嗗紓甯搁锛屼絾娌℃湁鍙牳瀵圭殑寮傚父棰樸€"
                                    } else {
                                        "AI 鏍稿锛氬綋鍓嶆病鏈夊彲渚涙牳瀵圭殑棰樼洰銆"
                                    }
                                    isStatusWarn = true
                                    return@ActionPillButton
                                }
                                if (limitedQuestions.isEmpty()) {
                                    statusText = "AI 鏍稿锛氬綋鍓嶈寖鍥村凡鍏ㄩ儴澶勭悊銆傚闇€閲嶆柊鏍稿锛岃閲嶆柊瑙ｆ瀽棰樺簱鎴栧垏鎹㈠鐞嗚寖鍥淬€"
                                    isStatusWarn = false
                                    return@ActionPillButton
                                }
                                val processedBefore = baseReviewQuestions.count { it.id in reviewedIdSet }.coerceAtMost(baseReviewQuestions.size)
                                statusText = "AI 鏍稿涓細鏈澶勭悊 ${limitedQuestions.size} 棰橈紝杩涘害 ${processedBefore + 1}-${processedBefore + limitedQuestions.size}/${baseReviewQuestions.size}銆"
                                isStatusWarn = false
                                importScope.launch {
                                    isImportBusy = true
                                    busyText = "AI 鏍稿涓€︹€"
                                    runCatching {
                                        withContext(Dispatchers.IO) {
                                            ShirohaAiClient.reviewQuestions(
                                                apiBaseUrl = QuizRepository.aiApiBaseUrl,
                                                apiKey = QuizRepository.aiApiKey,
                                                modelName = QuizRepository.aiModelName,
                                                questions = limitedQuestions,
                                                timeoutSeconds = QuizRepository.aiTimeoutSeconds
                                            )
                                        }
                                    }.onSuccess { suggestions ->
                                        aiReviewSuggestions = mergeAiReviewSuggestions(aiReviewSuggestions, suggestions, limitedQuestions)
                                        val aiWarnings = suggestionsToImportWarnings(suggestions, editableQuestions)
                                        importResult = displayResult.copy(
                                            warnings = mergeAiWarnings(
                                                currentWarnings = displayResult.warnings,
                                                aiWarnings = aiWarnings,
                                                processedQuestions = limitedQuestions
                                            )
                                        )
                                        val nextReviewedIds = (aiReviewedQuestionIds + limitedQuestions.map { it.id }).distinct()
                                        aiReviewedQuestionIds = nextReviewedIds
                                        val processedAfter = baseReviewQuestions.count { it.id in nextReviewedIds.toSet() }.coerceAtMost(baseReviewQuestions.size)
                                        previewOnlyAnomaly = aiWarnings.isNotEmpty() || previewOnlyAnomaly
                                        statusText = if (aiWarnings.isEmpty()) {
                                            "AI 鏍稿瀹屾垚锛氭湰鎵规湭鍙戠幇閲嶇偣闂锛屽凡澶勭悊 ${processedAfter}/${baseReviewQuestions.size} 棰樸€"
                                        } else {
                                            "AI 鏍稿瀹屾垚锛氭湰鎵圭敓鎴?${aiWarnings.size} 鏉″缓璁紝宸插鐞?${processedAfter}/${baseReviewQuestions.size} 棰樸€"
                                        } + if (processedAfter < baseReviewQuestions.size) " 鍙户缁偣鍑?AI 鏍稿澶勭悊涓嬩竴鎵广€? else " 褰撳墠鑼冨洿宸插鐞嗗畬銆?
                                        isStatusWarn = aiWarnings.isNotEmpty()
                                    }.onFailure { error ->
                                        statusText = "AI 鏍稿澶辫触锛${error.message ?: "璇锋鏌ユ帴鍙ｉ厤缃"}"
                                        isStatusWarn = true
                                    }
                                    isImportBusy = false
                                    busyText = ""
                                }
                            }
                        )
                        ActionPillButton(
                            icon = Icons.Rounded.AutoAwesome,
                            text = "AI瑙ｆ瀽",
                            primary = QuizRepository.isAiConfigured() && QuizRepository.aiAnalysisEnabled,
                            modifier = Modifier.alpha(if (QuizRepository.isAiConfigured() && QuizRepository.aiAnalysisEnabled) 1f else ShirohaDimens.DisabledAlpha),
                            enabled = editableQuestions.isNotEmpty() && !isImportBusy,
                            onClick = {
                                if (!QuizRepository.isAiConfigured()) {
                                    showAiConfigPrompt = true
                                    statusText = "AI 瑙ｆ瀽锛氳鍏堝湪涓汉鍋忓ソ 鈫?AI 璁剧疆涓厤缃帴鍙ｃ€"
                                    isStatusWarn = true
                                    return@ActionPillButton
                                }
                                if (!QuizRepository.aiAnalysisEnabled) {
                                    statusText = "AI 瑙ｆ瀽鏈惎鐢紝璇峰厛鍦ㄤ釜浜哄亸濂?鈫?AI 璁剧疆涓紑鍚€"
                                    isStatusWarn = true
                                    return@ActionPillButton
                                }
                                val allAnalysisTargets = editableQuestions.filter(::shouldApplyAiAnalysis)
                                val anomalyAnalysisTargets = anomalyQuestions.filter(::shouldApplyAiAnalysis)
                                if (allAnalysisTargets.isEmpty()) {
                                    statusText = "AI 瑙ｆ瀽锛氬綋鍓嶆病鏈夌己灏戣В鏋愭垨瑙ｆ瀽杩囩煭鐨勯鐩€"
                                    isStatusWarn = false
                                    return@ActionPillButton
                                }
                                val analyzedIdSet = aiAnalyzedQuestionIds.toSet()
                                val remainingAnomalyTargets = anomalyAnalysisTargets.filterNot { it.id in analyzedIdSet }
                                val remainingAllTargets = allAnalysisTargets.filterNot { it.id in analyzedIdSet }
                                val useAnomalyScope = QuizRepository.aiOnlyAnomaly && remainingAnomalyTargets.isNotEmpty()
                                val usingFallbackTargets = QuizRepository.aiOnlyAnomaly && !useAnomalyScope && remainingAllTargets.isNotEmpty()
                                val analysisTargetPool = if (useAnomalyScope) anomalyAnalysisTargets else allAnalysisTargets
                                val remainingAnalysisTargets = if (useAnomalyScope) remainingAnomalyTargets else remainingAllTargets
                                if (remainingAnalysisTargets.isEmpty()) {
                                    statusText = "AI 瑙ｆ瀽锛氬綋鍓嶇己瑙ｆ瀽棰樺凡鍏ㄩ儴灏濊瘯銆傝嫢浠嶆湁棰樼洰缂鸿В鏋愶紝鍙兘鏄ā鍨嬫湭杩斿洖瀵瑰簲缁撴灉锛涘彲閲嶆柊瑙ｆ瀽棰樺簱鎴栬皟鏁村崟娆￠鏁板悗閲嶈瘯銆"
                                    isStatusWarn = false
                                    return@ActionPillButton
                                }
                                val aiTargetQuestions = remainingAnalysisTargets.take(QuizRepository.aiMaxQuestions)
                                val processedBefore = analysisTargetPool.count { it.id in analyzedIdSet }.coerceAtMost(analysisTargetPool.size)
                                statusText = if (usingFallbackTargets) {
                                    "AI 瑙ｆ瀽涓細寮傚父棰樿寖鍥村凡鏃犳湭灏濊瘯瑙ｆ瀽鐩爣锛屽凡鏀逛负澶勭悊鍏ㄩ儴缂鸿В鏋愰锛涙湰娆″鐞?${aiTargetQuestions.size} 閬擄紝杩涘害 ${processedBefore + 1}-${processedBefore + aiTargetQuestions.size}/${analysisTargetPool.size}銆"
                                } else {
                                    "AI 瑙ｆ瀽涓細鏈澶勭悊 ${aiTargetQuestions.size} 閬撶己瑙ｆ瀽棰橈紝杩涘害 ${processedBefore + 1}-${processedBefore + aiTargetQuestions.size}/${analysisTargetPool.size}銆"
                                }
                                isStatusWarn = false
                                importScope.launch {
                                    isImportBusy = true
                                    busyText = "AI 瑙ｆ瀽鐢熸垚涓€︹€"
                                    runCatching {
                                        withContext(Dispatchers.IO) {
                                            ShirohaAiClient.generateAnalysis(
                                                apiBaseUrl = QuizRepository.aiApiBaseUrl,
                                                apiKey = QuizRepository.aiApiKey,
                                                modelName = QuizRepository.aiModelName,
                                                questions = aiTargetQuestions,
                                                timeoutSeconds = QuizRepository.aiTimeoutSeconds
                                            )
                                        }
                                    }.onSuccess { suggestions ->
                                        val suggestionMap = suggestions.associateBy { it.questionId }
                                        val appliedIds = mutableListOf<String>()
                                        val nextAnalyzedIds = (aiAnalyzedQuestionIds + aiTargetQuestions.map { it.id }).distinct()
                                        val nextQuestions = editableQuestions.map { question ->
                                            val suggestion = suggestionMap[question.id]
                                            if (suggestion != null && shouldApplyAiAnalysis(question) && suggestion.analysis.trim().isNotBlank()) {
                                                appliedIds += question.id
                                                applyAiAnalysisSuggestion(question, suggestion.analysis)
                                            } else {
                                                question
                                            }
                                        }
                                        syncEditableQuestions(nextQuestions, displayResult.warnings)
                                        aiAnalyzedQuestionIds = nextAnalyzedIds
                                        aiAnalysisAppliedQuestionIds = (aiAnalysisAppliedQuestionIds + appliedIds).distinct()
                                        val changedIds = appliedIds.toSet()
                                        val nextAnalyzedIdSet = nextAnalyzedIds.toSet()
                                        val skippedCount = (aiTargetQuestions.size - suggestionMap.keys.size).coerceAtLeast(0)
                                        val remainingAnalysisCount = editableQuestions.count { question ->
                                            shouldApplyAiAnalysis(question) && question.id !in nextAnalyzedIdSet
                                        }
                                        statusText = if (changedIds.isEmpty()) {
                                            "AI 瑙ｆ瀽瀹屾垚锛氭湰鎵规病鏈夊彲鍐欏叆鐨勮В鏋愬缓璁€"
                                        } else {
                                            "AI 瑙ｆ瀽瀹屾垚锛氬凡涓?${changedIds.size} 閬撻鍐欏叆寰呮牳瀵硅В鏋愶紝淇濆瓨鍓嶈浜哄伐纭銆"
                                        } + if (skippedCount > 0) {
                                            " 鏈壒鏈?${skippedCount} 閬撴湭杩斿洖瑙ｆ瀽锛屽凡璺宠繃浠ラ伩鍏嶅弽澶嶅崱浣忋€"
                                        } else {
                                            ""
                                        } + if (remainingAnalysisCount > 0) {
                                            " 浠嶆湁绾?${remainingAnalysisCount} 閬撶己瑙ｆ瀽棰橈紝鍙户缁偣鍑?AI 瑙ｆ瀽澶勭悊涓嬩竴鎵广€"
                                        } else {
                                            " 褰撳墠鑼冨洿宸插鐞嗗畬銆"
                                        }
                                        isStatusWarn = false
                                    }.onFailure { error ->
                                        statusText = "AI 瑙ｆ瀽澶辫触锛${error.message ?: "璇锋鏌ユ帴鍙ｉ厤缃"}"
                                        isStatusWarn = true
                                    }
                                    isImportBusy = false
                                    busyText = ""
                                }
                            }
                        )

                        ActionPillButton(
                            icon = Icons.Rounded.CheckCircle,
                            text = "鐪婣I寤鸿 $aiSuggestionCount",
                            primary = aiSuggestionCount > 0,
                            enabled = aiSuggestionCount > 0,
                            onClick = {
                                reviewFilterName = ReviewFilter.AI_SUGGESTION.name
                                firstMatchingQuestionIndex(
                                    questions = editableQuestions,
                                    warnings = warnings,
                                    aiSuggestions = aiReviewSuggestions,
                                    aiReviewedQuestionIds = aiReviewedQuestionIds,
                                    aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
                                    aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds,
                                    filter = ReviewFilter.AI_SUGGESTION
                                )?.let { reviewIndex = it }
                                reviewMode = true
                            }
                        )
                        ActionPillButton(
                            icon = Icons.Rounded.CheckCircle,
                            text = "鐪嬪彲閲囩撼 $aiApplicableCount",
                            primary = aiApplicableCount > 0,
                            enabled = aiApplicableCount > 0,
                            onClick = {
                                reviewFilterName = ReviewFilter.AI_APPLICABLE.name
                                firstMatchingQuestionIndex(
                                    questions = editableQuestions,
                                    warnings = warnings,
                                    aiSuggestions = aiReviewSuggestions,
                                    aiReviewedQuestionIds = aiReviewedQuestionIds,
                                    aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
                                    aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds,
                                    filter = ReviewFilter.AI_APPLICABLE
                                )?.let { reviewIndex = it }
                                reviewMode = true
                            }
                        )
                        ActionPillButton(
                            icon = Icons.Rounded.Description,
                            text = "鐪婣I琛ヨВ鏋?$aiAnalysisAppliedCount",
                            primary = aiAnalysisAppliedCount > 0,
                            enabled = aiAnalysisAppliedCount > 0,
                            onClick = {
                                reviewFilterName = ReviewFilter.AI_ANALYZED.name
                                firstMatchingQuestionIndex(
                                    questions = editableQuestions,
                                    warnings = warnings,
                                    aiSuggestions = aiReviewSuggestions,
                                    aiReviewedQuestionIds = aiReviewedQuestionIds,
                                    aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
                                    aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds,
                                    filter = ReviewFilter.AI_ANALYZED
                                )?.let { reviewIndex = it }
                                reviewMode = true
                            }
                        )
                    }
                    val aiStatusText = statusText.takeIf { shouldShowAiStatusInImport(it) }
                    if (aiStatusText != null) {
                        Spacer(Modifier.height(10.dp))
                        NoticeCard(aiStatusText, warning = isStatusWarn)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "纭鍐欏叆",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionPillButton(
                            icon = Icons.Rounded.Save,
                            text = "鏂板缓棰樺簱",
                            primary = saveMode == ImportSaveMode.NEW_BANK.name,
                            modifier = Modifier.weight(1f),
                            fillWidthContent = true,
                            onClick = { saveMode = ImportSaveMode.NEW_BANK.name }
                        )
                        ActionPillButton(
                            icon = Icons.Rounded.Add,
                            text = "杩藉姞棰樼洰",
                            primary = saveMode == ImportSaveMode.APPEND_TO_BANK.name,
                            modifier = Modifier.weight(1f),
                            fillWidthContent = true,
                            onClick = {
                                saveMode = ImportSaveMode.APPEND_TO_BANK.name
                                if (appendTargetBankId == null) {
                                    appendTargetBankId = QuizRepository.activeBank()?.id ?: QuizRepository.banks.firstOrNull()?.id
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    if (saveMode == ImportSaveMode.NEW_BANK.name) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = newBankGroupName,
                                onValueChange = { newBankGroupName = it },
                                label = { Text("涓€绾у垎缁") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newBankName,
                                onValueChange = { newBankName = it },
                                label = { Text("浜岀骇棰樺簱鍚") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            ActionPillButton(
                                icon = Icons.Rounded.Save,
                                text = "淇濆瓨鏂伴搴",
                                primary = true,
                                onClick = {
                                    val cleanGroupName = newBankGroupName.trim().ifBlank { DEFAULT_BANK_GROUP_NAME }
                                    val bankName = newBankName.trim().ifBlank { defaultImportBankName(selectedFileName) }
                                    QuizRepository.importBank(context, bankName, editableQuestions, cleanGroupName)
                                    statusText = "宸叉柊寤洪搴擄細$cleanGroupName / $bankName锛屽叡 ${editableQuestions.size} 棰樸€"
                                    isStatusWarn = false
                                    onImportSaved()
                                }
                            )
                        }
                    } else {
                        val appendTargetBank = QuizRepository.banks.firstOrNull { it.id == appendTargetBankId }
                            ?: QuizRepository.activeBank()
                            ?: QuizRepository.banks.firstOrNull()
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            NoticeCard("杩藉姞瀵煎叆浼氫繚鐣欐棫棰樺苟娣诲姞鏂伴锛涙湰鐗堜笉浼氳嚜鍔ㄥ幓閲嶏紝涔熶笉浼氭浛鎹㈠師棰樺簱銆", warning = false)
                            if (QuizRepository.banks.isEmpty()) {
                                NoticeCard("褰撳墠娌℃湁鍙拷鍔犵殑鏃ч搴擄紝璇峰厛淇濆瓨涓烘柊棰樺簱銆", warning = true)
                            } else {
                                Text(
                                    text = "閫夋嫨鐩爣棰樺簱",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    QuizRepository.banks.forEach { bank ->
                                        val selected = bank.id == appendTargetBank?.id
                                        ActionPillButton(
                                            icon = Icons.Rounded.Done,
                                            text = bankDisplayPath(bank.groupName, bank.name),
                                            primary = selected,
                                            onClick = { appendTargetBankId = bank.id }
                                        )
                                    }
                                }
                                ActionPillButton(
                                    icon = Icons.Rounded.Add,
                                    text = "杩藉姞鍒伴€変腑棰樺簱",
                                    primary = true,
                                    enabled = appendTargetBank != null,
                                    onClick = {
                                        val target = appendTargetBank ?: return@ActionPillButton
                                        val oldCount = target.questions.size
                                        val success = QuizRepository.appendQuestionsToBank(context, target.id, editableQuestions)
                                        statusText = if (success) {
                                            "宸茶拷鍔犲埌锛${bankDisplayPath(target.groupName, target.name)}锛屾柊澧?${editableQuestions.size} 棰橈紝褰撳墠鍏?${oldCount + editableQuestions.size} 棰樸€"
                                        } else {
                                            "杩藉姞澶辫触锛氭病鏈夋壘鍒扮洰鏍囬搴撱€"
                                        }
                                        isStatusWarn = !success
                                        if (success) onImportSaved()
                                    }
                                )
                            }
                        }
                    }
                }

                NativeImportPreview(
                    questions = previewQuestions,
                    totalQuestionCount = editableQuestions.size,
                    onlyShowAnomaly = previewOnlyAnomaly,
                    aiSuggestions = aiReviewSuggestions,
                    aiReviewedQuestionIds = aiReviewedQuestionIds,
                    aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
                    aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds
                )
            }

            if (importResult == null && rawText.isNotBlank()) {
                LoadingIllustration(
                    text = "鍑嗗濂戒互鍚庯紝鐐瑰嚮鈥滃紑濮嬭В鏋愨€濄€",
                    imageRes = R.drawable.illus_loading_state_webp
                )
            }

            // === 鏈€杩戝鍏$EditorialSection 鍖呰９ ===
            EditorialSection(
                kicker = "Recent",
                title = "鏈€杩戝鍏",
                scale = scale
            ) {
                if (QuizRepository.banks.isEmpty()) {
                    Text(
                        text = "鏆傛棤宸插鍏ラ搴?瀹屾垚棣栨瀵煎叆鍚庝細鍦ㄨ繖閲屽垪鍑恒€",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShirohaColors.TextSecondary
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm)) {
                        QuizRepository.banks.takeLast(5).reversed().forEach { bank ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bank.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${bank.groupName.ifBlank { DEFAULT_BANK_GROUP_NAME }} 路 ${bank.questions.size} 棰",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ShirohaColors.TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                StatusChip(
                                    text = "${bank.questions.size} 棰",
                                    selected = false
                                )
                            }
                        }
                        if (QuizRepository.banks.size > 5) {
                            Text(
                                text = "鍙︽湁 ${QuizRepository.banks.size - 5} 涓搴?璇峰埌棰樺簱绠＄悊鏌ョ湅瀹屾暣鍒楄〃銆",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShirohaColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAiConfigPrompt) {
        AlertDialog(
            onDismissRequest = { showAiConfigPrompt = false },
            title = { Text("闇€瑕侀厤缃?AI 鎺ュ彛") },
            text = { Text("璇峰厛鍦?鎴戠殑 鈫?AI 璁剧疆 涓厤缃帴鍙ｃ€") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAiConfigPrompt = false
                        onOpenPreference()
                    }
                ) {
                    Text("鍘婚厤缃")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiConfigPrompt = false }) {
                    Text("绋嶅悗鍐嶈")
                }
            }
        )
    }
}

private const val LARGE_TEXT_PREVIEW_THRESHOLD = 5000
private const val AI_WARNING_ID_MARKER = "__AI_QID__="
private const val IMPORT_WARNING_ID_MARKER = "__IMPORT_QID__="
private const val LARGE_TEXT_PREVIEW_CHARS = 1200
private const val IMPORT_FILE_WARN_BYTES = 30L * 1024L * 1024L
private const val IMPORT_FILE_BLOCK_BYTES = 80L * 1024L * 1024L

@Composable
private fun LargeImportTextPreview(
    text: String,
    label: String,
    showEditButton: Boolean = true,
    onEditFullText: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShirohaRadius.Lg),
        color = ShirohaColors.CardWhite78,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (showEditButton) {
                    Spacer(Modifier.width(10.dp))
                    ActionPillButton(
                        icon = Icons.Rounded.Edit,
                        text = "缂栬緫鍏ㄦ枃",
                        primary = false,
                        onClick = onEditFullText
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            SelectionContainer {
                Text(
                    text = text.take(LARGE_TEXT_PREVIEW_CHARS).trimEnd() + if (text.length > LARGE_TEXT_PREVIEW_CHARS) "\n鈥︹€" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FullImportTextEditorScreen(
    title: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onBack: () -> Unit
) {
    var showFindReplaceDialog by rememberSaveable { mutableStateOf(false) }
    var findText by rememberSaveable { mutableStateOf("") }
    var replaceText by rememberSaveable { mutableStateOf("") }
    var useRegexFind by rememberSaveable { mutableStateOf(false) }
    var findReplaceError by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm)) {
            Text(
                text = "Edit",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                EditorSaveButton(onClick = onBack)
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(ShirohaRadius.Lg),
            color = ShirohaColors.CardSoft,
            border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ShirohaSpacing.Xl)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "鍏ㄦ枃缂栬緫",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    ActionPillButton(
                        icon = Icons.Rounded.Search,
                        text = "鏌ユ壘",
                        primary = false,
                        onClick = { showFindReplaceDialog = true }
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    minLines = 16,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    placeholder = { Text(placeholder) }
                )
            }
        }
    }

    if (showFindReplaceDialog) {
        AlertDialog(
            onDismissRequest = { showFindReplaceDialog = false },
            title = { Text("鏌ユ壘 / 鏇挎崲") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = findText,
                        onValueChange = { findText = it },
                        label = { Text("鏌ユ壘鍐呭") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = replaceText,
                        onValueChange = { replaceText = it },
                        label = { Text("鏇挎崲鍐呭锛堢暀绌哄垯鍒犻櫎锛") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (useRegexFind) "姝ｅ垯妯″紡宸插紑鍚? else "鏅€氭枃鏈尮閰?,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            useRegexFind = !useRegexFind
                            findReplaceError = null
                        }) {
                            Text(if (useRegexFind) "鍏抽棴姝ｅ垯" else "浣跨敤姝ｅ垯")
                        }
                    }
                    Text(
                        text = if (useRegexFind) {
                            "浼氭寜姝ｅ垯琛ㄨ揪寮忔浛鎹㈠叏閮ㄥ尮閰嶅唴瀹癸紱鏇挎崲鍐呭涓嶅～鏃讹紝灏嗙洿鎺ュ垹闄ゅ尮閰嶆枃鏈€"
                        } else {
                            "浼氭浛鎹㈠叏閮ㄥ尮閰嶅唴瀹癸紱鏇挎崲鍐呭涓嶅～鏃讹紝灏嗙洿鎺ュ垹闄ゅ尮閰嶆枃鏈€"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    findReplaceError?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = findText.isNotBlank(),
                    onClick = {
                        val updatedText = if (useRegexFind) {
                            runCatching { Regex(findText).replace(value, replaceText) }
                                .onFailure { error ->
                                    findReplaceError = "姝ｅ垯琛ㄨ揪寮忔棤鏁堬細${error.message ?: "璇锋鏌ヨ娉"}"
                                }
                                .getOrNull()
                        } else {
                            value.replace(findText, replaceText)
                        }
                        if (updatedText != null) {
                            onValueChange(updatedText)
                            findReplaceError = null
                            showFindReplaceDialog = false
                        }
                    }
                ) {
                    Text(if (replaceText.isBlank()) "鍒犻櫎" else "鍏ㄩ儴鏇挎崲")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFindReplaceDialog = false }) {
                    Text("鍙栨秷")
                }
            }
        )
    }
}

@Composable
private fun EditorSaveButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(ShirohaRadius.Pill)
    Surface(
        modifier = Modifier
            .height(38.dp)
            .shirohaNoRippleClickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.primary,
        border = BorderStroke(ShirohaDimens.Hairline, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Save,
                contentDescription = "淇濆瓨鏇存敼",
                modifier = Modifier.size(16.dp),
                tint = ShirohaColors.TextOnBrand
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = "淇濆瓨鏇存敼",
                color = ShirohaColors.TextOnBrand,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ActionPillButton(
        icon = icon,
        text = text,
        primary = selected,
        modifier = modifier,
        fillWidthContent = true,
        onClick = onClick
    )
}


@Composable
private fun ReviewTypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .defaultMinSize(minHeight = 32.dp)
            .shirohaNoRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = if (selected) ShirohaColors.BrandPrimarySoft else ShirohaColors.CardMuted,
        border = BorderStroke(
            1.dp,
            if (selected) ShirohaColors.LineSelected else ShirohaColors.LineSoft
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) MaterialTheme.colorScheme.primary else ShirohaColors.TextSecondary,
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
        modifier = modifier
            .defaultMinSize(minHeight = 38.dp)
            .shirohaNoRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = if (primary) MaterialTheme.colorScheme.primary else ShirohaColors.CardWhite86,
        border = BorderStroke(ShirohaDimens.Hairline, if (primary) MaterialTheme.colorScheme.primary else ShirohaColors.LineStrong)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(15.dp),
                tint = if (primary) ShirohaColors.TextOnBrand else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = text,
                color = if (primary) ShirohaColors.TextOnBrand else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NativeImportSummary(
    result: ImportResult,
    aiSuggestions: List<AiReviewSuggestion>,
    aiReviewedQuestionIds: List<String>,
    aiAnalyzedQuestionIds: List<String>,
    aiAnalysisAppliedQuestionIds: List<String>
) {
    val hardCount = result.warnings.count { it.level == WarningLevel.ERROR }
    val softCount = result.warnings.count { it.level == WarningLevel.WARNING }
    val answeredCount = result.questions.count { it.answer.isNotEmpty() }
    val imageQuestionCount = result.questions.count { it.images.isNotEmpty() }
    val missingAnalysisCount = result.questions.count(::shouldApplyAiAnalysis)
    val aiReviewedIdSet = aiReviewedQuestionIds.toSet()
    val aiAnalyzedIdSet = aiAnalyzedQuestionIds.toSet()
    val aiAnalysisAppliedIdSet = aiAnalysisAppliedQuestionIds.toSet()
    val aiReviewSuggestionCount = result.questions.count { question ->
        aiSuggestionsForQuestion(question, aiSuggestions).any(::isActionableAiSuggestion)
    }
    val aiApplicableCount = result.questions.count { question ->
        aiSuggestionsForQuestion(question, aiSuggestions).any(::canApplyAiSuggestion)
    }
    val aiNeedConfirmCount = result.questions.count { question ->
        aiSuggestionsForQuestion(question, aiSuggestions).any(::isNeedHumanReviewAiSuggestion)
    }
    val aiHardErrorCount = result.questions.count { question ->
        aiSuggestionsForQuestion(question, aiSuggestions).any(::isHardErrorAiSuggestion)
    }
    val aiReviewedCount = result.questions.count { it.id in aiReviewedIdSet }
    val aiAnalyzedCount = result.questions.count { it.id in aiAnalyzedIdSet }
    val aiAnalysisAppliedCount = result.questions.count { it.id in aiAnalysisAppliedIdSet }

    GlassCard {
        Text(
            text = "瑙ｆ瀽缁撴灉",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatusChip("绛栫暐锛${result.strategyName}", selected = true)
            StatusChip("璇嗗埆棰樻暟锛${result.questions.size}", selected = true)
            StatusChip("宸茶瘑鍒瓟妗堬細$answeredCount", selected = answeredCount == result.questions.size)
            if (imageQuestionCount > 0) StatusChip("鍥剧墖棰橈細$imageQuestionCount", selected = true)
            StatusChip("纭敊璇細$hardCount", selected = hardCount == 0)
            StatusChip("鎻愮ず锛$softCount", selected = softCount == 0)
            StatusChip("缂?鐭В鏋愶細$missingAnalysisCount", selected = missingAnalysisCount == 0)
            if (aiReviewedCount > 0) StatusChip("AI宸叉牳瀵癸細$aiReviewedCount", selected = true)
            if (aiReviewSuggestionCount > 0) StatusChip("AI寤鸿锛$aiReviewSuggestionCount", selected = true)
            if (aiApplicableCount > 0) StatusChip("鍙噰绾筹細$aiApplicableCount", selected = true)
            if (aiNeedConfirmCount > 0) StatusChip("闇€纭锛$aiNeedConfirmCount", selected = false)
            if (aiHardErrorCount > 0) StatusChip("AI纭敊璇細$aiHardErrorCount", selected = false)
            if (aiAnalyzedCount > 0) StatusChip("AI宸插皾璇曡В鏋愶細$aiAnalyzedCount", selected = true)
            if (aiAnalysisAppliedCount > 0) StatusChip("AI宸茶ˉ瑙ｆ瀽锛$aiAnalysisAppliedCount", selected = true)
        }
        if (result.warnings.isNotEmpty()) {
            val (globalWarnings, questionWarnings) = result.warnings.partition { warning ->
                result.questions.none { question -> warningBelongsToQuestion(warning, question) }
            }
            if (globalWarnings.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "鍏ㄥ眬鎻愮ず",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                globalWarnings.forEach { warning ->
                    NoticeCard(
                        text = importWarningSummaryText(warning, result.questions),
                        warning = warning.level != WarningLevel.NORMAL
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (questionWarnings.isNotEmpty()) {
                Spacer(Modifier.height(if (globalWarnings.isEmpty()) 14.dp else 6.dp))
                Text(
                    text = "棰樼洰鎻愮ず",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                questionWarnings.take(6).forEach { warning ->
                    NoticeCard(
                        text = importWarningSummaryText(warning, result.questions),
                        warning = warning.level != WarningLevel.NORMAL
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (questionWarnings.size > 6) {
                    Text(
                        text = "鍙︽湁 ${questionWarnings.size - 6} 鏉￠鐩彁绀猴紝璇峰湪娌夋蹈鏍稿涓煡鐪嬪搴旈鐩€",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (result.diagnostics.notes.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "鍊欓€夌瓥鐣ヨ瘖鏂",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            result.diagnostics.notes.take(4).forEach { note ->
                Text(
                    text = "鈥?$note",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun NativeImportPreview(
    questions: List<Question>,
    totalQuestionCount: Int,
    onlyShowAnomaly: Boolean,
    aiSuggestions: List<AiReviewSuggestion>,
    aiReviewedQuestionIds: List<String>,
    aiAnalyzedQuestionIds: List<String>,
    aiAnalysisAppliedQuestionIds: List<String>
) {
    GlassCard {
        Text(
            text = if (onlyShowAnomaly) "寮傚父棰橀瑙? else "蹇€熼瑙?,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        if (questions.isEmpty()) {
            NoticeCard(
                text = if (onlyShowAnomaly) "褰撳墠娌℃湁鍙瑙堢殑寮傚父棰樸€? else "褰撳墠娌℃湁鍙瑙堢殑棰樼洰銆?,
                warning = false
            )
            return@GlassCard
        }
        questions.take(8).forEach { question ->
            val answerText = answerDisplayText(question)
            val optionText = question.options.joinToString("  ") { "${it.key}. ${it.text}" }

            Text(
                text = "${question.number}. ${typeLabel(question.type)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            SelectionContainer {
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (question.images.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                QuestionImagesBlock(question.images, maxPreviewHeight = 220.dp, showMeta = true)
            }
            if (optionText.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = optionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "绛旀锛$answerText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            val questionTags = importPreviewQuestionTags(
                question = question,
                aiSuggestions = aiSuggestionsForQuestion(question, aiSuggestions),
                aiReviewedQuestionIds = aiReviewedQuestionIds,
                aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
                aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds
            )
            if (questionTags.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = questionTags,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (question.analysis.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "瑙ｆ瀽锛${question.analysis}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(18.dp))
        }
        if (questions.size > 8) {
            NoticeCard("褰撳墠浠呭睍绀哄墠 8 棰樼敤浜庡揩閫熼瑙堬紝瀹屾暣鏍稿璇疯繘鍏ユ矇娴告牳瀵广€", warning = false)
        } else if (onlyShowAnomaly && questions.size < totalQuestionCount) {
            NoticeCard("褰撳墠浠呴瑙堝紓甯搁銆傜偣鍑讳笂鏂光€滄樉绀哄叏閮ㄩ鈥濆彲鎭㈠鍏ㄩ儴棰勮銆", warning = false)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun NativeQuestionReviewScreen(
    questions: List<Question>,
    warnings: List<ImportWarning>,
    aiSuggestions: List<AiReviewSuggestion>,
    aiReviewedQuestionIds: List<String>,
    aiAnalyzedQuestionIds: List<String>,
    aiAnalysisAppliedQuestionIds: List<String>,
    filter: ReviewFilter,
    currentIndex: Int,
    focusFilterListTick: Int,
    onFilterChange: (ReviewFilter) -> Unit,
    onIndexChange: (Int) -> Unit,
    onQuestionChange: (Int, Question) -> Unit,
    onApplyAiSuggestion: (Int, AiReviewSuggestion) -> Unit,
    onDeleteQuestion: (Int) -> Unit,
    onEditQuestion: (Int, Boolean) -> Unit,
    onBack: () -> Unit
) {
    if (questions.isEmpty()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            ShirohaHeader(
                kicker = "Review",
                title = "娌夋蹈鏍稿",
                subtitle = "褰撳墠娌℃湁鍙牳瀵圭殑棰樼洰銆"
            )
            ActionPillButton(
                icon = Icons.Rounded.ArrowBack,
                text = "杩斿洖瀵煎叆椤",
                primary = false,
                onClick = onBack
            )
        }
        return
    }

    val activeFilterCount = reviewFilterCount(
        questions = questions,
        warnings = warnings,
        aiSuggestions = aiSuggestions,
        aiReviewedQuestionIds = aiReviewedQuestionIds,
        aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
        aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds,
        filter = filter
    )
    val activeFilter = if (filter != ReviewFilter.ALL && activeFilterCount <= 0) ReviewFilter.ALL else filter
    val allIndices = questions.indices.toList()
    val filteredIndices = questions.indices.filter { index ->
        val candidate = questions[index]
        questionMatchesFilter(
            question = candidate,
            warnings = warningsForQuestion(candidate, warnings),
            filter = activeFilter,
            aiSuggestions = aiSuggestionsForQuestion(candidate, aiSuggestions),
            aiReviewedQuestionIds = aiReviewedQuestionIds,
            aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
            aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds
        )
    }
    val visibleIndices = if (activeFilter == ReviewFilter.ALL) allIndices else filteredIndices
    val safeIndex = when {
        questions.isEmpty() -> 0
        activeFilter == ReviewFilter.ALL -> currentIndex.coerceIn(0, questions.lastIndex)
        currentIndex in visibleIndices -> currentIndex
        visibleIndices.isNotEmpty() -> visibleIndices.first()
        else -> currentIndex.coerceIn(0, questions.lastIndex)
    }
    val question = questions[safeIndex]
    val questionWarnings = warningsForQuestion(question, warnings)
    val questionAiSuggestions = aiSuggestions.filter { it.questionId == question.id && isActionableAiSuggestion(it) }
    val visiblePosition = visibleIndices.indexOf(safeIndex).takeIf { it >= 0 } ?: 0
    val reviewScrollState = rememberScrollState()
    val filterListBringIntoViewRequester = remember { BringIntoViewRequester() }
    var filterListRootY by remember { mutableStateOf(0f) }
    val focusTopOffsetPx = with(LocalDensity.current) { 24.dp.toPx() }

    LaunchedEffect(focusFilterListTick, activeFilter, visibleIndices.size, safeIndex) {
        if (focusFilterListTick > 0 && activeFilter != ReviewFilter.ALL && visibleIndices.size > 1) {
            delay(120)
            val targetScroll = (reviewScrollState.value + filterListRootY - focusTopOffsetPx)
                .roundToInt()
                .coerceAtLeast(0)
            reviewScrollState.animateScrollTo(targetScroll)
            filterListBringIntoViewRequester.bringIntoView()
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(reviewScrollState)
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Review",
            title = "娌夋蹈鏍稿",
            subtitle = "閫愰鏍稿瀵煎叆缁撴灉锛屽彲鍏堢瓫閫夊紓甯告垨鏃犵瓟妗堛€"
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
                ReviewFilter.values().forEach { item ->
                    val count = reviewFilterCount(
                        questions = questions,
                        warnings = warnings,
                        aiSuggestions = aiSuggestions,
                        aiReviewedQuestionIds = aiReviewedQuestionIds,
                        aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
                        aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds,
                        filter = item
                    )
                    if (item == ReviewFilter.ALL || count > 0) {
                        ReviewTypeChip(
                            text = "${reviewFilterLabel(item)} $count",
                            selected = activeFilter == item,
                            onClick = { onFilterChange(item) }
                        )
                    }
                }
            }
            if (activeFilter != ReviewFilter.ALL && visibleIndices.isEmpty()) {
                Spacer(Modifier.height(12.dp))
                NoticeCard("褰撳墠绛涢€変笅娌℃湁闇€瑕佹牳瀵圭殑棰樼洰锛屽彲浠ュ垏鎹㈠埌鈥滃叏閮ㄢ€濈户缁祻瑙堛€", warning = false)
            }
        }

        if (activeFilter != ReviewFilter.ALL && visibleIndices.isEmpty()) {
            ActionPillButton(
                icon = Icons.Rounded.ArrowBack,
                text = "杩斿洖瀵煎叆椤",
                primary = false,
                onClick = onBack
            )
            return@Column
        }

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "绗?${safeIndex + 1} / ${questions.size} 棰",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            append(typeLabel(question.type))
                            append(" 路 绛旀锛")
                            append(answerDisplayText(question))
                            if (activeFilter != ReviewFilter.ALL) {
                                append(" 路 ${reviewFilterLabel(activeFilter)} ${visiblePosition + 1}/${visibleIndices.size}")
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
                    onClick = onBack
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReviewCompactButton(
                    icon = Icons.Rounded.Edit,
                    text = "缂栬緫鏈",
                    modifier = Modifier.weight(1f),
                    onClick = { onEditQuestion(safeIndex, false) }
                )
                ReviewCompactButton(
                    icon = Icons.Rounded.ArrowBack,
                    text = if (activeFilter == ReviewFilter.ALL) "涓婁竴棰? else "涓婁竴鏉?,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val target = previousIndexInList(visibleIndices, safeIndex) ?: (safeIndex - 1)
                        onIndexChange(target)
                    }
                )
                ReviewCompactButton(
                    icon = Icons.Rounded.ArrowForward,
                    text = if (activeFilter == ReviewFilter.ALL) "涓嬩竴棰? else "涓嬩竴鏉?,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val target = nextIndexInList(visibleIndices, safeIndex) ?: (safeIndex + 1)
                        onIndexChange(target)
                    }
                )
            }
        }

        ReviewQuestionAssistBlocks(
            question = question,
            questionWarnings = questionWarnings,
            questionAiSuggestions = questionAiSuggestions,
            aiReviewedQuestionIds = aiReviewedQuestionIds,
            aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
            aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds,
            onApplyAiSuggestion = { suggestion -> onApplyAiSuggestion(safeIndex, suggestion) }
        )

        if (activeFilter != ReviewFilter.ALL && visibleIndices.size > 1) {
            ReviewFilteredJumpList(
                questions = questions,
                indices = visibleIndices,
                currentIndex = safeIndex,
                warnings = warnings,
                onIndexChange = onIndexChange,
                onEditQuestion = { index -> onEditQuestion(index, true) },
                modifier = Modifier
                    .bringIntoViewRequester(filterListBringIntoViewRequester)
                    .onGloballyPositioned { coordinates ->
                        filterListRootY = coordinates.positionInRoot().y
                    }
            )
        }

        ReviewQuestionEditorContent(
            question = question,
            onQuestionChange = { updatedQuestion -> onQuestionChange(safeIndex, updatedQuestion) },
            onDeleteQuestion = { onDeleteQuestion(safeIndex) }
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewQuestionEditScreen(
    question: Question,
    questionIndex: Int,
    totalCount: Int,
    questionWarnings: List<ImportWarning>,
    questionAiSuggestions: List<AiReviewSuggestion>,
    aiReviewedQuestionIds: List<String>,
    aiAnalyzedQuestionIds: List<String>,
    aiAnalysisAppliedQuestionIds: List<String>,
    onQuestionChange: (Question) -> Unit,
    onApplyAiSuggestion: (AiReviewSuggestion) -> Unit,
    onDeleteQuestion: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Edit",
            title = "缂栬緫棰樼洰",
            subtitle = "绗?${questionIndex + 1} / $totalCount 棰?路 ${typeLabel(question.type)} 路 绛旀锛${answerDisplayText(question)}"
        )

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "棰樼洰缂栬緫",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "淇敼鍐呭浼氬悓姝ュ洖娌夋蹈鏍稿鍒楄〃銆",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ReviewCompactButton(
                    icon = Icons.Rounded.CheckCircle,
                    text = "淇濆瓨鏇存敼",
                    primary = true,
                    onClick = onBack
                )
            }
        }

        ReviewQuestionAssistBlocks(
            question = question,
            questionWarnings = questionWarnings,
            questionAiSuggestions = questionAiSuggestions,
            aiReviewedQuestionIds = aiReviewedQuestionIds,
            aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
            aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds,
            onApplyAiSuggestion = onApplyAiSuggestion
        )

        ReviewQuestionEditorContent(
            question = question,
            onQuestionChange = onQuestionChange,
            onDeleteQuestion = onDeleteQuestion
        )
    }
}

@Composable
private fun ReviewQuestionAssistBlocks(
    question: Question,
    questionWarnings: List<ImportWarning>,
    questionAiSuggestions: List<AiReviewSuggestion>,
    aiReviewedQuestionIds: List<String>,
    aiAnalyzedQuestionIds: List<String>,
    aiAnalysisAppliedQuestionIds: List<String>,
    onApplyAiSuggestion: (AiReviewSuggestion) -> Unit
) {
    if (questionWarnings.isNotEmpty()) {
        GlassCard {
            Text(
                text = "鏈鎻愮ず",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            questionWarnings.forEach { warning ->
                NoticeCard(displayImportWarningMessage(warning.message), warning = warning.level != WarningLevel.NORMAL)
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (question.id in aiReviewedQuestionIds.toSet() && questionAiSuggestions.isEmpty()) {
        GlassCard {
            Text(
                text = "AI 鏍稿鐘舵€",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            NoticeCard("AI 宸叉牳瀵规湰棰橈細鏈彂鐜伴渶瑕佹樉绀虹殑閲嶇偣闂銆", warning = false)
        }
    }

    if (shouldApplyAiAnalysis(question) || question.id in aiAnalyzedQuestionIds.toSet() || question.id in aiAnalysisAppliedQuestionIds.toSet()) {
        GlassCard {
            Text(
                text = "AI 瑙ｆ瀽鐘舵€",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            NoticeCard(
                text = analysisStatusText(
                    question = question,
                    aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
                    aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds
                ),
                warning = shouldApplyAiAnalysis(question) && question.id !in aiAnalysisAppliedQuestionIds.toSet()
            )
        }
    }

    if (questionAiSuggestions.isNotEmpty()) {
        GlassCard {
            Text(
                text = "AI 鏍稿寤鸿",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            questionAiSuggestions.forEach { suggestion ->
                AiReviewSuggestionCard(
                    suggestion = suggestion,
                    onApply = { onApplyAiSuggestion(suggestion) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewQuestionEditorContent(
    question: Question,
    onQuestionChange: (Question) -> Unit,
    onDeleteQuestion: (() -> Unit)? = null
) {
    var showRemoveImagesConfirm by remember(question.id) { mutableStateOf(false) }
    var showDeleteLastOptionConfirm by remember(question.id) { mutableStateOf(false) }
    var showDeleteQuestionConfirm by remember(question.id) { mutableStateOf(false) }

    if (showRemoveImagesConfirm) {
        ShirohaDangerConfirmDialog(
            title = "纭绉婚櫎鏈鍥剧墖锛",
            message = "杩欎細浠庡綋鍓嶆牳瀵归鐩腑绉婚櫎宸茬粦瀹氬浘鐗囥€備繚瀛橀搴撴椂浼氫娇鐢ㄧЩ闄ゅ浘鐗囧悗鐨勯鐩€",
            confirmText = "纭绉婚櫎",
            onDismiss = { showRemoveImagesConfirm = false },
            onConfirm = {
                onQuestionChange(question.copy(images = emptyList()))
                showRemoveImagesConfirm = false
            }
        )
    }

    if (showDeleteLastOptionConfirm) {
        ShirohaDangerConfirmDialog(
            title = "纭鍒犻櫎鏈€鍚庝竴涓€夐」锛",
            message = "杩欎細鍒犻櫎褰撳墠棰樼洰鐨勬渶鍚庝竴涓€夐」銆備繚瀛橀搴撴椂浼氫娇鐢ㄥ垹闄ゅ悗鐨勯鐩€",
            confirmText = "纭鍒犻櫎",
            onDismiss = { showDeleteLastOptionConfirm = false },
            onConfirm = {
                if (question.options.isNotEmpty()) {
                    onQuestionChange(question.copy(options = question.options.dropLast(1)))
                }
                showDeleteLastOptionConfirm = false
            }
        )
    }

    if (showDeleteQuestionConfirm) {
        ShirohaDangerConfirmDialog(
            title = "纭鍒犻櫎鏈锛",
            message = "杩欎細浠庡綋鍓嶆牳瀵瑰垪琛ㄤ腑鍒犻櫎鏈銆備繚瀛橀搴撴椂浼氫娇鐢ㄥ垹闄ゅ悗鐨勯鐩垪琛ㄣ€",
            confirmText = "纭鍒犻櫎",
            onDismiss = { showDeleteQuestionConfirm = false },
            onConfirm = {
                onDeleteQuestion?.invoke()
                showDeleteQuestionConfirm = false
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)) {
        GlassCard {
            Text(
                text = "棰樼洰鍐呭",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = question.number,
                    onValueChange = { value ->
                        onQuestionChange(question.copy(number = value))
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("棰樺彿") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = question.category,
                    onValueChange = { value ->
                        onQuestionChange(question.copy(category = value))
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
                        onClick = {
                            onQuestionChange(normalizeAfterTypeChange(question, type))
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = question.question,
                onValueChange = { value ->
                    onQuestionChange(question.copy(question = value))
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
                NoticeCard("鍥剧墖鎸夊鍏ユ枃妗ｄ腑鐨勪綅缃嚜鍔ㄧ粦瀹氥€傝祫鏂欏垎鏋愮被鍏变韩鍥剧墖浼氬紩鐢ㄥ埌鍚庣画棰樼洰锛涜閲嶇偣鏍稿鍥剧墖鏄惁灞炰簬鏈銆", warning = false)
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
                NoticeCard("褰撳墠棰樼洰娌℃湁閫夐」銆傚垽鏂鍙互鐐瑰嚮鈥滆ˉ榻愬垽鏂€夐」鈥濓紝閫夋嫨棰樺彲浠ョ偣鍑烩€滄坊鍔犻€夐」鈥濄€", warning = false)
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
                            val updated = question.options.mapIndexed { currentIndex, item ->
                                if (currentIndex == optionIndex) item.copy(key = value.uppercase().take(2)) else item
                            }
                            onQuestionChange(question.copy(options = updated))
                        },
                        modifier = Modifier.width(74.dp),
                        label = { Text("椤") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = option.text,
                        onValueChange = { value ->
                            val updated = question.options.mapIndexed { currentIndex, item ->
                                if (currentIndex == optionIndex) item.copy(text = value) else item
                            }
                            onQuestionChange(question.copy(options = updated))
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
                        onQuestionChange(question.copy(options = question.options + Option(key, "")))
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
                        onQuestionChange(
                            question.copy(
                                type = QuestionType.JUDGE,
                                options = defaultJudgeOptions(),
                                answer = if (question.answer.isEmpty()) listOf("A") else question.answer,
                                blankAnswers = emptyList()
                            )
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
            Text(
                text = "鍗曢€?A锛屽閫?ABC锛屽垽鏂?姝ｇ‘/閿欒锛涘绌哄～绌哄彲閫愮┖閰嶇疆涓荤瓟妗堜笌澶囬€夌瓟妗堛€",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            val detectedBlankCount = MultiBlankSupport.countExplicitBlanks(question.question)
            if (question.type == QuestionType.BLANK && question.blankAnswers.isNotEmpty()) {
                MultiBlankAnswerEditor(
                    blankAnswers = question.blankAnswers,
                    detectedBlankCount = detectedBlankCount,
                    onChange = { groups -> onQuestionChange(MultiBlankSupport.withBlankAnswers(question, groups)) },
                    onDisable = {
                        onQuestionChange(
                            question.copy(
                                answer = MultiBlankSupport.compatibilityAnswer(question.blankAnswers),
                                blankAnswers = emptyList()
                            )
                        )
                    }
                )
            } else {
                OutlinedTextField(
                    value = answerInputText(question),
                    onValueChange = { value ->
                        onQuestionChange(
                            question.copy(
                                answer = parseReviewAnswer(value, question.type),
                                blankAnswers = emptyList()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("绛旀") },
                    singleLine = true
                )
                if (question.type == QuestionType.BLANK && detectedBlankCount > 1) {
                    Spacer(Modifier.height(10.dp))
                    ActionPillButton(
                        icon = Icons.Rounded.Add,
                        text = "鍚敤澶氱┖绛旀",
                        primary = false,
                        onClick = {
                            onQuestionChange(
                                MultiBlankSupport.withBlankAnswers(
                                    question,
                                    MultiBlankSupport.initialGroups(question.question, question.answer)
                                )
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = question.analysis,
                onValueChange = { value ->
                    onQuestionChange(question.copy(analysis = value))
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
                    onQuestionChange(question.copy(analysis = value))
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
                text = "璇瘑鍒殑璇存槑銆侀〉鐪夐〉鑴氭垨纰庣墖锛屽彲鍒犻櫎鏈銆",
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


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiReviewSuggestionCard(
    suggestion: AiReviewSuggestion,
    onApply: () -> Unit
) {
    val riskText = when (suggestion.riskLevel.lowercase()) {
        "auto_safe" -> "浣庨闄"
        "hard_error" -> "纭敊璇"
        else -> "闇€纭"
    }
    val issueText = suggestion.issueTypes.takeIf { it.isNotEmpty() }?.joinToString("銆").orEmpty()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ShirohaColors.CardWhite62,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = buildString {
                    append("AI 寤鸿")
                    append(" 路 ").append(riskText)
                    if (suggestion.confidence > 0.0) append(" 路 缃俊搴?").append((suggestion.confidence * 100).toInt()).append("%")
                    if (issueText.isNotBlank()) append(" 路 ").append(issueText)
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            if (suggestion.reason.isNotBlank()) {
                Text(
                    text = suggestion.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (suggestion.suggestion.isNotBlank()) {
                Text(
                    text = "寤鸿锛${suggestion.suggestion}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val applySummary = aiSuggestionApplySummary(suggestion)
            if (applySummary.isNotBlank()) {
                Text(
                    text = "鍙噰绾冲唴瀹癸細$applySummary",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionPillButton(
                    icon = Icons.Rounded.CheckCircle,
                    text = if (canApplyAiSuggestion(suggestion)) "閲囩撼 AI 寤鸿" else "浠呮彁绀猴紝闇€鎵嬪姩澶勭悊",
                    primary = canApplyAiSuggestion(suggestion),
                    enabled = canApplyAiSuggestion(suggestion),
                    onClick = onApply
                )
            }
        }
    }
}


private data class AiRefactorApplyResult(
    val refactorResult: AiRefactorResult,
    val reparsedResult: ImportResult?
)

private fun shouldShowAiStatusInImport(text: String): Boolean {
    return text.startsWith("AI ") ||
        text.startsWith("AI鏍稿") ||
        text.startsWith("AI瑙ｆ瀽") ||
        text.contains("AI 閲嶆瀯") ||
        text.contains("AI 鏍稿") ||
        text.contains("AI 瑙ｆ瀽")
}


private fun aiRefactorImportWarnings(
    notes: List<String>,
    baseMessage: String = "AI閲嶆瀯宸茬敓鎴愭柊鐨勫緟鏍稿缁撴灉锛岃浜哄伐纭棰橀噺銆侀骞层€侀€夐」銆佺瓟妗堝拰瑙ｆ瀽鍚庡啀淇濆瓨銆"
): List<ImportWarning> {
    val base = listOf(
        ImportWarning(
            level = WarningLevel.WARNING,
            questionNumber = null,
            message = baseMessage
        )
    )
    val noteWarnings = notes.take(10).map { note ->
        ImportWarning(
            level = WarningLevel.WARNING,
            questionNumber = null,
            message = "AI閲嶆瀯鎻愮ず锛$note"
        )
    }
    return dedupeImportWarnings(base + noteWarnings)
}

private fun aiSuggestionsForQuestion(
    question: Question,
    suggestions: List<AiReviewSuggestion>
): List<AiReviewSuggestion> {
    return suggestions.filter { it.questionId == question.id && isActionableAiSuggestion(it) }
}

private fun isHardErrorAiSuggestion(suggestion: AiReviewSuggestion): Boolean {
    return suggestion.riskLevel.equals("hard_error", ignoreCase = true)
}

private fun isNeedHumanReviewAiSuggestion(suggestion: AiReviewSuggestion): Boolean {
    return isActionableAiSuggestion(suggestion) && (
        suggestion.needHumanReview ||
            !canApplyAiSuggestion(suggestion) ||
            (suggestion.riskLevel.equals("needs_confirm", ignoreCase = true) || suggestion.riskLevel.equals("needs_review", ignoreCase = true))
        )
}

private fun importPreviewQuestionTags(
    question: Question,
    aiSuggestions: List<AiReviewSuggestion>,
    aiReviewedQuestionIds: List<String>,
    aiAnalyzedQuestionIds: List<String>,
    aiAnalysisAppliedQuestionIds: List<String>
): String {
    val tags = mutableListOf<String>()
    if (shouldApplyAiAnalysis(question)) tags += "缂?鐭В鏋"
    if (question.id in aiReviewedQuestionIds.toSet()) tags += "AI宸叉牳瀵"
    if (question.id in aiReviewedQuestionIds.toSet() && aiSuggestions.isEmpty()) tags += "鏈彂鐜伴噸鐐归棶棰"
    if (question.id in aiAnalyzedQuestionIds.toSet()) tags += "AI宸插皾璇曡В鏋"
    if (question.id in aiAnalysisAppliedQuestionIds.toSet()) tags += "AI宸茶ˉ瑙ｆ瀽"
    if (aiSuggestions.isNotEmpty()) tags += "AI寤鸿"
    if (aiSuggestions.any(::canApplyAiSuggestion)) tags += "鍙噰绾"
    if (aiSuggestions.any(::isNeedHumanReviewAiSuggestion)) tags += "闇€纭"
    if (aiSuggestions.any(::isHardErrorAiSuggestion)) tags += "AI纭敊璇"
    return tags.distinct().joinToString(" 路 ")
}

private fun analysisStatusText(
    question: Question,
    aiAnalyzedQuestionIds: List<String>,
    aiAnalysisAppliedQuestionIds: List<String>
): String {
    val parts = mutableListOf<String>()
    if (shouldApplyAiAnalysis(question)) parts += "褰撳墠浠嶇己灏戞湁鏁堣В鏋愭垨瑙ｆ瀽鍋忕煭"
    if (question.id in aiAnalyzedQuestionIds.toSet()) parts += "AI 宸插皾璇曡В鏋愭湰棰"
    if (question.id in aiAnalysisAppliedQuestionIds.toSet()) parts += "AI 宸插啓鍏ヨˉ鍏呰В鏋愶紝淇濆瓨鍓嶈浜哄伐纭"
    if (parts.isEmpty()) parts += "褰撳墠瑙ｆ瀽闀垮害鍩烘湰姝ｅ父"
    return parts.joinToString("锛")
}

private fun suggestionsToImportWarnings(
    suggestions: List<AiReviewSuggestion>,
    questions: List<Question>
): List<ImportWarning> {
    val questionById = questions.associateBy { it.id }
    return suggestions
        .filter(::isActionableAiSuggestion)
        .mapNotNull { suggestion ->
            val question = questionById[suggestion.questionId] ?: return@mapNotNull null
            val issueText = suggestion.issueTypes.takeIf { it.isNotEmpty() }?.joinToString("銆").orEmpty()
            val message = buildString {
                append("AI寤鸿")
                if (issueText.isNotBlank()) append("[").append(issueText).append("]")
                if (suggestion.reason.isNotBlank()) append("锛").append(suggestion.reason)
                if (suggestion.suggestion.isNotBlank()) append("锛涘缓璁細").append(suggestion.suggestion)
                val applySummary = aiSuggestionApplySummary(suggestion)
                if (applySummary.isNotBlank()) {
                    append(if (canApplyAiSuggestion(suggestion)) "锛涘彲閲囩撼锛" else "锛涘缓璁唴瀹癸細")
                    append(applySummary)
                }
                append("锛").append(AI_WARNING_ID_MARKER).append(question.id)
            }
            val hard = suggestion.status.equals("error", ignoreCase = true) ||
                suggestion.riskLevel.equals("hard_error", ignoreCase = true)
            ImportWarning(
                level = if (hard) WarningLevel.ERROR else WarningLevel.WARNING,
                questionNumber = question.number,
                message = message.ifBlank { "AI 寤鸿浜哄伐纭鏈锛?AI_WARNING_ID_MARKER${question.id}" }
            )
        }
}

private fun importWarningSummaryText(warning: ImportWarning, questions: List<Question>): String {
    val question = questions.firstOrNull { warningBelongsToQuestion(warning, it) }
    val prefix = if (question != null) {
        buildString {
            append("绗?")
            append(question.number.ifBlank { warning.questionNumber ?: "-" })
            append(" 棰")
            append(" 路 ")
            append(typeLabel(question.type))
            val category = question.category.trim()
            if (category.isNotBlank()) {
                append(" 路 ")
                append(category)
            }
        }
    } else if (warning.questionNumber.isNullOrBlank()) {
        "鍏ㄥ眬鎻愮ず"
    } else {
        "绗?${warning.questionNumber} 棰"
    }
    return "$prefix锛${displayImportWarningMessage(warning.message)}"
}

private fun mergeAiWarnings(
    currentWarnings: List<ImportWarning>,
    aiWarnings: List<ImportWarning>,
    processedQuestions: List<Question>
): List<ImportWarning> {
    val processedIds = processedQuestions.map { it.id }.toSet()
    val processedNumbers = processedQuestions.map { normalizeQuestionNumber(it.number) }.toSet()
    val keptWarnings = currentWarnings.filterNot { warning ->
        if (!isAiImportWarning(warning)) return@filterNot false
        val warningQuestionId = aiWarningQuestionId(warning)
        if (warningQuestionId != null) warningQuestionId in processedIds
        else normalizeQuestionNumber(warning.questionNumber.orEmpty()) in processedNumbers
    }
    return dedupeImportWarnings(keptWarnings + aiWarnings)
}

private fun dedupeImportWarnings(warnings: List<ImportWarning>): List<ImportWarning> {
    val seen = mutableSetOf<String>()
    return warnings.filter { warning ->
        val key = listOf(
            warning.level.name,
            normalizeQuestionNumber(warning.questionNumber.orEmpty()),
            importWarningQuestionId(warning) ?: aiWarningQuestionId(warning).orEmpty(),
            normalizeImportWarningForDedupe(warning.message)
        ).joinToString("|")
        seen.add(key)
    }
}

private fun refreshImportWarningsForQuestions(
    currentWarnings: List<ImportWarning>,
    questions: List<Question>
): List<ImportWarning> {
    val preservedWarnings = currentWarnings.filterNot(::isReplaceableLocalImportWarning)
    return dedupeImportWarnings(
        preservedWarnings + validateQuestionsWithIdMarkers(questions) + duplicateQuestionNumberWarnings(questions)
    )
}

private fun validateQuestionsWithIdMarkers(questions: List<Question>): List<ImportWarning> {
    return questions.flatMap { question ->
        ImportValidator.validate(listOf(question)).map { warning ->
            warning.copy(message = warning.message.withImportWarningQuestionId(question.id))
        }
    }
}

private fun String.withImportWarningQuestionId(questionId: String): String {
    return "${displayImportWarningMessage(this)}锛?IMPORT_WARNING_ID_MARKER$questionId"
}

private fun isReplaceableLocalImportWarning(warning: ImportWarning): Boolean {
    val message = displayImportWarningMessage(warning.message)
    return message in replaceableLocalImportWarningMessages ||
        message.startsWith("鍚屼竴鍒嗗尯/棰樺瀷鍐呴鍙烽噸澶") ||
        isMultiBlankLocalWarning(message)
}

private fun isMultiBlankLocalWarning(message: String): Boolean {
    return (message.contains("涓绌") && (
        message.contains("鏈瘑鍒€愮┖绛旀") ||
            message.contains("绛旀鏁伴噺鏃犳硶瀵瑰簲") ||
            message.contains("褰撳墠閰嶇疆浜")
        )) || message == "澶氱┖濉┖棰樺瓨鍦ㄦ湭閰嶇疆绛旀鐨勯绌"
}

private val replaceableLocalImportWarningMessages = setOf(
    "棰樺共涓虹┖",
    "鍗曢€夐缂哄皯瓒冲閫夐」",
    "鍗曢€夐鏈瘑鍒埌绛旀",
    "鍗曢€夐鍑虹幇澶氫釜绛旀",
    "澶氶€夐缂哄皯瓒冲閫夐」",
    "澶氶€夐鏈瘑鍒埌绛旀",
    "绛旀閫夐」涓嶅湪褰撳墠棰樼洰閫夐」鑼冨洿鍐",
    "鍒ゆ柇棰樼己灏戝/閿欓€夐」锛屽凡灏濊瘯鑷姩琛ュ叏",
    "鍒ゆ柇棰樻湭璇嗗埆鍒扮瓟妗",
    "鍒ゆ柇棰樼瓟妗堜笉鏄爣鍑嗗/閿欐爣璁",
    "涓昏棰樻湭璇嗗埆鍒板弬鑰冪瓟妗"
)

private fun normalizeImportWarningForDedupe(message: String): String {
    return displayImportWarningMessage(message)
        .replace(Regex("\\s+"), "")
        .trim('锛?, ';', '銆?, ' ', '\n', '\t')
}

private fun isAiImportWarning(warning: ImportWarning): Boolean {
    return warning.message.startsWith("AI寤鸿") || warning.message.startsWith("AI 寤鸿")
}

private fun aiWarningQuestionId(warning: ImportWarning): String? {
    return markerValue(warning.message, AI_WARNING_ID_MARKER)
}

private fun importWarningQuestionId(warning: ImportWarning): String? {
    return markerValue(warning.message, IMPORT_WARNING_ID_MARKER)
}

private fun markerValue(message: String, marker: String): String? {
    val markerIndex = message.indexOf(marker)
    if (markerIndex < 0) return null
    return message.substring(markerIndex + marker.length)
        .substringBefore(' ')
        .substringBefore('锛?)
        .substringBefore(';')
        .trim()
        .takeIf { it.isNotBlank() }
}

private fun warningBelongsToQuestion(warning: ImportWarning, question: Question): Boolean {
    val warningQuestionId = importWarningQuestionId(warning) ?: aiWarningQuestionId(warning)
    if (warningQuestionId != null) return warningQuestionId == question.id
    return normalizeQuestionNumber(warning.questionNumber.orEmpty()) == normalizeQuestionNumber(question.number)
}

private fun aiWarningBelongsToQuestion(warning: ImportWarning, question: Question): Boolean {
    return warningBelongsToQuestion(warning, question)
}

private fun displayImportWarningMessage(message: String): String {
    val markerIndex = listOf(
        message.indexOf(AI_WARNING_ID_MARKER),
        message.indexOf(IMPORT_WARNING_ID_MARKER)
    ).filter { it >= 0 }.minOrNull() ?: return message
    return message.substring(0, markerIndex).trimEnd('锛?, ';', ' ', '\n', '\t')
}

private fun canApplyAiSuggestion(suggestion: AiReviewSuggestion): Boolean {
    return suggestion.canApply && !suggestion.riskLevel.equals("hard_error", ignoreCase = true)
}

private fun isActionableAiSuggestion(suggestion: AiReviewSuggestion): Boolean {
    val hasStructuredSuggestion = suggestion.suggestedType != null ||
        suggestion.suggestedAnswer.isNotEmpty() ||
        suggestion.suggestedQuestion != null ||
        suggestion.suggestedOptions.isNotEmpty() ||
        suggestion.suggestedAnalysis != null
    val pureOk = suggestion.status.equals("ok", ignoreCase = true) &&
        suggestion.issueTypes.isEmpty() &&
        !suggestion.needHumanReview &&
        !canApplyAiSuggestion(suggestion) &&
        !hasStructuredSuggestion
    return !pureOk && (
        !suggestion.status.equals("ok", ignoreCase = true) ||
            suggestion.needHumanReview ||
            canApplyAiSuggestion(suggestion) ||
            suggestion.issueTypes.isNotEmpty() ||
            hasStructuredSuggestion
        )
}

private fun mergeAiReviewSuggestions(
    current: List<AiReviewSuggestion>,
    incoming: List<AiReviewSuggestion>,
    processedQuestions: List<Question>
): List<AiReviewSuggestion> {
    val processedIds = processedQuestions.map { it.id }.toSet()
    val kept = current.filterNot { it.questionId in processedIds }
    return kept + incoming.filter(::isActionableAiSuggestion)
}

private fun aiSuggestionApplySummary(suggestion: AiReviewSuggestion): String {
    val parts = mutableListOf<String>()
    suggestion.suggestedType?.let { parts += "棰樺瀷鈫${suggestedTypeLabel(it)}" }
    if (suggestion.suggestedAnswer.isNotEmpty()) parts += "绛旀鈫${suggestion.suggestedAnswer.joinToString("")}"
    if (suggestion.suggestedQuestion != null) parts += "棰樺共"
    if (suggestion.suggestedOptions.isNotEmpty()) parts += "閫夐」 ${suggestion.suggestedOptions.size} 椤"
    if (suggestion.suggestedAnalysis != null) parts += "瑙ｆ瀽"
    return parts.joinToString("銆")
}

private fun suggestedTypeLabel(type: String): String = when (type.lowercase()) {
    "single" -> "鍗曢€夐"
    "multiple" -> "澶氶€夐"
    "judge" -> "鍒ゆ柇棰"
    "blank" -> "濉┖棰"
    "short" -> "绠€绛旈"
    else -> type
}

private fun applyAiReviewSuggestion(question: Question, suggestion: AiReviewSuggestion): Question {
    var next = question
    suggestion.suggestedType?.let { typeText ->
        suggestedQuestionType(typeText)?.let { targetType ->
            next = normalizeAfterTypeChange(next, targetType)
        }
    }
    suggestion.suggestedQuestion?.let { suggestedQuestion ->
        next = next.copy(question = suggestedQuestion)
    }
    if (suggestion.suggestedOptions.isNotEmpty()) {
        next = next.copy(options = suggestion.suggestedOptions)
    } else if (next.type == QuestionType.JUDGE && next.options.isEmpty()) {
        next = next.copy(options = defaultJudgeOptions())
    }
    if (suggestion.suggestedAnswer.isNotEmpty()) {
        next = next.copy(
            answer = normalizeSuggestedAnswer(suggestion.suggestedAnswer, next.type),
            blankAnswers = emptyList()
        )
    }
    suggestion.suggestedAnalysis?.let { suggestedAnalysis ->
        next = next.copy(analysis = suggestedAnalysis)
    }
    return next
}

private fun suggestedQuestionType(type: String): QuestionType? = when (type.trim().lowercase()) {
    "single", "鍗曢€", "鍗曢€夐" -> QuestionType.SINGLE
    "multiple", "multi", "澶氶€", "澶氶€夐" -> QuestionType.MULTIPLE
    "judge", "true_false", "鍒ゆ柇", "鍒ゆ柇棰" -> QuestionType.JUDGE
    "blank", "濉┖", "濉┖棰" -> QuestionType.BLANK
    "short", "essay", "绠€绛", "绠€绛旈" -> QuestionType.SHORT
    else -> null
}

private fun normalizeSuggestedAnswer(answer: List<String>, type: QuestionType): List<String> {
    val normalized = answer
        .flatMap { item -> item.split(',', '锛?, '銆?, '/', ' ') }
        .map { it.trim().uppercase() }
        .filter { it.isNotBlank() }
    return when (type) {
        QuestionType.SINGLE -> normalized.take(1)
        QuestionType.JUDGE -> normalized.map { value ->
            when (value) {
                "姝ｇ‘", "瀵", "TRUE", "T" -> "A"
                "閿欒", "閿", "FALSE", "F" -> "B"
                else -> value.take(1)
            }
        }.take(1)
        QuestionType.MULTIPLE -> normalized.distinct().sorted()
        else -> normalized
    }
}

private fun analysisTargetQuestions(
    questions: List<Question>,
    anomalyQuestions: List<Question>,
    onlyAnomaly: Boolean
): List<Question> {
    val source = if (onlyAnomaly) anomalyQuestions else questions
    return source.filter(::shouldApplyAiAnalysis)
}

private fun shouldApplyAiAnalysis(question: Question): Boolean {
    val clean = question.analysis.trim()
    val missingOrShortAnalysis = clean.isBlank() || clean.length < 20 || clean == "鏃" || clean == "鏆傛棤瑙ｆ瀽"
    val subjectiveMissingAnswer = question.type == QuestionType.SHORT && question.answer.isEmpty() && question.options.isEmpty()
    return missingOrShortAnalysis || subjectiveMissingAnswer
}

private fun applyAiAnalysisSuggestion(question: Question, generatedAnalysis: String): Question {
    val clean = generatedAnalysis.trim()
    if (clean.isBlank()) return question
    return if (question.type == QuestionType.SHORT && question.answer.isEmpty() && question.options.isEmpty()) {
        question.copy(
            answer = listOf(clean),
            analysis = clean
        )
    } else {
        question.copy(analysis = clean)
    }
}



private fun duplicateQuestionNumberWarnings(questions: List<Question>): List<ImportWarning> {
    val duplicateGroups = questions
        .filter { it.number.trim().isNotBlank() }
        .groupBy { duplicateQuestionNumberScopeKey(it) }
        .filterValues { it.size > 1 }
    return duplicateGroups.values.flatten().map { question ->
        ImportWarning(
            level = WarningLevel.WARNING,
            questionNumber = question.number,
            message = "鍚屼竴鍒嗗尯/棰樺瀷鍐呴鍙烽噸澶嶏細寤鸿浜哄伐纭銆傚鍏ラ瑙堝凡鎸夊唴閮ㄩ鐩甀D鍖哄垎锛岄伩鍏嶄粎鎸夐鍙锋贩娣嗐€傦紱$IMPORT_WARNING_ID_MARKER${question.id}"
        )
    }
}

private fun duplicateQuestionNumberScopeKey(question: Question): String {
    return listOf(
        normalizeQuestionCategoryScope(question.category),
        question.type.name,
        normalizeQuestionNumber(question.number)
    ).joinToString("|")
}

private fun normalizeQuestionCategoryScope(category: String): String {
    return category.trim().replace(Regex("""\s+"""), " ")
}

private fun normalizeQuestionNumber(number: String): String {
    val clean = number.trim()
    val trimmedZero = clean.trimStart('0')
    return trimmedZero.ifBlank { clean }
}

private enum class ReviewFilter {
    ALL,
    ANOMALY,
    NO_ANSWER,
    MISSING_ANALYSIS,
    IMAGE,
    HARD_ERROR,
    AI_REVIEWED,
    AI_SUGGESTION,
    AI_APPLICABLE,
    AI_NEED_REVIEW,
    AI_HARD_ERROR,
    AI_ANALYZED
}

private fun reviewFilterFromName(name: String): ReviewFilter {
    return runCatching { ReviewFilter.valueOf(name) }.getOrDefault(ReviewFilter.ALL)
}

private fun reviewFilterLabel(filter: ReviewFilter): String = when (filter) {
    ReviewFilter.ALL -> "鍏ㄩ儴"
    ReviewFilter.ANOMALY -> "浠呭紓甯"
    ReviewFilter.NO_ANSWER -> "浠呮棤绛旀"
    ReviewFilter.MISSING_ANALYSIS -> "缂?鐭В鏋"
    ReviewFilter.IMAGE -> "浠呭浘鐗囬"
    ReviewFilter.HARD_ERROR -> "浠呯‖閿欒"
    ReviewFilter.AI_REVIEWED -> "AI宸叉牳瀵"
    ReviewFilter.AI_SUGGESTION -> "浠匒I寤鸿"
    ReviewFilter.AI_APPLICABLE -> "浠呭彲閲囩撼"
    ReviewFilter.AI_NEED_REVIEW -> "浠呴渶纭"
    ReviewFilter.AI_HARD_ERROR -> "AI纭敊璇"
    ReviewFilter.AI_ANALYZED -> "AI宸茶ˉ瑙ｆ瀽"
}

private fun reviewFilterCount(
    questions: List<Question>,
    warnings: List<ImportWarning>,
    aiSuggestions: List<AiReviewSuggestion> = emptyList(),
    aiReviewedQuestionIds: List<String> = emptyList(),
    aiAnalyzedQuestionIds: List<String> = emptyList(),
    aiAnalysisAppliedQuestionIds: List<String> = emptyList(),
    filter: ReviewFilter
): Int {
    if (filter == ReviewFilter.ALL) return questions.size
    return questions.indices.count { index ->
        val question = questions[index]
        questionMatchesFilter(
            question = question,
            warnings = warningsForQuestion(question, warnings),
            filter = filter,
            aiSuggestions = aiSuggestionsForQuestion(question, aiSuggestions),
            aiReviewedQuestionIds = aiReviewedQuestionIds,
            aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
            aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds
        )
    }
}

private fun firstMatchingQuestionIndex(
    questions: List<Question>,
    warnings: List<ImportWarning>,
    aiSuggestions: List<AiReviewSuggestion> = emptyList(),
    aiReviewedQuestionIds: List<String> = emptyList(),
    aiAnalyzedQuestionIds: List<String> = emptyList(),
    aiAnalysisAppliedQuestionIds: List<String> = emptyList(),
    filter: ReviewFilter
): Int? {
    if (filter == ReviewFilter.ALL) return questions.indices.firstOrNull()
    return questions.indices.firstOrNull { index ->
        val question = questions[index]
        questionMatchesFilter(
            question = question,
            warnings = warningsForQuestion(question, warnings),
            filter = filter,
            aiSuggestions = aiSuggestionsForQuestion(question, aiSuggestions),
            aiReviewedQuestionIds = aiReviewedQuestionIds,
            aiAnalyzedQuestionIds = aiAnalyzedQuestionIds,
            aiAnalysisAppliedQuestionIds = aiAnalysisAppliedQuestionIds
        )
    }
}

private fun warningsForQuestion(question: Question, warnings: List<ImportWarning>): List<ImportWarning> {
    return dedupeImportWarnings(
        warnings.filter { warning ->
            warningBelongsToQuestion(warning, question)
        }
    )
}

private fun questionMatchesFilter(
    question: Question,
    warnings: List<ImportWarning>,
    filter: ReviewFilter,
    aiSuggestions: List<AiReviewSuggestion> = emptyList(),
    aiReviewedQuestionIds: List<String> = emptyList(),
    aiAnalyzedQuestionIds: List<String> = emptyList(),
    aiAnalysisAppliedQuestionIds: List<String> = emptyList()
): Boolean {
    return when (filter) {
        ReviewFilter.ALL -> true
        ReviewFilter.ANOMALY -> hasReviewAnomaly(question, warnings)
        ReviewFilter.NO_ANSWER -> question.answer.isEmpty()
        ReviewFilter.MISSING_ANALYSIS -> shouldApplyAiAnalysis(question)
        ReviewFilter.IMAGE -> question.images.isNotEmpty()
        ReviewFilter.HARD_ERROR -> hasHardReviewError(question, warnings)
        ReviewFilter.AI_REVIEWED -> question.id in aiReviewedQuestionIds.toSet()
        ReviewFilter.AI_SUGGESTION -> aiSuggestions.any(::isActionableAiSuggestion)
        ReviewFilter.AI_APPLICABLE -> aiSuggestions.any(::canApplyAiSuggestion)
        ReviewFilter.AI_NEED_REVIEW -> aiSuggestions.any(::isNeedHumanReviewAiSuggestion)
        ReviewFilter.AI_HARD_ERROR -> aiSuggestions.any(::isHardErrorAiSuggestion)
        ReviewFilter.AI_ANALYZED -> question.id in aiAnalysisAppliedQuestionIds.toSet()
    }
}

private fun hasReviewAnomaly(question: Question, warnings: List<ImportWarning>): Boolean {
    return hasSoftReviewWarning(question, warnings) || hasHardReviewError(question, warnings)
}

private fun hasSoftReviewWarning(question: Question, warnings: List<ImportWarning>): Boolean {
    val hasWarning = warnings.any { it.level == WarningLevel.WARNING }
    val choiceType = question.type in listOf(QuestionType.SINGLE, QuestionType.MULTIPLE)
    val optionCountWarning = choiceType && question.options.size == 1
    return hasWarning || question.answer.isEmpty() || optionCountWarning
}

private fun hasHardReviewError(question: Question, warnings: List<ImportWarning>): Boolean {
    if (warnings.any { it.level == WarningLevel.ERROR }) return true
    if (question.question.isBlank()) return true
    val choiceType = question.type in listOf(QuestionType.SINGLE, QuestionType.MULTIPLE)
    if (choiceType && question.options.isEmpty()) return true
    if (question.type == QuestionType.SINGLE && question.answer.size > 1) return true
    if (choiceType && question.answer.isNotEmpty()) {
        val optionKeys = question.options.map { it.key.uppercase() }.toSet()
        if (optionKeys.isNotEmpty() && question.answer.any { it.uppercase() !in optionKeys }) return true
    }
    if (question.type == QuestionType.JUDGE && question.answer.isNotEmpty()) {
        val allowed = setOf("A", "B", "姝ｇ‘", "閿欒", "瀵?, "閿?)
        if (question.answer.any { it.uppercase() !in allowed }) return true
    }
    return false
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
    warnings: List<ImportWarning>,
    onIndexChange: (Int) -> Unit,
    onEditQuestion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Text(
            text = "褰撳墠绛涢€夊垪琛",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        val pageSize = 5
        val currentPosition = indices.indexOf(currentIndex).takeIf { it >= 0 } ?: 0
        val pageStart = (currentPosition / pageSize) * pageSize
        val pageEnd = (pageStart + pageSize).coerceAtMost(indices.size)
        val pageIndices = indices.subList(pageStart, pageEnd)

        pageIndices.forEach { index ->
            val question = questions[index]
            val warningCount = warningsForQuestion(question, warnings).count { it.level != WarningLevel.NORMAL }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ShirohaRadius.Md),
                color = if (index == currentIndex) ShirohaColors.BrandPrimarySoft else ShirohaColors.CardWhite78,
                border = BorderStroke(
                    1.dp,
                    if (index == currentIndex) ShirohaColors.LineSelected else ShirohaColors.LineStrong
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .shirohaNoRippleClickable { onIndexChange(index) }
                    ) {
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
                        if (warningCount > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "鎻愮ず $warningCount 鏉",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    ReviewCompactButton(
                        icon = Icons.Rounded.Edit,
                        text = "缂栬緫",
                        onClick = { onEditQuestion(index) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (indices.size > pageSize) {
            NoticeCard(
                "褰撳墠绛涢€夊叡鏈?${indices.size} 棰橈紝姝ｅ湪鏄剧ず绗?${pageStart + 1}-${pageEnd} 鏉★紱鍙敤鈥滀笂涓€鏉?/ 涓嬩竴鏉♀€濈户缁牳瀵广€",
                warning = false
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
            "A", "姝ｇ‘", "瀵?, "鏄?, "TRUE", "T", "鈭?, "鉁?, "鉁?, "鉁?, "鈽" -> "A"
            "B", "閿欒", "閿?, "鍚?, "FALSE", "F", "脳", "X", "鉁?, "鉁?, "鉂?, "鉂? -> "B"
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

    val compactLetters = clean.uppercase().replace(Regex("[\\s,锛屻€?锛?锛沒+"), "")
    if (compactLetters.matches(Regex("^[A-H]{1,8}$"))) {
        return compactLetters.map { it.toString() }.distinct()
    }

    return clean
        .replace("锛", ",")
        .replace("銆", ",")
        .replace("/", ",")
        .replace("锛", ",")
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
            "A", "姝ｇ‘", "瀵?, "鏄?, "TRUE", "T", "鈭?, "鉁?, "鉁?, "鉁?, "鈽" -> "姝ｇ‘"
            "B", "閿欒", "閿?, "鍚?, "FALSE", "F", "脳", "X", "鉁?, "鉁?, "鉂?, "鉂? -> "閿欒"
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

private fun queryFileName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val index = it.getColumnIndex("_display_name")
        if (index >= 0 && it.moveToFirst()) {
            return it.getString(index) ?: "鏈懡鍚嶆枃浠"
        }
    }
    return uri.lastPathSegment ?: "鏈懡鍚嶆枃浠"
}

private data class ImportFileSizeCheck(
    val warnMessage: String? = null,
    val blockMessage: String? = null
)

private fun checkImportFileSize(context: Context, uri: Uri, fileName: String): ImportFileSizeCheck {
    val size = queryFileSize(context, uri) ?: return ImportFileSizeCheck()
    return when {
        size > IMPORT_FILE_BLOCK_BYTES -> ImportFileSizeCheck(
            blockMessage = "鏂囦欢杩囧ぇ锛$fileName锛堢害 ${formatFileSize(size)}锛夈€傚缓璁厛鍘嬬缉鍥剧墖銆佹媶鍒嗛搴擄紝鎴栨敼鐢ㄦ爣鍑嗘枃鏈?Excel/JSON 瀵煎叆銆"
        )
        size > IMPORT_FILE_WARN_BYTES -> ImportFileSizeCheck(
            warnMessage = "鏂囦欢杈冨ぇ锛$fileName锛堢害 ${formatFileSize(size)}锛夛紝鍚ぇ鍥炬垨澶嶆潅 Word 鍐呭鏃跺彲鑳借鍙栬緝鎱€"
        )
        else -> ImportFileSizeCheck()
    }
}

private fun queryFileSize(context: Context, uri: Uri): Long? {
    val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
    cursor?.use {
        val index = it.getColumnIndex(OpenableColumns.SIZE)
        if (index >= 0 && it.moveToFirst()) {
            val size = it.getLong(index)
            if (size > 0) return size
        }
    }
    return null
}

private fun formatFileSize(bytes: Long): String {
    val mb = bytes.toDouble() / 1024.0 / 1024.0
    return if (mb >= 10) {
        "${mb.roundToInt()} MB"
    } else {
        String.format(Locale.ROOT, "%.1f MB", mb)
    }
}

private fun readImportedContent(
    context: Context,
    uri: Uri,
    fileName: String
): QuestionImportAssetExtractor.DecodeResult {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: return QuestionImportAssetExtractor.DecodeResult.Failure("鏂囦欢鏃犳硶璇诲彇锛岃纭鏂囦欢浠嶅彲璁块棶銆")
    return QuestionImportAssetExtractor.decodeDetailed(context, bytes, fileName)
}

private fun readImportedText(context: Context, uri: Uri, fileName: String): TextImportDecoder.DecodeResult {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: return TextImportDecoder.DecodeResult.Failure("鏂囦欢鏃犳硶璇诲彇锛岃纭鏂囦欢浠嶅彲璁块棶銆")
    return TextImportDecoder.decodeDetailed(bytes, fileName)
}

private fun sampleImportText(): String = """
1. 瀹夊叏甯界殑涓昏浣滅敤鏄紙A锛?A. 淇濇姢澶撮儴
B. 瑁呴グ浣滅敤
C. 澧炲姞閲嶉噺
D. 鏃犲疄闄呬綔鐢?绛旀锛欰
瑙ｆ瀽锛氬畨鍏ㄥ附鐢ㄤ簬鍑忚交鍧犺惤鐗╁拰纰版挒瀵瑰ご閮ㄩ€犳垚鐨勪激瀹炽€?
2. 闆ㄥぉ椹鹃┒鏃跺簲娉ㄦ剰鍝簺浜嬮」锛圓B锛?A. 闄嶄綆杞﹂€?B. 鍔犲ぇ璺熻溅璺濈
C. 鎬ユ墦鏂瑰悜
D. 绱ф€ュ埗鍔?绛旀锛欰B
瑙ｆ瀽锛氶洦澶╄矾婊戯紝搴斿钩绋虫帶鍒惰溅杈嗗苟鐣欒冻瀹夊叏璺濈銆?
3. 鍥藉瀹夊叏鐢熶骇鏂归拡鏄€滃畨鍏ㄧ涓€锛岄闃蹭负涓烩€濄€傦紙瀵癸級
绛旀锛氬
瑙ｆ瀽锛氳繖鏄竴閬撳熀纭€鍒ゆ柇棰橈紝绛旀涓烘纭€"""".trimIndent()

private fun sampleAnswerText(): String = """
1. A
2. AB
3. 瀵"""".trimIndent()

@Composable
private fun ImportStepHeroCard() {
    val density = LocalDensity.current
    val floatDistancePx = with(density) { ShirohaMotion.HeroFloatDistance.toPx() }
    val heroFloat = rememberInfiniteTransition(label = "import_illustration_float")
    val imageOffsetY by heroFloat.animateFloat(
        initialValue = -floatDistancePx,
        targetValue = floatDistancePx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = ShirohaMotion.HeroFloatMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "import_illustration_float_y"
    )

    GlassCard(
        modifier = Modifier.height(ShirohaDimens.HeroCardHeight),
        contentPadding = ShirohaSpacing.Xl
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ImportStepPill(index = "1", text = "瀵煎叆鏂囦欢", selected = true)
                ImportStepPill(index = "2", text = "鏍稿缁撴灉", selected = false)
                ImportStepPill(index = "3", text = "鍒涘缓棰樺簱", selected = false)
            }
            if (QuizRepository.shirohaModeEnabled) {
                Box(
                    modifier = Modifier.size(ShirohaDimens.HeroImageFrameSize),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.illus_import_hint_webp),
                        contentDescription = null,
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

private enum class ImportSaveMode {
    NEW_BANK,
    APPEND_TO_BANK
}

private fun defaultImportBankName(fileName: String): String {
    return fileName
        .takeIf { it.isNotBlank() && it != "鏈€夋嫨鏂囦欢" }
        ?.substringBeforeLast('.')
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: "瀵煎叆棰樺簱"
}

@Composable
private fun ImportStepPill(
    index: String,
    text: String,
    selected: Boolean
) {
    Surface(
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = if (selected) ShirohaColors.BrandPrimarySoft else ShirohaColors.CardMuted,
        border = BorderStroke(ShirohaDimens.Hairline, if (selected) ShirohaColors.LineSelected else ShirohaColors.LineSoft),
        modifier = Modifier
            .width(ShirohaDimens.StepPillWidth)
            .defaultMinSize(minHeight = ShirohaDimens.StepPillMinHeight)
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
                color = if (selected) MaterialTheme.colorScheme.primary else ShirohaColors.TextSecondary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
