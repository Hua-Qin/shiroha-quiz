package com.yiqiu.readingquiz.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * cafe-ui 字体令牌。
 * 显式声明 : TextStyle 类型，避免与 FontFamily 同名导致的递归推断问题（spec 附录 A.3）。
 */
object CafeType {
    private val Sans: FontFamily = FontFamily.SansSerif
    private val MonoStyleFont: FontFamily = FontFamily.Monospace

    val Title: TextStyle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    )

    val Heading: TextStyle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    )

    val Body: TextStyle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )

    val Caption: TextStyle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )

    val MonoBody: TextStyle = TextStyle(
        fontFamily = MonoStyleFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    val Typography: Typography = Typography(
        titleLarge = Title,
        titleMedium = Heading,
        bodyLarge = Body,
        bodyMedium = Body,
        bodySmall = Caption,
        labelLarge = Caption
    )
}