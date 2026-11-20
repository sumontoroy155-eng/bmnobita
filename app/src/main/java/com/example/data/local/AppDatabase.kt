package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SavedProfileEntity::class,
        TrainingProgressEntity::class,
        AimSessionEntity::class,
        HudLayoutEntity::class,
        PerfectSetupEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun trainingProgressDao(): TrainingProgressDao
    abstract fun aimSessionDao(): AimSessionDao
    abstract fun hudLayoutDao(): HudLayoutDao
    abstract fun perfectSetupDao(): PerfectSetupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bm_gaming_pro.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
