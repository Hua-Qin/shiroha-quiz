package com.yiqiu.readingquiz.ui.screens

import android.util.Log
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
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * - 严格遵守 spec 附录 A.1：使用 Icons.Filled.Flag（已收藏）+ Icons.Rounded.BookmarkBorder（未收藏）+ Icons.Rounded.Fullscreen / FullscreenExit。
 * - Task 4：删除 snapshotFlow 自动全屏，改用 `immersive` 手动开关（TopBar 隐藏，BottomActionBar 保留）。
 * - Task 5：删除 Share 按钮。
 * - Task 3：递归渲染 ArticleBlock.Section。
 */
@Composable
fun ReadingScreen(
    articleId: String,
    onBack: () -> Unit,
    onEnterQuiz: (String) -> Unit
) {
    val article = remember(articleId) { ReadingRepository.getArticle(articleId) }
    Log.d("Reading", "open: articleId=$articleId, found=${article != null}")
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
    var immersive by remember { mutableStateOf(false) }

    val progress by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val total = info.totalItemsCount.coerceAtLeast(1)
            val visible = listState.firstVisibleItemIndex
            ((visible.toFloat() / total.toFloat()) * 100).toInt().coerceIn(0, 100)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(CafeColors.Bg)) {
        // Task 4：TopBar 仅在 immersive=false 时显示
        if (!immersive) {
            TopBar(
                article = article,
                isMarked = isMarked,
                immersive = immersive,
                onBack = onBack,
                onToggleMark = {
                    isMarked = !isMarked
                    ReadingRepository.toggleFavorite(article.id)
                },
                onToggleImmersive = {
                    immersive = !immersive
                    Log.d("Reading", "immersive=$immersive")
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
            // Task 3：递归渲染 Section / Paragraph / Image
            renderBlocks(article.blocks)
        }

        // Task 4：BottomActionBar 始终保留（仅 IconButton 可隐藏）
        BottomActionBar(
            immersive = immersive,
            articleId = article.id,
            onEnterQuiz = {
                Log.d("Reading", "→ quiz")
                onEnterQuiz(article.id)
            },
            onToggleImmersive = {
                immersive = !immersive
                Log.d("Reading", "immersive=$immersive")
            }
        )
    }
}

@Composable
private fun TopBar(
    article: Article,
    isMarked: Boolean,
    immersive: Boolean,
    onBack: () -> Unit,
    onToggleMark: () -> Unit,
    onToggleImmersive: () -> Unit
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
            val icon: androidx.compose.ui.graphics.vector.ImageVector =
                if (isMarked) Icons.Filled.Flag else Icons.Rounded.BookmarkBorder
            Icon(
                imageVector = icon,
                contentDescription = if (isMarked) "已标记" else "标记疑问",
                tint = if (isMarked) CafeColors.Accent2 else CafeColors.Fg
            )
        }
        IconButton(onClick = onToggleImmersive) {
            Icon(
                imageVector = Icons.Rounded.FullscreenExit,
                contentDescription = "退出沉浸模式",
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
    immersive: Boolean,
    articleId: String,
    onEnterQuiz: () -> Unit,
    onToggleImmersive: () -> Unit
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
        // Task 4：沉浸模式下显示 Fullscreen 进入按钮，否则隐藏（TopBar 已有 FullscreenExit）
        if (immersive) {
            IconButton(onClick = onToggleImmersive) {
                Icon(
                    imageVector = Icons.Rounded.Fullscreen,
                    contentDescription = "进入沉浸模式",
                    tint = CafeColors.Fg
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        CafePrimaryButton(
            text = "进入答题",
            onClick = onEnterQuiz
        )
    }
}

/**
 * 递归渲染 ArticleBlock 列表（顶层用 LazyListScope DSL）。
 */
private fun androidx.compose.foundation.lazy.LazyListScope.renderBlocks(
    blocks: List<ArticleBlock>
) {
    blocks.forEach { block ->
        when (block) {
            is ArticleBlock.Paragraph -> item(key = "p-${block.text.hashCode()}") {
                CafeHighlightText(text = block.text, highlights = block.highlights)
            }
            is ArticleBlock.Image -> item(key = "img-${block.path}") {
                Box(
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
            is ArticleBlock.Section -> {
                item(key = "s-${block.title}-${block.level}") {
                    SectionHeader(block.title, block.level)
                }
                renderBlocks(block.children)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, level: Int) {
    val (style, indent) = when (level) {
        1 -> CafeType.Title to 0.dp
        2 -> CafeType.Heading to 8.dp
        else -> CafeType.BodyBold to 16.dp
    }
    val color = if (level == 1) CafeColors.Accent else CafeColors.Fg
    Row(modifier = Modifier.padding(start = indent, top = 8.dp, bottom = 4.dp)) {
        Text(text = title, style = style, color = color)
    }
}