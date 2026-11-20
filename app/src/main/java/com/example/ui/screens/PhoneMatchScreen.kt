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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.data.DeviceCatalog
import com.example.data.local.SavedProfileEntity
import com.example.domain.SensitivityEngine
import com.example.i18n.Strings
import com.example.model.DeviceCatalogEntry
import com.example.ui.MainUiState
import com.example.ui.components.GlowingActionButton
import com.example.ui.components.SectionHeader
import com.example.ui.components.TacticalCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanHud
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverText
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurfaceElevated
import java.util.Locale

@Composable
fun PhoneMatchScreen(
    state: MainUiState,
    strings: Strings,
    savedProfiles: List<SavedProfileEntity>,
    onSelectCatalogDevice: (DeviceCatalogEntry) -> Unit,
    onApplyCustomDevice: (brand: String, model: String, ramGb: Int, refreshRateHz: Int, inches: Double) -> Unit,
    onResetAutoDetect: () -> Unit,
    onLoadSavedProfile: (SavedProfileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedBrand by remember { mutableStateOf<String?>(null) }
    var showCustomDialog by remember { mutableStateOf(false) }

    val active = state.deviceInfo
    val matchingSaved = savedProfiles.filter {
        it.deviceLabel.contains(active.model, ignoreCase = true) ||
            it.deviceLabel.contains(active.brand, ignoreCase = true)
    }
    val filtered = remember(query, selectedBrand) {
        DeviceCatalog.devices.filter { d ->
            val brandOk = selectedBrand == null || d.brand.equals(selectedBrand, ignoreCase = true)
            val queryOk = query.isBlank() || d.model.contains(query, ignoreCase = true) || d.brand.contains(query, ignoreCase = true)
            brandOk && queryOk
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Active Matched Phone Summary
        item {
            TacticalCard(highlightBorder = true) {
                SectionHeader(
                    title = strings.phoneMatchTitle,
                    subtitle = "${active.brand} ${active.model}",
                    badge = if (active.isManualOverride) strings.estimatedBadge else "DETECTED"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${active.androidVersion} • ${active.refreshRateHz ?: 60}Hz • ${active.totalRamGb?.let { String.format(Locale.US, "%.1f GB RAM", it) } ?: "--"} • ${active.processorLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverText
                )
                Spacer(modifier = Modifier.height(8.dp))
                val rec = SensitivityEngine.generateSensitivity(active, state.sensitivityMode, state.skillTier)
                Text(
                    text = "GEN ${rec.general} • RED ${rec.redDot} • 2X ${rec.scope2x} • 4X ${rec.scope4x} • (${strings.estimatedBadge})",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyanHud
                )
                if (active.isManualOverride) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onResetAutoDetect, border = BorderStroke(1.dp, TacticalBorder)) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = SilverText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auto Detect Device", color = SilverText)
                    }
                }
            }
        }

        // Saved Profiles for Selected Phone
        if (matchingSaved.isNotEmpty()) {
            item {
                SectionHeader(title = "${strings.testedSavedBadge} (${matchingSaved.size})")
            }
            items(matchingSaved, key = { it.id }) { prof ->
                TacticalCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prof.name, style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.Bold)
                            Text("GEN ${prof.general} • RED ${prof.redDot} • Fire ${prof.fireButtonPercent}%", style = MaterialTheme.typography.labelMedium, color = EmeraldOnline)
                        }
                        GlowingActionButton(text = "Load", onClick = { onLoadSavedProfile(prof) }, testTag = "load_matched_${prof.id}")
                    }
                }
            }
        }

        // Search & Brand Filters
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SilverText) },
                placeholder = { Text("Search Samsung, Redmi, POCO, iQOO, Infinix, Tecno...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("phone_match_search")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedBrand == null,
                    onClick = { selectedBrand = null },
                    label = { Text("All (${DeviceCatalog.devices.size})") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = PureWhite)
                )
                DeviceCatalog.supportedBrands.forEach { brand ->
                    FilterChip(
                        selected = selectedBrand == brand,
                        onClick = { selectedBrand = brand },
                        label = { Text(brand) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = PureWhite)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showCustomDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                border = BorderStroke(1.dp, CyanHud.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanHud)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enter Custom Device Specs")
            }
        }

        item {
            SectionHeader(title = "Device Database", badge = "${filtered.size} MODELS")
        }

        items(filtered, key = { it.id }) { entry ->
            TacticalCard {
                Text("${entry.brand} ${entry.model}", style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.Bold)
                Text("${entry.ramGb}GB RAM • ${entry.refreshRateHz}Hz • ${entry.screenInches}\" • ${entry.processorNote}", style = MaterialTheme.typography.bodyMedium, color = SilverText)
                Text(strings.estimatedBadge, style = MaterialTheme.typography.labelSmall, color = AmberWarning)
                Spacer(modifier = Modifier.height(8.dp))
                GlowingActionButton(
                    text = "Match Device",
                    onClick = { onSelectCatalogDevice(entry) },
                    testTag = "match_device_${entry.id}",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showCustomDialog) {
        var brand by remember { mutableStateOf("") }
        var model by remember { mutableStateOf("") }
        var ram by remember { mutableStateOf("6") }
        var hz by remember { mutableStateOf("90") }
        var inches by remember { mutableStateOf("6.6") }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            containerColor = TacticalSurfaceElevated,
            title = { Text("Custom Device Specs", color = PureWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, singleLine = true)
                    OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") }, singleLine = true)
                    OutlinedTextField(value = ram, onValueChange = { ram = it.filter(Char::isDigit) }, label = { Text("RAM (GB)") }, singleLine = true)
                    OutlinedTextField(value = hz, onValueChange = { hz = it.filter(Char::isDigit) }, label = { Text("Refresh Rate (Hz)") }, singleLine = true)
                    OutlinedTextField(value = inches, onValueChange = { inches = it }, label = { Text("Screen (Inches)") }, singleLine = true)
                }
            },
            confirmButton = {
                GlowingActionButton(
                    text = "Apply",
                    testTag = "confirm_custom_phone",
                    onClick = {
                        onApplyCustomDevice(brand, model, ram.toIntOrNull() ?: 6, hz.toIntOrNull() ?: 90, inches.toDoubleOrNull() ?: 6.6)
                        showCustomDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) { Text("Cancel", color = SilverText) }
            }
        )
    }
}
