package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SavedProfileEntity
import com.example.domain.SensitivityEngine
import com.example.i18n.Strings
import com.example.model.SensitivityMode
import com.example.model.SkillTier
import com.example.ui.MainUiState
import com.example.ui.components.GlowingActionButton
import com.example.ui.components.SectionHeader
import com.example.ui.components.TacticalCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanHud
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverText
import com.example.ui.theme.TacticalSurfaceElevated
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileLibraryScreen(
    state: MainUiState,
    strings: Strings,
    savedProfiles: List<SavedProfileEntity>,
    lastBackupTs: Long,
    onApplyPresetMode: (SensitivityMode) -> Unit,
    onLoadProfile: (SavedProfileEntity) -> Unit,
    onToggleFavorite: (SavedProfileEntity) -> Unit,
    onRenameProfile: (Long, String) -> Unit,
    onDuplicateProfile: (SavedProfileEntity) -> Unit,
    onDeleteProfile: (SavedProfileEntity) -> Unit,
    onShareProfile: (SavedProfileEntity) -> Unit,
    onCompareProfile: () -> Unit,
    onExportCopyJson: () -> Unit,
    onExportShareJson: () -> Unit,
    onImportJson: (String) -> Boolean,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("ALL") }
    var importText by remember { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<SavedProfileEntity?>(null) }

    val builtInPresets = listOf(
        Triple(SensitivityMode.BRAZILIAN, "Brazilian Sensitivity", "High-speed flick & drag"),
        Triple(SensitivityMode.DRAG, "Drag Headshot", "Consistent vertical chest-to-head"),
        Triple(SensitivityMode.ONE_TAP, "One Tap", "Shotgun & Desert Eagle snap"),
        Triple(SensitivityMode.BALANCED, "Balanced", "All-round Clash Squad & BR"),
        Triple(SensitivityMode.SMOOTH, "Smooth", "Controlled spray stability"),
        Triple(SensitivityMode.LOW_RAM, "Low RAM", "Boosted touch responsiveness")
    )

    val filteredSaved = remember(savedProfiles, query, filterCategory) {
        savedProfiles.filter { p ->
            val matchQuery = query.isBlank() || p.name.contains(query, true) || p.deviceLabel.contains(query, true)
            val matchCat = when (filterCategory) {
                "FAV" -> p.isFavorite
                "CUSTOM" -> true
                else -> true
            }
            matchQuery && matchCat
        }
    }

    val backupText = if (lastBackupTs > 0L) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(lastBackupTs))
    } else "Not backed up yet"

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Filter
        item {
            SectionHeader(title = strings.profileLibraryTitle, badge = "${savedProfiles.size} SAVED")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SilverText) },
                placeholder = { Text("Search profiles...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("profile_library_search")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to "All Profiles", "FAV" to "Favorites", "PRESETS" to "Built-in Presets").forEach { (key, label) ->
                    FilterChip(
                        selected = filterCategory == key,
                        onClick = { filterCategory = key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = PureWhite)
                    )
                }
            }
        }

        // Built-in Pro Presets
        if (filterCategory == "ALL" || filterCategory == "PRESETS") {
            items(builtInPresets.filter { query.isBlank() || it.second.contains(query, true) }, key = { it.first.id }) { (mode, title, sub) ->
                val preview = SensitivityEngine.generateSensitivity(state.deviceInfo, mode, SkillTier.PRO)
                TacticalCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.Bold)
                            Text(sub, style = MaterialTheme.typography.bodyMedium, color = SilverText)
                            Text("GEN ${preview.general} • RED ${preview.redDot} • 2X ${preview.scope2x} • 4X ${preview.scope4x}", style = MaterialTheme.typography.labelSmall, color = CyanHud)
                        }
                        GlowingActionButton(
                            text = "Use",
                            onClick = { onApplyPresetMode(mode) },
                            testTag = "use_preset_${mode.id}"
                        )
                    }
                }
            }
        }

        // Saved Custom Profiles
        if (filterCategory != "PRESETS") {
            items(filteredSaved, key = { it.id }) { profile ->
                TacticalCard(highlightBorder = profile.isFavorite) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profile.name, style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.Bold)
                            Text(profile.deviceLabel, style = MaterialTheme.typography.bodyMedium, color = SilverText)
                        }
                        IconButton(onClick = { onToggleFavorite(profile) }) {
                            Icon(
                                imageVector = if (profile.isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = "Favorite",
                                tint = if (profile.isFavorite) AmberWarning else SilverText
                            )
                        }
                    }
                    Text("GEN ${profile.general} • RED ${profile.redDot} • 2X ${profile.scope2x} • 4X ${profile.scope4x} • Fire ${profile.fireButtonPercent}%", style = MaterialTheme.typography.labelMedium, color = EmeraldOnline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        GlowingActionButton(
                            text = "Load",
                            onClick = { onLoadProfile(profile) },
                            testTag = "lib_load_${profile.id}",
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { renameTarget = profile }) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename", tint = PureWhite)
                        }
                        IconButton(onClick = { onDuplicateProfile(profile) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = CyanHud)
                        }
                        IconButton(onClick = onCompareProfile) {
                            Icon(Icons.Default.CompareArrows, contentDescription = "Compare", tint = EmeraldOnline)
                        }
                        IconButton(onClick = { onShareProfile(profile) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = PureWhite)
                        }
                        IconButton(onClick = { onDeleteProfile(profile) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // JSON Backup & Restore Section
        item {
            TacticalCard(highlightBorder = true) {
                SectionHeader(title = "JSON Backup & Restore", badge = backupText)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlowingActionButton(
                        text = "Copy JSON",
                        onClick = onExportCopyJson,
                        testTag = "backup_copy_json",
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = onExportShareJson,
                        modifier = Modifier.weight(1f).height(50.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share JSON")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    placeholder = { Text("Paste exported JSON backup...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                GlowingActionButton(
                    text = "Restore Backup",
                    testTag = "restore_json_button",
                    onClick = {
                        if (onImportJson(importText)) {
                            importText = ""
                            onShowMessage("Backup restored!")
                        } else {
                            onShowMessage("Invalid backup JSON")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    renameTarget?.let { target ->
        var newName by remember(target.id) { mutableStateOf(target.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            containerColor = TacticalSurfaceElevated,
            title = { Text("Rename Profile", color = PureWhite) },
            text = {
                OutlinedTextField(value = newName, onValueChange = { newName = it }, singleLine = true)
            },
            confirmButton = {
                GlowingActionButton(
                    text = "Save",
                    testTag = "confirm_rename_profile",
                    onClick = {
                        onRenameProfile(target.id, newName)
                        renameTarget = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel", color = SilverText) }
            }
        )
    }
}
