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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.yiqiu.readingquiz.data.model.HighlightSpan
import com.yiqiu.readingquiz.ui.theme.CafeColors

/**
 * 高亮正文。
 * 点击高亮 span 弹出释义气泡（用 pointerInput + detectTapGestures 实现，
 * 规避 Compose 1.6.x 中 Modifier.clickable { offset: Int -> ... } 不存在的陷阱，
 * 见 spec 附录 A.3）。
 */
@Composable
fun CafeHighlightText(
    text: String,
    highlights: List<HighlightSpan>,
    fontSize: TextUnit = 16.sp,
    lineHeight: TextUnit = 26.sp,
    color: Color = CafeColors.Fg,
    onHighlightClick: ((HighlightSpan) -> Unit)? = null
) {
    val annotated = remember(text, highlights) { buildAnnotated(text, highlights) }
    val layoutResult = remember { mutableListOf<TextLayoutResult>() }

    BasicText(
        text = annotated,
        modifier = Modifier.pointerInput(annotated) {
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

private fun buildAnnotated(text: String, highlights: List<HighlightSpan>): AnnotatedString =
    buildAnnotatedString {
        append(text)
        highlights.forEach { span ->
            if (span.startIndex in 0..span.endIndex && span.endIndex <= text.length) {
                addStyle(
                    style = SpanStyle(
                        color = CafeColors.Accent2,
                        textDecoration = TextDecoration.Underline
                    ),
                    start = span.startIndex,
                    end = span.endIndex
                )
            }
        }
        withStyle(SpanStyle(color = color())) { /* placeholder noop */ }
    }

private fun color(): Color = CafeColors.Fg