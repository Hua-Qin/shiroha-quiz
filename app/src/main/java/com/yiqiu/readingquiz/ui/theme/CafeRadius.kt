package com.yiqiu.readingquiz.ui.theme

import androidx.compose.ui.unit.dp

/**
 * cafe-ui 圆角令牌（caffe-ui §4 radius）。
 *
 * 全应用圆角唯一真源。任何 UI 组件必须只引用本 object 的字段，
 * 禁止硬编码 .dp 值（用于圆角时）。
 */
object CafeRadius {
    val rSm = 7.dp        // 品牌 mark
    val rMd = 8.dp        // 按钮 / 侧链
    val rBtn = 10.dp      // 主按钮
    val rCard = 12.dp     // 卡片 / 图表卡
    val rCardLg = 14.dp   // feature 卡片
    val rPanel = 16.dp    // 价目卡
    val rFrame = 18.dp    // preview 框架
    val rHero = 24.dp     // CTA banner
    val rPill = 9999.dp   // 完全圆角
}