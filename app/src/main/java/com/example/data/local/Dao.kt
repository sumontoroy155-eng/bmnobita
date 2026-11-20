package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM saved_profiles ORDER BY isFavorite DESC, updatedAt DESC")
    fun observeAllProfiles(): Flow<List<SavedProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: SavedProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<SavedProfileEntity>)

    @Query("UPDATE saved_profiles SET isFavorite = :favorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE saved_profiles SET name = :newName, updatedAt = :updatedAt WHERE id = :id")
    suspend fun renameProfile(id: Long, newName: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM saved_profiles WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface TrainingProgressDao {
    @Query("SELECT * FROM training_progress")
    fun observeAllProgress(): Flow<List<TrainingProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: TrainingProgressEntity)

    @Query("DELETE FROM training_progress")
    suspend fun resetAll()
}

@Dao
interface AimSessionDao {
    @Query("SELECT * FROM aim_sessions ORDER BY timestamp DESC LIMIT 50")
    fun observeRecentSessions(): Flow<List<AimSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AimSessionEntity): Long

    @Query("DELETE FROM aim_sessions")
    suspend fun clearAll()
}

@Dao
interface HudLayoutDao {
    @Query("SELECT * FROM hud_layouts ORDER BY updatedAt DESC")
    fun observeLayouts(): Flow<List<HudLayoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayout(layout: HudLayoutEntity): Long

    @Query("DELETE FROM hud_layouts WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface PerfectSetupDao {
    @Query("SELECT * FROM perfect_setups ORDER BY createdAt DESC")
    fun observeSetups(): Flow<List<PerfectSetupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetup(setup: PerfectSetupEntity): Long

    @Query("DELETE FROM perfect_setups WHERE id = :id")
    suspend fun deleteById(id: Long)
}
