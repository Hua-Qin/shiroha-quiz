package com.yiqiu.readingquiz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ui.theme.CafeColors

/**
 * Cafe-ui 进度圆点序列（caffe-ui §7 Progress spec）。
 *
 * - 当前点：accent 色
 * - 答对：correct 色 + CheckCircle
 * - 答错：wrong 色 + Cancel
 * - 未作答：neutral 色 + RadioButtonUnchecked
 * - 间距 6dp
 *
 * 不依赖颜色以外的可达性（图标区分状态）。
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
            val icon = when {
                i in answeredCorrect -> Icons.Filled.CheckCircle
                i in answeredWrong -> Icons.Outlined.Cancel
                else -> Icons.Filled.RadioButtonUnchecked
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}