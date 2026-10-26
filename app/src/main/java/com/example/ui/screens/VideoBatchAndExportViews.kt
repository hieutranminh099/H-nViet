package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AiAccountProfile
import com.example.data.model.LosslessExportReport
import com.example.data.model.ShortVideoClipItem
import com.example.data.model.SubtitleStyleConfig
import com.example.data.model.VideoExportMode
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SlateBorderStrong
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SoftIndigo
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

/**
 * Sub-Tab 2: AI Plot & Scene Sequencer + Smart Batch Merger
 */
@Composable
fun SmartBatchMergerWorkspace(
    clips: List<ShortVideoClipItem>,
    activeAccount: AiAccountProfile,
    isAiSorted: Boolean,
    trimOverlapEnabled: Boolean,
    onToggleTrimOverlap: (Boolean) -> Unit,
    onRunAiPlotSequencer: () -> Unit,
    onResetScrambled: () -> Unit,
    onLaunchVideoPicker: () -> Unit,
    onProceedToExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalRawSec = clips.sumOf { it.durationMs } / 1000.0
    val totalTrimmedSec = clips.sumOf {
        if (trimOverlapEnabled) (it.detectedOverlapStartMs + it.detectedOverlapEndMs) else 0L
    } / 1000.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("smart_batch_merger_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderStrong, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI sắp xếp cốt truyện & gộp video ngắn",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Surface(
                            color = if (isAiSorted) EmeraldSuccess.copy(alpha = 0.16f) else ImperialGold.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isAiSorted) "Đã xếp Tập 1 ➔ ${clips.size}" else "Thứ tự lộn xộn",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAiSorted) EmeraldSuccess else ImperialGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Tự động phân tích câu thoại đầu/cuối và số tập bằng ${activeAccount.provider.displayName} (${activeAccount.selectedModel}) để sắp xếp đúng mạch truyện từ Tập 1 đến Tập cuối.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    // Overlap Trimmer Switch (1-3s Intro/Outro deduplication)
                    Surface(
                        color = ObsidianBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SlateBorderStrong, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ContentCut, contentDescription = null, tint = SoftIndigo)
                                Column {
                                    Text(
                                        text = "Tự động cắt đoạn trùng lặp đầu/cuối tập (1–3s)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Thời lượng gốc: ${"%.1f".format(totalRawSec)}s ➔ Đã cắt bỏ ${"%.1f".format(totalTrimmedSec)}s bị lặp",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SoftIndigo
                                    )
                                }
                            }
                            Switch(
                                checked = trimOverlapEnabled,
                                onCheckedChange = onToggleTrimOverlap
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRunAiPlotSequencer,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.25f)
                                .testTag("btn_ai_sort_plot")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("AI xếp cốt truyện", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = onResetScrambled,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.75f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Xáo lại", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = onLaunchVideoPicker,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.75f)
                        ) {
                            Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Thêm clip", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        items(clips, key = { it.clipId }) { clip ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isAiSorted) PrimaryBlue.copy(alpha = 0.55f) else SlateBorderStrong,
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tập #${clip.aiAssignedOrder} • ${clip.fileName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Surface(
                            color = SlateSurfaceElevated,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = clip.cornerBadgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftIndigo,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "${clip.seriesTopic} • ${clip.resolution} • Thời lượng sau cắt lặp: ${"%.1f".format(clip.effectiveDurationAfterTrimMs / 1000.0)}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )

                    Text(
                        text = "Mở đầu: \"${clip.openingChineseLine}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "Kết tập: \"${clip.closingChineseLine}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Surface(
                        color = ObsidianBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = clip.aiSequencingReason,
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onProceedToExport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_proceed_lossless_export")
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Chuyển sang ghép nối & xuất video Lossless",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * Sub-Tab 3: Lossless / Zero-Degradation Video & Subtitle Export Center
 */
@Composable
fun LosslessExportWorkspace(
    clips: List<ShortVideoClipItem>,
    styleConfig: SubtitleStyleConfig,
    selectedMode: VideoExportMode,
    onSelectMode: (VideoExportMode) -> Unit,
    exportReport: LosslessExportReport?,
    onRunExport: () -> Unit,
    onSaveToCloudVault: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copyFeedback by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("lossless_export_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Chọn chế độ xuất video không giảm chất lượng",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        items(VideoExportMode.entries) { mode ->
            val selected = mode == selectedMode
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) SlateSurfaceElevated else SlateSurface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) PrimaryBlue else SlateBorderStrong,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelectMode(mode) }
                    .testTag("export_mode_${mode.name}")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mode.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selected) PrimaryBlue else TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = PrimaryBlue.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = mode.badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = mode.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRunExport,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.25f)
                        .testTag("btn_execute_lossless_export")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Kết xuất (${clips.size} tập • Nền ${(styleConfig.maskOpacity * 100).roundToInt()}%)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = onSaveToCloudVault,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftIndigo,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(0.95f)
                        .testTag("btn_export_save_cloud_vault")
                ) {
                    Text(
                        text = "Lưu Kho Cloud",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        exportReport?.let { report ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorderStrong, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                                Text(
                                    text = "Sẵn sàng tải về (${"%.1f".format(report.executionTimeMs)}ms)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "${report.preservedBitrateKbps} kbps • ${report.preservedResolution}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftIndigo
                            )
                        }

                        Text(
                            text = report.pronounSyncSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Text(
                            text = "File đầu ra: ${report.outputVideoFileName} • Thời lượng gộp: ${"%.1f".format(report.totalDurationMs / 1000.0)}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryBlue
                        )

                        copyFeedback?.let { msg ->
                            Text(text = msg, style = MaterialTheme.typography.labelMedium, color = EmeraldSuccess)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cb.setPrimaryClip(ClipData.newPlainText(report.outputAssFileName, report.generatedAssPreview))
                                    copyFeedback = "Đã sao chép nội dung file ${report.outputAssFileName}!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Sao chép .ASS", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cb.setPrimaryClip(ClipData.newPlainText(report.outputSrtFileName, report.generatedSrtPreview))
                                    copyFeedback = "Đã sao chép nội dung file ${report.outputSrtFileName}!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftIndigo, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Sao chép .SRT", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, report.outputAssFileName)
                                        putExtra(Intent.EXTRA_TEXT, report.generatedAssPreview)
                                    }
                                    runCatching { context.startActivity(Intent.createChooser(share, "Xuất phụ đề .ASS")) }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Chia sẻ file phụ đề", modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(
                            text = "Lệnh ghép nối Lossless thực thi:",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Surface(
                            color = ObsidianBg,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SlateBorderStrong, RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = report.ffmpegOrMuxerCommand,
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Text(
                            text = "Nội dung phụ đề .ASS (đã nhúng tọa độ kéo-thả & hộp che nền chữ gốc):",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Surface(
                            color = ObsidianBg,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SlateBorderStrong, RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = report.generatedAssPreview,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .horizontalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sub-Tab 5: Gemini & ChatGPT Multi-Account Auth Hub
 */
@Composable
fun GeminiChatGptAuthHubWorkspace(
    accounts: List<AiAccountProfile>,
    activeAccount: AiAccountProfile,
    onSelectAccount: (String) -> Unit,
    onSelectModel: (String, String) -> Unit,
    onToggleOAuth: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("auth_hub_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderStrong, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tài khoản AI (Google Gemini & OpenAI ChatGPT)",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Liên kết tài khoản Gemini hoặc ChatGPT để dịch phụ đề và đồng bộ chung Bộ nhớ ngữ cảnh cùng Từ điển thuật ngữ xuyên suốt mọi tập phim.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        items(accounts, key = { it.id }) { acc ->
            val isActive = acc.id == activeAccount.id
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) SlateSurfaceElevated else SlateSurface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isActive) 1.5.dp else 1.dp,
                        color = if (isActive) PrimaryBlue else SlateBorderStrong,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelectAccount(acc.id) }
                    .testTag("auth_account_${acc.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${acc.provider.displayName} • ${acc.displayName}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "${acc.emailOrHandle} • ${acc.authMode.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftIndigo
                            )
                        }

                        OutlinedButton(
                            onClick = { onToggleOAuth(acc.id) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (acc.isConnected) "Đã kết nối" else "Kết nối OAuth",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Text(
                        text = "Chọn mô hình dịch & phân tích cốt truyện:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        acc.provider.availableModels.forEach { modelId ->
                            FilterChip(
                                selected = acc.selectedModel == modelId,
                                onClick = {
                                    onSelectAccount(acc.id)
                                    onSelectModel(acc.id, modelId)
                                },
                                label = { Text(modelId, style = MaterialTheme.typography.labelMedium) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Trạng thái: ${acc.quotaStatus}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "✓ Đồng bộ từ điển & ngữ cảnh",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }
    }
}
