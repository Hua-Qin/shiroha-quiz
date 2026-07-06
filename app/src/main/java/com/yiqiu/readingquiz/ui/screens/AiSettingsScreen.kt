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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(onBack: () -> Unit) {
    Log.d("Nav", "→ ai-settings")
    val initial = ReadingRepository.aiConfig.value
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

    // Task 6：导入文章按钮（位于 AI 配置下方）
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
            when (val r = FileImporter.importFromUri(context, uri)) {
                is FileImporter.Result.Success -> "已导入：${r.article.title}"
                is FileImporter.Result.Failure -> "导入失败：${r.reason}"
            }
        } catch (e: Throwable) {
            "导入异常：${e.message ?: "未知错误"}"
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(CafeColors.Bg)) {
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
        Column(
            modifier = Modifier.padding(horizontal = CafeSpacing.ContainerPad),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = baseUrl, onValueChange = { baseUrl = it },
                label = { Text("API Base URL") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = apiKey, onValueChange = { apiKey = it },
                label = { Text("API Key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            // 模型选择：使用官方 ExposedDropdownMenuBox（可手动输入也可下拉）
            ExposedDropdownMenuBox(
                expanded = modelMenuOpen && modelOptions.isNotEmpty(),
                onExpandedChange = {
                    if (modelOptions.isNotEmpty()) modelMenuOpen = !modelMenuOpen
                }
            ) {
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("模型名") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelMenuOpen)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = modelMenuOpen,
                    onDismissRequest = { modelMenuOpen = false },
                    modifier = Modifier.heightIn(max = 360.dp)
                ) {
                    if (modelOptions.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("请先点击「获取模型列表」", color = CafeColors.Muted) },
                            onClick = { modelMenuOpen = false }
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                            items(modelOptions) { id ->
                                DropdownMenuItem(
                                    text = { Text(id) },
                                    onClick = {
                                        modelName = id
                                        modelMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CafeGhostButton(
                    text = if (fetchingModels) "获取中..." else "获取模型列表",
                    onClick = {
                        Log.d("AiSettings", "fetchModels clicked")
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
                                status = "已获取 ${r.value.size} 个模型，请从下拉选择。"
                                statusIsError = false
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
                    },
                    enabled = !fetchingModels && !testing,
                    modifier = Modifier.weight(1f)
                )
                CafeGhostButton(
                    text = if (testing) "测试中..." else "测试连接",
                    onClick = {
                        Log.d("AiSettings", "testConnection clicked")
                        testing = true
                        status = null
                        statusIsError = false
                        val cfg = AiConfig(
                            apiBaseUrl = baseUrl,
                            apiKey = apiKey,
                            modelName = modelName,
                            timeoutSeconds = timeout.toIntOrNull() ?: 60
                        )
                        // 入口 try/catch 兜底：即使 AiClient 内部异常也走状态栏
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
                    },
                    enabled = !testing && !fetchingModels,
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = timeout, onValueChange = { timeout = it.filter(Char::isDigit) },
                label = { Text("超时秒数") },
                modifier = Modifier.fillMaxWidth()
            )
            CafePrimaryButton(
                text = "保存",
                onClick = {
                    Log.d("AiSettings", "save clicked, model=$modelName")
                    ReadingRepository.updateAiConfig(
                        AiConfig(
                            apiBaseUrl = baseUrl,
                            apiKey = apiKey,
                            modelName = modelName,
                            timeoutSeconds = timeout.toIntOrNull() ?: 60
                        )
                    )
                    status = "已保存。"
                    statusIsError = false
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Task 6：导入文章区段（位于 AI 配置下方）
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "导入文章",
                style = CafeType.Heading,
                color = CafeColors.Fg
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
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
            CafeGhostButton(
                text = "选择文件导入",
                onClick = {
                    Log.d("FileImport", "user clicked importFile")
                    openDocumentLauncher.launch(
                        arrayOf(
                            "application/json",
                            "text/markdown",
                            "text/plain",
                            "text/*"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            importMessage?.let { msg ->
                Text(
                    text = msg,
                    style = CafeType.Caption,
                    color = if (msg.startsWith("已取消") || msg.startsWith("导入失败") || msg.startsWith("导入异常"))
                        CafeColors.Wrong else CafeColors.Accent2
                )
            }

            status?.let {
                CafeCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = if (statusIsError) CafeColors.Wrong else CafeColors.Accent2
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(text = it, style = CafeType.Body, color = CafeColors.Fg)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "提示：API Key 仅保存在本机，不会上传到任何远端服务。",
                style = CafeType.Caption,
                color = CafeColors.Muted
            )
        }
    }
}