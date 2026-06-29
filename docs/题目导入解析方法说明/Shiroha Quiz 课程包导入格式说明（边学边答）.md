# Shiroha Quiz 课程包导入格式说明（边学边答）

## 目的

本文档面向内容作者与开发者，说明 Shiroha Quiz 「边学边答」功能所使用的「课程包」JSON 格式规范。课程包把「教程章节」与「配套题目」整合到同一份 JSON 文件中，通过章节 `id` 与题目 `knowledgePoints` 字段双向关联，实现学完知识点即可立即答题巩固。

本文档同时面向开发者，详细描述课程包导入功能在原生安卓端（Kotlin + Compose）中的实现要点，便于后续维护与扩展。

---

## 目录

1. [课程包格式概述](#一课程包格式概述)
2. [完整字段说明](#二完整字段说明)
3. [示例：最小可用课程包](#三示例最小可用课程包)
4. [示例：包含完整字段的课程包](#四示例包含完整字段的课程包)
5. [章节 ↔ 题目关联规则](#五章节--题目关联规则)
6. [题型支持](#六题型支持)
7. [导入流程](#七导入流程)
8. [错误处理与提示](#八错误处理与提示)
9. [开发者实现要点](#九开发者实现要点)
10. [附录：FAQ](#十附录faq)

---

## 一、课程包格式概述

一个「课程包」是一个 JSON 文件，**同时包含教程与题目**，结构如下：

```json
{
  "course":    { /* 教程：知识点章节列表 */ },
  "bankName":  "配套题库名（可选）",
  "questions": [ /* 题目数组 */ ]
}
```

| 顶层字段 | 必填 | 说明 |
|---------|------|------|
| `course` | 是 | 教程内容节点，包含若干知识点章节 |
| `bankName` | 否 | 配套题库名；缺省使用「<课程名> 题目」 |
| `questions` | 是 | 配套题目数组，使用现有标准题目格式 |

**核心思想**：教程章节与题目通过 `id` ↔ `knowledgePoints` 关联，不需要外部索引文件。

---

## 二、完整字段说明

### 2.1 顶层字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `course` | object | ✓ | 教程内容节点 |
| `bankName` | string | – | 题库名（缺省时使用「<courseName> 题目」） |
| `questions` | array | ✓ | 题目数组 |

### 2.2 course 节点

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `courseId` | string | ✓ | 课程唯一标识，用于判断重复导入 |
| `courseName` | string | ✓ | 课程名称（显示在课程列表） |
| `description` | string | – | 课程简介 |
| `sections` | array | ✓ | 知识点章节数组，按数组顺序作为默认学习顺序 |

### 2.3 sections 节点（章节）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | ✓ | 章节唯一标识，与题目 `knowledgePoints` 关联。建议 ASCII（如 `ch1_3`） |
| `chapterTitle` | string | – | 大章节标题（如「第 1 章 走进新语言」） |
| `sectionTitle` | string | – | 小节标题（如「1.3 程序代码基本结构」） |
| `order` | int | – | 学习顺序号，缺省按数组下标递增 |
| `summary` | string | – | 一句话摘要（折叠态显示） |
| `content` | string | – | 详细讲解（纯文本，`\n` 分段） |
| `codeExample` | string | – | 示例代码（独立代码块显示） |
| `codeExplanation` | string | – | 代码要点说明 |
| `difficulty` | string | – | 难度：`easy` / `medium` / `hard` |
| `prerequisites` | array | – | 前置章节 `id` 列表（仅做提示，不强制） |
| `questionTag` | string | – | 题目筛选标签，缺省等于 `id` |

### 2.4 questions 节点（题目）

完全复用 Shiroha Quiz 现有标准题目格式（参见《Shiroha Quiz 题目导入解析方法说明》），核心字段：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | ✓ | 题型：`single` / `multiple` / `judge` / `blank` / `short` |
| `question` | string | ✓ | 题干 |
| `options` | array | 单选/多选必填 | 选项数组 |
| `answer` | array | ✓ | 答案数组（多选用数组，单选用单元素数组） |
| `analysis` | string | – | 解析 |
| `knowledgePoints` | array | ✓ | 关联章节 `id` 列表（如 `["ch1_3"]`） |
| `difficulty` | string | – | 题目难度 |
| `category` | string | – | 分类显示 |
| `blankAnswers` | array | 填空题 | 多个空的参考答案（与 `answer` 二选一） |

完整支持的题目字段集请参考现有 `parseQuestion()` 解析器（`QuizRepository.kt:4489`）。

---

## 三、示例：最小可用课程包

最简单的课程包只包含必要字段：

```json
{
  "course": {
    "courseId": "demo",
    "courseName": "示例课程",
    "sections": [
      {
        "id": "s1",
        "sectionTitle": "第一节",
        "content": "这是讲解内容。"
      }
    ]
  },
  "questions": [
    {
      "type": "single",
      "question": "示例题目？",
      "options": [
        {"key": "A", "text": "选项 A"},
        {"key": "B", "text": "选项 B"}
      ],
      "answer": ["A"],
      "knowledgePoints": ["s1"]
    }
  ]
}
```

此例可正常导入并支持学习 → 答题 → 记录进度。

---

## 四、示例：包含完整字段的课程包

```json
{
  "course": {
    "courseId": "kotlin-basic",
    "courseName": "Kotlin 程序设计基础（一）基础语法",
    "description": "Kotlin 入门基础语法，边学边练",
    "sections": [
      {
        "id": "ch1_3",
        "chapterTitle": "第 1 章 走进新语言",
        "sectionTitle": "1.3 程序代码基本结构",
        "order": 2,
        "summary": "Kotlin 程序由包声明、入口函数、语句组成。",
        "content": "Kotlin 程序的基本结构包括：\n1. 包声明（可省略）\n2. 入口函数 fun main()\n3. 函数体中的语句",
        "codeExample": "fun main() {\n    println(\"Hello, World!\")\n}",
        "codeExplanation": "fun 关键字声明函数，main 是入口函数名，println 用于输出。",
        "difficulty": "easy",
        "prerequisites": ["ch0"],
        "questionTag": "ch1_3"
      }
    ]
  },
  "bankName": "Kotlin 基础语法题目",
  "questions": [
    {
      "type": "single",
      "question": "Kotlin 程序的入口函数是？",
      "options": [
        {"key": "A", "text": "fun main()"},
        {"key": "B", "text": "public static void main"},
        {"key": "C", "text": "def main"},
        {"key": "D", "text": "void main"}
      ],
      "answer": ["A"],
      "analysis": "Kotlin 程序的入口函数标准写法是 fun main()",
      "knowledgePoints": ["ch1_3"],
      "category": "1.3 程序代码基本结构",
      "difficulty": "easy"
    },
    {
      "type": "judge",
      "question": "Kotlin 文件可以省略包声明。",
      "answer": ["对"],
      "analysis": "Kotlin 源文件可不写 package，默认在无名包下。",
      "knowledgePoints": ["ch1_3"],
      "difficulty": "easy"
    },
    {
      "type": "blank",
      "question": "Kotlin 中声明函数的关键字是 ____。",
      "answer": ["fun"],
      "analysis": "fun 是 function 的缩写。",
      "knowledgePoints": ["ch1_3"]
    },
    {
      "type": "multiple",
      "question": "以下哪些是 Kotlin 程序的组成部分？（多选）",
      "options": [
        {"key": "A", "text": "包声明"},
        {"key": "B", "text": "入口函数"},
        {"key": "C", "text": "宏定义"},
        {"key": "D", "text": "函数体语句"}
      ],
      "answer": ["A", "B", "D"],
      "analysis": "Kotlin 没有宏定义（macro）概念。",
      "knowledgePoints": ["ch1_3"],
      "difficulty": "medium"
    }
  ]
}
```

---

## 五、章节 ↔ 题目关联规则

### 5.1 匹配优先级

题目 `knowledgePoints` 数组中的任一元素与章节的 `id` 或 `questionTag` 字符串相等，即视为关联成功。

```json
// 章节定义
{
  "id": "ch1_3",
  "questionTag": "ch1_3",   // 可省略，默认等于 id
  "sectionTitle": "1.3 程序代码基本结构"
}

// 题目定义
{
  "knowledgePoints": ["ch1_3"]   // 关联到 ch1_3 章节
}
```

### 5.2 多章节共享题目

同一个题目可以关联多个章节（出现在多个章节的练习中）：

```json
{
  "knowledgePoints": ["ch1_3", "ch2_2"]
}
```

### 5.3 题目未关联章节

题目若没有 `knowledgePoints` 或值无法匹配任何章节，仍会作为普通题目导入到配套题库，可在普通练习模式下访问，但**不会**出现在任何章节的练习中。

### 5.4 章节无关联题目

章节若无任何题目匹配，学习页会显示「本章暂无题目，可标记为已学习后返回」，并提供直接标记按钮。

---

## 六、题型支持

支持 Shiroha Quiz 所有现有题型：

| `type` 值 | 说明 | 选项要求 |
|----------|------|----------|
| `single` | 单选 | 2-4 个选项，1 个正确答案 |
| `multiple` | 多选 | 2-4 个选项，至少 1 个正确答案 |
| `judge` | 判断 | 无选项（系统自动注入「对/错」） |
| `blank` | 填空 | 无选项，参考答案填入 `answer` 或 `blankAnswers` |
| `short` | 简答 | 不参与自动判分，仅展示参考答案 |

详细题型支持参见《Shiroha Quiz 题目导入解析方法说明》。

---

## 七、导入流程

### 7.1 用户操作流程

1. 打开 APP → 首页 → 点击「边学边答」入口
2. 学习主页 → 点击「导入课程包」按钮
3. 系统文件选择器打开 → 选择 `.json` 文件
4. 系统自动解析 → 显示成功 / 失败提示
5. 课程出现在学习主页列表中

### 7.2 数据流

```
用户选择文件 (.json)
    ↓ ActivityResultContracts.OpenDocument()
Uri
    ↓ ContentResolver.openInputStream()
字节流 → UTF-8 字符串
    ↓ QuizRepository.importCoursePackage(context, rawJson)
JSONObject 解析
    ├─ 校验 course 节点
    ├─ 解析 sections → KnowledgeSection 列表
    ├─ 解析 questions → 通过 parseQuestionsArray 复用现有题目解析
    ├─ 调用 importBank() 创建关联 QuizBank，记录 bankId
    ├─ 创建 KnowledgeCourse（linkedBankId = bankId）
    └─ knowledgeCourses.add(course) + persist()
    ↓
学习主页 UI 自动刷新（Compose 响应式）
```

### 7.3 重复导入

若导入与已存在课程相同的 `courseId`，系统会：

1. 清理旧课程对应的所有章节进度
2. 删除旧关联题库
3. 从课程列表移除旧课程
4. 创建新题库、添加新课程

**该流程无需用户手动确认**（连续重新导入场景下用户通常期望覆盖）。

---

## 八、错误处理与提示

| 场景 | 错误提示 |
|------|----------|
| 文件内容为空 | 「课程包内容为空」 |
| 缺少 `course` 节点 | 「未检测到课程包格式：缺少 course 节点」 |
| `course` 缺少 `courseId` | 「课程包缺少 courseId 字段」 |
| `sections` 为空 | 「课程包缺少 sections 章节数据」 |
| JSON 解析异常 | 「导入失败：<异常类型>」 |
| IO 异常 | 「无法读取文件」 |

所有错误通过学习主页 `NoticeCard` 显示（红色警告样式），不会导致 APP 崩溃。

---

## 九、开发者实现要点

### 9.1 涉及文件

#### 新增文件

| 文件 | 路径 |
|------|------|
| `KnowledgePoint.kt` | `apps/android/app/src/native/java/com/yiqiu/shirohaquiz/state/` |
| `CodeBlock.kt` | `apps/android/app/src/native/java/com/yiqiu/shirohaquiz/ui/components/` |
| `KnowledgeCard.kt` | `apps/android/app/src/native/java/com/yiqiu/shirohaquiz/ui/components/` |
| `StudyScreen.kt` | `apps/android/app/src/native/java/com/yiqiu/shirohaquiz/ui/screens/` |
| `StudyCourseScreen.kt` | `apps/android/app/src/native/java/com/yiqiu/shirohaquiz/ui/screens/` |
| `StudySessionScreen.kt` | `apps/android/app/src/native/java/com/yiqiu/shirohaquiz/ui/screens/` |

#### 修改文件

| 文件 | 修改内容 |
|------|----------|
| `QuizRepository.kt` | 5 处扩展：KEY 常量、状态变量、persist、init、序列化+业务方法+公开判分 |
| `ShirohaAppShell.kt` | 3 个 import、3 个 MainTab、AppRouteSnapshot 扩展、AnimatedContent 分支 |
| `HomeScreen.kt` | `onGoStudy` 回调、`StudyEntryCard` Composable |

### 9.2 数据模型

`KnowledgePoint.kt` 定义 4 个核心类：

```kotlin
data class KnowledgeSection(
    val id: String,
    val chapterTitle: String,
    val sectionTitle: String,
    val order: Int = 0,
    val summary: String = "",
    val content: String = "",
    val codeExample: String = "",
    val codeExplanation: String = "",
    val difficulty: String = "easy",
    val prerequisites: List<String> = emptyList(),
    val questionTag: String = ""
)

data class KnowledgeCourse(
    val courseId: String,
    val courseName: String,
    val description: String = "",
    val sections: List<KnowledgeSection> = emptyList(),
    val linkedBankId: String? = null,
    val importedAt: Long = 0L
)

data class SectionProgress(
    val studied: Boolean = false,
    val practiced: Boolean = false,
    val correctCount: Int = 0,
    val totalCount: Int = 0,
    val lastStudiedAt: Long = 0L,
    val bestAccuracy: Float = 0f,
    val practiceCount: Int = 0
)

enum class MasteryLevel(val label: String) {
    NOT_STARTED("未学习"),
    STUDIED("已学习"),
    NEED_RELEARN("需重学"),
    NEED_REVIEW("需巩固"),
    MASTERED("已掌握")
}
```

### 9.3 关键业务方法

#### `QuizRepository.importCoursePackage(context, rawJson): String`

入口方法。流程：

```kotlin
fun importCoursePackage(context: Context, rawJson: String): String {
    return runCatching {
        val root = JSONObject(rawJson)
        val courseObj = root.optJSONObject("course")
            ?: return@runCatching "未检测到课程包格式：缺少 course 节点"
        // ... 解析 sections、questions ...
        // 重复导入清理
        val existing = courseById(courseId)
        if (existing != null) {
            // 清理旧进度、删除旧题库
        }
        // 创建题库
        importBank(context, bankName, questions, DEFAULT_BANK_GROUP_NAME)
        val linkedBankId = activeBankId
        // 添加课程
        knowledgeCourses.add(KnowledgeCourse(...))
        persist()
        "成功导入课程「$courseName」：${sections.size} 个章节、${questions.size} 道题目"
    }.getOrElse { "导入失败：${it.message}" }
}
```

#### `QuizRepository.questionsForSection(courseId, sectionId)`

核心筛选方法：

```kotlin
fun questionsForSection(courseId: String, sectionId: String): List<Question> {
    val course = courseById(courseId) ?: return emptyList()
    val section = course.sections.firstOrNull { it.id == sectionId } ?: return emptyList()
    val bank = course.linkedBankId?.let { id -> banks.firstOrNull { it.id == id } } ?: return emptyList()
    val tags = setOf(section.id, section.questionTag).filter { it.isNotBlank() }
    return bank.questions.filter { q -> q.knowledgePoints.any { it in tags } }
}
```

#### `QuizRepository.judgeAnswer(question, userAnswer)`

公开判分方法，从原私有 `evaluateQuestion` 中抽取：

```kotlin
fun judgeAnswer(question: Question, userAnswer: List<String>): Boolean {
    return judgeQuestionCore(question, userAnswer.map { it.trim() }.filter { it.isNotBlank() })
}

private fun judgeQuestionCore(question: Question, normalizedUserAnswer: List<String>): Boolean {
    return when (question.type) {
        QuestionType.SINGLE,
        QuestionType.MULTIPLE,
        QuestionType.JUDGE ->
            normalizedUserAnswer.sorted() == question.answer.sorted() &&
                normalizedUserAnswer.isNotEmpty()
        QuestionType.BLANK -> /* 复用 MultiBlankSupport */
        QuestionType.SHORT -> false
    }
}
```

### 9.4 持久化

扩展两个 SharedPreferences 键：

| 键名 | 值类型 | 内容 |
|------|--------|------|
| `knowledge_courses` | JSON 字符串 | 课程列表（嵌套对象数组） |
| `study_progress` | JSON 对象 | 学习进度 Map，key 为 `"${courseId}::${sectionId}"` |

序列化方法分别仿照现有 `studyRecordsToJson`（嵌套对象列表）与 `sequentialProgressToJson`（简单 Map）。

### 9.5 路由扩展

`MainTab` 枚举新增 3 个值（均 `showInBottomBar = false`）：

```kotlin
Study("边学边答", Icons.Rounded.MenuBook, showInBottomBar = false),
StudyCourse("课程章节", Icons.Rounded.MenuBook, showInBottomBar = false),
StudySession("学习会话", Icons.Rounded.MenuBook, showInBottomBar = false)
```

`AppRouteSnapshot` 从 3 字段扩展到 5 字段（增加 `courseId`/`sectionId`），encode/decode 用 `split(SEPARATOR, limit = 5)`，**对老格式数据降级兼容**。

### 9.6 UI 组件关键点

- **`CodeBlock`**：用 `buildAnnotatedString { withStyle(...) { append(...) } }` 实现 Kotlin 语法高亮，**避免 `pushStyle`/`pop` 不匹配导致的运行时报错**。
- **`KnowledgeCard`**：`lineHeight = 22.sp`（不要用 `TextUnit(value, type)` 非公开 API）。
- **`StudySessionScreen`**：`LaunchedEffect(finished)` + `if (finished)` 守门，**保证「重新练习」场景下能重新记录进度**。
- **`StudyScreen` 空状态**：使用 `EmptyStateIllustration(title, message)` 默认参数，**不要传 `imageRes` 引用不存在的 drawable**。

### 9.7 文件选择器

课程包导入直接复用 `ActivityResultContracts.OpenDocument()`：

```kotlin
val filePicker = rememberLauncherForActivityResult(
    ActivityResultContracts.OpenDocument()
) { uri ->
    if (uri == null) return@rememberLauncherForActivityResult
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    val text = String(bytes ?: return@rememberLauncherForActivityResult, Charsets.UTF_8)
    val result = QuizRepository.importCoursePackage(context, text)
}

filePicker.launch(arrayOf("*/*"))  // MIME 接受所有类型
```

**无需运行时权限**：SAF 自动授予临时读取权限。

### 9.8 与现有 ImportScreen 完全解耦

课程包导入**不经过** `ImportScreen.startParse()`，避免影响现有题库导入流程。两个入口独立工作：

- 「数据管理」中的导入 → `ImportScreen` → 处理普通题库 JSON / 备份包
- 「边学边答」中的导入 → `StudyScreen` → 处理课程包 JSON

---

## 十、附录：FAQ

### Q1：课程包与标准题库 JSON 的区别？

| 维度 | 标准题库 | 课程包 |
|------|----------|--------|
| 顶层节点 | `name`/`questions` 或 `banks` | `course`/`questions` |
| 教程章节 | 不含 | 含 `course.sections` |
| 题目关联 | 无 | 通过 `knowledgePoints` 关联章节 |
| 导入入口 | 数据管理 / 导入页 | 边学边答 / 学习主页 |
| 学习流程 | 仅练习 | 学 → 练 → 进度跟踪 |

### Q2：能否只导入教程不导入题目？

可以。`questions` 数组留空 `[]` 或省略即可。系统会创建无关联题库的课程，章节学习页显示「本章暂无题目」。

### Q3：章节 ID 命名建议？

- 推荐 ASCII 短标识：`ch1_3`、`module_2_lesson_1`
- 避免中文字符、空格、特殊符号
- 同一课程内 ID 必须唯一
- 与题目 `knowledgePoints` 字段值一致

### Q4：课程包能否跨课程复用题目？

可以。导入多门课程时，每门课程独立创建自己的 `QuizBank`。同一题目 JSON 出现在不同课程包中会创建多份副本（每课程一份）。

### Q5：删除课程后能否恢复？

删除课程会同时清理关联题库与所有章节进度，**不可恢复**。建议删除前确认。

### Q6：进度数据存储在哪里？

`SharedPreferences`，文件名 `shiroha_quiz_native`，两个键：`knowledge_courses`、`study_progress`。卸载 APP 会清除。

### Q7：能否导出课程包？

当前版本未实现导出功能。后续可扩展：把 `KnowledgeCourse` 序列化回 JSON（含 `sections` 与从 `linkedBankId` 题库提取的 `questions`）。

---

## 版本与维护

- **对应代码版本**：v32.x（与《原生开发进度.md》同步）
- **依赖组件**：
  - `QuizRepository`（含 `importBank`/`deleteBank`/`parseQuestion`/`parseQuestionsArray`）
  - `ActivityResultContracts.OpenDocument`
  - `mutableStateListOf` / `mutableStateMapOf`（Compose 响应式状态）
- **可扩展方向**：
  - 课程包导出
  - Markdown 渲染（替代纯文本 `content`）
  - 富文本/图片内嵌代码块
  - 多语言（章节支持 `i18n` 节点）

---

**文档版本**：v1.0
**最后更新**：与代码同步
**相关文档**：
- 《Shiroha Quiz 题目导入解析方法说明》
- 《Shiroha Quiz 题库导入格式支持说明》
- 《Shiroha Quiz 题库导入策略与使用指南》