package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CloudVaultProjectEntity
import com.example.data.model.AiAccountProfile
import com.example.data.model.ExtractionChannel
import com.example.data.model.HardsubMaskMode
import com.example.data.model.LosslessExportReport
import com.example.data.model.MobileMemoryTelemetry
import com.example.data.model.ShortVideoClipItem
import com.example.data.model.SubtitleStyleConfig
import com.example.data.model.VideoExportMode
import com.example.data.model.VideoStudioSubTab
import com.example.data.model.VideoSubtitleCue
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
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private enum class BottomSheetControlTab(val label: String) {
    TRANSLATE_CLIPS("Dịch & Tập phim"),
    STYLE_MASK("Kiểu chữ & Nền che"),
    TIMELINE_CUES("Lời thoại")
}

@Composable
fun VideoStudioScreen(
    subTab: VideoStudioSubTab,
    onSelectSubTab: (VideoStudioSubTab) -> Unit,
    accounts: List<AiAccountProfile>,
    activeAccount: AiAccountProfile,
    onSelectAccount: (String) -> Unit,
    onSelectAccountModel: (String, String) -> Unit,
    onToggleAccountOAuth: (String) -> Unit,
    clips: List<ShortVideoClipItem>,
    selectedClip: ShortVideoClipItem,
    onSelectClip: (String) -> Unit,
    onImportLocalVideos: (List<Uri>) -> Unit,
    extractionChannel: ExtractionChannel,
    onSelectExtractionChannel: (ExtractionChannel) -> Unit,
    onRunVideoExtractionAndTranslation: () -> Unit,
    styleConfig: SubtitleStyleConfig,
    onUpdateStyleConfig: (SubtitleStyleConfig) -> Unit,
    onUpdateCueTextAndTime: (Int, String, Long, Long) -> Unit,
    isAiSorted: Boolean,
    trimOverlapEnabled: Boolean,
    onToggleTrimOverlap: (Boolean) -> Unit,
    onRunAiPlotSequencer: () -> Unit,
    onResetScrambledClips: () -> Unit,
    selectedExportMode: VideoExportMode,
    onSelectExportMode: (VideoExportMode) -> Unit,
    exportReport: LosslessExportReport?,
    onRunLosslessExport: () -> Unit,
    vaultProjects: List<CloudVaultProjectEntity> = emptyList(),
    mobileMemoryTelemetry: MobileMemoryTelemetry = MobileMemoryTelemetry(),
    lastSyncedCode: String = "849-206",
    onSaveCurrentToVault: (String) -> Unit = {},
    onSyncBy6DigitCode: (String) -> Unit = {},
    onImportHvCloudJson: (String) -> Unit = {},
    onStreamVaultProject: (CloudVaultProjectEntity) -> Unit = {},
    onResumeEditVaultProject: (CloudVaultProjectEntity) -> Unit = {},
    onDeleteVaultProject: (String) -> Unit = {},
    onReleaseMobileRam: () -> Unit = {},
    statusMessage: String,
    modifier: Modifier = Modifier
) {
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportLocalVideos(uris)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Minimalist Swipeable Top Pill Navigation Bar (Zero Truncation)
        Surface(
            color = ObsidianBg,
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = SlateBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VideoStudioSubTab.entries.forEach { tab ->
                        val isSelected = subTab == tab
                        Surface(
                            color = if (isSelected) PrimaryBlue else SlateSurface,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryBlue else SlateBorderStrong,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { onSelectSubTab(tab) }
                                .testTag("video_subtab_${tab.name}")
                        ) {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) Color.White else TextSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // Active AI model pill
                Surface(
                    color = SlateSurfaceElevated,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectSubTab(VideoStudioSubTab.AUTH_HUB) }
                ) {
                    Text(
                        text = activeAccount.provider.badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftIndigo,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }
        }

        when (subTab) {
            VideoStudioSubTab.PREVIEW_STYLER -> OneHandVideoPreviewAndBottomSheet(
                clips = clips,
                selectedClip = selectedClip,
                onSelectClip = onSelectClip,
                onLaunchVideoPicker = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                extractionChannel = extractionChannel,
                onSelectExtractionChannel = onSelectExtractionChannel,
                onRunExtraction = onRunVideoExtractionAndTranslation,
                styleConfig = styleConfig,
                onUpdateStyle = onUpdateStyleConfig,
                onUpdateCue = onUpdateCueTextAndTime,
                onQuickSaveToCloudVault = {
                    onSaveCurrentToVault("")
                    onSelectSubTab(VideoStudioSubTab.CLOUD_VAULT)
                },
                statusMessage = statusMessage,
                modifier = Modifier.weight(1f)
            )
            VideoStudioSubTab.BATCH_MERGER -> SmartBatchMergerWorkspace(
                clips = clips,
                activeAccount = activeAccount,
                isAiSorted = isAiSorted,
                trimOverlapEnabled = trimOverlapEnabled,
                onToggleTrimOverlap = onToggleTrimOverlap,
                onRunAiPlotSequencer = onRunAiPlotSequencer,
                onResetScrambled = onResetScrambledClips,
                onLaunchVideoPicker = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                onProceedToExport = { onSelectSubTab(VideoStudioSubTab.LOSSLESS_EXPORT) },
                modifier = Modifier.weight(1f)
            )
            VideoStudioSubTab.LOSSLESS_EXPORT -> LosslessExportWorkspace(
                clips = clips,
                styleConfig = styleConfig,
                selectedMode = selectedExportMode,
                onSelectMode = onSelectExportMode,
                exportReport = exportReport,
                onRunExport = onRunLosslessExport,
                onSaveToCloudVault = {
                    onSaveCurrentToVault("")
                    onSelectSubTab(VideoStudioSubTab.CLOUD_VAULT)
                },
                modifier = Modifier.weight(1f)
            )
            VideoStudioSubTab.CLOUD_VAULT -> CloudVaultWorkspace(
                activeAccount = activeAccount,
                vaultProjects = vaultProjects,
                mobileMemoryTelemetry = mobileMemoryTelemetry,
                lastSyncedCode = lastSyncedCode,
                statusMessage = statusMessage,
                onSaveCurrentToVault = onSaveCurrentToVault,
                onSyncBy6DigitCode = onSyncBy6DigitCode,
                onImportHvCloudJson = onImportHvCloudJson,
                onStreamVaultProject = onStreamVaultProject,
                onResumeEditVaultProject = onResumeEditVaultProject,
                onDeleteVaultProject = onDeleteVaultProject,
                onReleaseMobileRam = onReleaseMobileRam,
                modifier = Modifier.weight(1f)
            )
            VideoStudioSubTab.AUTH_HUB -> GeminiChatGptAuthHubWorkspace(
                accounts = accounts,
                activeAccount = activeAccount,
                onSelectAccount = onSelectAccount,
                onSelectModel = onSelectAccountModel,
                onToggleOAuth = onToggleAccountOAuth,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Requirement 1 & 4:
 * - Pinned Top Video Preview with Smart Solid/Backdrop Masking (90% default opacity) so Vietnamese text NEVER overlaps raw Chinese text.
 * - One-Hand Friendly Collapsible Bottom Sheet Drawer housing all controls (Upload, Translate, Background Opacity 90% Slider, Font Size, Timeline).
 */
@Composable
private fun OneHandVideoPreviewAndBottomSheet(
    clips: List<ShortVideoClipItem>,
    selectedClip: ShortVideoClipItem,
    onSelectClip: (String) -> Unit,
    onLaunchVideoPicker: () -> Unit,
    extractionChannel: ExtractionChannel,
    onSelectExtractionChannel: (ExtractionChannel) -> Unit,
    onRunExtraction: () -> Unit,
    styleConfig: SubtitleStyleConfig,
    onUpdateStyle: (SubtitleStyleConfig) -> Unit,
    onUpdateCue: (Int, String, Long, Long) -> Unit,
    onQuickSaveToCloudVault: () -> Unit,
    statusMessage: String,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPlayheadMs by remember(selectedClip.clipId) { mutableStateOf(1200L) }
    var editingCueId by remember { mutableStateOf<Int?>(null) }
    var isPeekOriginalChinese by remember { mutableStateOf(false) }
    var isBottomSheetExpanded by remember { mutableStateOf(true) }
    var activeSheetTab by remember { mutableStateOf(BottomSheetControlTab.TRANSLATE_CLIPS) }

    // Playback loop
    LaunchedEffect(isPlaying, selectedClip.clipId) {
        while (isPlaying) {
            delay(50L)
            val next = currentPlayheadMs + 50L
            currentPlayheadMs = if (next >= selectedClip.durationMs) 0L else next
        }
    }

    val activeCue = selectedClip.cues.firstOrNull {
        currentPlayheadMs in it.startTimeMs..it.endTimeMs
    } ?: selectedClip.cues.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("video_preview_styler_list")
    ) {
        // =========================================================================
        // TOP HALF: PINNED VIDEO VIEWPORT WITH SMART SOLID MASKING (ZERO OVERLAP)
        // =========================================================================
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .border(1.dp, SlateBorderStrong, RoundedCornerShape(18.dp))
        ) {
            Column {
                // Clean Header Bar inside Video Player
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
                            text = selectedClip.fileName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Vị trí chữ: X=${(styleConfig.normX * 100).roundToInt()}%, Y=${(styleConfig.normY * 100).roundToInt()}% • Nền che ${(styleConfig.maskOpacity * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    // Hold-to-Peek original Chinese toggle pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPeekOriginalChinese) ImperialGold else SlateSurfaceElevated)
                            .border(1.dp, SlateBorderStrong, RoundedCornerShape(10.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isPeekOriginalChinese = true
                                        tryAwaitRelease()
                                        isPeekOriginalChinese = false
                                    }
                                )
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Giữ để xem bản gốc",
                                tint = if (isPeekOriginalChinese) ObsidianBg else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isPeekOriginalChinese) "Đang xem gốc" else "Giữ: Xem gốc",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isPeekOriginalChinese) ObsidianBg else TextSecondary
                            )
                        }
                    }
                }

                // Interactive Video Frame Canvas (Drag-and-Drop Subtitle with Smart Solid Masking)
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isBottomSheetExpanded) 215.dp else 310.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(selectedClip.previewGradientStartHex),
                                    Color(selectedClip.previewGradientEndHex)
                                )
                            )
                        )
                        .pointerInput(styleConfig) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val nx = (change.position.x / size.width.toFloat()).coerceIn(0.15f, 0.85f)
                                val ny = (change.position.y / size.height.toFloat()).coerceIn(0.15f, 0.90f)
                                onUpdateStyle(styleConfig.copy(normX = nx, normY = ny))
                            }
                        }
                        .testTag("video_wysiwyg_canvas")
                ) {
                    val wPx = constraints.maxWidth.toFloat()
                    val hPx = constraints.maxHeight.toFloat()

                    // Subtle Episode Badge in top-right corner
                    Surface(
                        color = ObsidianBg.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .border(1.dp, SlateBorderStrong, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = selectedClip.cornerBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    activeCue?.let { cue ->
                        if (isPeekOriginalChinese) {
                            // Only when user holds "Xem gốc" do we show the underlying Chinese hardsub
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 18.dp, start = 16.dp, end = 16.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ObsidianBg.copy(alpha = 0.85f))
                                    .border(1.dp, ImperialGold, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "[Bản gốc tiếng Trung] ${cue.sourceChinese}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ImperialGold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // SMART SOLID / BACKDROP MASKING:
                            // 1. First, cover the bottom Hardsub ROI band so original Chinese is 100% hidden
                            if (styleConfig.maskMode != HardsubMaskMode.NONE && styleConfig.maskOpacity > 0.05f) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    ObsidianBg.copy(alpha = (styleConfig.maskOpacity * 0.75f).coerceIn(0f, 0.95f))
                                                )
                                            )
                                        )
                                )
                            }

                            // 2. Render the draggable Vietnamese Subtitle inside a high-contrast Solid/Backdrop Mask Box
                            // guaranteeing ZERO overlapping Chinese characters underneath!
                            val targetOffsetX = ((styleConfig.normX - 0.5f) * wPx * 0.7f).roundToInt()
                            val targetOffsetY = ((styleConfig.normY - 0.5f) * hPx * 0.78f).roundToInt()

                            val effectiveAlpha = styleConfig.maskOpacity.coerceIn(0.15f, 1.0f)
                            val maskBgColor = when (styleConfig.maskMode) {
                                HardsubMaskMode.GAUSSIAN_BLUR -> Color(0xFF090D16).copy(alpha = effectiveAlpha)
                                HardsubMaskMode.SOLID_BOX -> Color(0xFF020617).copy(alpha = effectiveAlpha)
                                HardsubMaskMode.NONE -> Color(0xFF020617).copy(alpha = 0.65f)
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset { IntOffset(targetOffsetX, targetOffsetY) }
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(maskBgColor)
                                    .border(
                                        width = 1.dp,
                                        color = PrimaryBlue.copy(alpha = 0.65f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenWith,
                                            contentDescription = null,
                                            tint = SoftIndigo,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "${cue.speakerTag} • Kéo để đổi vị trí",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SoftIndigo,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = cue.translatedVietnamese,
                                        color = Color(styleConfig.textColorHex),
                                        fontSize = styleConfig.fontSizeSp.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        lineHeight = (styleConfig.fontSizeSp + 5f).sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Minimalist Scrubber & Play/Pause Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateSurface)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                            .testTag("btn_video_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Phát hoặc Tạm dừng",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = VideoSubtitleCue.formatMsToTimecode(currentPlayheadMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )

                    Slider(
                        value = currentPlayheadMs.toFloat(),
                        onValueChange = { currentPlayheadMs = it.toLong() },
                        valueRange = 0f..selectedClip.durationMs.toFloat().coerceAtLeast(1000f),
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryBlue,
                            activeTrackColor = PrimaryBlue,
                            inactiveTrackColor = SlateSurfaceElevated
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = VideoSubtitleCue.formatMsToTimecode(selectedClip.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // =========================================================================
        // BOTTOM HALF: ONE-HAND FRIENDLY COLLAPSIBLE TABBED BOTTOM SHEET
        // =========================================================================
        Surface(
            color = SlateSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(
                    width = 1.dp,
                    color = SlateBorderStrong,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .testTag("video_bottom_sheet_drawer")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Bottom Sheet Grab Handle & Segmented Control Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Drag handle indicator
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
                                text = "Bảng điều khiển một tay",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = onQuickSaveToCloudVault,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_preview_save_cloud")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = SoftIndigo,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Lưu Cloud", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            }

                            IconButton(
                                onClick = { isBottomSheetExpanded = !isBottomSheetExpanded },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_toggle_bottom_sheet")
                            ) {
                                Icon(
                                    imageVector = if (isBottomSheetExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                    contentDescription = "Thu gọn hoặc mở rộng ngăn kéo",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3 Thumb-Friendly Bottom Sheet Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianBg)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BottomSheetControlTab.entries.forEach { tab ->
                            val selected = activeSheetTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (selected) SlateSurfaceElevated else Color.Transparent)
                                    .clickable {
                                        activeSheetTab = tab
                                        isBottomSheetExpanded = true
                                    }
                                    .padding(vertical = 8.dp)
                                    .testTag("sheet_tab_${tab.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) TextPrimary else TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Scrollable Content inside the Bottom Sheet
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (activeSheetTab) {
                        BottomSheetControlTab.TRANSLATE_CLIPS -> {
                            item {
                                // Primary CTA Row: Upload Video & Translate AI
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = onLaunchVideoPicker,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PrimaryBlue,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("btn_pick_local_videos")
                                    ) {
                                        Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Tải video lên", style = MaterialTheme.typography.titleMedium)
                                    }

                                    Button(
                                        onClick = onRunExtraction,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SoftIndigo,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("btn_extract_translate_video")
                                    ) {
                                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Dịch AI ngay", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }

                            item {
                                // Secondary Compact Chips: Extraction Mode (Hardsub OCR / Audio ASR / Hybrid)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Chế độ nhận diện phụ đề:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ExtractionChannel.entries.forEach { ch ->
                                            FilterChip(
                                                selected = extractionChannel == ch,
                                                onClick = { onSelectExtractionChannel(ch) },
                                                label = { Text(ch.badge, style = MaterialTheme.typography.labelMedium) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f),
                                                    selectedLabelColor = PrimaryBlue
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                // Episode Selector Strip
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Danh sách tập video đang nạp (${clips.size} tập):",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        clips.forEach { clip ->
                                            FilterChip(
                                                selected = clip.clipId == selectedClip.clipId,
                                                onClick = { onSelectClip(clip.clipId) },
                                                label = {
                                                    Text(
                                                        text = "${clip.cornerBadgeText} • ${clip.fileName.take(16)}",
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = SoftIndigo.copy(alpha = 0.22f),
                                                    selectedLabelColor = SoftIndigo
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Surface(
                                    color = ObsidianBg,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, SlateBorderStrong, RoundedCornerShape(12.dp))
                                ) {
                                    Text(
                                        text = statusMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }

                        BottomSheetControlTab.STYLE_MASK -> {
                            item {
                                WysiwygStylerControlsCard(
                                    styleConfig = styleConfig,
                                    onUpdateStyle = onUpdateStyle
                                )
                            }
                        }

                        BottomSheetControlTab.TIMELINE_CUES -> {
                            items(selectedClip.cues, key = { it.id }) { cue ->
                                TimelineCueEditableCard(
                                    cue = cue,
                                    isActiveAtPlayhead = activeCue?.id == cue.id,
                                    isEditing = editingCueId == cue.id,
                                    onSeekToCue = { currentPlayheadMs = cue.startTimeMs + 100L },
                                    onToggleEdit = {
                                        editingCueId = if (editingCueId == cue.id) null else cue.id
                                    },
                                    onSaveCueEdit = { newText, newStart, newEnd ->
                                        onUpdateCue(cue.id, newText, newStart, newEnd)
                                        editingCueId = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Requirement 1: Smart Solid Masking & Background Opacity Slider (Default 90%)
 */
@Composable
private fun WysiwygStylerControlsCard(
    styleConfig: SubtitleStyleConfig,
    onUpdateStyle: (SubtitleStyleConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. Background Opacity Slider (Default 90%)
        Surface(
            color = ObsidianBg,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorderStrong, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    Surface(
                        color = PrimaryBlue.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${(styleConfig.maskOpacity * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Mặc định 90% để che khuất hoàn toàn chữ tiếng Trung gốc bên dưới, chống rối mắt.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Slider(
                    value = styleConfig.maskOpacity,
                    onValueChange = { onUpdateStyle(styleConfig.copy(maskOpacity = it)) },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryBlue,
                        activeTrackColor = PrimaryBlue
                    ),
                    modifier = Modifier.testTag("slider_background_opacity")
                )

                // Mask Mode Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HardsubMaskMode.entries.forEach { mode ->
                        FilterChip(
                            selected = styleConfig.maskMode == mode,
                            onClick = { onUpdateStyle(styleConfig.copy(maskMode = mode)) },
                            label = { Text(mode.label, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }
        }

        // 2. Font Size, Vertical Position & Color Picker
        Surface(
            color = ObsidianBg,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorderStrong, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cỡ chữ: ${"%.1f".format(styleConfig.fontSizeSp)}sp",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                        Slider(
                            value = styleConfig.fontSizeSp,
                            onValueChange = { onUpdateStyle(styleConfig.copy(fontSizeSp = it)) },
                            valueRange = 12f..24f
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vị trí dọc Y: ${(styleConfig.normY * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                        Slider(
                            value = styleConfig.normY,
                            onValueChange = { onUpdateStyle(styleConfig.copy(normY = it)) },
                            valueRange = 0.20f..0.90f
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Be Vietnam Pro", "Space Grotesk", "JetBrains Mono").forEach { fontName ->
                            FilterChip(
                                selected = styleConfig.fontFamilyName == fontName,
                                onClick = { onUpdateStyle(styleConfig.copy(fontFamilyName = fontName)) },
                                label = { Text(fontName.take(10), style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0xFFFFFFFFL, 0xFFFBBF24L, 0xFF38BDF8L, 0xFFA7F3D0L).forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .border(
                                        width = if (styleConfig.textColorHex == hex) 2.5.dp else 1.dp,
                                        color = if (styleConfig.textColorHex == hex) PrimaryBlue else SlateBorderStrong,
                                        shape = CircleShape
                                    )
                                    .clickable { onUpdateStyle(styleConfig.copy(textColorHex = hex)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineCueEditableCard(
    cue: VideoSubtitleCue,
    isActiveAtPlayhead: Boolean,
    isEditing: Boolean,
    onSeekToCue: () -> Unit,
    onToggleEdit: () -> Unit,
    onSaveCueEdit: (String, Long, Long) -> Unit
) {
    var draftText by remember(cue.translatedVietnamese) { mutableStateOf(cue.translatedVietnamese) }
    var draftStartMs by remember(cue.startTimeMs) { mutableStateOf(cue.startTimeMs.toString()) }
    var draftEndMs by remember(cue.endTimeMs) { mutableStateOf(cue.endTimeMs.toString()) }

    Surface(
        color = if (isActiveAtPlayhead) SlateSurfaceElevated else ObsidianBg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isActiveAtPlayhead) PrimaryBlue else SlateBorderStrong,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSeekToCue() }
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PrimaryBlue.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = cue.formattedTimeRange,
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "${cue.speakerTag} • ${cue.channel.badge}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                IconButton(onClick = onToggleEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Chỉnh sửa phụ đề",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = cue.translatedVietnamese,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            Text(
                text = "Bản gốc: ${cue.sourceChinese}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            if (isEditing) {
                OutlinedTextField(
                    value = draftText,
                    onValueChange = { draftText = it },
                    label = { Text("Sửa lời dịch tiếng Việt") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = draftStartMs,
                        onValueChange = { draftStartMs = it },
                        label = { Text("Bắt đầu (ms)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = draftEndMs,
                        onValueChange = { draftEndMs = it },
                        label = { Text("Kết thúc (ms)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            onSaveCueEdit(
                                draftText,
                                draftStartMs.toLongOrNull() ?: cue.startTimeMs,
                                draftEndMs.toLongOrNull() ?: cue.endTimeMs
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Lưu", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
