package com.yiqiu.readingquiz.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeMotion
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Cafe-ui 按钮（caffe-ui §7 Button spec）。
 *
 * 三种变体：
 * - Primary: accent 填充 + accentFg 文字（主 CTA）
 * - Ghost: 透明 + 1px border + fg 文字（次要操作）
 * - OnDark: accentFg 背景 + accent 文字（CTA banner 上的反色按钮）
 *
 * 状态：default / pressed（+6% brightness）/ focus（visible ring）/ disabled（30% 透明）/ loading。
 * 高度 44-48dp；圆角 CafeRadius.rBtn；padding 13×22dp。
 *
 * 旧 CafePrimaryButton / CafeGhostButton 已废弃，统一用 CafeButton。
 */
enum class CafeButtonVariant { Primary, Ghost, OnDark }

@Composable
fun CafeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: CafeButtonVariant = CafeButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val (containerColor, contentColor, borderColor) = when (variant) {
        CafeButtonVariant.Primary -> Triple(CafeColors.Accent, CafeColors.AccentFg, Color.Transparent)
        CafeButtonVariant.Ghost -> Triple(Color.Transparent, CafeColors.Fg, CafeColors.Border)
        CafeButtonVariant.OnDark -> Triple(CafeColors.AccentFg, CafeColors.Accent, Color.White.copy(alpha = 0.35f))
    }

    // 颜色动画：按下时 +6% brightness（用 overlay 模拟）
    val pressedOverlay = if (isPressed && enabled) Color.White.copy(alpha = 0.06f) else Color.Transparent
    val animatedContainer by animateColorAsState(
        targetValue = containerColor,
        animationSpec = tween(CafeMotion.hover),
        label = "container"
    )
    val disabledAlpha = if (enabled) 1f else 0.3f

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .height(if (fullWidth) 48.dp else 44.dp)
            .clip(RoundedCornerShape(CafeRadius.rBtn))
            .background(animatedContainer.copy(alpha = disabledAlpha))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(CafeRadius.rBtn)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled && !loading,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = CafeSpacing.btnPadH, vertical = CafeSpacing.btnPadV),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
            } else if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text,
                style = CafeType.bodySmall.copy(
                    color = contentColor.copy(alpha = disabledAlpha)
                )
            )
        }
        // pressed overlay（叠加在内容上）
        if (isPressed && enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(CafeRadius.rBtn))
                    .background(pressedOverlay)
            )
        }
    }
}

/**
 * 便捷：带 AutoAwesome 魔法图标的 Primary 按钮（用于 AI 出题等场景）。
 *
 * 视觉约束：此按钮常用于 [com.yiqiu.readingquiz.ui.components.CafeCtaBanner] 等
 * 渐变背景容器内（brown → green），或 [CafeCard] 白色背景内：
 * - onBanner=true  → 用 OnDark 变体（白底 + accent 文字），避免与棕色渐变融为一体
 * - onBanner=false → 用 Primary 变体（accent 填充 + 白字），与卡片白色背景形成强对比
 */
@Composable
fun CafeButtonAi(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    onBanner: Boolean = false
) {
    CafeButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        variant = if (onBanner) CafeButtonVariant.OnDark else CafeButtonVariant.Primary,
        enabled = enabled,
        loading = loading,
        leadingIcon = Icons.Rounded.AutoAwesome
    )
}