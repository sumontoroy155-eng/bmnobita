package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PerfectSetupEntity
import com.example.data.local.SavedProfileEntity
import com.example.domain.ProfileJsonCodec
import com.example.model.HudControlNode
import org.junit.Assert.assertEquals
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
        assertEquals("BM GAMING PRO", appName)
    }

    @Test
    fun `profile json export and import round-trips cleanly`() {
        val original = listOf(
            SavedProfileEntity(
                id = 1,
                name = "Pro Drag",
                deviceLabel = "Samsung Galaxy A54",
                modeId = "high_fps",
                general = 188,
                redDot = 182,
                scope2x = 166,
                scope4x = 154,
                sniperScope = 120,
                freeLook = 170,
                fireButtonPercent = 46,
                dpiNote = "440dp",
                isFavorite = true
            )
        )
        val json = ProfileJsonCodec.exportProfiles(original)
        val decoded = ProfileJsonCodec.importProfiles(json).getOrThrow()
        assertEquals(1, decoded.size)
        assertEquals("Pro Drag", decoded[0].name)
        assertEquals(188, decoded[0].general)
        assertEquals(46, decoded[0].fireButtonPercent)
        assertTrue(decoded[0].isFavorite)
    }

    @Test
    fun `hud controls serialize and deserialize accurately`() {
        val nodes = listOf(
            HudControlNode("fire", "FIRE", 0.82f, 0.68f, 64, 0.9f),
            HudControlNode("gloo", "GLOO", 0.18f, 0.24f, 48, 0.85f)
        )
        val encoded = ProfileJsonCodec.encodeHudControls(nodes)
        val decoded = ProfileJsonCodec.decodeHudControls(encoded)
        assertEquals(2, decoded.size)
        assertEquals("FIRE", decoded[0].label)
        assertEquals(64, decoded[0].sizeDp)
        assertEquals("GLOO", decoded[1].label)
    }

    @Test
    fun `perfect setup exports full package fields`() {
        val setup = PerfectSetupEntity(
            id = 1,
            title = "POCO F5 3F Setup",
            deviceLabel = "POCO F5",
            general = 192,
            redDot = 184,
            scope2x = 170,
            scope4x = 158,
            sniperScope = 132,
            freeLook = 180,
            recommendedDpiRange = "440-480 dp",
            fireButtonPercent = 46,
            hudFingers = 3
        )
        val json = ProfileJsonCodec.exportPerfectSetup(setup)
        assertTrue(json.contains("POCO F5 3F Setup"))
        assertTrue(json.contains("440-480 dp"))
        assertTrue(json.contains("\"hudFingers\": 3"))
    }
}
