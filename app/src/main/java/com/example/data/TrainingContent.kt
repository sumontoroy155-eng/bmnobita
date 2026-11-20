package com.example.data

import com.example.model.AimSimulatorMode
import com.example.model.ChallengeDifficulty
import com.example.model.DailyChallengeSpec
import com.example.model.HudPresetItem
import com.example.model.TrainingDiagramType
import com.example.model.TrainingDrill
import com.example.model.WeaponGuideItem
import java.util.Calendar
import kotlin.math.abs

object TrainingContent {

    val drills: List<TrainingDrill> = listOf(
        TrainingDrill(
            id = "drill_drag",
            diagramType = TrainingDiagramType.DRAG_HEADSHOT,
            titleBn = "স্ট্রেইট ও J-কার্ভ ড্র্যাগ হেডশট",
            titleEn = "Straight & J-Curve Drag Headshot",
            subtitleBn = "শত্রুর বুক থেকে সোজা মাথার দিকে থাম্ব ড্র্যাগ কৌশল",
            subtitleEn = "Smooth thumb drag from upper chest to head hitbox",
            stepsBn = listOf(
                "শত্রুর পা বা বুকের নিচে হোয়াইট ক্রসহেয়ার না রেখে বুক-লেভেলে রাখুন।",
                "ফায়ার বাটনে টাচ করেই একটানা মাঝারি-দ্রুত গতিতে সোজা উপরে (অথবা হালকা J-আকারে) টানুন।",
                "শত্রু দৌড়ালে তার চলার দিক অনুযায়ী সামান্য অ্যাঙ্গেল দিন।"
            ),
            stepsEn = listOf(
                "Keep crosshair near upper chest level before firing rather than at foot level.",
                "Press Fire and glide your thumb straight upward (or subtle J-curve) in one fluid motion.",
                "Match slight horizontal angle when the opponent strafes left or right."
            ),
            weaponTipBn = "SMG (MP40, UMP) এবং AR (SCAR, M4A1) এর জন্য সবচেয়ে কার্যকর।",
            weaponTipEn = "Ideal for SMGs (MP40, UMP) and ARs (SCAR, M4A1).",
            targetReps = 10
        ),
        TrainingDrill(
            id = "drill_onetap",
            diagramType = TrainingDiagramType.ONE_TAP,
            titleBn = "ওয়ান-ট্যাপ হেডশট টাইমিং",
            titleEn = "One-Tap Headshot Timing",
            subtitleBn = "শটগান ও পিস্তলে রোটেশন/C-ড্র্যাগ + কুইক সুইচ",
            subtitleEn = "Rotational C-drag with quick weapon switch rhythm",
            stepsBn = listOf(
                "শত্রুর পাশে হালকা হোয়াইট ক্রসহেয়ার রেখে পজিশন নিন।",
                "ফায়ার বাটন হালকা নিচে থেকে ঘুরিয়ে (C-আকারে) দ্রুত উপরে তুলুন।",
                "গুলি চালানোর সাথে সাথেই কুইক ওয়েপন সুইচ বা স্প্রিন্ট চাপুন রিকয়েল এড়াতে।"
            ),
            stepsEn = listOf(
                "Position crosshair slightly off-target (white dot) at upper-torso height.",
                "Flick in a quick compact C-arc from slightly down-side to straight up.",
                "Immediately trigger weapon switch or sprint to reset recoil animation."
            ),
            weaponTipBn = "M1887, M1014, Desert Eagle এবং Woodpecker এর জন্য সেরা।",
            weaponTipEn = "Best suited for M1887, M1014, Desert Eagle, and Woodpecker.",
            targetReps = 10
        ),
        TrainingDrill(
            id = "drill_close",
            diagramType = TrainingDiagramType.CLOSE_RANGE,
            titleBn = "ক্লোজ-রেঞ্জ কমব্যাট ও ফ্লিক",
            titleEn = "Close-Range Combat & Flick",
            subtitleBn = "খুব কাছের ফাইটে প্রশস্ত ও দ্রুত আপওয়ার্ড ড্র্যাগ",
            subtitleEn = "Wide, rapid upward swipe for point-blank duels",
            stepsBn = listOf(
                "কাছাকাছি ফাইটে General সেন্সিটিভিটি বেশি (১৭৫+) রাখুন দ্রুত টার্নের জন্য।",
                "শত্রু যত কাছে, থাম্ব ড্র্যাগ তত বেশি দূরত্ব ও গতিতে উপরে টানতে হবে।",
                "গ্লু-ওয়াল বা কভারের পর পিক করে ফায়ার করার অভ্যাস করুন।"
            ),
            stepsEn = listOf(
                "Use higher General sensitivity (175+) for instant close-quarters camera rotation.",
                "Closer targets require longer, faster vertical thumb travel to clear torso lock.",
                "Reset behind cover or gloo wall immediately after 4-6 SMG rounds."
            ),
            weaponTipBn = "MP40, MP5, MAC10 ও M1887 ক্লোজ ফাইটে ব্যবহার করুন।",
            weaponTipEn = "Use with MP40, MP5, MAC10, and M1887 in close duels.",
            targetReps = 8
        ),
        TrainingDrill(
            id = "drill_long",
            diagramType = TrainingDiagramType.LONG_RANGE,
            titleBn = "লং-রেঞ্জ স্কোপ এইম ও বার্স্ট কন্ট্রোল",
            titleEn = "Long-Range Scope Aim & Burst",
            subtitleBn = "2X ও 4X স্কোপে হালকা মাইক্রো-ড্র্যাগ ও ৩-৪ বুলেট বার্স্ট",
            subtitleEn = "Controlled micro-drag and short bursts on 2X/4X scopes",
            stepsBn = listOf(
                "দূরের শত্রুতে একটানা স্প্রে না করে ৩-৪ গুলি করে বার্স্ট ফায়ার দিন।",
                "2X/4X স্কোপ ওপেন করার পর খুব সামান্য উপরে মাইক্রো-ড্র্যাগ করুন।",
                "রিকয়েল বেড়ে গেলে স্কোপ বন্ধ করে আবার ওপেন (Scope-in/out) করুন।"
            ),
            stepsEn = listOf(
                "Fire controlled 3-4 round bursts at long distance instead of holding full auto.",
                "Apply gentle micro-drags upward while scoped in with 2X or 4X.",
                "Quickly release and re-ADS if bullet spread starts widening."
            ),
            weaponTipBn = "Woodpecker, AC80, SCAR, Parafal এবং AWM এর জন্য উপযুক্ত।",
            weaponTipEn = "Optimal for Woodpecker, AC80, SCAR, Parafal, and AWM.",
            targetReps = 8
        )
    )

    val weaponGuides: List<WeaponGuideItem> = listOf(
        WeaponGuideItem(
            id = "wg_smg",
            categoryBn = "SMG মাস্টার গাইড",
            categoryEn = "SMG Master Guide",
            weapons = "MP40 • UMP • MP5 • MAC10",
            recommendedRedDot = "175 - 195",
            recommended2x = "155 - 175",
            fireButtonRange = "42% - 50%",
            closeTipBn = "ক্লোজ: MP40-তে দ্রুত ও লম্বা সোজা ড্র্যাগ দিন।",
            closeTipEn = "Close: Rapid long straight upward drag on MP40.",
            midTipBn = "মিড: UMP দিয়ে হালকা J-ড্র্যাগে ২-৩ সেকেন্ডের বার্স্ট চালান।",
            midTipEn = "Mid: Use UMP steady J-drag in controlled 2s bursts.",
            longTipBn = "লং: SMG লং রেঞ্জে এড়িয়ে চলুন, শুধু পিন-সাপ্রেশন দিন।",
            longTipEn = "Long: Avoid full sprays; tap for suppression only."
        ),
        WeaponGuideItem(
            id = "wg_shotgun",
            categoryBn = "শটগান ও ওয়ান-ট্যাপ গাইড",
            categoryEn = "Shotguns & Heavy Pistols",
            weapons = "M1887 • M1014 • Desert Eagle",
            recommendedRedDot = "182 - 200",
            recommended2x = "145 - 165",
            fireButtonRange = "45% - 54%",
            closeTipBn = "ক্লোজ: শত্রুর কাঁধের পাশে হোয়াইট ডট রেখে দ্রুত C-ড্র্যাগ দিন।",
            closeTipEn = "Close: White dot near shoulder, fast C-drag + weapon switch.",
            midTipBn = "মিড: Desert Eagle-এ শত্রু স্থির হওয়ার সাথে সাথে স্ট্রেইট ট্যাপ।",
            midTipEn = "Mid: Wait for enemy micro-pause, execute crisp straight flick.",
            longTipBn = "লং: Woodpecker বা প্রাইমারি AR-এ সুইচ করুন।",
            longTipEn = "Long: Switch to AR or Marksman rifle."
        ),
        WeaponGuideItem(
            id = "wg_ar",
            categoryBn = "অ্যাসল্ট রাইফেল (AR) গাইড",
            categoryEn = "Assault Rifles (AR)",
            weapons = "AK47 • SCAR • M4A1 • Groza",
            recommendedRedDot = "160 - 182",
            recommended2x = "150 - 172",
            fireButtonRange = "46% - 55%",
            closeTipBn = "ক্লোজ: হিপ-ফায়ারে মিডিয়াম স্পিড আপওয়ার্ড ড্র্যাগ ব্যবহার করুন।",
            closeTipEn = "Close: Hip-fire medium upward swipe at chest height.",
            midTipBn = "মিড: Red Dot বা 2X-এ ৪-৫ বুলেট বার্স্ট + হালকা ড্র্যাগ।",
            midTipEn = "Mid: 4-5 round bursts on Red Dot/2X with gentle lift.",
            longTipBn = "লং: 4X স্কোপে ৩ বুলেট করে ট্যাপ করলে রিকয়েল জিরো থাকে।",
            longTipEn = "Long: 3-round tap bursts on 4X keep recoil minimal."
        ),
        WeaponGuideItem(
            id = "wg_marksman",
            categoryBn = "মার্কসম্যান ও স্নাইপার গাইড",
            categoryEn = "Marksman & Sniper",
            weapons = "Woodpecker • AC80 • AWM • M82B",
            recommendedRedDot = "142 - 168",
            recommended2x = "135 - 160",
            fireButtonRange = "48% - 58%",
            closeTipBn = "ক্লোজ: ডাবল স্নাইপার বা দ্রুত কুইক-সুইচ বডি+ফিনিশ।",
            closeTipEn = "Close: Quick-scope switch combo near cover.",
            midTipBn = "মিড: Woodpecker/AC80 সিঙ্গেল ট্যাপ + মাইক্রো ড্র্যাগ।",
            midTipEn = "Mid: Woodpecker single-tap rhythm with micro-lift.",
            longTipBn = "লং: স্নাইপার সেন্সি ১০০-১২৫ রেখে স্থির হেড ট্র্যাকিং করুন।",
            longTipEn = "Long: Keep Sniper Scope 100-125 for steady head tracking."
        )
    )

    val hudLayouts: List<HudPresetItem> = listOf(
        HudPresetItem(
            id = "hud_2f",
            fingers = 2,
            titleBn = "২-ফিঙ্গার ক্লাসিক HUD",
            titleEn = "2-Finger Classic HUD",
            descBn = "ডান থাম্বে ফায়ার + জাম্প/ক্রাউচ, বাম থাম্বে মুভমেন্ট। নতুন ও সব ফোনের জন্য সবচেয়ে স্থিতিশীল।",
            descEn = "Left thumb for joystick movement, right thumb for drag fire, jump & crouch. Most comfortable for beginners."
        ),
        HudPresetItem(
            id = "hud_3f",
            fingers = 3,
            titleBn = "৩-ফিঙ্গার ট্যাকটিক্যাল ক্ল",
            titleEn = "3-Finger Tactical Claw",
            descBn = "বাম ইনডেক্স আঙুলে জাম্প/গ্লু-ওয়াল বা স্কোপ রাখুন, যাতে ডান থাম্ব শুধু নিখুঁত ড্র্যাগ করতে পারে।",
            descEn = "Left index handles Gloo Wall or Scope/Switch while right thumb focuses 100% on pure drag aim."
        ),
        HudPresetItem(
            id = "hud_4f",
            fingers = 4,
            titleBn = "৪-ফিঙ্গার প্রো ক্ল HUD",
            titleEn = "4-Finger Pro Claw HUD",
            descBn = "দুই উপরের আঙুলে স্কোপ, কুইক-সুইচ ও গ্লু-ওয়াল — দ্রুত মুভমেন্ট ও ক্রাউচ-গ্লু কম্বোর জন্য।",
            descEn = "Top left/right index fingers control Scope, Quick Switch & Gloo Wall for high-speed pro combos."
        )
    )

    val dailyTipsBn = listOf(
        "প্রতি ম্যাচের আগে অন্তত ৫ মিনিট এইম সিমুলেটরে হাত ওয়ার্ম-আপ করে নিন।",
        "বারবার সেন্সিটিভিটি পরিবর্তন করবেন না — একটি সেটিংসে কমপক্ষে ৩-৫ দিন খেলুন।",
        "হাত ঘামলে পাউডার বা ফিঙ্গার স্লিভ ব্যবহার করুন, এতে ড্র্যাগ মাঝপথে আটকে যাবে না।",
        "লো-র‍্যাম ফোনে Smooth + High FPS রাখলে টাচ রেসপন্স অনেক ভালো থাকে।",
        "ফায়ার বাটনের নিচে অন্তত ১ ইঞ্চি জায়গা ফাঁকা রাখুন, না হলে স্ক্রিন বর্ডার ড্র্যাগ আটকে দেবে।"
    )

    val dailyTipsEn = listOf(
        "Warm up for 5 minutes in the Aim Practice Simulator before ranked matches.",
        "Stick to one sensitivity profile for 3-5 days so thumb muscle memory stabilizes.",
        "Keep your display clean or use finger sleeves for consistent drag friction.",
        "On 3-4GB RAM phones, Smooth graphics + High FPS keeps touch latency low.",
        "Leave clear space below your Fire Button so thumb sweeps never hit the bezel."
    )

    val batteryTipsBn = listOf(
        "গেম শুরু করার আগে ব্যাকগ্রাউন্ডের ভারী অ্যাপগুলো বন্ধ করুন।",
        "চার্জে বসিয়ে গেম খেললে ব্যাটারি তাপমাত্রা বাড়ে — চার্জ ৩০% এর উপরে রেখে খেলুন।",
        "তাপমাত্রা ৪১°C ছাড়ালে ৫ মিনিট বিরতি দিন থার্মাল থ্রটলিং এড়াতে।",
        "অপ্রয়োজনীয় ব্লুটুথ, লোকেশন ও অটো-সিঙ্ক বন্ধ রাখলে র‍্যাম ফ্রি থাকে।"
    )

    val batteryTipsEn = listOf(
        "Close heavy background apps before launching your match.",
        "Avoid fast-charging while gaming; play unplugged above 30% battery.",
        "Take a 5-minute cooldown break if battery temperature exceeds 41°C.",
        "Disable unused Bluetooth and background sync to free RAM."
    )

    fun todaysDailyChallenge(): DailyChallengeSpec {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val dateKey = "$year-$dayOfYear"
        val difficulties = ChallengeDifficulty.entries
        val modes = AimSimulatorMode.entries
        val diff = difficulties[abs(dayOfYear) % difficulties.size]
        val mode = modes[abs(dayOfYear + 2) % modes.size]
        return DailyChallengeSpec(
            dateKey = dateKey,
            difficulty = diff,
            mode = mode,
            titleBn = "${diff.labelBn} চ্যালেঞ্জ: ${mode.labelBn} (${diff.targetScore}+ স্কোর)",
            titleEn = "${diff.labelEn} Challenge: ${mode.labelEn} (${diff.targetScore}+ pts)"
        )
    }
}
