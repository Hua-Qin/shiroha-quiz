package com.yiqiu.readingquiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * Cafe-ui KPI Card（caffe-ui §7 KPI card spec）。
 *
 * - surface 填充 + 1px border + 10dp 圆角
 * - 14×16dp 内边距
 * - eyebrow label（12sp mono muted）
 * - 24sp / 700 value
 * - 11sp mono accent delta
 */
@Composable
fun CafeKpiCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    delta: String? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CafeRadius.rBtn))
            .background(CafeColors.Surface)
            .border(1.dp, CafeColors.Border, RoundedCornerShape(CafeRadius.rBtn))
            .padding(horizontal = CafeSpacing.kpiPadH, vertical = CafeSpacing.kpiPadV),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = CafeType.eyebrow,
            color = CafeColors.Muted
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = value,
                style = CafeType.kpi,
                color = CafeColors.Fg
            )
            if (delta != null) {
                Text(
                    text = delta,
                    style = CafeType.meta,
                    color = CafeColors.Accent,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}