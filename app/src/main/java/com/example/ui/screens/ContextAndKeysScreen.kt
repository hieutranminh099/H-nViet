package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.GlossaryEntryEntity
import com.example.data.model.ApiKeySlot
import com.example.data.model.ContextDomain
import com.example.data.model.KeyHealthStatus
import com.example.ui.StudioUiState
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

@Composable
fun ContextAndKeysScreen(
    uiState: StudioUiState,
    glossaryEntries: List<GlossaryEntryEntity>,
    onSelectDomain: (ContextDomain, Boolean) -> Unit,
    onSimulateFailover: (KeyHealthStatus) -> Unit,
    onResetKeysHealth: () -> Unit,
    onAddGlossary: (String, String, String, String) -> Unit,
    onDeleteGlossary: (Int) -> Unit,
    onClearSlidingWindow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cnTerm by remember { mutableStateOf("") }
    var vnTerm by remember { mutableStateOf("") }
    var noteTerm by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("context_and_keys_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Feature 2: Multi-Key Rotation Pool Dashboard (<1ms Failover on 429 or >450ms)
        item {
            MultiKeyPoolCard(
                uiState = uiState,
                onSimulateFailover = onSimulateFailover,
                onResetKeysHealth = onResetKeysHealth
            )
        }

        // 2. Sliding Context Window (Deque maxlen=6) & Auto-Context Profiler
        item {
            SlidingContextWindowCard(
                uiState = uiState,
                onSelectDomain = onSelectDomain,
                onClearSlidingWindow = onClearSlidingWindow
            )
        }

        // 3. Dynamic Glossary Injection Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Từ điển thuật ngữ cá nhân hóa (Glossary)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Chỉ đính kèm những thuật ngữ thực sự xuất hiện trong câu tiếng Trung trên màn hình vào System Prompt để tiết kiệm tối đa Token và giữ độ trễ < 450ms.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cnTerm,
                            onValueChange = { cnTerm = it },
                            label = { Text("Chữ Trung (VD: 灵石)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_glossary_cn")
                        )
                        OutlinedTextField(
                            value = vnTerm,
                            onValueChange = { vnTerm = it },
                            label = { Text("Nghĩa Việt (VD: Linh thạch)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_glossary_vn")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = noteTerm,
                            onValueChange = { noteTerm = it },
                            label = { Text("Ghi chú ngữ cảnh (Tùy chọn)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (cnTerm.isNotBlank() && vnTerm.isNotBlank()) {
                                    onAddGlossary(cnTerm, vnTerm, uiState.activeDomain.name, noteTerm)
                                    cnTerm = ""
                                    vnTerm = ""
                                    noteTerm = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TacticalJade,
                                contentColor = TextPrimary
                            ),
                            modifier = Modifier.testTag("btn_add_glossary")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Thêm", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        items(glossaryEntries, key = { it.id }) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${item.chineseTerm}  ➔  ${item.vietnameseTerm}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        if (item.notes.isNotBlank()) {
                            Text(
                                text = "${item.domainCategory} • ${item.notes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = { onDeleteGlossary(item.id) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa thuật ngữ",
                            tint = CrimsonAlert
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiKeyPoolCard(
    uiState: StudioUiState,
    onSimulateFailover: (KeyHealthStatus) -> Unit,
    onResetKeysHealth: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = ElectricCyan)
                    Text(
                        text = "Quản lý & Xoay vòng nhiều API Key (Chống nghẽn 429)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Round-Robin <1ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = TacticalJade
                )
            }

            Text(
                text = "Cấu hình Gemini API Key chính qua bảng Secrets trong AI Studio (BuildConfig.GEMINI_API_KEY). Pool tự động xoay vòng Round-Robin và chuyển đổi dự phòng tức thì (< 1ms) nếu gặp lỗi 429 Rate Limit hoặc phản hồi > 450ms.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Text(
                text = stringResource(R.string.security_prototype_warning),
                style = MaterialTheme.typography.labelSmall,
                color = ImperialGold
            )

            uiState.lastFailoverBanner?.let { banner ->
                Surface(
                    color = ImperialGold.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = banner,
                        style = MaterialTheme.typography.labelMedium,
                        color = ImperialGold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            uiState.keyPool.forEach { slot ->
                KeySlotRow(slot = slot)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSimulateFailover(KeyHealthStatus.COOLDOWN_429) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonAlert,
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_simulate_429_failover"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.SyncProblem, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Giả Lập 429 Failover", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = { onSimulateFailover(KeyHealthStatus.SLOW_FAILOVER) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImperialGold,
                        contentColor = ObsidianBg
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_simulate_slow_failover"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text("Giả Lập >450ms", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onResetKeysHealth,
                    modifier = Modifier.testTag("btn_reset_key_pool"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Key Pool", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun KeySlotRow(slot: ApiKeySlot) {
    val badgeColor = when (slot.status) {
        KeyHealthStatus.HEALTHY -> TacticalJade
        KeyHealthStatus.COOLDOWN_429 -> CrimsonAlert
        KeyHealthStatus.SLOW_FAILOVER -> ImperialGold
    }
    Surface(
        color = SlateSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = slot.label, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                Text(
                    text = "${slot.maskedKey} • Avg: ${"%.0f".format(slot.avgLatencyMs)}ms • Calls: ${slot.requestCount} • Failovers: ${slot.failoverCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Surface(
                color = badgeColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = slot.status.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SlidingContextWindowCard(
    uiState: StudioUiState,
    onSelectDomain: (ContextDomain, Boolean) -> Unit,
    onClearSlidingWindow: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = TacticalJade)
                    Text(
                        text = "SLIDING CONTEXT WINDOW (DEQUE MAXLEN=6)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TacticalJade
                    )
                }
                OutlinedButton(
                    onClick = onClearSlidingWindow,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Xóa Bộ Nhớ", style = MaterialTheme.typography.labelSmall)
                }
            }

            Text(
                text = "Quy tắc đại từ hiện tại (${uiState.activeDomain.displayName}): ${uiState.activeDomain.pronounRule}",
                style = MaterialTheme.typography.labelMedium,
                color = ElectricCyan
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ContextDomain.entries.forEach { dom ->
                    FilterChip(
                        selected = uiState.activeDomain == dom,
                        onClick = { onSelectDomain(dom, false) },
                        label = { Text(dom.badgeLabel, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            if (uiState.slidingContextWindow.isEmpty()) {
                Text(
                    text = "Hàng đợi ngữ cảnh đang trống. Quét khung hình hoặc chạy Benchmark để lưu 6 câu thoại gần nhất.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            } else {
                uiState.slidingContextWindow.forEachIndexed { idx, turn ->
                    Surface(
                        color = SlateSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "[Turn #${idx + 1}] ${turn.sourceChinese}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "➔ ${turn.translatedVietnamese}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
