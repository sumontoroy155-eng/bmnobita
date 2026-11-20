package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CrosshairConfig
import com.example.model.CrosshairShape
import com.example.model.DragLessonDistance
import com.example.model.FireButtonStyle
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanHud
import com.example.ui.theme.DeepCarbon
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.MutedSteel
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverText
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurface
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

fun crosshairColorFromIndex(index: Int, fallbackPrimary: Color): Color = when (index) {
    0 -> fallbackPrimary
    1 -> CyanHud
    2 -> EmeraldOnline
    3 -> PureWhite
    else -> AmberWarning
}

@Composable
fun CinematicBmIntroScreen(
    startLabel: String,
    skipLabel: String,
    subtitleText: String,
    isLowRam: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val bright = MaterialTheme.colorScheme.secondary

    // Master 0..1 timeline over ~3.6 seconds
    val master = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        master.animateTo(1f, animationSpec = tween(durationMillis = 3600, easing = FastOutSlowInEasing))
    }

    val loop = rememberInfiniteTransition(label = "intro_energy")
    val arcPhase by loop.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
        label = "arc_phase"
    )

    val t = master.value
    val logoAlpha = ((t - 0.16f) / 0.25f).coerceIn(0f, 1f)
    val logoScale = 0.72f + (0.34f * ((t - 0.16f) / 0.45f).coerceIn(0f, 1f))
    val titleAlpha = ((t - 0.45f) / 0.22f).coerceIn(0f, 1f)
    val flashAlpha = if (t in 0.70f..0.85f) {
        (1f - abs(t - 0.77f) / 0.08f).coerceIn(0f, 0.35f)
    } else 0f
    val buttonAlpha = ((t - 0.72f) / 0.22f).coerceIn(0f, 1f)
    val particleCount = if (isLowRam) 14 else 28

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Steps 2, 4, 5: Neon particles from edges, electric energy arcs, glowing circular ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h * 0.44f

            // Edge neon particles converging smoothly toward center
            val pProgress = (t / 0.65f).coerceIn(0f, 1f)
            for (i in 0 until particleCount) {
                val angle = (i * (360f / particleCount)) * (PI / 180.0)
                val maxR = size.maxDimension * 0.65f
                val r = maxR * (1f - pProgress * 0.62f)
                val px = cx + (cos(angle) * r).toFloat()
                val py = cy + (sin(angle) * r).toFloat()
                drawCircle(
                    color = bright.copy(alpha = (0.75f * (1f - pProgress * 0.3f)).coerceIn(0.15f, 0.85f)),
                    radius = if (i % 3 == 0) 4.5f else 2.5f,
                    center = Offset(px, py)
                )
            }

            // Glowing energy ring
            val ringAlpha = ((t - 0.28f) / 0.3f).coerceIn(0f, 1f)
            if (ringAlpha > 0f) {
                drawCircle(
                    color = primary.copy(alpha = ringAlpha * 0.35f),
                    radius = 152f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 8f)
                )
                drawArc(
                    brush = Brush.sweepGradient(listOf(Color.Transparent, bright, PureWhite, Color.Transparent)),
                    startAngle = arcPhase,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(cx - 140f, cy - 140f),
                    size = androidx.compose.ui.geometry.Size(280f, 280f),
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )
                // Electric jagged energy arcs around logo
                val boltAlpha = ((t - 0.32f) / 0.3f).coerceIn(0f, 0.9f)
                for (b in 0 until 4) {
                    val baseAngle = (arcPhase + b * 90f) * (PI / 180.0)
                    val p1 = Offset(cx + (cos(baseAngle) * 102f).toFloat(), cy + (sin(baseAngle) * 102f).toFloat())
                    val pMid = Offset(
                        cx + (cos(baseAngle + 0.18) * 136f).toFloat(),
                        cy + (sin(baseAngle + 0.18) * 136f).toFloat()
                    )
                    val p2 = Offset(cx + (cos(baseAngle + 0.35) * 116f).toFloat(), cy + (sin(baseAngle + 0.35) * 116f).toFloat())
                    val path = Path().apply {
                        moveTo(p1.x, p1.y)
                        lineTo(pMid.x, pMid.y)
                        lineTo(p2.x, p2.y)
                    }
                    drawPath(path, color = bright.copy(alpha = boltAlpha), style = Stroke(width = 3f, cap = StrokeCap.Round))
                }
            }

            // Step 8: Light flash overlay
            if (flashAlpha > 0f) {
                drawRect(color = bright.copy(alpha = flashAlpha))
            }
        }

        // Skip button top-right
        TextButton(
            onClick = onFinish,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .testTag("intro_skip_button")
        ) {
            Text(skipLabel, color = SilverText, style = MaterialTheme.typography.labelLarge)
        }

        val zoomScale = 1f + (flashAlpha * 0.14f)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .scale(zoomScale)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_bm_logo_fg_1795148864354),
                contentDescription = "BM GAMING PRO Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(134.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .clip(CircleShape)
                    .border(2.dp, primary, CircleShape)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "BM GAMING PRO",
                style = MaterialTheme.typography.displayLarge,
                color = PureWhite,
                modifier = Modifier.alpha(titleAlpha)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.labelLarge,
                color = bright,
                modifier = Modifier.alpha(titleAlpha)
            )
            Spacer(modifier = Modifier.height(34.dp))
            GlowingActionButton(
                text = startLabel,
                onClick = onFinish,
                testTag = "intro_start_gaming_button",
                modifier = Modifier
                    .fillMaxWidth(0.76f)
                    .alpha(buttonAlpha)
            )
        }
    }
}

@Composable
fun SubtleParticlesBackground(
    enabled: Boolean,
    isLowRam: Boolean,
    modifier: Modifier = Modifier
) {
    if (!enabled) return
    val primary = MaterialTheme.colorScheme.primary
    val transition = rememberInfiniteTransition(label = "bg_particles")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "drift"
    )
    val count = if (isLowRam) 8 else 16

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (i in 0 until count) {
            val x = ((i * 173 + 47) % 100) / 100f * w
            val baseY = ((i * 251 + 19) % 100) / 100f
            val y = ((baseY - drift + 1f) % 1f) * h
            drawCircle(
                color = primary.copy(alpha = 0.14f),
                radius = if (i % 2 == 0) 3.5f else 2f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun TacticalCard(
    modifier: Modifier = Modifier,
    highlightBorder: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = TacticalSurface,
        border = BorderStroke(
            width = if (highlightBorder) 1.5.dp else 1.dp,
            color = if (highlightBorder) primary.copy(alpha = 0.78f) else TacticalBorder
        ),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun GlowingActionButton(
    text: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(50.dp)
            .shadow(
                elevation = if (enabled) 14.dp else 0.dp,
                shape = MaterialTheme.shapes.small,
                ambientColor = primary,
                spotColor = secondary
            )
            .testTag(testTag),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = primary,
            contentColor = PureWhite,
            disabledContainerColor = TacticalBorder,
            disabledContentColor = MutedSteel
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    badge: String? = null
) {
    val primary = MaterialTheme.colorScheme.primary
    val container = MaterialTheme.colorScheme.primaryContainer
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .background(primary, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite
                )
            }
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverText,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
        if (!badge.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = container.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, primary.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = PureWhite,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun FireButtonLabPreviewCanvas(
    sizePercent: Int,
    compareSizePercent: Int?,
    opacity: Float,
    style: FireButtonStyle,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val container = MaterialTheme.colorScheme.primaryContainer
    val transition = rememberInfiniteTransition(label = "fire_btn_pulse")
    val ringAlpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "ring_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Brush.verticalGradient(listOf(DeepCarbon, Color(0xFF150A0E))))
            .border(1.dp, TacticalBorder, MaterialTheme.shapes.medium)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w * 0.68f, h * 0.62f)
            for (i in 1..4) {
                drawLine(
                    color = TacticalBorder.copy(alpha = 0.4f),
                    start = Offset(0f, h * i / 5f),
                    end = Offset(w, h * i / 5f),
                    strokeWidth = 1f
                )
            }

            fun radiusFor(pct: Int): Float = 26f + 36f * ((pct - 35f) / 35f)

            if (compareSizePercent != null) {
                val cRadius = radiusFor(compareSizePercent)
                drawCircle(
                    color = CyanHud.copy(alpha = 0.75f),
                    radius = cRadius,
                    center = center,
                    style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                )
            }

            val radius = radiusFor(sizePercent)
            val dragTop = Offset(center.x, h * 0.14f)
            drawLine(
                color = CyanHud.copy(alpha = 0.55f),
                start = center,
                end = dragTop,
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
            )
            drawCircle(color = CyanHud, radius = 6f, center = dragTop)

            if (style == FireButtonStyle.NEON_RING) {
                drawCircle(
                    color = primary.copy(alpha = ringAlpha * 0.45f * opacity),
                    radius = radius + 12f,
                    center = center,
                    style = Stroke(width = 3.5f)
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondary.copy(alpha = opacity), container.copy(alpha = opacity)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            drawCircle(
                color = PureWhite.copy(alpha = opacity),
                radius = radius,
                center = center,
                style = Stroke(width = 3f)
            )
            when (style) {
                FireButtonStyle.NEON_RING -> drawCircle(
                    color = PureWhite.copy(alpha = 0.75f * opacity),
                    radius = radius * 0.42f,
                    center = center,
                    style = Stroke(width = 2f)
                )
                FireButtonStyle.SOLID_CORE -> drawCircle(
                    color = PureWhite.copy(alpha = 0.5f * opacity),
                    radius = radius * 0.3f,
                    center = center
                )
                FireButtonStyle.CROSSHAIR_DOT -> {
                    drawLine(PureWhite.copy(alpha = opacity), Offset(center.x - 10f, center.y), Offset(center.x + 10f, center.y), 2.5f)
                    drawLine(PureWhite.copy(alpha = opacity), Offset(center.x, center.y - 10f), Offset(center.x, center.y + 10f), 2.5f)
                }
            }
        }

        Column(modifier = Modifier.align(Alignment.TopStart).padding(14.dp)) {
            Text("FIRE LAB", style = MaterialTheme.typography.labelSmall, color = MutedSteel)
            Text("$sizePercent%", style = MaterialTheme.typography.displayLarge, color = secondary)
            if (compareSizePercent != null) {
                Text("vs $compareSizePercent% (Cyan)", style = MaterialTheme.typography.labelSmall, color = CyanHud)
            }
        }
    }
}

fun DrawScope.drawConfiguredCrosshair(
    center: Offset,
    config: CrosshairConfig,
    primaryFallback: Color
) {
    val color = crosshairColorFromIndex(config.colorIndex, primaryFallback).copy(alpha = config.opacity)
    val r = config.sizeDp.toFloat()
    when (config.shape) {
        CrosshairShape.CLASSIC_PLUS -> {
            val gap = r * 0.25f
            drawLine(color, Offset(center.x - r, center.y), Offset(center.x - gap, center.y), 3.5f, StrokeCap.Round)
            drawLine(color, Offset(center.x + gap, center.y), Offset(center.x + r, center.y), 3.5f, StrokeCap.Round)
            drawLine(color, Offset(center.x, center.y - r), Offset(center.x, center.y - gap), 3.5f, StrokeCap.Round)
            drawLine(color, Offset(center.x, center.y + gap), Offset(center.x, center.y + r), 3.5f, StrokeCap.Round)
            drawCircle(color, radius = 2.8f, center = center)
        }
        CrosshairShape.PRECISION_DOT -> {
            drawCircle(color, radius = (r * 0.28f).coerceAtLeast(4f), center = center)
            drawCircle(color.copy(alpha = (config.opacity * 0.45f)), radius = r * 0.65f, center = center, style = Stroke(2f))
        }
        CrosshairShape.TACTICAL_RING -> {
            drawCircle(color, radius = r * 0.72f, center = center, style = Stroke(3f))
            drawCircle(color, radius = 3.2f, center = center)
        }
        CrosshairShape.X_CROSS -> {
            val d = r * 0.68f
            drawLine(color, Offset(center.x - d, center.y - d), Offset(center.x + d, center.y + d), 3f, StrokeCap.Round)
            drawLine(color, Offset(center.x - d, center.y + d), Offset(center.x + d, center.y - d), 3f, StrokeCap.Round)
        }
    }
}

@Composable
fun HeadshotLearningCanvas(
    distance: DragLessonDistance,
    slowMotion: Boolean,
    replaySeed: Int,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val progress = remember { Animatable(0f) }

    LaunchedEffect(distance, slowMotion, replaySeed) {
        while (true) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = if (slowMotion) 2800 else 1150,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    val p = progress.value
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(MaterialTheme.shapes.small)
            .background(ObsidianBlack)
            .border(1.dp, TacticalBorder, MaterialTheme.shapes.small)
    ) {
        val w = size.width
        val h = size.height
        val targetX = w * 0.28f
        val headCenter = Offset(targetX, h * 0.26f)

        // 2D Character torso & shoulders
        drawRoundRect(
            color = TacticalSurface,
            topLeft = Offset(targetX - 32f, h * 0.42f),
            size = androidx.compose.ui.geometry.Size(64f, h * 0.44f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
        )
        drawRoundRect(
            color = MutedSteel,
            topLeft = Offset(targetX - 32f, h * 0.42f),
            size = androidx.compose.ui.geometry.Size(64f, h * 0.44f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
            style = Stroke(2f)
        )

        // Head target
        drawCircle(primary.copy(alpha = 0.25f), radius = 28f, center = headCenter)
        drawCircle(secondary, radius = 22f, center = headCenter, style = Stroke(3f))

        // Crosshair travel from chest to head
        val aimStart = when (distance) {
            DragLessonDistance.SHORT_DRAG -> Offset(targetX, h * 0.44f)
            DragLessonDistance.MEDIUM_DRAG -> Offset(targetX, h * 0.56f)
            DragLessonDistance.LONG_DRAG -> Offset(targetX, h * 0.70f)
        }
        val aimNow = Offset(
            x = aimStart.x + (headCenter.x - aimStart.x) * p,
            y = aimStart.y + (headCenter.y - aimStart.y) * p
        )
        drawLine(
            color = EmeraldOnline.copy(alpha = 0.65f),
            start = aimStart,
            end = headCenter,
            strokeWidth = 2.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )
        drawCircle(PureWhite, radius = 11f, center = aimNow, style = Stroke(2.5f))
        drawCircle(primary, radius = 3.5f, center = aimNow)

        // Finger Drag Path (Right side)
        val startPt = Offset(w * 0.73f, h * 0.80f)
        val endY = when (distance) {
            DragLessonDistance.SHORT_DRAG -> h * 0.52f
            DragLessonDistance.MEDIUM_DRAG -> h * 0.34f
            DragLessonDistance.LONG_DRAG -> h * 0.16f
        }
        val thumbPath = Path().apply {
            moveTo(startPt.x, startPt.y)
            quadraticTo(startPt.x + 18f, (startPt.y + endY) / 2f, startPt.x + 2f, endY)
        }
        drawPath(thumbPath, color = CyanHud, style = Stroke(6f, cap = StrokeCap.Round))
        drawCircle(primary.copy(alpha = 0.5f), radius = 22f, center = startPt)
        drawCircle(PureWhite, radius = 8f, center = Offset(startPt.x, startPt.y + (endY - startPt.y) * p))
    }
}

@Composable
fun DragPracticePadCanvas(
    hintText: String,
    angleLabel: String,
    speedLabel: String,
    resultGreat: String,
    resultCurve: String,
    resultSlow: String,
    onPracticeAttempt: (score: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val points = remember { mutableStateListOf<Offset>() }
    var feedbackText by remember { mutableStateOf(hintText) }
    var lastScore by remember { mutableStateOf<Int?>(null) }
    var lastAngleDeg by remember { mutableStateOf(0) }
    var lastSpeedPx by remember { mutableStateOf(0) }
    var dragStartTime by remember { mutableLongStateOf(0L) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(ObsidianBlack)
                .border(1.5.dp, primary.copy(alpha = 0.65f), MaterialTheme.shapes.medium)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            points.clear()
                            points.add(offset)
                            dragStartTime = System.currentTimeMillis()
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            points.add(change.position)
                        },
                        onDragEnd = {
                            if (points.size >= 2) {
                                val start = points.first()
                                val end = points.last()
                                val dx = end.x - start.x
                                val dy = start.y - end.y
                                val dist = hypot(dx.toDouble(), dy.toDouble())
                                val dt = (System.currentTimeMillis() - dragStartTime).coerceAtLeast(16L)
                                val speed = ((dist / dt) * 1000.0).roundToInt()
                                val angleFromVertical = abs(Math.toDegrees(atan2(dx.toDouble(), dy.coerceAtLeast(1f).toDouble()))).roundToInt()
                                lastAngleDeg = angleFromVertical
                                lastSpeedPx = speed

                                val score = when {
                                    dy < 45f -> 40
                                    angleFromVertical <= 18 && speed >= 380 -> 96
                                    angleFromVertical <= 28 && speed >= 260 -> 82
                                    else -> 64
                                }
                                lastScore = score
                                feedbackText = when {
                                    dy < 45f || speed < 260 -> resultSlow
                                    angleFromVertical > 22 -> resultCurve
                                    else -> resultGreat
                                }
                                onPracticeAttempt(score)
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawCircle(EmeraldOnline.copy(alpha = 0.25f), radius = 26f, center = Offset(w / 2f, h * 0.22f))
                drawCircle(EmeraldOnline, radius = 26f, center = Offset(w / 2f, h * 0.22f), style = Stroke(2.5f))
                drawLine(
                    color = TacticalBorder,
                    start = Offset(w / 2f, h * 0.78f),
                    end = Offset(w / 2f, h * 0.22f),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
                drawCircle(primary.copy(alpha = 0.35f), radius = 28f, center = Offset(w / 2f, h * 0.78f))

                if (points.size > 1) {
                    val userPath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                    }
                    drawPath(userPath, color = secondary, style = Stroke(6f, cap = StrokeCap.Round))
                }
            }

            Text(
                text = lastScore?.let { "$it%" } ?: "SWIPE ↑",
                style = MaterialTheme.typography.labelLarge,
                color = if ((lastScore ?: 0) >= 85) EmeraldOnline else PureWhite,
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$angleLabel: ${lastAngleDeg}°", style = MaterialTheme.typography.labelMedium, color = CyanHud)
            Text("$speedLabel: $lastSpeedPx px/s", style = MaterialTheme.typography.labelMedium, color = SilverText)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(feedbackText, style = MaterialTheme.typography.bodyMedium, color = if ((lastScore ?: 0) >= 85) EmeraldOnline else SilverText)
    }
}

@Composable
fun HudLayoutCanvas(
    fingers: Int,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(MaterialTheme.shapes.small)
            .background(ObsidianBlack)
            .border(1.dp, TacticalBorder, MaterialTheme.shapes.small)
    ) {
        val w = size.width
        val h = size.height
        drawCircle(SilverText.copy(alpha = 0.25f), radius = 20f, center = Offset(w * 0.16f, h * 0.72f))
        drawCircle(SilverText, radius = 20f, center = Offset(w * 0.16f, h * 0.72f), style = Stroke(2f))
        drawCircle(primary.copy(alpha = 0.35f), radius = 22f, center = Offset(w * 0.82f, h * 0.70f))
        drawCircle(secondary, radius = 22f, center = Offset(w * 0.82f, h * 0.70f), style = Stroke(2.5f))

        if (fingers >= 3) {
            drawCircle(CyanHud.copy(alpha = 0.35f), radius = 16f, center = Offset(w * 0.18f, h * 0.24f))
            drawCircle(CyanHud, radius = 16f, center = Offset(w * 0.18f, h * 0.24f), style = Stroke(2f))
        }
        if (fingers >= 4) {
            drawCircle(EmeraldOnline.copy(alpha = 0.35f), radius = 16f, center = Offset(w * 0.82f, h * 0.24f))
            drawCircle(EmeraldOnline, radius = 16f, center = Offset(w * 0.82f, h * 0.24f), style = Stroke(2f))
        }
    }
}
