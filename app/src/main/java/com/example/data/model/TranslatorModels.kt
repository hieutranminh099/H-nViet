package com.example.data.model

import kotlinx.serialization.Serializable

enum class ContextDomain(
    val displayName: String,
    val badgeLabel: String,
    val pronounRule: String,
    val systemPrompt: String
) {
    WUXIA_XIANXIA(
        displayName = "Tiên Hiệp / Kiếm Hiệp / Cổ Trang",
        badgeLabel = "TIÊN HIỆP",
        pronounRule = "Bản tọa / Đạo hữu / Sư tôn / Đệ tử / Ta - Ngươi / Huynh - Muội",
        systemPrompt = "Dịch Trung-Việt chuẩn văn phong Tiên Hiệp/Cổ Trang. Giữ nguyên đại từ (Sư tôn, Đệ tử, Bản tọa, Đạo hữu, Ta/Ngươi) và các biến số {0}, {1}. Chỉ trả về đúng các dòng có tiền tố [ID], không giải thích."
    ),
    SCIFI_ANIME_RPG(
        displayName = "Anime / Sci-Fi RPG / Hiện Đại",
        badgeLabel = "SCI-FI RPG",
        pronounRule = "Tôi / Cậu / Anh / Em / Đội trưởng / Khai Phá Giả",
        systemPrompt = "Dịch Trung-Việt chuẩn văn phong Game Anime/Sci-Fi RPG hiện đại (Honkai/Genshin). Ngắn gọn, tự nhiên, giữ nguyên biến số {0}, {1} và tiền tố [ID]."
    ),
    ECOMMERCE_SOCIAL(
        displayName = "TMĐT & MXH (Taobao / Xiaohongshu)",
        badgeLabel = "TMĐT / MXH",
        pronounRule = "Shop / Bạn / Mình / Voucher / Freeship",
        systemPrompt = "Dịch Trung-Việt chuẩn giao diện TMĐT (Taobao/Douyin/Xiaohongshu). Ngắn gọn súc tích cho nút bấm, giá tiền, khuyến mãi. Giữ nguyên biến số {0}, {1} và tiền tố [ID]."
    ),
    SYSTEM_UI(
        displayName = "Giao Diện Menu / Kỹ Năng Chiến Đấu",
        badgeLabel = "COMBAT HUD",
        pronounRule = "Thuật ngữ RPG cô đọng (Sát thương, Hồi chiêu, Bạo kích)",
        systemPrompt = "Dịch Trung-Việt chuẩn bảng kỹ năng và chỉ số game RPG. Cực kỳ ngắn gọn để vừa khung UI, bắt buộc giữ nguyên các vị trí biến số {0}, {1} và tiền tố [ID]."
    )
}

enum class ScanMode(val label: String, val description: String) {
    AUTO_WATCH("Auto-Watch ROI", "Tự động theo dõi vùng hội thoại (dHash > 5% + 80ms cố định)"),
    TAP_LOUPE("Tap-to-Translate Loupe", "Kéo kính lúp vào khung chữ bất kỳ để dịch tức thì")
}

enum class RenderStage(val label: String) {
    IDLE("Chờ quét"),
    CACHE_HIT_EXACT("Tầng 0: L1/L2 Exact Cache (<1ms)"),
    CACHE_HIT_TEMPLATE("Tầng 0: Parameterized Template {0},{1} (<1ms)"),
    STAGE_1_VIETPHRASE("Pha 1: Vietphrase + Glossary (<20ms)"),
    STAGE_2_STREAMING("Pha 2: Gemini AI Streaming..."),
    STAGE_2_COMPLETE("Pha 2: Gemini AI Chuẩn Ngữ Cảnh")
}

enum class KeyHealthStatus(val label: String) {
    HEALTHY("READY"),
    COOLDOWN_429("429 RATE LIMIT"),
    SLOW_FAILOVER("SLOW >450ms")
}

data class ApiKeySlot(
    val id: String,
    val label: String,
    val apiKey: String,
    val isBuiltIn: Boolean = false,
    val status: KeyHealthStatus = KeyHealthStatus.HEALTHY,
    val lastLatencyMs: Double = 0.0,
    val avgLatencyMs: Double = 0.0,
    val requestCount: Int = 0,
    val failoverCount: Int = 0,
    val cooldownUntilEpochMs: Long = 0L
) {
    val maskedKey: String
        get() = if (apiKey.length <= 8) "••••••••"
        else "${apiKey.take(4)}••••${apiKey.takeLast(4)}"
}

@Serializable
data class OcrTextBox(
    val id: Int,
    val rawChinese: String,
    val normLeft: Float,
    val normTop: Float,
    val normRight: Float,
    val normBottom: Float,
    val sampledBgHex: Long = 0xE6111827,
    val textColorHex: Long = 0xFFF8FAFC,
    val speakerHint: String = ""
) {
    val widthRatio: Float get() = (normRight - normLeft).coerceAtLeast(0.01f)
    val heightRatio: Float get() = (normBottom - normTop).coerceAtLeast(0.01f)
    // Feature 3: Vertical Chinese Text Detection when height / width > 2.2
    val aspectRatioHw: Float get() = (heightRatio * 1000f) / (widthRatio * 560f)
    val isVerticalText: Boolean get() = aspectRatioHw > 2.2f
    val centerX: Float get() = (normLeft + normRight) / 2f
    val centerY: Float get() = (normTop + normBottom) / 2f
}

data class ParameterizedText(
    val originalText: String,
    val templateKey: String,
    val extractedParams: List<String>
) {
    val hasParameters: Boolean get() = extractedParams.isNotEmpty()
}

data class TranslatedBoxState(
    val box: OcrTextBox,
    val parameterized: ParameterizedText,
    val stage1Vietphrase: String,
    val currentDisplayVietnamese: String,
    val renderStage: RenderStage,
    val boxLatencyMs: Double,
    val calculatedFontSizeSp: Float,
    val matchedGlossaryTerms: List<String> = emptyList(),
    val rtlColumnOrder: Int? = null
)

data class LatencyTelemetry(
    val captureMs: Double = 6.4,
    val diffMs: Double = 1.8,
    val ocrAndMergeMs: Double = 38.5,
    val stage1Ms: Double = 4.2,
    val geminiTtftMs: Double = 0.0,
    val geminiTotalMs: Double = 0.0,
    val totalEndToEndMs: Double = 50.9,
    val cacheStatus: String = "WARM TEMPLATE HIT",
    val activeKeyLabel: String = "Key #1 (Primary)",
    val dHashDeltaPercent: Float = 14.2f,
    val stabilizationWaitMs: Int = 80,
    val modelName: String = "gemini-2.5-flash-lite",
    val verticalColumnsMerged: Int = 0
) {
    val isUnderOneSecondSla: Boolean get() = totalEndToEndMs < 1000.0
}

data class DialogueTurn(
    val turnIndex: Int,
    val sourceChinese: String,
    val translatedVietnamese: String,
    val domain: ContextDomain,
    val timestampMs: Long = System.currentTimeMillis()
)

data class SimulatedGameScene(
    val id: String,
    val title: String,
    val subtitle: String,
    val domain: ContextDomain,
    val backgroundGradientStart: Long,
    val backgroundGradientEnd: Long,
    val accentColor: Long,
    val boxes: List<OcrTextBox>,
    val description: String
)

data class BenchmarkStepResult(
    val stepName: String,
    val scenarioType: String,
    val sourceText: String,
    val stage1Preview: String,
    val finalTranslation: String,
    val captureMs: Double,
    val diffMs: Double,
    val ocrMs: Double,
    val cacheOrAiMs: Double,
    val totalMs: Double,
    val passedPronounContext: Boolean,
    val cacheHitType: String,
    val keyUsed: String
)
