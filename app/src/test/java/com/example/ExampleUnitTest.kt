package com.example

import com.example.data.model.AiProviderType
import com.example.data.model.ContextDomain
import com.example.data.model.HardsubMaskMode
import com.example.data.model.KeyHealthStatus
import com.example.data.model.OcrTextBox
import com.example.data.model.SubtitleStyleConfig
import com.example.data.model.VideoExportMode
import com.example.engine.MultiKeyGeminiEngine
import com.example.engine.ParameterizedTemplateEngine
import com.example.engine.VideoTranslatorMergerEngine
import com.example.service.ScreenTranslatorOverlayService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun parameterizedTemplateCache_extractsAndInterpolatesUnderOneMillisecond() = runBlocking {
        val engine = ParameterizedTemplateEngine(cacheDao = null)
        val raw1 = "九天玄雷剑：对前方敌人造成1250点火焰伤害，并附加35%暴击率提升，冷却时间8秒。"
        val p1 = engine.parameterize(raw1)

        assertEquals(
            "九天玄雷剑：对前方敌人造成{0}点火焰伤害，并附加{1}暴击率提升，冷却时间{2}秒。",
            p1.templateKey
        )
        assertEquals(listOf("1250", "35%", "8"), p1.extractedParams)

        engine.saveTranslationToCache(
            domain = ContextDomain.SYSTEM_UI,
            parameterized = p1,
            translatedVietnamese = "Cửu Thiên Huyền Lôi Kiếm: Gây {0} điểm sát thương Hỏa, tăng {1} tỷ lệ bạo kích, hồi chiêu {2} giây."
        )

        val raw2 = "九天玄雷剑：对前方敌人造成9840点火焰伤害，并附加60%暴击率提升，冷却时间3秒。"
        val t0 = System.nanoTime()
        val p2 = engine.parameterize(raw2)
        val cached = engine.lookupCachedTranslation(ContextDomain.SYSTEM_UI, p2)
        val elapsedMs = (System.nanoTime() - t0) / 1_000_000.0

        assertNotNull(cached)
        assertTrue("Parameterized lookup must be < 5ms, was ${elapsedMs}ms", elapsedMs < 5.0)
        assertEquals(
            "Cửu Thiên Huyền Lôi Kiếm: Gây 9840 điểm sát thương Hỏa, tăng 60% tỷ lệ bạo kích, hồi chiêu 3 giây.",
            cached!!.first
        )
    }

    @Test
    fun verticalChineseDetection_sortsColumnsRightToLeftWhenAspectRatioOver2Point2() {
        val engine = ParameterizedTemplateEngine(cacheDao = null)

        val leftMiddleCol = OcrTextBox(
            id = 1,
            rawChinese = "一剑光寒十九洲",
            normLeft = 0.50f,
            normTop = 0.10f,
            normRight = 0.62f,
            normBottom = 0.70f
        )
        val rightmostCol = OcrTextBox(
            id = 2,
            rawChinese = "剑气纵横三万里",
            normLeft = 0.78f,
            normTop = 0.10f,
            normRight = 0.90f,
            normBottom = 0.70f
        )

        assertTrue(leftMiddleCol.isVerticalText)
        assertTrue(rightmostCol.isVerticalText)

        val (sorted, verticalCount) = engine.sortAndMergeBoxesWithVerticalDetection(
            listOf(leftMiddleCol, rightmostCol)
        )
        assertEquals(2, verticalCount)
        assertEquals("剑气纵横三万里", sorted[0].rawChinese)
        assertEquals("一剑光寒十九洲", sorted[1].rawChinese)
    }

    @Test
    fun multiKeyRotationPool_failsOverUnderOneMillisecondOn429OrSlowLatency() {
        val gemini = MultiKeyGeminiEngine()
        val initialSlot = gemini.selectNextHealthyKey()
        val (failoverSlot, switchMs) = gemini.triggerInstantFailover(
            failedKeyId = initialSlot.id,
            reason = KeyHealthStatus.COOLDOWN_429
        )

        assertTrue("Key failover must complete quickly", switchMs < 15.0)
        assertTrue("Failover must select a different healthy key", failoverSlot.id != initialSlot.id)
        assertEquals(KeyHealthStatus.HEALTHY, failoverSlot.status)
    }

    @Test
    fun holdToPeek_togglesOverlayPassthroughStateImmediately() {
        ScreenTranslatorOverlayService.setHoldToPeekActive(true)
        assertTrue(ScreenTranslatorOverlayService.isHoldToPeek.value)
        ScreenTranslatorOverlayService.setHoldToPeekActive(false)
        assertTrue(!ScreenTranslatorOverlayService.isHoldToPeek.value)
    }

    @Test
    fun aiPlotSequencer_reordersScrambledShortVideosAndTrimsOverlap() {
        val templateEngine = ParameterizedTemplateEngine(cacheDao = null)
        val geminiEngine = MultiKeyGeminiEngine()
        val videoEngine = VideoTranslatorMergerEngine(templateEngine, geminiEngine)

        val accounts = videoEngine.getInitialAccountProfiles()
        assertTrue(accounts.any { it.provider == AiProviderType.GEMINI })
        assertTrue(accounts.any { it.provider == AiProviderType.OPENAI })

        val scrambled = videoEngine.getDefaultScrambledShortDramaBatch()
        assertEquals(listOf(3, 1, 4, 2), scrambled.map { it.trueStoryEpisodeNumber })

        val ordered = videoEngine.sequenceClipsByStoryline(
            scrambledClips = scrambled,
            activeAccount = accounts.first(),
            trimOverlapEnabled = true
        )
        assertEquals(listOf(1, 2, 3, 4), ordered.map { it.trueStoryEpisodeNumber })
        assertEquals(listOf(1, 2, 3, 4), ordered.map { it.aiAssignedOrder })
    }

    @Test
    fun losslessExportGenerator_producesValidAssWithWysiwygCoordinatesAndLosslessConcatCommand() {
        val templateEngine = ParameterizedTemplateEngine(cacheDao = null)
        val geminiEngine = MultiKeyGeminiEngine()
        val videoEngine = VideoTranslatorMergerEngine(templateEngine, geminiEngine)

        val accounts = videoEngine.getInitialAccountProfiles()
        val ordered = videoEngine.sequenceClipsByStoryline(
            scrambledClips = videoEngine.getDefaultScrambledShortDramaBatch(),
            activeAccount = accounts.first(),
            trimOverlapEnabled = true
        )
        val customStyle = SubtitleStyleConfig(
            normX = 0.50f,
            normY = 0.80f,
            fontSizeSp = 18.0f,
            maskMode = HardsubMaskMode.GAUSSIAN_BLUR
        )

        val report = videoEngine.generateLosslessExportReport(
            orderedClips = ordered,
            style = customStyle,
            exportMode = VideoExportMode.LOSSLESS_STREAM_COPY,
            trimOverlapEnabled = true,
            activeAccount = accounts.first()
        )

        assertEquals(4, report.mergedClipCount)
        assertTrue(report.totalOverlapTrimmedMs > 0L)
        assertTrue(report.ffmpegOrMuxerCommand.contains("-f concat -safe 0"))
        assertTrue(report.ffmpegOrMuxerCommand.contains("-c:v copy"))
        assertTrue(report.generatedAssPreview.contains("\\pos(540,1536)"))
        assertTrue(report.generatedAssPreview.contains("\\blur1.8"))
        assertTrue(report.generatedSrtPreview.contains("-->"))
    }

    @Test
    fun cloudVaultBundle_serializesAndRestoresClipsCoordinatesSubtitlesAndGlossary() {
        val templateEngine = ParameterizedTemplateEngine(cacheDao = null)
        val geminiEngine = MultiKeyGeminiEngine()
        val videoEngine = VideoTranslatorMergerEngine(templateEngine, geminiEngine)

        val account = videoEngine.getInitialAccountProfiles().first()
        val orderedClips = videoEngine.sequenceClipsByStoryline(
            scrambledClips = videoEngine.getDefaultScrambledShortDramaBatch(),
            activeAccount = account,
            trimOverlapEnabled = true
        )
        val style = SubtitleStyleConfig(
            normX = 0.48f,
            normY = 0.77f,
            fontSizeSp = 17.5f,
            maskMode = HardsubMaskMode.GAUSSIAN_BLUR
        )
        val report = videoEngine.generateLosslessExportReport(
            orderedClips = orderedClips,
            style = style,
            exportMode = VideoExportMode.LOSSLESS_STREAM_COPY,
            trimOverlapEnabled = true,
            activeAccount = account
        )

        val entity = videoEngine.buildCloudVaultProjectEntity(
            projectId = "vault_test_849",
            projectTitle = "Phàm Nhân Tu Tiên — PC Sync Test",
            geminiEmail = account.emailOrHandle,
            syncCode6Digit = "849-206",
            sourceDeviceBadge = "PC GIẢ LẬP (LDPlayer 9)",
            clips = orderedClips,
            styleConfig = style,
            exportReport = report,
            glossarySnapshot = listOf("本座=Bản tọa", "师尊=Sư tôn"),
            contextHistorySummary = report.pronounSyncSummary
        )

        assertEquals("849-206", entity.syncCode6Digit)
        assertEquals(0.48f, entity.normX, 0.001f)
        assertEquals(0.77f, entity.normY, 0.001f)
        assertTrue(entity.assSubtitleContent.contains("[Script Info]"))
        assertTrue(entity.srtSubtitleContent.contains("-->"))

        val decoded = videoEngine.decodeHvCloudBundle(entity.hvCloudBundleJson)
        assertNotNull(decoded)
        assertEquals(4, decoded!!.clips.size)
        assertEquals(0.48f, decoded.styleConfig.normX, 0.001f)
        assertEquals(0.77f, decoded.styleConfig.normY, 0.001f)
        assertTrue(decoded.glossarySnapshot.any { it.contains("Bản tọa") })
    }

    @Test
    fun mobileHardwareRamGuard_keepsActiveBufferUnder48MbOn20VideoBatchAndRevokesUrls() {
        val templateEngine = ParameterizedTemplateEngine(cacheDao = null)
        val geminiEngine = MultiKeyGeminiEngine()
        val videoEngine = VideoTranslatorMergerEngine(templateEngine, geminiEngine)

        val telemetry20Clips = videoEngine.calculateMobileMemoryTelemetry(
            clipCount = 20,
            revokedCount = 18
        )
        assertTrue(
            "Mobile active buffer must stay <= 48MB even with 20 clips",
            telemetry20Clips.allocatedRamMb <= 48.0
        )
        assertTrue(telemetry20Clips.allocatedRamMb < telemetry20Clips.maxSafeRamBudgetMb)
        assertEquals(2.0, telemetry20Clips.chunkStreamSizeMb, 0.01)
        assertEquals(18, telemetry20Clips.revokedObjectUriCount)
        assertTrue(telemetry20Clips.touchScrollLockActive)
    }

    @Test
    fun smartSolidMasking_defaultsTo90PercentBackgroundOpacityToEliminateTextOverlap() {
        val defaultStyle = SubtitleStyleConfig()
        assertEquals(0.90f, defaultStyle.maskOpacity, 0.001f)
        assertEquals(AppDestination.VIDEO_STUDIO, AppDestination.entries.first())
    }
}

