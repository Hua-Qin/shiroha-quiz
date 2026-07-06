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