package com.yiqiu.readingquiz.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.yiqiu.readingquiz.ui.components.CafeCtaBanner
import com.yiqiu.readingquiz.ui.components.CafeEyebrow
import com.yiqiu.readingquiz.ui.components.CafeFeatureCard
import com.yiqiu.readingquiz.ui.components.CafeIconTile
import com.yiqiu.readingquiz.ui.components.CafeKpiCard
import com.yiqiu.readingquiz.ui.components.CafeListRow
import com.yiqiu.readingquiz.ui.components.CafeTopBar
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * 首页（cafe-ui 风格）。
 *
 * 布局：
 * 1. CafeTopBar（showBrandLogo + AI 设置入口）
 * 2. LazyColumn 滚动主区：
 *    - 欢迎 Feature Card
 *    - 3 列 KPI Row
 *    - 文章列表（每行 CafeListRow + Badge 进度）
 *    - 底部 CTA Banner（导入文章 + AI 设置）
 *
 * 函数签名 `HomeScreen(onOpenArticle, onOpenAiSettings)` 必须保持不变。
 */
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
            .background(CafeColors.Bg)
    ) {
        // ===== 顶部导航 =====
        CafeTopBar(
            title = "Reading Quiz",
            subtitle = "${articles.size} 篇文章",
            showBrandLogo = true,
            actions = {
                IconButton(onClick = onOpenAiSettings) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "AI 设置",
                        tint = CafeColors.Fg
                    )
                }
            }
        )

        if (articles.isEmpty()) {
            // ===== 空态 =====
            EmptyHomeState(onOpenAiSettings = onOpenAiSettings)
        } else {
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
                // 欢迎 Feature Card
                item("welcome") {
                    CafeFeatureCard(
                        icon = Icons.Rounded.AutoStories,
                        eyebrow = "READING QUIZ",
                        title = "读懂每一篇文章",
                        body = "把阅读时间变成可量化的成长：边读边做 AI 智能题，错题自动归档，复习有的放矢。"
                    )
                }

                // KPI 行
                item("kpis") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CafeSpacing.sm)
                    ) {
                        val totalArticles = articles.size
                        val totalLearnedSections = articles.sumOf { a ->
                            countLearnedSections(a)
                        }
                        val totalSections = articles.sumOf { a -> countAllSections(a) }
                        val totalQuestions = articles.sumOf { a ->
                            ReadingRepository.getQuestions(a.id).size
                        }

                        CafeKpiCard(
                            modifier = Modifier.weight(1f),
                            label = "Articles",
                            value = totalArticles.toString(),
                            delta = if (totalArticles > 0) "+${totalArticles}" else null
                        )
                        CafeKpiCard(
                            modifier = Modifier.weight(1f),
                            label = "Sections",
                            value = "$totalLearnedSections/$totalSections",
                            delta = if (totalSections > 0)
                                "${(totalLearnedSections * 100 / totalSections)}%"
                            else null
                        )
                        CafeKpiCard(
                            modifier = Modifier.weight(1f),
                            label = "Questions",
                            value = totalQuestions.toString(),
                            delta = if (totalQuestions > 0) "AI" else null
                        )
                    }
                }

                // 文章列表 Eyebrow 标题
                item("library-eyebrow") {
                    Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
                        CafeEyebrow(text = "Library", showLeadingDot = true)
                        Text(
                            text = "我的文章",
                            style = CafeType.cardTitle,
                            color = CafeColors.Fg
                        )
                    }
                }

                // 文章列表
                items(articles, key = { it.id }) { article ->
                    runCatching {
                        ArticleListItem(
                            article = article,
                            showTopBorder = false,
                            onClick = {
                                Log.d(
                                    "Reading",
                                    "user clicked article: id=${article.id}, title='${article.title}'"
                                )
                                onOpenArticle(article.id)
                            }
                        )
                    }.getOrElse { e ->
                        Log.w(
                            "Home",
                            "article card render failed: id=${article.id}, err=${e.message}",
                            e
                        )
                        FailedArticleCard(articleId = article.id)
                    }
                }

                // 底部 CTA Banner
                item("cta") {
                    CafeCtaBanner(
                        title = "导入你的第一篇文章",
                        body = "支持 JSON / Markdown / TXT 三种格式，AI 自动出题。",
                        primaryButton = {
                            CafeButton(
                                text = "导入文章",
                                onClick = onOpenAiSettings,
                                variant = CafeButtonVariant.OnDark
                            )
                        },
                        secondaryButton = {
                            CafeButton(
                                text = "AI 设置",
                                onClick = onOpenAiSettings,
                                variant = CafeButtonVariant.OnDark
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * 单行文章列表项（CafeListRow + 收藏角标 + 进度 Badge）。
 */
@Composable
private fun ArticleListItem(
    article: Article,
    showTopBorder: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        CafeListRow(
            name = article.title,
            meta = buildArticleMeta(article),
            showTopBorder = showTopBorder,
            onClick = onClick,
            trailing = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                ) {
                    if (article.favorite) {
                        Icon(
                            imageVector = Icons.Rounded.Bookmark,
                            contentDescription = "已收藏",
                            tint = CafeColors.Accent2,
                            modifier = Modifier
                        )
                    }
                    val learned = countLearnedSections(article)
                    val total = countAllSections(article)
                    CafeBadge(
                        text = "$learned/$total",
                        variant = CafeBadgeVariant.Up
                    )
                }
            }
        )
    }
}

/**
 * 构建文章 meta 字符串："作者 · 分类"。
 */
private fun buildArticleMeta(article: Article): String {
    val author = article.author.ifBlank { "佚名" }
    val category = article.category.ifBlank { "未分类" }
    return "$author · $category"
}

/**
 * 递归统计文章所有 Section（含 L1/L2/L3）。
 */
private fun countAllSections(article: Article): Int {
    var count = 0
    article.blocks.forEach { block ->
        if (block is ArticleBlock.Section) {
            count += countSectionTree(block)
        }
    }
    return count
}

private fun countSectionTree(section: ArticleBlock.Section): Int {
    var c = 1
    section.children.forEach { child ->
        if (child is ArticleBlock.Section) {
            c += countSectionTree(child)
        }
    }
    return c
}

/**
 * 统计文章所有已学完的 Section 数量。
 */
private fun countLearnedSections(article: Article): Int {
    var count = 0
    article.blocks.forEach { block ->
        if (block is ArticleBlock.Section) {
            count += countLearnedInTree(article.id, block)
        }
    }
    return count
}

private fun countLearnedInTree(articleId: String, section: ArticleBlock.Section): Int {
    var c = 0
    if (ReadingRepository.progressOf(articleId, section.id).completed) c++
    section.children.forEach { child ->
        if (child is ArticleBlock.Section) {
            c += countLearnedInTree(articleId, child)
        }
    }
    return c
}

/**
 * 首页空态。
 */
@Composable
private fun EmptyHomeState(onOpenAiSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeColors.Bg)
            .padding(CafeSpacing.containerPad),
        verticalArrangement = Arrangement.spacedBy(CafeSpacing.md)
    ) {
        CafeCtaBanner(
            title = "暂无文章，去导入你的第一篇吧",
            body = "支持 JSON / Markdown / TXT 三种格式，AI 自动出题。",
            primaryButton = {
                CafeButton(
                    text = "AI 设置",
                    onClick = onOpenAiSettings,
                    variant = CafeButtonVariant.OnDark,
                    leadingIcon = Icons.Rounded.Tune
                )
            },
            secondaryButton = {
                CafeButton(
                    text = "导入文章",
                    onClick = onOpenAiSettings,
                    variant = CafeButtonVariant.OnDark,
                    leadingIcon = Icons.Rounded.AutoStories
                )
            }
        )

        // 提示卡片
        CafeCard {
            Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                ) {
                    CafeIconTile(icon = Icons.Rounded.AutoStories)
                    CafeEyebrow(text = "Getting started", showLeadingDot = true)
                }
                Text(
                    text = "三步开始你的阅读训练",
                    style = CafeType.cardTitle,
                    color = CafeColors.Fg
                )
                Text(
                    text = "1) 在「AI 设置」中配置模型  2) 导入 JSON / Markdown / TXT 文章  3) 进入文章边读边练。",
                    style = CafeType.bodySmall,
                    color = CafeColors.Muted
                )
            }
        }
    }
}

/**
 * 单项文章渲染失败时的兜底卡片。
 */
@Composable
private fun FailedArticleCard(articleId: String) {
    CafeCard {
        Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
            Text(
                text = "加载失败",
                style = CafeType.cardTitle,
                color = CafeColors.Wrong
            )
            Text(
                text = "文章 ID: $articleId",
                style = CafeType.meta,
                color = CafeColors.Muted
            )
            Text(
                text = "请尝试重新导入或重启应用",
                style = CafeType.bodyXSmall,
                color = CafeColors.Muted
            )
        }
    }
}