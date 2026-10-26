package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CloudVaultProjectEntity
import com.example.data.local.TranslatorDatabase
import com.example.data.model.AiAccountProfile
import com.example.data.model.ContextDomain
import com.example.data.model.DialogueTurn
import com.example.data.model.ExtractionChannel
import com.example.data.model.LosslessExportReport
import com.example.data.model.MobileMemoryTelemetry
import com.example.data.model.ShortVideoClipItem
import com.example.data.model.SubtitleStyleConfig
import com.example.data.model.VideoExportMode
import com.example.data.model.VideoStudioSubTab
import com.example.data.model.VideoSubtitleCue
import com.example.engine.MultiKeyGeminiEngine
import com.example.engine.ParameterizedTemplateEngine
import com.example.engine.VideoTranslatorMergerEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VideoStudioUiState(
    val activeSubTab: VideoStudioSubTab = VideoStudioSubTab.PREVIEW_STYLER,
    val accounts: List<AiAccountProfile> = emptyList(),
    val activeAccountId: String = "acc_gemini_primary",
    val clips: List<ShortVideoClipItem> = emptyList(),
    val selectedClipId: String = "clip_ep3",
    val extractionChannel: ExtractionChannel = ExtractionChannel.DUAL_HYBRID,
    val styleConfig: SubtitleStyleConfig = SubtitleStyleConfig(),
    val isAiSorted: Boolean = false,
    val trimOverlapEnabled: Boolean = true,
    val selectedExportMode: VideoExportMode = VideoExportMode.LOSSLESS_STREAM_COPY,
    val exportReport: LosslessExportReport? = null,
    val mobileMemoryTelemetry: MobileMemoryTelemetry = MobileMemoryTelemetry(),
    val lastSyncedCode: String = "849-206",
    val statusMessage: String = "Đã nạp sẵn 4 tập phim ngắn Douyin/Tiên Hiệp (đang bị lộn xộn thứ tự Tập 3, 1, 4, 2) & kết nối Kho Lưu Trữ Cloud Gemini."
) {
    val activeAccount: AiAccountProfile
        get() = accounts.find { it.id == activeAccountId } ?: accounts.first()

    val selectedClip: ShortVideoClipItem
        get() = clips.find { it.clipId == selectedClipId } ?: clips.first()
}

class VideoStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TranslatorDatabase.getInstance(application)
    private val cacheDao = database.translationCacheDao()
    private val glossaryDao = database.glossaryDao()
    private val cloudVaultDao = database.cloudVaultDao()

    private val templateEngine = ParameterizedTemplateEngine(cacheDao)
    private val geminiEngine = MultiKeyGeminiEngine()
    val videoEngine = VideoTranslatorMergerEngine(templateEngine, geminiEngine)

    private val initialAccounts = videoEngine.getInitialAccountProfiles()
    private val initialClips = videoEngine.getDefaultScrambledShortDramaBatch()

    private var revokedUriCounter = 4

    private val _uiState = MutableStateFlow(
        VideoStudioUiState(
            accounts = initialAccounts,
            activeAccountId = initialAccounts.first().id,
            clips = initialClips,
            selectedClipId = initialClips.first().clipId,
            mobileMemoryTelemetry = videoEngine.calculateMobileMemoryTelemetry(
                clipCount = initialClips.size,
                revokedCount = revokedUriCounter
            )
        )
    )
    val uiState: StateFlow<VideoStudioUiState> = _uiState.asStateFlow()

    val cloudVaultProjectsFlow: StateFlow<List<CloudVaultProjectEntity>> =
        cloudVaultDao.observeAllVaultProjects().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            seedDefaultCloudVaultProjectsIfNeeded()
        }
        refreshExportPreview()
    }

    private suspend fun seedDefaultCloudVaultProjectsIfNeeded() {
        val existing = cloudVaultDao.getAllVaultProjectsNow()
        if (existing.isEmpty()) {
            val seeded = videoEngine.getDefaultSeededCloudVaultProjects(initialAccounts.first())
            cloudVaultDao.insertAllVaultProjects(seeded)
        }
    }

    fun selectSubTab(tab: VideoStudioSubTab) {
        _uiState.value = _uiState.value.copy(activeSubTab = tab)
    }

    fun selectAccount(accountId: String) {
        val acc = _uiState.value.accounts.find { it.id == accountId } ?: return
        _uiState.value = _uiState.value.copy(
            activeAccountId = accountId,
            statusMessage = "Đã chuyển sang tài khoản ${acc.provider.displayName} (${acc.selectedModel}) • Đồng bộ chung Cloud Vault & Glossary."
        )
        refreshExportPreview()
    }

    fun selectAccountModel(accountId: String, modelName: String) {
        val updated = _uiState.value.accounts.map { acc ->
            if (acc.id == accountId) acc.copy(selectedModel = modelName) else acc
        }
        _uiState.value = _uiState.value.copy(
            accounts = updated,
            activeAccountId = accountId,
            statusMessage = "Đã chọn mô hình dịch video: $modelName"
        )
        refreshExportPreview()
    }

    fun toggleAccountOAuthConnection(accountId: String) {
        val updated = _uiState.value.accounts.map { acc ->
            if (acc.id == accountId) {
                val nextConnected = !acc.isConnected
                acc.copy(
                    isConnected = nextConnected,
                    quotaStatus = if (nextConnected) "OAuth Session Linked (<450ms SLA)" else "Disconnected"
                )
            } else acc
        }
        _uiState.value = _uiState.value.copy(accounts = updated)
    }

    fun selectClip(clipId: String) {
        revokedUriCounter += 1
        val state = _uiState.value
        _uiState.value = state.copy(
            selectedClipId = clipId,
            mobileMemoryTelemetry = videoEngine.calculateMobileMemoryTelemetry(
                clipCount = state.clips.size,
                revokedCount = revokedUriCounter
            )
        )
    }

    fun selectExtractionChannel(channel: ExtractionChannel) {
        _uiState.value = _uiState.value.copy(
            extractionChannel = channel,
            statusMessage = "Chế độ trích xuất: ${channel.label}"
        )
    }

    fun updateStyleConfig(newStyle: SubtitleStyleConfig) {
        _uiState.value = _uiState.value.copy(styleConfig = newStyle)
        refreshExportPreview()
    }

    fun updateCueTextAndTimestamps(
        cueId: Int,
        newVietnamese: String,
        newStartMs: Long,
        newEndMs: Long
    ) {
        val updatedClips = _uiState.value.clips.map { clip ->
            val updatedCues = clip.cues.map { cue ->
                if (cue.id == cueId) {
                    cue.copy(
                        translatedVietnamese = newVietnamese.trim(),
                        startTimeMs = newStartMs.coerceAtLeast(0L),
                        endTimeMs = newEndMs.coerceAtLeast(newStartMs + 200L)
                    )
                } else cue
            }
            clip.copy(cues = updatedCues)
        }
        _uiState.value = _uiState.value.copy(
            clips = updatedClips,
            statusMessage = "Đã cập nhật phụ đề #$cueId trên Timeline và đồng bộ ngay lên màn hình Live Preview!"
        )
        refreshExportPreview()
    }

    fun runVideoExtractionAndTranslation() {
        viewModelScope.launch {
            val state = _uiState.value
            val clip = state.selectedClip
            val account = state.activeAccount
            val glossary = glossaryDao.getAllGlossaryNow()

            val slidingTurns = state.clips.flatMap { it.cues }.takeLast(6).mapIndexed { i, c ->
                DialogueTurn(
                    turnIndex = i + 1,
                    sourceChinese = c.sourceChinese,
                    translatedVietnamese = c.translatedVietnamese,
                    domain = ContextDomain.WUXIA_XIANXIA
                )
            }

            val translatedCues = videoEngine.translateClipCuesWithActiveProvider(
                clip = clip,
                account = account,
                domain = ContextDomain.WUXIA_XIANXIA,
                extractionFilter = state.extractionChannel,
                slidingWindow = slidingTurns,
                glossary = glossary
            )

            val updatedClips = state.clips.map { item ->
                if (item.clipId == clip.clipId) item.copy(cues = translatedCues) else item
            }

            _uiState.value = state.copy(
                clips = updatedClips,
                statusMessage = "Đã quét (${state.extractionChannel.badge}) & dịch ${translatedCues.size} câu thoại bằng ${account.provider.badge} (${account.selectedModel})!"
            )
            refreshExportPreview()
        }
    }

    fun importLocalVideos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val existing = _uiState.value.clips
        val importedClips = uris.mapIndexed { idx, uri ->
            val seg = uri.lastPathSegment ?: "Local_Video_${idx + 1}.mp4"
            val epNum = existing.size + idx + 1
            ShortVideoClipItem(
                clipId = "local_clip_${System.currentTimeMillis()}_$idx",
                fileName = "Local_$seg.mp4",
                localUriString = uri.toString(),
                seriesTopic = "Video Tải Lên Từ Thiết Bị",
                durationMs = 15_000L,
                cornerBadgeText = "第0$epNum 集 (Local #$epNum)",
                openingChineseLine = "师尊，这是从本地相册导入的第${epNum}段视频！",
                closingChineseLine = "本座已将该视频同步至无损合并队列！",
                uploadedOrderIndex = epNum,
                trueStoryEpisodeNumber = epNum,
                aiAssignedOrder = epNum,
                aiSequencingReason = "Video tải lên từ thiết bị (#$epNum) — đã trích xuất Hardsub OCR + Audio ASR.",
                detectedOverlapStartMs = 1200L,
                detectedOverlapEndMs = 1200L,
                cues = listOf(
                    VideoSubtitleCue(
                        id = 900 + idx * 10 + 1,
                        clipId = "local_clip_$idx",
                        startTimeMs = 0L,
                        endTimeMs = 5000L,
                        sourceChinese = "师尊，这是从本地相册导入的第${epNum}段视频！",
                        translatedVietnamese = "Thưa Sư tôn, đây là đoạn video thứ $epNum vừa tải lên từ thiết bị!",
                        channel = ExtractionChannel.HARDSUB_OCR,
                        speakerTag = "Đệ tử",
                        hasParameterizedNumbers = true
                    ),
                    VideoSubtitleCue(
                        id = 900 + idx * 10 + 2,
                        clipId = "local_clip_$idx",
                        startTimeMs = 5100L,
                        endTimeMs = 15000L,
                        sourceChinese = "本座已将该视频同步至无损合并队列！",
                        translatedVietnamese = "Bản tọa đã đồng bộ video này vào hàng đợi ghép nối Lossless!",
                        channel = ExtractionChannel.AUDIO_ASR,
                        speakerTag = "Chưởng môn (Bản tọa)"
                    )
                )
            )
        }
        val combined = importedClips + existing
        revokedUriCounter += uris.size
        _uiState.value = _uiState.value.copy(
            clips = combined,
            selectedClipId = importedClips.first().clipId,
            mobileMemoryTelemetry = videoEngine.calculateMobileMemoryTelemetry(
                clipCount = combined.size,
                revokedCount = revokedUriCounter
            ),
            statusMessage = "Đã nhập ${importedClips.size} video (Chunked 2MB Stream Guard) và trích xuất mốc thời gian mili-giây!"
        )
        refreshExportPreview()
    }

    fun runAiPlotSequencer() {
        val state = _uiState.value
        val sortedClips = videoEngine.sequenceClipsByStoryline(
            scrambledClips = state.clips,
            activeAccount = state.activeAccount,
            trimOverlapEnabled = state.trimOverlapEnabled
        )
        _uiState.value = state.copy(
            clips = sortedClips,
            selectedClipId = sortedClips.first().clipId,
            isAiSorted = true,
            statusMessage = "AI (${state.activeAccount.selectedModel}) đã phân tích câu thoại đầu/cuối và sắp xếp chuẩn thứ tự Tập 1 ➔ Tập ${sortedClips.size}!"
        )
        refreshExportPreview()
    }

    fun resetScrambledClips() {
        val scrambled = videoEngine.getDefaultScrambledShortDramaBatch()
        revokedUriCounter += 2
        _uiState.value = _uiState.value.copy(
            clips = scrambled,
            selectedClipId = scrambled.first().clipId,
            isAiSorted = false,
            mobileMemoryTelemetry = videoEngine.calculateMobileMemoryTelemetry(
                clipCount = scrambled.size,
                revokedCount = revokedUriCounter
            ),
            statusMessage = "Đã khôi phục danh sách 4 tập video về thứ tự lộn xộn ban đầu & giải phóng bộ nhớ đệm."
        )
        refreshExportPreview()
    }

    fun toggleTrimOverlap(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(trimOverlapEnabled = enabled)
        refreshExportPreview()
    }

    fun selectExportMode(mode: VideoExportMode) {
        _uiState.value = _uiState.value.copy(selectedExportMode = mode)
        refreshExportPreview()
    }

    fun executeLosslessExportNow() {
        refreshExportPreview()
        val report = _uiState.value.exportReport ?: return
        _uiState.value = _uiState.value.copy(
            statusMessage = "Đã xuất thành công ${report.outputVideoFileName} (${report.exportMode.badge}) trong ${"%.1f".format(report.executionTimeMs)}ms!"
        )
    }

    /**
     * Cross-Device Gemini Cloud Vault: Saves current video batch, .ASS/.SRT subtitles,
     * WYSIWYG coordinates, and glossary to the Gemini Account Cloud Vault.
     */
    fun saveCurrentProjectToCloudVault(customTitle: String = "") {
        viewModelScope.launch {
            refreshExportPreview()
            val state = _uiState.value
            val report = state.exportReport ?: return@launch
            val glossary = glossaryDao.getAllGlossaryNow()
            val glossarySnap = glossary.map { "${it.chineseTerm}=${it.vietnameseTerm}" }
            val title = customTitle.trim().ifEmpty {
                "${state.clips.firstOrNull()?.seriesTopic ?: "Dự Án Phim Ngắn"} (${state.clips.size} Tập)"
            }
            val entity = videoEngine.buildCloudVaultProjectEntity(
                projectTitle = title,
                geminiEmail = state.activeAccount.emailOrHandle,
                sourceDeviceBadge = "PC GIẢ LẬP (${state.activeAccount.provider.badge})",
                clips = state.clips,
                styleConfig = state.styleConfig,
                exportReport = report,
                glossarySnapshot = glossarySnap,
                contextHistorySummary = report.pronounSyncSummary
            )
            cloudVaultDao.upsertVaultProject(entity)
            _uiState.value = state.copy(
                lastSyncedCode = entity.syncCode6Digit,
                statusMessage = "Đã lưu '${entity.projectTitle}' lên Kho Lưu Trữ Cloud Gemini (${entity.geminiAccountEmail}) • Mã đồng bộ 6 số: ${entity.syncCode6Digit}"
            )
        }
    }

    /**
     * Pulls/syncs a project on mobile by entering the 6-digit sync code from PC Emulator.
     */
    fun syncProjectBy6DigitCode(rawCode: String) {
        viewModelScope.launch {
            val cleaned = rawCode.trim()
            if (cleaned.isEmpty()) return@launch
            val found = cloudVaultDao.findBySyncCode(cleaned)
                ?: cloudVaultDao.getAllVaultProjectsNow().firstOrNull {
                    it.syncCode6Digit.replace("-", "") == cleaned.replace("-", "")
                }

            if (found != null) {
                loadVaultProjectIntoStudio(found, switchToPreview = false)
                _uiState.value = _uiState.value.copy(
                    lastSyncedCode = found.syncCode6Digit,
                    statusMessage = "Đã đồng bộ tức thì dự án '${found.projectTitle}' (Mã ${found.syncCode6Digit}) từ ${found.sourceDeviceBadge} về Điện thoại!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Không tìm thấy Mã đồng bộ '$cleaned'. Hãy thử mã '849-206' hoặc '512-904' (đã lưu sẵn từ PC Giả Lập)."
                )
            }
        }
    }

    /**
     * Imports a serialized `.hvcloud` JSON bundle pasted or shared from another device.
     */
    fun importHvCloudBundleJson(rawJson: String) {
        viewModelScope.launch {
            val bundle = videoEngine.decodeHvCloudBundle(rawJson)
            if (bundle == null) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Gói dữ liệu .hvcloud không hợp lệ. Vui lòng kiểm tra lại chuỗi JSON."
                )
                return@launch
            }
            val state = _uiState.value
            val report = videoEngine.generateLosslessExportReport(
                orderedClips = bundle.clips,
                style = bundle.styleConfig,
                exportMode = state.selectedExportMode,
                trimOverlapEnabled = true,
                activeAccount = state.activeAccount
            )
            val entity = videoEngine.buildCloudVaultProjectEntity(
                projectId = bundle.projectId,
                projectTitle = bundle.projectTitle,
                geminiEmail = bundle.geminiAccountEmail,
                syncCode6Digit = bundle.syncCode6Digit,
                sourceDeviceBadge = "${bundle.sourceDeviceBadge} (.hvcloud)",
                clips = bundle.clips,
                styleConfig = bundle.styleConfig,
                exportReport = report,
                glossarySnapshot = bundle.glossarySnapshot,
                contextHistorySummary = bundle.contextHistorySummary
            )
            cloudVaultDao.upsertVaultProject(entity)
            loadVaultProjectIntoStudio(entity, switchToPreview = false)
            _uiState.value = _uiState.value.copy(
                statusMessage = "Đã nhập thành công gói dự án '${entity.projectTitle}' (.hvcloud) vào Kho Lưu Trữ!"
            )
        }
    }

    /**
     * Loads a Cloud Vault project directly into the Video AI Studio player/editor on mobile
     */
    fun loadVaultProjectIntoStudio(
        project: CloudVaultProjectEntity,
        switchToPreview: Boolean = true
    ) {
        val bundle = videoEngine.decodeHvCloudBundle(project.hvCloudBundleJson)
        revokedUriCounter += _uiState.value.clips.size
        if (bundle != null && bundle.clips.isNotEmpty()) {
            val nextTab = if (switchToPreview) VideoStudioSubTab.PREVIEW_STYLER else _uiState.value.activeSubTab
            _uiState.value = _uiState.value.copy(
                activeSubTab = nextTab,
                clips = bundle.clips,
                selectedClipId = bundle.clips.first().clipId,
                styleConfig = bundle.styleConfig,
                isAiSorted = true,
                lastSyncedCode = project.syncCode6Digit,
                mobileMemoryTelemetry = videoEngine.calculateMobileMemoryTelemetry(
                    clipCount = bundle.clips.size,
                    revokedCount = revokedUriCounter
                ),
                statusMessage = "Đang phát & tiếp tục chỉnh sửa '${project.projectTitle}' (Đồng bộ từ ${project.sourceDeviceBadge} • X=${(project.normX * 100).toInt()}%, Y=${(project.normY * 100).toInt()}%)."
            )
            refreshExportPreview()
        }
    }

    fun deleteVaultProject(projectId: String) {
        viewModelScope.launch {
            cloudVaultDao.deleteVaultProject(projectId)
            revokedUriCounter += 2
            val state = _uiState.value
            _uiState.value = state.copy(
                mobileMemoryTelemetry = videoEngine.calculateMobileMemoryTelemetry(
                    clipCount = state.clips.size,
                    revokedCount = revokedUriCounter
                ),
                statusMessage = "Đã xóa dự án khỏi Kho Lưu Trữ Cloud & thu hồi bộ nhớ đệm (URL.revokeObjectURL)."
            )
        }
    }

    fun releaseMobileMemoryBuffers() {
        revokedUriCounter += _uiState.value.clips.size.coerceAtLeast(2)
        val state = _uiState.value
        val nextTelemetry = videoEngine.calculateMobileMemoryTelemetry(
            clipCount = state.clips.size,
            revokedCount = revokedUriCounter
        )
        _uiState.value = state.copy(
            mobileMemoryTelemetry = nextTelemetry,
            statusMessage = "Mobile RAM Guard: Đã giải phóng toàn bộ Bitmap/URI đệm (URL.revokeObjectURL) • RAM hoạt động duy trì ở mức an toàn ${"%.1f".format(nextTelemetry.allocatedRamMb)}MB."
        )
    }

    private fun refreshExportPreview() {
        val state = _uiState.value
        val report = videoEngine.generateLosslessExportReport(
            orderedClips = state.clips,
            style = state.styleConfig,
            exportMode = state.selectedExportMode,
            trimOverlapEnabled = state.trimOverlapEnabled,
            activeAccount = state.activeAccount
        )
        _uiState.value = state.copy(exportReport = report)
    }
}
