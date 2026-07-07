package com.yiqiu.readingquiz.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.yiqiu.readingquiz.data.model.HighlightSpan
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * Cafe-ui 高亮正文（caffe-ui §7 Highlight text spec）。
 *
 * - 默认用 CafeType.body（16sp / 400）+ CafeColors.fg
 * - 高亮 span 用 CafeColors.accent + 下划线
 * - 点击高亮 span 弹出释义气泡（用 pointerInput + detectTapGestures 实现）
 */
@Composable
fun CafeHighlightText(
    text: String,
    highlights: List<HighlightSpan>,
    modifier: Modifier = Modifier,
    style: TextStyle = CafeType.body,
    onHighlightClick: ((HighlightSpan) -> Unit)? = null
) {
    val annotated = remember(text, highlights) { buildAnnotated(text, highlights, style.color) }
    val layoutResult = remember { mutableListOf<TextLayoutResult>() }

    BasicText(
        text = annotated,
        modifier = modifier.pointerInput(annotated) {
            detectTapGestures { offset ->
                val result = layoutResult.firstOrNull() ?: return@detectTapGestures
                val position = result.getOffsetForPosition(offset)
                val clicked = highlights.firstOrNull { span ->
                    position in span.startIndex until span.endIndex
                }
                if (clicked != null) onHighlightClick?.invoke(clicked)
            }
        }
    )
}

private fun buildAnnotated(
    text: String,
    highlights: List<HighlightSpan>,
    baseColor: Color
): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = baseColor)) {
        append(text)
    }
    highlights.forEach { span ->
        if (span.startIndex in 0..span.endIndex && span.endIndex <= text.length) {
            addStyle(
                style = SpanStyle(
                    color = CafeColors.Accent,
                    textDecoration = TextDecoration.Underline
                ),
                start = span.startIndex,
                end = span.endIndex
            )
        }
    }
}