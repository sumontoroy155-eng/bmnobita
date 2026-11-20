package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PerfectSetupEntity
import com.example.data.local.SavedProfileEntity
import com.example.domain.ProfileJsonCodec
import com.example.domain.SensitivityEngine
import com.example.i18n.Strings
import com.example.model.AppLanguage
import com.example.model.DpiPreference
import com.example.model.FireButtonPreset
import com.example.model.FireButtonStyle
import com.example.model.SensitivityMode
import com.example.model.SensitivityValues
import com.example.model.SkillTier
import com.example.ui.MainUiState
import com.example.ui.components.FireButtonLabPreviewCanvas
import com.example.ui.components.GlowingActionButton
import com.example.ui.components.HudLayoutCanvas
import com.example.ui.components.SectionHeader
import com.example.ui.components.TacticalCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanHud
import com.example.ui.theme.DeepCarbon
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverText
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurfaceElevated
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SensitivityScreen(
    state: MainUiState,
    strings: Strings,
    language: AppLanguage,
    dpiNote: String,
    fireLabAppearance: Pair<Float, FireButtonStyle>,
    savedProfiles: List<SavedProfileEntity>,
    savedPerfectSetups: List<PerfectSetupEntity>,
    initialSubTab: Int,
    onSelectMode: (SensitivityMode) -> Unit,
    onSelectSkillTier: (SkillTier) -> Unit,
    onUpdateSensitivity: (SensitivityValues) -> Unit,
    onRecalculate: () -> Unit,
    onCopySensitivity: () -> Unit,
    onSaveProfile: (name: String) -> Unit,
    onUpdateFireButton: (Int) -> Unit,
    onUpdateFireCompare: (Int) -> Unit,
    onUpdateFireAppearance: (Float, FireButtonStyle) -> Unit,
    onUpdateDpiInches: (Double) -> Unit,
    onUpdateDpiPref: (DpiPreference) -> Unit,
    onSaveDpiNote: (String) -> Unit,
    onSavePerfectSetup: (title: String, hudFingers: Int) -> Unit,
    onDeletePerfectSetup: (PerfectSetupEntity) -> Unit,
    onExportSetupJson: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        strings.sensitivityDoctorTitle,
        strings.compareTabTitle,
        strings.fireLabTabTitle,
        strings.perfectSetupTabTitle,
        strings.dpiCalcSubTitle
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
                    modifier = Modifier.testTag("sens_subtab_$index"),
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
            0 -> SensitivityDoctorTab(
                state = state,
                strings = strings,
                onSelectMode = onSelectMode,
                onSelectSkillTier = onSelectSkillTier,
                onUpdateSensitivity = onUpdateSensitivity,
                onRecalculate = onRecalculate,
                onCopySensitivity = onCopySensitivity,
                onOpenSaveDialog = { showSaveDialog = true }
            )
            1 -> SensitivityComparisonTab(
                state = state,
                strings = strings,
                savedProfiles = savedProfiles,
                onSaveWinner = { name, values, fire ->
                    onUpdateSensitivity(values)
                    onUpdateFireButton(fire)
                    onSaveProfile(name)
                }
            )
            2 -> FireButtonLabTab(
                state = state,
                strings = strings,
                language = language,
                opacity = fireLabAppearance.first,
                style = fireLabAppearance.second,
                onUpdateFireButton = onUpdateFireButton,
                onUpdateFireCompare = onUpdateFireCompare,
                onUpdateAppearance = onUpdateFireAppearance,
                onSavePreferred = { showSaveDialog = true }
            )
            3 -> PerfectSetupTab(
                state = state,
                strings = strings,
                language = language,
                savedSetups = savedPerfectSetups,
                onSaveSetup = onSavePerfectSetup,
                onDeleteSetup = onDeletePerfectSetup,
                onExportJson = { onExportSetupJson(ProfileJsonCodec.exportPerfectSetup(it)) }
            )
            4 -> DpiGuideTab(
                state = state,
                strings = strings,
                language = language,
                dpiNote = dpiNote,
                onUpdateInches = onUpdateDpiInches,
                onUpdatePref = onUpdateDpiPref,
                onSaveDpiNote = onSaveDpiNote
            )
        }
    }

    if (showSaveDialog) {
        var profileName by remember { mutableStateOf("${state.deviceInfo.model} ${state.sensitivityMode.name}") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = TacticalSurfaceElevated,
            title = { Text(strings.saveProfileBtn, color = PureWhite) },
            text = {
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Profile Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                GlowingActionButton(
                    text = strings.saveProfileBtn,
                    testTag = "confirm_save_profile_button",
                    onClick = {
                        onSaveProfile(profileName)
                        showSaveDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel", color = SilverText) }
            }
        )
    }
}

@Composable
private fun SensitivityDoctorTab(
    state: MainUiState,
    strings: Strings,
    onSelectMode: (SensitivityMode) -> Unit,
    onSelectSkillTier: (SkillTier) -> Unit,
    onUpdateSensitivity: (SensitivityValues) -> Unit,
    onRecalculate: () -> Unit,
    onCopySensitivity: () -> Unit,
    onOpenSaveDialog: () -> Unit
) {
    val values = state.sensitivityValues
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TacticalCard {
                SectionHeader(
                    title = strings.sensitivityDoctorTitle,
                    subtitle = "${state.deviceInfo.brand} ${state.deviceInfo.model} • ${state.deviceInfo.refreshRateHz ?: 60}Hz",
                    badge = "AI DOCTOR"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(
                        SensitivityMode.GENERAL to "General",
                        SensitivityMode.DRAG to "Drag",
                        SensitivityMode.ONE_TAP to "One Tap",
                        SensitivityMode.BRAZILIAN to "Brazilian",
                        SensitivityMode.BALANCED to "Balanced",
                        SensitivityMode.SMOOTH to "Smooth",
                        SensitivityMode.HIGH_FPS to "High FPS",
                        SensitivityMode.LOW_RAM to "Low RAM"
                    )
                    modes.forEach { (mode, label) ->
                        FilterChip(
                            selected = state.sensitivityMode == mode,
                            onClick = { onSelectMode(mode) },
                            modifier = Modifier.testTag("mode_chip_${mode.id}"),
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primary,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        SkillTier.BEGINNER to "Beginner",
                        SkillTier.INTERMEDIATE to "Intermediate",
                        SkillTier.PRO to "Pro"
                    ).forEach { (tier, label) ->
                        FilterChip(
                            selected = state.skillTier == tier,
                            onClick = { onSelectSkillTier(tier) },
                            modifier = Modifier.weight(1f),
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
            }
        }

        item {
            TacticalCard(highlightBorder = true) {
                SensSlider(strings.generalSens, values.general, secondary) { onUpdateSensitivity(values.copy(general = it)) }
                SensSlider(strings.redDotSens, values.redDot, secondary) { onUpdateSensitivity(values.copy(redDot = it)) }
                SensSlider(strings.scope2xSens, values.scope2x, secondary) { onUpdateSensitivity(values.copy(scope2x = it)) }
                SensSlider(strings.scope4xSens, values.scope4x, secondary) { onUpdateSensitivity(values.copy(scope4x = it)) }
                SensSlider(strings.sniperSens, values.sniperScope, secondary) { onUpdateSensitivity(values.copy(sniperScope = it)) }
                SensSlider(strings.freeLookSens, values.freeLook, secondary) { onUpdateSensitivity(values.copy(freeLook = it)) }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlowingActionButton(
                        text = strings.copySettingsBtn,
                        onClick = onCopySensitivity,
                        testTag = "copy_sensitivity_button",
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = onOpenSaveDialog,
                        modifier = Modifier.weight(1f).height(50.dp),
                        border = BorderStroke(1.dp, primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.saveProfileBtn)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRecalculate,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    border = BorderStroke(1.dp, TacticalBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SilverText)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.resetRecommendedBtn)
                }
            }
        }
    }
}

@Composable
private fun SensSlider(
    label: String,
    value: Int,
    accent: androidx.compose.ui.graphics.Color,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = PureWhite)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onValueChange((value - 1).coerceIn(0, 200)) }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease $label", tint = SilverText)
                }
                Text(value.toString(), style = MaterialTheme.typography.titleLarge, color = accent)
                IconButton(onClick = { onValueChange((value + 1).coerceIn(0, 200)) }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase $label", tint = SilverText)
                }
            }
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt().coerceIn(0, 200)) },
            valueRange = 0f..200f,
            colors = SliderDefaults.colors(
                thumbColor = PureWhite,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = TacticalBorder
            )
        )
    }
}

@Composable
private fun SensitivityComparisonTab(
    state: MainUiState,
    strings: Strings,
    savedProfiles: List<SavedProfileEntity>,
    onSaveWinner: (name: String, values: SensitivityValues, fire: Int) -> Unit
) {
    val activeEntry = SavedProfileEntity(
        id = -1,
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
        dpiNote = ""
    )
    val brPreset = SensitivityEngine.generateSensitivity(state.deviceInfo, SensitivityMode.BRAZILIAN, SkillTier.PRO)
    val brEntry = SavedProfileEntity(
        id = -2,
        name = "Brazilian Pro",
        deviceLabel = "${state.deviceInfo.brand} ${state.deviceInfo.model}",
        modeId = "brazilian",
        general = brPreset.general,
        redDot = brPreset.redDot,
        scope2x = brPreset.scope2x,
        scope4x = brPreset.scope4x,
        sniperScope = brPreset.sniperScope,
        freeLook = brPreset.freeLook,
        fireButtonPercent = 45,
        dpiNote = ""
    )

    val candidates = listOf(activeEntry, brEntry) + savedProfiles
    var leftIndex by remember { mutableIntStateOf(0) }
    var rightIndex by remember { mutableIntStateOf(1) }
    val left = candidates[leftIndex.coerceIn(candidates.indices)]
    val right = candidates[rightIndex.coerceIn(candidates.indices)]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TacticalCard {
                Text("Profile A (Cyan)", style = MaterialTheme.typography.labelLarge, color = CyanHud)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    candidates.forEachIndexed { idx, p ->
                        FilterChip(
                            selected = leftIndex == idx,
                            onClick = { leftIndex = idx },
                            label = { Text(p.name) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = PureWhite)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Profile B (Green)", style = MaterialTheme.typography.labelLarge, color = EmeraldOnline)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    candidates.forEachIndexed { idx, p ->
                        FilterChip(
                            selected = rightIndex == idx,
                            onClick = { rightIndex = idx },
                            label = { Text(p.name) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = PureWhite)
                        )
                    }
                }
            }
        }

        item {
            TacticalCard(highlightBorder = true) {
                DiffRow("General", left.general, right.general)
                DiffRow("Red Dot", left.redDot, right.redDot)
                DiffRow("2X Scope", left.scope2x, right.scope2x)
                DiffRow("4X Scope", left.scope4x, right.scope4x)
                DiffRow("Sniper", left.sniperScope, right.sniperScope)
                DiffRow("Free Look", left.freeLook, right.freeLook)
                DiffRow("Fire %", left.fireButtonPercent, right.fireButtonPercent)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onSaveWinner("${left.name} ★", left.toSensitivityValues(), left.fireButtonPercent) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        border = BorderStroke(1.dp, CyanHud)
                    ) {
                        Text("Save A", color = CyanHud)
                    }
                    GlowingActionButton(
                        text = "Save B",
                        onClick = { onSaveWinner("${right.name} ★", right.toSensitivityValues(), right.fireButtonPercent) },
                        testTag = "save_better_profile_b",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiffRow(label: String, a: Int, b: Int) {
    val diff = b - a
    val changed = diff != 0
    Surface(
        color = if (changed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f) else TacticalSurfaceElevated.copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = PureWhite, modifier = Modifier.weight(1f))
            Text("$a", style = MaterialTheme.typography.titleMedium, color = CyanHud, modifier = Modifier.width(50.dp))
            Text("$b", style = MaterialTheme.typography.titleMedium, color = EmeraldOnline, modifier = Modifier.width(50.dp))
            Text(
                text = when {
                    diff > 0 -> "+$diff"
                    diff < 0 -> "$diff"
                    else -> "="
                },
                style = MaterialTheme.typography.labelMedium,
                color = if (changed) MaterialTheme.colorScheme.secondary else SilverText,
                modifier = Modifier.width(44.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FireButtonLabTab(
    state: MainUiState,
    strings: Strings,
    language: AppLanguage,
    opacity: Float,
    style: FireButtonStyle,
    onUpdateFireButton: (Int) -> Unit,
    onUpdateFireCompare: (Int) -> Unit,
    onUpdateAppearance: (Float, FireButtonStyle) -> Unit,
    onSavePreferred: () -> Unit
) {
    var compareEnabled by remember { mutableStateOf(true) }
    val primary = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TacticalCard(highlightBorder = true) {
                SectionHeader(title = strings.fireLabTabTitle, badge = "${state.fireButtonPercent}%")
                Spacer(modifier = Modifier.height(10.dp))
                FireButtonLabPreviewCanvas(
                    sizePercent = state.fireButtonPercent,
                    compareSizePercent = if (compareEnabled) state.fireComparePercent else null,
                    opacity = opacity,
                    style = style
                )
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FireButtonPreset.entries.forEach { preset ->
                        FilterChip(
                            selected = state.fireButtonPercent == preset.percent,
                            onClick = { onUpdateFireButton(preset.percent) },
                            label = { Text("${preset.percent}%") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primary, selectedLabelColor = PureWhite)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (language == AppLanguage.BN) "সাইজ তুলনা (Cyan রিং)" else "Compare Size Overlay (Cyan)", style = MaterialTheme.typography.bodyMedium, color = PureWhite)
                    Switch(checked = compareEnabled, onCheckedChange = { compareEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = primary))
                }
                if (compareEnabled) {
                    Slider(
                        value = state.fireComparePercent.toFloat(),
                        onValueChange = { onUpdateFireCompare(it.roundToInt()) },
                        valueRange = 35f..70f
                    )
                }

                Text("Opacity: ${(opacity * 100).roundToInt()}%", style = MaterialTheme.typography.bodyMedium, color = SilverText)
                Slider(
                    value = opacity,
                    onValueChange = { onUpdateAppearance(it, style) },
                    valueRange = 0.3f..1.0f
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FireButtonStyle.entries.forEach { st ->
                        FilterChip(
                            selected = style == st,
                            onClick = { onUpdateAppearance(opacity, st) },
                            modifier = Modifier.weight(1f),
                            label = { Text(if (language == AppLanguage.BN) st.labelBn else st.labelEn, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primary, selectedLabelColor = PureWhite)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                GlowingActionButton(
                    text = strings.saveProfileBtn,
                    onClick = onSavePreferred,
                    testTag = "save_fire_lab_button",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            TacticalCard {
                Text(
                    text = if (language == AppLanguage.BN) "অফিশিয়াল গেম HUD-তে সেট করার নিয়ম" else "Apply Manually in Official Game HUD",
                    style = MaterialTheme.typography.titleMedium,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.BN) {
                        "গেমের Settings > Controls > Custom HUD খুলুন, মূল ফায়ার বাটনটি সিলেক্ট করে ${state.fireButtonPercent}% সাইজ এবং ${(opacity * 100).roundToInt()}% অপাসিটি দিন।"
                    } else {
                        "Open official Settings > Controls > Custom HUD, select the main Fire Button, set Size to ${state.fireButtonPercent}% and Transparency to ${(opacity * 100).roundToInt()}%."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverText
                )
            }
        }
    }
}

@Composable
private fun PerfectSetupTab(
    state: MainUiState,
    strings: Strings,
    language: AppLanguage,
    savedSetups: List<PerfectSetupEntity>,
    onSaveSetup: (title: String, hudFingers: Int) -> Unit,
    onDeleteSetup: (PerfectSetupEntity) -> Unit,
    onExportJson: (PerfectSetupEntity) -> Unit
) {
    var hudFingers by remember { mutableIntStateOf(3) }
    val dpiRange = SensitivityEngine.recommendDpiRange(
        screenInches = state.dpiScreenInches,
        preference = state.dpiPreference,
        currentSmallestWidthDp = state.deviceInfo.smallestWidthDp
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TacticalCard(highlightBorder = true) {
                SectionHeader(
                    title = strings.perfectSetupTabTitle,
                    subtitle = "${state.deviceInfo.brand} ${state.deviceInfo.model}",
                    badge = "FULL PACKAGE"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("GEN ${state.sensitivityValues.general} • RED ${state.sensitivityValues.redDot} • 2X ${state.sensitivityValues.scope2x} • 4X ${state.sensitivityValues.scope4x}", style = MaterialTheme.typography.labelMedium, color = CyanHud)
                Spacer(modifier = Modifier.height(4.dp))
                Text("DPI: ${dpiRange.first}-${dpiRange.last} dp • Fire Button: ${state.fireButtonPercent}%", style = MaterialTheme.typography.labelMedium, color = EmeraldOnline)
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2, 3, 4).forEach { f ->
                        FilterChip(
                            selected = hudFingers == f,
                            onClick = { hudFingers = f },
                            modifier = Modifier.weight(1f),
                            label = { Text("${f}-Finger") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = PureWhite)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HudLayoutCanvas(fingers = hudFingers)
                Spacer(modifier = Modifier.height(12.dp))
                GlowingActionButton(
                    text = strings.savePerfectSetupBtn,
                    onClick = { onSaveSetup("${state.deviceInfo.model} ${hudFingers}F Package", hudFingers) },
                    testTag = "save_perfect_setup_button",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        items(savedSetups, key = { it.id }) { setup ->
            TacticalCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(setup.title, style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.Bold)
                        Text("${setup.deviceLabel} • ${setup.hudFingers}F • DPI ${setup.recommendedDpiRange} • Fire ${setup.fireButtonPercent}%", style = MaterialTheme.typography.bodyMedium, color = SilverText)
                        Text("GEN ${setup.general} • RED ${setup.redDot} • 2X ${setup.scope2x} • 4X ${setup.scope4x}", style = MaterialTheme.typography.labelSmall, color = CyanHud)
                    }
                    Row {
                        IconButton(onClick = { onExportJson(setup) }) {
                            Icon(Icons.Default.Share, contentDescription = strings.exportPerfectSetupBtn, tint = CyanHud)
                        }
                        IconButton(onClick = { onDeleteSetup(setup) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        if (savedSetups.size >= 2) {
            item {
                var pkgAIndex by remember(savedSetups.size) { mutableIntStateOf(0) }
                var pkgBIndex by remember(savedSetups.size) { mutableIntStateOf(1) }
                val pkgA = savedSetups[pkgAIndex.coerceIn(savedSetups.indices)]
                val pkgB = savedSetups[pkgBIndex.coerceIn(savedSetups.indices)]
                TacticalCard(highlightBorder = true) {
                    SectionHeader(
                        title = if (language == AppLanguage.BN) "সেটআপ প্যাকেজ তুলনা" else "Compare Setup Packages",
                        badge = "A vs B"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        savedSetups.forEachIndexed { idx, pkg ->
                            FilterChip(
                                selected = pkgAIndex == idx,
                                onClick = { pkgAIndex = idx },
                                label = { Text("A: ${pkg.title}") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyanHud, selectedLabelColor = PureWhite)
                            )
                        }
                    }
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        savedSetups.forEachIndexed { idx, pkg ->
                            FilterChip(
                                selected = pkgBIndex == idx,
                                onClick = { pkgBIndex = idx },
                                label = { Text("B: ${pkg.title}") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldOnline, selectedLabelColor = PureWhite)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    DiffRow("General", pkgA.general, pkgB.general)
                    DiffRow("Red Dot", pkgA.redDot, pkgB.redDot)
                    DiffRow("2X Scope", pkgA.scope2x, pkgB.scope2x)
                    DiffRow("4X Scope", pkgA.scope4x, pkgB.scope4x)
                    DiffRow("Fire %", pkgA.fireButtonPercent, pkgB.fireButtonPercent)
                    DiffRow("HUD Fingers", pkgA.hudFingers, pkgB.hudFingers)
                }
            }
        }
    }
}

@Composable
private fun DpiGuideTab(
    state: MainUiState,
    strings: Strings,
    language: AppLanguage,
    dpiNote: String,
    onUpdateInches: (Double) -> Unit,
    onUpdatePref: (DpiPreference) -> Unit,
    onSaveDpiNote: (String) -> Unit
) {
    val recommendedRange = SensitivityEngine.recommendDpiRange(
        screenInches = state.dpiScreenInches,
        preference = state.dpiPreference,
        currentSmallestWidthDp = state.deviceInfo.smallestWidthDp
    )
    var noteDraft by remember(dpiNote) { mutableStateOf(dpiNote) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TacticalCard(highlightBorder = true) {
                SectionHeader(title = strings.dpiCalcSubTitle, badge = "MANUAL GUIDE")
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Density: ${state.deviceInfo.densityDpi ?: "--"} dpi", style = MaterialTheme.typography.bodyMedium, color = PureWhite)
                    Text("Smallest Width: ${state.deviceInfo.smallestWidthDp ?: "--"} dp", style = MaterialTheme.typography.bodyMedium, color = CyanHud)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("${String.format(Locale.US, "%.1f\"", state.dpiScreenInches)}", style = MaterialTheme.typography.titleMedium, color = PureWhite)
                Slider(
                    value = state.dpiScreenInches.toFloat(),
                    onValueChange = { onUpdateInches((it * 10f).roundToInt() / 10.0) },
                    valueRange = 5.2f..7.2f
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(DpiPreference.STANDARD to "Standard", DpiPreference.BALANCED to "Balanced", DpiPreference.FAST_DRAG to "Fast").forEach { (p, l) ->
                        FilterChip(
                            selected = state.dpiPreference == p,
                            onClick = { onUpdatePref(p) },
                            modifier = Modifier.weight(1f),
                            label = { Text(l) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = PureWhite)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${recommendedRange.first} - ${recommendedRange.last} dp",
                    style = MaterialTheme.typography.displayLarge,
                    color = EmeraldOnline
                )
                Text(
                    text = if (language == AppLanguage.BN) "ম্যানুয়াল ধাপ: Settings > Developer Options > Smallest Width-এ গিয়ে সতর্কতার সাথে মান সেট করুন।" else "Manual Steps: Settings > Developer Options > Smallest Width. Never auto-modified.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmberWarning
                )
            }
        }
        item {
            TacticalCard {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    label = { Text("Custom DPI Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                GlowingActionButton(
                    text = strings.saveProfileBtn,
                    onClick = { onSaveDpiNote(noteDraft) },
                    testTag = "save_dpi_note_button",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
