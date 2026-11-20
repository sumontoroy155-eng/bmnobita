package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.TrainingContent
import com.example.domain.SensitivityEngine
import com.example.i18n.Strings
import com.example.model.AppLanguage
import com.example.ui.MainUiState
import com.example.ui.components.GlowingActionButton
import com.example.ui.components.SectionHeader
import com.example.ui.components.TacticalCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CyanHud
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverText
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurfaceElevated
import java.util.Locale

@Composable
fun GamingToolsScreen(
    state: MainUiState,
    strings: Strings,
    language: AppLanguage,
    dailyGamingSeconds: Int,
    onRunPingTest: () -> Unit,
    onGrantDndPermission: () -> Unit,
    onToggleDnd: () -> Unit,
    onSetTimerDuration: (Int) -> Unit,
    onSetBreakReminder: (Int) -> Unit,
    onDismissBreakAlert: () -> Unit,
    onToggleVoiceCue: () -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onNavigateRoute: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val t = state.telemetry
    val d = state.deviceInfo
    val perf = state.performanceScore
    val ping = state.pingResult

    var voiceTextInput by remember { mutableStateOf("") }
    var voiceStatus by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    DisposableEffect(speechRecognizer) {
        onDispose { speechRecognizer?.destroy() }
    }

    fun startSpeechCapture() {
        if (speechRecognizer == null) {
            voiceStatus = if (language == AppLanguage.BN) "এই ডিভাইসে ভয়েস রেকগনিশন নেই — নিচে লিখে কমান্ড দিন।" else "Speech recognition unavailable — use text command below."
            return
        }
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isListening = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) {
                isListening = false
                voiceStatus = "Mic error ($error). Try typing command below."
            }
            override fun onResults(results: Bundle?) {
                isListening = false
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                voiceTextInput = spoken
                val route = SensitivityEngine.resolveVoiceCommandRoute(spoken)
                if (route != null) {
                    voiceStatus = "Matched: $spoken → $route"
                    onNavigateRoute(route)
                } else {
                    voiceStatus = "Heard: \"$spoken\" (Try: Home, Sensitivity, Training, HUD, Ping)"
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (language == AppLanguage.BN) "bn-BD" else "en-US")
        }
        speechRecognizer.startListening(intent)
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startSpeechCapture()
        } else {
            voiceStatus = if (language == AppLanguage.BN) "মাইক্রোফোন অনুমতি পাওয়া যায়নি। নিচে লিখে কমান্ড দিন।" else "Microphone permission denied. Type a command below."
        }
    }

    var customTimerMinutes by remember { mutableStateOf("10") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Feature 2: Phone Performance Score
        item {
            TacticalCard(highlightBorder = true) {
                SectionHeader(
                    title = strings.perfScoreTitle,
                    subtitle = if (language == AppLanguage.BN) perf.tierLabelBn else perf.tierLabelEn,
                    badge = "${perf.totalScore} / 100"
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { perf.totalScore / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = EmeraldOnline,
                    trackColor = TacticalBorder
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(strings.perfScoreExplainTitle, style = MaterialTheme.typography.labelLarge, color = CyanHud)
                Text(
                    text = "CPU (${d.cpuCores} Cores): ${perf.cpuPoints}/25 • RAM: ${perf.ramPoints}/30 • Display (${d.refreshRateHz ?: 60}Hz): ${perf.displayPoints}/25 • Battery/Thermal: ${perf.batteryPoints}/20",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverText
                )
            }
        }

        // Feature 16: Voice Command (BN & EN)
        item {
            TacticalCard {
                SectionHeader(title = strings.voiceCommandTitle, badge = if (isListening) "LISTENING" else "BN / EN")
                Spacer(modifier = Modifier.height(6.dp))
                Text(strings.voiceCommandHint, style = MaterialTheme.typography.bodyMedium, color = SilverText)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = voiceTextInput,
                        onValueChange = { voiceTextInput = it },
                        placeholder = { Text("সেন্সিটিভিটি / Ping / HUD...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            if (granted) startSpeechCapture() else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        modifier = Modifier.size(48.dp).testTag("voice_mic_button")
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Command", tint = if (isListening) EmeraldOnline else MaterialTheme.colorScheme.secondary)
                    }
                }
                if (voiceStatus.isNotBlank()) {
                    Text(voiceStatus, style = MaterialTheme.typography.labelSmall, color = CyanHud, modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                GlowingActionButton(
                    text = if (language == AppLanguage.BN) "কমান্ড চালান" else "Run Command",
                    testTag = "run_voice_text_command",
                    onClick = {
                        val route = SensitivityEngine.resolveVoiceCommandRoute(voiceTextInput)
                        if (route != null) {
                            onNavigateRoute(route)
                        } else {
                            onShowMessage("Try: Home, Sensitivity, Training, HUD, Ping, Profile")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Feature 6: Ping Monitor
        item {
            TacticalCard {
                SectionHeader(title = strings.pingMonitorTitle, badge = ping.targetHost)
                Spacer(modifier = Modifier.height(8.dp))
                if (ping.isRunning) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                } else if (ping.errorMessage != null) {
                    Text("Offline / Error: ${ping.errorMessage} (Loss: ${ping.packetLossPercent}%)", style = MaterialTheme.typography.bodyMedium, color = CrimsonBright)
                } else if (ping.avgMs != null) {
                    val quality = when {
                        ping.avgMs <= 55 && ping.packetLossPercent == 0 -> "EXCELLENT"
                        ping.avgMs <= 100 -> "GOOD"
                        else -> "HIGH LATENCY"
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("AVG ${ping.avgMs}ms", style = MaterialTheme.typography.titleMedium, color = EmeraldOnline)
                        Text("JITTER ${ping.jitterMs}ms", style = MaterialTheme.typography.labelMedium, color = CyanHud)
                        Text("LOSS ${ping.packetLossPercent}%", style = MaterialTheme.typography.labelMedium, color = PureWhite)
                        Text(quality, style = MaterialTheme.typography.labelMedium, color = AmberWarning)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == AppLanguage.BN) "টিপস: ৫GHz Wi-Fi ব্যবহার করুন ও ব্যাকগ্রাউন্ড ডাউনলোড বন্ধ রাখুন।" else "Tip: Prefer 5GHz Wi-Fi and pause background downloads for lower ping.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverText
                )
                Spacer(modifier = Modifier.height(8.dp))
                GlowingActionButton(
                    text = strings.runPingBtn,
                    onClick = onRunPingTest,
                    enabled = !ping.isRunning,
                    testTag = "run_ping_test_button",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Feature 7: Temperature Watch & Battery
        item {
            TacticalCard {
                SectionHeader(title = strings.tempWatchTitle, badge = t.batteryPercent?.let { "$it%" } ?: "--")
                Spacer(modifier = Modifier.height(8.dp))
                val temp = t.batteryTempCelsius
                val tempColor = when {
                    temp == null -> SilverText
                    temp >= 42f -> CrimsonBright
                    temp >= 38f -> AmberWarning
                    else -> EmeraldOnline
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Thermostat, contentDescription = null, tint = tempColor)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = temp?.let { String.format(Locale.US, "%.1f°C • %s", it, if (t.isCharging) "Charging" else "Discharging") }
                            ?: "Battery temp sensor not reported",
                        style = MaterialTheme.typography.titleMedium,
                        color = tempColor
                    )
                }
                if (temp != null && temp >= 40f) {
                    Text(
                        text = if (language == AppLanguage.BN) "সতর্কতা: তাপমাত্রা বেশি — ফোনের কভার খুলে ৫ মিনিট বিরতি নিন।" else "Thermal warning: Remove thick case and rest 5 minutes to cool down.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AmberWarning
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.BN) "* শুধু অ্যান্ড্রয়েড অনুমোদিত ব্যাটারি তাপমাত্রা প্রদর্শিত হয় (অপ্রাপ্য CPU সেন্সর অনুমান করা হয় না)।" else "* Shows exposed Android battery thermal sensor only (unavailable CPU sensors are never fabricated).",
                    style = MaterialTheme.typography.labelSmall,
                    color = SilverText
                )
            }
        }

        // Features 11 & 12: Screen Checker & Refresh Rate Checker
        item {
            TacticalCard {
                SectionHeader(title = "${strings.screenCheckerTitle} & ${strings.refreshCheckerTitle}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Resolution: ${d.resolutionLabel} (${d.widthDp} × ${d.heightDp} dp)", style = MaterialTheme.typography.bodyMedium, color = PureWhite)
                Text("Density: ${d.densityDpi ?: "--"} dpi • Smallest Width: ${d.smallestWidthDp ?: "--"} dp • ~${d.estimatedInches ?: "--"}\"", style = MaterialTheme.typography.bodyMedium, color = CyanHud)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Active Refresh: ${d.refreshRateHz ?: "--"} Hz • Supported: ${d.supportedRefreshRates.joinToString(", ") { "${it}Hz" }.ifBlank { "60Hz" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmeraldOnline
                )
                Text(
                    text = if (language == AppLanguage.BN) "৬০Hz স্ট্যান্ডার্ড, ৯০Hz স্মুথ ও ১২০Hz+ আল্ট্রা-স্মুথ ফ্রেম রেসপন্স দেয়।" else "60Hz is standard, 90Hz smoother, and 120Hz+ provides ultra-fluid touch tracking.",
                    style = MaterialTheme.typography.labelSmall,
                    color = SilverText
                )
            }
        }

        // Features 13 & 14: RAM Monitor & Storage Cleaner Guide
        item {
            TacticalCard {
                SectionHeader(title = "${strings.ramMonitorTitle} & ${strings.storageCleanerTitle}")
                Spacer(modifier = Modifier.height(8.dp))
                val usedRamMb = if (t.totalRamMb != null && t.availableRamMb != null) (t.totalRamMb - t.availableRamMb).coerceAtLeast(0) else null
                Text(
                    text = if (t.totalRamMb != null && t.availableRamMb != null) {
                        "RAM Used: ${usedRamMb} MB / ${t.totalRamMb} MB (${t.availableRamMb} MB Free)"
                    } else "RAM usage unavailable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                val usedStorageGb = if (t.totalStorageGb != null && t.freeStorageGb != null) (t.totalStorageGb - t.freeStorageGb).coerceAtLeast(0.0) else null
                Text(
                    text = if (usedStorageGb != null && t.totalStorageGb != null) {
                        String.format(Locale.US, "Storage Used: %.1f GB / %.1f GB (%.1f GB Free)", usedStorageGb, t.totalStorageGb, t.freeStorageGb)
                    } else "Storage info unavailable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyanHud
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == AppLanguage.BN) "নিরাপদ ক্লিনআপ: Android Settings > Storage > Cached Data থেকে অপ্রয়োজনীয় ক্যাশ পরিষ্কার করুন (অ্যাপ নিজ থেকে কোনো ফাইল ডিলিট করে না)।" else "Safe Cleanup: Clear unused app caches via Android Settings > Storage. This app never deletes user files.",
                    style = MaterialTheme.typography.labelSmall,
                    color = SilverText
                )
            }
        }

        // Feature 15: Gaming Timer + Break Reminder + Daily Duration + DND
        item {
            val mins = state.timerRemainingSeconds / 60
            val secs = state.timerRemainingSeconds % 60
            val dailyMins = dailyGamingSeconds / 60
            TacticalCard {
                SectionHeader(title = strings.gamingTimerTitle, badge = "TODAY ${dailyMins}m")
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = customTimerMinutes,
                        onValueChange = { customTimerMinutes = it.filter(Char::isDigit).take(3) },
                        label = { Text("Minutes") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { onSetTimerDuration((customTimerMinutes.toIntOrNull() ?: 10).coerceIn(1, 180) * 60) },
                        modifier = Modifier.height(48.dp)
                    ) { Text("Set") }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(String.format(Locale.US, "%02d:%02d", mins, secs), style = MaterialTheme.typography.displayLarge, color = if (state.isTimerRunning) EmeraldOnline else PureWhite)
                    Row {
                        IconButton(onClick = onToggleTimer) {
                            Icon(if (state.isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Toggle Timer", tint = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(onClick = onResetTimer) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset Timer", tint = SilverText)
                        }
                    }
                }
                Text("Break Reminder: every ${state.breakReminderMinutes} min", style = MaterialTheme.typography.labelSmall, color = SilverText)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 30, 45, 60).forEach { min ->
                        FilterChip(
                            selected = state.breakReminderMinutes == min,
                            onClick = { onSetBreakReminder(min) },
                            modifier = Modifier.weight(1f),
                            label = { Text("${min}m") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = PureWhite)
                        )
                    }
                }
                if (state.breakAlertTriggered) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        onClick = onDismissBreakAlert,
                        color = AmberWarning.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, AmberWarning),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "বিরতি রিমাইন্ডার! চোখ ও আঙুলের বিশ্রাম নিন (ট্যাপ করে বন্ধ করুন)" else "Break Reminder! Rest eyes & fingers briefly (Tap to dismiss)",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmberWarning,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (state.voiceCueEnabled) "Voice Focus Cue: ON" else "Voice Focus Cue: OFF",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanHud
                    )
                    OutlinedButton(onClick = onToggleVoiceCue, border = BorderStroke(1.dp, TacticalBorder)) {
                        Text(if (state.voiceCueEnabled) "Mute Cue" else "Enable Cue", color = PureWhite)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (state.dndPermissionGranted) {
                    GlowingActionButton(
                        text = if (state.dndActive) "Disable DND Mode" else "Enable DND Focus Mode",
                        onClick = onToggleDnd,
                        testTag = "toggle_dnd_button",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedButton(
                        onClick = onGrantDndPermission,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.DoNotDisturbOn, contentDescription = null, tint = PureWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Grant DND Policy Access", color = PureWhite)
                    }
                }
            }
        }
    }
}
