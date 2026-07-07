package com.yiqiu.readingquiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * Cafe-ui List Row（caffe-ui §7 List row spec）。
 *
 * - 12×16dp 内边距
 * - 1px 顶 border（首行无）
 * - name 13.5sp / 500
 * - meta 11.5sp mono muted
 * - trailing badge（可选）
 */
@Composable
fun CafeListRow(
    name: String,
    meta: String? = null,
    modifier: Modifier = Modifier,
    showTopBorder: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(0.dp)
    val borderModifier = if (showTopBorder) {
        Modifier.border(1.dp, CafeColors.Border, shape)
    } else Modifier
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier)
            .then(clickModifier)
            .background(CafeColors.Surface)
            .padding(horizontal = CafeSpacing.listRowPadH, vertical = CafeSpacing.listRowPadV),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = CafeType.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = CafeColors.Fg
            )
            if (meta != null) {
                Text(
                    text = meta,
                    style = CafeType.meta,
                    color = CafeColors.Muted
                )
            }
        }
        if (trailing != null) trailing()
    }
}