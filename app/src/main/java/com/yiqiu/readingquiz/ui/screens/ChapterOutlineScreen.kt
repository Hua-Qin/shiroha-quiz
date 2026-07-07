package com.yiqiu.readingquiz.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.model.Article
import com.yiqiu.readingquiz.data.model.ArticleBlock
import com.yiqiu.readingquiz.ui.components.CafeBadge
import com.yiqiu.readingquiz.ui.components.CafeBadgeVariant
import com.yiqiu.readingquiz.ui.components.CafeButton
import com.yiqiu.readingquiz.ui.components.CafeButtonVariant
import com.yiqiu.readingquiz.ui.components.CafeCard
import com.yiqiu.readingquiz.ui.components.CafeEyebrow
import com.yiqiu.readingquiz.ui.components.CafeIconTile
import com.yiqiu.readingquiz.ui.components.CafeKpiCard
import com.yiqiu.readingquiz.ui.components.CafeListRow
import com.yiqiu.readingquiz.ui.components.CafeTopBar
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * 章节大纲页（cafe-ui 风格）。
 *
 * 布局：
 * 1. CafeTopBar（返回 + 文章标题 + "X 章节" 副标题）
 * 2. 顶部"继续上次阅读"KPI 卡（若有 lastReadSectionId）
 * 3. L1 Section 列表（CafeCard + IconTile + Badge 进度）
 *    - 点击展开后，下属 L2/L3 用 CafeListRow 渲染（trailing 是箭头 IconButton → onSelectSection）
 *
 * 函数签名 `ChapterOutlineScreen(articleId, onBack, onSelectSection)` 必须保持不变。
 */
@Composable
fun ChapterOutlineScreen(
    articleId: String,
    onBack: () -> Unit,
    onSelectSection: (sectionId: String) -> Unit
) {
    val article = remember(articleId) { ReadingRepository.getArticle(articleId) }
    Log.d("ChapterOutline", "open: articleId=$articleId, found=${article != null}")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeColors.Bg)
    ) {
        // ===== 顶部栏 =====
        CafeTopBar(
            title = article?.title ?: "章节大纲",
            subtitle = if (article != null) "${countAllSections(article)} 章节" else "未找到文章",
            onBack = onBack
        )

        if (article == null) {
            EmptyChapterOutline(onBack = onBack)
            return
        }

        val sections = remember(article.id) { extractL1Sections(article) }
        val totalCount = sections.sumOf { countAllDescendants(it) }
        val completedCount = sections.count { l1 ->
            collectAllDescendants(l1).all { desc ->
                ReadingRepository.progressOf(article.id, desc.id).completed
            }
        }

        // 折叠状态：sectionId → 展开？
        val expanded = remember { mutableStateListOf<String>() }
        // 初始化：默认展开所有 L1
        remember(sections) {
            sections.forEach { if (!expanded.contains(it.id)) expanded.add(it.id) }
        }

        // "继续上次阅读"：当前文章下最后一个有进度的章节
        val lastReadSectionId = remember(article.id) {
            ReadingRepository.sectionProgressFor(article.id)
                .filter { it.lastUpdated > 0L && !it.completed }
                .maxByOrNull { it.lastUpdated }
                ?.sectionId
        }

        // ===== 主区 =====
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CafeColors.Bg),
            contentPadding = PaddingValues(
                start = CafeSpacing.containerPad,
                end = CafeSpacing.containerPad,
                top = CafeSpacing.md,
                bottom = CafeSpacing.sectionY
            ),
            verticalArrangement = Arrangement.spacedBy(CafeSpacing.md)
        ) {
            // 顶部 KPI 行
            item("overview-kpis") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CafeSpacing.sm)
                ) {
                    CafeKpiCard(
                        modifier = Modifier.weight(1f),
                        label = "Sections",
                        value = sections.size.toString(),
                        delta = "L1"
                    )
                    CafeKpiCard(
                        modifier = Modifier.weight(1f),
                        label = "Subsections",
                        value = totalCount.toString(),
                        delta = "All"
                    )
                    CafeKpiCard(
                        modifier = Modifier.weight(1f),
                        label = "Done",
                        value = "$completedCount/$totalCount",
                        delta = if (totalCount > 0)
                            "${(completedCount * 100 / totalCount)}%"
                        else null
                    )
                }
            }

            // 继续上次阅读 KPI（如果存在）
            if (!lastReadSectionId.isNullOrBlank()) {
                item("resume") {
                    ResumeCard(
                        articleId = article.id,
                        sectionId = lastReadSectionId,
                        onClick = { onSelectSection(lastReadSectionId) }
                    )
                }
            }

            // Section Eyebrow + 标题
            item("outline-eyebrow") {
                Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
                    CafeEyebrow(text = "Outline", showLeadingDot = true)
                    Text(
                        text = "章节大纲",
                        style = CafeType.cardTitle,
                        color = CafeColors.Fg
                    )
                }
            }

            if (sections.isEmpty()) {
                item("empty-outline") {
                    EmptySections()
                }
            } else {
                items(sections, key = { it.id.ifEmpty { it.title } }) { l1 ->
                    L1SectionCard(
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
}

/**
 * 继续上次阅读卡。
 */
@Composable
private fun ResumeCard(
    articleId: String,
    sectionId: String,
    onClick: () -> Unit
) {
    val article = ReadingRepository.getArticle(articleId)
    val sectionTitle = article?.blocks?.firstNotNullOfOrNull { block ->
        if (block is ArticleBlock.Section) findSectionTitle(block, sectionId) else null
    } ?: "未命名章节"

    CafeCard {
        Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
            ) {
                CafeIconTile(icon = Icons.Rounded.AutoStories)
                CafeEyebrow(text = "Resume", showLeadingDot = true)
            }
            Text(
                text = "继续上次阅读",
                style = CafeType.cardTitle,
                color = CafeColors.Fg
            )
            Text(
                text = sectionTitle,
                style = CafeType.bodySmall,
                color = CafeColors.Muted
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                CafeButton(
                    text = "继续阅读",
                    onClick = onClick,
                    variant = CafeButtonVariant.Primary,
                    leadingIcon = Icons.Rounded.AutoStories
                )
            }
        }
    }
}

/**
 * 递归查找指定 sectionId 对应的标题。
 */
private fun findSectionTitle(section: ArticleBlock.Section, targetId: String): String? {
    if (section.id == targetId) return section.title
    section.children.forEach { child ->
        if (child is ArticleBlock.Section) {
            val found = findSectionTitle(child, targetId)
            if (found != null) return found
        }
    }
    return null
}

/**
 * L1 Section 卡片（含展开/折叠的子章节列表）。
 */
@Composable
private fun L1SectionCard(
    article: Article,
    section: ArticleBlock.Section,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelectSection: (String) -> Unit
) {
    val children = section.children.filterIsInstance<ArticleBlock.Section>()
    val hasChildren = children.isNotEmpty()

    // 进度统计：本 L1 + 所有后代
    val allDescendants = collectAllDescendants(section)
    val completed = allDescendants.count {
        ReadingRepository.progressOf(article.id, it.id).completed
    }

    CafeCard(onClick = onToggleExpand) {
        Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
            // L1 header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
            ) {
                CafeIconTile(icon = Icons.Rounded.Folder)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.title,
                        style = CafeType.cardTitle,
                        color = CafeColors.Fg
                    )
                    if (hasChildren) {
                        Text(
                            text = "${children.size} 个子章节",
                            style = CafeType.meta,
                            color = CafeColors.Muted
                        )
                    }
                }
                CafeBadge(
                    text = "已完成 $completed / ${allDescendants.size}",
                    variant = CafeBadgeVariant.Up
                )
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "折叠" else "展开",
                    tint = CafeColors.Muted,
                    modifier = Modifier.size(CafeSpacing.md)
                )
            }

            // 展开后的子章节列表
            AnimatedVisibility(visible = expanded && hasChildren) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                ) {
                    children.forEach { child ->
                        SubSectionListItem(
                            article = article,
                            section = child,
                            onSelectSection = onSelectSection
                        )
                    }
                }
            }
        }
    }
}

/**
 * 单个子章节行（CafeListRow + 题目数 + 箭头）。
 */
@Composable
private fun SubSectionListItem(
    article: Article,
    section: ArticleBlock.Section,
    onSelectSection: (String) -> Unit
) {
    val questionCount = remember(article.id, section.id) {
        ReadingRepository.getQuestions(article.id).count { it.sectionId == section.id }
    }
    val progress = ReadingRepository.progressOf(article.id, section.id)
    val statusBadge: CafeBadgeVariant = when {
        progress.completed -> CafeBadgeVariant.Correct
        progress.wrongCount > 0 -> CafeBadgeVariant.Wrong
        questionCount > 0 -> CafeBadgeVariant.Default
        else -> CafeBadgeVariant.Default
    }
    val statusText = when {
        progress.completed -> "已完成"
        progress.wrongCount > 0 -> "错 ${progress.wrongCount}"
        progress.unansweredCount > 0 -> "未答 ${progress.unansweredCount}"
        else -> "—"
    }

    CafeListRow(
        name = section.title,
        meta = "L${section.level}  ·  $questionCount 题",
        showTopBorder = true,
        onClick = if (section.id.isNotBlank()) {
            { onSelectSection(section.id) }
        } else null,
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
            ) {
                CafeBadge(text = statusText, variant = statusBadge)
                IconButton(
                    onClick = if (section.id.isNotBlank()) {
                        { onSelectSection(section.id) }
                    } else { {} }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "进入阅读",
                        tint = CafeColors.Muted,
                        modifier = Modifier.size(CafeSpacing.md)
                    )
                }
            }
        }
    )
}

/**
 * 空章节列表提示卡。
 */
@Composable
private fun EmptySections() {
    CafeCard {
        Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
            ) {
                CafeIconTile(icon = Icons.Rounded.Folder)
                CafeEyebrow(text = "Empty", showLeadingDot = true)
            }
            Text(
                text = "暂无章节",
                style = CafeType.cardTitle,
                color = CafeColors.Fg
            )
            Text(
                text = "该文章尚未识别到任何章节，可直接进入阅读页开始练习。",
                style = CafeType.bodySmall,
                color = CafeColors.Muted
            )
        }
    }
}

/**
 * 文章不存在时的空态。
 */
@Composable
private fun EmptyChapterOutline(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeColors.Bg)
            .padding(CafeSpacing.containerPad),
        verticalArrangement = Arrangement.spacedBy(CafeSpacing.md)
    ) {
        CafeCard {
            Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                ) {
                    CafeIconTile(icon = Icons.Rounded.Folder)
                    CafeEyebrow(text = "Not found", showLeadingDot = true)
                }
                Text(
                    text = "文章不存在",
                    style = CafeType.cardTitle,
                    color = CafeColors.Wrong
                )
                Text(
                    text = "可能已被删除或导入失败。请返回首页重新导入。",
                    style = CafeType.bodySmall,
                    color = CafeColors.Muted
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    CafeButton(
                        text = "返回首页",
                        onClick = onBack,
                        variant = CafeButtonVariant.Primary
                    )
                }
            }
        }
    }
}

// ========== 工具函数（保持原 ChapterOutlineScreen 的语义） ==========

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

/**
 * 统计文章中所有 Section（L1/L2/L3）数量。
 */
private fun countAllSections(article: Article): Int {
    var count = 0
    article.blocks.forEach { block ->
        if (block is ArticleBlock.Section) {
            count += countAllDescendants(block)
        }
    }
    return count
}