package com.example.sitani.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface KepentinganUmumDao {
    @Upsert
    suspend fun upsert(item: KepentinganUmumEntity)

    @Query("SELECT * FROM kepentingan_umum WHERE deletedAt IS NULL ORDER BY tanggal DESC")
    fun getAllActive(): Flow<List<KepentinganUmumEntity>>

    @Query("SELECT * FROM kepentingan_umum WHERE isSynced = 0 OR deletedAt IS NOT NULL AND isSynced = 0")
    suspend fun getUnsynced(): List<KepentinganUmumEntity>

    @Query("SELECT COUNT(*) FROM kepentingan_umum WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("UPDATE kepentingan_umum SET buktiFotoUrl = :url WHERE id = :id")
    suspend fun updateBuktiFotoUrlOnly(id: String, url: String)

    @Query("UPDATE kepentingan_umum SET isSynced = 1 WHERE id = :id AND updatedAt = :sentUpdatedAt")
    suspend fun markSyncedIfUnchanged(id: String, sentUpdatedAt: Long): Int

    @Query("SELECT * FROM kepentingan_umum WHERE id = :id")
    suspend fun getById(id: String): KepentinganUmumEntity?

    @Query("UPDATE kepentingan_umum SET deletedAt = :deletedAt, updatedAt = :updatedAt, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long, updatedAt: Long)
}
