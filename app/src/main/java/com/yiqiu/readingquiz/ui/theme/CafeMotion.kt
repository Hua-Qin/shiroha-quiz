package com.yiqiu.readingquiz.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

/**
 * cafe-ui 动效令牌（caffe-ui §6 motion）。
 *
 * 全应用动效唯一真源。任何 UI 组件的动画 duration / easing / slide 距离
 * 必须只引用本 object。
 */
object CafeMotion {
    /** hover / 按下反馈 120-180ms */
    const val hover: Int = 150
    /** surface tint 过渡 150ms */
    const val filterSoft: Int = 150
    /** 页面切换 fade + slide 250ms */
    const val pageEnter: Int = 250

    /** 标准 ease-out */
    val easeOut: Easing = FastOutSlowInEasing
    /** 线性（用于进度条 / loading） */
    val linear: Easing = LinearEasing

    /** 页面切换 - 水平 slide 距离（dp 转 px 由调用方处理） */
    val pageSlideDp: Int = 8
}