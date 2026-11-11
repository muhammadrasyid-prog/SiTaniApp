package com.example.sitani.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PenjualanPanenEntity::class,
        PerawatanLahanEntity::class,
        KepentinganUmumEntity::class,
        PupukMasukEntity::class,
        PupukTerpakaiEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun penjualanPanenDao(): PenjualanPanenDao
    abstract fun perawatanLahanDao(): PerawatanLahanDao
    abstract fun kepentinganUmumDao(): KepentinganUmumDao
    abstract fun pupukMasukDao(): PupukMasukDao
    abstract fun pupukTerpakaiDao(): PupukTerpakaiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sitani_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
