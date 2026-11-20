package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.TrainingContent
import com.example.data.local.HudLayoutEntity
import com.example.i18n.Strings
import com.example.model.AppLanguage
import com.example.model.HudControlNode
import com.example.ui.components.GlowingActionButton
import com.example.ui.components.SectionHeader
import com.example.ui.components.TacticalCard
import com.example.ui.theme.CyanHud
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverText
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurfaceElevated
import kotlin.math.roundToInt

@Composable
fun HudDesignerScreen(
    strings: Strings,
    language: AppLanguage,
    controls: List<HudControlNode>,
    savedLayouts: List<HudLayoutEntity>,
    onUpdateControls: (List<HudControlNode>) -> Unit,
    onLoadPresetFingers: (Int) -> Unit,
    onSaveLayout: (name: String, fingers: Int) -> Unit,
    onLoadSavedLayout: (HudLayoutEntity) -> Unit,
    onDeleteLayout: (HudLayoutEntity) -> Unit,
    onExportHudJson: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedControlId by remember(controls) {
        mutableStateOf(controls.firstOrNull()?.id)
    }
    var activeFingers by remember { mutableIntStateOf(3) }
    var canvasSize by remember { mutableStateOf(IntSize(800, 420)) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val selectedControl = controls.firstOrNull { it.id == selectedControlId }
    val density = LocalDensity.current
    val primary = MaterialTheme.colorScheme.primary

    val availableSamples = listOf("FIRE", "SCOPE", "JUMP", "CROUCH", "GLOO", "RELOAD", "SPRINT", "SWITCH", "MEDKIT")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TacticalCard(highlightBorder = true) {
                SectionHeader(title = strings.hudDesignerTitle, badge = "${controls.size} CONTROLS")
                Spacer(modifier = Modifier.height(8.dp))

                // Finger preset selector
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2, 3, 4).forEach { f ->
                        FilterChip(
                            selected = activeFingers == f,
                            onClick = {
                                activeFingers = f
                                onLoadPresetFingers(f)
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("${f}-Finger") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primary, selectedLabelColor = PureWhite)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Interactive Canvas HUD Workspace
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(ObsidianBlack)
                        .border(1.5.dp, primary.copy(alpha = 0.7f), MaterialTheme.shapes.medium)
                        .onSizeChanged { canvasSize = it }
                ) {
                    controls.forEach { node ->
                        val isSelected = node.id == selectedControlId
                        val sizePx = with(density) { node.sizeDp.dp.toPx() }
                        val xPx = (node.xFraction * canvasSize.width - sizePx / 2f).roundToInt()
                        val yPx = (node.yFraction * canvasSize.height - sizePx / 2f).roundToInt()

                        Surface(
                            modifier = Modifier
                                .offset { IntOffset(xPx, yPx) }
                                .size(node.sizeDp.dp)
                                .alpha(node.opacity)
                                .pointerInput(node.id, canvasSize) {
                                    detectTapGestures { selectedControlId = node.id }
                                }
                                .pointerInput(node.id, canvasSize, controls) {
                                    detectDragGestures(
                                        onDragStart = { selectedControlId = node.id },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val w = canvasSize.width.coerceAtLeast(100).toFloat()
                                            val h = canvasSize.height.coerceAtLeast(100).toFloat()
                                            val updated = controls.map { item ->
                                                if (item.id == node.id) {
                                                    item.copy(
                                                        xFraction = (item.xFraction + dragAmount.x / w).coerceIn(0.08f, 0.92f),
                                                        yFraction = (item.yFraction + dragAmount.y / h).coerceIn(0.10f, 0.90f)
                                                    )
                                                } else item
                                            }
                                            onUpdateControls(updated)
                                        }
                                    )
                                },
                            shape = CircleShape,
                            color = if (isSelected) primary.copy(alpha = 0.45f) else TacticalSurfaceElevated.copy(alpha = 0.85f),
                            border = BorderStroke(if (isSelected) 2.dp else 1.5.dp, if (isSelected) CyanHud else PureWhite)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(node.label, style = MaterialTheme.typography.labelSmall, color = PureWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add sample controls strip
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    availableSamples.forEach { sampleLabel ->
                        OutlinedButton(
                            onClick = {
                                val newNode = HudControlNode(
                                    id = "${sampleLabel.lowercase()}_${System.currentTimeMillis()}",
                                    label = sampleLabel,
                                    xFraction = 0.50f,
                                    yFraction = 0.50f,
                                    sizeDp = 46,
                                    opacity = 0.88f
                                )
                                onUpdateControls(controls + newNode)
                                selectedControlId = newNode.id
                            },
                            border = BorderStroke(1.dp, TacticalBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = CyanHud, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(sampleLabel, style = MaterialTheme.typography.labelSmall, color = PureWhite)
                        }
                    }
                }

                // Selected control Size, Opacity & Remove
                if (selectedControl != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Selected: ${selectedControl.label} (${selectedControl.sizeDp}dp)", style = MaterialTheme.typography.labelLarge, color = CyanHud)
                        TextButton(
                            onClick = {
                                val next = controls.filterNot { it.id == selectedControl.id }
                                onUpdateControls(next)
                                selectedControlId = next.firstOrNull()?.id
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Slider(
                        value = selectedControl.sizeDp.toFloat(),
                        onValueChange = { sz ->
                            onUpdateControls(controls.map { if (it.id == selectedControl.id) it.copy(sizeDp = sz.roundToInt()) else it })
                        },
                        valueRange = 30f..95f
                    )
                    Text("Opacity: ${(selectedControl.opacity * 100).roundToInt()}%", style = MaterialTheme.typography.labelSmall, color = SilverText)
                    Slider(
                        value = selectedControl.opacity,
                        onValueChange = { op ->
                            onUpdateControls(controls.map { if (it.id == selectedControl.id) it.copy(opacity = op) else it })
                        },
                        valueRange = 0.25f..1.0f
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlowingActionButton(
                        text = strings.saveHudBtn,
                        onClick = { showSaveDialog = true },
                        testTag = "save_hud_layout_button",
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = onExportHudJson,
                        modifier = Modifier.weight(1f).height(50.dp),
                        border = BorderStroke(1.dp, CyanHud),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanHud)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.exportHudBtn)
                    }
                }
            }
        }

        // Official Reference Guides
        item {
            TacticalCard {
                SectionHeader(title = if (language == AppLanguage.BN) "অফিশিয়াল HUD রেফারেন্স গাইড" else "Official HUD Reference Guides")
                Spacer(modifier = Modifier.height(8.dp))
                TrainingContent.hudLayouts.forEach { hud ->
                    Text(
                        text = "• ${if (language == AppLanguage.BN) hud.titleBn else hud.titleEn}: ${if (language == AppLanguage.BN) hud.descBn else hud.descEn}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SilverText,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }

        // Saved HUD Layouts
        if (savedLayouts.isNotEmpty()) {
            item {
                SectionHeader(title = "Saved HUD Layouts (${savedLayouts.size})")
            }
            items(savedLayouts, key = { it.id }) { layout ->
                TacticalCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(layout.name, style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.Bold)
                            Text("${layout.fingers}-Finger Layout", style = MaterialTheme.typography.labelSmall, color = EmeraldOnline)
                        }
                        Row {
                            TextButton(onClick = { onLoadSavedLayout(layout) }) { Text("Load", color = CyanHud) }
                            IconButton(onClick = { onDeleteLayout(layout) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        var layoutName by remember { mutableStateOf("${activeFingers}-Finger Custom HUD") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = TacticalSurfaceElevated,
            title = { Text(strings.saveHudBtn, color = PureWhite) },
            text = {
                OutlinedTextField(value = layoutName, onValueChange = { layoutName = it }, label = { Text("Layout Name") }, singleLine = true)
            },
            confirmButton = {
                GlowingActionButton(
                    text = "Save",
                    testTag = "confirm_save_hud",
                    onClick = {
                        onSaveLayout(layoutName, activeFingers)
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
