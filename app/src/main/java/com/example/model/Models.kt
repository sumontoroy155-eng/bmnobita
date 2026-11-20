package com.example.model

enum class AppLanguage(val code: String) {
    BN("bn"),
    EN("en");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            if (code == EN.code) EN else BN
    }
}

enum class AppThemeAccent(val id: String, val labelEn: String, val labelBn: String) {
    CRIMSON_RED("crimson", "Crimson Red", "ক্রিমসন রেড"),
    BLOOD_RUBY("ruby", "Blood Ruby", "ব্লাড রুবি"),
    CYBER_EMBER("ember", "Cyber Ember", "সাইবার এম্বার");

    companion object {
        fun fromId(id: String?): AppThemeAccent =
            entries.firstOrNull { it.id == id } ?: CRIMSON_RED
    }
}

enum class SensitivityMode(val id: String) {
    GENERAL("general"),
    DRAG("drag"),
    ONE_TAP("one_tap"),
    BRAZILIAN("brazilian"),
    SMOOTH("smooth"),
    BALANCED("balanced"),
    HIGH_FPS("high_fps"),
    LOW_RAM("low_ram")
}

enum class SkillTier(val id: String) {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    PRO("pro")
}

enum class BrazilianStyle(val id: String) {
    DRAG_HEADSHOT("brazil_drag"),
    ONE_TAP("brazil_onetap"),
    FAST_DRAG("brazil_fast")
}

enum class DpiPreference(val id: String) {
    STANDARD("standard"),
    BALANCED("balanced"),
    FAST_DRAG("fast_drag")
}

enum class FireButtonPreset(val percent: Int) {
    P35(35),
    P40(40),
    P45(45),
    P50(50),
    P55(55),
    P60(60),
    P65(65),
    P70(70)
}

enum class FireButtonStyle(val id: String, val labelEn: String, val labelBn: String) {
    NEON_RING("neon_ring", "Neon Ring", "নিয়ন রিং"),
    SOLID_CORE("solid_core", "Solid Core", "সলিড কোর"),
    CROSSHAIR_DOT("crosshair_dot", "Crosshair Dot", "ক্রসহেয়ার ডট")
}

enum class CrosshairShape(val id: String, val labelEn: String, val labelBn: String) {
    CLASSIC_PLUS("plus", "Classic +", "ক্লাসিক +"),
    PRECISION_DOT("dot", "Precision Dot", "প্রিসিশন ডট"),
    TACTICAL_RING("ring", "Tactical Ring", "ট্যাকটিক্যাল রিং"),
    X_CROSS("xcross", "X-Cross", "এক্স-ক্রস")
}

data class CrosshairConfig(
    val shape: CrosshairShape = CrosshairShape.CLASSIC_PLUS,
    val colorIndex: Int = 0, // 0=Red, 1=Cyan, 2=Emerald, 3=White, 4=Amber
    val sizeDp: Int = 24,
    val opacity: Float = 0.9f
)

enum class AimSimulatorMode(val id: String, val labelEn: String, val labelBn: String) {
    STATIONARY("stationary", "Stationary", "স্থির টার্গেট"),
    MOVING("moving", "Moving", "চলমান টার্গেট"),
    REACTION("reaction", "Reaction", "রিঅ্যাকশন স্পিড"),
    FLICK("flick", "Flick Aim", "ফ্লিক এইম"),
    TRACKING("tracking", "Tracking", "ট্র্যাকিং এইম")
}

enum class BmRankTier(
    val minXp: Int,
    val titleEn: String,
    val titleBn: String
) {
    BEGINNER(0, "Beginner", "বিগিনার"),
    BRONZE(150, "Bronze", "ব্রোঞ্জ"),
    SILVER(350, "Silver", "সিলভার"),
    GOLD(650, "Gold", "গোল্ড"),
    PLATINUM(1050, "Platinum", "প্ল্যাটিনাম"),
    DIAMOND(1600, "Diamond", "ডায়মন্ড"),
    MASTER(2400, "Master", "মাস্টার"),
    BM_LEGEND(3500, "BM Legend", "বিএম লেজেন্ড");

    companion object {
        fun fromXp(xp: Int): BmRankTier =
            entries.lastOrNull { xp >= it.minXp } ?: BEGINNER
    }
}

enum class ChallengeDifficulty(val id: String, val labelEn: String, val labelBn: String, val targetScore: Int, val xpReward: Int) {
    EASY("easy", "Easy", "সহজ", 120, 40),
    MEDIUM("medium", "Medium", "মাঝারি", 200, 75),
    HARD("hard", "Hard", "কঠিন", 280, 115),
    PRO("pro", "Pro", "প্রো", 360, 175)
}

data class DailyChallengeSpec(
    val dateKey: String,
    val difficulty: ChallengeDifficulty,
    val mode: AimSimulatorMode,
    val titleBn: String,
    val titleEn: String
)

data class SensitivityValues(
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniperScope: Int,
    val freeLook: Int
) {
    fun clamped(): SensitivityValues = copy(
        general = general.coerceIn(0, 200),
        redDot = redDot.coerceIn(0, 200),
        scope2x = scope2x.coerceIn(0, 200),
        scope4x = scope4x.coerceIn(0, 200),
        sniperScope = sniperScope.coerceIn(0, 200),
        freeLook = freeLook.coerceIn(0, 200)
    )
}

data class DetectedDeviceInfo(
    val brand: String,
    val model: String,
    val androidVersion: String,
    val sdkInt: Int,
    val resolutionLabel: String,
    val widthPx: Int,
    val heightPx: Int,
    val widthDp: Int,
    val heightDp: Int,
    val densityDpi: Int?,
    val xdpi: Float,
    val ydpi: Float,
    val smallestWidthDp: Int?,
    val estimatedInches: Double?,
    val refreshRateHz: Int?,
    val supportedRefreshRates: List<Int>,
    val processorLabel: String,
    val cpuCores: Int,
    val totalRamMb: Long?,
    val availableRamMb: Long?,
    val isLowRamDevice: Boolean,
    val isManualOverride: Boolean = false
) {
    val totalRamGb: Double?
        get() = totalRamMb?.let { it / 1024.0 }
}

data class PerformanceScoreBreakdown(
    val totalScore: Int,
    val cpuPoints: Int,
    val ramPoints: Int,
    val displayPoints: Int,
    val batteryPoints: Int,
    val tierLabelEn: String,
    val tierLabelBn: String
)

data class DeviceCatalogEntry(
    val id: String,
    val brand: String,
    val model: String,
    val ramGb: Int,
    val refreshRateHz: Int,
    val screenInches: Double,
    val processorNote: String
)

data class LiveTelemetry(
    val uiFps: Int = 0,
    val batteryPercent: Int? = null,
    val batteryTempCelsius: Float? = null,
    val isCharging: Boolean = false,
    val availableRamMb: Long? = null,
    val totalRamMb: Long? = null,
    val freeStorageGb: Double? = null,
    val totalStorageGb: Double? = null,
    val networkTypeLabel: String = "--",
    val isNetworkConnected: Boolean = false
)

data class PingResult(
    val isRunning: Boolean = false,
    val targetHost: String = "1.1.1.1",
    val avgMs: Int? = null,
    val minMs: Int? = null,
    val maxMs: Int? = null,
    val jitterMs: Int? = null,
    val packetLossPercent: Int = 0,
    val errorMessage: String? = null
)

data class ChecklistState(
    val appsCleared: Boolean = false,
    val dndReady: Boolean = false,
    val batteryAbove30: Boolean = false,
    val networkChecked: Boolean = false,
    val brightnessLocked: Boolean = false
)

enum class TrainingDiagramType {
    DRAG_HEADSHOT,
    ONE_TAP,
    CLOSE_RANGE,
    LONG_RANGE
}

enum class DragLessonDistance(val id: String, val labelEn: String, val labelBn: String) {
    SHORT_DRAG("short", "Short Drag", "শর্ট ড্র্যাগ"),
    MEDIUM_DRAG("medium", "Medium Drag", "মিডিয়াম ড্র্যাগ"),
    LONG_DRAG("long", "Long Drag", "লং ড্র্যাগ")
}

data class TrainingDrill(
    val id: String,
    val diagramType: TrainingDiagramType,
    val titleBn: String,
    val titleEn: String,
    val subtitleBn: String,
    val subtitleEn: String,
    val stepsBn: List<String>,
    val stepsEn: List<String>,
    val weaponTipBn: String,
    val weaponTipEn: String,
    val targetReps: Int
)

data class WeaponGuideItem(
    val id: String,
    val categoryBn: String,
    val categoryEn: String,
    val weapons: String,
    val recommendedRedDot: String,
    val recommended2x: String,
    val fireButtonRange: String,
    val closeTipBn: String,
    val closeTipEn: String,
    val midTipBn: String,
    val midTipEn: String,
    val longTipBn: String,
    val longTipEn: String
)

data class HudPresetItem(
    val id: String,
    val fingers: Int,
    val titleBn: String,
    val titleEn: String,
    val descBn: String,
    val descEn: String
)

data class HudControlNode(
    val id: String,
    val label: String,
    val xFraction: Float,
    val yFraction: Float,
    val sizeDp: Int,
    val opacity: Float
)

data class CoachInsight(
    val hasData: Boolean,
    val avgAccuracy: Int,
    val avgReactionMs: Int,
    val flickAvg: Int,
    val trackingAvg: Int,
    val weakestAreaEn: String,
    val weakestAreaBn: String,
    val planEn: List<String>,
    val planBn: List<String>,
    val feedbackEn: String,
    val feedbackBn: String
)
