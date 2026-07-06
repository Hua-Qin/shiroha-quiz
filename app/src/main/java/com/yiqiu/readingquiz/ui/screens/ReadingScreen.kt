package com.yiqiu.readingquiz.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.Article
import com.yiqiu.readingquiz.data.model.ArticleBlock
import com.yiqiu.readingquiz.ui.components.CafeHighlightText
import com.yiqiu.readingquiz.ui.components.CafePrimaryButton
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * 阅读页。
 * 严格遵守 spec 附录 A.1：使用 Icons.Filled.Flag（已收藏）与 Icons.Rounded.BookmarkBorder（未收藏）；
 * 并显式 import 对应的 extension property（spec 附录 A.2）。
 */
@Composable
fun ReadingScreen(
    articleId: String,
    onBack: () -> Unit,
    onEnterQuiz: (String) -> Unit
) {
    val article = remember(articleId) { ReadingRepository.getArticle(articleId) }
    if (article == null) {
        Column(modifier = Modifier.fillMaxSize().padding(CafeSpacing.ContainerPad)) {
            Text(text = "文章不存在", style = CafeType.Heading, color = CafeColors.Wrong)
            Spacer(modifier = Modifier.height(12.dp))
            CafePrimaryButton(text = "返回", onClick = onBack)
        }
        return
    }

    val listState = rememberLazyListState()
    var isMarked by remember { mutableStateOf(article.favorite) }
    var isFullscreen by remember { mutableStateOf(false) }
    val progress by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val total = info.totalItemsCount.coerceAtLeast(1)
            val visible = listState.firstVisibleItemIndex
            ((visible.toFloat() / total.toFloat()) * 100).toInt().coerceIn(0, 100)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling -> isFullscreen = scrolling }
    }

    Column(modifier = Modifier.fillMaxSize().background(CafeColors.Bg)) {
        if (!isFullscreen) {
            TopBar(
                article = article,
                isMarked = isMarked,
                onBack = onBack,
                onToggleMark = {
                    isMarked = !isMarked
                    ReadingRepository.toggleFavorite(article.id)
                }
            )
            ProgressIndicator(percent = progress)
        }

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(CafeSpacing.ContainerPad),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Text(text = article.title, style = CafeType.Title, color = CafeColors.Fg)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${article.author} · ${article.source}",
                    style = CafeType.Caption,
                    color = CafeColors.Muted
                )
            }
            items(count = article.blocks.size) { idx ->
                when (val block = article.blocks[idx]) {
                    is ArticleBlock.Paragraph -> CafeHighlightText(
                        text = block.text,
                        highlights = block.highlights
                    )
                    is ArticleBlock.Image -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(CafeColors.Border)
                    ) {
                        Text(
                            text = "[图片：${block.caption}]",
                            style = CafeType.Caption,
                            color = CafeColors.Muted,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        if (!isFullscreen) {
            BottomActionBar(
                articleId = article.id,
                onEnterQuiz = onEnterQuiz
            )
        }
    }
}

@Composable
private fun TopBar(
    article: Article,
    isMarked: Boolean,
    onBack: () -> Unit,
    onToggleMark: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CafeSpacing.ContainerPad),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "返回",
                tint = CafeColors.Fg
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = article.category, style = CafeType.Caption, color = CafeColors.Muted)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onToggleMark) {
            // 关键点：Flag 与 FlagBorder 都从允许清单选；共用 Icon 变量需要显式声明公共类型
            val icon: androidx.compose.ui.graphics.vector.ImageVector =
                if (isMarked) Icons.Filled.Flag else Icons.Rounded.BookmarkBorder
            Icon(
                imageVector = icon,
                contentDescription = if (isMarked) "已标记" else "标记疑问",
                tint = if (isMarked) CafeColors.Accent2 else CafeColors.Fg
            )
        }
        IconButton(onClick = { /* TODO: share intent */ }) {
            Icon(
                imageVector = Icons.Rounded.Share,
                contentDescription = "分享",
                tint = CafeColors.Fg
            )
        }
    }
}

@Composable
private fun ProgressIndicator(percent: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CafeSpacing.ContainerPad),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$percent%", style = CafeType.Caption, color = CafeColors.Muted)
        Spacer(modifier = Modifier.size(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .background(CafeColors.Border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = percent / 100f)
                    .height(4.dp)
                    .background(CafeColors.Accent)
            )
        }
    }
}

@Composable
private fun BottomActionBar(
    articleId: String,
    onEnterQuiz: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CafeSpacing.ContainerPad),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* TODO: note dialog */ }) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "记笔记",
                tint = CafeColors.Fg
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        CafePrimaryButton(
            text = "进入答题",
            onClick = { onEnterQuiz(articleId) }
        )
    }
}