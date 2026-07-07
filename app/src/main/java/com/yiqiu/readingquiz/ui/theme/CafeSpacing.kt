package com.yiqiu.readingquiz.ui.theme

import androidx.compose.ui.unit.dp

/**
 * cafe-ui 间距令牌（caffe-ui §3 spacing & layout）。
 *
 * 全应用间距唯一真源。任何 UI 组件必须只引用本 object 的字段，
 * 禁止硬编码 .dp 值（用于间距 / padding / gap 时）。
 */
object CafeSpacing {
    // 基础 gap
    val xs = 12.dp        // 按钮行 gap
    val sm = 14.dp        // KPI 行 gap
    val md = 18.dp        // 卡片网格 gap
    val lg = 32.dp        // 导航链接 cluster gap

    // 容器 / 区域
    val containerPad = 28.dp  // 容器水平内边距
    val sectionY = 96.dp      // 区域竖直内边距
    val cardPadLg = 28.dp     // 大卡片（price）内边距
    val cardPad = 26.dp       // feature / quote 卡片内边距
    val cardPadSm = 18.dp     // 小卡片（chart）内边距

    // 按钮
    val btnPadH = 22.dp       // 按钮水平 padding
    val btnPadV = 13.dp       // 按钮竖直 padding

    // 列表 / 行
    val listRowPadH = 16.dp
    val listRowPadV = 12.dp

    // KPI / Icon
    val kpiPadH = 16.dp
    val kpiPadV = 14.dp
    val iconTileSize = 36.dp
}