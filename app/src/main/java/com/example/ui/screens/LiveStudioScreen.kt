package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LatencyTelemetry
import com.example.data.model.RenderStage
import com.example.data.model.ScanMode
import com.example.data.model.TranslatedBoxState
import com.example.ui.StudioUiState
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderStrong
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SoftIndigo
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun LiveStudioScreen(
    uiState: StudioUiState,
    isOverlayRunning: Boolean,
    onSelectScene: (String) -> Unit,
    onSetScanMode: (ScanMode) -> Unit,
    onUpdateLoupe: (Float, Float) -> Unit,
    onSetHoldToPeek: (Boolean) -> Unit,
    onRandomizeNumbers: () -> Unit,
    onRetranslateCold: () -> Unit,
    onRetranslateWarm: () -> Unit,
    onToggleInpainting: (Boolean) -> Unit,
    onToggleOverlayService: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var liveBackgroundOpacity by remember { mutableFloatStateOf(0.92f) }
    var isSheetExpanded by remember { mutableStateOf(true) }
    var activeSheetTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("live_studio_list")
    ) {
        // 1. Minimalist Swipeable Scene Selector & Latency Pill Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                uiState.scenes.forEach { scene ->
                    val selected = scene.id == uiState.selectedScene.id
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectScene(scene.id) },
                        label = {
                            Text(
                                text = scene.title,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.22f),
                            selectedLabelColor = PrimaryBlue
                        ),
                        modifier = Modifier.testTag("scene_chip_${scene.id}")
                    )
                }
            }

            // Compact Latency HUD Badge
            Surface(
                color = SlateSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .border(1.dp, SlateBorderStrong, RoundedCornerShape(12.dp))
                    .testTag("realtime_latency_hud")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (uiState.telemetry.isUnderOneSecondSla) EmeraldSuccess else CrimsonAlert)
                    )
                    Text(
                        text = "${"%.0f".format(uiState.telemetry.totalEndToEndMs)}ms",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                }
            }
        }

        // 2. Top Half: Pinned Interactive Game Overlay Viewport with Smart Solid Masking (Zero Overlap)
        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
            InteractiveGameOverlayCanvas(
                uiState = uiState,
                backgroundOpacity = liveBackgroundOpacity,
                isCompactHeight = isSheetExpanded,
                onUpdateLoupe = onUpdateLoupe,
                onSetHoldToPeek = onSetHoldToPeek
            )
        }

        // 3. Bottom Half: One-Hand Friendly Collapsible Control Drawer
        Surface(
            color = SlateSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 6.dp)
                .border(
                    width = 1.dp,
                    color = SlateBorderStrong,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Sheet Handle & Tab Switcher
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
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
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(SlateBorderStrong)
                            )
                            Text(
                                text = "Điều khiển dịch màn hình trực tiếp",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = { isSheetExpanded = !isSheetExpanded },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = if (isSheetExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = "Thu gọn ngăn kéo",
                                tint = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianBg)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "Điều khiển & Nền che (${(liveBackgroundOpacity * 100).roundToInt()}%)",
                            "Chi tiết câu dịch (${uiState.translatedBoxes.size})"
                        ).forEachIndexed { idx, title ->
                            val selected = activeSheetTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (selected) SlateSurfaceElevated else Color.Transparent)
                                    .clickable { activeSheetTab = idx }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) TextPrimary else TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activeSheetTab == 0) {
                        item {
                            CombatQuickControlsCard(
                                uiState = uiState,
                                isOverlayRunning = isOverlayRunning,
                                backgroundOpacity = liveBackgroundOpacity,
                                onBackgroundOpacityChange = { liveBackgroundOpacity = it },
                                onSetScanMode = onSetScanMode,
                                onRandomizeNumbers = onRandomizeNumbers,
                                onRetranslateCold = onRetranslateCold,
                                onRetranslateWarm = onRetranslateWarm,
                                onToggleInpainting = onToggleInpainting,
                                onToggleOverlayService = {
                                    if (!Settings.canDrawOverlays(context)) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        runCatching { context.startActivity(intent) }
                                    } else {
                                        onToggleOverlayService()
                                    }
                                }
                            )
                        }
                    } else {
                        items(uiState.translatedBoxes, key = { it.box.id }) { boxState ->
                            TranslatedBoxDetailCard(boxState = boxState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveGameOverlayCanvas(
    uiState: StudioUiState,
    backgroundOpacity: Float,
    isCompactHeight: Boolean,
    onUpdateLoupe: (Float, Float) -> Unit,
    onSetHoldToPeek: (Boolean) -> Unit
) {
    val scene = uiState.selectedScene
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorderStrong, RoundedCornerShape(18.dp))
    ) {
        Column {
            // Scene Header Bar with Hold-to-Peek Trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = scene.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${scene.subtitle} • Nền che ${(backgroundOpacity * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Feature 4: Hold-to-Peek Interactive Trigger Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (uiState.isHoldToPeekActive) ImperialGold else SlateSurfaceElevated
                        )
                        .border(1.dp, SlateBorderStrong, RoundedCornerShape(10.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onSetHoldToPeek(true)
                                    tryAwaitRelease()
                                    onSetHoldToPeek(false)
                                }
                            )
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("hold_to_peek_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Giữ để xem bản gốc",
                            tint = if (uiState.isHoldToPeekActive) ObsidianBg else TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (uiState.isHoldToPeekActive) "Đang xem gốc" else "Giữ: Xem gốc",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.isHoldToPeekActive) ObsidianBg else TextSecondary
                        )
                    }
                }
            }

            // Simulated Game Frame Viewport
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isCompactHeight) 225.dp else 305.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(scene.backgroundGradientStart),
                                Color(scene.backgroundGradientEnd)
                            )
                        )
                    )
                    .pointerInput(uiState.scanMode) {
                        if (uiState.scanMode == ScanMode.TAP_LOUPE) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val nx = (change.position.x / size.width.toFloat()).coerceIn(0.05f, 0.95f)
                                val ny = (change.position.y / size.height.toFloat()).coerceIn(0.05f, 0.95f)
                                onUpdateLoupe(nx, ny)
                            }
                        }
                    }
                    .testTag("game_overlay_canvas")
            ) {
                val canvasW = maxWidth
                val canvasH = maxHeight

                // Render every bounding box on the simulated game screen with Smart Solid Masking
                uiState.translatedBoxes.forEach { item ->
                    val b = item.box
                    val boxX = canvasW * b.normLeft
                    val boxY = canvasH * b.normTop
                    val boxW = canvasW * b.widthRatio
                    val boxH = canvasH * b.heightRatio

                    val solidMaskAlpha = if (uiState.smartInpaintingEnabled) {
                        backgroundOpacity.coerceIn(0.25f, 1.0f)
                    } else {
                        0.85f
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = boxX, y = boxY)
                            .width(boxW)
                            .height(boxH)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (uiState.isHoldToPeekActive) {
                                    Color(0xFF020617).copy(alpha = 0.88f)
                                } else {
                                    // Smart Solid Masking: Completely covers underlying Chinese characters
                                    Color(b.sampledBgHex).copy(alpha = solidMaskAlpha)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    uiState.isHoldToPeekActive -> ImperialGold.copy(alpha = 0.7f)
                                    b.isVerticalText -> SoftIndigo
                                    item.renderStage == RenderStage.CACHE_HIT_TEMPLATE -> PrimaryBlue
                                    else -> SlateBorderStrong
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        if (uiState.isHoldToPeekActive) {
                            // Show original underlying Chinese game UI text ONLY when Hold-to-Peek is pressed
                            Column(verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = "Bản gốc • ${b.speakerHint}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ImperialGold,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = b.rawChinese,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            // Clean Vietnamese Translation over Solid Mask Backdrop (Zero Chinese Overlap)
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (b.isVerticalText) {
                                            "Cột dọc #${item.rtlColumnOrder ?: 1}"
                                        } else {
                                            b.speakerHint
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SoftIndigo,
                                        fontSize = 9.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${"%.1f".format(item.boxLatencyMs)}ms",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        fontSize = 9.sp
                                    )
                                }

                                Text(
                                    text = item.currentDisplayVietnamese,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(b.textColorHex),
                                    fontSize = item.calculatedFontSizeSp.coerceIn(10.5f, 13.5f).sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Draggable Tap-to-Translate Magnifier Loupe when in TAP_LOUPE mode
                if (uiState.scanMode == ScanMode.TAP_LOUPE) {
                    val loupePxX = (constraints.maxWidth * uiState.loupeNormX).roundToInt() - 60
                    val loupePxY = (constraints.maxHeight * uiState.loupeNormY).roundToInt() - 60
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(loupePxX, loupePxY) }
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.28f))
                            .border(2.dp, PrimaryBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = "Kính lúp Tap-to-Translate",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CombatQuickControlsCard(
    uiState: StudioUiState,
    isOverlayRunning: Boolean,
    backgroundOpacity: Float,
    onBackgroundOpacityChange: (Float) -> Unit,
    onSetScanMode: (ScanMode) -> Unit,
    onRandomizeNumbers: () -> Unit,
    onRetranslateCold: () -> Unit,
    onRetranslateWarm: () -> Unit,
    onToggleInpainting: (Boolean) -> Unit,
    onToggleOverlayService: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 1. Primary Action Row: Warm Scan, Mutate Numbers {0}, Cold AI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRetranslateWarm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("btn_warm_scan"),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Dịch nhanh", style = MaterialTheme.typography.labelLarge)
            }

            Button(
                onClick = onRandomizeNumbers,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftIndigo,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.1f)
                    .height(46.dp)
                    .testTag("btn_randomize_numbers"),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Đổi số {0} (<1ms)", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
                onClick = onRetranslateCold,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(0.85f)
                    .height(46.dp)
                    .testTag("btn_cold_scan"),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("Dịch lại AI", style = MaterialTheme.typography.labelMedium)
            }
        }

        // 2. Background Opacity Slider (Default 92%) to guarantee zero text overlap
        Surface(
            color = ObsidianBg,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorderStrong, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Độ mờ nền chữ (Background Opacity)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${(backgroundOpacity * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryBlue
                    )
                }
                Text(
                    text = "Che phủ hoàn toàn chữ tiếng Trung bên dưới trước khi in bản dịch tiếng Việt.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Slider(
                    value = backgroundOpacity,
                    onValueChange = onBackgroundOpacityChange,
                    valueRange = 0.25f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryBlue,
                        activeTrackColor = PrimaryBlue
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ScanMode.entries.forEach { mode ->
                            FilterChip(
                                selected = uiState.scanMode == mode,
                                onClick = { onSetScanMode(mode) },
                                label = { Text(mode.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Che nền", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Switch(
                            checked = uiState.smartInpaintingEnabled,
                            onCheckedChange = onToggleInpainting
                        )
                    }
                }
            }
        }

        // 3. System Floating Overlay Action Ball Launcher
        Surface(
            color = ObsidianBg,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorderStrong, RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOverlayRunning) {
                            "Nút nổi dịch mọi ứng dụng: Đang bật"
                        } else {
                            "Nút nổi dịch trên mọi Game & Ứng dụng"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Hiển thị lớp dịch thông minh đè lên màn hình điện thoại",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onToggleOverlayService,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOverlayRunning) CrimsonAlert else PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_toggle_system_overlay")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PictureInPictureAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isOverlayRunning) "Tắt nổi" else "Bật nút nổi",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun TranslatedBoxDetailCard(boxState: TranslatedBoxState) {
    val b = boxState.box
    Surface(
        color = ObsidianBg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorderStrong, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PrimaryBlue.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (b.isVerticalText) {
                                "Chữ dọc RTL #${boxState.rtlColumnOrder}"
                            } else {
                                "Khung #${b.id}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = boxState.renderStage.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftIndigo
                    )
                }

                Text(
                    text = "${"%.1f".format(boxState.boxLatencyMs)}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Text(
                text = boxState.currentDisplayVietnamese,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            Text(
                text = "Bản gốc: ${b.rawChinese}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            AnimatedVisibility(visible = boxState.parameterized.hasParameters) {
                Surface(
                    color = SlateSurfaceElevated,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Mẫu số liệu: \"${boxState.parameterized.templateKey}\" (${
                            boxState.parameterized.extractedParams.mapIndexed { i, v -> "{$i}=$v" }.joinToString(", ")
                        })",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftIndigo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
