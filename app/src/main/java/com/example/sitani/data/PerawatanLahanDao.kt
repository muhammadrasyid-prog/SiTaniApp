package com.example.sitani.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PerawatanLahanDao {
    @Upsert
    suspend fun upsert(item: PerawatanLahanEntity)

    @Query("SELECT * FROM perawatan_lahan WHERE deletedAt IS NULL ORDER BY tanggal DESC")
    fun getAllActive(): Flow<List<PerawatanLahanEntity>>

    @Query("SELECT * FROM perawatan_lahan WHERE isSynced = 0 OR deletedAt IS NOT NULL AND isSynced = 0")
    suspend fun getUnsynced(): List<PerawatanLahanEntity>

    @Query("SELECT COUNT(*) FROM perawatan_lahan WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("UPDATE perawatan_lahan SET buktiFotoUrl = :url WHERE id = :id")
    suspend fun updateBuktiFotoUrlOnly(id: String, url: String)

    @Query("UPDATE perawatan_lahan SET isSynced = 1 WHERE id = :id AND updatedAt = :sentUpdatedAt")
    suspend fun markSyncedIfUnchanged(id: String, sentUpdatedAt: Long): Int

    @Query("SELECT * FROM perawatan_lahan WHERE id = :id")
    suspend fun getById(id: String): PerawatanLahanEntity?

    @Query("UPDATE perawatan_lahan SET deletedAt = :deletedAt, updatedAt = :updatedAt, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long, updatedAt: Long)
}
