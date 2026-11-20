package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TrainingContent
import com.example.data.local.SavedProfileEntity
import com.example.i18n.Strings
import com.example.model.AppLanguage
import com.example.model.BmRankTier
import com.example.ui.MainUiState
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
import java.util.Locale

@Composable
fun HomeScreen(
    state: MainUiState,
    strings: Strings,
    language: AppLanguage,
    currentRank: BmRankTier,
    totalXp: Int,
    dailyStreak: Int,
    favoriteProfiles: List<SavedProfileEntity>,
    tutorialDismissed: Boolean,
    onDismissTutorial: () -> Unit,
    onCopyQuickSens: () -> Unit,
    onNextTip: () -> Unit,
    onNavigateRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val device = state.deviceInfo
    val perf = state.performanceScore
    val challenge = TrainingContent.todaysDailyChallenge()
    val tipsList = if (language == AppLanguage.BN) TrainingContent.dailyTipsBn else TrainingContent.dailyTipsEn
    val currentTip = tipsList[state.dailyTipIndex % tipsList.size]
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // First-time user tutorial card
        if (!tutorialDismissed) {
            item {
                TacticalCard(highlightBorder = true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.tutorialTitle, style = MaterialTheme.typography.titleMedium, color = secondary, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismissTutorial, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = SilverText)
                        }
                    }
                    Text(
                        text = if (language == AppLanguage.BN) {
                            "১. ফোন ম্যাচ থেকে আপনার ফোন যাচাই করুন • ২. সেন্সিটিভিটি ডক্টরে প্রোফাইল তৈরি করুন • ৩. এইম ট্রেনিং ও HUD ডিজাইনার দিয়ে প্র্যাকটিস করুন।"
                        } else {
                            "1. Verify your device in Phone Match • 2. Generate settings in Sensitivity Doctor • 3. Train flicks & tracking in Aim Training."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SilverText
                    )
                }
            }
        }

        // BM Hero Card: Logo + Device Model + Performance Score
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(166.dp)
                    .clip(MaterialTheme.shapes.large)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_bm_hero_banner_1795148863885),
                    contentDescription = "BM Esports Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(ObsidianBlack.copy(alpha = 0.90f), ObsidianBlack.copy(alpha = 0.50f))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_bm_logo_fg_1795148864354),
                        contentDescription = "BM Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${device.brand} ${device.model}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = PureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = strings.editionSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                onClick = { onNavigateRoute("gaming_tools") },
                                color = ObsidianBlack.copy(alpha = 0.8f),
                                border = BorderStroke(1.dp, EmeraldOnline.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "SCORE ${perf.totalScore}/100",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldOnline,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = ObsidianBlack.copy(alpha = 0.8f),
                                border = BorderStroke(1.dp, CyanHud.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${device.refreshRateHz ?: 60}Hz • ${device.totalRamGb?.let { String.format(Locale.US, "%.0fGB", it) } ?: "--"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CyanHud,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Sensitivity Profile Snapshot
        item {
            TacticalCard(highlightBorder = true) {
                SectionHeader(
                    title = "${strings.navSensitivity} (${state.sensitivityMode.name})",
                    badge = "${state.fireButtonPercent}% FIRE"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("GEN", state.sensitivityValues.general.toString(), secondary, Modifier.weight(1f))
                    MiniStat("RED", state.sensitivityValues.redDot.toString(), PureWhite, Modifier.weight(1f))
                    MiniStat("2X", state.sensitivityValues.scope2x.toString(), CyanHud, Modifier.weight(1f))
                    MiniStat("4X", state.sensitivityValues.scope4x.toString(), EmeraldOnline, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlowingActionButton(
                        text = strings.copySettingsBtn,
                        onClick = onCopyQuickSens,
                        testTag = "home_copy_sens_button",
                        modifier = Modifier.weight(1f)
                    )
                    GlowingActionButton(
                        text = strings.navSensitivity,
                        onClick = { onNavigateRoute("sensitivity") },
                        testTag = "home_open_sens_button",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Daily Challenge & Current Training Rank Row
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TacticalCard(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.currentRankLabel, style = MaterialTheme.typography.labelSmall, color = SilverText)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (language == AppLanguage.BN) currentRank.titleBn else currentRank.titleEn,
                        style = MaterialTheme.typography.titleLarge,
                        color = AmberWarning
                    )
                    Text("$totalXp XP • Streak: ${dailyStreak}d", style = MaterialTheme.typography.labelSmall, color = CyanHud)
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = { onNavigateRoute("my_profile") },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(strings.navMyProfile, color = secondary, style = MaterialTheme.typography.labelLarge)
                    }
                }

                TacticalCard(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = secondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.dailyChallengeTitle, style = MaterialTheme.typography.labelSmall, color = SilverText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (language == AppLanguage.BN) challenge.difficulty.labelBn else challenge.difficulty.labelEn,
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite
                    )
                    Text(
                        text = "${challenge.difficulty.targetScore}+ pts • +${challenge.difficulty.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldOnline
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = { onNavigateRoute("aim_training") },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(strings.playChallengeBtn, color = primary, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Quick Access Hub (All 8 Main Modules)
        item {
            TacticalCard {
                SectionHeader(title = strings.quickAccessTitle)
                Spacer(modifier = Modifier.height(10.dp))
                val gridItems = listOf(
                    Triple("sensitivity", strings.navSensitivity, Icons.Default.Tune),
                    Triple("aim_training", strings.navAimTraining, Icons.Default.SportsEsports),
                    Triple("phone_match", strings.navPhoneMatch, Icons.Default.PhoneAndroid),
                    Triple("hud_designer", strings.navHudDesigner, Icons.Default.DashboardCustomize),
                    Triple("profile_library", strings.navProfileLibrary, Icons.Default.Bookmark),
                    Triple("gaming_tools", strings.navGamingTools, Icons.Default.Build),
                    Triple("my_profile", strings.navMyProfile, Icons.Default.Person),
                    Triple("gaming_tools", strings.perfScoreTitle, Icons.Default.Speed)
                )
                gridItems.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { (route, label, icon) ->
                            Surface(
                                onClick = { onNavigateRoute(route) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("quick_hub_$route"),
                                color = TacticalSurfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, TacticalBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(icon, contentDescription = label, tint = secondary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = PureWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Favorite Profiles Strip
        if (favoriteProfiles.isNotEmpty()) {
            item {
                TacticalCard {
                    SectionHeader(title = strings.favoriteFeaturesTitle)
                    Spacer(modifier = Modifier.height(8.dp))
                    favoriteProfiles.take(3).forEach { fav ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${fav.name} (${fav.deviceLabel})", style = MaterialTheme.typography.bodyMedium, color = PureWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("GEN ${fav.general}", style = MaterialTheme.typography.labelMedium, color = CyanHud)
                        }
                    }
                }
            }
        }

        // Daily Tip
        item {
            TacticalCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.dailyTipTitle, style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onNextTip) { Text(strings.nextTip, color = secondary) }
                }
                Text(currentTip, style = MaterialTheme.typography.bodyMedium, color = SilverText)
            }
        }

        // Safety Notice
        item {
            Surface(
                color = TacticalSurfaceElevated,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, TacticalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldOnline, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(strings.safetyBannerTitle, style = MaterialTheme.typography.titleMedium, color = PureWhite)
                        Text(strings.safetyBannerBody, style = MaterialTheme.typography.bodyMedium, color = SilverText)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = TacticalSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, TacticalBorder)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SilverText)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = color)
        }
    }
}
