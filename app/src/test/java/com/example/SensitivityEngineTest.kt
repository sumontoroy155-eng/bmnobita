package com.example

import com.example.data.DeviceCatalog
import com.example.data.local.AimSessionEntity
import com.example.domain.SensitivityEngine
import com.example.model.BmRankTier
import com.example.model.BrazilianStyle
import com.example.model.DetectedDeviceInfo
import com.example.model.DpiPreference
import com.example.model.LiveTelemetry
import com.example.model.SensitivityMode
import com.example.model.SkillTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SensitivityEngineTest {

    private val sampleDevice = DetectedDeviceInfo(
        brand = "Samsung",
        model = "Galaxy A54 5G",
        androidVersion = "Android 14 (API 34)",
        sdkInt = 34,
        resolutionLabel = "1080 × 2340",
        widthPx = 1080,
        heightPx = 2340,
        widthDp = 384,
        heightDp = 832,
        densityDpi = 420,
        xdpi = 403f,
        ydpi = 403f,
        smallestWidthDp = 384,
        estimatedInches = 6.4,
        refreshRateHz = 120,
        supportedRefreshRates = listOf(60, 120),
        processorLabel = "Exynos 1380",
        cpuCores = 8,
        totalRamMb = 8192L,
        availableRamMb = 4096L,
        isLowRamDevice = false
    )

    @Test
    fun `generated sensitivities stay within 0 to 200 across all modes`() {
        SensitivityMode.entries.forEach { mode ->
            SkillTier.entries.forEach { tier ->
                val result = SensitivityEngine.generateSensitivity(sampleDevice, mode, tier)
                assertTrue(result.general in 0..200)
                assertTrue(result.redDot in 0..200)
                assertTrue(result.scope2x in 0..200)
                assertTrue(result.scope4x in 0..200)
                assertTrue(result.sniperScope in 0..200)
                assertTrue(result.freeLook in 0..200)
            }
        }
    }

    @Test
    fun `brazilian presets stay within valid 0 to 200 bounds`() {
        BrazilianStyle.entries.forEach { style ->
            val result = SensitivityEngine.generateBrazilianPreset(style, sampleDevice)
            assertTrue(result.general in 150..200)
            assertTrue(result.redDot in 150..200)
        }
    }

    @Test
    fun `dpi calculator produces safe ordered range`() {
        DpiPreference.entries.forEach { pref ->
            val range = SensitivityEngine.recommendDpiRange(6.5, pref, 384)
            assertTrue(range.first >= 380)
            assertTrue(range.last > range.first)
            assertTrue(range.last <= 580)
        }
    }

    @Test
    fun `performance score is transparent and bounded 0 to 100`() {
        val tel = LiveTelemetry(
            uiFps = 120,
            batteryPercent = 80,
            batteryTempCelsius = 34.5f,
            availableRamMb = 4096L,
            totalRamMb = 8192L
        )
        val breakdown = SensitivityEngine.computePerformanceScore(sampleDevice, tel)
        assertTrue(breakdown.totalScore in 1..100)
        assertEquals(
            breakdown.totalScore,
            breakdown.cpuPoints + breakdown.ramPoints + breakdown.displayPoints + breakdown.batteryPoints
        )
    }

    @Test
    fun `voice command router resolves bengali and english destinations`() {
        assertEquals("sensitivity", SensitivityEngine.resolveVoiceCommandRoute("সেন্সিটিভিটি খোলো"))
        assertEquals("aim_training", SensitivityEngine.resolveVoiceCommandRoute("open aim training"))
        assertEquals("hud_designer", SensitivityEngine.resolveVoiceCommandRoute("HUD designer"))
        assertEquals("gaming_tools", SensitivityEngine.resolveVoiceCommandRoute("পিং চেক করো"))
    }

    @Test
    fun `coach analyzer processes real aim sessions and rank tiers map cleanly`() {
        val sessions = listOf(
            AimSessionEntity(modeId = "flick", hits = 8, totalShots = 10, accuracyPercent = 80, avgReactionMs = 390, score = 200, xpEarned = 50),
            AimSessionEntity(modeId = "tracking", hits = 5, totalShots = 10, accuracyPercent = 50, avgReactionMs = 540, score = 120, xpEarned = 30)
        )
        val insight = SensitivityEngine.analyzeCoachInsights(sessions)
        assertTrue(insight.hasData)
        assertEquals(65, insight.avgAccuracy)
        assertEquals(BmRankTier.BM_LEGEND, BmRankTier.fromXp(3500))
    }

    @Test
    fun `device catalog contains at least 100 models across brands`() {
        assertTrue(DeviceCatalog.devices.size >= 100)
        DeviceCatalog.supportedBrands.forEach { brand ->
            assertTrue(DeviceCatalog.devices.count { it.brand == brand } >= 6)
        }
    }
}
