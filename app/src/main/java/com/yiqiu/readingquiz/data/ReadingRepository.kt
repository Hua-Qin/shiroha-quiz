package com.yiqiu.readingquiz.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.yiqiu.readingquiz.data.model.Article
import com.yiqiu.readingquiz.data.model.Question
import com.yiqiu.readingquiz.data.model.QuestionType
import com.yiqiu.readingquiz.data.model.QuizSession
import com.yiqiu.readingquiz.data.model.ReadingNote
import com.yiqiu.readingquiz.data.model.UserAnswer
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * 单例 Repository（参考 native QuizRepository 模式）。
 * 字段名 `store` 而非 `persistence`，规避与 fun persistence() 同名引发的
 * Overload resolution ambiguity（spec 附录 A.3）。
 */
object ReadingRepository {

    private const val TAG = "Repo"

    private lateinit var store: SharedPreferences

    val articles = mutableStateListOf<Article>()
    val notes = mutableStateListOf<ReadingNote>()
    val sessions = mutableStateListOf<QuizSession>()
    val aiConfig = mutableStateOf(AiConfig.EMPTY)

    /**
     * 文章 → 已生成题目缓存（用于持久化 + 跨重启复用）。
     */
    val questionsByArticle = mutableStateMapOf<String, List<Question>>()

    fun init(context: Context) {
        if (::store.isInitialized) return
        store = context.applicationContext.getSharedPreferences(
            "reading_quiz_prefs",
            Context.MODE_PRIVATE
        )
        loadAll()
    }

    // ----------------- 文章 -----------------

    fun addArticle(article: Article) {
        Log.d(TAG, "addArticle: id=${article.id}, title='${article.title}'")
        articles.add(article)
        saveArticles()
    }

    fun deleteArticle(articleId: String) {
        articles.removeAll { it.id == articleId }
        notes.removeAll { it.articleId == articleId }
        sessions.removeAll { it.articleId == articleId }
        saveArticles()
        saveNotes()
        saveSessions()
    }

    fun toggleFavorite(articleId: String) {
        val idx = articles.indexOfFirst { it.id == articleId }
        if (idx >= 0) {
            val newState = !articles[idx].favorite
            Log.d(TAG, "toggleFavorite: id=$articleId → $newState")
            articles[idx] = articles[idx].copy(
                favorite = newState,
                updatedAt = System.currentTimeMillis()
            )
            saveArticles()
        }
    }

    fun getArticle(articleId: String): Article? = articles.firstOrNull { it.id == articleId }

    // ----------------- 笔记 -----------------

    fun addNote(note: ReadingNote) {
        notes.add(note)
        saveNotes()
    }

    fun deleteNote(noteId: String) {
        notes.removeAll { it.id == noteId }
        saveNotes()
    }

    fun notesForArticle(articleId: String): List<ReadingNote> =
        notes.filter { it.articleId == articleId }

    // ----------------- 答题会话 -----------------

    fun getOrCreateSession(articleId: String, questions: List<Question>): QuizSession {
        val existing = sessions.firstOrNull { it.articleId == articleId && !it.completed }
        if (existing != null) return existing
        val session = QuizSession(
            id = UUID.randomUUID().toString(),
            articleId = articleId,
            questions = questions,
            answers = emptyList(),
            markedForReview = emptyList(),
            currentIndex = 0,
            startedAt = System.currentTimeMillis(),
            durationMs = 0L,
            completed = false
        )
        sessions.add(session)
        saveSessions()
        return session
    }

    fun updateAnswer(sessionId: String, answer: UserAnswer) {
        val idx = sessions.indexOfFirst { it.id == sessionId }
        if (idx < 0) return
        val session = sessions[idx]
        val newAnswers = session.answers.filter { it.questionId != answer.questionId } + answer
        sessions[idx] = session.copy(answers = newAnswers)
        saveSessions()
    }

    fun toggleMarked(sessionId: String, questionId: String) {
        val idx = sessions.indexOfFirst { it.id == sessionId }
        if (idx < 0) return
        val session = sessions[idx]
        val newMarked = if (session.markedForReview.contains(questionId)) {
            session.markedForReview - questionId
        } else {
            session.markedForReview + questionId
        }
        sessions[idx] = session.copy(markedForReview = newMarked)
        saveSessions()
    }

    fun advanceToNext(sessionId: String) {
        val idx = sessions.indexOfFirst { it.id == sessionId }
        if (idx < 0) return
        val session = sessions[idx]
        sessions[idx] = session.copy(
            currentIndex = (session.currentIndex + 1).coerceAtMost(session.questions.size - 1)
        )
        saveSessions()
    }

    fun completeSession(sessionId: String, durationMs: Long) {
        val idx = sessions.indexOfFirst { it.id == sessionId }
        if (idx < 0) return
        sessions[idx] = sessions[idx].copy(
            completed = true,
            durationMs = durationMs
        )
        saveSessions()
    }

    fun sessionFor(articleId: String): QuizSession? =
        sessions.firstOrNull { it.articleId == articleId && !it.completed }
            ?: sessions.firstOrNull { it.articleId == articleId }

    // ----------------- AI 配置 -----------------

    fun updateAiConfig(config: AiConfig) {
        Log.i(TAG, "updateAiConfig: baseUrl=${config.apiBaseUrl}, model=${config.modelName}")
        aiConfig.value = config
        saveAiConfig()
    }

    // ----------------- 题目缓存 -----------------

    fun setQuestions(articleId: String, questions: List<Question>) {
        Log.d(TAG, "setQuestions: articleId=$articleId, count=${questions.size}")
        questionsByArticle[articleId] = questions
        saveQuestions()
    }

    fun getQuestions(articleId: String): List<Question> =
        questionsByArticle[articleId] ?: emptyList()

    // ----------------- 持久化 -----------------

    private fun loadAll() {
        loadArticles()
        loadNotes()
        loadSessions()
        loadAiConfig()
        loadQuestions()
    }

    private fun loadArticles() {
        val raw = store.getString("articles_json", null) ?: return
        runCatching {
            val arr = JSONArray(raw)
            articles.clear()
            for (i in 0 until arr.length()) {
                articles.add(articleFromJson(arr.getJSONObject(i)))
            }
        }
    }

    private fun saveArticles() {
        if (!::store.isInitialized) return
        val arr = JSONArray()
        articles.forEach { arr.put(articleToJson(it)) }
        store.edit().putString("articles_json", arr.toString()).apply()
    }

    private fun loadNotes() {
        val raw = store.getString("notes_json", null) ?: return
        runCatching {
            val arr = JSONArray(raw)
            notes.clear()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                notes.add(
                    ReadingNote(
                        id = o.getString("id"),
                        articleId = o.getString("articleId"),
                        content = o.getString("content"),
                        anchorText = o.optString("anchorText", ""),
                        createdAt = o.getLong("createdAt")
                    )
                )
            }
        }
    }

    private fun saveNotes() {
        if (!::store.isInitialized) return
        val arr = JSONArray()
        notes.forEach { n ->
            arr.put(
                JSONObject()
                    .put("id", n.id)
                    .put("articleId", n.articleId)
                    .put("content", n.content)
                    .put("anchorText", n.anchorText)
                    .put("createdAt", n.createdAt)
            )
        }
        store.edit().putString("notes_json", arr.toString()).apply()
    }

    private fun loadSessions() {
        val raw = store.getString("sessions_json", null) ?: return
        runCatching {
            val arr = JSONArray(raw)
            sessions.clear()
            for (i in 0 until arr.length()) {
                sessions.add(sessionFromJson(arr.getJSONObject(i)))
            }
        }
    }

    private fun saveSessions() {
        if (!::store.isInitialized) return
        val arr = JSONArray()
        sessions.forEach { arr.put(sessionToJson(it)) }
        store.edit().putString("sessions_json", arr.toString()).apply()
    }

    private fun loadAiConfig() {
        val raw = store.getString("ai_config_json", null) ?: return
        runCatching {
            val o = JSONObject(raw)
            aiConfig.value = AiConfig(
                apiBaseUrl = o.optString("apiBaseUrl", ""),
                apiKey = o.optString("apiKey", ""),
                modelName = o.optString("modelName", ""),
                timeoutSeconds = o.optInt("timeoutSeconds", 60)
            )
        }
    }

    private fun saveAiConfig() {
        if (!::store.isInitialized) return
        val c = aiConfig.value
        val o = JSONObject()
            .put("apiBaseUrl", c.apiBaseUrl)
            .put("apiKey", c.apiKey)
            .put("modelName", c.modelName)
            .put("timeoutSeconds", c.timeoutSeconds)
        store.edit().putString("ai_config_json", o.toString()).apply()
    }

    /**
     * 题目缓存持久化：顶层 map，每个 articleId → questions JSON array。
     */
    private fun loadQuestions() {
        val raw = store.getString("questions_map_json", null) ?: return
        runCatching {
            val map = JSONObject(raw)
            val keys = map.keys()
            while (keys.hasNext()) {
                val articleId = keys.next()
                val arr = map.optJSONArray(articleId) ?: continue
                val qs = mutableListOf<Question>()
                for (i in 0 until arr.length()) {
                    qs.add(questionFromJson(arr.getJSONObject(i)))
                }
                questionsByArticle[articleId] = qs
            }
            Log.d(TAG, "loadQuestions: ${questionsByArticle.size} article entries")
        }.onFailure {
            Log.w(TAG, "loadQuestions FAILED: ${it.message}", it)
        }
    }

    private fun saveQuestions() {
        if (!::store.isInitialized) return
        val o = JSONObject()
        // 显式标注参数类型规避 questionsByArticle 同时实现 Iterable 与 Map 时的 forEach 重载歧义
        questionsByArticle.forEach { entry: Map.Entry<String, List<Question>> ->
            val articleId = entry.key
            val qs = entry.value
            val arr = JSONArray()
            qs.forEach { q -> arr.put(questionToJson(q)) }
            o.put(articleId, arr)
        }
        store.edit().putString("questions_map_json", o.toString()).apply()
    }

    // ----------------- JSON 互转 -----------------

    private fun articleToJson(article: Article): JSONObject {
        val blocksArr = JSONArray()
        article.blocks.forEach { b ->
            blocksArr.put(blockToJson(b))
        }
        return JSONObject()
            .put("id", article.id)
            .put("title", article.title)
            .put("author", article.author)
            .put("source", article.source)
            .put("category", article.category)
            .put("coverSummary", article.coverSummary)
            .put("blocks", blocksArr)
            .put("favorite", article.favorite)
            .put("createdAt", article.createdAt)
            .put("updatedAt", article.updatedAt)
    }

    private fun blockToJson(b: com.yiqiu.readingquiz.data.model.ArticleBlock): JSONObject = when (b) {
        is com.yiqiu.readingquiz.data.model.ArticleBlock.Paragraph -> {
            val obj = JSONObject()
            obj.put("type", "paragraph")
            obj.put("text", b.text)
            val hlArr = JSONArray()
            b.highlights.forEach { h ->
                hlArr.put(
                    JSONObject()
                        .put("text", h.text)
                        .put("startIndex", h.startIndex)
                        .put("endIndex", h.endIndex)
                        .put("explanation", h.explanation)
                )
            }
            obj.put("highlights", hlArr)
        }
        is com.yiqiu.readingquiz.data.model.ArticleBlock.Image -> {
            JSONObject()
                .put("type", "image")
                .put("path", b.path)
                .put("caption", b.caption)
        }
        is com.yiqiu.readingquiz.data.model.ArticleBlock.Section -> {
            val childrenArr = JSONArray()
            b.children.forEach { childrenArr.put(blockToJson(it)) }
            JSONObject()
                .put("type", "section")
                .put("title", b.title)
                .put("level", b.level)
                .put("children", childrenArr)
        }
    }

    private fun articleFromJson(o: JSONObject): Article {
        val blocksArr = o.getJSONArray("blocks")
        val blocks = mutableListOf<com.yiqiu.readingquiz.data.model.ArticleBlock>()
        for (i in 0 until blocksArr.length()) {
            blocks.addAll(blockFromJson(blocksArr.getJSONObject(i)))
        }
        return Article(
            id = o.getString("id"),
            title = o.getString("title"),
            author = o.optString("author", ""),
            source = o.optString("source", ""),
            category = o.optString("category", ""),
            coverSummary = o.optString("coverSummary", ""),
            blocks = blocks,
            notes = emptyList(),
            favorite = o.optBoolean("favorite", false),
            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    /**
     * 从 JSON 单个 block 对象反序列化。
     * 注意：section 类型会递归解析 children 数组，每个 child 可能是任意类型。
     */
    private fun blockFromJson(b: JSONObject): List<com.yiqiu.readingquiz.data.model.ArticleBlock> {
        return when (b.optString("type", "paragraph")) {
            "image" -> listOf(
                com.yiqiu.readingquiz.data.model.ArticleBlock.Image(
                    path = b.optString("path", ""),
                    caption = b.optString("caption", "")
                )
            )
            "section" -> {
                val childrenArr = b.optJSONArray("children")
                val children = mutableListOf<com.yiqiu.readingquiz.data.model.ArticleBlock>()
                if (childrenArr != null) {
                    for (j in 0 until childrenArr.length()) {
                        children.addAll(blockFromJson(childrenArr.getJSONObject(j)))
                    }
                }
                listOf(
                    com.yiqiu.readingquiz.data.model.ArticleBlock.Section(
                        title = b.optString("title", ""),
                        level = b.optInt("level", 1),
                        children = children
                    )
                )
            }
            else -> {
                val hlArr = b.optJSONArray("highlights")
                val highlights = mutableListOf<com.yiqiu.readingquiz.data.model.HighlightSpan>()
                if (hlArr != null) {
                    for (j in 0 until hlArr.length()) {
                        val h = hlArr.getJSONObject(j)
                        highlights.add(
                            com.yiqiu.readingquiz.data.model.HighlightSpan(
                                text = h.optString("text", ""),
                                startIndex = h.optInt("startIndex", 0),
                                endIndex = h.optInt("endIndex", 0),
                                explanation = h.optString("explanation", "")
                            )
                        )
                    }
                }
                listOf(
                    com.yiqiu.readingquiz.data.model.ArticleBlock.Paragraph(
                        text = b.optString("text", ""),
                        highlights = highlights
                    )
                )
            }
        }
    }

    private fun sessionToJson(session: QuizSession): JSONObject {
        val qArr = JSONArray()
        session.questions.forEach { q -> qArr.put(questionToJson(q)) }
        val aArr = JSONArray()
        session.answers.forEach { a ->
            aArr.put(
                JSONObject()
                    .put("questionId", a.questionId)
                    .put("selectedKeys", JSONArray(a.selectedKeys))
                    .put("blankInputs", JSONArray(a.blankInputs))
                    .put("shortAnswer", a.shortAnswer)
                    .put("judged", a.judged)
                    .put("correct", a.correct)
                    .put("submittedAt", a.submittedAt)
            )
        }
        return JSONObject()
            .put("id", session.id)
            .put("articleId", session.articleId)
            .put("questions", qArr)
            .put("answers", aArr)
            .put("markedForReview", JSONArray(session.markedForReview))
            .put("currentIndex", session.currentIndex)
            .put("startedAt", session.startedAt)
            .put("durationMs", session.durationMs)
            .put("completed", session.completed)
    }

    private fun sessionFromJson(o: JSONObject): QuizSession {
        val qArr = o.getJSONArray("questions")
        val questions = mutableListOf<Question>()
        for (i in 0 until qArr.length()) {
            questions.add(questionFromJson(qArr.getJSONObject(i)))
        }
        val aArr = o.optJSONArray("answers") ?: JSONArray()
        val answers = mutableListOf<UserAnswer>()
        for (i in 0 until aArr.length()) {
            val a = aArr.getJSONObject(i)
            answers.add(
                UserAnswer(
                    questionId = a.getString("questionId"),
                    selectedKeys = a.optJSONArray("selectedKeys").toStringList(),
                    blankInputs = a.optJSONArray("blankInputs").toStringList(),
                    shortAnswer = a.optString("shortAnswer", ""),
                    judged = a.optBoolean("judged", false),
                    correct = a.optBoolean("correct", false),
                    submittedAt = a.optLong("submittedAt", 0L)
                )
            )
        }
        val mArr = o.optJSONArray("markedForReview") ?: JSONArray()
        val marked = mArr.toStringList()
        return QuizSession(
            id = o.getString("id"),
            articleId = o.getString("articleId"),
            questions = questions,
            answers = answers,
            markedForReview = marked,
            currentIndex = o.optInt("currentIndex", 0),
            startedAt = o.optLong("startedAt", 0L),
            durationMs = o.optLong("durationMs", 0L),
            completed = o.optBoolean("completed", false)
        )
    }

    private fun questionToJson(q: Question): JSONObject {
        val oArr = JSONArray()
        q.options.forEach { opt -> oArr.put(JSONObject().put("key", opt.key).put("text", opt.text)) }
        return JSONObject()
            .put("id", q.id)
            .put("type", q.type.name)
            .put("question", q.question)
            .put("options", oArr)
            .put("answer", JSONArray(q.answer))
            .put("blankAnswers", JSONArray(q.blankAnswers))
            .put("analysis", q.analysis)
            .put("category", q.category)
    }

    private fun questionFromJson(o: JSONObject): Question {
        val oArr = o.optJSONArray("options") ?: JSONArray()
        val options = mutableListOf<com.yiqiu.readingquiz.data.model.Option>()
        for (i in 0 until oArr.length()) {
            val op = oArr.getJSONObject(i)
            options.add(
                com.yiqiu.readingquiz.data.model.Option(
                    key = op.optString("key", ""),
                    text = op.optString("text", "")
                )
            )
        }
        return Question(
            id = o.optString("id", UUID.randomUUID().toString()),
            type = runCatching { QuestionType.valueOf(o.optString("type", "SINGLE")) }
                .getOrDefault(QuestionType.SINGLE),
            question = o.optString("question", ""),
            options = options,
            answer = o.optJSONArray("answer").toStringList(),
            blankAnswers = o.optJSONArray("blankAnswers").toStringList(),
            analysis = o.optString("analysis", ""),
            category = o.optString("category", "")
        )
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until length()) {
            list.add(optString(i, ""))
        }
        return list
    }
}