package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AimSessionEntity
import com.example.i18n.Strings
import com.example.model.AppLanguage
import com.example.model.AppThemeAccent
import com.example.model.BmRankTier
import com.example.ui.components.GlowingActionButton
import com.example.ui.components.SectionHeader
import com.example.ui.components.TacticalCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanHud
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverText
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurfaceElevated

@Composable
fun MyProfileScreen(
    strings: Strings,
    language: AppLanguage,
    themeAccent: AppThemeAccent,
    introEnabled: Boolean,
    particlesEnabled: Boolean,
    currentRank: BmRankTier,
    totalXp: Int,
    dailyStreak: Int,
    recentSessions: List<AimSessionEntity>,
    onToggleLanguage: () -> Unit,
    onSelectThemeAccent: (AppThemeAccent) -> Unit,
    onToggleIntroEnabled: (Boolean) -> Unit,
    onToggleParticlesEnabled: (Boolean) -> Unit,
    onReplayIntro: () -> Unit,
    onReplayTutorial: () -> Unit,
    onResetPreferences: () -> Unit,
    modifier: Modifier = Modifier
) {
    var settingsQuery by remember { mutableStateOf("") }
    var showResetConfirm by remember { mutableStateOf(false) }

    val primary = MaterialTheme.colorScheme.primary
    val nextRank = BmRankTier.entries.firstOrNull { it.minXp > totalXp }
    val nextXpGoal = nextRank?.minXp ?: currentRank.minXp.coerceAtLeast(3500)
    val progressFrac = (totalXp.toFloat() / nextXpGoal.toFloat()).coerceIn(0f, 1f)

    val badges = listOf(
        Triple("First Drill", recentSessions.isNotEmpty(), "Complete 1 Aim Simulator session"),
        Triple("Sharpshooter", recentSessions.any { it.accuracyPercent >= 80 }, "Reach 80%+ accuracy"),
        Triple("Streak Fighter", dailyStreak >= 1, "Complete a Daily Challenge"),
        Triple("BM Elite", totalXp >= 650, "Reach Gold Rank (650+ XP)")
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Feature 20: BM Gaming Ranking
        item {
            TacticalCard(highlightBorder = true) {
                SectionHeader(
                    title = strings.myRankTitle,
                    badge = if (language == AppLanguage.BN) currentRank.titleBn else currentRank.titleEn
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(34.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$totalXp XP", style = MaterialTheme.typography.displayLarge, color = AmberWarning)
                        Text(
                            text = nextRank?.let { "Next: ${it.titleEn} at ${it.minXp} XP" } ?: "MAX RANK: BM LEGEND",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanHud
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressFrac },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = AmberWarning,
                    trackColor = TacticalBorder
                )
            }
        }

        // Achievement Badges
        item {
            TacticalCard {
                SectionHeader(title = if (language == AppLanguage.BN) "অর্জন ব্যাজসমূহ" else "Achievement Badges")
                Spacer(modifier = Modifier.height(8.dp))
                badges.forEach { (title, unlocked, desc) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = if (unlocked) EmeraldOnline else SilverText.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(title, style = MaterialTheme.typography.titleMedium, color = if (unlocked) PureWhite else SilverText)
                            Text(desc, style = MaterialTheme.typography.labelSmall, color = SilverText)
                        }
                    }
                }
            }
        }

        // Weekly Progress Chart + Score History
        item {
            TacticalCard {
                SectionHeader(title = if (language == AppLanguage.BN) "পারফরম্যান্স প্রগ্রেস চার্ট" else "Progress Score Chart")
                Spacer(modifier = Modifier.height(10.dp))
                val scores = recentSessions.take(7).reversed().map { it.score }
                val maxScore = (scores.maxOrNull() ?: 100).coerceAtLeast(100)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(ObsidianBlack)
                        .border(1.dp, TacticalBorder, MaterialTheme.shapes.small)
                ) {
                    if (scores.isNotEmpty()) {
                        val barW = size.width / (scores.size * 2f)
                        scores.forEachIndexed { idx, s ->
                            val barH = (s.toFloat() / maxScore) * (size.height * 0.78f)
                            val x = (idx * 2f + 0.5f) * barW
                            drawRect(
                                color = primary,
                                topLeft = Offset(x, size.height - barH - 12f),
                                size = Size(barW, barH)
                            )
                        }
                    }
                }
                if (recentSessions.isEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("No sessions logged yet. Play Aim Training to populate your chart.", style = MaterialTheme.typography.labelSmall, color = SilverText)
                }
            }
        }

        // Searchable App Settings & Custom Themes
        item {
            TacticalCard {
                SectionHeader(title = strings.settingsTitle)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = settingsQuery,
                    onValueChange = { settingsQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SilverText) },
                    placeholder = { Text(strings.searchSettingsHint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (settingsQuery.isBlank() || "language বাংলা english".contains(settingsQuery, true)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Language (${if (language == AppLanguage.BN) "বাংলা" else "English"})", style = MaterialTheme.typography.bodyLarge, color = PureWhite)
                        OutlinedButton(onClick = onToggleLanguage, border = BorderStroke(1.dp, primary)) {
                            Text(if (language == AppLanguage.BN) "Switch to EN" else "বাংলা করুন", color = PureWhite)
                        }
                    }
                }

                if (settingsQuery.isBlank() || "theme accent color".contains(settingsQuery, true)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Custom Theme Accent", style = MaterialTheme.typography.bodyMedium, color = SilverText)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppThemeAccent.entries.forEach { acc ->
                            FilterChip(
                                selected = themeAccent == acc,
                                onClick = { onSelectThemeAccent(acc) },
                                modifier = Modifier.weight(1f),
                                label = { Text(if (language == AppLanguage.BN) acc.labelBn else acc.labelEn, maxLines = 1) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primary, selectedLabelColor = PureWhite)
                            )
                        }
                    }
                }

                if (settingsQuery.isBlank() || "intro animation".contains(settingsQuery, true)) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Cinematic Intro Animation", style = MaterialTheme.typography.bodyMedium, color = PureWhite)
                        Switch(checked = introEnabled, onCheckedChange = onToggleIntroEnabled, colors = SwitchDefaults.colors(checkedTrackColor = primary))
                    }
                }

                if (settingsQuery.isBlank() || "particles background".contains(settingsQuery, true)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Animated Particles Background", style = MaterialTheme.typography.bodyMedium, color = PureWhite)
                        Switch(checked = particlesEnabled, onCheckedChange = onToggleParticlesEnabled, colors = SwitchDefaults.colors(checkedTrackColor = primary))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onReplayIntro, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, CyanHud)) {
                        Text("Replay Intro", color = CyanHud)
                    }
                    OutlinedButton(onClick = onReplayTutorial, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, EmeraldOnline)) {
                        Text("Show Tutorial", color = EmeraldOnline)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(strings.resetPrefsBtn)
                }
            }
        }

        // Help & Support + Clear Cache Instructions + About BM GAMING PRO
        item {
            TacticalCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = CyanHud)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.helpSupportTitle, style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.BN) {
                        "• ক্যাশ পরিষ্কার: Settings > Apps > BM GAMING PRO > Storage > Clear Cache।\n• যেকোনো সেটিংস রিসেট বা ব্যাকআপ নিতে Profile Library ব্যবহার করুন।"
                    } else {
                        "• Clear Cache: Android Settings > Apps > BM GAMING PRO > Storage > Clear Cache.\n• Use Profile Library to export or restore JSON backups anytime."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverText
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.aboutBmTitle, style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "BM GAMING PRO • ULTIMATE EDITION (BM ESPORTS)\nOffline-capable tactical companion for legitimate aim training, device telemetry, and manual sensitivity tuning.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverText
                )
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = TacticalSurfaceElevated,
            title = { Text(strings.resetPrefsBtn, color = PureWhite) },
            text = { Text("Reset theme, intro, crosshair, and language preferences to defaults?", color = SilverText) },
            confirmButton = {
                GlowingActionButton(
                    text = "Reset",
                    testTag = "confirm_reset_prefs",
                    onClick = {
                        onResetPreferences()
                        showResetConfirm = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel", color = SilverText) }
            }
        )
    }
}
