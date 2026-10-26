package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.source.NativeCodeCatalog
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderStrong
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SoftIndigo
import com.example.ui.theme.TacticalJade
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Feature 5: Native Code & GitHub Actions APK Builder Exporter Tab
 * Provides full, copyable, and exportable configuration files for:
 * - GitHub Actions APK CI/CD (.github/workflows/android-build.yml)
 * - Capacitor Configuration (capacitor.config.json, capacitor.config.ts, package.json)
 * - Step-by-step GitHub Push & Actions Artifact APK Download Guide
 * - PC & Android Native Lossless Video Merger / HUD Translator Code
 */
@Composable
fun NativeCodeExporterScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val files = NativeCodeCatalog.files
    var selectedFile by remember { mutableStateOf(files.first()) }
    var copiedNotice by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("native_code_exporter_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1: Quick Step-by-Step GitHub Actions & Capacitor APK Download Guide
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
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
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Tự động đóng gói APK trên GitHub Actions",
                                tint = PrimaryBlue
                            )
                            Column {
                                Text(
                                    text = "Tự động đóng gói file .APK trên GitHub Actions",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Đã tạo sẵn capacitor.config.json, package.json & .github/workflows/android-build.yml",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SoftIndigo
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("Git Push Commands", NativeCodeCatalog.GIT_PUSH_QUICK_COMMANDS)
                                )
                                copiedNotice = "Đã sao chép cụm lệnh Git Push vào bộ nhớ tạm!"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_copy_git_push_commands")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Chép lệnh Git", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Surface(
                        color = SlateSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SlateBorderStrong),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "1. Đẩy code lên GitHub: Nhấn nút 'Push to GitHub' trên thanh công cụ AI Studio hoặc chạy cụm lệnh git push.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            Text(
                                text = "2. Mở tab Actions: Trên trang GitHub Repository, bấm vào tab 'Actions' ở thanh menu ngang phía trên cùng, sau đó bấm vào dòng 'Build Android APK (Capacitor + Native Gradle)' có dấu tích xanh.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            Text(
                                text = "3. Vị trí nút tải file .APK: Cuộn xuống cuối trang kết quả (Summary) tới mục 'Artifacts' -> Bấm vào 'HanViet-Studio-Debug-APK' để tải file .zip chứa app-debug.apk về cài đặt ngay.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TacticalJade,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Card 2: File Selector Chips
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderStrong, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = PrimaryBlue)
                        Text(
                            text = "Danh mục file cấu hình Capacitor, GitHub Actions & Mã nguồn",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Chọn từng file bên dưới để xem chi tiết hoặc sao chép trực tiếp:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        files.forEach { fileItem ->
                            val isSelected = fileItem.id == selectedFile.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedFile = fileItem
                                    copiedNotice = null
                                },
                                label = {
                                    Text(fileItem.fileName, style = MaterialTheme.typography.labelMedium)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue.copy(alpha = 0.22f),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("code_tab_${fileItem.id}")
                            )
                        }
                    }
                }
            }
        }

        // Card 3: Code Viewer & Copy/Share Actions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedFile.fileName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${selectedFile.platformBadge} • ${selectedFile.language}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ImperialGold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText(selectedFile.fileName, selectedFile.code)
                                    )
                                    copiedNotice = "Đã sao chép toàn bộ ${selectedFile.fileName} vào bộ nhớ tạm!"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_copy_native_code")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Sao chép", style = MaterialTheme.typography.labelMedium)
                            }

                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, selectedFile.fileName)
                                        putExtra(Intent.EXTRA_TEXT, selectedFile.code)
                                    }
                                    runCatching {
                                        context.startActivity(Intent.createChooser(shareIntent, "Xuất file ${selectedFile.fileName}"))
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_share_native_code")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Chia sẻ mã nguồn", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    copiedNotice?.let { msg ->
                        Surface(
                            color = TacticalJade.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.labelMedium,
                                color = TacticalJade,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Text(
                        text = selectedFile.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Surface(
                        color = SlateSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SlateBorder, RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = selectedFile.code,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            color = ElectricCyan,
                            modifier = Modifier
                                .padding(12.dp)
                                .horizontalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}
