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