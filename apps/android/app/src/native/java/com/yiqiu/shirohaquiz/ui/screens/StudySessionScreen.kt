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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.importer.model.Option
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.KnowledgeCourse
import com.yiqiu.shirohaquiz.state.KnowledgeSection
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.ArticleReader
import com.yiqiu.shirohaquiz.ui.components.EditorialFigure
import com.yiqiu.shirohaquiz.ui.components.EditorialSection
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.ShirohaTypography
import com.yiqiu.shirohaquiz.ui.theme.editorialScaleFor
import com.yiqiu.shirohaquiz.ui.theme.screenClassFor

/**
 * 瀛︿範浼氳瘽闃舵鐘舵€佹満锛? *  - LEARN锛氶槄璇婚樁娈碉紝浠呭睍绀烘枃绔犲紡瀛︿範鍐呭锛屼笉鏄剧ず浠讳綍棰樼洰
 *  - QUIZ 锛氱瓟棰橀樁娈碉紝涓嶆樉绀哄涔犲唴瀹癸紙鍙€氳繃?鏌ョ湅鐭ヨ瘑鐐?鎵撳紑鍗婂睆瑕嗙洊灞傦級
 *  - DONE 锛氱粌涔犲畬鎴愶紝灞曠ず鎬荤粨
 */
private enum class StudySessionPhase { LEARN, QUIZ, DONE }

/**
 * 瀛︿範浼氳瘽椤碉細涓ら樁娈靛垎绂伙紙鏆栬壊缂栬緫鏉傚織椋庨噸鍐欙級
 *
 * 闃舵 1锛圠EARN锛夛細闃呰鏂囩珷 鈫?銆屾垜宸插瀹岋紝寮€濮嬬粌涔犮€? * 闃舵 2锛圦UIZ锛夛細绾瓟棰?鈫?椤舵爮鍙墦寮€"鏌ョ湅鐭ヨ瘑鐐?鍙瑕嗙洊灞? * 闃舵 3锛圖ONE锛夛細鎬荤粨鍗★紙EditorialFigure 脳N 澶ф暟瀛楋級鈫?閲嶆柊缁冧範 / 涓嬩竴绔犺妭 / 杩斿洖
 *
 * 甯冨眬鑷笂鑰屼笅锛? *  1. ShirohaHeader锛坘icker=璇剧▼鍚?/ title=褰撳墠闃舵 / subtitle=闃舵鎻忚堪锛? *  2. 闃舵杩涘害鏉?EditorialSection + 3 涓?EditorialFigure锛圠EARN / QUIZ / DONE锛? *  3. 鍐呭鍖烘寜 phase 鍒囨崲锛歀EARN锛圓rticleReader锛? QUIZ锛堥鐩級/ DONE锛堢粨鏋$EditorialFigure 脳N锛? */
@Composable
fun StudySessionScreen(
    courseId: String,
    sectionId: String,
    onBack: () -> Unit,
    onNextSection: (String) -> Unit
) {
    val course = QuizRepository.courseById(courseId)
    val section = course?.sections?.firstOrNull { it.id == sectionId }
    if (course == null || section == null) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .shirohaEditorialBackground()
        ) {
            val screenClass = screenClassFor(maxWidth)
            val scale = editorialScaleFor(screenClass)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "绔犺妭涓嶅瓨鍦",
                    style = if (scale == 1f) {
                        ShirohaTypography.headlineSmall
                    } else {
                        ShirohaTypography.headlineSmall.copy(
                            fontSize = (ShirohaTypography.headlineSmall.fontSize.value * scale).sp,
                            lineHeight = (ShirohaTypography.headlineSmall.lineHeight.value * scale).sp
                        )
                    }
                )
                Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
                ActionPillButton(
                    icon = Icons.Rounded.ArrowBack,
                    text = "杩斿洖",
                    primary = false,
                    onClick = onBack
                )
            }
        }
        return
    }

    val questions = remember(sectionId, courseId) {
        QuizRepository.questionsForSection(courseId, sectionId)
    }

    // 璇剧▼鍏宠仈棰樺簱锛氱敤浜庨敊棰樻湰鍐欏叆銆傞搴撻殢 courseId 缂撳瓨銆?    val linkedBank = remember(courseId) {
        QuizRepository.courseById(courseId)?.linkedBankId?.let { bid ->
            QuizRepository.banks.firstOrNull { it.id == bid }
        }
    }

    // 闃舵鐘舵€佹満锛氳繘鍏ラ〉闈㈤粯璁や负瀛︿範闃呰闃舵
    var phase by remember { mutableStateOf(StudySessionPhase.LEARN) }
    var currentIdx by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<List<String>>(emptyList()) }
    var submitted by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }
    var showKnowledgeDialog by remember { mutableStateOf(false) }

    // 杩涘叆椤甸潰锛氭爣璁板凡瀛︿範锛堜粎 LEARN 闃舵鍒濇杩涘叆鏃惰Е鍙戯級
    LaunchedEffect(sectionId) {
        QuizRepository.markSectionStudied(courseId, sectionId)
    }

    // DONE 瀹堥棬锛氭瘡娆?phase 鍙樹负 DONE 鏃惰褰曚竴娆＄粌涔犵粨鏋?    // 娉ㄦ剰锛氫娇鐢?phase 浣滀负 key锛屼笖鍑芥暟浣撳唴鐢?if 瀹堥棬锛岀‘淇濇瘡娆￠噸鏂拌繘鍏?DONE 鏃堕兘浼氭墽琛?    // 锛堝鏋滅敤 LaunchedEffect(Unit) 鍦ㄣ€岄噸鏂扮粌涔犮€嶅悗閲嶆柊杩涘叆 DONE 鏃朵笉浼氳Е鍙戯級
    // Step 2 璋冩暣锛氭瘡棰樼粨鏋滃凡鍦?onSubmit 鍐呴€氳繃 markSectionQuestionResult 绱姞鍒?SectionProgress锛?    // 杩欓噷涓嶅啀璋冪敤 recordSectionResult锛堥伩鍏嶈鐩栭€愰绱姞鐨勬纭巼锛夈€?    LaunchedEffect(phase) {
        // 鏁呮剰鐣欑┖锛氫細璇濈粨鏋滅敱 onSubmit 閫愰绱姞锛孌ONE 闃舵涓嶅啀鍋氳鐩栧紡鍐欏叆銆?    }

    val currentQuestion = questions.getOrNull(currentIdx)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shirohaEditorialBackground()
    ) {
        val screenClass = screenClassFor(maxWidth)
        val scale = editorialScaleFor(screenClass)

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ShirohaHeader(
                kicker = section.chapterTitle.ifBlank { "璇剧▼瀛︿範" },
                title = section.sectionTitle.ifBlank { section.id },
                subtitle = when (phase) {
                    StudySessionPhase.LEARN -> "闃舵 1 / 闃呰瀛︿範鍐呭"
                    StudySessionPhase.QUIZ -> "闃舵 2 / 瀹屾垚缁冧範棰"
                    StudySessionPhase.DONE -> "闃舵 3 / 缁冧範鎬荤粨"
                },
                scale = scale
            )

            // 闃舵杩涘害鏉★紙涓嶅弬涓庨樁娈靛唴瀹规粴鍔紝鐙珛鍛堢幇锛夛細3 涓?EditorialFigure
            PhaseIndicatorRow(phase = phase, scale = scale)

            when (phase) {
                StudySessionPhase.LEARN -> {
                    LearnPhase(
                        section = section,
                        questionsSize = questions.size,
                        onStartPractice = {
                            phase = StudySessionPhase.QUIZ
                        },
                        onBack = onBack
                    )
                }
                StudySessionPhase.QUIZ -> {
                    QuizPhase(
                        questions = questions,
                        currentQuestion = currentQuestion,
                        currentIdx = currentIdx,
                        selectedAnswer = selectedAnswer,
                        submitted = submitted,
                        onSelectAnswer = { selectedAnswer = it },
                        onSubmit = {
                            val q = currentQuestion
                            if (q != null) {
                                val isCorrect = QuizRepository.judgeAnswer(q, selectedAnswer)
                                if (isCorrect) correctCount++

                                // 1) 鍐欏叆閿欓鏈細绛旈敊鍏ラ敊棰橈紝绛斿鍒欎粠閿欓鏈疮鍔犳纭鏁?                                val bank = linkedBank
                                if (bank != null) {
                                    if (isCorrect) {
                                        // markWrongQuestionRight 鏄?private锛岃繖閲屾敼涓鸿蛋 addWrongContext 鐨勫叕寮€閲嶈浇涓嶅瓨鍦ㄥ搴旀竻闄よ矾寰勶紝
                                        // 鏀逛负璇诲彇閿欓鏈潯鐩悗鐢?copy 璋冩暣锛堜笉渚濊禆 private 鏂规硶锛夈€傚鏋滈鐩笉鍦ㄩ敊棰樻湰鍒欑洿鎺ヨ烦杩囥€?                                        val idx = QuizRepository.wrongBook.indexOfFirst {
                                            it.bankId == bank.id && it.question.id == q.id
                                        }
                                        if (idx >= 0) {
                                            val entry = QuizRepository.wrongBook[idx]
                                            val now = System.currentTimeMillis()
                                            val nextRightCount = entry.rightCount + 1
                                            val nextReviewRightCount = entry.reviewRightCount + 1
                                            val nextStreak = entry.streakCorrectCount + 1
                                            QuizRepository.wrongBook[idx] = entry.copy(
                                                rightCount = nextRightCount,
                                                reviewRightCount = nextReviewRightCount,
                                                streakCorrectCount = nextStreak,
                                                lastCorrectAt = now,
                                                lastReviewedAt = now,
                                                updatedAt = now
                                            )
                                            QuizRepository.persist()
                                        }
                                    } else {
                                        // 绛旈敊锛氬鐢ㄧ幇鏈?addWrongContext(bank, question, userAnswer, source, correctAnswer, addedManually)
                                        QuizRepository.addWrongContext(
                                            bank = bank,
                                            question = q,
                                            userAnswer = selectedAnswer,
                                            source = "杈瑰杈圭瓟",
                                            correctAnswer = q.answer,
                                            addedManually = false
                                        )
                                    }
                                }

                                // 2) 鍐欏叆 studyRecord锛堥€愰璁板綍锛?                                // recordPracticeResult 褰撳墠鏄?private锛涙敼涓虹洿鎺ユ瀯閫$StudyRecord 鍐欏叆 studyRecords 鍒楄〃
                                val record = com.yiqiu.shirohaquiz.state.StudyRecord(
                                    id = "study_${q.id}_${System.currentTimeMillis()}",
                                    bankId = bank?.id,
                                    bankName = bank?.name ?: "鏈懡鍚嶉搴",
                                    source = "杈瑰杈圭瓟",
                                    title = q.question.take(24),
                                    total = 1,
                                    correct = if (isCorrect) 1 else 0,
                                    timestamp = System.currentTimeMillis(),
                                    questionResults = listOf(
                                        com.yiqiu.shirohaquiz.state.StudyQuestionResult(
                                            question = q,
                                            userAnswer = selectedAnswer,
                                            correct = isCorrect,
                                            answerText = q.answer.joinToString(" / ").ifBlank { "鏈瘑鍒瓟妗" },
                                            autoScored = true,
                                            sourceBankId = bank?.id,
                                            sourceBankName = bank?.name
                                        )
                                    ),
                                    scopeType = "course",
                                    scopeName = course.courseName
                                )
                                QuizRepository.studyRecords.add(0, record)
                                QuizRepository.persist()

                                // 3) 鏇存柊 SectionProgress锛歮arkSectionQuestionResult 灏氭湭鍦?QuizRepository 鏆撮湶锛?                                // 鐢?studyProgressKey 鐩存帴绱姞锛堜笌 recordSectionResult 瀛楁璇箟涓€鑷达級
                                val key = QuizRepository.studyProgressKey(courseId, sectionId)
                                val cur = QuizRepository.studyProgress[key]
                                    ?: com.yiqiu.shirohaquiz.state.SectionProgress()
                                val newCorrect = cur.correctCount + (if (isCorrect) 1 else 0)
                                val newTotal = cur.totalCount + 1
                                QuizRepository.studyProgress[key] = cur.copy(
                                    studied = true,
                                    practiced = true,
                                    correctCount = newCorrect,
                                    totalCount = newTotal,
                                    lastStudiedAt = System.currentTimeMillis(),
                                    bestAccuracy = maxOf(
                                        cur.bestAccuracy,
                                        newCorrect.toFloat() / newTotal.coerceAtLeast(1).toFloat()
                                    ),
                                    practiceCount = cur.practiceCount + 1
                                    practiceCount = cur.practiceCount + 1
                                QuizRepository.persist()
                            }
                            submitted = true
                        },
                        onNext = {
                            submitted = false
                            selectedAnswer = emptyList()
                            if (currentIdx + 1 >= questions.size) {
                                // 鍏ㄩ儴绛斿畬锛岃繘鍏ュ畬鎴愭€侊紱璁板綍缁撴灉鐢?LaunchedEffect(finished) 瀹堥棬
                                phase = StudySessionPhase.DONE
                            } else {
                                currentIdx++
                            }
                        },
                        onBackToLearn = {
                            // 鍥炲埌闃呰闃舵鍐嶈涓€閬嶏紙鐢ㄤ簬鍦?QUIZ 涓斁寮冨苟鍥炲埌 LEARN锛?                            phase = StudySessionPhase.LEARN
                        },
                        onShowKnowledge = { showKnowledgeDialog = true }
                    )
                }
                StudySessionPhase.DONE -> {
                    DonePhase(
                        total = questions.size,
                        correct = correctCount,
                        onRetry = {
                            currentIdx = 0
                            selectedAnswer = emptyList()
                            submitted = false
                            correctCount = 0
                            // 閲嶆柊缁冧範锛氬洖鍒?LEARN锛岃鐢ㄦ埛鍐嶈涓€娆＄煡璇嗙偣
                            phase = StudySessionPhase.LEARN
                        },
                        onNext = {
                            nextSectionId(course, sectionId)?.let { onNextSection(it) }
                        },
                        onBack = onBack,
                        hasNext = nextSectionId(course, sectionId) != null
                    )
                }
            }
        }
    }

    // 绛旈闃舵 鏌ョ湅鐭ヨ瘑鐐?鍗婂睆 Dialog 鍙璇诲紑骞抽棶?
    AlertDialog(
        onDismissRequest = { showKnowledgeDialog = false },
        confirmButton = {
            TextButton(onClick = { showKnowledgeDialog = false }) {
                Text("鍏抽棴")
            }
        },
        dismissButton = {
            TextButton(onClick = { showKnowledgeDialog = false }) {
                Text("缁х画绛旈")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = section.sectionTitle.ifBlank { "鐭ヨ瘑鐐" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showKnowledgeDialog = false }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "鍏抽棴",
                        tint = ShirohaColors.TextSecondary
                    )
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ArticleReader(section = section)
            }
        }
    )
}

/**
 * LEARN 闃舵锛氶槄璇昏鍥撅紙浠呮枃绔犲唴瀹癸紝涓嶆樉绀轰换浣曢鐩級
 * 鍐呴儴鐢$BoxWithConstraints 閲嶆柊璁＄畻 scale锛屼繚鐣欏閮ㄧ鍚嶄笉鍙樸€? */
@Composable
private fun LearnPhase(
    section: KnowledgeSection,
    questionsSize: Int,
    onStartPractice: () -> Unit,
    onBack: () -> Unit
) {
    val scale = currentEditorialScale()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ArticleReader(section = section)
        Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
        if (questionsSize > 0) {
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = "鎴戝凡瀛﹀畬锛屽紑濮嬬粌涔狅紙$questionsSize 棰橈級",
                primary = true,
                fillWidthContent = true,
                onClick = onStartPractice
            )
        } else {
            NoticeCard(text = "鏈珷鏆傛棤棰樼洰锛屽彲鐐瑰嚮涓嬫柟鎸夐挳杩斿洖", warning = false)
            ActionPillButton(
                icon = Icons.Rounded.CheckCircle,
                text = "鏍囪涓哄凡瀛︿範骞惰繑鍥",
                primary = true,
                fillWidthContent = true,
                onClick = onBack
            )
        }
    }
}

/**
 * QUIZ 闃舵锛氱瓟棰樿鍥撅紙涓嶆贩鎺掑涔犲唴瀹癸級
 * 鍐呴儴鐢$BoxWithConstraints 閲嶆柊璁＄畻 scale锛屼繚鐣欏閮ㄧ鍚嶄笉鍙樸€? */
@Composable
private fun QuizPhase(
    questions: List<Question>,
    currentQuestion: Question?,
    currentIdx: Int,
    selectedAnswer: List<String>,
    submitted: Boolean,
    onSelectAnswer: (List<String>) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
    onBackToLearn: () -> Unit,
    onShowKnowledge: () -> Unit
) {
    val scale = currentEditorialScale()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        if (questions.isEmpty()) {
            NoticeCard(text = "鏈珷鏆傛棤棰樼洰锛屾棤娉曡繘鍏ョ粌涔", warning = false)
            ActionPillButton(
                icon = Icons.Rounded.ArrowBack,
                text = "杩斿洖瀛︿範",
                primary = false,
                fillWidthContent = true,
                onClick = onBackToLearn
            )
            return@Column
        }

        // 椤堕儴宸ュ叿鏉★細鏌ョ湅鐭ヨ瘑鐐?        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip(text = "绗?${currentIdx + 1} / ${questions.size} 棰", selected = true)
            Spacer(modifier = Modifier.weight(1f))
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = "鏌ョ湅鐭ヨ瘑鐐",
                primary = false,
                onClick = onShowKnowledge
            )
        }

        if (currentQuestion != null) {
            QuestionPracticeBlock(
                question = currentQuestion,
                questionNumber = currentIdx + 1,
                totalQuestions = questions.size,
                selectedAnswer = selectedAnswer,
                onSelect = onSelectAnswer,
                submitted = submitted,
                onSubmit = onSubmit,
                onNext = onNext
            )
        }
    }
}

/**
 * DONE 闃舵锛氭€荤粨瑙嗗浘锛圗ditorialFigure 脳N 澶ф暟瀛楀憟鐜帮級
 * 鍐呴儴鐢$BoxWithConstraints 閲嶆柊璁＄畻 scale锛屼繚鐣欏閮ㄧ鍚嶄笉鍙樸€? */
@Composable
private fun DonePhase(
    total: Int,
    correct: Int,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    hasNext: Boolean
) {
    val scale = currentEditorialScale()
    val accuracy = if (total > 0) correct.toFloat() / total else 0f
    val percent = (accuracy * 100).toInt()
    val wrong = (total - correct).coerceAtLeast(0)
    val color = when {
        accuracy >= 0.8f -> Color(0xFF10B981)
        accuracy >= 0.6f -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        // 瀹屾垚缁撴灉 EditorialSection + EditorialFigure 脳N
        EditorialSection(
            kicker = "Summary",
            title = "缁冧範鎬荤粨",
            scale = scale
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
            ) {
                EditorialFigure(
                    modifier = Modifier.weight(1f),
                    scale = scale,
                    value = "$percent",
                    label = "姝ｇ‘鐜",
                    unit = "%"
                )
                EditorialFigure(
                    modifier = Modifier.weight(1f),
                    scale = scale,
                    value = "$correct",
                    label = "绛斿",
                    unit = "棰"
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
            ) {
                EditorialFigure(
                    modifier = Modifier.weight(1f),
                    scale = scale,
                    value = "$wrong",
                    label = "绛旈敊",
                    unit = "棰"
                )
                EditorialFigure(
                    modifier = Modifier.weight(1f),
                    scale = scale,
                    value = "$total",
                    label = "鎬婚鏁",
                    unit = "棰"
                )
            }
        }

        // 璇勪环鍥炬爣
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(ShirohaRadius.Md),
            color = ShirohaColors.CardWhite86,
            border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ShirohaSpacing.Lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (accuracy >= 0.6f) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
                Text(
                    text = "姝ｇ‘ $correct / $total",
                    style = MaterialTheme.typography.titleMedium,
                    color = ShirohaColors.TextSecondary
                )
            }
        }

        // 鎿嶄綔鎸夐挳
        ActionPillButton(
            icon = Icons.Rounded.PlayArrow,
            text = "閲嶆柊瀛︿範骞剁粌涔",
            primary = false,
            fillWidthContent = true,
            onClick = onRetry
        )
        Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
        if (hasNext) {
            ActionPillButton(
                icon = Icons.Rounded.ArrowForward,
                text = "涓嬩竴绔犺妭",
                primary = true,
                fillWidthContent = true,
                onClick = onNext
            )
            Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
        }
        ActionPillButton(
            icon = Icons.Rounded.ArrowBack,
            text = "杩斿洖绔犺妭鍒楄〃",
            primary = false,
            fillWidthContent = true,
            onClick = onBack
        )
    }
}

/**
 * 闃舵杩涘害鏉★細3 涓$EditorialFigure 涓€琛岋紙LEARN / QUIZ / DONE锛? * 鐙珛浜庨樁娈靛唴瀹规粴鍔紝纭繚鐢ㄦ埛鍦ㄤ换浣曢樁娈甸兘鐪嬪埌褰撳墠浣嶇疆
 */
@Composable
private fun PhaseIndicatorRow(phase: StudySessionPhase, scale: Float = 1f) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
    ) {
        EditorialFigure(
            modifier = Modifier.weight(1f),
            scale = scale,
            value = if (phase == StudySessionPhase.LEARN) "1" else "路",
            label = "LEARN",
            unit = when (phase) {
                StudySessionPhase.LEARN -> "闃呰"
                else -> "鉁"
            }
        )
        EditorialFigure(
            modifier = Modifier.weight(1f),
            scale = scale,
            value = if (phase == StudySessionPhase.QUIZ) "2" else if (phase == StudySessionPhase.DONE) "鉁" else "路",
            label = "QUIZ",
            unit = if (phase == StudySessionPhase.QUIZ) "绛旈" else ""
        )
        EditorialFigure(
            modifier = Modifier.weight(1f),
            scale = scale,
            value = if (phase == StudySessionPhase.DONE) "3" else "路",
            label = "DONE",
            unit = if (phase == StudySessionPhase.DONE) "瀹屾垚" else ""
        )
    }
}

private fun nextSectionId(course: KnowledgeCourse, currentSectionId: String): String? {
    val sections = course.sections.sortedBy { it.order }
    val idx = sections.indexOfFirst { it.id == currentSectionId }
    return if (idx in 0 until sections.size - 1) sections[idx + 1].id else null
}

@Composable
private fun QuestionPracticeBlock(
    question: Question,
    questionNumber: Int,
    totalQuestions: Int,
    selectedAnswer: List<String>,
    onSelect: (List<String>) -> Unit,
    submitted: Boolean,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val scale = currentEditorialScale()
    val isCorrect = remember(submitted, selectedAnswer, question) {
        if (!submitted) null else QuizRepository.judgeAnswer(question, selectedAnswer)
    }
    val correctAnswers = remember(question) {
        when (question.type) {
            QuestionType.JUDGE -> {
                val ans = question.answer.firstOrNull().orEmpty().trim()
                when {
                    ans.contains("閿") || ans.equals("false", true) ||
                        ans.equals("脳", true) || ans.equals("f", true) -> listOf("閿")
                    else -> listOf("瀵")
                }
            }
            else -> question.answer
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShirohaRadius.Md),
        color = ShirohaColors.CardWhite86,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
    ) {
        Column(modifier = Modifier.padding(ShirohaSpacing.Lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(text = "绗?$questionNumber / $totalQuestions 棰", selected = true)
                Spacer(modifier = Modifier.weight(1f))
                StatusChip(text = typeLabel(question.type.name), selected = false)
            }
            Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
            // 棰樼洰棰樺共锛歋erif 澶ф爣棰橈紙ShirohaTypography.headlineSmall锛屾寜 scale 缂╂斁锛?            val headlineStyle = if (scale == 1f) {
                ShirohaTypography.headlineSmall
            } else {
                ShirohaTypography.headlineSmall.copy(
                    fontSize = (ShirohaTypography.headlineSmall.fontSize.value * scale).sp,
                    lineHeight = (ShirohaTypography.headlineSmall.lineHeight.value * scale).sp
                )
            }
            Text(
                text = question.question,
                style = headlineStyle,
                fontWeight = FontWeight.SemiBold,
                color = ShirohaColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(ShirohaSpacing.Md))

            when (question.type) {
                QuestionType.SINGLE, QuestionType.MULTIPLE, QuestionType.JUDGE -> {
                    val optionsToShow = when (question.type) {
                        QuestionType.JUDGE -> listOf(
                            Option("瀵", "瀵"),
                            Option("閿", "閿")
                        )
                        else -> question.options
                    }
                    optionsToShow.forEach { opt ->
                        val isSelected = selectedAnswer.contains(opt.key)
                        val isCorrectOpt = correctAnswers.contains(opt.key)
                        val state = when {
                            !submitted -> {
                                if (isSelected) OptionState.SELECTED else OptionState.NEUTRAL
                            }
                            isCorrectOpt -> OptionState.CORRECT
                            isSelected -> OptionState.WRONG
                            else -> OptionState.NEUTRAL
                        }
                        OptionButton(
                            label = opt.key,
                            text = opt.text,
                            state = state,
                            onClick = {
                                if (submitted) return@OptionButton
                                if (question.type == QuestionType.SINGLE || question.type == QuestionType.JUDGE) {
                                    onSelect(listOf(opt.key))
                                } else {
                                    if (isSelected) {
                                        onSelect(selectedAnswer - opt.key)
                                    } else {
                                        onSelect(selectedAnswer + opt.key)
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
                    }
                }
                QuestionType.BLANK -> {
                    Text(
                        text = "濉┖棰橈細${question.question}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShirohaColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
                    Text(
                        text = "鍙傝€冪瓟妗堬細${question.answer.joinToString(" / ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ShirohaColors.TextSecondary
                    )
                }
                QuestionType.SHORT -> {
                    Text(
                        text = "绠€绛旈锛${question.question}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShirohaColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(ShirohaSpacing.Sm))
                    Text(
                        text = "鍙傝€冪瓟妗堬細${question.answer.joinToString("锛")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ShirohaColors.TextSecondary
                    )
                }
            }

            if (submitted && question.analysis.isNotBlank()) {
                Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
                NoticeCard(
                    text = (if (isCorrect == true) "鉁?鍥炵瓟姝ｇ‘" else "鉁?鍥炵瓟閿欒") + "\n\n瑙ｆ瀽锛${question.analysis}",
                    warning = isCorrect != true
                )
            }

            Spacer(modifier = Modifier.height(ShirohaSpacing.Md))
            if (!submitted) {
                ActionPillButton(
                    icon = Icons.Rounded.CheckCircle,
                    text = "鎻愪氦绛旀",
                    primary = true,
                    fillWidthContent = true,
                    enabled = selectedAnswer.isNotEmpty() || question.type == QuestionType.BLANK || question.type == QuestionType.SHORT,
                    onClick = onSubmit
                )
            } else {
                ActionPillButton(
                    icon = Icons.Rounded.ArrowForward,
                    text = if (currentIsLast(questionNumber, totalQuestions)) "瀹屾垚缁冧範" else "涓嬩竴棰",
                    primary = true,
                    fillWidthContent = true,
                    onClick = onNext
                )
            }
        }
    }
}

private fun currentIsLast(current: Int, total: Int): Boolean = current >= total

private enum class OptionState { NEUTRAL, SELECTED, CORRECT, WRONG }

@Composable
private fun OptionButton(
    label: String,
    text: String,
    state: OptionState,
    onClick: () -> Unit
) {
    val (bg, border, fg) = when (state) {
        OptionState.NEUTRAL -> Triple(
            ShirohaColors.CardWhite78,
            ShirohaColors.LineSoft,
            ShirohaColors.TextPrimary
        )
        OptionState.SELECTED -> Triple(
            ShirohaColors.BrandPrimarySoft,
            ShirohaColors.LineSelected,
            ShirohaColors.BrandPrimary
        )
        OptionState.CORRECT -> Triple(
            Color(0xFFDCFCE7),
            Color(0xFF10B981),
            Color(0xFF047857)
        )
        OptionState.WRONG -> Triple(
            Color(0xFFFEE2E2),
            Color(0xFFEF4444),
            Color(0xFFB91C1C)
        )
    }
    Surface(
        shape = RoundedCornerShape(ShirohaRadius.Md),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = Modifier
            .fillMaxWidth()
            .shirohaNoRippleClickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = fg
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = fg
            )
        }
    }
}

private fun typeLabel(type: String): String = when (type) {
    "SINGLE" -> "鍗曢€"
    "MULTIPLE" -> "澶氶€"
    "JUDGE" -> "鍒ゆ柇"
    "BLANK" -> "濉┖"
    "SHORT" -> "绠€绛"
    else -> type
}

/**
 * 鍦ㄥ瓙 Composable 涓幏鍙栧綋鍓嶅睆骞曞搴旂殑 editorial 缂╂斁鍥犲瓙銆? * 浣跨敤 BoxWithConstraints 鍙栧緱 maxWidth 鍚庤绠?scale锛屼笌涓诲嚱鏁颁繚鎸佷竴鑷淬€? */
@Composable
private fun currentEditorialScale(): Float {
    var scale by remember { mutableStateOf(1f) }
    BoxWithConstraints(modifier = Modifier) {
        scale = editorialScaleFor(screenClassFor(maxWidth))
    }
    return scale
}


