package com.example.data.model

import kotlinx.serialization.Serializable

enum class AiProviderType(
    val displayName: String,
    val badge: String,
    val availableModels: List<String>
) {
    GEMINI(
        displayName = "Google Gemini AI",
        badge = "GEMINI",
        availableModels = listOf(
            "gemini-2.5-flash-lite",
            "gemini-2.5-flash",
            "gemini-3.1-pro-preview"
        )
    ),
    OPENAI(
        displayName = "OpenAI ChatGPT",
        badge = "CHATGPT",
        availableModels = listOf(
            "gpt-4o-mini",
            "gpt-4o"
        )
    )
}

enum class AuthMode(val label: String) {
    OAUTH_SESSION("OAuth Sign-In / Session"),
    API_KEY_POOL("API Key Pool (BuildConfig)")
}

data class AiAccountProfile(
    val id: String,
    val provider: AiProviderType,
    val displayName: String,
    val emailOrHandle: String,
    val authMode: AuthMode,
    val selectedModel: String,
    val isConnected: Boolean,
    val quotaStatus: String,
    val syncSharedContextAndGlossary: Boolean = true,
    val sessionTokenPreview: String = "oauth2_sess_••••9f2a"
)

enum class ExtractionChannel(val label: String, val badge: String, val description: String) {
    HARDSUB_OCR(
        label = "Quét Phụ Đề Cứng (Hardsub OCR)",
        badge = "HARDSUB OCR",
        description = "Chỉ quét vùng 25% khung hình dưới bằng dHash Frame Diffing để bắt chính xác mốc start_time -> end_time từng mili-giây."
    ),
    AUDIO_ASR(
        label = "Nhận Diện Giọng Nói (Audio ASR)",
        badge = "AUDIO ASR",
        description = "Trích xuất âm thanh tiếng Trung cho video gốc chưa có chữ và dịch chuẩn ngữ cảnh sang tiếng Việt."
    ),
    DUAL_HYBRID(
        label = "Chế Độ Kép (Hardsub OCR + Audio ASR)",
        badge = "DUAL HYBRID",
        description = "Kết hợp đồng thời quét vùng phụ đề cứng và nhận diện giọng nói để không bỏ sót bất kỳ câu thoại hay biển hiệu nào."
    )
}

enum class HardsubMaskMode(val label: String, val description: String) {
    GAUSSIAN_BLUR("Dải Làm Mờ (Gaussian Blur Band)", "Làm mờ mượt vùng phụ đề tiếng Trung gốc trước khi vẽ chữ Việt"),
    SOLID_BOX("Hộp Nền Tương Thích (Adaptive Box)", "Lấy mẫu màu nền tối đè khít vùng chữ gốc"),
    NONE("Tắt Che Phụ Đề Gốc", "Chỉ hiển thị lớp chữ tiếng Việt có viền (Stroke)")
}

@Serializable
data class SubtitleStyleConfig(
    val normX: Float = 0.50f,           // 0.1..0.9 horizontal center
    val normY: Float = 0.82f,           // 0.1..0.92 vertical position (default bottom 18%)
    val bottomMarginDp: Int = 24,
    val fontSizeSp: Float = 16.5f,
    val fontFamilyName: String = "Be Vietnam Pro",
    val textColorHex: Long = 0xFFFFFFFFL,
    val outlineColorHex: Long = 0xFF090D16L,
    val outlineWidthDp: Float = 2.0f,
    val dropShadowEnabled: Boolean = true,
    val maskMode: HardsubMaskMode = HardsubMaskMode.GAUSSIAN_BLUR,
    val maskOpacity: Float = 0.90f,      // Default 90% solid/backdrop masking to completely cover underlying Chinese text
    val roiScanBottomRatio: Float = 0.25f // Scans bottom 25% of video frame for Hardsub OCR
)

@Serializable
data class VideoSubtitleCue(
    val id: Int,
    val clipId: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val sourceChinese: String,
    val translatedVietnamese: String,
    val channel: ExtractionChannel,
    val speakerTag: String = "Nhân vật",
    val hasParameterizedNumbers: Boolean = false
) {
    val formattedTimeRange: String
        get() = "${formatMsToTimecode(startTimeMs)} ➔ ${formatMsToTimecode(endTimeMs)}"

    companion object {
        fun formatMsToTimecode(ms: Long): String {
            val totalSec = (ms / 1000).coerceAtLeast(0)
            val mins = totalSec / 60
            val secs = totalSec % 60
            val millis = ms % 1000
            return "%02d:%02d.%03d".format(mins, secs, millis)
        }

        fun formatMsToSrtTimestamp(ms: Long): String {
            val totalSec = (ms / 1000).coerceAtLeast(0)
            val hours = totalSec / 3600
            val mins = (totalSec % 3600) / 60
            val secs = totalSec % 60
            val millis = ms % 1000
            return "%02d:%02d:%02d,%03d".format(hours, mins, secs, millis)
        }

        fun formatMsToAssTimestamp(ms: Long): String {
            val totalSec = (ms / 1000).coerceAtLeast(0)
            val hours = totalSec / 3600
            val mins = (totalSec % 3600) / 60
            val secs = totalSec % 60
            val centis = (ms % 1000) / 10
            return "%d:%02d:%02d.%02d".format(hours, mins, secs, centis)
        }
    }
}

@Serializable
data class ShortVideoClipItem(
    val clipId: String,
    val fileName: String,
    val localUriString: String? = null,
    val seriesTopic: String,
    val durationMs: Long,
    val resolution: String = "1080x1920 (FullHD 60fps)",
    val fps: Int = 60,
    val bitrateKbps: Int = 14800,
    val cornerBadgeText: String,
    val openingChineseLine: String,
    val closingChineseLine: String,
    val uploadedOrderIndex: Int,
    val trueStoryEpisodeNumber: Int,
    val aiAssignedOrder: Int,
    val aiSequencingReason: String,
    val detectedOverlapStartMs: Long = 0L,
    val detectedOverlapEndMs: Long = 0L,
    val cues: List<VideoSubtitleCue> = emptyList(),
    val previewGradientStartHex: Long = 0xFF0F172AL,
    val previewGradientEndHex: Long = 0xFF064E3BL
) {
    val effectiveDurationAfterTrimMs: Long
        get() = (durationMs - detectedOverlapStartMs - detectedOverlapEndMs).coerceAtLeast(1000L)
}

enum class VideoExportMode(
    val title: String,
    val badge: String,
    val subtitle: String,
    val qualityRetention: String
) {
    LOSSLESS_STREAM_COPY(
        title = "1. Ghép Siêu Tốc Lossless Stream Copy (-f concat -c copy)",
        badge = "0% SUY HAO (LOSSLESS)",
        subtitle = "Nối hàng loạt video ngắn không render lại hình ảnh (MediaMuxer / FFmpeg -c copy) + Nhúng Softsub & xuất file .SRT / .ASS giữ nguyên vị trí.",
        qualityRetention = "100% Bitrate & FPS Gốc"
    ),
    MASTER_HARDSUB_BURN_IN(
        title = "2. In Phụ Đề Chất Lượng Gốc (Master-Quality Hardsub Burn-in)",
        badge = "99.9% MASTER CRF 17",
        subtitle = "Tự động đọc Bitrate gốc (14.8 Mbps) và in chết phụ đề tiếng Việt + dải che Gaussian Blur bằng NVENC / libx264 CRF 17 preset slow.",
        qualityRetention = "99.9% Master Bitrate"
    ),
    SUBTITLE_FILES_ONLY(
        title = "3. Xuất Riêng Bộ Phụ Đề Chuẩn Tọa Độ (.ASS & .SRT)",
        badge = "SRT + ASS WYSIWYG",
        subtitle = "Xuất ngay file phụ đề .ASS (chứa thẻ tọa độ \\pos(X,Y), màu sắc, viền chữ, hộp che chữ gốc) và .SRT chuẩn mili-giây.",
        qualityRetention = "Vector Text 100%"
    )
}

data class LosslessExportReport(
    val exportMode: VideoExportMode,
    val outputVideoFileName: String,
    val outputAssFileName: String,
    val outputSrtFileName: String,
    val mergedClipCount: Int,
    val totalOverlapTrimmedMs: Long,
    val totalDurationMs: Long,
    val executionTimeMs: Double,
    val preservedResolution: String,
    val preservedBitrateKbps: Int,
    val ffmpegOrMuxerCommand: String,
    val generatedAssPreview: String,
    val generatedSrtPreview: String,
    val pronounSyncSummary: String
)

/**
 * Portable .hvcloud Cloud Sync Bundle for PC Emulator <-> Android Phone synchronization
 */
@Serializable
data class HvCloudProjectBundle(
    val bundleVersion: Int = 1,
    val projectId: String,
    val projectTitle: String,
    val geminiAccountEmail: String,
    val syncCode6Digit: String,
    val sourceDeviceBadge: String,
    val styleConfig: SubtitleStyleConfig,
    val clips: List<ShortVideoClipItem>,
    val assSubtitleContent: String,
    val srtSubtitleContent: String,
    val glossarySnapshot: List<String>,
    val contextHistorySummary: String,
    val exportedAtEpochMs: Long
)

/**
 * Real Mobile Hardware RAM & Chunked Stream Guard Telemetry
 */
data class MobileMemoryTelemetry(
    val activeClipBuffers: Int = 4,
    val allocatedRamMb: Double = 16.8,
    val maxSafeRamBudgetMb: Double = 256.0,
    val chunkStreamSizeMb: Double = 2.0,
    val revokedObjectUriCount: Int = 2,
    val touchScrollLockActive: Boolean = true
)

enum class VideoStudioSubTab(val title: String) {
    PREVIEW_STYLER("Xem & Sửa"),
    BATCH_MERGER("Ghép Lô AI"),
    LOSSLESS_EXPORT("Xuất Video"),
    CLOUD_VAULT("Kho Cloud"),
    AUTH_HUB("Tài Khoản AI")
}
