package com.yiqiu.readingquiz.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * cafe-ui 色板（caffe-ui §1 color tokens）。
 * 主色：咖啡棕 #5D4432；点缀色：清新绿 #16A34A；底色：暖白 #F9F7F5。
 *
 * 全应用唯一颜色真源。任何 UI 组件必须只引用本 object 的字段，
 * 禁止硬编码 Color(0xFF...) 或 "#RRGGBB"。
 */
object CafeColors {
    // 基础色
    val Bg: Color = Color(0xFFF9F7F5)        // #F9F7F5 暖白
    val Surface: Color = Color(0xFFFFFFFF)   // 卡片表面（白色，与 Bg 略有区分）
    val Fg: Color = Color(0xFF0A0A0A)        // 主前景文字
    val Accent: Color = Color(0xFF5D4432)    // #5D4432 咖啡棕
    val AccentFg: Color = Color(0xFFFFFFFF)  // accent 上的文字
    val Accent2: Color = Color(0xFF16A34A)   // #16A34A 清新绿
    val Accent2Fg: Color = Color(0xFFFFFFFF) // accent2 上的文字
    val Muted: Color = Color(0xFF666666)     // 次要文字
    val Border: Color = Color(0xFFE6E6E6)    // 细边

    /** 导航条半透明白底（70% 透明度） */
    val NavScrim: Color = Color(0xB3FFFFFF)  // = Color.White.copy(alpha = 0.7f)

    // 笔记浮窗纸张色（NotePadWindow 专用，保持 cafe-ui token 体系单一真源）
    val PaperBg: Color = Color(0xFFFFF8E7)       // 浅米黄纸张
    val PaperBorder: Color = Color(0xFFE6D9B8)   // 纸张淡边框
    val PaperEditor: Color = Color(0xFFFFFBEF)   // 编辑区略浅

    // 答题判题反馈用色（不依赖颜色之外的可达性）
    val Correct: Color = Accent2              // 答对 = 绿
    val Wrong: Color = Color(0xFFB91C1C)      // 答错 = 红
    val Neutral: Color = Color(0xFF94A3B8)    // 未作答 = 灰
}