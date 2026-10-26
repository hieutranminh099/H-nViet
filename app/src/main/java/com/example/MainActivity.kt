package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.VideoStudioSubTab
import com.example.ui.TranslatorViewModel
import com.example.ui.VideoStudioViewModel
import com.example.ui.screens.BenchmarkLabScreen
import com.example.ui.screens.ContextAndKeysScreen
import com.example.ui.screens.LiveStudioScreen
import com.example.ui.screens.NativeCodeExporterScreen
import com.example.ui.screens.OnboardingGuideModal
import com.example.ui.screens.VideoStudioScreen
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SlateBorderStrong
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SoftIndigo
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class AppDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    VIDEO_STUDIO("video_studio", "Video AI", Icons.Default.MovieFilter),
    LIVE_HUD("live_hud", "Live HUD", Icons.Default.Translate),
    BENCHMARK("benchmark", "Hiệu năng", Icons.Default.Speed),
    CONTEXT_KEYS("context_keys", "Ngữ cảnh", Icons.Default.Key),
    NATIVE_CODE("native_code", "Mã nguồn", Icons.Default.Code)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HanVietFlashHudApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HanVietFlashHudApp(
    viewModel: TranslatorViewModel = viewModel(),
    videoViewModel: VideoStudioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val videoUiState by videoViewModel.uiState.collectAsStateWithLifecycle()
    val cloudVaultProjects by videoViewModel.cloudVaultProjectsFlow.collectAsStateWithLifecycle()
    val cachedTemplates by viewModel.cachedTemplatesFlow.collectAsStateWithLifecycle()
    val glossaryEntries by viewModel.glossaryFlow.collectAsStateWithLifecycle()
    val isOverlayRunning by viewModel.isSystemOverlayRunning.collectAsStateWithLifecycle()

    // Default to Video AI Studio on launch per user preference
    var currentTab by rememberSaveable { mutableStateOf(AppDestination.VIDEO_STUDIO) }
    // Show 3-step Interactive Onboarding modal on first launch and when tapping "❓ Hướng dẫn"
    var showOnboardingModal by rememberSaveable { mutableStateOf(true) }

    // Mandatory BackHandler for custom tab navigation to return to home tab
    if (currentTab != AppDestination.VIDEO_STUDIO) {
        BackHandler {
            currentTab = AppDestination.VIDEO_STUDIO
        }
    }

    if (showOnboardingModal) {
        OnboardingGuideModal(
            onDismiss = { showOnboardingModal = false }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = ObsidianBg,
            topBar = {
                Column {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = SlateSurface,
                            titleContentColor = TextPrimary
                        ),
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    color = PrimaryBlue.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f)),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MovieFilter,
                                            contentDescription = "HánViệt Studio Icon",
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "HánViệt Studio",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Dịch Video & Che Phụ Đề Gốc",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        },
                        actions = {
                            // Cloud Vault quick access pill
                            Surface(
                                color = SlateSurfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, SlateBorderStrong),
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .height(36.dp)
                                    .clickable {
                                        currentTab = AppDestination.VIDEO_STUDIO
                                        videoViewModel.selectSubTab(VideoStudioSubTab.CLOUD_VAULT)
                                    }
                                    .testTag("btn_topbar_cloud_vault")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = "Kho lưu trữ Cloud Gemini",
                                        tint = ImperialGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = "Cloud (${cloudVaultProjects.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextPrimary
                                    )
                                }
                            }

                            // Interactive Onboarding Guide ("❓ Hướng dẫn") button
                            Surface(
                                color = PrimaryBlue.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .height(36.dp)
                                    .clickable { showOnboardingModal = true }
                                    .testTag("btn_topbar_onboarding_guide")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                        contentDescription = "Mở hướng dẫn 3 bước",
                                        tint = SoftIndigo,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Hướng dẫn",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    )
                    HorizontalDivider(thickness = 1.dp, color = SlateSurfaceElevated)
                }
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    Column {
                        HorizontalDivider(thickness = 1.dp, color = SlateSurfaceElevated)
                        NavigationBar(
                            containerColor = SlateSurface,
                            tonalElevation = 0.dp
                        ) {
                            AppDestination.entries.forEach { dest ->
                                NavigationBarItem(
                                    selected = currentTab == dest,
                                    onClick = { currentTab = dest },
                                    icon = {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = dest.title,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = dest.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = PrimaryBlue,
                                        indicatorColor = PrimaryBlue,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${dest.route}")
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(
                        containerColor = SlateSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        AppDestination.entries.forEach { dest ->
                            NavigationRailItem(
                                selected = currentTab == dest,
                                onClick = { currentTab = dest },
                                icon = { Icon(dest.icon, contentDescription = dest.title) },
                                label = {
                                    Text(
                                        text = dest.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlue,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("rail_tab_${dest.route}")
                            )
                        }
                    }
                }

                val contentModifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(ObsidianBg)

                when (currentTab) {
                    AppDestination.VIDEO_STUDIO -> VideoStudioScreen(
                        subTab = videoUiState.activeSubTab,
                        onSelectSubTab = videoViewModel::selectSubTab,
                        accounts = videoUiState.accounts,
                        activeAccount = videoUiState.activeAccount,
                        onSelectAccount = videoViewModel::selectAccount,
                        onSelectAccountModel = videoViewModel::selectAccountModel,
                        onToggleAccountOAuth = videoViewModel::toggleAccountOAuthConnection,
                        clips = videoUiState.clips,
                        selectedClip = videoUiState.selectedClip,
                        onSelectClip = videoViewModel::selectClip,
                        onImportLocalVideos = videoViewModel::importLocalVideos,
                        extractionChannel = videoUiState.extractionChannel,
                        onSelectExtractionChannel = videoViewModel::selectExtractionChannel,
                        onRunVideoExtractionAndTranslation = videoViewModel::runVideoExtractionAndTranslation,
                        styleConfig = videoUiState.styleConfig,
                        onUpdateStyleConfig = videoViewModel::updateStyleConfig,
                        onUpdateCueTextAndTime = videoViewModel::updateCueTextAndTimestamps,
                        isAiSorted = videoUiState.isAiSorted,
                        trimOverlapEnabled = videoUiState.trimOverlapEnabled,
                        onToggleTrimOverlap = videoViewModel::toggleTrimOverlap,
                        onRunAiPlotSequencer = videoViewModel::runAiPlotSequencer,
                        onResetScrambledClips = videoViewModel::resetScrambledClips,
                        selectedExportMode = videoUiState.selectedExportMode,
                        onSelectExportMode = videoViewModel::selectExportMode,
                        exportReport = videoUiState.exportReport,
                        onRunLosslessExport = videoViewModel::executeLosslessExportNow,
                        vaultProjects = cloudVaultProjects,
                        mobileMemoryTelemetry = videoUiState.mobileMemoryTelemetry,
                        lastSyncedCode = videoUiState.lastSyncedCode,
                        onSaveCurrentToVault = videoViewModel::saveCurrentProjectToCloudVault,
                        onSyncBy6DigitCode = videoViewModel::syncProjectBy6DigitCode,
                        onImportHvCloudJson = videoViewModel::importHvCloudBundleJson,
                        onStreamVaultProject = { project ->
                            videoViewModel.loadVaultProjectIntoStudio(project, switchToPreview = true)
                        },
                        onResumeEditVaultProject = { project ->
                            videoViewModel.loadVaultProjectIntoStudio(project, switchToPreview = true)
                        },
                        onDeleteVaultProject = videoViewModel::deleteVaultProject,
                        onReleaseMobileRam = videoViewModel::releaseMobileMemoryBuffers,
                        statusMessage = videoUiState.statusMessage,
                        modifier = contentModifier
                    )
                    AppDestination.LIVE_HUD -> LiveStudioScreen(
                        uiState = uiState,
                        isOverlayRunning = isOverlayRunning,
                        onSelectScene = viewModel::selectScene,
                        onSetScanMode = viewModel::setScanMode,
                        onUpdateLoupe = viewModel::updateLoupePosition,
                        onSetHoldToPeek = viewModel::setHoldToPeek,
                        onRandomizeNumbers = viewModel::randomizeNumbersForTemplateCacheTest,
                        onRetranslateCold = { viewModel.executeRealTimePipeline(forceColdRun = true) },
                        onRetranslateWarm = { viewModel.executeRealTimePipeline(forceColdRun = false) },
                        onToggleInpainting = viewModel::toggleSmartInpainting,
                        onToggleOverlayService = viewModel::toggleSystemOverlayService,
                        modifier = contentModifier
                    )
                    AppDestination.BENCHMARK -> BenchmarkLabScreen(
                        uiState = uiState,
                        cachedTemplates = cachedTemplates,
                        onRunBenchmark = viewModel::runClosedLoopBenchmarkSuite,
                        onClearCache = viewModel::clearAllTranslationCache,
                        modifier = contentModifier
                    )
                    AppDestination.CONTEXT_KEYS -> ContextAndKeysScreen(
                        uiState = uiState,
                        glossaryEntries = glossaryEntries,
                        onSelectDomain = viewModel::setDomainOverride,
                        onSimulateFailover = viewModel::simulateKeyFailover,
                        onResetKeysHealth = viewModel::resetKeyPoolHealth,
                        onAddGlossary = viewModel::addGlossaryTerm,
                        onDeleteGlossary = viewModel::deleteGlossaryTerm,
                        onClearSlidingWindow = viewModel::clearSlidingContextWindow,
                        modifier = contentModifier
                    )
                    AppDestination.NATIVE_CODE -> NativeCodeExporterScreen(
                        modifier = contentModifier
                    )
                }
            }
        }
    }
}
