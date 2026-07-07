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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.LibraryBooks
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.ArticleBlock
import com.yiqiu.readingquiz.ui.components.CafeButton
import com.yiqiu.readingquiz.ui.components.CafeButtonVariant
import com.yiqiu.readingquiz.ui.components.CafeEyebrow
import com.yiqiu.readingquiz.ui.components.CafeHighlightText
import com.yiqiu.readingquiz.ui.components.CafeKpiCard
import com.yiqiu.readingquiz.ui.components.CafeTopBar
import com.yiqiu.readingquiz.ui.components.EditArticleDialog
import com.yiqiu.readingquiz.ui.components.NoteActionMenu
import com.yiqiu.readingquiz.ui.components.NotePadWindow
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType
import dev.jeziellago.compose.markdowntext.MarkdownText

// 文件级私有间距 / 尺寸常量（无对应全局 token 时使用，避免在屏幕上散落 .dp 字面量）
private val ChipPadV = 8.dp            // 题目库 chip 竖直 padding
private val ChipAccentDot = 8.dp       // 题目库 chip 内 accent 圆点直径
private val ChipIconSize = 16.dp       // 题目库 chip 内图标尺寸
private val BottomActionIconBtnSize = 40.dp  // 底部操作栏图标按钮尺寸

/**
 * 阅读页（cafe-ui 风格重写）。
 * - TopBar 用 CafeTopBar（题目库 chip 在 actions 中保留上一轮已实现样式）。
 * - SectionHeader 用 CafeEyebrow 标签（level 1 = accent + leading dot，level 2/3 = muted）。
 * - 章节进度用 CafeKpiCard 风格改造 SectionProgressBar。
 * - BottomActionBar 改为 CafeButton + accent chip（题目库）。
 * - ProgressIndicator 用 CafeKpiCard 风格。
 * - 内容渲染沿用 renderBlocks 扩展（CafeHighlightText / MarkdownText）。
 */
@Composable
fun ReadingScreen(
    articleId: String,
    onBack: () -> Unit,
    onEnterQuiz: (String) -> Unit,
    initialSectionId: String? = null,
    onOpenQuestionBank: ((String) -> Unit)? = null
) {
    val article = remember(articleId) { ReadingRepository.getArticle(articleId) }
    Log.d("Reading", "open: articleId=$articleId, section=$initialSectionId, found=${article != null}")
    if (article == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CafeColors.Bg)
                .padding(CafeSpacing.containerPad)
        ) {
            Text(text = "文章不存在", style = CafeType.displaySection, color = CafeColors.Wrong)
            Spacer(modifier = Modifier.height(CafeSpacing.md))
            CafeButton(text = "返回", onClick = onBack)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeColors.Bg)
    ) {
        // immersive=false 时显示 TopBar
        if (!immersive) {
            CafeTopBar(
                title = article.title,
                subtitle = article.category.ifBlank { null },
                onBack = onBack,
                actions = {
                    // 收藏 IconButton（实心 Flag / 空心 BookmarkBorder）
                    IconButton(
                        onClick = {
                            isMarked = !isMarked
                            ReadingRepository.toggleFavorite(article.id)
                        }
                    ) {
                        Icon(
                            imageVector = if (isMarked) Icons.Filled.Flag else Icons.Rounded.BookmarkBorder,
                            contentDescription = if (isMarked) "已标记" else "标记疑问",
                            tint = if (isMarked) CafeColors.Accent2 else CafeColors.Fg
                        )
                    }
                    // 笔记 IconButton（点击弹出 NoteActionMenu）
                    IconButton(onClick = { showActionMenu = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "记笔记",
                            tint = CafeColors.Fg
                        )
                    }
                    // 沉浸模式 IconButton（topbar 上用 FullscreenExit 表示"退出沉浸"）
                    IconButton(
                        onClick = {
                            immersive = !immersive
                            Log.d("Reading", "immersive=$immersive")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FullscreenExit,
                            contentDescription = "退出沉浸模式",
                            tint = CafeColors.Fg
                        )
                    }
                    // 题目库 chip：accent 圆点 + "题目库" 文字（保留上一轮已实现的样式）
                    if (onOpenQuestionBank != null) {
                        QuestionBankChip(
                            onClick = { onOpenQuestionBank(article.id) }
                        )
                    }
                }
            )
            ProgressIndicator(percent = progress)
            // 章节进度条（已学章节 X/Y）
            SectionProgressBar(articleId = article.id)
        }

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = CafeSpacing.containerPad, vertical = CafeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(CafeSpacing.md),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Text(text = article.title, style = CafeType.displaySection, color = CafeColors.Fg)
                Spacer(modifier = Modifier.height(CafeSpacing.xs))
                Text(
                    text = listOfNotNull(
                        article.author.ifBlank { null },
                        article.source.ifBlank { null }
                    ).joinToString(" · "),
                    style = CafeType.bodyXSmall,
                    color = CafeColors.Muted
                )
            }
            renderBlocks(article.blocks)
        }

        // BottomActionBar 始终保留（仅 TopBar 可隐藏）
        BottomActionBar(
            immersive = immersive,
            articleId = article.id,
            initialSectionId = initialSectionId,
            onEditClick = { showActionMenu = true },
            onEnterQuiz = {
                Log.d("Reading", "→ quiz")
                onEnterQuiz(article.id)
            },
            onToggleImmersive = {
                immersive = !immersive
                Log.d("Reading", "immersive=$immersive")
            },
            onOpenQuestionBank = onOpenQuestionBank
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

/**
 * 题目库 chip：accent 圆点 + "题目库" 文字（保留上一轮已实现的醒目样式）。
 * 用 cafe-ui token 重建：accent.copy(alpha=0.12f) 浅色背景 + 完全圆角。
 */
@Composable
private fun QuestionBankChip(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(
                CafeColors.Accent.copy(alpha = 0.12f),
                RoundedCornerShape(CafeRadius.rPill)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = CafeSpacing.xs, vertical = ChipPadV),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(ChipAccentDot)
                .background(CafeColors.Accent, RoundedCornerShape(CafeRadius.rPill))
        )
        Icon(
            imageVector = Icons.Rounded.LibraryBooks,
            contentDescription = null,
            tint = CafeColors.Accent,
            modifier = Modifier.size(ChipIconSize)
        )
        Text(
            text = "题目库",
            style = CafeType.eyebrow.copy(color = CafeColors.Accent),
            color = CafeColors.Accent
        )
    }
}

/**
 * 阅读进度指示器（cafe-ui CafeKpiCard 风格）。
 * - surface + 1px border + rBtn 圆角
 * - eyebrow label "READING PROGRESS" + 24sp/700 value + meta delta
 */
@Composable
private fun ProgressIndicator(percent: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CafeSpacing.containerPad, vertical = CafeSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(CafeSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CafeKpiCard(
            label = "Reading",
            value = "$percent%",
            modifier = Modifier.weight(1f),
            delta = if (percent >= 100) "DONE" else "IN PROGRESS"
        )
    }
}

/**
 * 底部操作栏：编辑图标 + 题目库 chip + 主 CTA。
 * - 主 CTA：进入答题 / 答本章节题（CafeButton Primary）
 * - 题目库 chip：accent 圆点 + "题目库"
 */
@Composable
private fun BottomActionBar(
    immersive: Boolean,
    articleId: String,
    initialSectionId: String?,
    onEditClick: () -> Unit,
    onEnterQuiz: () -> Unit,
    onToggleImmersive: () -> Unit,
    onOpenQuestionBank: ((String) -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CafeSpacing.containerPad, vertical = CafeSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onEditClick,
            modifier = Modifier
                .background(Color.Transparent, RoundedCornerShape(CafeRadius.rMd))
                .size(BottomActionIconBtnSize)
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "记笔记",
                tint = CafeColors.Fg
            )
        }
        // 题目库 chip：保留上一轮已实现样式（accent 圆点 + "题目库"）
        if (onOpenQuestionBank != null) {
            QuestionBankChip(onClick = { onOpenQuestionBank(articleId) })
        }
        // 沉浸模式下显示 Fullscreen 进入按钮
        if (immersive) {
            IconButton(
                onClick = onToggleImmersive,
                modifier = Modifier
                    .background(Color.Transparent, RoundedCornerShape(CafeRadius.rMd))
                    .size(BottomActionIconBtnSize)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Fullscreen,
                    contentDescription = "进入沉浸模式",
                    tint = CafeColors.Fg
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        CafeButton(
            text = if (initialSectionId != null) "答本章节题" else "进入答题",
            onClick = onEnterQuiz,
            variant = CafeButtonVariant.Primary
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
                    val indent = when (block.level) {
                        1 -> 0.dp
                        2 -> CafeSpacing.xs
                        else -> CafeSpacing.md
                    }
                    val tag = "Chapter · Lv ${block.level}"
                    val titleStyle = when (block.level) {
                        1 -> CafeType.displaySection.copy(color = CafeColors.Accent)
                        2 -> CafeType.cardTitle.copy(color = CafeColors.Fg)
                        else -> CafeType.bodySmall.copy(color = CafeColors.Fg)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = indent, top = CafeSpacing.md, bottom = CafeSpacing.xs)
                    ) {
                        CafeEyebrow(
                            text = tag,
                            showLeadingDot = block.level == 1,
                            textColor = if (block.level == 1) CafeColors.Accent else CafeColors.Muted
                        )
                        Spacer(modifier = Modifier.size(CafeSpacing.xs))
                        Text(text = block.title, style = titleStyle)
                    }
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
                .clip(RoundedCornerShape(CafeRadius.rCard))
                .clickable { showPreview = true }
        )
        if (caption.isNotBlank()) {
            Spacer(modifier = Modifier.height(CafeSpacing.xs))
            Text(
                text = caption,
                style = CafeType.bodyXSmall,
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
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(CafeSpacing.md)
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
 * 用 cafe-ui CafeKpiCard 风格重写：surface + 1px border + 圆角 + eyebrow label + kpi value。
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
            .padding(horizontal = CafeSpacing.containerPad, vertical = CafeSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(CafeSpacing.sm)
    ) {
        CafeKpiCard(
            label = "Sections",
            value = "$completedCount / $totalSections",
            modifier = Modifier.weight(1f),
            delta = if (totalSections > 0 && completedCount >= totalSections) "DONE" else "IN PROGRESS"
        )
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