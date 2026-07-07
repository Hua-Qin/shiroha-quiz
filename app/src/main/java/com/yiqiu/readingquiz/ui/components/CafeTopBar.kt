package com.yiqiu.readingquiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ui.theme.CafeBrand
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * Cafe-ui 顶部导航栏（caffe-ui §7 Top navigation spec）。
 *
 * - 高度 64dp，navScrim 70% 半透明白底
 * - 左侧：返回按钮（可选）
 * - 中部：标题（17sp / 700）+ 可选副标题
 * - 右侧：自定义 actions 行
 * - 可选：品牌 logo（gradient 26×26 + wordmark 17sp / 700）
 */
@Composable
fun CafeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    showBrandLogo: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(CafeColors.NavScrim)
            .padding(horizontal = CafeSpacing.containerPad)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CafeSpacing.sm)
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "返回",
                        tint = CafeColors.Fg
                    )
                }
            }
            if (showBrandLogo) {
                BrandLogo()
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = CafeType.cardTitle.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = CafeColors.Fg,
                    maxLines = 1
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = CafeType.bodyXSmall,
                        color = CafeColors.Muted,
                        maxLines = 1
                    )
                }
            }
            actions()
        }
    }
}

/**
 * 品牌 logo：26×26 渐变方块 + 文字 wordmark（17sp / 700）。
 */
@Composable
fun BrandLogo(
    wordmark: String = "Reading Quiz",
    showWordmark: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(CafeRadius.rSm))
                .background(CafeBrand.brandGradient)
        )
        if (showWordmark) {
            Text(
                text = wordmark,
                style = CafeType.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = CafeColors.Fg
            )
        }
    }
}