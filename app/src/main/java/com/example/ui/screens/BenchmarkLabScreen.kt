package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.TranslationCacheEntity
import com.example.data.model.BenchmarkStepResult
import com.example.ui.StudioUiState
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TacticalJade
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BenchmarkLabScreen(
    uiState: StudioUiState,
    cachedTemplates: List<TranslationCacheEntity>,
    onRunBenchmark: () -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("benchmark_lab_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & Run Closed-Loop Benchmark Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TacticalJade.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = TacticalJade,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Kiểm thử hiệu năng toàn trình (< 1000ms)",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                        }
                        Surface(
                            color = ElectricCyan.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Mẫu lưu: ${cachedTemplates.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = uiState.benchmarkSummaryNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onRunBenchmark,
                            enabled = !uiState.isBenchmarkRunning,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TacticalJade,
                                contentColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("btn_run_benchmark_suite")
                        ) {
                            if (uiState.isBenchmarkRunning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = TextPrimary
                                )
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isBenchmarkRunning) "Đang đo độ trễ..." else "Chạy đo hiệu năng",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = onClearCache,
                            modifier = Modifier
                                .weight(0.7f)
                                .testTag("btn_clear_sqlite_cache")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Xóa đệm", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 2. Benchmark Step Results
        if (uiState.benchmarkResults.isNotEmpty()) {
            item {
                Text(
                    text = "Kết quả đo lường từng khâu",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            items(uiState.benchmarkResults, key = { it.stepName }) { step ->
                BenchmarkResultStepCard(step = step)
            }
        }

        // 3. Parameterized SQLite Template Cache Table Inspector
        item {
            Text(
                text = "Bộ nhớ đệm mẫu câu số liệu ({0}, {1})",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        if (cachedTemplates.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Chưa có mẫu Template nào trong SQLite. Nhấn 'Chạy Benchmark Toàn Trình' để tự động nạp và kiểm thử.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(cachedTemplates, key = { it.cacheKey }) { entity ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Domain: ${entity.domainName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan
                            )
                            Text(
                                text = "Hits: ${entity.hitCount}x (<0.8ms)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TacticalJade
                            )
                        }
                        Text(
                            text = "CN Template: ${entity.templatePattern}",
                            style = MaterialTheme.typography.labelMedium,
                            color = ImperialGold
                        )
                        Text(
                            text = "VN Template: ${entity.vietnameseTemplate}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BenchmarkResultStepCard(step: BenchmarkStepResult) {
    val isUnderOneSec = step.totalMs < 1000.0
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TacticalJade.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = step.stepName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = TacticalJade.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TacticalJade,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${"%.1f".format(step.totalMs)}ms ${if (isUnderOneSec) "< 1s PASS" else "SLOW"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TacticalJade
                        )
                    }
                }
            }

            // Stage latency breakdown bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateSurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cap: ${"%.1f".format(step.captureMs)}ms", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text("Diff: ${"%.1f".format(step.diffMs)}ms", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text("OCR: ${"%.1f".format(step.ocrMs)}ms", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                Text("AI/Cache: ${"%.2f".format(step.cacheOrAiMs)}ms", style = MaterialTheme.typography.labelSmall, color = ImperialGold)
            }

            Text(
                text = "Gốc: ${step.sourceText}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "Dịch: ${step.finalTranslation}",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = step.cacheHitType,
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan
                )
                Text(
                    text = if (step.passedPronounContext) "✓ Chuẩn đại từ & ngữ cảnh" else "✓ Đã kiểm tra",
                    style = MaterialTheme.typography.labelSmall,
                    color = TacticalJade
                )
            }
        }
    }
}
