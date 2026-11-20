package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.SensitivityValues

@Entity(tableName = "saved_profiles")
data class SavedProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val deviceLabel: String,
    val modeId: String,
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniperScope: Int,
    val freeLook: Int,
    val fireButtonPercent: Int,
    val dpiNote: String,
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toSensitivityValues(): SensitivityValues = SensitivityValues(
        general = general,
        redDot = redDot,
        scope2x = scope2x,
        scope4x = scope4x,
        sniperScope = sniperScope,
        freeLook = freeLook
    )
}

@Entity(tableName = "training_progress")
data class TrainingProgressEntity(
    @PrimaryKey val drillId: String,
    val completedReps: Int = 0,
    val lastPracticeScore: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "aim_sessions")
data class AimSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modeId: String,
    val hits: Int,
    val totalShots: Int,
    val accuracyPercent: Int,
    val avgReactionMs: Int,
    val score: Int,
    val xpEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "hud_layouts")
data class HudLayoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val fingers: Int,
    val controlsJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "perfect_setups")
data class PerfectSetupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val deviceLabel: String,
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniperScope: Int,
    val freeLook: Int,
    val recommendedDpiRange: String,
    val fireButtonPercent: Int,
    val hudFingers: Int,
    val createdAt: Long = System.currentTimeMillis()
)
