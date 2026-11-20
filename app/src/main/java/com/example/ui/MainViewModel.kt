package com.example.ui

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TrainingContent
import com.example.data.local.AimSessionEntity
import com.example.data.local.AppDatabase
import com.example.data.local.HudLayoutEntity
import com.example.data.local.PerfectSetupEntity
import com.example.data.local.PreferencesRepository
import com.example.data.local.ProfileRepository
import com.example.data.local.SavedProfileEntity
import com.example.data.local.TrainingProgressEntity
import com.example.domain.ProfileJsonCodec
import com.example.domain.SensitivityEngine
import com.example.hardware.DeviceInspector
import com.example.model.AimSimulatorMode
import com.example.model.AppLanguage
import com.example.model.AppThemeAccent
import com.example.model.BmRankTier
import com.example.model.BrazilianStyle
import com.example.model.ChecklistState
import com.example.model.CrosshairConfig
import com.example.model.DetectedDeviceInfo
import com.example.model.DeviceCatalogEntry
import com.example.model.DpiPreference
import com.example.model.FireButtonStyle
import com.example.model.HudControlNode
import com.example.model.LiveTelemetry
import com.example.model.PerformanceScoreBreakdown
import com.example.model.PingResult
import com.example.model.SensitivityMode
import com.example.model.SensitivityValues
import com.example.model.SkillTier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val showIntro: Boolean? = null, // null until DataStore resolves, or set false when skipped/finished
    val deviceInfo: DetectedDeviceInfo,
    val performanceScore: PerformanceScoreBreakdown,
    val sensitivityMode: SensitivityMode = SensitivityMode.BALANCED,
    val skillTier: SkillTier = SkillTier.INTERMEDIATE,
    val sensitivityValues: SensitivityValues,
    val fireButtonPercent: Int = 48,
    val fireComparePercent: Int = 55,
    val dpiScreenInches: Double = 6.5,
    val dpiPreference: DpiPreference = DpiPreference.BALANCED,
    val dailyTipIndex: Int = 0,
    val telemetry: LiveTelemetry = LiveTelemetry(),
    val pingResult: PingResult = PingResult(),
    val dndPermissionGranted: Boolean = false,
    val dndActive: Boolean = false,
    val timerTotalSeconds: Int = 300,
    val timerRemainingSeconds: Int = 300,
    val isTimerRunning: Boolean = false,
    val breakReminderMinutes: Int = 30,
    val breakAlertTriggered: Boolean = false,
    val voiceCueEnabled: Boolean = true,
    val voiceCuePromptIndex: Int = 0,
    val hudControls: List<HudControlNode> = defaultHudNodes(3)
)

fun defaultHudNodes(fingers: Int): List<HudControlNode> = buildList {
    add(HudControlNode("fire", "FIRE", 0.82f, 0.68f, 64, 0.92f))
    add(HudControlNode("move", "MOVE", 0.16f, 0.70f, 68, 0.78f))
    add(HudControlNode("jump", "JUMP", 0.88f, 0.44f, 44, 0.85f))
    add(HudControlNode("crouch", "CROUCH", 0.72f, 0.82f, 42, 0.85f))
    add(HudControlNode("scope", "SCOPE", if (fingers >= 4) 0.84f else 0.90f, if (fingers >= 4) 0.24f else 0.34f, 46, 0.88f))
    add(HudControlNode("gloo", "GLOO", if (fingers >= 3) 0.18f else 0.28f, if (fingers >= 3) 0.24f else 0.76f, 48, 0.88f))
    if (fingers >= 3) {
        add(HudControlNode("switch", "SWITCH", 0.34f, 0.22f, 42, 0.82f))
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val inspector = DeviceInspector(application)
    private val database = AppDatabase.getInstance(application)
    private val profileRepository = ProfileRepository(
        profileDao = database.profileDao(),
        trainingProgressDao = database.trainingProgressDao(),
        aimSessionDao = database.aimSessionDao(),
        hudLayoutDao = database.hudLayoutDao(),
        perfectSetupDao = database.perfectSetupDao()
    )
    private val preferencesRepository = PreferencesRepository(application)

    private val initialDevice = inspector.detectDeviceInfo()
    private val initialTelemetry = inspector.readTelemetry(currentUiFps = 60)

    private val _uiState = MutableStateFlow(
        MainUiState(
            deviceInfo = initialDevice,
            performanceScore = SensitivityEngine.computePerformanceScore(initialDevice, initialTelemetry),
            sensitivityValues = SensitivityEngine.generateSensitivity(
                device = initialDevice,
                mode = SensitivityMode.BALANCED,
                skillTier = SkillTier.INTERMEDIATE
            ),
            fireButtonPercent = SensitivityEngine.recommendFireButtonPercent(
                screenInches = initialDevice.estimatedInches ?: 6.5,
                skillTier = SkillTier.INTERMEDIATE
            ),
            dpiScreenInches = initialDevice.estimatedInches ?: 6.5,
            telemetry = initialTelemetry,
            dndPermissionGranted = inspector.isDndPermissionGranted(),
            dndActive = inspector.isDndActive()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val language: StateFlow<AppLanguage> = preferencesRepository.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.BN)

    val themeAccent: StateFlow<AppThemeAccent> = preferencesRepository.themeAccentFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeAccent.CRIMSON_RED)

    val introEnabled: StateFlow<Boolean> = preferencesRepository.introEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val particlesEnabled: StateFlow<Boolean> = preferencesRepository.particlesEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val tutorialDismissed: StateFlow<Boolean> = preferencesRepository.tutorialDismissedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val dpiNote: StateFlow<String> = preferencesRepository.dpiNoteFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val crosshairConfig: StateFlow<CrosshairConfig> = preferencesRepository.crosshairConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CrosshairConfig())

    val fireLabAppearance: StateFlow<Pair<Float, FireButtonStyle>> = preferencesRepository.fireLabAppearanceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.92f to FireButtonStyle.NEON_RING)

    val savedProfiles: StateFlow<List<SavedProfileEntity>> = profileRepository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trainingProgress: StateFlow<List<TrainingProgressEntity>> = profileRepository.allTrainingProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAimSessions: StateFlow<List<AimSessionEntity>> = profileRepository.recentAimSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedHudLayouts: StateFlow<List<HudLayoutEntity>> = profileRepository.savedHudLayouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPerfectSetups: StateFlow<List<PerfectSetupEntity>> = profileRepository.savedPerfectSetups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedChallengeSteps: StateFlow<Set<Int>> = preferencesRepository.completedChallengeStepsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val dailyStreak: StateFlow<Pair<Int, String>> = preferencesRepository.dailyStreakFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0 to "")

    val dailyGamingSeconds: StateFlow<Pair<String, Int>> = preferencesRepository.dailyGamingSecondsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "" to 0)

    val lastBackupTs: StateFlow<Long> = preferencesRepository.lastBackupTimestampFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val checklistState: StateFlow<ChecklistState> = preferencesRepository.checklistFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChecklistState())

    private var timerJob: Job? = null
    private var latestUiFps: Int = 60

    init {
        viewModelScope.launch {
            preferencesRepository.introEnabledFlow.collect { enabled ->
                _uiState.update { state ->
                    if (state.showIntro == null) state.copy(showIntro = enabled) else state
                }
            }
        }
        viewModelScope.launch {
            while (true) {
                refreshTelemetry()
                delay(2500)
            }
        }
    }

    fun dismissIntro() {
        _uiState.update { it.copy(showIntro = false) }
    }

    fun replayIntro() {
        _uiState.update { it.copy(showIntro = true) }
    }

    fun updateUiFps(fps: Int) {
        if (fps > 0) latestUiFps = fps
    }

    fun refreshTelemetry() {
        _uiState.update { state ->
            val tel = inspector.readTelemetry(latestUiFps)
            state.copy(
                telemetry = tel,
                performanceScore = SensitivityEngine.computePerformanceScore(state.deviceInfo, tel),
                dndPermissionGranted = inspector.isDndPermissionGranted(),
                dndActive = inspector.isDndActive()
            )
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val next = if (language.value == AppLanguage.BN) AppLanguage.EN else AppLanguage.BN
            preferencesRepository.setLanguage(next)
        }
    }

    fun setThemeAccent(accent: AppThemeAccent) {
        viewModelScope.launch { preferencesRepository.setThemeAccent(accent) }
    }

    fun setIntroEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setIntroEnabled(enabled) }
    }

    fun setParticlesEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setParticlesEnabled(enabled) }
    }

    fun dismissTutorial() {
        viewModelScope.launch { preferencesRepository.setTutorialDismissed(true) }
    }

    fun showTutorial() {
        viewModelScope.launch { preferencesRepository.setTutorialDismissed(false) }
    }

    fun nextDailyTip() {
        _uiState.update { state ->
            state.copy(dailyTipIndex = (state.dailyTipIndex + 1) % TrainingContent.dailyTipsBn.size)
        }
    }

    fun selectCatalogDevice(entry: DeviceCatalogEntry) {
        _uiState.update { state ->
            val updated = inspector.applyManualCatalogDevice(state.deviceInfo, entry)
            state.copy(
                deviceInfo = updated,
                performanceScore = SensitivityEngine.computePerformanceScore(updated, state.telemetry),
                dpiScreenInches = entry.screenInches,
                sensitivityValues = SensitivityEngine.generateSensitivity(updated, state.sensitivityMode, state.skillTier),
                fireButtonPercent = SensitivityEngine.recommendFireButtonPercent(entry.screenInches, state.skillTier)
            )
        }
    }

    fun applyCustomDeviceSpecs(
        brand: String,
        model: String,
        ramGb: Int,
        refreshRateHz: Int,
        screenInches: Double
    ) {
        val entry = DeviceCatalogEntry(
            id = "custom_${System.currentTimeMillis()}",
            brand = brand.ifBlank { "Android" },
            model = model.ifBlank { "Custom Device" },
            ramGb = ramGb.coerceIn(1, 24),
            refreshRateHz = refreshRateHz.coerceIn(60, 165),
            screenInches = screenInches.coerceIn(4.5, 11.0),
            processorNote = "User Entered Specs"
        )
        selectCatalogDevice(entry)
    }

    fun resetToAutoDetectedDevice() {
        val detected = inspector.detectDeviceInfo()
        _uiState.update { state ->
            state.copy(
                deviceInfo = detected,
                performanceScore = SensitivityEngine.computePerformanceScore(detected, state.telemetry),
                dpiScreenInches = detected.estimatedInches ?: 6.5,
                sensitivityValues = SensitivityEngine.generateSensitivity(detected, state.sensitivityMode, state.skillTier),
                fireButtonPercent = SensitivityEngine.recommendFireButtonPercent(detected.estimatedInches ?: 6.5, state.skillTier)
            )
        }
    }

    fun selectSensitivityMode(mode: SensitivityMode) {
        _uiState.update { state ->
            state.copy(
                sensitivityMode = mode,
                sensitivityValues = SensitivityEngine.generateSensitivity(state.deviceInfo, mode, state.skillTier)
            )
        }
    }

    fun selectSkillTier(tier: SkillTier) {
        _uiState.update { state ->
            state.copy(
                skillTier = tier,
                sensitivityValues = SensitivityEngine.generateSensitivity(state.deviceInfo, state.sensitivityMode, tier),
                fireButtonPercent = SensitivityEngine.recommendFireButtonPercent(state.dpiScreenInches, tier)
            )
        }
    }

    fun recalculateRecommended() {
        _uiState.update { state ->
            state.copy(
                sensitivityValues = SensitivityEngine.generateSensitivity(state.deviceInfo, state.sensitivityMode, state.skillTier),
                fireButtonPercent = SensitivityEngine.recommendFireButtonPercent(state.dpiScreenInches, state.skillTier)
            )
        }
    }

    fun updateSensitivityValues(values: SensitivityValues) {
        _uiState.update { it.copy(sensitivityValues = values.clamped()) }
    }

    fun applyBrazilianPreset(style: BrazilianStyle) {
        _uiState.update { state ->
            state.copy(sensitivityValues = SensitivityEngine.generateBrazilianPreset(style, state.deviceInfo))
        }
    }

    fun updateDpiScreenInches(inches: Double) {
        _uiState.update { it.copy(dpiScreenInches = inches.coerceIn(5.0, 8.5)) }
    }

    fun updateDpiPreference(pref: DpiPreference) {
        _uiState.update { it.copy(dpiPreference = pref) }
    }

    fun saveDpiNote(note: String) {
        viewModelScope.launch { preferencesRepository.setDpiNote(note) }
    }

    fun updateFireButtonPercent(percent: Int) {
        _uiState.update { it.copy(fireButtonPercent = percent.coerceIn(35, 70)) }
    }

    fun updateFireComparePercent(percent: Int) {
        _uiState.update { it.copy(fireComparePercent = percent.coerceIn(35, 70)) }
    }

    fun updateFireLabAppearance(opacity: Float, style: FireButtonStyle) {
        viewModelScope.launch { preferencesRepository.setFireLabAppearance(opacity, style) }
    }

    fun updateCrosshairConfig(config: CrosshairConfig) {
        viewModelScope.launch { preferencesRepository.setCrosshairConfig(config) }
    }

    fun saveCurrentProfile(profileName: String, favorite: Boolean = false) {
        val state = _uiState.value
        val note = dpiNote.value
        viewModelScope.launch {
            profileRepository.saveProfile(
                SavedProfileEntity(
                    name = profileName.ifBlank { "${state.deviceInfo.model} ${state.sensitivityMode.name}" },
                    deviceLabel = "${state.deviceInfo.brand} ${state.deviceInfo.model}",
                    modeId = state.sensitivityMode.id,
                    general = state.sensitivityValues.general,
                    redDot = state.sensitivityValues.redDot,
                    scope2x = state.sensitivityValues.scope2x,
                    scope4x = state.sensitivityValues.scope4x,
                    sniperScope = state.sensitivityValues.sniperScope,
                    freeLook = state.sensitivityValues.freeLook,
                    fireButtonPercent = state.fireButtonPercent,
                    dpiNote = note,
                    isFavorite = favorite
                )
            )
        }
    }

    fun duplicateProfile(profile: SavedProfileEntity) {
        viewModelScope.launch {
            profileRepository.saveProfile(
                profile.copy(
                    id = 0,
                    name = "${profile.name} (Copy)",
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun renameProfile(id: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { profileRepository.renameProfile(id, newName.trim()) }
    }

    fun loadSavedProfile(profile: SavedProfileEntity) {
        _uiState.update { state ->
            state.copy(
                sensitivityValues = profile.toSensitivityValues(),
                fireButtonPercent = profile.fireButtonPercent.coerceIn(35, 70)
            )
        }
        if (profile.dpiNote.isNotBlank()) saveDpiNote(profile.dpiNote)
    }

    fun toggleFavoriteProfile(profile: SavedProfileEntity) {
        viewModelScope.launch { profileRepository.setFavorite(profile.id, !profile.isFavorite) }
    }

    fun deleteSavedProfile(profile: SavedProfileEntity) {
        viewModelScope.launch { profileRepository.deleteProfile(profile.id) }
    }

    fun exportProfilesJson(): String {
        val list = savedProfiles.value.ifEmpty {
            val state = _uiState.value
            listOf(
                SavedProfileEntity(
                    name = "Active Setup",
                    deviceLabel = "${state.deviceInfo.brand} ${state.deviceInfo.model}",
                    modeId = state.sensitivityMode.id,
                    general = state.sensitivityValues.general,
                    redDot = state.sensitivityValues.redDot,
                    scope2x = state.sensitivityValues.scope2x,
                    scope4x = state.sensitivityValues.scope4x,
                    sniperScope = state.sensitivityValues.sniperScope,
                    freeLook = state.sensitivityValues.freeLook,
                    fireButtonPercent = state.fireButtonPercent,
                    dpiNote = dpiNote.value
                )
            )
        }
        viewModelScope.launch {
            preferencesRepository.setLastBackupTimestamp(System.currentTimeMillis())
        }
        return ProfileJsonCodec.exportProfiles(list)
    }

    fun importProfilesJson(json: String): Boolean {
        val parsed = ProfileJsonCodec.importProfiles(json).getOrNull() ?: return false
        viewModelScope.launch {
            profileRepository.importProfiles(parsed)
            preferencesRepository.setLastBackupTimestamp(System.currentTimeMillis())
        }
        return true
    }

    fun savePerfectSetupPackage(title: String, hudFingers: Int) {
        val state = _uiState.value
        val dpiRange = SensitivityEngine.recommendDpiRange(
            state.dpiScreenInches,
            state.dpiPreference,
            state.deviceInfo.smallestWidthDp
        )
        viewModelScope.launch {
            profileRepository.savePerfectSetup(
                PerfectSetupEntity(
                    title = title.ifBlank { "${state.deviceInfo.model} Perfect Setup" },
                    deviceLabel = "${state.deviceInfo.brand} ${state.deviceInfo.model}",
                    general = state.sensitivityValues.general,
                    redDot = state.sensitivityValues.redDot,
                    scope2x = state.sensitivityValues.scope2x,
                    scope4x = state.sensitivityValues.scope4x,
                    sniperScope = state.sensitivityValues.sniperScope,
                    freeLook = state.sensitivityValues.freeLook,
                    recommendedDpiRange = "${dpiRange.first}-${dpiRange.last} dp",
                    fireButtonPercent = state.fireButtonPercent,
                    hudFingers = hudFingers
                )
            )
        }
    }

    fun deletePerfectSetup(setup: PerfectSetupEntity) {
        viewModelScope.launch { profileRepository.deletePerfectSetup(setup.id) }
    }

    fun saveAimPracticeSession(
        mode: AimSimulatorMode,
        hits: Int,
        totalShots: Int,
        avgReactionMs: Int,
        score: Int
    ) {
        val acc = if (totalShots > 0) ((hits * 100f) / totalShots).toInt().coerceIn(0, 100) else 0
        val xp = (score / 4).coerceIn(10, 180)
        val challenge = TrainingContent.todaysDailyChallenge()
        viewModelScope.launch {
            profileRepository.insertAimSession(
                AimSessionEntity(
                    modeId = mode.id,
                    hits = hits,
                    totalShots = totalShots,
                    accuracyPercent = acc,
                    avgReactionMs = avgReactionMs,
                    score = score,
                    xpEarned = xp
                )
            )
            if (score >= challenge.difficulty.targetScore) {
                preferencesRepository.recordDailyChallengeComplete(challenge.dateKey)
            }
        }
    }

    fun incrementDrillRep(drillId: String, score: Int = 90) {
        val currentReps = trainingProgress.value.firstOrNull { it.drillId == drillId }?.completedReps ?: 0
        viewModelScope.launch {
            profileRepository.incrementDrillRep(drillId, currentReps, score)
        }
    }

    fun recordSwipePractice(score: Int) {
        val currentReps = trainingProgress.value.firstOrNull { it.drillId == "drill_drag" }?.completedReps ?: 0
        viewModelScope.launch {
            profileRepository.recordPracticeScore("drill_drag", currentReps, score)
        }
    }

    fun resetAllTrainingProgress() {
        viewModelScope.launch { profileRepository.resetTrainingProgress() }
    }

    fun updateHudControls(controls: List<HudControlNode>) {
        _uiState.update { it.copy(hudControls = controls) }
    }

    fun loadHudPreset(fingers: Int) {
        _uiState.update { it.copy(hudControls = defaultHudNodes(fingers)) }
    }

    fun saveCurrentHudLayout(name: String, fingers: Int) {
        val json = ProfileJsonCodec.encodeHudControls(_uiState.value.hudControls)
        viewModelScope.launch {
            profileRepository.saveHudLayout(
                HudLayoutEntity(
                    name = name.ifBlank { "${fingers}-Finger Custom HUD" },
                    fingers = fingers,
                    controlsJson = json
                )
            )
        }
    }

    fun loadSavedHudLayout(entity: HudLayoutEntity) {
        val decoded = ProfileJsonCodec.decodeHudControls(entity.controlsJson)
        if (decoded.isNotEmpty()) {
            _uiState.update { it.copy(hudControls = decoded) }
        }
    }

    fun deleteHudLayout(entity: HudLayoutEntity) {
        viewModelScope.launch { profileRepository.deleteHudLayout(entity.id) }
    }

    fun exportActiveHudJson(): String = ProfileJsonCodec.encodeHudControls(_uiState.value.hudControls)

    fun updateChecklist(updated: ChecklistState) {
        viewModelScope.launch { preferencesRepository.updateChecklist(updated) }
    }

    fun runPingTest(host: String = "1.1.1.1") {
        if (_uiState.value.pingResult.isRunning) return
        _uiState.update { it.copy(pingResult = PingResult(isRunning = true, targetHost = host)) }
        viewModelScope.launch {
            val res = inspector.runRealPingTest(host = host)
            _uiState.update { it.copy(pingResult = res) }
        }
    }

    fun toggleDnd(): Boolean {
        val toggled = inspector.toggleDndMode()
        refreshTelemetry()
        return toggled
    }

    fun createDndSettingsIntent() = inspector.createDndSettingsIntent()

    fun setTimerDuration(seconds: Int) {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                timerTotalSeconds = seconds.coerceAtLeast(30),
                timerRemainingSeconds = seconds.coerceAtLeast(30),
                isTimerRunning = false
            )
        }
    }

    fun setBreakReminderMinutes(minutes: Int) {
        _uiState.update { it.copy(breakReminderMinutes = minutes.coerceIn(5, 120), breakAlertTriggered = false) }
    }

    fun dismissBreakAlert() {
        _uiState.update { it.copy(breakAlertTriggered = false) }
    }

    fun toggleTimer() {
        val running = _uiState.value.isTimerRunning
        if (running) {
            timerJob?.cancel()
            _uiState.update { it.copy(isTimerRunning = false) }
        } else {
            if (_uiState.value.timerRemainingSeconds <= 0) {
                _uiState.update { it.copy(timerRemainingSeconds = it.timerTotalSeconds, breakAlertTriggered = false) }
            }
            _uiState.update { it.copy(isTimerRunning = true) }
            val dateKey = TrainingContent.todaysDailyChallenge().dateKey
            timerJob = viewModelScope.launch {
                var elapsedTick = 0
                while (_uiState.value.timerRemainingSeconds > 0 && _uiState.value.isTimerRunning) {
                    delay(1000)
                    elapsedTick++
                    if (elapsedTick % 5 == 0) {
                        preferencesRepository.addDailyGamingSeconds(dateKey, 5)
                    }
                    val breakSec = _uiState.value.breakReminderMinutes * 60
                    val hitBreak = elapsedTick > 0 && elapsedTick % breakSec == 0
                    if (hitBreak && _uiState.value.voiceCueEnabled) {
                        playCompletionTone()
                    }
                    _uiState.update { state ->
                        val next = (state.timerRemainingSeconds - 1).coerceAtLeast(0)
                        val nextPrompt = if (next % 15 == 0) (state.voiceCuePromptIndex + 1) % 4 else state.voiceCuePromptIndex
                        state.copy(
                            timerRemainingSeconds = next,
                            isTimerRunning = next > 0,
                            voiceCuePromptIndex = nextPrompt,
                            breakAlertTriggered = state.breakAlertTriggered || hitBreak
                        )
                    }
                }
                if (_uiState.value.timerRemainingSeconds == 0 && _uiState.value.voiceCueEnabled) {
                    playCompletionTone()
                }
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                timerRemainingSeconds = it.timerTotalSeconds,
                isTimerRunning = false,
                breakAlertTriggered = false
            )
        }
    }

    fun toggleVoiceCue() {
        _uiState.update { it.copy(voiceCueEnabled = !it.voiceCueEnabled) }
    }

    fun resetAllPreferences() {
        viewModelScope.launch {
            preferencesRepository.resetAllPreferences()
        }
    }

    fun totalXp(sessions: List<AimSessionEntity>, progress: List<TrainingProgressEntity>, streak: Int): Int {
        val sessionXp = sessions.sumOf { it.xpEarned }
        val drillXp = progress.sumOf { it.completedReps * 15 }
        val streakXp = streak * 50
        return sessionXp + drillXp + streakXp
    }

    fun currentRank(xp: Int): BmRankTier = BmRankTier.fromXp(xp)

    private fun playCompletionTone() {
        runCatching {
            val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
