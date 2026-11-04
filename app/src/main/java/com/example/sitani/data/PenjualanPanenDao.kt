package com.example.sitani.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PenjualanPanenDao {
    @Upsert
    suspend fun upsert(item: PenjualanPanenEntity)

    @Query("SELECT * FROM penjualan_panen WHERE deletedAt IS NULL ORDER BY tanggalJual DESC")
    fun getAllActive(): Flow<List<PenjualanPanenEntity>>

    @Query("SELECT * FROM penjualan_panen WHERE isSynced = 0 OR deletedAt IS NOT NULL AND isSynced = 0")
    suspend fun getUnsynced(): List<PenjualanPanenEntity>

    @Query("SELECT COUNT(*) FROM penjualan_panen WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("UPDATE penjualan_panen SET buktiFotoUrl = :url WHERE id = :id")
    suspend fun updateBuktiFotoUrlOnly(id: String, url: String)

    @Query("UPDATE penjualan_panen SET isSynced = 1 WHERE id = :id AND updatedAt = :sentUpdatedAt")
    suspend fun markSyncedIfUnchanged(id: String, sentUpdatedAt: Long): Int

    @Query("SELECT * FROM penjualan_panen WHERE id = :id")
    suspend fun getById(id: String): PenjualanPanenEntity?

    @Query("UPDATE penjualan_panen SET deletedAt = :deletedAt, updatedAt = :updatedAt, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long, updatedAt: Long)
}
