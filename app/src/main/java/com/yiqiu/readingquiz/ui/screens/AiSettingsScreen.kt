package com.yiqiu.readingquiz.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yiqiu.readingquiz.ai.ErrorLogStore
import com.yiqiu.readingquiz.ai.ModelPresets
import com.yiqiu.readingquiz.ai.ReadingAiClient
import com.yiqiu.readingquiz.data.AiConfig
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.importexport.FileImporter
import com.yiqiu.readingquiz.ui.components.CafeBadge
import com.yiqiu.readingquiz.ui.components.CafeBadgeVariant
import com.yiqiu.readingquiz.ui.components.CafeButton
import com.yiqiu.readingquiz.ui.components.CafeButtonAi
import com.yiqiu.readingquiz.ui.components.CafeButtonVariant
import com.yiqiu.readingquiz.ui.components.CafeCard
import com.yiqiu.readingquiz.ui.components.CafeEyebrow
import com.yiqiu.readingquiz.ui.components.CafeListRow
import com.yiqiu.readingquiz.ui.components.CafeTopBar
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeRadius
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 文件级私有尺寸常量（无对应全局 token 时使用，避免在屏幕上散落 .dp 字面量）
private val ErrorLogDialogMaxHeight = 400.dp   // 错误日志对话框内容区最大高度
private val ErrorLogEntryGap = 4.dp            // 错误日志条目内部竖直 gap
private val ModelDropdownMaxHeight = 360.dp    // 模型下拉菜单内容最大高度

/**
 * AI 设置页（cafe-ui 重写版）。
 *
 * 性能优化：
 * 1. 顶层用 LazyColumn 替代 Column，让超长内容可滚动
 * 2. 提取子 Composable（PresetCard / StatusCard / ModelDropdownField / ErrorLogDialog），
 *    避免每帧重建 30+ 嵌套节点
 * 3. forEach 加 key，便于 LazyColumn diff
 * 4. DropdownMenu 内层 LazyColumn → Column + Modifier.verticalScroll + heightIn
 * 5. remember(key1) 锁定 ReadingRepository.aiConfig.value 的初始读取，避免重组时回退
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(onBack: () -> Unit) {
    Log.d("Nav", "→ ai-settings")
    val initial = remember { ReadingRepository.aiConfig.value }
    val scope = rememberCoroutineScope()

    var baseUrl by remember { mutableStateOf(initial.apiBaseUrl) }
    var apiKey by remember { mutableStateOf(initial.apiKey) }
    var modelName by remember { mutableStateOf(initial.modelName) }
    var timeout by remember { mutableStateOf(initial.timeoutSeconds.toString()) }
    var status by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    var fetchingModels by remember { mutableStateOf(false) }

    // 模型下拉
    var modelOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var modelMenuOpen by remember { mutableStateOf(false) }

    // 错误日志弹窗
    var showLogDialog by remember { mutableStateOf(false) }

    // 导入文章
    val context = LocalContext.current
    var importMessage by remember { mutableStateOf<String?>(null) }
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            importMessage = "已取消导入。"
            return@rememberLauncherForActivityResult
        }
        importMessage = try {
            val result = FileImporter.importFromUri(context, uri)
            when (result) {
                is FileImporter.Result.Success -> "已导入：${result.article.title}"
                is FileImporter.Result.Failure -> {
                    Log.w("AiSettings", "import failed: ${result.reason}")
                    "导入失败：${result.reason}"
                }
            }
        } catch (e: Throwable) {
            Log.w("AiSettings", "import exception: ${e.message}", e)
            "导入异常：${e.message ?: "未知错误"}"
        }
    }

    val applyPreset: (ModelPresets.Preset) -> Unit = { preset ->
        baseUrl = preset.baseUrl
        modelName = preset.defaultModel
        status = if (apiKey.isBlank()) {
            "已应用预设：${preset.displayName}。请填写 API Key 后点击「测试连接」验证。"
        } else {
            "已应用预设：${preset.displayName}（Base URL 已更新）"
        }
        statusIsError = false
    }

    val onFetchModels: () -> Unit = onFetchModels@{
        Log.d("AiSettings", "fetchModels clicked")
        if (baseUrl.isBlank()) {
            status = "请先填写 API Base URL"
            statusIsError = true
            return@onFetchModels
        }
        if (apiKey.isBlank()) {
            status = "请先填写 API Key（缺少密钥无法获取模型列表）"
            statusIsError = true
            return@onFetchModels
        }
        fetchingModels = true
        status = null
        statusIsError = false
        val cfg = AiConfig(
            apiBaseUrl = baseUrl,
            apiKey = apiKey,
            modelName = "",
            timeoutSeconds = timeout.toIntOrNull() ?: 60
        )
        scope.launch {
            val r = withContext(Dispatchers.IO) {
                ReadingAiClient.fetchModels(cfg)
            }
            when (r) {
                is ReadingAiClient.AiResult.Success -> {
                    modelOptions = r.value
                    status = if (r.value.isEmpty()) "该 API 未返回任何模型（请检查 Base URL 是否正确）"
                    else "已获取 ${r.value.size} 个模型，请从下拉选择。"
                    statusIsError = r.value.isEmpty()
                    if (modelName.isBlank() && r.value.isNotEmpty()) {
                        modelName = r.value.first()
                    }
                }
                is ReadingAiClient.AiResult.Failure -> {
                    status = "获取失败：${r.message}"
                    statusIsError = true
                }
            }
            fetchingModels = false
        }
    }

    val onTestConnection: () -> Unit = onTestConnection@{
        Log.d("AiSettings", "testConnection clicked")
        when {
            baseUrl.isBlank() -> {
                status = "请先填写 API Base URL"
                statusIsError = true
                return@onTestConnection
            }
            apiKey.isBlank() -> {
                status = "请先填写 API Key（缺少密钥无法测试连接）"
                statusIsError = true
                return@onTestConnection
            }
            modelName.isBlank() -> {
                status = "请先填写或选择模型名"
                statusIsError = true
                return@onTestConnection
            }
        }
        testing = true
        status = null
        statusIsError = false
        val cfg = AiConfig(
            apiBaseUrl = baseUrl,
            apiKey = apiKey,
            modelName = modelName,
            timeoutSeconds = timeout.toIntOrNull() ?: 60
        )
        scope.launch {
            val r = withContext(Dispatchers.IO) {
                ReadingAiClient.testConnection(cfg)
            }
            when (r) {
                is ReadingAiClient.AiResult.Success -> {
                    statusIsError = false
                    status = r.value
                }
                is ReadingAiClient.AiResult.Failure -> {
                    statusIsError = true
                    status = "失败：${r.message}"
                }
            }
            testing = false
        }
    }

    val onSave: () -> Unit = onSave@{
        Log.d("AiSettings", "save clicked, model=$modelName")
        if (baseUrl.isBlank()) {
            status = "保存失败：请先填写 API Base URL"
            statusIsError = true
            return@onSave
        }
        ReadingRepository.updateAiConfig(
            AiConfig(
                apiBaseUrl = baseUrl,
                apiKey = apiKey,
                modelName = modelName,
                timeoutSeconds = timeout.toIntOrNull() ?: 60
            )
        )
        status = if (apiKey.isBlank()) "已保存（注意：API Key 为空，将无法连接 AI 服务）"
        else "已保存。"
        statusIsError = apiKey.isBlank()
    }

    val onImportClick: () -> Unit = onImportClick@{
        Log.d("FileImport", "user clicked importFile")
        openDocumentLauncher.launch(
            arrayOf(
                "application/json",
                "text/markdown",
                "text/plain",
                "text/*"
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(CafeColors.Bg),
        contentPadding = PaddingValues(
            horizontal = CafeSpacing.containerPad,
            vertical = CafeSpacing.md
        ),
        verticalArrangement = Arrangement.spacedBy(CafeSpacing.md)
    ) {
        item(key = "top-bar") {
            CafeTopBar(title = "AI 设置", onBack = onBack)
        }

        // ============ API 配置组 ============
        item(key = "api-eyebrow") {
            CafeEyebrow(text = "API 设置", showLeadingDot = true)
        }

        item(key = "api-card") {
            CafeCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.md)) {
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("API Base URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    ModelDropdownField(
                        modelName = modelName,
                        onModelNameChange = { modelName = it },
                        modelOptions = modelOptions,
                        menuOpen = modelMenuOpen,
                        onMenuOpenChange = { newState ->
                            if (modelOptions.isNotEmpty()) modelMenuOpen = newState
                            else modelMenuOpen = false
                        },
                        onModelSelected = { id ->
                            modelName = id
                            modelMenuOpen = false
                        }
                    )
                    OutlinedTextField(
                        value = timeout,
                        onValueChange = { timeout = it.filter(Char::isDigit) },
                        label = { Text("超时秒数") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ============ 模型预设组 ============
        item(key = "preset-eyebrow") {
            CafeEyebrow(text = "模型预设", showLeadingDot = true)
        }

        item(key = "preset-tip") {
            TipText("提示：智谱清言与阶跃星辰均提供 OpenAI 兼容 API，可直接使用「获取模型列表」自动拉取完整模型清单。")
        }

        // 预设列表
        items(ModelPresets.ALL, key = { it.id }) { preset ->
            PresetCard(preset = preset, onApply = { applyPreset(preset) })
        }

        // ============ 测试连接组 ============
        item(key = "test-eyebrow") {
            CafeEyebrow(text = "测试连接", showLeadingDot = true)
        }

        item(key = "test-card") {
            CafeCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                    ) {
                        CafeButton(
                            text = if (fetchingModels) "获取中..." else "获取模型列表",
                            onClick = onFetchModels,
                            variant = CafeButtonVariant.Ghost,
                            enabled = !fetchingModels && !testing,
                            loading = fetchingModels,
                            modifier = Modifier.weight(1f)
                        )
                        CafeButtonAi(
                            text = if (testing) "测试中..." else "测试连接",
                            onClick = onTestConnection,
                            enabled = !testing && !fetchingModels,
                            loading = testing,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    CafeButton(
                        text = "保存配置",
                        onClick = onSave,
                        variant = CafeButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ============ 状态反馈 ============
        status?.let {
            item(key = "status-card") {
                StatusCard(message = it, isError = statusIsError)
            }
        }

        // ============ 导入文章组 ============
        item(key = "import-eyebrow") {
            CafeEyebrow(text = "导入文章", showLeadingDot = true)
        }

        item(key = "import-tip") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FileDownload,
                    contentDescription = null,
                    tint = CafeColors.Accent
                )
                Text(
                    text = "支持 JSON / Markdown / TXT 格式",
                    style = CafeType.bodyXSmall,
                    color = CafeColors.Muted
                )
            }
        }

        item(key = "import-button") {
            CafeButton(
                text = "选择文件导入",
                onClick = onImportClick,
                variant = CafeButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth()
            )
        }

        importMessage?.let { msg ->
            item(key = "import-msg-$msg") {
                val isError = msg.startsWith("已取消") ||
                    msg.startsWith("导入失败") ||
                    msg.startsWith("导入异常")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                ) {
                    CafeBadge(
                        text = if (isError) "失败" else "成功",
                        variant = if (isError) CafeBadgeVariant.Wrong else CafeBadgeVariant.Correct
                    )
                    Text(
                        text = msg,
                        style = CafeType.bodyXSmall,
                        color = if (isError) CafeColors.Wrong else CafeColors.Accent2
                    )
                }
            }
        }

        // ============ 错误日志组 ============
        item(key = "error-log-eyebrow") {
            CafeEyebrow(text = "错误日志", showLeadingDot = true)
        }

        item(key = "error-log-card") {
            CafeCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BugReport,
                        contentDescription = null,
                        tint = CafeColors.Muted
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "最近错误记录",
                            style = CafeType.cardTitle,
                            color = CafeColors.Fg
                        )
                        Text(
                            text = "共 ${ErrorLogStore.entries.size} 条",
                            style = CafeType.meta,
                            color = CafeColors.Muted
                        )
                    }
                    CafeBadge(
                        text = "${ErrorLogStore.entries.size}",
                        variant = CafeBadgeVariant.Default
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = CafeSpacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                ) {
                    CafeButton(
                        text = "查看日志",
                        onClick = { showLogDialog = true },
                        variant = CafeButtonVariant.Ghost,
                        modifier = Modifier.weight(1f)
                    )
                    CafeButton(
                        text = "清空",
                        onClick = { ErrorLogStore.clear() },
                        variant = CafeButtonVariant.Ghost,
                        enabled = ErrorLogStore.entries.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item(key = "footer-tip") {
            Text(
                text = "提示：API Key 仅保存在本机，不会上传到任何远端服务。",
                style = CafeType.bodyXSmall,
                color = CafeColors.Muted,
                modifier = Modifier.padding(top = CafeSpacing.xs)
            )
        }
    }

    if (showLogDialog) {
        ErrorLogDialog(onDismiss = { showLogDialog = false })
    }
}

/**
 * 错误日志弹窗：cafe-ui 风格（Surface + 圆角 Card）。
 */
@Composable
private fun ErrorLogDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(CafeRadius.rCardLg),
            color = CafeColors.Surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(CafeSpacing.containerPad)
        ) {
            Column(
                modifier = Modifier.padding(CafeSpacing.cardPad),
                verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "错误日志",
                            style = CafeType.cardTitle,
                            color = CafeColors.Fg
                        )
                        Text(
                            text = "最近 ${ErrorLogStore.entries.size} 条",
                            style = CafeType.meta,
                            color = CafeColors.Muted
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("关闭", color = CafeColors.Muted)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = ErrorLogDialogMaxHeight)
                        .clip(RoundedCornerShape(CafeRadius.rCard))
                        .background(CafeColors.Bg)
                        .padding(CafeSpacing.xs)
                ) {
                    if (ErrorLogStore.entries.isEmpty()) {
                        Text(
                            text = "暂无错误记录。",
                            style = CafeType.body,
                            color = CafeColors.Muted,
                            modifier = Modifier.padding(CafeSpacing.xs)
                        )
                    } else {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                        ) {
                            ErrorLogStore.entries.forEach { entry ->
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(ErrorLogEntryGap)
                                ) {
                                    Text(
                                        text = "${entry.level}/${entry.tag}: ${entry.message}",
                                        style = CafeType.meta,
                                        color = when (entry.level) {
                                            "E" -> CafeColors.Wrong
                                            "W" -> CafeColors.Accent2
                                            else -> CafeColors.Muted
                                        }
                                    )
                                    entry.throwable?.let {
                                        Text(
                                            text = it,
                                            style = CafeType.meta,
                                            color = CafeColors.Muted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                ) {
                    CafeButton(
                        text = "清空",
                        onClick = { ErrorLogStore.clear() },
                        variant = CafeButtonVariant.Ghost,
                        enabled = ErrorLogStore.entries.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                    CafeButton(
                        text = "一键复制",
                        onClick = {
                            if (ErrorLogStore.entries.isNotEmpty()) {
                                val text = ErrorLogStore.toClipboardText()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("错误日志", text))
                                Toast.makeText(context, "已复制 ${ErrorLogStore.entries.size} 条日志", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "暂无日志可复制", Toast.LENGTH_SHORT).show()
                            }
                        },
                        variant = CafeButtonVariant.Primary,
                        leadingIcon = Icons.Rounded.ContentCopy,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ============== 子 Composable ==============

/**
 * 提示文本（cafe-ui CafeCard 轻量风格：surface + border + bodySmall muted 文字）。
 */
@Composable
private fun TipText(text: String) {
    CafeCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = CafeSpacing.cardPad, vertical = CafeSpacing.cardPadSm)
    ) {
        Text(
            text = text,
            style = CafeType.bodyXSmall,
            color = CafeColors.Muted
        )
    }
}

/**
 * 预设卡片：cafe-ui CafeListRow 风格（name = 模型名，meta = 描述，trailing = 应用按钮）。
 */
@Composable
private fun PresetCard(
    preset: ModelPresets.Preset,
    onApply: () -> Unit
) {
    CafeCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(CafeSpacing.xs)) {
            CafeListRow(
                name = preset.displayName,
                meta = "默认模型：${preset.defaultModel}",
                showTopBorder = false,
                trailing = {
                    CafeButton(
                        text = "应用",
                        onClick = onApply,
                        variant = CafeButtonVariant.Ghost
                    )
                }
            )
            Text(
                text = preset.baseUrl,
                style = CafeType.meta,
                color = CafeColors.Muted,
                modifier = Modifier.padding(horizontal = CafeSpacing.listRowPadH)
            )
        }
    }
}

/**
 * 状态卡片：cafe-ui CafeCard + 状态 Badge。
 */
@Composable
private fun StatusCard(message: String, isError: Boolean) {
    CafeCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (isError) CafeColors.Wrong else CafeColors.Accent2
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = message, style = CafeType.body, color = CafeColors.Fg)
            }
            CafeBadge(
                text = if (isError) "失败" else "成功",
                variant = if (isError) CafeBadgeVariant.Wrong else CafeBadgeVariant.Correct
            )
        }
    }
}

/**
 * 模型下拉：cafe-ui CafeListRow + DropdownMenu 弹出风格。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelDropdownField(
    modelName: String,
    onModelNameChange: (String) -> Unit,
    modelOptions: List<String>,
    menuOpen: Boolean,
    onMenuOpenChange: (Boolean) -> Unit,
    onModelSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = modelName,
            onValueChange = onModelNameChange,
            label = { Text("模型名") },
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            CafeListRow(
                name = if (modelOptions.isEmpty()) "请先点击「获取模型列表」" else "从已获取模型中选择",
                meta = if (modelOptions.isEmpty()) "暂无模型数据"
                else "已加载 ${modelOptions.size} 个模型",
                showTopBorder = false,
                onClick = {
                    if (modelOptions.isNotEmpty()) {
                        onMenuOpenChange(!menuOpen)
                    }
                },
                trailing = {
                    if (modelOptions.isNotEmpty()) {
                        Text(
                            text = if (menuOpen) "▲" else "▼",
                            style = CafeType.bodySmall,
                            color = CafeColors.Muted
                        )
                    }
                }
            )
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { onMenuOpenChange(false) },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = ModelDropdownMaxHeight)
            ) {
                Column(modifier = Modifier.heightIn(max = ModelDropdownMaxHeight).verticalScroll(rememberScrollState())) {
                    modelOptions.forEach { id ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(CafeSpacing.xs)
                                ) {
                                    if (modelName == id) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = CafeColors.Accent2
                                        )
                                    }
                                    Text(
                                        text = id,
                                        color = if (modelName == id) CafeColors.Fg else CafeColors.Muted
                                    )
                                }
                            },
                            onClick = { onModelSelected(id) }
                        )
                    }
                }
            }
        }
    }
}