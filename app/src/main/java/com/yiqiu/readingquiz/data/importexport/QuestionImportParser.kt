package com.yiqiu.readingquiz.data.importexport

import android.util.Log
import com.yiqiu.readingquiz.data.model.Option
import com.yiqiu.readingquiz.data.model.Question
import com.yiqiu.readingquiz.data.model.QuestionType
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * 题目库导入解析器（独立于 ArticleImporter）。
 *
 * 支持三种格式：
 * 1. **JSON** (`questions.json`)：`{"questions":[Question, ...]}` 或顶层数组 `[Question, ...]`
 * 2. **Markdown** (`questions.md`)：
 *    ```
 *    # 题库标题
 *    ## 1. 单选题
 *    **题干**：xxxx
 *    - A. 选项1
 *    - B. 选项2
 *    **答案**：B
 **解析**：...
 *    ## 2. 判断题
 *    ...
 *    ```
 * 3. **TXT**：每行一道题，固定格式（不推荐，但提供兜底）
 *
 * 返回 `Result<List<Question>>`：Success(questions) / Failure(reason)
 */
object QuestionImportParser {

    private const val TAG = "QuestionImport"

    sealed class Result {
        data class Success(val questions: List<Question>) : Result()
        data class Failure(val reason: String) : Result()
    }

    fun importFromText(raw: String, fileName: String): Result {
        if (raw.isBlank()) return Result.Failure("文件内容为空：$fileName")
        val ext = fileName.substringAfterLast('.', "").lowercase()
        Log.d(TAG, "importFromText: file=$fileName, ext=$ext, bytes=${raw.length}")
        return when (ext) {
            "json" -> importJson(raw)
            "md", "markdown" -> importMarkdown(raw)
            else -> importTextFallback(raw)
        }
    }

    // ---------- JSON ----------

    private fun importJson(raw: String): Result {
        return try {
            val trimmed = raw.trim()
            val questions: List<Question> = when {
                trimmed.startsWith("[") -> {
                    val arr = JSONArray(trimmed)
                    (0 until arr.length()).mapNotNull { i -> questionFromJson(arr.getJSONObject(i)) }
                }
                trimmed.startsWith("{") -> {
                    val obj = JSONObject(trimmed)
                    val arr = obj.optJSONArray("questions")
                        ?: return Result.Failure("JSON 缺少 questions 数组")
                    (0 until arr.length()).mapNotNull { i -> questionFromJson(arr.getJSONObject(i)) }
                }
                else -> return Result.Failure("JSON 格式不正确（不以 { 或 [ 开头）")
            }
            if (questions.isEmpty()) Result.Failure("JSON 中无有效题目") else Result.Success(questions)
        } catch (e: Exception) {
            Log.w(TAG, "importJson FAILED: ${e.message}", e)
            Result.Failure("JSON 解析失败：${e.message ?: "未知错误"}")
        }
    }

    private fun questionFromJson(o: JSONObject): Question? = try {
        val type = runCatching { QuestionType.valueOf(o.optString("type", "SINGLE").uppercase()) }
            .getOrDefault(QuestionType.SINGLE)
        val optsArr = o.optJSONArray("options")
        val options = mutableListOf<Option>()
        if (optsArr != null) {
            for (i in 0 until optsArr.length()) {
                val op = optsArr.getJSONObject(i)
                options.add(Option(key = op.optString("key", "A"), text = op.optString("text", "")))
            }
        }
        // JUDGE 题若 options 为空，注入默认
        if (type == QuestionType.JUDGE && options.isEmpty()) {
            options.add(Option("A", "正确"))
            options.add(Option("B", "错误"))
        }
        Question(
            id = o.optString("id", UUID.randomUUID().toString()),
            type = type,
            question = o.optString("question", ""),
            options = options,
            answer = o.optJSONArray("answer").toStringList(),
            blankAnswers = o.optJSONArray("blankAnswers").toStringList(),
            analysis = o.optString("analysis", ""),
            category = o.optString("category", ""),
            sectionId = if (o.isNull("sectionId")) null else o.optString("sectionId", "").ifBlank { null },
            anchorText = o.optString("anchorText", "")
        )
    } catch (e: Exception) {
        Log.w(TAG, "questionFromJson skipped: ${e.message}")
        null
    }

    private fun org.json.JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until length()) list.add(optString(i, ""))
        return list.filter { it.isNotBlank() }
    }

    // ---------- Markdown ----------

    private fun importMarkdown(raw: String): Result {
        return try {
            val questions = mutableListOf<Question>()
            val sections = raw.split(Regex("(?m)^## ")).filter { it.isNotBlank() }
            for (section in sections) {
                val lines = section.trim().lines()
                val titleLine = lines.firstOrNull() ?: continue
                // 标题格式：数字. 题型 例如 "1. 单选题"
                val type = detectTypeFromTitle(titleLine)
                val body = lines.drop(1).joinToString("\n")
                val q = parseMarkdownBody(body, type) ?: continue
                questions.add(q)
            }
            if (questions.isEmpty()) {
                Result.Failure("Markdown 中未发现有效题目（请用 ## 1. 单选题 格式）")
            } else Result.Success(questions)
        } catch (e: Exception) {
            Log.w(TAG, "importMarkdown FAILED: ${e.message}", e)
            Result.Failure("Markdown 解析失败：${e.message ?: "未知错误"}")
        }
    }

    private fun detectTypeFromTitle(title: String): QuestionType {
        return when {
            title.contains("单选") -> QuestionType.SINGLE
            title.contains("多选") -> QuestionType.MULTIPLE
            title.contains("判断") -> QuestionType.JUDGE
            title.contains("填空") -> QuestionType.BLANK
            title.contains("简答") -> QuestionType.SHORT
            else -> QuestionType.SINGLE
        }
    }

    private fun parseMarkdownBody(body: String, defaultType: QuestionType): Question? {
        val text = body.trim()
        if (text.isBlank()) return null
        // 提取题干（**题干**：...）
        val questionText = Regex("\\*\\*题干\\*\\*[：:]\\s*(.+?)(?=\\n\\n|\\n\\*\\*|$)", RegexOption.DOT_MATCHES_ALL)
            .find(text)?.groupValues?.get(1)?.trim() ?: return null
        // 提取选项（A. xxx / - A. xxx）
        val options = mutableListOf<Option>()
        val optionRegex = Regex("^[\\s\\-]*([A-Z])\\s*[\\.、]\\s*(.+)$", RegexOption.MULTILINE)
        optionRegex.findAll(text).forEach { match ->
            val key = match.groupValues[1]
            val value = match.groupValues[2].trim()
            // 截断到下一个 **xxx** 标签
            val valueClean = Regex("\\s+\\*\\*").let { rx ->
                val idx = value.indexOf("\n\n")
                if (idx >= 0) value.substring(0, idx) else value
            }.trim()
            options.add(Option(key = key, text = valueClean))
        }
        // 提取答案
        val answer = Regex("\\*\\*答案\\*\\*[：:]\\s*(.+?)(?=\\n|$)", RegexOption.MULTILINE)
            .find(text)?.groupValues?.get(1)?.trim() ?: ""
        val answerList = answer.split(Regex("[,，\\s]+")).filter { it.isNotBlank() }
        // 提取解析
        val analysis = Regex("\\*\\*解析\\*\\*[：:]\\s*(.+?)(?=\\n\\n\\*\\*|$)", RegexOption.DOT_MATCHES_ALL)
            .find(text)?.groupValues?.get(1)?.trim() ?: ""
        // 提取填空答案
        val blankAnswers = Regex("\\*\\*填空答案\\*\\*[：:]\\s*(.+?)(?=\\n|$)", RegexOption.MULTILINE)
            .find(text)?.groupValues?.get(1)?.trim()?.split(Regex("[,，;；]\\s*"))
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        val type = defaultType
        val optionsFinal = if (type == QuestionType.JUDGE && options.isEmpty()) {
            listOf(Option("A", "正确"), Option("B", "错误"))
        } else options
        return Question(
            id = UUID.randomUUID().toString(),
            type = type,
            question = questionText,
            options = optionsFinal,
            answer = answerList,
            blankAnswers = if (type == QuestionType.BLANK) blankAnswers else emptyList(),
            analysis = analysis,
            category = ""
        )
    }

    // ---------- TXT 兜底 ----------

    private fun importTextFallback(raw: String): Result {
        // 兜底：尝试按行解析。第一行为题干，第二行起为选项（"A. xxx"），最后是 "答案:X"
        val lines = raw.trim().lines().filter { it.isNotBlank() }
        if (lines.size < 3) return Result.Failure("TXT 格式无法识别：需至少 3 行")
        return try {
            val q = lines.first()
            val options = mutableListOf<Option>()
            var answerLine: String? = null
            for (i in 1 until lines.size) {
                val line = lines[i]
                val opt = Regex("^([A-Z])\\s*[\\.、]\\s*(.+)$").find(line)
                if (opt != null) {
                    options.add(Option(opt.groupValues[1], opt.groupValues[2].trim()))
                } else if (line.startsWith("答案") || line.startsWith("Answer")) {
                    answerLine = line.substringAfter("：").substringAfter(":").trim()
                }
            }
            val answers = answerLine?.split(Regex("[,，\\s]+"))?.filter { it.isNotBlank() } ?: emptyList()
            val questions = listOf(
                Question(
                    id = UUID.randomUUID().toString(),
                    type = if (options.size == 2 && options.all { it.text == "正确" || it.text == "错误" })
                        QuestionType.JUDGE else QuestionType.SINGLE,
                    question = q,
                    options = options,
                    answer = answers,
                    blankAnswers = emptyList(),
                    analysis = ""
                )
            )
            Result.Success(questions)
        } catch (e: Exception) {
            Result.Failure("TXT 解析失败：${e.message ?: "未知错误"}")
        }
    }
}