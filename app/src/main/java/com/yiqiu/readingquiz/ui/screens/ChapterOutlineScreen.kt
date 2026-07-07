package com.yiqiu.readingquiz.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.Article
import com.yiqiu.readingquiz.data.model.ArticleBlock
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * 章节大纲页。
 *
 * - 顶部：返回 + 文章标题 + 整体进度（已学 X/Y 章节）
 * - 列表：L1 Section 行；点击展开显示其下属 L2/L3 Section
 * - 每个章节项右侧：进度徽章（已完成 ✓ / 错 N / 未答 N）
 * - 点击 L2/L3 章节：调用 onSelectSection(sectionId) 外层负责跳转
 */
@Composable
fun ChapterOutlineScreen(
    articleId: String,
    onBack: () -> Unit,
    onSelectSection: (sectionId: String) -> Unit
) {
    val article = remember(articleId) { ReadingRepository.getArticle(articleId) }
    Log.d("ChapterOutline", "open: articleId=$articleId, found=${article != null}")

    if (article == null) {
        EmptyOutline(onBack)
        return
    }

    val sections = remember(article.id) {
        extractL1Sections(article)
    }
    val totalCount = sections.sumOf { countAllDescendants(it) }
    val completedCount = sections.count { l1 ->
        // L1 完成 = 所有下属子章节全部完成
        collectAllDescendants(l1).all { desc ->
            ReadingRepository.progressOf(article.id, desc.id).completed
        }
    }

    // 折叠状态：sectionId → 展开？
    val expanded = remember { mutableStateListOf<String>() }
    // 初始化：默认展开所有 L1
    remember {
        sections.forEach { if (!expanded.contains(it.id)) expanded.add(it.id) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeColors.Bg)
            .padding(CafeSpacing.ContainerPad)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "章节大纲",
                    style = CafeType.Caption,
                    color = CafeColors.Muted
                )
                Text(
                    text = article.title,
                    style = CafeType.Heading,
                    color = CafeColors.Fg,
                    maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "已学 $completedCount / $totalCount",
                    style = CafeType.Caption,
                    color = CafeColors.Accent2
                )
                Text(
                    text = "章节",
                    style = CafeType.Caption,
                    color = CafeColors.Muted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 列表
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sections, key = { it.id.ifEmpty { it.title } }) { l1 ->
                SectionItem(
                    article = article,
                    section = l1,
                    expanded = expanded.contains(l1.id),
                    onToggleExpand = {
                        if (expanded.contains(l1.id)) expanded.remove(l1.id)
                        else expanded.add(l1.id)
                    },
                    onSelectSection = onSelectSection
                )
            }
        }
    }
}

/**
 * 单个章节行（L1 + 展开后的子章节）。
 */
@Composable
private fun SectionItem(
    article: Article,
    section: ArticleBlock.Section,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelectSection: (String) -> Unit
) {
    val progress = ReadingRepository.progressOf(article.id, section.id)
    val children = section.children.filterIsInstance<ArticleBlock.Section>()
    val hasChildren = children.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CafeColors.Surface, RoundedCornerShape(CafeRadius.Card))
            .border(1.dp, CafeColors.Border, RoundedCornerShape(CafeRadius.Card))
    ) {
        // L1 主章节行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasChildren) {
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "折叠" else "展开",
                    tint = CafeColors.Muted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = section.title,
                    style = CafeType.Heading,
                    color = CafeColors.Accent
                )
                if (hasChildren) {
                    Text(
                        text = "${children.size} 个子章节",
                        style = CafeType.Caption,
                        color = CafeColors.Muted
                    )
                }
            }
            ProgressBadge(progress = progress)
        }

        // 展开后的子章节
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                children.forEach { child ->
                    SubSectionRow(
                        article = article,
                        section = child,
                        depth = 1,
                        onSelectSection = onSelectSection
                    )
                }
            }
        }
    }
}

/**
 * 子章节行（递归）。
 */
@Composable
private fun SubSectionRow(
    article: Article,
    section: ArticleBlock.Section,
    depth: Int,
    onSelectSection: (String) -> Unit
) {
    val progress = ReadingRepository.progressOf(article.id, section.id)
    val indent = (depth * 12).dp

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = section.id.isNotBlank()) {
                    onSelectSection(section.id)
                }
                .padding(start = indent + 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "L${section.level}  ·  ${section.title}",
                    style = CafeType.Body,
                    color = CafeColors.Fg
                )
            }
            ProgressBadge(progress = progress)
        }

        // 递归展示更深层级
        section.children.filterIsInstance<ArticleBlock.Section>().forEach { grand ->
            SubSectionRow(
                article = article,
                section = grand,
                depth = depth + 1,
                onSelectSection = onSelectSection
            )
        }
    }
}

/**
 * 进度徽章（已完成 ✓ / 错 N / 未答 N）。
 */
@Composable
private fun ProgressBadge(progress: com.yiqiu.readingquiz.data.model.SectionProgress) {
    when {
        progress.completed -> Box(
            modifier = Modifier
                .background(CafeColors.Accent2.copy(alpha = 0.12f), RoundedCornerShape(CafeRadius.Pill))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "已完成",
                    tint = CafeColors.Accent2,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "完成",
                    style = CafeType.Caption,
                    color = CafeColors.Accent2,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        progress.wrongCount > 0 -> Text(
            text = "错 ${progress.wrongCount}",
            style = CafeType.Caption,
            color = CafeColors.Wrong
        )
        progress.unansweredCount > 0 -> Text(
            text = "未答 ${progress.unansweredCount}",
            style = CafeType.Caption,
            color = CafeColors.Muted
        )
        else -> Text(
            text = "—",
            style = CafeType.Caption,
            color = CafeColors.Muted
        )
    }
}

/**
 * 抽取顶层 L1 Section 列表（保证显示顺序稳定）。
 */
private fun extractL1Sections(article: Article): List<ArticleBlock.Section> {
    return article.blocks.filterIsInstance<ArticleBlock.Section>().filter { it.level == 1 }
}

/**
 * 递归统计该章节及其所有后代 Section 的数量。
 */
private fun countAllDescendants(section: ArticleBlock.Section): Int {
    var count = 1
    section.children.filterIsInstance<ArticleBlock.Section>().forEach {
        count += countAllDescendants(it)
    }
    return count
}

/**
 * 收集该章节及其所有后代 Section（深度优先）。
 */
private fun collectAllDescendants(section: ArticleBlock.Section): List<ArticleBlock.Section> {
    val list = mutableListOf(section)
    section.children.filterIsInstance<ArticleBlock.Section>().forEach {
        list.addAll(collectAllDescendants(it))
    }
    return list
}

@Composable
private fun EmptyOutline(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(CafeColors.Bg).padding(CafeSpacing.ContainerPad)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "返回",
                    tint = CafeColors.Fg
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "章节大纲", style = CafeType.Heading, color = CafeColors.Fg)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "文章不存在",
            style = CafeType.Heading,
            color = CafeColors.Wrong
        )
    }
}