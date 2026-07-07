package com.yiqiu.readingquiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ui.theme.CafeBrand
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * Cafe-ui 卡片（caffe-ui §7 Feature card / Chart card spec）。
 *
 * 三种变体：
 * - Default (feature): surface + 1px border + 14dp 圆角 + 26dp 内边距
 * - Quote: 同 feature 风格，body 17sp display
 * - Inverted: fg 填充 + bg 文字（featured 价目 / 高亮卡）
 */
enum class CafeCardVariant { Default, Quote, Inverted }

@Composable
fun CafeCard(
    modifier: Modifier = Modifier,
    variant: CafeCardVariant = CafeCardVariant.Default,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(CafeSpacing.cardPad),
    content: @Composable () -> Unit
) {
    val (containerColor, borderColor) = when (variant) {
        CafeCardVariant.Default -> CafeColors.Surface to CafeColors.Border
        CafeCardVariant.Quote -> CafeColors.Surface to CafeColors.Border
        CafeCardVariant.Inverted -> CafeColors.Fg to Color.Transparent
    }
    val shape = RoundedCornerShape(CafeRadius.rCardLg)
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .then(clickModifier)
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * Feature Card 完整布局：icon tile + title + body + 可选 eyebrow 头部。
 *
 * 参数：
 * - icon: 36×36 gradient tile 中的 icon
 * - eyebrow: 顶部 eyebrow 文字（可选）
 * - title: 18sp/700
 * - body: muted 14.5sp
 */
@Composable
fun CafeFeatureCard(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    onClick: (() -> Unit)? = null
) {
    CafeCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(CafeSpacing.iconTileSize)
                    .clip(RoundedCornerShape(CafeRadius.rMd))
                    .background(CafeBrand.brandGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CafeColors.AccentFg,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                if (eyebrow != null) {
                    CafeEyebrow(text = eyebrow, showLeadingDot = true)
                }
                Text(
                    text = title,
                    style = CafeType.cardTitle,
                    color = CafeColors.Fg
                )
                Text(
                    text = body,
                    style = CafeType.bodySmall,
                    color = CafeColors.Muted
                )
            }
        }
    }
}

/**
 * Quote Card：作者引用，前置引号 icon。
 */
@Composable
fun CafeQuoteCard(
    body: String,
    author: String,
    role: String,
    modifier: Modifier = Modifier,
    avatarIcon: ImageVector? = null
) {
    CafeCard(modifier = modifier, variant = CafeCardVariant.Quote) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "\u201C$body\u201D",
                style = CafeType.body.copy(fontSize = androidx.compose.ui.unit.TextUnit(17f, androidx.compose.ui.unit.TextUnitType.Sp)),
                color = CafeColors.Fg
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (avatarIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(CafeRadius.rPill))
                            .background(CafeBrand.brandGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = avatarIcon,
                            contentDescription = null,
                            tint = CafeColors.AccentFg,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column {
                    Text(text = author, style = CafeType.bodySmall, color = CafeColors.Fg)
                    Text(text = role, style = CafeType.bodyXSmall, color = CafeColors.Muted)
                }
            }
        }
    }
}
