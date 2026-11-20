package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.TrainingContent
import com.example.data.local.AimSessionEntity
import com.example.domain.SensitivityEngine
import com.example.i18n.Strings
import com.example.model.AimSimulatorMode
import com.example.model.AppLanguage
import com.example.model.CrosshairConfig
import com.example.model.CrosshairShape
import com.example.model.DragLessonDistance
import com.example.ui.components.DragPracticePadCanvas
import com.example.ui.components.GlowingActionButton
import com.example.ui.components.HeadshotLearningCanvas
import com.example.ui.components.SectionHeader
import com.example.ui.components.TacticalCard
import com.example.ui.components.crosshairColorFromIndex
import com.example.ui.components.drawConfiguredCrosshair
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanHud
import com.example.ui.theme.DeepCarbon
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverText
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurfaceElevated
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.delay

@Composable
fun AimTrainingScreen(
    strings: Strings,
    language: AppLanguage,
    crosshairConfig: CrosshairConfig,
    recentSessions: List<AimSessionEntity>,
    dailyStreak: Int,
    lastChallengeDate: String,
    initialSubTab: Int,
    onUpdateCrosshair: (CrosshairConfig) -> Unit,
    onSaveAimSession: (AimSimulatorMode, hits: Int, totalShots: Int, avgReactionMs: Int, score: Int) -> Unit,
    onRecordSwipePractice: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab) }
    val tabs = listOf(
        strings.aimSimulatorTab,
        strings.headshotAnimTab,
        strings.crosshairTab,
        strings.coachTab,
        strings.weaponGuideTab,
        strings.challengeTab
    )

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DeepCarbon,
            contentColor = PureWhite,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.testTag("training_subtab_$index"),
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == index) PureWhite else SilverText
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> AimSimulatorTab(
                strings = strings,
                language = language,
                crosshair = crosshairConfig,
                recentSessions = recentSessions,
                onSaveSession = onSaveAimSession
            )
            1 -> HeadshotLearningTab(
                strings = strings,
                language = language,
                onPracticeAttempt = onRecordSwipePractice
            )
            2 -> CustomCrosshairTab(
                strings = strings,
                language = language,
                config = crosshairConfig,
                onUpdate = onUpdateCrosshair
            )
            3 -> BmCoachTab(
                language = language,
                sessions = recentSessions
            )
            4 -> WeaponMasterTab(
                language = language
            )
            5 -> DailyChallengeTab(
                strings = strings,
                language = language,
                dailyStreak = dailyStreak,
                lastCompletedDate = lastChallengeDate,
                onLaunchSim = { selectedTab = 0 }
            )
        }
    }
}

@Composable
private fun AimSimulatorTab(
    strings: Strings,
    language: AppLanguage,
    crosshair: CrosshairConfig,
    recentSessions: List<AimSessionEntity>,
    onSaveSession: (AimSimulatorMode, Int, Int, Int, Int) -> Unit
) {
    var mode by remember { mutableStateOf(AimSimulatorMode.STATIONARY) }
    var isRunning by remember { mutableStateOf(false) }
    var timeLeftSec by remember { mutableIntStateOf(20) }
    var arenaSize by remember { mutableStateOf(IntSize(600, 420)) }
    var targetPos by remember { mutableStateOf(Offset(300f, 200f)) }
    var targetVisible by remember { mutableStateOf(true) }
    var vx by remember { mutableFloatStateOf(4.5f) }
    var vy by remember { mutableFloatStateOf(3.2f) }
    var hits by remember { mutableIntStateOf(0) }
    var shots by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var targetSpawnTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var reactionWaitUntil by remember { mutableLongStateOf(0L) }
    var reactionSumMs by remember { mutableLongStateOf(0L) }
    var lastTapPos by remember { mutableStateOf<Offset?>(null) }
    var trackingActivePos by remember { mutableStateOf<Offset?>(null) }
    var trackingTickAccum by remember { mutableIntStateOf(0) }

    fun spawnTarget(forReactionWait: Boolean = false) {
        val w = arenaSize.width.coerceAtLeast(240).toFloat()
        val h = arenaSize.height.coerceAtLeast(240).toFloat()
        targetPos = Offset(
            x = Random.nextFloat() * (w - 110f) + 55f,
            y = Random.nextFloat() * (h - 110f) + 55f
        )
        vx = (if (Random.nextBoolean()) 1f else -1f) * (4f + Random.nextFloat() * 4f)
        vy = (if (Random.nextBoolean()) 1f else -1f) * (3f + Random.nextFloat() * 3.5f)
        if (forReactionWait && mode == AimSimulatorMode.REACTION) {
            targetVisible = false
            reactionWaitUntil = System.currentTimeMillis() + Random.nextLong(350L, 1050L)
        } else {
            targetVisible = true
            targetSpawnTime = System.currentTimeMillis()
        }
    }

    LaunchedEffect(isRunning, mode) {
        if (!isRunning) return@LaunchedEffect
        var msAccum = 0
        while (isRunning && timeLeftSec > 0) {
            delay(32)
            msAccum += 32
            if (msAccum >= 1000) {
                msAccum = 0
                timeLeftSec--
            }
            if (mode == AimSimulatorMode.REACTION && !targetVisible && System.currentTimeMillis() >= reactionWaitUntil) {
                targetVisible = true
                targetSpawnTime = System.currentTimeMillis()
            }
            if (mode == AimSimulatorMode.MOVING || mode == AimSimulatorMode.TRACKING) {
                val w = arenaSize.width.coerceAtLeast(240).toFloat()
                val h = arenaSize.height.coerceAtLeast(240).toFloat()
                val nx = targetPos.x + vx
                val ny = targetPos.y + vy
                if (nx < 48f || nx > w - 48f) vx = -vx
                if (ny < 48f || ny > h - 48f) vy = -vy
                targetPos = Offset(nx.coerceIn(48f, w - 48f), ny.coerceIn(48f, h - 48f))
            }
            if (mode == AimSimulatorMode.TRACKING && trackingActivePos != null) {
                trackingTickAccum += 32
                if (trackingTickAccum >= 180) {
                    trackingTickAccum = 0
                    shots++
                    val dist = hypot(trackingActivePos!!.x - targetPos.x, trackingActivePos!!.y - targetPos.y)
                    if (dist <= 62f) {
                        hits++
                        score += 12
                        reactionSumMs += 160L
                    }
                }
            }
        }
        if (timeLeftSec <= 0 && isRunning) {
            isRunning = false
            val avgReact = if (hits > 0) (reactionSumMs / hits).toInt() else 600
            onSaveSession(mode, hits, shots, avgReact, score)
        }
    }

    val accuracy = if (shots > 0) ((hits * 100f) / shots).roundToInt() else 0
    val avgReact = if (hits > 0) (reactionSumMs / hits).toInt() else 0
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AimSimulatorMode.entries.forEach { m ->
                    FilterChip(
                        selected = mode == m,
                        onClick = { if (!isRunning) mode = m },
                        label = { Text(if (language == AppLanguage.BN) m.labelBn else m.labelEn) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primary, selectedLabelColor = PureWhite)
                    )
                }
            }
        }

        item {
            TacticalCard(highlightBorder = true) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${strings.scoreLabel}: $score", style = MaterialTheme.typography.titleMedium, color = EmeraldOnline)
                    Text("${strings.accuracyLabel}: $accuracy%", style = MaterialTheme.typography.titleMedium, color = CyanHud)
                    Text("${timeLeftSec}s", style = MaterialTheme.typography.titleMedium, color = AmberWarning)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(ObsidianBlack)
                        .border(1.5.dp, primary.copy(alpha = 0.7f), MaterialTheme.shapes.medium)
                        .onSizeChanged { arenaSize = it }
                        .pointerInput(isRunning, mode) {
                            if (mode == AimSimulatorMode.TRACKING) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        if (isRunning) {
                                            trackingActivePos = offset
                                            lastTapPos = offset
                                        }
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        if (isRunning) {
                                            trackingActivePos = change.position
                                            lastTapPos = change.position
                                        }
                                    },
                                    onDragEnd = { trackingActivePos = null },
                                    onDragCancel = { trackingActivePos = null }
                                )
                            } else {
                                detectTapGestures { tap ->
                                    if (!isRunning) return@detectTapGestures
                                    lastTapPos = tap
                                    shots++
                                    if (!targetVisible) return@detectTapGestures
                                    val dist = hypot(tap.x - targetPos.x, tap.y - targetPos.y)
                                    val hitRadius = if (mode == AimSimulatorMode.FLICK) 52f else 58f
                                    if (dist <= hitRadius) {
                                        hits++
                                        val react = (System.currentTimeMillis() - targetSpawnTime).coerceIn(90L, 2500L)
                                        reactionSumMs += react
                                        score += if (dist <= 24f) 30 else 20
                                        spawnTarget(forReactionWait = mode == AimSimulatorMode.REACTION)
                                    }
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (isRunning && targetVisible) {
                            drawCircle(primary.copy(alpha = 0.25f), radius = 44f, center = targetPos)
                            drawCircle(secondary, radius = 34f, center = targetPos, style = Stroke(3.5f))
                            drawCircle(PureWhite, radius = 12f, center = targetPos)
                        }
                        val chCenter = trackingActivePos ?: lastTapPos ?: Offset(size.width / 2f, size.height / 2f)
                        drawConfiguredCrosshair(chCenter, crosshair, primary)
                    }
                    if (!isRunning) {
                        Text(
                            text = if (mode == AimSimulatorMode.TRACKING) {
                                if (language == AppLanguage.BN) "স্টার্ট করে চলন্ত টার্গেটের ওপর আঙুল ধরে অনুসরণ করুন" else "Start and drag finger continuously to track moving target"
                            } else {
                                if (language == AppLanguage.BN) "নিচের বাটনে চাপ দিয়ে লক্ষ্যবস্তুতে ট্যাপ করুন" else "Tap Start Drill and hit targets cleanly"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = SilverText,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp)
                        )
                    } else if (mode == AimSimulatorMode.REACTION && !targetVisible) {
                        Text(
                            text = if (language == AppLanguage.BN) "টার্গেটের জন্য অপেক্ষা করুন..." else "WAIT FOR TARGET...",
                            style = MaterialTheme.typography.labelLarge,
                            color = AmberWarning,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("${strings.reactionLabel}: ${avgReact} ms • Hits: $hits/$shots", style = MaterialTheme.typography.labelMedium, color = SilverText)
                Spacer(modifier = Modifier.height(10.dp))

                GlowingActionButton(
                    text = if (isRunning) strings.stopSimBtn else strings.startSimBtn,
                    testTag = "aim_simulator_toggle_button",
                    onClick = {
                        if (isRunning) {
                            isRunning = false
                            trackingActivePos = null
                            val react = if (hits > 0) (reactionSumMs / hits).toInt() else 600
                            onSaveSession(mode, hits, shots, react, score)
                        } else {
                            hits = 0
                            shots = 0
                            score = 0
                            reactionSumMs = 0L
                            trackingTickAccum = 0
                            trackingActivePos = null
                            timeLeftSec = 20
                            lastTapPos = null
                            spawnTarget(forReactionWait = mode == AimSimulatorMode.REACTION)
                            isRunning = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (recentSessions.isNotEmpty()) {
            item {
                SectionHeader(title = if (language == AppLanguage.BN) "সাম্প্রতিক সিমুলেটর স্কোর" else "Recent Drill Scores")
            }
            items(recentSessions.take(5), key = { it.id }) { s ->
                TacticalCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${s.modeId.uppercase()} • ${s.accuracyPercent}%", style = MaterialTheme.typography.titleMedium, color = PureWhite)
                        Text("${s.score} pts (+${s.xpEarned} XP)", style = MaterialTheme.typography.labelMedium, color = EmeraldOnline)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeadshotLearningTab(
    strings: Strings,
    language: AppLanguage,
    onPracticeAttempt: (Int) -> Unit
) {
    var distance by remember { mutableStateOf(DragLessonDistance.MEDIUM_DRAG) }
    var slowMotion by remember { mutableStateOf(false) }
    var replaySeed by remember { mutableIntStateOf(0) }
    val primary = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TacticalCard(highlightBorder = true) {
                SectionHeader(title = strings.headshotAnimTab, badge = if (slowMotion) "0.4x" else "1.0x")
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DragLessonDistance.entries.forEach { d ->
                        FilterChip(
                            selected = distance == d,
                            onClick = { distance = d },
                            modifier = Modifier.weight(1f),
                            label = { Text(if (language == AppLanguage.BN) d.labelBn else d.labelEn, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primary, selectedLabelColor = PureWhite)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HeadshotLearningCanvas(distance = distance, slowMotion = slowMotion, replaySeed = replaySeed)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = slowMotion, onCheckedChange = { slowMotion = it }, colors = SwitchDefaults.colors(checkedTrackColor = primary))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.slowMotionLabel, style = MaterialTheme.typography.bodyMedium, color = PureWhite)
                    }
                    OutlinedButton(onClick = { replaySeed++ }, border = BorderStroke(1.dp, CyanHud)) {
                        Icon(Icons.Default.Replay, contentDescription = null, tint = CyanHud, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.replayAnimBtn, color = CyanHud)
                    }
                }
            }
        }

        item {
            TacticalCard {
                SectionHeader(title = if (language == AppLanguage.BN) "ফিঙ্গার মুভমেন্ট ও প্র্যাকটিস" else "Finger Movement Practice")
                Spacer(modifier = Modifier.height(8.dp))
                DragPracticePadCanvas(
                    hintText = if (language == AppLanguage.BN) "নিচ থেকে উপরে সোজা বা হালকা J-ড্র্যাগ অনুশীলন করুন" else "Swipe smoothly upward from origin to train vertical drag",
                    angleLabel = "Angle",
                    speedLabel = "Speed",
                    resultGreat = if (language == AppLanguage.BN) "চমৎকার সোজা ড্র্যাগ!" else "Clean vertical drag!",
                    resultCurve = if (language == AppLanguage.BN) "সামান্য বাঁকা হয়েছে — আরও সোজা টানুন।" else "Slightly angled — swipe straighter upward.",
                    resultSlow = if (language == AppLanguage.BN) "আঙুলের স্পিড আরেকটু বাড়ান।" else "Increase upward drag speed.",
                    onPracticeAttempt = onPracticeAttempt
                )
            }
        }
    }
}

@Composable
private fun CustomCrosshairTab(
    strings: Strings,
    language: AppLanguage,
    config: CrosshairConfig,
    onUpdate: (CrosshairConfig) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TacticalCard(highlightBorder = true) {
                SectionHeader(title = strings.crosshairTab, badge = "SIMULATOR ONLY")
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(ObsidianBlack)
                        .border(1.dp, TacticalBorder, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawConfiguredCrosshair(Offset(size.width / 2f, size.height / 2f), config, primary)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CrosshairShape.entries.forEach { sh ->
                        FilterChip(
                            selected = config.shape == sh,
                            onClick = { onUpdate(config.copy(shape = sh)) },
                            label = { Text(if (language == AppLanguage.BN) sh.labelBn else sh.labelEn) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primary, selectedLabelColor = PureWhite)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Red", 1 to "Cyan", 2 to "Green", 3 to "White", 4 to "Amber").forEach { (idx, name) ->
                        FilterChip(
                            selected = config.colorIndex == idx,
                            onClick = { onUpdate(config.copy(colorIndex = idx)) },
                            label = { Text(name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = crosshairColorFromIndex(idx, primary),
                                selectedLabelColor = ObsidianBlack
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Size: ${config.sizeDp} dp", style = MaterialTheme.typography.bodyMedium, color = PureWhite)
                Slider(value = config.sizeDp.toFloat(), onValueChange = { onUpdate(config.copy(sizeDp = it.roundToInt())) }, valueRange = 12f..40f)
                Text("Opacity: ${(config.opacity * 100).roundToInt()}%", style = MaterialTheme.typography.bodyMedium, color = PureWhite)
                Slider(value = config.opacity, onValueChange = { onUpdate(config.copy(opacity = it)) }, valueRange = 0.3f..1f)
            }
        }
    }
}

@Composable
private fun BmCoachTab(
    language: AppLanguage,
    sessions: List<AimSessionEntity>
) {
    val insight = remember(sessions) { SensitivityEngine.analyzeCoachInsights(sessions) }
    val plan = if (language == AppLanguage.BN) insight.planBn else insight.planEn

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TacticalCard(highlightBorder = true) {
                SectionHeader(title = "BM GAMING COACH", badge = if (insight.hasData) "ANALYZED" else "NEW")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (language == AppLanguage.BN) insight.feedbackBn else insight.feedbackEn,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PureWhite
                )
                if (insight.hasData) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Acc: ${insight.avgAccuracy}%", style = MaterialTheme.typography.labelMedium, color = EmeraldOnline)
                        Text("React: ${insight.avgReactionMs}ms", style = MaterialTheme.typography.labelMedium, color = CyanHud)
                        Text("Flick: ${insight.flickAvg}%", style = MaterialTheme.typography.labelMedium, color = PureWhite)
                        Text("Track: ${insight.trackingAvg}%", style = MaterialTheme.typography.labelMedium, color = AmberWarning)
                    }
                }
            }
        }
        item {
            TacticalCard {
                Text(if (language == AppLanguage.BN) "ব্যক্তিগত ট্রেনিং প্ল্যান" else "Personalized Training Plan", style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                plan.forEachIndexed { idx, step ->
                    Text("${idx + 1}. $step", style = MaterialTheme.typography.bodyMedium, color = SilverText, modifier = Modifier.padding(vertical = 3.dp))
                }
            }
        }
    }
}

@Composable
private fun WeaponMasterTab(language: AppLanguage) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(TrainingContent.weaponGuides, key = { it.id }) { item ->
            TacticalCard {
                Text(if (language == AppLanguage.BN) item.categoryBn else item.categoryEn, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                Text(item.weapons, style = MaterialTheme.typography.labelMedium, color = CyanHud)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Red Dot: ${item.recommendedRedDot}", style = MaterialTheme.typography.labelSmall, color = PureWhite)
                    Text("2X: ${item.recommended2x}", style = MaterialTheme.typography.labelSmall, color = PureWhite)
                    Text("Fire: ${item.fireButtonRange}", style = MaterialTheme.typography.labelSmall, color = EmeraldOnline)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(if (language == AppLanguage.BN) item.closeTipBn else item.closeTipEn, style = MaterialTheme.typography.bodyMedium, color = SilverText)
                Text(if (language == AppLanguage.BN) item.midTipBn else item.midTipEn, style = MaterialTheme.typography.bodyMedium, color = SilverText)
                Text(if (language == AppLanguage.BN) item.longTipBn else item.longTipEn, style = MaterialTheme.typography.bodyMedium, color = SilverText)
            }
        }
    }
}

@Composable
private fun DailyChallengeTab(
    strings: Strings,
    language: AppLanguage,
    dailyStreak: Int,
    lastCompletedDate: String,
    onLaunchSim: () -> Unit
) {
    val spec = remember { TrainingContent.todaysDailyChallenge() }
    val doneToday = lastCompletedDate == spec.dateKey
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TacticalCard(highlightBorder = true) {
                SectionHeader(title = strings.dailyChallengeTitle, badge = "STREAK ${dailyStreak}d")
                Spacer(modifier = Modifier.height(10.dp))
                Text(if (language == AppLanguage.BN) spec.titleBn else spec.titleEn, style = MaterialTheme.typography.titleLarge, color = PureWhite)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (doneToday) "✓ Completed Today (+${spec.difficulty.xpReward} XP)" else "Target: ${spec.difficulty.targetScore}+ score in ${spec.mode.labelEn}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (doneToday) EmeraldOnline else AmberWarning
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (doneToday) 1f else 0.15f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = EmeraldOnline,
                    trackColor = TacticalBorder
                )
                Spacer(modifier = Modifier.height(12.dp))
                GlowingActionButton(
                    text = strings.playChallengeBtn,
                    onClick = onLaunchSim,
                    testTag = "launch_daily_challenge_sim",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
