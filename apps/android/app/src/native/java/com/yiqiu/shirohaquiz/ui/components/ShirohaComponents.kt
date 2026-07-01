package com.yiqiu.shirohaquiz.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.EditorialFigureStyle
import com.yiqiu.shirohaquiz.ui.theme.EditorialKickerStyle
import com.yiqiu.shirohaquiz.ui.theme.ShirohaMotion
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.ShirohaTypography
import com.yiqiu.shirohaquiz.ui.text.LatexDisplayFormatter


@Composable
fun ShirohaDangerConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "确认",
    dismissText: String = "取消",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissText) }
        }
    )
}

@Composable
private fun Modifier.cardRiseMotion(enabled: Boolean): Modifier {
    if (!enabled) return this

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 7f,
        animationSpec = tween(durationMillis = 180),
        label = "shiroha_card_rise_y"
    )
    val alphaValue by animateFloatAsState(
        targetValue = if (visible) 1f else 0.92f,
        animationSpec = tween(durationMillis = 160),
        label = "shiroha_card_rise_alpha"
    )

    return graphicsLayer {
        translationY = offsetY
        alpha = alphaValue
    }
}

@Composable
fun ShirohaHeader(
    kicker: String,
    title: String,
    subtitle: String,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) {
    val kickerStyle = if (scale == 1f) {
        EditorialKickerStyle
    } else {
        EditorialKickerStyle.copy(
            fontSize = (EditorialKickerStyle.fontSize.value * scale).sp,
            lineHeight = (EditorialKickerStyle.lineHeight.value * scale).sp
        )
    }
    val titleStyle = if (scale == 1f) {
        ShirohaTypography.displaySmall
    } else {
        ShirohaTypography.displaySmall.copy(
            fontSize = (ShirohaTypography.displaySmall.fontSize.value * scale).sp,
            lineHeight = (ShirohaTypography.displaySmall.lineHeight.value * scale).sp
        )
    }
    val subtitleStyle = if (scale == 1f) {
        MaterialTheme.typography.bodyLarge
    } else {
        MaterialTheme.typography.bodyLarge.copy(
            fontSize = (MaterialTheme.typography.bodyLarge.fontSize.value * scale).sp,
            lineHeight = (MaterialTheme.typography.bodyLarge.lineHeight.value * scale).sp
        )
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm)
    ) {
        if (kicker.isNotBlank()) {
            Text(
                text = kicker.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                style = kickerStyle
            )
        }
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = titleStyle,
                color = ShirohaColors.TextPrimary
            )
        }
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = subtitleStyle,
                color = ShirohaColors.TextSecondary
            )
        }
    }
}

/* ====== 编辑式组件库(Warm Editorial Magazine) ====== */

/**
 * 编辑式大数字组件(杂志封面级数据呈现,响应式字号)
 * 衬线粗体超大数字 + 小标签 + 发丝下划线
 *
 * @param scale 字号缩放因子,默认 1f。
 *   - 移动端推荐 0.65f(通过 `editorialScaleFor(ScreenClass.COMPACT)` 取得)
 *   - 中等屏 0.85f
 *   - 桌面 1.0f
 */
@Composable
fun EditorialFigure(
    value: String,
    label: String,
    unit: String = "",
    scale: Float = 1f,
    modifier: Modifier = Modifier
) {
    val figureStyle = if (scale == 1f) {
        EditorialFigureStyle
    } else {
        EditorialFigureStyle.copy(
            fontSize = (EditorialFigureStyle.fontSize.value * scale).sp,
            lineHeight = (EditorialFigureStyle.lineHeight.value * scale).sp
        )
    }
    val unitStyle = MaterialTheme.typography.titleMedium
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = figureStyle,
                color = ShirohaColors.TextPrimary
            )
            if (unit.isNotBlank()) {
                Text(
                    text = unit,
                    style = unitStyle,
                    color = ShirohaColors.TextSecondary,
                    modifier = Modifier.padding(bottom = (8 * scale).coerceAtLeast(4f).dp)
                )
            }
        }
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)) {
            drawLine(
                color = ShirohaColors.LineStrong,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = ShirohaColors.TextSecondary
        )
    }
}

/**
 * 编辑式分隔线(发丝线 + 可选居中标签)
 */
@Composable
fun EditorialDivider(
    label: String? = null,
    modifier: Modifier = Modifier
) {
    if (label.isNullOrBlank()) {
        Canvas(modifier = modifier
            .fillMaxWidth()
            .height(1.dp)) {
            drawLine(
                color = ShirohaColors.LineStrong,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Canvas(modifier = Modifier
                .weight(1f)
                .height(1.dp)) {
                drawLine(
                    color = ShirohaColors.LineStrong,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            Text(
                text = label.uppercase(),
                style = EditorialKickerStyle,
                color = ShirohaColors.TextSecondary
            )
            Canvas(modifier = Modifier
                .weight(1f)
                .height(1.dp)) {
                drawLine(
                    color = ShirohaColors.LineStrong,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}

/**
 * 编辑式区块(发丝分隔线 + 可选 kicker/title + 内容)
 * 替代重卡片容器堆叠,营造杂志式版面呼吸感
 */
@Composable
fun EditorialSection(
    title: String? = null,
    kicker: String? = null,
    scale: Float = 1f,
    content: @Composable ColumnScope.() -> Unit
) {
    val kickerStyle = if (scale == 1f) {
        EditorialKickerStyle
    } else {
        EditorialKickerStyle.copy(
            fontSize = (EditorialKickerStyle.fontSize.value * scale).sp,
            lineHeight = (EditorialKickerStyle.lineHeight.value * scale).sp
        )
    }
    val titleStyle = if (scale == 1f) {
        ShirohaTypography.headlineSmall
    } else {
        ShirohaTypography.headlineSmall.copy(
            fontSize = (ShirohaTypography.headlineSmall.fontSize.value * scale).sp,
            lineHeight = (ShirohaTypography.headlineSmall.lineHeight.value * scale).sp
        )
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        if (!kicker.isNullOrBlank() || !title.isNullOrBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!kicker.isNullOrBlank()) {
                    Text(
                        text = kicker.uppercase(),
                        style = kickerStyle,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (!title.isNullOrBlank()) {
                    Text(
                        text = title,
                        style = titleStyle,
                        color = ShirohaColors.TextPrimary
                    )
                }
            }
            EditorialDivider()
        }
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    contentPadding: Dp = ShirohaSpacing.Xl,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .cardRiseMotion(animated && QuizRepository.cardAnimationEnabled),
        shape = RoundedCornerShape(ShirohaRadius.Lg),
        colors = CardDefaults.cardColors(containerColor = ShirohaColors.CardSoft),
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}


@Composable
fun StatusChip(
    text: String,
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.defaultMinSize(minHeight = ShirohaDimens.StatusChipMinHeight),
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = if (selected) ShirohaColors.BrandPrimarySoft else ShirohaColors.CardMuted,
        border = BorderStroke(
            ShirohaDimens.Hairline,
            if (selected) ShirohaColors.LineSelected else ShirohaColors.LineSoft
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = ShirohaDimens.StatusChipHorizontalPadding, vertical = ShirohaDimens.StatusChipVerticalPadding),
            color = if (selected) MaterialTheme.colorScheme.primary else ShirohaColors.TextSecondary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun ActionPillButton(
    icon: ImageVector,
    text: String,
    primary: Boolean = true,
    modifier: Modifier = Modifier,
    fillWidthContent: Boolean = false,
    enabled: Boolean = true,
    textMaxLines: Int = 1,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(ShirohaRadius.Pill)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = ShirohaDimens.ActionButtonMinHeight)
            .clip(shape)
            .alpha(if (enabled) 1f else ShirohaDimens.DisabledAlpha)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        shape = shape,
        color = if (primary) MaterialTheme.colorScheme.primary else ShirohaColors.CardWhite86,
        border = BorderStroke(
            ShirohaDimens.Hairline,
            if (primary) MaterialTheme.colorScheme.primary else ShirohaColors.LineStrong
        )
    ) {
        Row(
            modifier = (if (fillWidthContent) Modifier.fillMaxSize() else Modifier.defaultMinSize(minHeight = ShirohaDimens.ActionButtonMinHeight))
                .padding(horizontal = if (fillWidthContent) ShirohaDimens.ActionButtonEqualHorizontalPadding else ShirohaDimens.ActionButtonHorizontalPadding, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (fillWidthContent) Arrangement.Center else Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(ShirohaDimens.ActionButtonIconSize),
                tint = if (primary) ShirohaColors.TextOnBrand else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(if (fillWidthContent) ShirohaDimens.ActionButtonEqualIconTextGap else ShirohaDimens.ActionButtonIconTextGap))
            Text(
                text = text,
                color = if (primary) ShirohaColors.TextOnBrand else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge,
                maxLines = textMaxLines.coerceAtLeast(1),
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}



@Composable
fun QuizSessionExitIconButton(
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = CircleShape
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .size(46.dp)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = shape,
        color = ShirohaColors.CardWhite86,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineStrong)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MetricGlassCard(
    label: String,
    value: String,
    desc: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) modifier.shirohaNoRippleClickable(onClick = onClick) else modifier
    GlassCard(modifier = cardModifier) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(ShirohaSpacing.Xs))
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ShortcutGlassCard(
    title: String,
    icon: ImageVector,
    desc: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) modifier.shirohaNoRippleClickable(onClick = onClick) else modifier
    GlassCard(modifier = cardModifier) {
        Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

enum class QuizOptionResultStyle {
    Neutral,
    Correct,
    Wrong
}

@Composable
fun QuizOptionCard(
    label: String,
    text: String,
    selected: Boolean,
    resultStyle: QuizOptionResultStyle = QuizOptionResultStyle.Neutral,
    compact: Boolean = QuizRepository.compactOptionsEnabled,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(ShirohaRadius.Lg)
    val interactionSource = remember { MutableInteractionSource() }
    val isCorrect = resultStyle == QuizOptionResultStyle.Correct
    val isWrong = resultStyle == QuizOptionResultStyle.Wrong
    val containerColor = when (resultStyle) {
        QuizOptionResultStyle.Correct -> ShirohaColors.StateSuccessSoft.copy(alpha = if (ShirohaColors.isDarkMode) 0.9f else 0.72f)
        QuizOptionResultStyle.Wrong -> ShirohaColors.StateDangerSoft.copy(alpha = if (ShirohaColors.isDarkMode) 0.9f else 0.72f)
        QuizOptionResultStyle.Neutral -> if (selected) ShirohaColors.BrandPrimarySoft else ShirohaColors.CardWhite84
    }
    val borderColor = when (resultStyle) {
        QuizOptionResultStyle.Correct -> ShirohaColors.StateSuccess.copy(alpha = 0.45f)
        QuizOptionResultStyle.Wrong -> ShirohaColors.StateDanger.copy(alpha = 0.45f)
        QuizOptionResultStyle.Neutral -> if (selected) ShirohaColors.LineSelected else ShirohaColors.LineSoft
    }
    val labelColor = when (resultStyle) {
        QuizOptionResultStyle.Correct -> ShirohaColors.StateSuccess
        QuizOptionResultStyle.Wrong -> ShirohaColors.StateDanger
        QuizOptionResultStyle.Neutral -> if (selected) MaterialTheme.colorScheme.primary else ShirohaColors.OptionLabelIdle
    }
    val labelTextColor = when {
        isCorrect || isWrong -> ShirohaColors.TextOnBrand
        selected -> ShirohaColors.TextOnBrand
        else -> MaterialTheme.colorScheme.onSurface
    }
    val optionFontSize = QuizRepository.optionFontSizeSp().sp
    val optionLineHeight = QuizRepository.optionLineHeightSp().sp
    val displayText = LatexDisplayFormatter.format(text)

    if (compact) {
        val compactContentColor = when {
            isCorrect -> ShirohaColors.StateSuccess
            isWrong -> ShirohaColors.StateDanger
            selected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 42.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$label.",
                color = compactContentColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = optionFontSize,
                    lineHeight = optionLineHeight
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = displayText,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = optionFontSize,
                    lineHeight = optionLineHeight
                ),
                fontWeight = if (selected || resultStyle != QuizOptionResultStyle.Neutral) FontWeight.SemiBold else FontWeight.Normal,
                color = compactContentColor
            )
        }
        return
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = shape,
        color = containerColor,
        border = BorderStroke(ShirohaDimens.Hairline, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ShirohaDimens.OptionCardHorizontalPadding, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = labelColor,
                modifier = Modifier.size(ShirohaDimens.OptionLabelSize)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        color = labelTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(ShirohaDimens.OptionLabelTextGap))
            Text(
                text = displayText,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = optionFontSize, lineHeight = optionLineHeight),
                fontWeight = if (selected || resultStyle != QuizOptionResultStyle.Neutral) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (selected && resultStyle == QuizOptionResultStyle.Neutral) {
                Spacer(Modifier.width(8.dp))
                StatusChip("已选", selected = true)
            }
        }
    }
}

@Composable
fun UploadPanel(
    title: String,
    desc: String,
    icon: ImageVector
) {
    Surface(
        shape = RoundedCornerShape(ShirohaRadius.Lg),
        color = ShirohaColors.CardSoft,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ShirohaSpacing.Xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NoticeCard(
    text: String,
    warning: Boolean = true
) {
    val background = if (warning) ShirohaColors.StateWarningSoft else ShirohaColors.CardWhite78
    val foreground = if (warning) ShirohaColors.TextWarning else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = background
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = foreground
        )
    }
}

@Composable
fun IllustrationHeroCard(
    title: String,
    subtitle: String,
    @DrawableRes imageRes: Int,
    modifier: Modifier = Modifier,
    imageSize: Dp = ShirohaDimens.HeroImageSize,
    scale: Float = 1f,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val showIllustration = QuizRepository.shirohaModeEnabled
    val heroTitleStyle = if (scale == 1f) {
        ShirohaTypography.headlineSmall
    } else {
        ShirohaTypography.headlineSmall.copy(
            fontSize = (ShirohaTypography.headlineSmall.fontSize.value * scale).sp,
            lineHeight = (ShirohaTypography.headlineSmall.lineHeight.value * scale).sp
        )
    }
    val heroSubtitleStyle = if (scale == 1f) {
        MaterialTheme.typography.bodyLarge
    } else {
        MaterialTheme.typography.bodyLarge.copy(
            fontSize = (MaterialTheme.typography.bodyLarge.fontSize.value * scale).sp,
            lineHeight = (MaterialTheme.typography.bodyLarge.lineHeight.value * scale).sp
        )
    }

    GlassCard(modifier = modifier) {
        Row(
            modifier = if (showIllustration) {
                Modifier.fillMaxWidth()
            } else {
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            },
            horizontalArrangement = if (showIllustration) {
                Arrangement.spacedBy(ShirohaSpacing.Lg)
            } else {
                Arrangement.Center
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = if (showIllustration) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                horizontalAlignment = if (showIllustration) Alignment.Start else Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm)
            ) {
                Text(
                    text = title,
                    style = heroTitleStyle,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = if (showIllustration) TextAlign.Start else TextAlign.Center,
                    maxLines = if (showIllustration) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = heroSubtitleStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = if (showIllustration) TextAlign.Start else TextAlign.Center,
                        maxLines = if (showIllustration) 2 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                content()
            }
            if (showIllustration) {
                val density = LocalDensity.current
                val floatDistancePx = with(density) { ShirohaMotion.HeroFloatDistance.toPx() }
                val heroFloat = rememberInfiniteTransition(label = "hero_illustration_float")
                val imageOffsetY by heroFloat.animateFloat(
                    initialValue = -floatDistancePx,
                    targetValue = floatDistancePx,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = ShirohaMotion.HeroFloatMillis),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "hero_illustration_float_y"
                )
                Box(
                    modifier = Modifier.size(imageSize + ShirohaDimens.HeroImageFrameExtra),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(imageSize + ShirohaDimens.HeroImageFrameExtra)
                            .graphicsLayer { translationY = imageOffsetY }
                            .alpha(ShirohaDimens.HeroImageAlpha),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateIllustration(
    title: String,
    message: String,
    @DrawableRes imageRes: Int = R.drawable.illus_empty_state_webp,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
        ) {
            if (QuizRepository.shirohaModeEnabled) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(ShirohaDimens.EmptyStateImageSize)
                        .alpha(0.9f),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            action?.invoke()
        }
    }
}

@Composable
fun LoadingIllustration(
    text: String,
    @DrawableRes imageRes: Int = R.drawable.illus_loading_state_webp
) {
    val transition = rememberInfiniteTransition(label = "loading_illus")
    val scale = transition.animateFloat(
        initialValue = ShirohaMotion.LoadingScaleMin,
        targetValue = ShirohaMotion.LoadingScaleMax,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = ShirohaMotion.LoadingScaleMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_scale"
    )

    GlassCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
        ) {
            if (QuizRepository.shirohaModeEnabled) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(ShirohaDimens.LoadingImageSize)
                        .scale(scale.value)
                        .alpha(0.9f),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
