package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.AppThemeAccent
import com.example.model.ChecklistState
import com.example.model.CrosshairConfig
import com.example.model.CrosshairShape
import com.example.model.FireButtonStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bm_gaming_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language_code")
        val THEME_ACCENT = stringPreferencesKey("theme_accent")
        val INTRO_ENABLED = booleanPreferencesKey("intro_enabled")
        val PARTICLES_ENABLED = booleanPreferencesKey("particles_enabled")
        val TUTORIAL_DISMISSED = booleanPreferencesKey("tutorial_dismissed")
        val DPI_NOTE = stringPreferencesKey("custom_dpi_note")
        val CHALLENGE_DONE = stringSetPreferencesKey("challenge_done_steps")
        val DAILY_STREAK = intPreferencesKey("daily_streak_count")
        val LAST_CHALLENGE_DATE = stringPreferencesKey("last_challenge_date")
        val DAILY_GAMING_DATE = stringPreferencesKey("daily_gaming_date")
        val DAILY_GAMING_SECONDS = intPreferencesKey("daily_gaming_seconds")
        val LAST_BACKUP_TS = longPreferencesKey("last_backup_ts")
        val CROSSHAIR_SHAPE = stringPreferencesKey("crosshair_shape")
        val CROSSHAIR_COLOR = intPreferencesKey("crosshair_color")
        val CROSSHAIR_SIZE = intPreferencesKey("crosshair_size")
        val CROSSHAIR_OPACITY = floatPreferencesKey("crosshair_opacity")
        val FIRE_LAB_OPACITY = floatPreferencesKey("fire_lab_opacity")
        val FIRE_LAB_STYLE = stringPreferencesKey("fire_lab_style")
        val CHECK_APPS = booleanPreferencesKey("check_apps_cleared")
        val CHECK_DND = booleanPreferencesKey("check_dnd_ready")
        val CHECK_BATTERY = booleanPreferencesKey("check_battery_ok")
        val CHECK_NET = booleanPreferencesKey("check_net_ok")
        val CHECK_BRIGHTNESS = booleanPreferencesKey("check_brightness_ok")
    }

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        AppLanguage.fromCode(prefs[Keys.LANGUAGE])
    }

    val themeAccentFlow: Flow<AppThemeAccent> = context.dataStore.data.map { prefs ->
        AppThemeAccent.fromId(prefs[Keys.THEME_ACCENT])
    }

    val introEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.INTRO_ENABLED] ?: true
    }

    val particlesEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.PARTICLES_ENABLED] ?: true
    }

    val tutorialDismissedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.TUTORIAL_DISMISSED] ?: false
    }

    val dpiNoteFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.DPI_NOTE] ?: ""
    }

    val completedChallengeStepsFlow: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        (prefs[Keys.CHALLENGE_DONE] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
    }

    val dailyStreakFlow: Flow<Pair<Int, String>> = context.dataStore.data.map { prefs ->
        (prefs[Keys.DAILY_STREAK] ?: 0) to (prefs[Keys.LAST_CHALLENGE_DATE] ?: "")
    }

    val dailyGamingSecondsFlow: Flow<Pair<String, Int>> = context.dataStore.data.map { prefs ->
        (prefs[Keys.DAILY_GAMING_DATE] ?: "") to (prefs[Keys.DAILY_GAMING_SECONDS] ?: 0)
    }

    val lastBackupTimestampFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_BACKUP_TS] ?: 0L
    }

    val crosshairConfigFlow: Flow<CrosshairConfig> = context.dataStore.data.map { prefs ->
        val shape = CrosshairShape.entries.firstOrNull { it.id == prefs[Keys.CROSSHAIR_SHAPE] }
            ?: CrosshairShape.CLASSIC_PLUS
        CrosshairConfig(
            shape = shape,
            colorIndex = prefs[Keys.CROSSHAIR_COLOR] ?: 0,
            sizeDp = prefs[Keys.CROSSHAIR_SIZE] ?: 24,
            opacity = prefs[Keys.CROSSHAIR_OPACITY] ?: 0.9f
        )
    }

    val fireLabAppearanceFlow: Flow<Pair<Float, FireButtonStyle>> = context.dataStore.data.map { prefs ->
        val opacity = prefs[Keys.FIRE_LAB_OPACITY] ?: 0.92f
        val style = FireButtonStyle.entries.firstOrNull { it.id == prefs[Keys.FIRE_LAB_STYLE] }
            ?: FireButtonStyle.NEON_RING
        opacity to style
    }

    val checklistFlow: Flow<ChecklistState> = context.dataStore.data.map { prefs ->
        ChecklistState(
            appsCleared = prefs[Keys.CHECK_APPS] ?: false,
            dndReady = prefs[Keys.CHECK_DND] ?: false,
            batteryAbove30 = prefs[Keys.CHECK_BATTERY] ?: false,
            networkChecked = prefs[Keys.CHECK_NET] ?: false,
            brightnessLocked = prefs[Keys.CHECK_BRIGHTNESS] ?: false
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setThemeAccent(accent: AppThemeAccent) {
        context.dataStore.edit { it[Keys.THEME_ACCENT] = accent.id }
    }

    suspend fun setIntroEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.INTRO_ENABLED] = enabled }
    }

    suspend fun setParticlesEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PARTICLES_ENABLED] = enabled }
    }

    suspend fun setTutorialDismissed(dismissed: Boolean) {
        context.dataStore.edit { it[Keys.TUTORIAL_DISMISSED] = dismissed }
    }

    suspend fun setDpiNote(note: String) {
        context.dataStore.edit { it[Keys.DPI_NOTE] = note }
    }

    suspend fun setCrosshairConfig(config: CrosshairConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CROSSHAIR_SHAPE] = config.shape.id
            prefs[Keys.CROSSHAIR_COLOR] = config.colorIndex
            prefs[Keys.CROSSHAIR_SIZE] = config.sizeDp
            prefs[Keys.CROSSHAIR_OPACITY] = config.opacity
        }
    }

    suspend fun setFireLabAppearance(opacity: Float, style: FireButtonStyle) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FIRE_LAB_OPACITY] = opacity
            prefs[Keys.FIRE_LAB_STYLE] = style.id
        }
    }

    suspend fun recordDailyChallengeComplete(dateKey: String) {
        context.dataStore.edit { prefs ->
            val prevDate = prefs[Keys.LAST_CHALLENGE_DATE] ?: ""
            if (prevDate != dateKey) {
                prefs[Keys.DAILY_STREAK] = (prefs[Keys.DAILY_STREAK] ?: 0) + 1
                prefs[Keys.LAST_CHALLENGE_DATE] = dateKey
            }
        }
    }

    suspend fun addDailyGamingSeconds(dateKey: String, deltaSeconds: Int) {
        context.dataStore.edit { prefs ->
            val currentDate = prefs[Keys.DAILY_GAMING_DATE] ?: ""
            val prevSec = if (currentDate == dateKey) prefs[Keys.DAILY_GAMING_SECONDS] ?: 0 else 0
            prefs[Keys.DAILY_GAMING_DATE] = dateKey
            prefs[Keys.DAILY_GAMING_SECONDS] = prevSec + deltaSeconds
        }
    }

    suspend fun setLastBackupTimestamp(ts: Long) {
        context.dataStore.edit { it[Keys.LAST_BACKUP_TS] = ts }
    }

    suspend fun toggleChallengeStep(index: Int) {
        context.dataStore.edit { prefs ->
            val current = (prefs[Keys.CHALLENGE_DONE] ?: emptySet()).toMutableSet()
            val key = index.toString()
            if (current.contains(key)) current.remove(key) else current.add(key)
            prefs[Keys.CHALLENGE_DONE] = current
        }
    }

    suspend fun updateChecklist(state: ChecklistState) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CHECK_APPS] = state.appsCleared
            prefs[Keys.CHECK_DND] = state.dndReady
            prefs[Keys.CHECK_BATTERY] = state.batteryAbove30
            prefs[Keys.CHECK_NET] = state.networkChecked
            prefs[Keys.CHECK_BRIGHTNESS] = state.brightnessLocked
        }
    }

    suspend fun resetAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
