package com.yiqiu.readingquiz.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * cafe-ui 品牌识别资源（caffe-ui §1 brand gradient）。
 *
 * 唯一品牌渐变：135° accent (#5D4432) → accent2 (#16A34A)。
 * 用于：CTA banner、icon tile、品牌 logo、feature card 渐变标题。
 */
object CafeBrand {
    /**
     * 主品牌渐变（135°，accent → accent2）。
     * 角度定义：从屏幕左上 (0,0) 到右下 (1,1)，对角 135°。
     */
    val brandGradient: Brush = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to CafeColors.Accent,
            1.0f to CafeColors.Accent2
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /** 深色面上的反色 surface（fg 背景上用 bg 色文字） */
    val inverseSurface: Color = CafeColors.Bg
    val inverseOnSurface: Color = CafeColors.Fg
}