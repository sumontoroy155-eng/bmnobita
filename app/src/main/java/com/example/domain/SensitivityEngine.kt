package com.example.domain

import com.example.data.local.AimSessionEntity
import com.example.model.AimSimulatorMode
import com.example.model.BrazilianStyle
import com.example.model.CoachInsight
import com.example.model.DetectedDeviceInfo
import com.example.model.DpiPreference
import com.example.model.LiveTelemetry
import com.example.model.PerformanceScoreBreakdown
import com.example.model.SensitivityMode
import com.example.model.SensitivityValues
import com.example.model.SkillTier
import kotlin.math.roundToInt

object SensitivityEngine {

    fun generateSensitivity(
        device: DetectedDeviceInfo,
        mode: SensitivityMode,
        skillTier: SkillTier
    ): SensitivityValues {
        val ramGb = device.totalRamGb ?: 4.0
        val refreshHz = device.refreshRateHz ?: 60
        val screenInches = device.estimatedInches ?: 6.5

        var general = when {
            ramGb <= 3.5 -> 192
            ramGb <= 4.5 -> 186
            ramGb <= 6.5 -> 178
            ramGb <= 8.5 -> 172
            else -> 166
        }

        val hzOffset = when {
            refreshHz >= 144 -> -6
            refreshHz >= 120 -> -4
            refreshHz >= 90 -> 0
            else -> +4
        }

        val screenOffset = when {
            screenInches >= 6.7 -> +4
            screenInches <= 6.2 -> -4
            else -> 0
        }

        val modeDelta = when (mode) {
            SensitivityMode.GENERAL -> 0
            SensitivityMode.DRAG -> +7
            SensitivityMode.ONE_TAP -> +10
            SensitivityMode.BRAZILIAN -> +12
            SensitivityMode.SMOOTH -> -8
            SensitivityMode.BALANCED -> -3
            SensitivityMode.HIGH_FPS -> +5
            SensitivityMode.LOW_RAM -> +9
        }

        val skillDelta = when (skillTier) {
            SkillTier.BEGINNER -> -10
            SkillTier.INTERMEDIATE -> 0
            SkillTier.PRO -> +8
        }

        general = (general + hzOffset + screenOffset + modeDelta + skillDelta).coerceIn(115, 200)
        val redDot = when (mode) {
            SensitivityMode.ONE_TAP -> (general - 2).coerceIn(120, 200)
            SensitivityMode.BRAZILIAN -> (general - 4).coerceIn(120, 200)
            else -> (general - 6 + if (skillTier == SkillTier.PRO) 4 else 0).coerceIn(105, 200)
        }
        val scope2x = (general - 18).coerceIn(90, 195)
        val scope4x = (general - 28).coerceIn(85, 190)
        val sniper = when (skillTier) {
            SkillTier.BEGINNER -> 92
            SkillTier.INTERMEDIATE -> 112
            SkillTier.PRO -> 128
        }
        val freeLook = (general - 15).coerceIn(90, 195)

        return SensitivityValues(
            general = general,
            redDot = redDot,
            scope2x = scope2x,
            scope4x = scope4x,
            sniperScope = sniper,
            freeLook = freeLook
        ).clamped()
    }

    fun generateBrazilianPreset(
        style: BrazilianStyle,
        device: DetectedDeviceInfo
    ): SensitivityValues {
        val hzAdjust = if ((device.refreshRateHz ?: 60) >= 120) -2 else +2
        return when (style) {
            BrazilianStyle.DRAG_HEADSHOT -> SensitivityValues(
                general = 192 + hzAdjust,
                redDot = 186 + hzAdjust,
                scope2x = 172,
                scope4x = 160,
                sniperScope = 114,
                freeLook = 175
            )
            BrazilianStyle.ONE_TAP -> SensitivityValues(
                general = 196 + hzAdjust,
                redDot = 194,
                scope2x = 164,
                scope4x = 148,
                sniperScope = 122,
                freeLook = 168
            )
            BrazilianStyle.FAST_DRAG -> SensitivityValues(
                general = 200,
                redDot = 192,
                scope2x = 180,
                scope4x = 168,
                sniperScope = 128,
                freeLook = 185
            )
        }.clamped()
    }

    fun recommendDpiRange(
        screenInches: Double,
        preference: DpiPreference,
        currentSmallestWidthDp: Int?
    ): IntRange {
        val base = currentSmallestWidthDp?.coerceIn(360, 420) ?: when {
            screenInches >= 6.7 -> 392
            screenInches >= 6.4 -> 384
            else -> 360
        }

        return when (preference) {
            DpiPreference.STANDARD -> (base + 20)..(base + 50)
            DpiPreference.BALANCED -> (base + 55)..(base + 95)
            DpiPreference.FAST_DRAG -> (base + 100)..(base + 145)
        }
    }

    fun recommendFireButtonPercent(
        screenInches: Double,
        skillTier: SkillTier
    ): Int {
        val base = when {
            screenInches >= 6.7 -> 48
            screenInches >= 6.4 -> 46
            else -> 44
        }
        val offset = when (skillTier) {
            SkillTier.BEGINNER -> +4
            SkillTier.INTERMEDIATE -> 0
            SkillTier.PRO -> -4
        }
        return (base + offset).coerceIn(35, 70)
    }

    fun computePerformanceScore(
        device: DetectedDeviceInfo,
        telemetry: LiveTelemetry
    ): PerformanceScoreBreakdown {
        // Transparent 100-point rubric: CPU (25) + RAM (30) + Display Hz (25) + Battery/Thermal (20)
        val cpuPoints = when {
            device.cpuCores >= 8 -> 25
            device.cpuCores >= 6 -> 20
            device.cpuCores >= 4 -> 14
            else -> 9
        }

        val ramGb = device.totalRamGb ?: 4.0
        val freeRatio = if (telemetry.totalRamMb != null && telemetry.availableRamMb != null && telemetry.totalRamMb > 0) {
            telemetry.availableRamMb.toDouble() / telemetry.totalRamMb.toDouble()
        } else 0.45
        val ramBase = when {
            ramGb >= 11.0 -> 24
            ramGb >= 7.5 -> 20
            ramGb >= 5.5 -> 16
            ramGb >= 3.5 -> 12
            else -> 8
        }
        val ramBonus = (freeRatio * 6.0).roundToInt().coerceIn(0, 6)
        val ramPoints = (ramBase + ramBonus).coerceIn(0, 30)

        val hz = device.refreshRateHz ?: 60
        val displayPoints = when {
            hz >= 144 -> 25
            hz >= 120 -> 23
            hz >= 90 -> 18
            else -> 13
        }

        val temp = telemetry.batteryTempCelsius
        val batPct = telemetry.batteryPercent ?: 60
        val tempScore = when {
            temp == null -> 10
            temp <= 36f -> 12
            temp <= 40f -> 9
            temp <= 43f -> 5
            else -> 2
        }
        val batScore = when {
            batPct >= 50 -> 8
            batPct >= 25 -> 6
            else -> 3
        }
        val batteryPoints = (tempScore + batScore).coerceIn(0, 20)

        val total = (cpuPoints + ramPoints + displayPoints + batteryPoints).coerceIn(0, 100)
        val (tierEn, tierBn) = when {
            total >= 85 -> "ESPORTS TIER" to "ই-স্পোর্টস টিয়ার"
            total >= 70 -> "HIGH PERFORMANCE" to "হাই পারফরম্যান্স"
            total >= 52 -> "BALANCED READY" to "ব্যালেন্সড রেডি"
            else -> "LOW-RAM OPTIMIZE" to "লো-র‍্যাম অপ্টিমাইজ"
        }

        return PerformanceScoreBreakdown(
            totalScore = total,
            cpuPoints = cpuPoints,
            ramPoints = ramPoints,
            displayPoints = displayPoints,
            batteryPoints = batteryPoints,
            tierLabelEn = tierEn,
            tierLabelBn = tierBn
        )
    }

    fun analyzeCoachInsights(sessions: List<AimSessionEntity>): CoachInsight {
        if (sessions.isEmpty()) {
            return CoachInsight(
                hasData = false,
                avgAccuracy = 0,
                avgReactionMs = 0,
                flickAvg = 0,
                trackingAvg = 0,
                weakestAreaEn = "No sessions yet",
                weakestAreaBn = "এখনো সেশন নেই",
                planEn = listOf(
                    "Play 1 Stationary drill to measure baseline accuracy.",
                    "Play 1 Flick Aim drill to measure target snap speed.",
                    "Play 1 Tracking drill to evaluate smooth cursor control."
                ),
                planBn = listOf(
                    "বেসলাইন সঠিকতা মাপতে ১টি স্টেশনারি ড্রিল খেলুন।",
                    "ফ্লিক স্পিড বুঝতে ১টি ফ্লিক এইম ড্রিল খেলুন।",
                    "স্মুথ কন্ট্রোল যাচাই করতে ১টি ট্র্যাকিং ড্রিল খেলুন।"
                ),
                feedbackEn = "Complete an Aim Practice Simulator session so BM Coach can analyze your real performance metrics.",
                feedbackBn = "BM কোচ আপনার আসল স্কোর বিশ্লেষণ করতে অন্তত একটি এইম প্র্যাকটিস সিমুলেটর সেশন সম্পূর্ণ করুন।"
            )
        }

        val avgAcc = sessions.map { it.accuracyPercent }.average().roundToInt()
        val avgReact = sessions.map { it.avgReactionMs }.average().roundToInt()
        val flickSessions = sessions.filter { it.modeId == AimSimulatorMode.FLICK.id || it.modeId == AimSimulatorMode.REACTION.id }
        val trackSessions = sessions.filter { it.modeId == AimSimulatorMode.TRACKING.id || it.modeId == AimSimulatorMode.MOVING.id }

        val flickAvg = if (flickSessions.isNotEmpty()) flickSessions.map { it.accuracyPercent }.average().roundToInt() else avgAcc
        val trackAvg = if (trackSessions.isNotEmpty()) trackSessions.map { it.accuracyPercent }.average().roundToInt() else avgAcc

        val reactionScore = (100 - ((avgReact - 250).coerceAtLeast(0) / 8)).coerceIn(20, 100)
        val weakest = listOf(
            Triple("Accuracy", "সঠিকতা (Accuracy)", avgAcc),
            Triple("Reaction Speed", "রিঅ্যাকশন স্পিড", reactionScore),
            Triple("Flick Aim", "ফ্লিক এইম", flickAvg),
            Triple("Tracking", "ট্র্যাকিং এইম", trackAvg)
        ).minByOrNull { it.third }!!

        val planEn = mutableListOf<String>()
        val planBn = mutableListOf<String>()
        if (avgAcc < 72) {
            planEn.add("Slow down flicks slightly and run 2 Stationary drills aiming for 85%+ accuracy.")
            planBn.add("তাড়াহুড়ো কমিয়ে ৮৫%+ সঠিকতার জন্য ২টি স্টেশনারি ড্রিল খেলুন।")
        } else {
            planEn.add("Accuracy is solid ($avgAcc%) — push speed in Flick Aim mode.")
            planBn.add("সঠিকতা ভালো ($avgAcc%) — এবার ফ্লিক এইমে স্পিড বাড়ান।")
        }
        if (avgReact > 520) {
            planEn.add("Run 3 Reaction Speed drills daily to lower response time below 450 ms.")
            planBn.add("রিঅ্যাকশন ৪৫০ ms-এর নিচে নামাতে প্রতিদিন ৩টি রিঅ্যাকশন ড্রিল করুন।")
        } else {
            planEn.add("Great reaction speed (${avgReact} ms) — combine with Moving target tracking.")
            planBn.add("দারুণ রিঅ্যাকশন (${avgReact} ms) — চলমান টার্গেটে ট্র্যাকিং অনুশীলন করুন।")
        }
        planEn.add("Practice 10 straight upward drags on the Headshot Learning pad before ranked play.")
        planBn.add("র‍্যাংকড ম্যাচের আগে হেডশট লার্নিং প্যাডে ১০টি সোজা ড্র্যাগ অনুশীলন করুন।")

        return CoachInsight(
            hasData = true,
            avgAccuracy = avgAcc,
            avgReactionMs = avgReact,
            flickAvg = flickAvg,
            trackingAvg = trackAvg,
            weakestAreaEn = weakest.first,
            weakestAreaBn = weakest.second,
            planEn = planEn,
            planBn = planBn,
            feedbackEn = "Based on ${sessions.size} saved drill(s): Average accuracy $avgAcc%, average reaction ${avgReact}ms. Primary focus area: ${weakest.first}.",
            feedbackBn = "আপনার ${sessions.size}টি সেভ করা ড্রিলের ভিত্তিতে গড় সঠিকতা $avgAcc% এবং রিঅ্যাকশন ${avgReact}ms। মূল উন্নতির জায়গা: ${weakest.second}।"
        )
    }

    fun resolveVoiceCommandRoute(spokenText: String): String? {
        val text = spokenText.lowercase().trim()
        return when {
            text.contains("home") || text.contains("হোম") || text.contains("ড্যাশবোর্ড") -> "home"
            text.contains("sens") || text.contains("সেন্সি") || text.contains("doctor") || text.contains("ডক্টর") || text.contains("fire") || text.contains("ফায়ার") || text.contains("dpi") -> "sensitivity"
            text.contains("aim") || text.contains("train") || text.contains("head") || text.contains("crosshair") || text.contains("এইম") || text.contains("ট্রেনিং") || text.contains("হেড") || text.contains("ক্রস") -> "aim_training"
            text.contains("phone") || text.contains("device") || text.contains("match") || text.contains("ফোন") || text.contains("ডিভাইস") || text.contains("ম্যাচ") -> "phone_match"
            text.contains("hud") || text.contains("layout") || text.contains("ডিজাইন") || text.contains("হুড") -> "hud_designer"
            text.contains("library") || text.contains("backup") || text.contains("লাইব্রেরি") || text.contains("ব্যাকআপ") -> "profile_library"
            text.contains("tool") || text.contains("ping") || text.contains("ram") || text.contains("temp") || text.contains("timer") || text.contains("টুল") || text.contains("পিং") || text.contains("র‍্যাম") || text.contains("টাইমার") -> "gaming_tools"
            text.contains("profile") || text.contains("rank") || text.contains("setting") || text.contains("প্রোফাইল") || text.contains("র‍্যাংক") || text.contains("সেটিং") -> "my_profile"
            else -> null
        }
    }

    fun formatShareText(
        profileName: String,
        deviceLabel: String,
        values: SensitivityValues,
        fireButtonPercent: Int,
        dpiNote: String
    ): String = buildString {
        appendLine("🔥 BM GAMING PRO - $profileName")
        appendLine("📱 Device: $deviceLabel")
        appendLine("────────────────────────")
        appendLine("• General: ${values.general}")
        appendLine("• Red Dot: ${values.redDot}")
        appendLine("• 2X Scope: ${values.scope2x}")
        appendLine("• 4X Scope: ${values.scope4x}")
        appendLine("• Sniper Scope: ${values.sniperScope}")
        appendLine("• Free Look: ${values.freeLook}")
        appendLine("• Fire Button: $fireButtonPercent%")
        if (dpiNote.isNotBlank()) {
            appendLine("• DPI Note: $dpiNote")
        }
        appendLine("────────────────────────")
        append("Recommended manual settings • BM GAMING PRO Ultimate")
    }

    fun computeEstimateInches(widthPx: Int, heightPx: Int, xdpi: Float, ydpi: Float): Double? {
        if (widthPx <= 0 || heightPx <= 0 || xdpi <= 50f || ydpi <= 50f) return null
        val wInches = widthPx / xdpi.toDouble()
        val hInches = heightPx / ydpi.toDouble()
        val diag = kotlin.math.sqrt(wInches * wInches + hInches * hInches)
        return ((diag * 10.0).roundToInt() / 10.0).takeIf { it in 3.5..13.5 }
    }
}
