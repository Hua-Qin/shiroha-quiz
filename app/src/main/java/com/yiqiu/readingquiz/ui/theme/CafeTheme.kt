package com.yiqiu.readingquiz.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun CafeTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = CafeColors.Accent,
        onPrimary = CafeColors.AccentFg,
        secondary = CafeColors.Accent2,
        onSecondary = CafeColors.Accent2Fg,
        background = CafeColors.Bg,
        onBackground = CafeColors.Fg,
        surface = CafeColors.Surface,
        onSurface = CafeColors.Fg,
        outline = CafeColors.Border
    )

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(CafeRadius.Sm),
        small = RoundedCornerShape(CafeRadius.Md),
        medium = RoundedCornerShape(CafeRadius.Card),
        large = RoundedCornerShape(CafeRadius.CardLg),
        extraLarge = RoundedCornerShape(CafeRadius.Panel)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CafeType.Typography,
        shapes = shapes,
        content = content
    )
}