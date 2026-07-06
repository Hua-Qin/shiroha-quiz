# 导入格式规范（嵌入式）

本文件作为应用内的格式参考，通过 `R.raw.import_format_spec` 引用。

## 支持的格式

1. **JSON（推荐）**：完整结构化数据，含章节嵌套与高亮
2. **Markdown**：教程类文章，自动识别 `#`/`##`/`###` 三级章节
3. **TXT 纯文本**：按空行分段，文件名作标题

## 章节层级映射

| 标记 | 层级 | 渲染样式 |
|---|---|---|
| `# Title` | L1 | 标题（Accent 色） |
| `## Title` | L2 | 子标题（缩进 8dp） |
| `### Title` | L3 | 三级标题（缩进 16dp） |

## 高亮语法（JSON 内）

```json
{
  "type": "paragraph",
  "text": "Kotlin 与 Java 完全兼容",
  "highlights": [
    { "text": "完全兼容", "startIndex": 4, "endIndex": 8, "explanation": "双向互操作" }
  ]
}
```

## 图片语法（JSON / Markdown）

JSON：
```json
{ "type": "image", "path": "https://...", "caption": "图注" }
```

Markdown：
```markdown
![图注](https://example.com/x.png)
```

## 导入路径

`首页 → 设置 → AI 设置 → AI 配置下方「选择文件导入」`

## 调试

`adb logcat -s FileImport:V ArticleImport:V Repo:V`