# 导入指南：从 0 到可导入

> **目标**：照着抄，30 分钟做出一份能导入、能做题、有章节绑定的文章文件。
> 详细字段参考 [IMPORT_FORMAT.md](../IMPORT_FORMAT.md) 与 [IMPORT_FORMAT_STANDARD.md](./IMPORT_FORMAT_STANDARD.md)。

---

## 一、目录结构（你要做的就这两种东西）

```
article.json                  ← 文章 + 题目 + 答案（一次性导入）
articles/
  ├── math.md                 ← 一篇文章（一级章节 = 顶层 # 标题）
  └── physics.md
```

**两种工作流**：
- **A. JSON 单文件**：文章内容 + 题目 + 答案写在一个 JSON 里 → 适合结构化题库
- **B. Markdown 多文件**：文章内容写在 .md 里 → 题目由 AI 生成（无需手写）

---

## 二、A 流程：手写 JSON 文件（推荐，最完整）

### 2.1 复制模板（5 秒）

保存为 `article.json`：

```json
{
  "title": "Kotlin 入门",
  "author": "JetBrains",
  "category": "编程",
  "blocks": [
    {
      "type": "section",
      "title": "走进新语言",
      "level": 1,
      "children": [
        { "type": "paragraph", "text": "Kotlin 是一种现代的静态类型编程语言。", "highlights": [] }
      ]
    }
  ]
}
```

> 💡 **导入后系统会自动给每个 section 生成 `S#01`、`S#02` 这样的稳定 ID**，题目可以靠这个 ID 关联章节。

### 2.2 段落里能写什么

| `type` | 作用 | 示例 |
|---|---|---|
| `paragraph` | 段落文本 | `{ "type": "paragraph", "text": "你好", "highlights": [] }` |
| `image` | 图片 | `{ "type": "image", "path": "https://.../x.png", "caption": "图注" }` |
| `section` | 章节（可嵌套） | 见 2.3 |

### 2.3 一级 / 二级章节写法

**level=1 = 一级**（章节大纲里的可展开卡片），**level=2/3 = 二级以下**（每个可独立点开）：

```json
"blocks": [
  { "type": "section", "title": "走进新语言", "level": 1, "children": [
    { "type": "paragraph", "text": "介绍段…" },
    { "type": "section", "title": "开发环境", "level": 2, "children": [
      { "type": "paragraph", "text": "JDK 安装…" }
    ]}
  ]}
]
```

### 2.4 导入后系统在首页看到的

```
┌─ 文章卡片 ────────────────┐
│ Kotlin 入门                │
│ 编程 · JetBrains          │
│ [开始阅读]                │
└───────────────────────────┘
        ↓ 点击
┌─ 章节大纲 ────────────────┐
│ 📂 走进新语言            ▼ │  ← 一级，可展开
│   L2  开发环境          → │  ← 二级，可单独点
└───────────────────────────┘
        ↓ 点 L2
   → 跳到阅读页，自动滚到「开发环境」位置
```

---

## 三、B 流程：写 Markdown（最简单，AI 自动出题）

### 3.1 复制模板

保存为 `mynote.md`（UTF-8 编码）：

```markdown
# 我的第一篇文章

第一段正文。

## 这是子标题

第二段正文。
```

### 3.2 Markdown 标题层级 → 章节层级

| Markdown | 章节层级 | 在章节大纲中 |
|---|---|---|
| `# 标题` | 一级（L1） | 顶层卡片，可展开 |
| `## 标题` | 二级（L2） | L1 展开后，可独立点 |
| `### 标题` | 三级（L3） | L2 展开后，可独立点 |
| `#### 标题` | 三级（压平） | 同上 |

### 3.3 图片怎么写

```markdown
![图注](https://example.com/x.png)
```

必须 HTTPS。空行隔开。

### 3.4 导入后

- 首页出现文章卡片
- 进入后看到章节大纲
- 阅读页底部点「进入答题」→ AI 自动生成 5 道混合题型题目（SINGLE / MULTIPLE / JUDGE / BLANK / SHORT）
- AI 会自动把题目绑定到对应章节 ID（`S#01` 等）

**题目无需手写**。

---

## 四、题目字段表（手写 JSON 时参考）

> 99% 情况下不要手写题目 —— 用 AI 自动生成。只有结构化题库场景需要手写。

```json
{
  "type": "SINGLE",
  "question": "Kotlin 运行在哪个平台？",
  "options": [
    {"key": "A", "text": ".NET"},
    {"key": "B", "text": "JVM"},
    {"key": "C", "text": "Python 解释器"},
    {"key": "D", "text": "浏览器"}
  ],
  "answer": ["B"],
  "blankAnswers": [],
  "analysis": "Kotlin 编译为 JVM 字节码，兼容 Java。",
  "category": "概念",
  "sectionId": "S#01",
  "anchorText": "Kotlin 是一种现代的静态类型编程语言"
}
```

**5 种题型速查**：

| `type` | `options` | `answer` | `blankAnswers` |
|---|---|---|---|
| `SINGLE` | 4 个选项 | `["B"]` | `[]` |
| `MULTIPLE` | ≥2 个选项 | `["A","C"]` | `[]` |
| `JUDGE` | 固定 `[{"key":"A","text":"正确"},{"key":"B","text":"错误"}]` | `["A"]` | `[]` |
| `BLANK` | `[]` | `[]` | `["答案1","答案2"]`，题干用 ⬚ 占位 |
| `SHORT` | `[]` | `["参考答案"]` | `[]` |

---


**关键字段**：

| 字段 | 必填 | 说明 |
|---|---|---|
| `sectionId` | 否 | 关联到哪个章节（值如 `S#01`），没填视为未绑定 |
| `anchorText` | 否 | 题目的原文片段（≤80 字符），去重和上下文展示用 |

---

## 五、从 0 制作一份完整导入文件的步骤

### Step 1：选格式
- 含章节嵌套 → JSON
- 含 Markdown 语法（代码块 / 表格 / 列表）→ Markdown
- 纯段落 → TXT

### Step 2：抄模板
- JSON → 复制本文 §2.1
- Markdown → 复制本文 §3.1

### Step 3：填内容
按下面顺序填充，每填一段空一行：

```
# 一级标题（必须有，作为章节 L1）

第一段正文…

## 二级标题

第二段正文…
```

### Step 4：保存为 UTF-8

| 工具 | 操作 |
|---|---|
| VS Code | 右下角编码 → UTF-8 |
| Notepad++ | 编码 → UTF-8 |
| Windows 记事本 | 另存为 → 编码 UTF-8 |
| macOS TextEdit | 偏好设置 → 默认编码 UTF-8 |

### Step 5：导入到 App

1. 把文件传到手机（微信 / USB / 云盘）
2. 打开 Reading Quiz → 设置 → AI 设置 → **导入文章** → **选择文件导入**
3. 几秒后首页出现

### Step 6：验证

```
首页 → 文章卡片 → 章节大纲
   ├─ 一级章节显示？ ✓
   ├─ 二级章节显示？ ✓
   └─ 点二级 → 自动滚到正确位置 ✓

阅读页顶部 → 「已学章节 0/N」
答题 → 题目与章节对应 ✓
```

---

## 六、3 类错误 + 1 秒修复

| 报错 | 1 秒修复 |
|---|---|
| `解析失败：缺失字段` | JSON 缺 `title` 或 `blocks`，补上 |
| `导入成功但文章为空` | `blocks` 是空数组 `[]`，加至少一个 paragraph |
| `导入失败：无法读取文件` | 重启 App 重选；或重启手机 |

更多错误参考 [IMPORT_FORMAT.md §七](../IMPORT_FORMAT.md)。

---

## 七、目录命名规范（批量导入场景）

如果一次导入多份 Markdown，建议放同一目录：

```
articles/
  ├── kotlin-basics.md      → 导入后文章 ID 自动生成
  ├── python-intro.md
  └── algo-sorting.md
```

文件名仅用于**导入时的日志显示**，不影响标题（标题由首个 `# 标题` 决定）。

---

## 八、关联文档

- [IMPORT_FORMAT.md](../IMPORT_FORMAT.md) — 完整字段表 + 13 章节格式规范
- [IMPORT_FORMAT_STANDARD.md](./IMPORT_FORMAT_STANDARD.md) — JSON Schema + 主项目兼容矩阵
- [FEATURE_TEST_CASES.md](./FEATURE_TEST_CASES.md) — 功能测试用例

---

## 九、单独导入题目文件（给已有文章追加题库）

> **场景**：你已经有文章了，想给它**批量追加**题库（10、20、50 道都行）。
> 文章的题目数量是 0 也不影响 —— 直接点「AI 智能出题」或「导入题目」。

### 9.1 入口在哪里

1. 打开文章（点章节进入阅读页）
2. 底栏有 **「题目库」** 文字按钮（在「记笔记」右边，橙色 chip 样式）—— 点进去
3. 进入题目库页，有两个主要操作：
   - **顶部大按钮「AI 智能出题」**：让 AI 读文章自己出题（数量自动按文章字数决定）
   - **下方「导入题目」按钮**：从本地文件批量导入
4. 选择 `.json` / `.md` / `.txt` 文件 → 自动解析 → 自动追加到当前文章

### 9.2 JSON 格式（最推荐，结构化）

文件名 `questions.json`，支持两种顶层结构：

**结构 A：对象 + questions 数组**（最通用）

```json
{
  "questions": [
    {
      "type": "SINGLE",
      "question": "Kotlin 主要运行在哪个平台？",
      "options": [
        {"key": "A", "text": ".NET 运行时"},
        {"key": "B", "text": "JVM"},
        {"key": "C", "text": "Python 解释器"},
        {"key": "D", "text": "浏览器原生"}
      ],
      "answer": ["B"],
      "blankAnswers": [],
      "analysis": "Kotlin 编译为 JVM 字节码，兼容 Java 生态。",
      "category": "概念",
      "sectionId": "S#01",
      "anchorText": "Kotlin 是一种现代的静态类型编程语言"
    },
    {
      "type": "JUDGE",
      "question": "Kotlin 完全兼容 Java 代码。",
      "answer": ["A"],
      "blankAnswers": [],
      "analysis": "可与 Java 互操作，但并非完全兼容。",
      "category": "概念"
    },
    {
      "type": "BLANK",
      "question": "Kotlin 用 ⬚ 关键字声明只读变量，用 ⬚ 声明可变变量。",
      "answer": [],
      "blankAnswers": ["val", "var"],
      "analysis": "val = value（只读），var = variable（可变）。"
    }
  ]
}
```

**结构 B：顶层直接是数组**（更简洁）

```json
[
  {"type": "SINGLE", "question": "...", "options": [...], "answer": ["B"]},
  {"type": "JUDGE", "question": "...", "answer": ["A"]}
]
```

**字段说明**：
- `type`：`SINGLE` / `MULTIPLE` / `JUDGE` / `BLANK` / `SHORT`，不填默认 `SINGLE`
- `id`：可选，不填自动生成 UUID
- `sectionId`：可选，对应文章里的章节 ID（`S#01` 格式），用于在阅读页跳转
- `anchorText`：可选，原文片段（≤80 字），用于跨章节去重
- `category`：可选，分类标签（如「概念」「方法」）
- `analysis`：可选，解析说明

**判断题**：`options` 可以省略，系统自动注入「正确 / 错误」两项。

### 9.3 Markdown 格式（手写友好）

文件名 `questions.md`，每道题用 `## 序号. 题型` 分段。完整模板（复制即可用）：

```markdown
# Kotlin 入门题库

## 1. 单选题
**题干**：Kotlin 编译后主要运行在什么平台？
- A. .NET 运行时
- B. JVM
- C. Python 解释器
- D. 浏览器原生

**答案**：B

**解析**：Kotlin 编译为 JVM 字节码，兼容 Java 生态。

## 2. 多选题
**题干**：下列属于 Kotlin 特性的是？（多选）
- A. 空指针安全
- B. 扩展函数
- C. 宏定义
- D. 协程

**答案**：A, B, D

**解析**：Kotlin 的核心特性是空指针安全、扩展函数、协程；宏定义属于 C/C++。

## 3. 判断题
**题干**：Kotlin 完全兼容 Java 代码。

**答案**：A

**解析**：可与 Java 互操作，但并非完全兼容。

## 4. 填空题
**题干**：Kotlin 用 ⬚ 关键字声明只读变量，用 ⬚ 声明可变变量。

**填空答案**：val, var

**解析**：val = value（只读），var = variable（可变）。

## 5. 简答题
**题干**：简述 Kotlin 与 Java 的主要差异。

**答案**：空指针安全、扩展函数、协程、数据类、密封类、函数式编程支持更友好。

**解析**：Kotlin 在 Java 基础上引入了大量现代语言特性。
```

**关键规则**：
- 题目分段：`## 1. 单选题`（必须 `## ` 起）
- 题型识别：根据标题里的「单选/多选/判断/填空/简答」自动判定
- 题干：`**题干**：` 开头
- 选项：每行一个，`A. xxx` 或 `- A. xxx` 都行
- 答案：`**答案**：` 开头；多选用逗号或空格分隔
- 解析：`**解析**：` 开头
- 填空答案：`**填空答案**：` 开头（用逗号/分号分隔）
- 顺序：题干 → 选项 → 答案 → 解析（填空题可省选项）

### 9.4 TXT 格式（兜底，简化）

文件名 `questions.txt`，按行写：

```
Kotlin 编译后主要运行在什么平台？
A. .NET 运行时
B. JVM
C. Python 解释器
D. 浏览器原生
答案：B
```

- 第 1 行：题干
- 中间行：选项（`A. xxx` 格式）
- 末行：`答案：X`（必须以「答案」或「Answer」开头）
- 一份文件只能放一道题（要导入多道请用 JSON 或 Markdown）

### 9.5 导入后的操作

导入成功后，题目会**追加**到当前文章末尾：
- 列表里能看到新导入的题
- 长按题目卡片 → 弹出确认 → 删除
- 点击题目卡片 → 进入编辑器（可改题型、改选项、绑定章节）
- 章节绑定（`sectionId`）在导入时已写入的会保留；未填的可在编辑器里手填

### 9.6 常见问题

**Q：导入后没看到题目？**
A：先确认你在「题目库」页（不是阅读页）。导入是追加到当前文章的题库，不会清空已有题目。

**Q：JSON 报错「缺少 questions 数组」？**
A：你用了结构 B（顶层数组），请改用结构 A（`{"questions": [...]}`），或反过来。

**Q：Markdown 提示「未发现有效题目」？**
A：检查每道题是否用 `## 序号. 题型` 起头，并且至少有一行 `**题干**：xxx`。

**Q：导入的题跟 AI 出的题混在一起，能区分吗？**
A：当前版本不区分来源。可以在 `analysis` 字段里手动加「来源：手动导入」等标识。

**Q：能一次性给所有文章导入同一份题库吗？**
A：不能。导入是针对当前文章操作的。如果想给多篇文章导入相同题目，需要分别进入每篇文章的题目库页操作。