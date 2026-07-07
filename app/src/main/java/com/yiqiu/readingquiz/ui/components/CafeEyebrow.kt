package com.yiqiu.readingquiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * Cafe-ui Eyebrow / 状态标签（caffe-ui §7 Eyebrow spec）。
 *
 * - Pill 形（完全圆角）
 * - surface 填充 + 1px border
 * - eyebrow TextStyle（12sp / 500 mono / +0.08em / UPPERCASE）
 * - 可选 6px 圆点前缀（accent 色）
 */
@Composable
fun CafeEyebrow(
    text: String,
    modifier: Modifier = Modifier,
    showLeadingDot: Boolean = false,
    textColor: Color = CafeColors.Muted
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(CafeRadius.rPill))
            .background(CafeColors.Surface)
            .border(1.dp, CafeColors.Border, RoundedCornerShape(CafeRadius.rPill))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (showLeadingDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(CafeColors.Accent)
            )
        }
        Text(
            text = text.uppercase(),
            style = CafeType.eyebrow,
            color = textColor
        )
    }
}