package com.yiqiu.shirohaquiz.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 屏幕尺寸分档(响应式字号的输入)
 *
 *  - COMPACT(< 600dp):手机竖屏,默认
 *  - MEDIUM(600-839dp):手机横屏 / 小平板
 *  - EXPANDED(>= 840dp):大平板 / 桌面
 */
enum class ScreenClass { COMPACT, MEDIUM, EXPANDED }

fun screenClassFor(maxWidth: Dp): ScreenClass = when {
    maxWidth >= 840.dp -> ScreenClass.EXPANDED
    maxWidth >= 600.dp -> ScreenClass.MEDIUM
    else -> ScreenClass.COMPACT
}

/**
 * 编辑式字号的响应式缩放因子。
 * 移动端用更小的字号,桌面端放大,确保两端可读性 + 视觉平衡。
 *
 *  - COMPACT:  0.65  (移动端,缩 35%)
 *  - MEDIUM:   0.85  (中等)
 *  - EXPANDED: 1.00  (桌面,基准)
 */
fun editorialScaleFor(screenClass: ScreenClass): Float = when (screenClass) {
    ScreenClass.COMPACT -> 0.65f
    ScreenClass.MEDIUM -> 0.85f
    ScreenClass.EXPANDED -> 1.0f
}

/** 通用(非编辑式)字号的响应式缩放:更温和 */
fun uiScaleFor(screenClass: ScreenClass): Float = when (screenClass) {
    ScreenClass.COMPACT -> 0.85f
    ScreenClass.MEDIUM -> 0.95f
    ScreenClass.EXPANDED -> 1.0f
}

private fun Int.sp(s: Float): TextUnit = (this.toFloat() * s).sp

/**
 * 编辑式大数字(杂志封面级)响应式字号:
 *  COMPACT 36sp  / MEDIUM 48sp / EXPANDED 56sp
 */
fun editorialFigureStyleFor(screenClass: ScreenClass): TextStyle {
    val s = editorialScaleFor(screenClass)
    return TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp(s),
        lineHeight = 60.sp(s)
    )
}

/** 编辑式 kicker 响应式字号 */
fun editorialKickerStyleFor(screenClass: ScreenClass): TextStyle {
    val s = editorialScaleFor(screenClass)
    return TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp(s),
        lineHeight = 16.sp(s),
        letterSpacing = 1.6.sp
    )
}

/** Hero 衬线大标题响应式字号 */
fun heroTitleStyleFor(screenClass: ScreenClass): TextStyle {
    val s = editorialScaleFor(screenClass)
    return TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp(s),
        lineHeight = 40.sp(s)
    )
}

/** Section 衬线中标题响应式 */
fun sectionTitleStyleFor(screenClass: ScreenClass): TextStyle {
    val s = editorialScaleFor(screenClass)
    return TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp(s),
        lineHeight = 32.sp(s)
    )
}

// 兼容旧 API:保留常量,但使用桌面尺寸作为默认(响应式函数优先)
val EditorialFigureStyle: TextStyle = editorialFigureStyleFor(ScreenClass.EXPANDED)
val EditorialKickerStyle: TextStyle = editorialKickerStyleFor(ScreenClass.EXPANDED)
