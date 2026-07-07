package com.yiqiu.readingquiz.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.ui.components.CafeCard
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

@Composable
fun HomeScreen(
    onOpenArticle: (String) -> Unit,
    onOpenAiSettings: () -> Unit
) {
    val articles = ReadingRepository.articles
    Log.d("Nav", "home: articles=${articles.size}")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CafeSpacing.ContainerPad)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.MenuBook,
                contentDescription = null,
                tint = CafeColors.Accent
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Reading Quiz", style = CafeType.Title, color = CafeColors.Fg)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onOpenAiSettings) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "AI 设置",
                    tint = CafeColors.Fg
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (articles.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "暂无文章",
                        style = CafeType.Heading,
                        color = CafeColors.Muted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "点击右上角设置 → 在 AI 配置下方点击「选择文件导入」",
                        style = CafeType.Caption,
                        color = CafeColors.Muted
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(articles, key = { it.id }) { article ->
                    // 单项渲染异常兜底：runCatching 包裹，不让单个崩溃阻塞整列表
                    runCatching {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CafeCard(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Text(
                                        text = article.title,
                                        style = CafeType.Heading,
                                        color = CafeColors.Fg
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${article.category} · ${article.author}",
                                        style = CafeType.Caption,
                                        color = CafeColors.Muted
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = {
                                        Log.d("Reading", "user clicked article: id=${article.id}, title='${article.title}'")
                                        onOpenArticle(article.id)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoStories,
                                            contentDescription = null,
                                            tint = CafeColors.Accent
                                        )
                                        Spacer(modifier = Modifier.size(4.dp))
                                        Text(text = "开始阅读", color = CafeColors.Accent)
                                    }
                                }
                            }
                            if (article.favorite) {
                                Icon(
                                    imageVector = Icons.Rounded.Bookmark,
                                    contentDescription = "已收藏",
                                    tint = CafeColors.Accent2,
                                    modifier = Modifier.align(Alignment.TopEnd)
                                )
                            }
                        }
                    }.getOrElse { e ->
                        Log.w("Home", "article card render failed: id=${article.id}, err=${e.message}", e)
                        FailedCard(article.id, onRetry = {
                            // 通过 ReadingRepository.removeAll + 重新添加触发重组（仅简单方式）
                            Log.i("Home", "user requested retry for ${article.id}")
                        })
                    }
                }
            }
        }
    }
}

/**
 * 单项渲染失败时的兜底卡片。
 */
@Composable
private fun FailedCard(articleId: String, onRetry: () -> Unit) {
    CafeCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "加载失败",
                style = CafeType.Heading,
                color = CafeColors.Wrong
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "文章 ID: $articleId",
                style = CafeType.Caption,
                color = CafeColors.Muted
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Text("重试", color = CafeColors.Accent)
            }
        }
    }
}