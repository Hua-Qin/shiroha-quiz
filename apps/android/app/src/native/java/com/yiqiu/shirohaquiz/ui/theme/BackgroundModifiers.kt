package com.yiqiu.shirohaquiz.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * 暖色编辑杂志风背景。
 * 暖色启用时:暖纸张径向渐变(#FEF3C7 → #FBF6EC → #FBF1DE)。
 * 冷色/深色时:纯色 `BgApp`。
 */
fun Modifier.shirohaEditorialBackground(): Modifier {
    return if (ShirohaColors.warmThemeEnabled) {
        val gradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFEF3C7),
                Color(0xFFFBF6EC),
                Color(0xFFFBF1DE)
            )
        )
        this.background(gradient)
    } else {
        this.background(ShirohaColors.BgApp)
    }
}
