package com.yiqiu.readingquiz.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius

@Composable
fun CafeCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(26.dp),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = CafeColors.Border,
                shape = RoundedCornerShape(CafeRadius.CardLg)
            ),
        shape = RoundedCornerShape(CafeRadius.CardLg),
        colors = CardDefaults.cardColors(containerColor = CafeColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}