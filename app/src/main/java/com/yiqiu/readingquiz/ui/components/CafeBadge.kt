package com.yiqiu.readingquiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * Cafe-ui Badge（caffe-ui §7 Badge spec）。
 *
 * - Pill 形
 * - bg 填充 + 1px border
 * - 11sp / 500 mono 字体（meta style）
 *
 * 变体：
 * - Default: muted 文字
 * - Up: accent 色文字 + accent 色 border（高亮状态）
 * - Filled: accent 填充 + accentFg 文字（重点状态）
 */
enum class CafeBadgeVariant { Default, Up, Filled, Correct, Wrong }

@Composable
fun CafeBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: CafeBadgeVariant = CafeBadgeVariant.Default
) {
    val (containerColor, contentColor, borderColor) = when (variant) {
        CafeBadgeVariant.Default -> Triple(CafeColors.Bg, CafeColors.Muted, CafeColors.Border)
        CafeBadgeVariant.Up -> Triple(CafeColors.Bg, CafeColors.Accent, CafeColors.Accent)
        CafeBadgeVariant.Filled -> Triple(CafeColors.Accent, CafeColors.AccentFg, Color.Transparent)
        CafeBadgeVariant.Correct -> Triple(CafeColors.Bg, CafeColors.Correct, CafeColors.Correct)
        CafeBadgeVariant.Wrong -> Triple(CafeColors.Bg, CafeColors.Wrong, CafeColors.Wrong)
    }
    Text(
        text = text,
        style = CafeType.meta,
        color = contentColor,
        modifier = modifier
            .clip(RoundedCornerShape(CafeRadius.rPill))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(CafeRadius.rPill))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}