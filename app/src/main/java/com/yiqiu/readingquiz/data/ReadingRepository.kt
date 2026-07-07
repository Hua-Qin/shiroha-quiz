package com.yiqiu.readingquiz.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.yiqiu.readingquiz.data.model.Article
import com.yiqiu.readingquiz.data.model.Question
import com.yiqiu.readingquiz.data.model.QuestionType
import com.yiqiu.readingquiz.data.model.QuizSession
import com.yiqiu.readingquiz.data.model.ReadingNote
import com.yiqiu.readingquiz.data.model.SectionProgress
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
     * 全局统一题库：所有题目挂在单一 List 下（按 articleId 字段归属文章）。
     * 用 mutableStateListOf 让 Compose 自动订阅变更，触发 UI 重组。
     */
    val questions = mutableStateListOf<Question>()

    /**
     * 章节学习进度（key = "${articleId}#${sectionId}"）。
     * Compose 通过 collectAsState 订阅实现实时更新。
     */
    val sectionProgress = mutableStateMapOf<String, SectionProgress>()

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

    // ----------------- 章节学习进度 -----------------

    /**
     * 拼装 sectionProgress 的 key（articleId#sectionId）。
     * 暴露为公开函数供 UI 层复用，避免硬编码格式。
     */
    fun sectionProgressKey(articleId: String, sectionId: String): String =
        "${articleId}#${sectionId}"

    /**
     * 获取指定文章的所有章节进度。
     */
    fun sectionProgressFor(articleId: String): List<SectionProgress> {
        return sectionProgress.values.filter { it.articleId == articleId }
    }

    /**
     * 获取单个章节的进度（不存在时返回默认值）。
     */
    fun progressOf(articleId: String, sectionId: String): SectionProgress {
        val key = sectionProgressKey(articleId, sectionId)
        return sectionProgress[key] ?: SectionProgress(
            articleId = articleId,
            sectionId = sectionId
        )
    }

    /**
     * 标记某章节已完成（题目全部答完时调用）。
     */
    fun markSectionCompleted(articleId: String, sectionId: String) {
        val key = sectionProgressKey(articleId, sectionId)
        val cur = sectionProgress[key]
        val updated = SectionProgress(
            articleId = articleId,
            sectionId = sectionId,
            completed = true,
            wrongCount = cur?.wrongCount ?: 0,
            unansweredCount = 0,
            lastUpdated = System.currentTimeMillis()
        )
        sectionProgress[key] = updated
        Log.i(TAG, "markSectionCompleted: $key")
        saveSectionProgress()
    }

    /**
     * 累计章节答错题数。
     */
    fun incrementSectionWrong(articleId: String, sectionId: String) {
        val key = sectionProgressKey(articleId, sectionId)
        val cur = sectionProgress[key]
        val updated = (cur ?: SectionProgress(articleId, sectionId)).copy(
            wrongCount = (cur?.wrongCount ?: 0) + 1,
            lastUpdated = System.currentTimeMillis()
        )
        sectionProgress[key] = updated
        Log.d(TAG, "incrementSectionWrong: $key → ${updated.wrongCount}")
        saveSectionProgress()
    }

    /**
     * 累计章节未答题数。
     */
    fun incrementSectionUnanswered(articleId: String, sectionId: String) {
        val key = sectionProgressKey(articleId, sectionId)
        val cur = sectionProgress[key]
        val updated = (cur ?: SectionProgress(articleId, sectionId)).copy(
            unansweredCount = (cur?.unansweredCount ?: 0) + 1,
            lastUpdated = System.currentTimeMillis()
        )
        sectionProgress[key] = updated
        Log.d(TAG, "incrementSectionUnanswered: $key → ${updated.unansweredCount}")
        saveSectionProgress()
    }

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

    /**
     * 按章节获取答题会话（仅包含该 sectionId 下的题目）。
     * - 若已存在该 articleId 的会话，按 sectionId 过滤 questions 后返回临时视图
     * - 若不存在会话，返回 null（由 UI 引导用户先进入阅读页生成题目）
     */
    fun sessionForArticleAndSection(articleId: String, sectionId: String): QuizSession? {
        val base = sessionFor(articleId) ?: return null
        val filtered = base.questions.filter { it.sectionId == sectionId }
        if (filtered.isEmpty()) return null
        return base.copy(
            questions = filtered,
            answers = base.answers.filter { ans -> filtered.any { it.id == ans.questionId } },
            markedForReview = base.markedForReview.filter { id -> filtered.any { it.id == id } }
        )
    }

    // ----------------- AI 配置 -----------------

    fun updateAiConfig(config: AiConfig) {
        Log.i(TAG, "updateAiConfig: baseUrl=${config.apiBaseUrl}, model=${config.modelName}")
        aiConfig.value = config
        saveAiConfig()
    }

    // ----------------- 题目缓存（统一题库） -----------------
    //
    // 设计：所有题目挂在单一 `questions` 列表下，按 Question.articleId 字段归属文章。
    // 任何"按文章"的操作都是这条列表的过滤 / 子集写入，不存在第二份题库副本。

    /** 用传入列表整体替换单一题库（用于还原/导入）。 */
    fun setQuestions(newQuestions: List<Question>) {
        questions.clear()
        questions.addAll(newQuestions)
        Log.d(TAG, "setQuestions: replaced with ${questions.size} questions")
        saveQuestions()
    }

    /** 整库只读快照。 */
    fun allQuestions(): List<Question> = questions.toList()

    /**
     * 取出指定文章的所有题目（按入题时间/位置保持顺序）。
     */
    fun getQuestions(articleId: String): List<Question> =
        questions.filter { it.articleId == articleId }

    /**
     * 向统一题库追加新题目（保留所有已有题目）。
     * 调用方需保证传入的 Question.articleId 已正确赋值，否则归到 articleId 入参所属文章。
     */
    fun addQuestions(articleId: String, newQuestions: List<Question>) {
        if (newQuestions.isEmpty()) return
        // 兜底：若调用方忘了填 articleId，按入参补齐
        val normalized = newQuestions.map { if (it.articleId.isBlank()) it.copy(articleId = articleId) else it }
        questions.addAll(normalized)
        Log.d(TAG, "addQuestions: articleId=$articleId, added=${normalized.size}, total=${questions.size}")
        saveQuestions()
    }

    /**
     * 在统一题库中按 id 删除一道题。
     */
    fun deleteQuestion(articleId: String, questionId: String) {
        val idx = questions.indexOfFirst { it.id == questionId && it.articleId == articleId }
        if (idx < 0) {
            Log.w(TAG, "deleteQuestion: id=$questionId not found in articleId=$articleId")
            return
        }
        questions.removeAt(idx)
        Log.d(TAG, "deleteQuestion: articleId=$articleId, id=$questionId, remaining=${questions.size}")
        saveQuestions()
    }

    /**
     * 更新单道题目（用于编辑器保存）。
     * articleId 入参用于安全性校验：id 必须属于指定文章。
     */
    fun updateQuestion(articleId: String, updated: Question) {
        val safe = if (updated.articleId.isBlank()) updated.copy(articleId = articleId) else updated
        val idx = questions.indexOfFirst { it.id == safe.id && it.articleId == articleId }
        if (idx < 0) {
            Log.w(TAG, "updateQuestion: id=${safe.id} not found in articleId=$articleId")
            return
        }
        questions[idx] = safe
        Log.d(TAG, "updateQuestion: articleId=$articleId, id=${safe.id}")
        saveQuestions()
    }

    // ----------------- 持久化 -----------------

    private fun loadAll() {
        loadArticles()
        loadNotes()
        loadSessions()
        loadAiConfig()
        loadQuestions()
        loadSectionProgress()
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
     * 题目缓存持久化：单一 JSON array，每条记录自带 articleId。
     * - 新格式直接读取：[...]
     * - 旧格式（questions_map_json：{ articleId: [...] }）自动迁移一次
     */
    private fun loadQuestions() {
        // 优先尝试新格式 questions_json
        val rawNew = store.getString("questions_json", null)
        if (rawNew != null) {
            runCatching {
                val arr = JSONArray(rawNew)
                questions.clear()
                for (i in 0 until arr.length()) {
                    questions.add(questionFromJson(arr.getJSONObject(i)))
                }
                Log.d(TAG, "loadQuestions: loaded ${questions.size} from questions_json")
            }.onFailure {
                Log.w(TAG, "loadQuestions new-format FAILED: ${it.message}", it)
            }
            return
        }
        // 兼容老格式 questions_map_json
        val rawOld = store.getString("questions_map_json", null) ?: return
        runCatching {
            val map = JSONObject(rawOld)
            val keys = map.keys()
            questions.clear()
            while (keys.hasNext()) {
                val articleId = keys.next()
                val arr = map.optJSONArray(articleId) ?: continue
                for (i in 0 until arr.length()) {
                    val q = questionFromJson(arr.getJSONObject(i))
                    // 老数据没有 articleId 字段 → 补齐到 map key
                    questions.add(if (q.articleId.isBlank()) q.copy(articleId = articleId) else q)
                }
            }
            Log.i(TAG, "loadQuestions migrated from questions_map_json: ${questions.size} questions")
            // 迁移完成：写入新格式并清掉旧 key
            saveQuestions()
            store.edit().remove("questions_map_json").apply()
        }.onFailure {
            Log.w(TAG, "loadQuestions legacy FAILED: ${it.message}", it)
        }
    }

    private fun saveQuestions() {
        if (!::store.isInitialized) return
        val arr = JSONArray()
        questions.forEach { q -> arr.put(questionToJson(q)) }
        store.edit().putString("questions_json", arr.toString()).apply()
    }

    /**
     * 章节进度持久化：JSON map（key=articleId#sectionId）。
     */
    private fun loadSectionProgress() {
        val raw = store.getString("section_progress_json", null) ?: return
        runCatching {
            val obj = JSONObject(raw)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val o = obj.optJSONObject(key) ?: continue
                sectionProgress[key] = SectionProgress(
                    articleId = o.optString("articleId", ""),
                    sectionId = o.optString("sectionId", ""),
                    completed = o.optBoolean("completed", false),
                    wrongCount = o.optInt("wrongCount", 0),
                    unansweredCount = o.optInt("unansweredCount", 0),
                    lastUpdated = o.optLong("lastUpdated", 0L)
                )
            }
            Log.d(TAG, "loadSectionProgress: ${sectionProgress.size} entries")
        }.onFailure {
            Log.w(TAG, "loadSectionProgress FAILED: ${it.message}", it)
        }
    }

    private fun saveSectionProgress() {
        if (!::store.isInitialized) return
        val o = JSONObject()
        val asMap: Map<String, SectionProgress> = sectionProgress
        asMap.forEach { (key, p) ->
            o.put(key, JSONObject()
                .put("articleId", p.articleId)
                .put("sectionId", p.sectionId)
                .put("completed", p.completed)
                .put("wrongCount", p.wrongCount)
                .put("unansweredCount", p.unansweredCount)
                .put("lastUpdated", p.lastUpdated))
        }
        store.edit().putString("section_progress_json", o.toString()).apply()
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
                .put("id", b.id)  // 持久化章节稳定 ID
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
                        children = children,
                        id = b.optString("id", "")  // 兼容旧 JSON 无 id 字段
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
            .put("articleId", q.articleId)
            .put("sectionId", q.sectionId ?: JSONObject.NULL)
            .put("anchorText", q.anchorText)
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
            category = o.optString("category", ""),
            articleId = o.optString("articleId", ""),
            // 章节绑定：optString("sectionId") 遇 JSONObject.NULL 时返回 "null" 字符串；用 isNull 严格判断
            sectionId = if (o.isNull("sectionId")) null else o.optString("sectionId", "").ifBlank { null },
            anchorText = o.optString("anchorText", "")
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