package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.CloudVaultProjectEntity
import com.example.data.model.AiAccountProfile
import com.example.data.model.MobileMemoryTelemetry
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TacticalJade
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun CloudVaultWorkspace(
    activeAccount: AiAccountProfile,
    vaultProjects: List<CloudVaultProjectEntity>,
    mobileMemoryTelemetry: MobileMemoryTelemetry,
    lastSyncedCode: String,
    statusMessage: String,
    onSaveCurrentToVault: (String) -> Unit,
    onSyncBy6DigitCode: (String) -> Unit,
    onImportHvCloudJson: (String) -> Unit,
    onStreamVaultProject: (CloudVaultProjectEntity) -> Unit,
    onResumeEditVaultProject: (CloudVaultProjectEntity) -> Unit,
    onDeleteVaultProject: (String) -> Unit,
    onReleaseMobileRam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var customProjectTitle by remember { mutableStateOf("") }
    var syncCodeInput by remember { mutableStateOf(lastSyncedCode) }
    var showBundleImportBox by remember { mutableStateOf(false) }
    var bundleJsonInput by remember { mutableStateOf("") }
    var localClipboardFeedback by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("cloud_vault_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Unified Gemini Cloud Storage Header (PC Emulator <-> Android Phone)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TacticalJade.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = TacticalJade,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Kho lưu trữ đám mây Gemini (PC ⇄ Điện thoại)",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Tài khoản đồng bộ: ${activeAccount.emailOrHandle} • ${activeAccount.provider.badge}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyan
                                )
                            }
                        }

                        Surface(
                            color = ImperialGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Mã: $lastSyncedCode",
                                style = MaterialTheme.typography.labelMedium,
                                color = ImperialGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Lưu toàn bộ Video đã dịch/gộp, File phụ đề (.ASS/.SRT), Tọa độ/Kích cỡ kéo-thả (X/Y) và Từ điển ngữ cảnh từ Giả lập PC lên Kho Lưu Trữ. Khi mở trên Điện thoại, bạn có thể Xem trực tiếp (Stream), Chỉnh sửa tiếp hoặc Tải về máy ngay lập tức.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customProjectTitle,
                            onValueChange = { customProjectTitle = it },
                            label = { Text("Tên dự án lưu lên Cloud (Tùy chọn)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_cloud_project_title")
                        )

                        Button(
                            onClick = {
                                onSaveCurrentToVault(customProjectTitle)
                                customProjectTitle = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TacticalJade,
                                contentColor = TextPrimary
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("btn_save_to_cloud_vault")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Lưu vào kho", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    // 6-Digit Instant Sync Code & .hvcloud Bundle Pull Bar
                    Surface(
                        color = SlateSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.QrCode2, contentDescription = null, tint = ImperialGold, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Đồng bộ nhanh giữa PC & Điện thoại qua mã 6 số:",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextPrimary
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showBundleImportBox = !showBundleImportBox },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (showBundleImportBox) "Ẩn .hvcloud" else "Dán gói .hvcloud",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = syncCodeInput,
                                    onValueChange = { syncCodeInput = it },
                                    label = { Text("Mã 6 số từ PC (VD: 849-206 hoặc 512-904)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_sync_code_6digit")
                                )

                                Button(
                                    onClick = { onSyncBy6DigitCode(syncCodeInput) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricCyan,
                                        contentColor = TextPrimary
                                    ),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("btn_sync_by_6digit_code")
                                ) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Đồng bộ ngay", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            AnimatedVisibility(visible = showBundleImportBox) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = bundleJsonInput,
                                        onValueChange = { bundleJsonInput = it },
                                        label = { Text("Dán nội dung gói JSON .hvcloud từ thiết bị khác") },
                                        maxLines = 4,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            onImportHvCloudJson(bundleJsonInput)
                                            bundleJsonInput = ""
                                            showBundleImportBox = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ImperialGold,
                                            contentColor = ObsidianBg
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Khôi Phục Từ Gói .hvcloud", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalJade
                    )

                    localClipboardFeedback?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelSmall,
                            color = ImperialGold
                        )
                    }
                }
            }
        }

        // 2. Mobile Hardware RAM & Chunked Stream Guard Card
        item {
            MobileHardwareRamGuardCard(
                telemetry = mobileMemoryTelemetry,
                onReleaseMobileRam = onReleaseMobileRam
            )
        }

        // 3. Section Header for Synced Cloud Vault Projects
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dự án đã đồng bộ trên Cloud (${vaultProjects.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Đồng bộ tự động PC ⇄ Android",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan
                )
            }
        }

        // 4. Synced Cloud Vault Project Cards
        items(vaultProjects, key = { it.projectId }) { project ->
            CloudVaultProjectCard(
                project = project,
                onStream = { onStreamVaultProject(project) },
                onResumeEdit = { onResumeEditVaultProject(project) },
                onCopyAss = {
                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cb.setPrimaryClip(ClipData.newPlainText("${project.projectTitle}.ass", project.assSubtitleContent))
                    localClipboardFeedback = "Đã sao chép phụ đề .ASS của '${project.projectTitle}' vào bộ nhớ tạm!"
                },
                onShareBundle = {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "${project.projectTitle} (Mã Cloud: ${project.syncCode6Digit})")
                        putExtra(Intent.EXTRA_TEXT, project.hvCloudBundleJson)
                    }
                    runCatching {
                        context.startActivity(Intent.createChooser(share, "Chia sẻ gói đồng bộ .hvcloud"))
                    }
                },
                onDelete = { onDeleteVaultProject(project.projectId) }
            )
        }
    }
}

@Composable
private fun MobileHardwareRamGuardCard(
    telemetry: MobileMemoryTelemetry,
    onReleaseMobileRam: () -> Unit
) {
    val usageRatio = (telemetry.allocatedRamMb / telemetry.maxSafeRamBudgetMb)
        .toFloat()
        .coerceIn(0.05f, 1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
            .testTag("mobile_ram_guard_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Bảo vệ RAM điện thoại & Chunked Stream",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = TacticalJade.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "An toàn 100%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalJade,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { usageRatio },
                color = TacticalJade,
                trackColor = SlateSurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Đệm luồng Chunk (${"%.1f".format(telemetry.chunkStreamSizeMb)}MB): ${"%.1f".format(telemetry.allocatedRamMb)}MB / ${"%.0f".format(telemetry.maxSafeRamBudgetMb)}MB",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Đã thu hồi ${telemetry.revokedObjectUriCount} ObjectURL/Bitmap • Hỗ trợ mượt 10–20 tập video",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                OutlinedButton(
                    onClick = onReleaseMobileRam,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("btn_release_mobile_ram")
                ) {
                    Text("Giải phóng đệm", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun CloudVaultProjectCard(
    project: CloudVaultProjectEntity,
    onStream: () -> Unit,
    onResumeEdit: () -> Unit,
    onCopyAss: () -> Unit,
    onShareBundle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
            .testTag("vault_project_card_${project.syncCode6Digit}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.projectTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhonelinkSetup,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Nguồn: ${project.sourceDeviceBadge} • ${project.geminiAccountEmail}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                }

                Surface(
                    color = ImperialGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Mã: ${project.syncCode6Digit}",
                        style = MaterialTheme.typography.labelMedium,
                        color = ImperialGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Surface(
                color = SlateSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${project.clipCount} tập phim (${project.resolution}) • Tổng thời lượng: ${"%.1f".format(project.totalDurationMs / 1000.0)}s • Font: ${project.fontFamilyName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalJade
                    )
                    Text(
                        text = "Vị trí phụ đề đã căn chỉnh: X=${(project.normX * 100).roundToInt()}%, Y=${(project.normY * 100).roundToInt()}% • Cỡ chữ ${"%.1f".format(project.fontSizeSp)}sp",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "Ngữ cảnh (${project.glossaryTermCount} thuật ngữ): ${project.contextHistorySummary}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // Action Buttons (Stream Directly, Resume Editing, Export .ASS/.hvcloud, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onStream,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalJade,
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1.15f)
                        .minimumInteractiveComponentSize()
                        .testTag("btn_vault_stream_${project.syncCode6Digit}"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PlayCircleFilled, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Xem ngay", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = onResumeEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1.15f)
                        .minimumInteractiveComponentSize()
                        .testTag("btn_vault_edit_${project.syncCode6Digit}"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sửa tiếp", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onCopyAss,
                    modifier = Modifier
                        .weight(0.95f)
                        .minimumInteractiveComponentSize()
                        .testTag("btn_vault_download_${project.syncCode6Digit}"),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Tải .ASS", style = MaterialTheme.typography.labelSmall)
                }

                IconButton(
                    onClick = onShareBundle,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Chia sẻ gói đồng bộ .hvcloud",
                        tint = ImperialGold
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("btn_vault_delete_${project.syncCode6Digit}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa dự án khỏi Cloud Vault",
                        tint = CrimsonAlert
                    )
                }
            }
        }
    }
}
