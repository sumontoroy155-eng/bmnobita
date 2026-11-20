package com.example.data.local

import kotlinx.coroutines.flow.Flow

class ProfileRepository(
    private val profileDao: ProfileDao,
    private val trainingProgressDao: TrainingProgressDao,
    private val aimSessionDao: AimSessionDao,
    private val hudLayoutDao: HudLayoutDao,
    private val perfectSetupDao: PerfectSetupDao
) {
    val allProfiles: Flow<List<SavedProfileEntity>> = profileDao.observeAllProfiles()
    val allTrainingProgress: Flow<List<TrainingProgressEntity>> = trainingProgressDao.observeAllProgress()
    val recentAimSessions: Flow<List<AimSessionEntity>> = aimSessionDao.observeRecentSessions()
    val savedHudLayouts: Flow<List<HudLayoutEntity>> = hudLayoutDao.observeLayouts()
    val savedPerfectSetups: Flow<List<PerfectSetupEntity>> = perfectSetupDao.observeSetups()

    suspend fun saveProfile(profile: SavedProfileEntity): Long = profileDao.insertProfile(profile)

    suspend fun importProfiles(profiles: List<SavedProfileEntity>) = profileDao.insertProfiles(profiles)

    suspend fun setFavorite(id: Long, isFavorite: Boolean) = profileDao.updateFavorite(id, isFavorite)

    suspend fun renameProfile(id: Long, newName: String) = profileDao.renameProfile(id, newName)

    suspend fun deleteProfile(id: Long) = profileDao.deleteById(id)

    suspend fun incrementDrillRep(drillId: String, currentReps: Int, lastScore: Int) {
        trainingProgressDao.upsertProgress(
            TrainingProgressEntity(
                drillId = drillId,
                completedReps = currentReps + 1,
                lastPracticeScore = lastScore,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun recordPracticeScore(drillId: String, currentReps: Int, score: Int) {
        trainingProgressDao.upsertProgress(
            TrainingProgressEntity(
                drillId = drillId,
                completedReps = currentReps,
                lastPracticeScore = score,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetTrainingProgress() = trainingProgressDao.resetAll()

    suspend fun insertAimSession(session: AimSessionEntity): Long = aimSessionDao.insertSession(session)

    suspend fun saveHudLayout(layout: HudLayoutEntity): Long = hudLayoutDao.insertLayout(layout)

    suspend fun deleteHudLayout(id: Long) = hudLayoutDao.deleteById(id)

    suspend fun savePerfectSetup(setup: PerfectSetupEntity): Long = perfectSetupDao.insertSetup(setup)

    suspend fun deletePerfectSetup(id: Long) = perfectSetupDao.deleteById(id)
}
