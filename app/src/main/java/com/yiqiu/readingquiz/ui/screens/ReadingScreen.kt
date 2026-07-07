package com.yiqiu.readingquiz.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.Article
import com.yiqiu.readingquiz.data.model.ArticleBlock
import com.yiqiu.readingquiz.data.model.ReadingNote
import com.yiqiu.readingquiz.ui.components.CafeHighlightText
import com.yiqiu.readingquiz.ui.components.CafePrimaryButton
import com.yiqiu.readingquiz.ui.components.EditArticleDialog
import com.yiqiu.readingquiz.ui.components.NoteActionMenu
import com.yiqiu.readingquiz.ui.components.NotePadWindow
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.util.UUID

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
    onEnterQuiz: (String) -> Unit,
    initialSectionId: String? = null
) {
    val article = remember(articleId) { ReadingRepository.getArticle(articleId) }
    Log.d("Reading", "open: articleId=$articleId, section=$initialSectionId, found=${article != null}")
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
    // 笔记操作三态：菜单 → 文档编辑 / 浮窗
    var showActionMenu by remember { mutableStateOf(false) }
    var showEditArticle by remember { mutableStateOf(false) }
    var showNotePad by remember { mutableStateOf(false) }

    // 章节锚点：进入时计算 sectionId → item index 映射，进入后 scrollToItem
    val sectionIndexMap = remember(article.id, article.blocks) {
        buildSectionIndexMap(article.blocks)
    }
    LaunchedEffect(initialSectionId, sectionIndexMap) {
        if (initialSectionId != null && sectionIndexMap.containsKey(initialSectionId)) {
            val idx = sectionIndexMap[initialSectionId] ?: return@LaunchedEffect
            // 滚动到该章节 item（item 0 是文章标题，所以 idx 需要 +1）
            listState.scrollToItem(idx + 1)
            Log.d("Reading", "scrolled to section=$initialSectionId at index=$idx")
        }
    }

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
            // 新增章节进度条（已学章节 X/Y）
            SectionProgressBar(articleId = article.id)
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
            sectionId = initialSectionId,
            initialSectionId = initialSectionId,
            onEditClick = { showActionMenu = true },
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

    // 笔记操作菜单（点击 Edit 按钮后弹出，选择「直接编辑文档」或「新建笔记」）
    if (showActionMenu) {
        NoteActionMenu(
            onEditArticle = { showEditArticle = true },
            onCreateNote = { showNotePad = true },
            onDismiss = { showActionMenu = false }
        )
    }

    // 直接编辑文档（占位对话框）
    if (showEditArticle) {
        EditArticleDialog(
            articleTitle = article.title,
            onDismiss = { showEditArticle = false }
        )
    }

    // 便笺浮窗（新建笔记）
    if (showNotePad) {
        NotePadWindow(
            articleId = article.id,
            onDismiss = { showNotePad = false }
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
    sectionId: String?,
    initialSectionId: String?,
    onEditClick: () -> Unit,
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
        IconButton(onClick = onEditClick) {
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
            text = if (initialSectionId != null) "答本章节题" else "进入答题",
            onClick = onEnterQuiz
        )
    }
}

/**
 * 递归渲染 ArticleBlock 列表（顶层用 LazyListScope DSL）。
 * 使用全局计数器为每个 item 生成唯一 key（spec 附录 A.3：
 * 禁止用 String.hashCode() 作 LazyColumn key，中文短文本碰撞率极高）。
 */
private fun androidx.compose.foundation.lazy.LazyListScope.renderBlocks(
    blocks: List<ArticleBlock>,
    counter: KeyCounter = KeyCounter()
) {
    blocks.forEach { block ->
        when (block) {
            is ArticleBlock.Paragraph -> item(key = counter.next("p")) {
                // 有 highlights 时保留 CafeHighlightText（点击释义交互优先）；
                // 无 highlights 时用 MarkdownText 渲染完整 Markdown 行内格式（粗体/斜体/代码/列表/表格等）
                if (block.highlights.isNotEmpty()) {
                    CafeHighlightText(text = block.text, highlights = block.highlights)
                } else {
                    MarkdownText(
                        markdown = block.text,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            is ArticleBlock.Image -> item(key = counter.next("img")) {
                ArticleImage(path = block.path, caption = block.caption)
            }
            is ArticleBlock.Section -> {
                item(key = counter.next("s")) {
                    SectionHeader(block.title, block.level)
                }
                renderBlocks(block.children, counter)
            }
        }
    }
}

/**
 * 全局唯一 key 生成器（基于 AtomicInteger 防止 LazyColumn key 碰撞）。
 * 每次 next(prefix) 返回 "$prefix-${递增 id}"，天然唯一。
 */
private class KeyCounter {
    private val seq = java.util.concurrent.atomic.AtomicInteger(0)
    fun next(prefix: String): String = "$prefix-${seq.incrementAndGet()}"
}

@Composable
private fun SectionHeader(title: String, level: Int) {
    // CafeType 无 BodyBold，使用 Body + Bold weight 替代（FontWeight.SemiBold 即可视觉区分层级）
    val (style, indent) = when (level) {
        1 -> CafeType.Title to 0.dp
        2 -> CafeType.Heading to 8.dp
        else -> CafeType.Body to 16.dp
    }
    val color = if (level == 1) CafeColors.Accent else CafeColors.Fg
    Row(modifier = Modifier.padding(start = indent, top = 8.dp, bottom = 4.dp)) {
        Text(text = title, style = style, color = color)
    }
}

/**
 * 文章内嵌图片：用 Coil AsyncImage 加载，点击弹出全屏预览。
 */
@Composable
private fun ArticleImage(path: String, caption: String) {
    var showPreview by remember { mutableStateOf(false) }
    Column {
        AsyncImage(
            model = path,
            contentDescription = caption.ifBlank { "文章图片" },
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPreview = true }
        )
        if (caption.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = caption,
                style = CafeType.Caption,
                color = CafeColors.Muted
            )
        }
    }
    if (showPreview) {
        ImagePreviewDialog(path = path, onDismiss = { showPreview = false })
    }
}

/**
 * 图片全屏预览 Dialog：支持双指缩放（pinch-to-zoom）和拖动。
 */
@Composable
private fun ImagePreviewDialog(path: String, onDismiss: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { onDismiss() }
        ) {
            AsyncImage(
                model = path,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    }
            )
            // 关闭按钮
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "关闭预览",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * 构建 Section.id → LazyColumn item index 的映射。
 * 顶层 item 是文章标题（index 0），其后按 renderBlocks 的 DFS 顺序排列。
 * 仅统计 level > 0 的 Section 起始位置（SectionHeader 占据的 item）。
 */
private fun buildSectionIndexMap(blocks: List<ArticleBlock>): Map<String, Int> {
    val map = mutableMapOf<String, Int>()
    var itemIndex = 0
    fun walk(list: List<ArticleBlock>) {
        for (block in list) {
            when (block) {
                is ArticleBlock.Section -> {
                    // SectionHeader 占用 1 个 item
                    if (block.id.isNotBlank()) {
                        map[block.id] = itemIndex
                    }
                    itemIndex++
                    walk(block.children)
                }
                is ArticleBlock.Paragraph -> itemIndex++
                is ArticleBlock.Image -> itemIndex++
            }
        }
    }
    walk(blocks)
    return map
}

/**
 * 章节进度条（已学章节 X/Y）。
 * 订阅 ReadingRepository.sectionProgress 实现实时更新。
 */
@Composable
private fun SectionProgressBar(articleId: String) {
    val totalSections = remember(articleId) {
        ReadingRepository.getArticle(articleId)?.let { countAllSections(it.blocks) } ?: 0
    }
    val completedCount = ReadingRepository.sectionProgress.values
        .count { it.articleId == articleId && it.completed }
        .coerceAtMost(totalSections.coerceAtLeast(1))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CafeSpacing.ContainerPad, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "已学章节 $completedCount / $totalSections",
            style = CafeType.Caption,
            color = CafeColors.Accent2
        )
        Spacer(modifier = Modifier.size(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(3.dp)
                .background(CafeColors.Border)
        ) {
            val frac = if (totalSections > 0) completedCount.toFloat() / totalSections else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = frac.coerceIn(0f, 1f))
                    .height(3.dp)
                    .background(CafeColors.Accent2)
            )
        }
    }
}

/**
 * 统计文章中所有 Section 的数量（含嵌套）。
 */
private fun countAllSections(blocks: List<ArticleBlock>): Int {
    var count = 0
    fun walk(list: List<ArticleBlock>) {
        for (block in list) {
            when (block) {
                is ArticleBlock.Section -> {
                    count++
                    walk(block.children)
                }
                else -> { /* no-op */ }
            }
        }
    }
    walk(blocks)
    return count
}