# Reading Quiz - 导入格式规范

本文档描述 Reading Quiz 应用支持的文章导入格式，与主项目 `apps/android/.../native/Persistence.kt` 字段对齐。

## 一、支持的文件格式

| 格式 | 扩展名 | 解析器 | 用途 |
|---|---|---|---|
| JSON | `.json` | `ArticleImporter.importArticleJson` | 结构化导入（含元数据 / 章节 / 高亮） |
| Markdown | `.md` / `.markdown` | `ArticleImporter.parseMarkdownSections` | 教程文章（带章节层级） |
| 纯文本 | `.txt` | `ArticleImporter.importPlainText` | 兜底（按空行分段） |

## 二、JSON 格式规范

### 最小示例（flat 段落）

```json
{
  "id": "可选-UUID",
  "title": "文章标题",
  "author": "作者",
  "source": "来源",
  "category": "分类",
  "coverSummary": "封面摘要",
  "favorite": false,
  "blocks": [
    { "type": "paragraph", "text": "第一段正文", "highlights": [] },
    { "type": "image", "path": "https://example.com/x.png", "caption": "图注" }
  ]
}
```

### 完整示例（含章节层级 + 高亮）

```json
{
  "id": "kotlin-basics-01",
  "title": "Kotlin 入门",
  "author": "JetBrains",
  "source": "kotlinlang.org",
  "category": "编程语言",
  "coverSummary": "Kotlin 是 JetBrains 开发的静态类型编程语言。",
  "favorite": false,
  "blocks": [
    {
      "type": "section",
      "title": "走进新语言",
      "level": 1,
      "children": [
        {
          "type": "section",
          "title": "开发环境配置",
          "level": 2,
          "children": [
            { "type": "paragraph", "text": "Kotlin 程序运行在 JVM 上，需要 JDK 8+。", "highlights": [] },
            { "type": "paragraph", "text": "Kotlin 编译器可以从 GitHub 下载。", "highlights": [] }
          ]
        }
      ]
    },
    {
      "type": "paragraph",
      "text": "Kotlin 与 Java 完全兼容。",
      "highlights": [
        { "text": "完全兼容", "startIndex": 4, "endIndex": 8, "explanation": "Kotlin 可以调用 Java 代码，反之亦然。" }
      ]
    }
  ]
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | String | 否 | 文章唯一 ID；省略时随机生成 |
| `title` | String | 是 | 文章标题 |
| `author` | String | 否 | 作者 |
| `source` | String | 否 | 来源 |
| `category` | String | 否 | 分类 |
| `coverSummary` | String | 否 | 封面摘要 |
| `favorite` | Boolean | 否 | 是否收藏（默认 false） |
| `blocks` | Array | 是 | 文章块列表（Paragraph / Image / Section） |

### Block 类型

#### Paragraph

```json
{ "type": "paragraph", "text": "段落内容", "highlights": [] }
```

`highlights` 是 `HighlightSpan` 列表：

```json
{
  "text": "关键词",
  "startIndex": 4,
  "endIndex": 8,
  "explanation": "点击气泡显示的解释"
}
```

#### Image

```json
{ "type": "image", "path": "https://...", "caption": "图注" }
```

#### Section（章节嵌套）

```json
{
  "type": "section",
  "title": "章节标题",
  "level": 1,
  "children": [ ...任意 block 列表... ]
}
```

`level`: 1=主章节，2=子章节，3=子子章节。`children` 可以嵌套任意 block。

## 三、Markdown 格式规范

### 章节层级识别

| 语法 | 层级 | 渲染样式 |
|---|---|---|
| `# 标题` | L1 主章节 | CafeType.Title，CafeColors.Accent |
| `## 标题` | L2 子章节 | CafeType.Heading，缩进 8dp |
| `### 标题` | L3 子章节 | CafeType.BodyBold，缩进 16dp |
| `#### 标题` | L3（压平） | 同上 |

### 段落

空行分段。同段落内的多行连续文字会拼接为单个段落。

### 图片

```markdown
![图注文字](https://example.com/x.png)
```

### 完整示例

````markdown
# Kotlin 程序设计初级篇

> 引用：在开始学习之前，推荐各位小伙伴有一定的编程语言基础。

Kotlin 是一种现代但已经成熟的编程语言，旨在让开发人员更快乐。

## 走进新语言

欢迎大家进入到 Kotlin 程序设计的学习中。

### 开发环境配置

要开发 Kotlin 程序，我们首先需要安装 Java 环境。

### IDEA 安装与使用

推荐使用 IntelliJ IDEA 社区版作为开发工具。

## 变量与基本类型

### 数字类型介绍

Kotlin 提供了 `Int`、`Long`、`Float`、`Double` 等数字类型。

![Kotlin Logo](https://example.com/kotlin.png)
````

导入后渲染效果：

- **Kotlin 程序设计初级篇**（L1 主章节，Accent 色）
  - 走进新语言（L2 子章节）
    - 开发环境配置（L3）
    - IDEA 安装与使用（L3）
  - 变量与基本类型（L2 子章节）
    - 数字类型介绍（L3）

## 四、TXT 纯文本

按空行分段，每段作为一个 Paragraph，文件名作标题。

```
第一段内容。

第二段内容。

第三段内容。
```

## 五、导入路径

用户操作路径：`首页 → 右上角设置 → AI 设置页 → 滚动到 AI 配置下方 → 点击「选择文件导入」`。

支持的文件类型：`application/json`、`text/markdown`、`text/plain`、`text/*`。

## 六、调试日志

所有导入路径会输出 `adb logcat -s FileImport:V ArticleImport:V Repo:V` 可见的日志，定位导入失败原因。

## 七、错误排查

| 错误现象 | 原因 | 解决方案 |
|---|---|---|
| 导入失败：无法读取文件 | Uri 无效或权限不足 | 重启应用后重试 |
| 导入失败：解析失败 | JSON 格式错误或 Markdown 无标题 | 检查文件编码（UTF-8）+ 格式 |
| 导入成功但文章为空 | JSON `blocks` 缺失或为空数组 | 补充 `blocks` 字段 |
| 章节未显示 | Markdown 标题行首有空格 | 删除行首空格 |

---

## 八、使用指南（端到端流程）

### 8.1 从文件管理器导入

1. 准备 `.json` / `.md` / `.txt` 格式文章文件（推荐 UTF-8 编码）
2. 打开应用 → 首页 → 右上角 ⚙️ 设置图标 → 进入 **AI 设置** 页
3. 滚动到 **AI 配置下方** → 「导入文章」分区
4. 点击 **「选择文件导入」** → 系统文件选择器打开
5. 选择文件 → 自动解析 → 导入成功后回到首页可见新文章

### 8.2 导入成功后做什么

- **进入文章**：点击首页列表项 → 进入阅读页
- **查看章节层级**：阅读页顶部主章节标题（L1）以 Accent 色显示
- **生成题目**：阅读页底部 → 「进入答题」→ AI 自动生成题目
- **记笔记**：阅读页底部 → 「记笔记」图标 → 选择菜单 → 「新建笔记」或「直接编辑文档」

### 8.3 章节合并策略

- `#### 标题`（L4+）自动**压平**为 L3（与 `###` 等价）
- 若同一文件有多个 `# Title`，取**首个**作为文章标题，其余视作章节
- 标题行允许带 BOM 字符；纯空格缩进不影响识别

---

## 九、踩坑指南（FAQ）

### Q1：为什么导入后文章标题是文件名？
**A**：Markdown 文件首个 `# Title` 不存在或被空行隔断。检查文件首行：
```markdown
# Kotlin 程序设计初级篇   ← 必须为第一行或第一段的首行
## 走进新语言
```

### Q2：JSON 中 `blocks` 是空数组怎么办？
**A**：会被解析为空文章。请补充至少一个 paragraph 或 image 块：
```json
{ "type": "paragraph", "text": "正文内容", "highlights": [] }
```

### Q3：图片不显示？
**A**：
1. URL 必须 HTTPS（HTTP 在 release 包会被阻止）
2. 异步加载需要时间，请稍候或下拉刷新
3. 若服务器禁用 Referer，可改用 `cdn.example.com` 直链

### Q4：Markdown 列表 / 表格 / 代码块支持吗？
**A**：支持。`MarkdownText` 组件（依赖 `compose-markdown 0.5.8`）支持：
- 列表（`-` / `*` / `1.`）
- 表格（`| col1 | col2 |`）
- 代码块（` ```kotlin `）
- 引用（`>`）
- 行内粗体 / 斜体 / 代码 / 链接

### Q5：如何导入多张图片？
**A**：每行一张图片，独立成行（不能与段落同行）：
```markdown
段落一。

![图1](https://a.com/1.png)

段落二。

![图2](https://a.com/2.png)
```

### Q6：导入很慢或超时？
**A**：
1. 大文件（>1MB）建议分批
2. 网络不佳时增大 AI 设置中的「超时秒数」（默认 60）
3. JSON 文件 >500KB 时建议拆分章节

### Q7：高亮（highlights）不生效？
**A**：
1. `startIndex` / `endIndex` 必须是 UTF-16 代码单元偏移（Java/Kotlin String 索引）
2. 中文字符占 1 个代码单元；emoji 占 2 个
3. `text` 必须**精确匹配** `paragraph.text.substring(startIndex, endIndex)`

---

## 十、字段映射对照表（与主项目 Persistence.kt）

| 子项目字段 | 主项目 Persistence 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| `id` | `id` | String | 否 | UUID，默认自动生成 |
| `title` | `title` | String | 是 | 文章标题 |
| `author` | `author` | String | 否 | 作者 |
| `source` | `source` | String | 否 | 来源（Markdown 文件名作 source）|
| `category` | `category` | String | 否 | 分类 |
| `coverSummary` | `coverSummary` | String | 否 | 封面摘要（首页卡片副标题）|
| `favorite` | `favorite` | Boolean | 否 | 是否收藏 |
| `blocks` | `blocks` | Array | 是 | 见 Block 类型 |
| `notes` | `notes` | Array | 否 | 笔记列表（运行时填充，不从导入读取）|
| `createdAt` | `createdAt` | Long | 否 | 毫秒时间戳 |
| `updatedAt` | `updatedAt` | Long | 否 | 毫秒时间戳 |

> 与主项目的字段表对齐；新增字段仅在子项目中使用（如 `coverSummary`），导入主项目时需忽略。

---

## 十一、集成流程（开发者视角）

### 11.1 在代码中触发导入

```kotlin
val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
) { uri ->
    if (uri != null) {
        val result = FileImporter.importFromUri(context, uri)
        when (result) {
            is FileImporter.Result.Success -> showToast("已导入：${result.article.title}")
            is FileImporter.Result.Failure -> showToast("导入失败：${result.reason}")
        }
    }
}
launcher.launch(arrayOf("application/json", "text/markdown", "text/plain", "text/*"))
```

### 11.2 自定义 ArticleImporter

```kotlin
// 解析自定义格式
val customArticle = ArticleImporter.importArticleJson(rawText)
if (customArticle == null) {
    // 解析失败，读取错误日志定位问题
    Log.w("MyTag", "import failed")
}
```

### 11.3 调试日志

```bash
# 仅过滤导入相关日志
adb logcat -s FileImport:V ArticleImport:V Repo:V

# 查看完整错误堆栈
adb logcat -s ArticleImport:V *:E
```

---

## 十二、版本历史

| 版本 | 日期 | 变更 |
|---|---|---|
| 0.1.0-alpha | 2025-12 | 初始版本：JSON / Markdown / TXT 三格式导入 |
| 0.1.5 | 2026-01 | 新增 Section 嵌套（L1/L2/L3）+ 高亮 spans |
| 0.2.0 | 2026-02 | Markdown 解析改用栈式递归，支持 `####` 压平为 L3 |
| 0.2.5 | 2026-06 | 扩充本文档：FAQ / 字段映射 / 集成流程 |
| 0.3.0 | 2026-07 | 增加 MarkdownText 渲染（compose-markdown 0.5.8）+ 图片预览缩放 |

---

## 十三、未来路线

- **OPML 导入**：从 RSS / 稍后读服务批量导入
- **导出为 Markdown**：将现有文章导出为 `.md` 文件
- **双向同步**：与主项目 `apps/android` 数据互通

---

## 十四、自动化验证（v2：章节绑定）

### 14.1 运行单元测试

```bash
cd reading-quiz
./gradlew :app:testDebugUnitTest --tests com.yiqiu.readingquiz.data.importexport.ArticleImporterTest
```

预期输出：
```
> Task :app:testDebugUnitTest
BUILD SUCCESSFUL in 12s
4 tests completed, 0 failed
```

### 14.2 字段映射 v2（章节绑定 + 学习进度）

| 子项目字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `Question.sectionId` | String? | 否 | 指向 `ArticleBlock.Section.id`；null 表示未绑定章节（旧数据兼容） |
| `Question.anchorText` | String | 否 | 题目关联的原文片段（≤80 字符），用于去重与上下文展示 |
| `ArticleBlock.Section.id` | String | 否 | 章节稳定 ID；默认 `""`，Markdown 导入时自动生成 `S#01`、`S#02` 格式 |
| `SectionProgress.articleId` | String | 是 | 文章 ID |
| `SectionProgress.sectionId` | String | 是 | 章节 ID |
| `SectionProgress.completed` | Boolean | 否 | 是否已完成（默认 false） |
| `SectionProgress.wrongCount` | Int | 否 | 累计答错题数 |
| `SectionProgress.unansweredCount` | Int | 否 | 累计未答题数 |
| `SectionProgress.lastUpdated` | Long | 否 | 毫秒时间戳 |

### 14.3 Fixture 示例

#### 合法 Markdown（`fixtures/article-kotlin.md`）
```markdown
# Kotlin 入门

欢迎大家进入到 Kotlin 程序设计的学习中。

## 走进新语言

Kotlin 是一种现代但已经成熟的编程语言。

### 开发环境配置

要开发 Kotlin 程序，我们首先需要安装 Java 环境。
```

预期解析结果：
- 1 个 L1 章节：标题「Kotlin 入门」，ID `S#01`
- 1 个 L2 章节：「走进新语言」，ID `S#02`
- 1 个 L3 章节：「开发环境配置」，ID `S#03`

#### 损坏 JSON（`fixtures/article-broken.json`）
```json
{ "title": "缺失字段", "blocks": [ { "type": "paragraph", "text": "未闭合
```
预期：返回 `Failure("解析失败：缺失字段")`。

#### 空 TXT（`fixtures/article-empty.txt`）
（空文件）

预期：返回成功但 `blocks` 为空数组，标题为文件名（"article-empty"）。
- **导入历史**：保留最近 20 次导入记录，可回滚误操作