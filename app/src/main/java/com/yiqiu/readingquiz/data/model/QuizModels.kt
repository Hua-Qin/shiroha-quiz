package com.yiqiu.readingquiz.data.model

/**
 * 答题模型（题型、用户作答、答题会话）。
 * 多空填空通过 blankAnswers 列表承载，单空时列表长度为 1。
 */
enum class QuestionType { SINGLE, MULTIPLE, JUDGE, BLANK, SHORT }

data class Option(
    val key: String,
    val text: String
)

data class Question(
    val id: String,
    val type: QuestionType,
    val question: String,
    val options: List<Option>,
    val answer: List<String>,
    val blankAnswers: List<String>,
    val analysis: String,
    val category: String
)

data class UserAnswer(
    val questionId: String,
    val selectedKeys: List<String>,
    val blankInputs: List<String>,
    val shortAnswer: String,
    val judged: Boolean,
    val correct: Boolean,
    val submittedAt: Long
)

data class QuizSession(
    val id: String,
    val articleId: String,
    val questions: List<Question>,
    val answers: List<UserAnswer>,
    val markedForReview: List<String>,
    val currentIndex: Int,
    val startedAt: Long,
    val durationMs: Long,
    val completed: Boolean
)