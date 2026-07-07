package com.yiqiu.readingquiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ui.theme.CafeBrand
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * Cafe-ui CTA Banner（caffe-ui §7 CTA banner spec）。
 *
 * - 全宽，24dp 圆角
 * - 135° brand gradient 填充（accent → accent2）
 * - 标题 32sp / 700（displaySection）
 * - body 16sp 92% 透明白
 * - 双按钮组（primary OnDark + secondary）可选
 */
@Composable
fun CafeCtaBanner(
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    primaryButton: @Composable (() -> Unit)? = null,
    secondaryButton: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CafeRadius.rHero))
            .background(CafeBrand.brandGradient)
            .padding(horizontal = 28.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = CafeType.displaySection,
            color = CafeColors.AccentFg
        )
        if (body != null) {
            Text(
                text = body,
                style = CafeType.body,
                color = CafeColors.AccentFg.copy(alpha = 0.92f)
            )
        }
        if (primaryButton != null || secondaryButton != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (primaryButton != null) primaryButton()
                if (secondaryButton != null) secondaryButton()
            }
        }
    }
}