package com.yiqiu.readingquiz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ui.theme.CafeColors

/**
 * 进度圆点序列（不依赖颜色，使用图标区分对错状态，spec 附录 A.1 允许清单）。
 */
@Composable
fun CafeProgressDots(
    total: Int,
    current: Int,
    answeredCorrect: Set<Int>,
    answeredWrong: Set<Int>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 0 until total) {
            val color = when {
                i == current -> CafeColors.Accent
                i in answeredCorrect -> CafeColors.Correct
                i in answeredWrong -> CafeColors.Wrong
                else -> CafeColors.Neutral
            }
            val icon: androidx.compose.ui.graphics.vector.ImageVector = when {
                i in answeredCorrect -> Icons.Filled.CheckCircle
                i in answeredWrong -> Icons.Outlined.Cancel
                else -> Icons.Filled.RadioButtonUnchecked
            }
            Surface(
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
                color = androidx.compose.ui.graphics.Color.Transparent
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}