package com.yiqiu.shirohaquiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius

/**
 * 等宽字体代码块显示组件
 * 用于「边学边答」中展示教程代码示例
 * - 深色背景（浅色/深色模式均保持）
 * - 圆角、内边距
 * - 支持水平滚动保留缩进
 */
@Composable
fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
) {
    if (code.isBlank()) return
    val scrollState = rememberScrollState()
    val codeBg = Color(0xFF1E1E2E)
    val codeFg = Color(0xFFE6E6F0)
    val commentColor = Color(0xFF7F849C)
    val keywordColor = Color(0xFF82AAFF)
    val stringColor = Color(0xFFC3E88D)
    val numberColor = Color(0xFFF78C6C)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(codeBg, RoundedCornerShape(ShirohaRadius.Sm))
            .horizontalScroll(scrollState)
            .padding(contentPadding)
    ) {
        Text(
            text = buildHighlightedString(
                code = code,
                base = codeFg,
                comment = commentColor,
                keyword = keywordColor,
                string = stringColor,
                number = numberColor
            ),
            color = codeFg,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 22.sp,
            softWrap = false
        )
    }
}

private val kotlinKeywords = setOf(
    "fun", "val", "var", "if", "else", "when", "for", "while", "do",
    "return", "true", "false", "null", "is", "in", "out", "class",
    "object", "interface", "package", "import", "private", "public",
    "internal", "protected", "override", "open", "abstract", "companion",
    "data", "sealed", "enum", "by", "lateinit", "const", "suspend",
    "this", "super", "throw", "try", "catch", "finally", "as", "typealias"
)

private val tokenRegex = Regex("(\"[^\"]*\"|\\b[a-zA-Z_][a-zA-Z0-9_]*\\b|\\b\\d+(\\.\\d+)?[fFlL]?\\b|//.*$)")

/**
 * 构建带语法高亮的 AnnotatedString（使用 withStyle 安全作用域）
 */
private fun buildHighlightedString(
    code: String,
    base: Color,
    comment: Color,
    keyword: Color,
    string: Color,
    number: Color
): AnnotatedString = buildAnnotatedString {
    val lines = code.split("\n")
    lines.forEachIndexed { index, line ->
        val trimmed = line.trimStart()
        val isComment = trimmed.startsWith("//")
        if (isComment) {
            withStyle(SpanStyle(color = comment)) {
                append(line)
            }
        } else {
            var cursor = 0
            tokenRegex.findAll(line).forEach { match ->
                if (match.range.first > cursor) {
                    append(line.substring(cursor, match.range.first))
                }
                val token = match.value
                val color = when {
                    token.startsWith("//") -> comment
                    token.startsWith("\"") -> string
                    kotlinKeywords.contains(token) -> keyword
                    token.matches(Regex("\\b\\d+(\\.\\d+)?[fFlL]?\\b")) -> number
                    else -> null
                }
                if (color != null) {
                    withStyle(SpanStyle(color = color)) {
                        append(token)
                    }
                } else {
                    append(token)
                }
                cursor = match.range.last + 1
            }
            if (cursor < line.length) append(line.substring(cursor))
        }
        if (index < lines.size - 1) append("\n")
    }
}