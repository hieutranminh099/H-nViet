package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.TranslatorDatabase
import com.example.data.model.SubtitleStyleConfig
import com.example.data.model.VideoExportMode
import com.example.engine.MultiKeyGeminiEngine
import com.example.engine.ParameterizedTemplateEngine
import com.example.engine.VideoTranslatorMergerEngine
import com.example.ui.VideoStudioViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HánViệt FlashHUD", appName)
    }

    @Test
    fun `cloud vault room database stores and retrieves synced pc emulator projects by 6-digit code`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = TranslatorDatabase.getInstance(context)
        val cloudVaultDao = db.cloudVaultDao()

        val videoEngine = VideoTranslatorMergerEngine(
            ParameterizedTemplateEngine(db.translationCacheDao()),
            MultiKeyGeminiEngine()
        )
        val defaultAccount = videoEngine.getInitialAccountProfiles().first()
        val seeded = videoEngine.getDefaultSeededCloudVaultProjects(defaultAccount)
        cloudVaultDao.insertAllVaultProjects(seeded)

        val project849 = cloudVaultDao.findBySyncCode("849-206")
        assertNotNull("Project with 6-digit sync code 849-206 must exist in Cloud Vault", project849)
        assertEquals(4, project849!!.clipCount)
        assertTrue(project849.assSubtitleContent.contains("\\pos("))

        val project512 = cloudVaultDao.findBySyncCode("512-904")
        assertNotNull("Project with 6-digit sync code 512-904 must exist in Cloud Vault", project512)
    }

    @Test
    fun `video studio viewModel saves project to cloud vault and releases mobile ram buffers`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = VideoStudioViewModel(app)

        vm.updateStyleConfig(
            SubtitleStyleConfig(
                normX = 0.45f,
                normY = 0.76f,
                fontSizeSp = 19.0f
            )
        )
        vm.selectExportMode(VideoExportMode.LOSSLESS_STREAM_COPY)
        vm.saveCurrentProjectToCloudVault("Dự Án Kiểm Thử Tự Động PC -> Mobile")

        val beforeRevoked = vm.uiState.value.mobileMemoryTelemetry.revokedObjectUriCount
        vm.releaseMobileMemoryBuffers()
        val afterTelemetry = vm.uiState.value.mobileMemoryTelemetry

        assertTrue(afterTelemetry.revokedObjectUriCount > beforeRevoked)
        assertTrue(afterTelemetry.allocatedRamMb < afterTelemetry.maxSafeRamBudgetMb)
    }
}
