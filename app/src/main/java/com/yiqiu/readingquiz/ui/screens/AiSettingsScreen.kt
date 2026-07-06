package com.yiqiu.readingquiz.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.yiqiu.readingquiz.ai.ModelPresets
import com.yiqiu.readingquiz.ai.ReadingAiClient
import com.yiqiu.readingquiz.data.AiConfig
import com.yiqiu.readingquiz.data.ReadingRepository
import com.yiqiu.readingquiz.data.importexport.FileImporter
import com.yiqiu.readingquiz.ui.components.CafeCard
import com.yiqiu.readingquiz.ui.components.CafeGhostButton
import com.yiqiu.readingquiz.ui.components.CafePrimaryButton
import com.yiqiu.readingquiz.ui.theme.CafeColors
import com.yiqiu.readingquiz.ui.theme.CafeSpacing
import com.yiqiu.readingquiz.ui.theme.CafeType

/**
 * AI 设置页。
 *
 * 性能优化（修复「卡顿 + 无法下滑」两个问题）：
 * 1. 顶层用 LazyColumn 替代 Column，让超长内容可滚动
 * 2. 提取子 Composable（SectionPresetCard / StatusCard / ImportSection），避免每帧重建 30+ 嵌套节点
 * 3. forEach 加 key，便于 LazyColumn diff
 * 4. ExposedDropdownMenu 内层 LazyColumn → Column + Modifier.verticalScroll + heightIn（解决嵌套滚动冲突）
 * 5. remember(key1) 锁定 ReadingRepository.aiConfig.value 的初始读取，避免重组时回退
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(onBack: () -> Unit) {
    Log.d("Nav", "→ ai-settings")
    // 锁定初始值（避免 Composable 重组时回退到旧值）
    val initial = remember { ReadingRepository.aiConfig.value }

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

    // 导入文章（位于 AI 配置下方）
    val context = LocalContext.current
    var importMessage by remember { mutableStateOf<String?>(null) }
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            importMessage = "已取消导入。"
            return@rememberLauncherForActivityResult
        }
        // 真实处理：捕获所有异常，给出明确文案，避免崩溃
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
        // 前置校验：API Base URL 与 API Key 必填；空值直接给出明确提示，避免调用后报错
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
        when (val r = ReadingAiClient.fetchModels(cfg)) {
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

    val onTestConnection: () -> Unit = onTestConnection@{
        Log.d("AiSettings", "testConnection clicked")
        // 前置校验：缺密钥/URL/模型时直接提示
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
        status = try {
            when (val r = ReadingAiClient.testConnection(cfg)) {
                is ReadingAiClient.AiResult.Success -> {
                    statusIsError = false
                    r.value
                }
                is ReadingAiClient.AiResult.Failure -> {
                    statusIsError = true
                    "失败：${r.message}"
                }
            }
        } catch (e: Throwable) {
            statusIsError = true
            "失败：${e.message ?: "未知错误"}"
        }
        testing = false
    }

    val onSave: () -> Unit = onSave@{
        Log.d("AiSettings", "save clicked, model=$modelName")
        // 前置校验：保存时至少需要 baseUrl（密钥缺失时仍允许保存，方便用户分阶段配置）
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
        modifier = Modifier.fillMaxSize().background(CafeColors.Bg)
    ) {
        item(key = "top-bar") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(CafeSpacing.ContainerPad),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "返回",
                        tint = CafeColors.Fg
                    )
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Text(text = "AI 设置", style = CafeType.Heading, color = CafeColors.Fg)
            }
        }

        item(key = "preset-section-header") {
            SectionHeader("快速选择服务")
        }

        // 预设卡片（每个独立 item，便于 LazyColumn diff）
        items(ModelPresets.ALL, key = { it.id }) { preset ->
            PresetCard(preset = preset, onApply = { applyPreset(preset) })
        }

        item(key = "preset-tip") {
            TipText("提示：智谱清言与阶跃星辰均提供 OpenAI 兼容 API，可直接使用「获取模型列表」自动拉取完整模型清单。")
        }

        item(key = "input-base-url") {
            OutlinedTextField(
                value = baseUrl, onValueChange = { baseUrl = it },
                label = { Text("API Base URL") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = CafeSpacing.ContainerPad)
            )
        }
        item(key = "input-api-key") {
            OutlinedTextField(
                value = apiKey, onValueChange = { apiKey = it },
                label = { Text("API Key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = CafeSpacing.ContainerPad)
            )
        }

        item(key = "model-dropdown") {
            ModelDropdownField(
                modelName = modelName,
                onModelNameChange = { modelName = it },
                modelOptions = modelOptions,
                menuOpen = modelMenuOpen,
                onMenuOpenChange = { newState ->
                    // 仅在已有模型列表时响应展开；空列表强制折叠避免无内容弹出
                    if (modelOptions.isNotEmpty()) modelMenuOpen = newState
                    else modelMenuOpen = false
                },
                onModelSelected = { id ->
                    modelName = id
                    modelMenuOpen = false
                }
            )
        }

        item(key = "action-buttons") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = CafeSpacing.ContainerPad),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CafeGhostButton(
                    text = if (fetchingModels) "获取中..." else "获取模型列表",
                    onClick = onFetchModels,
                    enabled = !fetchingModels && !testing,
                    modifier = Modifier.weight(1f)
                )
                CafeGhostButton(
                    text = if (testing) "测试中..." else "测试连接",
                    onClick = onTestConnection,
                    enabled = !testing && !fetchingModels,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item(key = "input-timeout") {
            OutlinedTextField(
                value = timeout, onValueChange = { timeout = it.filter(Char::isDigit) },
                label = { Text("超时秒数") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = CafeSpacing.ContainerPad)
            )
        }

        item(key = "save-button") {
            CafePrimaryButton(
                text = "保存",
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().padding(horizontal = CafeSpacing.ContainerPad)
            )
        }

        item(key = "import-section-header") {
            SectionHeader("导入文章")
        }

        item(key = "import-tip") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = CafeSpacing.ContainerPad),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.FileDownload,
                    contentDescription = null,
                    tint = CafeColors.Accent
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "支持 JSON / Markdown / TXT 格式",
                    style = CafeType.Caption,
                    color = CafeColors.Muted
                )
            }
        }

        item(key = "import-button") {
            CafeGhostButton(
                text = "选择文件导入",
                onClick = onImportClick,
                modifier = Modifier.fillMaxWidth().padding(horizontal = CafeSpacing.ContainerPad)
            )
        }

        importMessage?.let { msg ->
            item(key = "import-msg-$msg") {
                Text(
                    text = msg,
                    style = CafeType.Caption,
                    color = if (msg.startsWith("已取消") || msg.startsWith("导入失败") || msg.startsWith("导入异常"))
                        CafeColors.Wrong else CafeColors.Accent2,
                    modifier = Modifier.padding(horizontal = CafeSpacing.ContainerPad)
                )
            }
        }

        status?.let {
            item(key = "status-card") {
                StatusCard(message = it, isError = statusIsError)
            }
        }

        item(key = "footer-tip") {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "提示：API Key 仅保存在本机，不会上传到任何远端服务。",
                style = CafeType.Caption,
                color = CafeColors.Muted,
                modifier = Modifier.padding(horizontal = CafeSpacing.ContainerPad)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============== 子 Composable（拆出来减少 AiSettingsScreen 的重组代价） ==============

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = CafeType.Heading,
        color = CafeColors.Fg,
        modifier = Modifier.padding(horizontal = CafeSpacing.ContainerPad, vertical = 8.dp)
    )
}

@Composable
private fun TipText(text: String) {
    Text(
        text = text,
        style = CafeType.Caption,
        color = CafeColors.Muted,
        modifier = Modifier.padding(horizontal = CafeSpacing.ContainerPad)
    )
}

@Composable
private fun PresetCard(
    preset: ModelPresets.Preset,
    onApply: () -> Unit
) {
    CafeCard(modifier = Modifier.fillMaxWidth().padding(horizontal = CafeSpacing.ContainerPad)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.displayName,
                    style = CafeType.Heading,
                    color = CafeColors.Fg
                )
                Text(
                    text = "默认模型：${preset.defaultModel}",
                    style = CafeType.Caption,
                    color = CafeColors.Muted
                )
                Text(
                    text = preset.baseUrl,
                    style = CafeType.Caption,
                    color = CafeColors.Muted
                )
            }
            CafeGhostButton(text = "应用", onClick = onApply)
        }
    }
}

@Composable
private fun StatusCard(message: String, isError: Boolean) {
    CafeCard(modifier = Modifier.fillMaxWidth().padding(horizontal = CafeSpacing.ContainerPad)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (isError) CafeColors.Wrong else CafeColors.Accent2
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text = message, style = CafeType.Body, color = CafeColors.Fg)
        }
    }
}

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
    ExposedDropdownMenuBox(
        expanded = menuOpen && modelOptions.isNotEmpty(),
        onExpandedChange = onMenuOpenChange,
        modifier = Modifier.padding(horizontal = CafeSpacing.ContainerPad)
    ) {
        OutlinedTextField(
            value = modelName,
            onValueChange = onModelNameChange,
            label = { Text("模型名") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { onMenuOpenChange(false) },
            modifier = Modifier.heightIn(max = 360.dp)
        ) {
            if (modelOptions.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("请先点击「获取模型列表」", color = CafeColors.Muted) },
                    onClick = { onMenuOpenChange(false) }
                )
            } else {
                // 用 Column + verticalScroll 替代嵌套 LazyColumn（避免测量冲突）
                Column(modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                    modelOptions.forEach { id ->
                        DropdownMenuItem(
                            text = { Text(id) },
                            onClick = { onModelSelected(id) }
                        )
                    }
                }
            }
        }
    }
}