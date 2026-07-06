package com.yiqiu.readingquiz.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.importexport.FileImporter
import com.yiqiu.readingquiz.ui.components.CafeCard
import com.yiqiu.readingquiz.ui.components.CafePrimaryButton
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

@Composable
fun HomeScreen(
    onOpenArticle: (String) -> Unit,
    onOpenAiSettings: () -> Unit
) {
    val articles = ReadingRepository.articles
    val context = LocalContext.current
    var importMessage by remember { mutableStateOf<String?>(null) }

    // 文件选择器：接受 application/json、text/markdown、text/plain
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            importMessage = "已取消导入。"
            return@rememberLauncherForActivityResult
        }
        importMessage = try {
            when (val r = FileImporter.importFromUri(context, uri)) {
                is FileImporter.Result.Success -> "已导入：${r.article.title}"
                is FileImporter.Result.Failure -> "导入失败：${r.reason}"
            }
        } catch (e: Throwable) {
            "导入异常：${e.message ?: "未知错误"}"
        }
    }

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

        // 操作栏：导入文章
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    openDocumentLauncher.launch(
                        arrayOf(
                            "application/json",
                            "text/markdown",
                            "text/plain",
                            "text/*"
                        )
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.FileDownload,
                    contentDescription = null,
                    tint = CafeColors.Accent
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = "导入文章", color = CafeColors.Accent)
            }
            importMessage?.let {
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = it,
                    style = CafeType.Caption,
                    color = CafeColors.Muted
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
                        text = "点击上方「导入文章」从 JSON / Markdown / TXT 加载",
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
                                TextButton(onClick = { onOpenArticle(article.id) }) {
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
                }
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    CafePrimaryButton(
                        text = "导入更多文章",
                        onClick = {
                            openDocumentLauncher.launch(
                                arrayOf("application/json", "text/markdown", "text/plain", "text/*")
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}