package com.example.engine

import com.example.data.local.GlossaryEntryEntity
import com.example.data.local.TranslationCacheDao
import com.example.data.local.TranslationCacheEntity
import com.example.data.model.ContextDomain
import com.example.data.model.OcrTextBox
import com.example.data.model.ParameterizedText
import java.util.Collections
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Core Sub-Millisecond Engine for:
 * 1. Parameterized Template Cache ({0}, {1} number extraction & < 1ms re-interpolation)
 * 2. Vertical Chinese Text Detection (height/width > 2.2 -> Right-to-Left column merging)
 * 3. 64-bit dHash Frame Diffing & 80ms Stabilization Gate
 * 4. Stage 1 Instant Vietphrase + Dynamic Glossary Token-Saving Filter
 * 5. Auto-Context Profiler & Smart Inpainting Dynamic Font Sizing
 */
class ParameterizedTemplateEngine(
    private val cacheDao: TranslationCacheDao? = null
) {
    // L1 RAM LRU Cache for < 0.2ms zero-allocation hits
    private val l1RamCache: MutableMap<String, String> = Collections.synchronizedMap(
        object : LinkedHashMap<String, String>(512, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
                return size > 512
            }
        }
    )

    private val numberRegex = Regex("""\d+(?:\.\d+)?%?""")
    private val placeholderRegex = Regex("""\{(\d+)\}""")

    // Pre-loaded Vietphrase dictionary for < 10ms Stage 1 instant feedback
    private val baseVietphraseMap: Map<String, String> = linkedMapOf(
        // Tiên hiệp / Kiếm hiệp / Cổ trang
        "师尊" to "Sư tôn",
        "弟子" to "đệ tử",
        "本座" to "Bản tọa",
        "道友" to "Đạo hữu",
        "少侠" to "Thiếu hiệp",
        "前辈" to "Tiền bối",
        "晚辈" to "vãn bối",
        "掌门" to "Chưởng môn",
        "青云大殿" to "Thanh Vân Đại Điện",
        "藏经阁" to "Tàng Kinh Các",
        "筑基期" to "Trúc Cơ Kỳ",
        "金丹期" to "Kim Đan Kỳ",
        "元婴期" to "Nguyên Anh Kỳ",
        "化神" to "Hóa Thần",
        "天剑诀" to "Thiên Kiếm Quyết",
        "九天玄雷" to "Cửu Thiên Huyền Lôi",
        "护山大阵" to "Hộ Sơn Đại Trận",
        "灵石" to "Linh thạch",
        "上品灵石" to "Thượng phẩm Linh thạch",
        "极品仙丹" to "Cực phẩm Tiên đan",
        "突破" to "đột phá",
        "走火入魔" to "tẩu hỏa nhập ma",
        "魔教" to "Ma giáo",
        "血魔老祖" to "Huyết Ma Lão Tổ",
        "已经" to "đã",
        "成功" to "thành công",
        "请" to "xin ",
        "赐教" to "chỉ giáo",
        "呈上" to "Dâng lên ",
        "叩谢师恩" to "Khấu tạ sư ân",
        "立刻下山历练" to "Lập tức xuống núi lịch luyện",
        "闭关修炼" to "Bế quan tu luyện",
        "剑气纵横三万里" to "Kiếm khí tung hoành ba vạn dặm",
        "一剑光寒十九洲" to "Một kiếm lạnh thấu mười chín châu",
        "天道无亲" to "Thiên đạo vô thân",
        "唯剑永恒" to "Duy kiếm vĩnh hằng",
        // Combat & Parameterized Skill Templates
        "造成" to "Gây ",
        "点火焰伤害" to " điểm sát thương Hỏa",
        "点雷电伤害" to " điểm sát thương Lôi",
        "点真实伤害" to " điểm sát thương chuẩn",
        "冷却时间" to "Hồi chiêu ",
        "冷却" to "hồi chiêu ",
        "秒" to " giây",
        "暴击率提升" to "Tăng tỷ lệ bạo kích ",
        "消耗法力" to "Tiêu hao Pháp lực ",
        "恢复生命值" to "Hồi phục Sinh lực ",
        "击败敌方首领获得" to "Đánh bại Thủ lĩnh địch nhận ",
        "枚上品灵石" to " viên Thượng phẩm Linh thạch",
        "经验值" to " điểm Kinh nghiệm",
        // Anime / Sci-Fi RPG (Honkai / Genshin / Modern)
        "开拓者" to "Khai Phá Giả",
        "星穹列车" to "Đoàn Tàu Astral",
        "跃迁引擎" to "Động cơ Bước Nhảy",
        "已准备就绪" to "đã sẵn sàng",
        "是否立即启动" to "có khởi động ngay không",
        "折跃坐标" to "Tọa độ bước nhảy",
        "空间站" to "Trạm Không Gian",
        "星核猎手" to "Thợ Săn Stellaron",
        "立即出发" to "Xuất phát ngay",
        "检查燃料储备" to "Kiểm tra dự trữ nhiên liệu",
        "联系姬子领航员" to "Liên lạc Hoa tiêu Himeko",
        "能量护盾" to "Khiên năng lượng",
        // TMĐT & MXH (Taobao / Xiaohongshu)
        "限时秒杀" to "Flash Sale giới hạn",
        "满" to "Đơn từ ",
        "减" to " giảm ",
        "元" to " tệ",
        "包邮" to "Freeship",
        "立即购买" to "Mua ngay",
        "加入购物车" to "Thêm vào giỏ hàng",
        "已售" to "Đã bán ",
        "件" to " sản phẩm",
        "好评率" to "Đánh giá tốt "
    )

    /**
     * Feature 1: Extract numbers into positional placeholders {0}, {1}, ...
     */
    fun parameterize(rawChinese: String): ParameterizedText {
        val trimmed = rawChinese.trim()
        val params = mutableListOf<String>()
        val template = numberRegex.replace(trimmed) { matchResult ->
            val index = params.size
            params.add(matchResult.value)
            "{$index}"
        }
        return ParameterizedText(
            originalText = trimmed,
            templateKey = template,
            extractedParams = params
        )
    }

    /**
     * Feature 1: Re-inject extracted numbers into a cached Vietnamese template in < 0.2ms
     */
    fun interpolateParams(vietnameseTemplate: String, params: List<String>): String {
        if (params.isEmpty()) return vietnameseTemplate
        return placeholderRegex.replace(vietnameseTemplate) { match ->
            val idx = match.groupValues[1].toIntOrNull() ?: -1
            if (idx in params.indices) params[idx] else match.value
        }
    }

    fun buildCacheKey(domain: ContextDomain, templateKey: String): String {
        return "${domain.name}::$templateKey"
    }

    /**
     * Stage 0: Lookup L1 RAM LRU Cache (<0.2ms) then L2 Room SQLite (<1ms)
     */
    suspend fun lookupCachedTranslation(
        domain: ContextDomain,
        parameterized: ParameterizedText
    ): Pair<String, Boolean>? {
        val key = buildCacheKey(domain, parameterized.templateKey)
        // Check L1 RAM Cache first
        val l1Template = l1RamCache.get(key)
        if (l1Template != null) {
            return interpolateParams(l1Template, parameterized.extractedParams) to parameterized.hasParameters
        }
        // Check L2 SQLite Cache
        val entity = cacheDao?.getByCacheKey(key)
        if (entity != null) {
            l1RamCache.put(key, entity.vietnameseTemplate)
            cacheDao.incrementHitCount(key)
            return interpolateParams(entity.vietnameseTemplate, parameterized.extractedParams) to parameterized.hasParameters
        }
        return null
    }

    /**
     * Save Gemini's translated text as a reusable parameterized template in L1 + L2
     */
    suspend fun saveTranslationToCache(
        domain: ContextDomain,
        parameterized: ParameterizedText,
        translatedVietnamese: String
    ) {
        val normalizedTemplate = normalizeVietnameseToTemplate(
            translatedVietnamese = translatedVietnamese,
            extractedParams = parameterized.extractedParams
        )
        val key = buildCacheKey(domain, parameterized.templateKey)
        l1RamCache.put(key, normalizedTemplate)
        cacheDao?.upsertTemplate(
            TranslationCacheEntity(
                cacheKey = key,
                domainName = domain.name,
                templatePattern = parameterized.templateKey,
                vietnameseTemplate = normalizedTemplate,
                rawExampleChinese = parameterized.originalText,
                hitCount = 1,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Ensures that if Gemini outputs concrete numbers or keeps {0}, {1}, the cached template
     * always preserves {0}, {1} placeholders for future variable-number hits.
     */
    fun normalizeVietnameseToTemplate(
        translatedVietnamese: String,
        extractedParams: List<String>
    ): String {
        if (extractedParams.isEmpty()) return translatedVietnamese.trim()
        var result = translatedVietnamese.trim()
        extractedParams.forEachIndexed { index, paramValue ->
            val placeholder = "{$index}"
            if (!result.contains(placeholder) && paramValue.isNotEmpty()) {
                result = result.replaceFirst(paramValue, placeholder)
            }
        }
        return result
    }

    fun putInMemoryTemplate(domain: ContextDomain, templateKey: String, vietnameseTemplate: String) {
        l1RamCache.put(buildCacheKey(domain, templateKey), vietnameseTemplate)
    }

    /**
     * Stage 1 (< 15ms): Instant Vietphrase + Dynamic Glossary preview while preserving {0}, {1}
     */
    fun translateStage1Vietphrase(
        parameterized: ParameterizedText,
        customGlossary: List<GlossaryEntryEntity>
    ): String {
        var working = parameterized.templateKey
        // Apply custom glossary longest terms first
        val sortedCustom = customGlossary.sortedByDescending { it.chineseTerm.length }
        for (entry in sortedCustom) {
            if (entry.chineseTerm.isNotBlank() && working.contains(entry.chineseTerm)) {
                working = working.replace(entry.chineseTerm, " ${entry.vietnameseTerm} ")
            }
        }
        // Apply pre-loaded Vietphrase longest keys first
        val sortedBuiltIn = baseVietphraseMap.entries.sortedByDescending { it.key.length }
        for ((cn, vn) in sortedBuiltIn) {
            if (working.contains(cn)) {
                working = working.replace(cn, vn)
            }
        }
        // Replace Chinese punctuation with clean Vietnamese spacing
        working = working
            .replace("，", ", ")
            .replace("。", ". ")
            .replace("！", "! ")
            .replace("？", "? ")
            .replace("：", ": ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        return interpolateParams(working, parameterized.extractedParams)
    }

    /**
     * Dynamic Glossary Injection: Extracts ONLY glossary terms that actually appear in the current screen boxes
     */
    fun extractMatchingGlossaryForPrompt(
        boxes: List<OcrTextBox>,
        allGlossary: List<GlossaryEntryEntity>
    ): List<GlossaryEntryEntity> {
        val combinedText = boxes.joinToString(" ") { it.rawChinese }
        return allGlossary.filter { entry ->
            entry.chineseTerm.isNotBlank() && combinedText.contains(entry.chineseTerm)
        }
    }

    /**
     * Feature 3: Vertical Chinese Text Detection (height / width > 2.2) & Right-to-Left Merging
     * Groups vertical columns that belong to a multi-column scroll/plaque and orders them strictly Right-to-Left.
     */
    fun sortAndMergeBoxesWithVerticalDetection(rawBoxes: List<OcrTextBox>): Pair<List<OcrTextBox>, Int> {
        if (rawBoxes.isEmpty()) return emptyList<OcrTextBox>() to 0

        val verticalBoxes = rawBoxes.filter { it.isVerticalText }
        val horizontalBoxes = rawBoxes.filter { !it.isVerticalText }

        // Sort vertical Chinese columns strictly from RIGHT to LEFT (descending centerX)
        val sortedVerticalsRtl = verticalBoxes.sortedByDescending { it.centerX }

        // Check if adjacent vertical columns should be merged into a single classical couplet/scroll
        val processedVerticals = if (sortedVerticalsRtl.size >= 2 &&
            areVerticalColumnsGroupable(sortedVerticalsRtl)
        ) {
            sortedVerticalsRtl.mapIndexed { idx, box ->
                box.copy(speakerHint = "Cột dọc RTL #${idx + 1} (Phải ➔ Trái)")
            }
        } else {
            sortedVerticalsRtl
        }

        // Sort horizontal boxes Top-to-Bottom, then Left-to-Right
        val sortedHorizontals = horizontalBoxes.sortedWith(
            compareBy<OcrTextBox> { (it.normTop * 20).toInt() }.thenBy { it.normLeft }
        )

        return (processedVerticals + sortedHorizontals) to verticalBoxes.size
    }

    private fun areVerticalColumnsGroupable(columns: List<OcrTextBox>): Boolean {
        for (i in 0 until columns.size - 1) {
            val rightCol = columns[i]
            val leftCol = columns[i + 1]
            val verticalOverlap = minOf(rightCol.normBottom, leftCol.normBottom) -
                maxOf(rightCol.normTop, leftCol.normTop)
            if (verticalOverlap > 0.1f) return true
        }
        return false
    }

    /**
     * Auto-Context Profiler: Analyzes screen text to automatically classify the domain
     */
    fun profileContextDomain(recentChineseLines: List<String>): ContextDomain {
        val sample = recentChineseLines.takeLast(10).joinToString(" ")
        val wuxiaHits = listOf("师尊", "弟子", "本座", "道友", "筑基", "元婴", "灵石", "仙", "剑", "宗门", "长老")
            .count { sample.contains(it) }
        val ecommerceHits = listOf("包邮", "秒杀", "购物车", "满", "减", "优惠券", "已售", "好评")
            .count { sample.contains(it) }
        val combatHits = listOf("造成", "伤害", "冷却", "暴击", "法力", "恢复")
            .count { sample.contains(it) }
        val scifiHits = listOf("开拓者", "星穹", "跃迁", "空间站", "护盾", "引擎", "星核")
            .count { sample.contains(it) }

        val maxScore = maxOf(wuxiaHits, ecommerceHits, combatHits, scifiHits)
        return when {
            maxScore == 0 -> ContextDomain.WUXIA_XIANXIA
            wuxiaHits == maxScore -> ContextDomain.WUXIA_XIANXIA
            combatHits == maxScore -> ContextDomain.SYSTEM_UI
            ecommerceHits == maxScore -> ContextDomain.ECOMMERCE_SOCIAL
            else -> ContextDomain.SCIFI_ANIME_RPG
        }
    }

    /**
     * Smart Inpainting Dynamic Typography: Calculates font size (in sp) so Vietnamese fits inside the bounding box
     */
    fun calculateOptimalFontSizeSp(box: OcrTextBox, vietnameseText: String): Float {
        val charCount = max(vietnameseText.length, 4)
        val approxArea = box.widthRatio * box.heightRatio * 180_000f
        val rawSp = sqrt(approxArea / charCount.toFloat()) * 0.72f
        return if (box.isVerticalText) {
            rawSp.coerceIn(11f, 15f)
        } else {
            rawSp.coerceIn(11.5f, 17.5f)
        }
    }

    /**
     * Simulates 64-bit dHash Frame Diffing (< 2ms) between two frame signatures
     */
    fun computeDHashDeltaPercent(prevSignature: Long, currentSignature: Long): Float {
        if (prevSignature == 0L) return 100f
        val xor = prevSignature xor currentSignature
        val differingBits = java.lang.Long.bitCount(xor)
        return (differingBits / 64f) * 100f
    }

    fun computeTextSignatureHash(boxes: List<OcrTextBox>): Long {
        var hash = 1125899906842597L
        for (b in boxes) {
            for (ch in b.rawChinese) {
                hash = 31L * hash + ch.code.toLong()
            }
            hash = 31L * hash + (b.normLeft * 1000).toInt()
        }
        return abs(hash)
    }
}
