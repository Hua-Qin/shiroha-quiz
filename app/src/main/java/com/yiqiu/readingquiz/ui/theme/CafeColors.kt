package com.yiqiu.readingquiz.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * cafe-ui 色板（必须先于组件使用）。
 * 主色：咖啡棕 #5D4432；点缀色：清新绿 #16A34A；底色：暖白 #F9F7F5。
 */
object CafeColors {
    val Bg: Color = Color(0xFFF9F7F5)
    val Surface: Color = Color(0xFFFFFFFF)
    val Fg: Color = Color(0xFF0A0A0A)
    val Accent: Color = Color(0xFF5D4432)
    val AccentFg: Color = Color(0xFFFFFFFF)
    val Accent2: Color = Color(0xFF16A34A)
    val Accent2Fg: Color = Color(0xFFFFFFFF)
    val Muted: Color = Color(0xFF666666)
    val Border: Color = Color(0xFFE6E6E6)
    val NavScrim: Color = Color(0x80000000)

    // 答题判题反馈用色（不依赖颜色之外的可达性）
    val Correct: Color = Color(0xFF16A34A)
    val Wrong: Color = Color(0xFFB91C1C)
    val Neutral: Color = Color(0xFF94A3B8)
}