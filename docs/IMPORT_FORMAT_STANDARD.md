# Reading Quiz 导入格式技术规范 v2

> 本文档面向**开发者**（维护 ArticleImporter / BackupExporter / ArticleImporterTest）。
> 字段映射、JSON Schema、可空性、最大长度、跨章节 ID 规则、与主项目 Persistence.kt 的兼容矩阵。
> 使用者向文档：[IMPORT_USER_GUIDE.md](./IMPORT_USER_GUIDE.md)。
> 完整字段表：[IMPORT_FORMAT.md](../IMPORT_FORMAT.md)。

---

## 1. 版本管理

| 版本 | 发布日期 | 兼容 | 变更 |
|---|---|---|---|
| 1.0 | 2025-12 | ✅ | 初始版本（JSON / Markdown / TXT） |
| 2.0 | 2026-02 | ✅ 向前 | Markdown 栈式递归解析 + section 压平 L4+ |
| **2.5** | **2026-07** | ✅ 向前 | + Question.sectionId / anchorText / Section.id / SectionProgress；+ JSON Schema |

**兼容性原则**：
- 新增字段 → **可选**，旧导入文件保留全部功能
- 字段类型变更 → **major version bump**，提供迁移脚本
- 字段删除 → **major version bump**，提供过渡版本

---

## 2. 文件格式支持矩阵

| 扩展名 | MIME | 解析器 | 入口 | 章节绑定 | 单测 |
|---|---|---|---|---|---|
| `.json` | `application/json` | `ArticleImporter.importArticleJson` | `FileImporter.importFromUri` | ✅ 显式 | ✅ |
| `.md`/`.markdown` | `text/markdown` | `ArticleImporter.importMarkdown` | 同上 | ✅ 自动生成 ID | ✅ |
| `.txt` | `text/plain` | `ArticleImporter.importPlainText` | 同上 | ⚠️ 仅顶层 | ✅ |

可执行范围：
```kotlin
launcher.launch(arrayOf("application/json", "text/markdown", "text/plain", "text/*"))
```

---

## 3. JSON Schema v2.5（嵌入式）

### 3.1 顶层 Article

```json
{
  "type": "object",
  "required": ["title", "blocks"],
  "properties": {
    "id": { "type": "string", "description": "UUID；省略时随机生成" },
    "title": { "type": "string", "maxLength": 200, "description": "文章标题" },
    "author": { "type": "string", "maxLength": 100, "default": "" },
    "source": { "type": "string", "maxLength": 200, "default": "" },
    "category": { "type": "string", "maxLength": 50, "default": "" },
    "coverSummary": { "type": "string", "maxLength": 300, "default": "" },
    "favorite": { "type": "boolean", "default": false },
    "blocks": { "type": "array", "minItems": 1, "items": { "$ref": "#/$defs/block" } }
  },
  "additionalProperties": false,
  "$defs": {
    "block": {
      "oneOf": [
        { "$ref": "#/$defs/paragraph" },
        { "$ref": "#/$defs/image" },
        { "$ref": "#/$defs/section" }
      ]
    },
    "paragraph": {
      "type": "object",
      "required": ["type", "text"],
      "properties": {
        "type": { "const": "paragraph" },
        "text": { "type": "string", "maxLength": 10000 },
        "highlights": {
          "type": "array",
          "items": { "$ref": "#/$defs/highlight" },
          "default": []
        }
      }
    },
    "image": {
      "type": "object",
      "required": ["type", "path"],
      "properties": {
        "type": { "const": "image" },
        "path": { "type": "string", "pattern": "^https?://" },
        "caption": { "type": "string", "default": "", "maxLength": 200 }
      }
    },
    "section": {
      "type": "object",
      "required": ["type", "title", "level", "children"],
      "properties": {
        "type": { "const": "section" },
        "id": { "type": "string", "pattern": "^S#[0-9]{2,}$" },
        "title": { "type": "string", "maxLength": 200 },
        "level": { "type": "integer", "minimum": 1, "maximum": 3 },
        "children": { "type": "array", "items": { "$ref": "#/$defs/block" } }
      }
    },
    "highlight": {
      "type": "object",
      "required": ["text", "startIndex", "endIndex"],
      "properties": {
        "text": { "type": "string" },
        "startIndex": { "type": "integer", "minimum": 0 },
        "endIndex": { "type": "integer", "minimum": 0 },
        "explanation": { "type": "string", "default": "" }
      }
    }
  }
}
```

### 3.2 Question（AI 生成题目）

```json
{
  "type": "object",
  "required": ["id", "type", "question"],
  "properties": {
    "id": { "type": "string", "description": "UUID；运行期生成" },
    "type": { "enum": ["SINGLE", "MULTIPLE", "JUDGE", "BLANK", "SHORT"] },
    "question": { "type": "string", "maxLength": 500 },
    "options": { "type": "array", "items": { "type": "object", "required": ["key", "text"], "properties": { "key": { "type": "string" }, "text": { "type": "string" } } } },
    "answer": { "type": "array", "items": { "type": "string" } },
    "blankAnswers": { "type": "array", "items": { "type": "string" } },
    "analysis": { "type": "string", "maxLength": 1000 },
    "category": { "type": "string", "default": "" },
    "sectionId": { "type": ["string", "null"], "pattern": "^S#[0-9]{2,}$", "description": "指向 ArticleBlock.Section.id" },
    "anchorText": { "type": "string", "maxLength": 80, "default": "" }
  }
}
```

### 3.3 SectionProgress（学习进度）

```json
{
  "type": "object",
  "required": ["articleId", "sectionId"],
  "properties": {
    "articleId": { "type": "string" },
    "sectionId": { "type": "string", "pattern": "^S#[0-9]{2,}$" },
    "completed": { "type": "boolean", "default": false },
    "wrongCount": { "type": "integer", "minimum": 0, "default": 0 },
    "unansweredCount": { "type": "integer", "minimum": 0, "default": 0 },
    "lastUpdated": { "type": "integer", "description": "Unix ms timestamp" }
  }
}
```

---

## 4. 字段可空性与默认值

### 4.1 ArticleBlock

| 字段 | 可空 | 默认值 | 备注 |
|---|---|---|---|
| `Section.title` | 否 | — | 必填 |
| `Section.level` | 否 | — | 1/2/3，超出截断为 3 |
| `Section.children` | 否 | `[]` | 可递归 |
| `Section.id` | 是 | `""` | Markdown 自动生成 `S#NN` 格式 |
| `Paragraph.text` | 否 | — | 必填 |
| `Paragraph.highlights` | 否 | `[]` | 旧数据缺省 `[]` |
| `Image.path` | 否 | — | 必须 HTTP/HTTPS |
| `Image.caption` | 是 | `""` | 可空 |

### 4.2 Question

| 字段 | 可空 | 默认值 | 备注 |
|---|---|---|---|
| `id` | 否 | UUID 自动生成 | 运行时分配 |
| `type` | 否 | SINGLE | 解析失败回退到 SINGLE |
| `question` | 否 | — | 必填 |
| `options` | 是 | `[]` | SINGLE/MULTIPLE/JUDGE 必填 |
| `answer` | 是 | `[]` | JUDGE `[A]` / `[B]`；BLANK 多空时列表对齐空数 |
| `blankAnswers` | 是 | `[]` | 多空填空 |
| `analysis` | 是 | `""` | 可空 |
| `category` | 是 | `""` | |
| **sectionId** | **是** | `null` | **v2.5 新增** |
| **anchorText** | **是** | `""` | **v2.5 新增** |

### 4.3 SectionProgress

| 字段 | 可空 | 默认值 |
|---|---|---|
| `articleId` | 否 | — |
| `sectionId` | 否 | — |
| `completed` | 否 | `false` |
| `wrongCount` | 否 | `0` |
| `unansweredCount` | 否 | `0` |
| `lastUpdated` | 否 | `System.currentTimeMillis()` |

---

## 5. 章节 ID 生成规则

### 5.1 Markdown 导入时

`ArticleImporter.parseMarkdownSections` 内部计数器 `sectionCounter[0]`，每次遇到新 Section 自增，格式：

```
S# + 两位零填充数字
```

示例：第一篇文章的章节 ID 序列为 `S#01`, `S#02`, `S#03`。

### 5.2 JSON 导入时

JSON 中显式提供 `id` 字段：

```json
{ "type": "section", "id": "S#05", "title": "...", "level": 1, "children": [] }
```

- 必须匹配正则 `^S#[0-9]{2,}$`
- 缺省为空字符串（视为未绑定章节）
- **不推荐使用非标准 ID 格式** — AI `sectionId` 填空时会按 `S#NN` 字面匹配

### 5.3 跨文章 ID 冲突

每个 ArticleBlock.Section 的 ID 仅在该文章内唯一。跨文章时通过 `"$articleId#$sectionId"` 复合 key 区分。

---

## 6. 与主项目 Persistence.kt 兼容矩阵

> 主项目 `apps/android/app/src/native/Persistence.kt`（不在本仓库）字段对齐。

| 子项目（v2.5） | 主项目字段 | 类型匹配 | 序列化兼容 | 反序列化兼容 |
|---|---|---|---|---|
| `Article.id` | `id` | ✅ | ✅ 直传 | ✅ 直读 |
| `Article.title` | `title` | ✅ | ✅ | ✅ |
| `Article.author` | `author` | ✅ | ✅ | ✅ |
| `Article.source` | `source` | ✅ | ✅ | ✅ |
| `Article.category` | `category` | ✅ | ✅ | ✅ |
| `Article.coverSummary` | — (新增) | ⚠️ 子项目专用 | ❌ 主项目忽略 | ❌ 主项目不读 |
| `Article.favorite` | `favorite` | ✅ | ✅ | ✅ |
| `Article.blocks` | `blocks` | ✅ | ✅ | ✅ |
| `Article.notes` | `notes` | ✅ | ✅ | ✅ |
| `Article.createdAt` | `createdAt` | ✅ | ✅ | ✅ |
| `Article.updatedAt` | `updatedAt` | ✅ | ✅ | ✅ |
| `Question.sectionId` | — (新增) | ⚠️ 子项目专用 | ❌ 主项目忽略 | ❌ 主项目放弃 |
| `Question.anchorText` | `anchorText` (notes 也有) | ⚠️ 命名冲突但语义独立 | ⚠️ 字段同名但语义不同 | ⚠️ 字段同名 |
| `SectionProgress.*` | — (新增) | ⚠️ 子项目专用 | ❌ | ❌ |

**兼容策略**：导出去主项目时，仅在 v2.5 子项目之间互通完整字段；导入主项目备份时忽略子项目专用字段。

---

## 7. JSON 序列化反序列化关键代码点

### 7.1 Article ↔ JSON

实现位置：`ReadingRepository.articleToJson` / `articleFromJson` / `blockToJson` / `blockFromJson`

| 路径 | 操作 |
|---|---|
| `Article → JSON` | `articleToJson(article)` → `blockToJson(it)` 递归序列化所有 blocks |
| `JSON → Article` | `articleFromJson(o)` → `getJSONArray("blocks")` → `blockFromJson(it)` 递归 |

### 7.2 Question ↔ JSON

| 路径 | 操作 |
|---|---|
| `Question → JSON` | `questionToJson` 包含全部字段（`sectionId` 序列化为 `JSONObject.NULL` 当 null） |
| `JSON → Question` | `questionFromJson` 用 `o.isNull("sectionId")` 严格判 null |

### 7.3 关键反序列化陷阱

⚠️ **`JSONObject.optString("sectionId")` 遇 `JSONObject.NULL` 返回字符串 `"null"`**，不是 null：
```kotlin
// 错误：会得到 "null" 字符串
sectionId = o.optString("sectionId", "")

// 正确：
sectionId = if (o.isNull("sectionId")) null else o.optString("sectionId", "").ifBlank { null }
```

---

## 8. 持久化存储键

| 数据 | SharedPreferences key | 序列化方式 |
|---|---|---|
| 文章列表 | `articles_json` | JSONArray of articleToJson |
| 笔记 | `notes_json` | JSONArray |
| 会话 | `sessions_json` | JSONArray |
| AI 配置 | `ai_config_json` | JSONObject（不含密钥导出）|
| 题目缓存 | `questions_map_json` | JSONObject of articleId → JSONArray |
| **章节进度** | **`section_progress_json`** | JSONObject of `"articleId#sectionId"` → progress |

---

## 9. 单测覆盖清单

| 测试用例 | 文件 | 覆盖范围 |
|---|---|---|
| `valid JSON parses correctly` | `ArticleImporterTest` | JSON 导入 + 嵌套章节 + section id |
| `valid Markdown assigns section ids in order` | 同上 | Markdown 解析 + ID 自动生成 |
| `plain text imports as paragraphs with filename title` | 同上 | TXT 分段 |
| `broken JSON returns null without crashing` | 同上 | 损坏 JSON 容错 |
| `empty plain text returns empty blocks` | 同上 | 空 TXT 边界 |

运行命令：
```bash
cd reading-quiz
./gradlew :app:testDebugUnitTest --tests com.yiqiu.readingquiz.data.importexport.ArticleImporterTest
```

---

## 10. 错误处理契约

| 错误类别 | 何时触发 | 返回值 |
|---|---|---|
| `config` | Base URL/Key 为空 | `AiResult.Failure("config", "...")` |
| `http` | HTTP 4xx/5xx | `AiResult.Failure("http", "HTTP 401: ...")` |
| `exception` | 网络异常/超时 | `AiResult.Failure("exception", "...")` |
| `parse` | JSON 解析失败 | `AiResult.Failure("parse", "...")` |
| `Success` | 全部通过 | `AiResult.Success(value)` |

调用方应**严格匹配 category**，避免误把 `Failure` 当 `Success`。

---

## 11. 未来 Schema 演进路线

| 版本 | 计划 |
|---|---|
| 2.6 | + MathJax 公式支持（`$...$` 与 `$$...$$`）|
| 2.7 | + 关联题目引用（`relatedQuestionIds: [questionId]`）|
| 3.0 | + 多语言（`locale: "zh-CN" / "en-US"`）|

每次演进保留 v2.5 兼容性 ≥ 2 个 minor 版本。

---

## 12. 相关文件索引

| 文件 | 作用 |
|---|---|
| `app/src/main/java/.../data/importexport/ArticleImporter.kt` | 三种格式解析器 |
| `app/src/main/java/.../data/importexport/FileImporter.kt` | URI → 解析器统一入口 |
| `app/src/main/java/.../data/importexport/BackupExporter.kt` | 导出（含 sectionId / anchorText） |
| `app/src/main/java/.../data/ReadingRepository.kt` | 持久化 + sectionProgress |
| `app/src/test/java/.../data/importexport/ArticleImporterTest.kt` | 5 个单测 |
| `app/src/test/resources/fixtures/*.md/json/txt` | 标准化 fixture |
| `../IMPORT_FORMAT.md` | 完整字段表（13 章节） |
| `./IMPORT_USER_GUIDE.md` | 使用者文档 |