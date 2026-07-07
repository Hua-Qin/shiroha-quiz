package com.yiqiu.readingquiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ui.theme.CafeBrand
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing

/**
 * Cafe-ui Icon Tile（caffe-ui §7 Feature card icon spec）。
 *
 * - 36×36（用 CafeSpacing.iconTileSize）
 * - 8dp 圆角
 * - brandGradient 填充
 * - 白色 unicode / Material Icon（16-20dp）
 *
 * 也支持纯字符 emoji / unicode 渲染：传入 `glyph: String` 时显示文字。
 */
@Composable
fun CafeIconTile(
    icon: ImageVector? = null,
    glyph: String? = null,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = CafeSpacing.iconTileSize,
    contentColor: Color = CafeColors.AccentFg
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(CafeRadius.rMd))
            .background(CafeBrand.brandGradient),
        contentAlignment = Alignment.Center
    ) {
        when {
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            glyph != null -> Text(
                text = glyph,
                color = contentColor,
                fontSize = TextUnit(18f, TextUnitType.Sp)
            )
        }
    }
}