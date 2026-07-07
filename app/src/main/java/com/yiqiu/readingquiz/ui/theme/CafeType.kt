package com.yiqiu.readingquiz.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * cafe-ui 排版令牌（caffe-ui §2 type scale）。
 *
 * 显式声明 : TextStyle 类型，避免与 FontFamily 同名导致的递归推断问题（spec 附录 A.3）。
 * 全应用排版唯一真源。任何 UI 组件必须只引用本 object 的 TextStyle，
 * 禁止自定义 .sp 值或 FontWeight。
 */
object CafeType {
    private val Sans: FontFamily = FontFamily.SansSerif
    private val Mono: FontFamily = FontFamily.Monospace

    /** 大标题（Hero）：首页 / 引导页，44sp / 700 / -0.025em */
    val displayHero: TextStyle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 45.sp,
        letterSpacing = (-0.025).em
    )

    /** 区域标题（Section）：章节标题 / 大数字 / 卡片 H1，32sp / 700 / -0.02em */
    val displaySection: TextStyle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.02).em
    )

    /** 卡片标题（Card H3）：feature card 标题，18sp / 700 / -0.01em */
    val cardTitle: TextStyle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.01).em
    )

    /** 正文（Body lede）：长段说明 / 题干，16sp / 400 / 1.6 */
    val body: TextStyle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp
    )

    /** 紧凑正文（Body）：列表项 / 表单，15sp / 400 / 1.55 */
    val bodyCompact: TextStyle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    )

    /** 小字（Small）：按钮 / 导航 / 标签，14sp / 500 / 1.55 */
    val bodySmall: TextStyle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    /** 微字（Small）：14sp / 400 */
    val bodyXSmall: TextStyle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )

    /** Eyebrow / 状态标签：12sp / 500 mono / +0.08em / UPPERCASE */
    val eyebrow: TextStyle = TextStyle(
        fontFamily = Mono,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.08.em
    )

    /** KPI 数值：24sp / 700 / -0.01em */
    val kpi: TextStyle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.01).em
    )

    /** KPI 增量 / 元数据：11sp / 400 mono */
    val meta: TextStyle = TextStyle(
        fontFamily = Mono,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )

    /** 价格 / 大数字：44sp / 700 / -0.02em */
    val price: TextStyle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.02).em
    )

    /** Mono 正文：用于解析 / 解析代码块，14sp mono */
    val monoBody: TextStyle = TextStyle(
        fontFamily = Mono,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    /**
     * Material 3 Typography 映射：把 M3 默认 title/body/label 全部指向 cafe-ui token。
     * 这样任何用 `MaterialTheme.typography.titleLarge` 的地方都会自动用我们的 token。
     */
    val Typography: Typography = Typography(
        displayLarge = displayHero,
        displayMedium = displaySection,
        displaySmall = displaySection,
        headlineLarge = displaySection,
        headlineMedium = cardTitle,
        headlineSmall = cardTitle,
        titleLarge = cardTitle,
        titleMedium = bodySmall,
        titleSmall = bodySmall,
        bodyLarge = body,
        bodyMedium = bodyCompact,
        bodySmall = bodyXSmall,
        labelLarge = bodySmall,
        labelMedium = eyebrow,
        labelSmall = meta
    )
}