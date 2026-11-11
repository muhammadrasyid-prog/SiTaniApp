package com.example.sitani.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PupukTerpakaiDao {
    @Upsert
    suspend fun upsert(item: PupukTerpakaiEntity)

    @Query("SELECT * FROM pupuk_terpakai WHERE deletedAt IS NULL ORDER BY tanggalPakai DESC")
    fun getAllActive(): Flow<List<PupukTerpakaiEntity>>

    @Query("SELECT * FROM pupuk_terpakai WHERE isSynced = 0 OR deletedAt IS NOT NULL AND isSynced = 0")
    suspend fun getUnsynced(): List<PupukTerpakaiEntity>

    @Query("SELECT COUNT(*) FROM pupuk_terpakai WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("UPDATE pupuk_terpakai SET buktiFotoUrl = :url WHERE id = :id")
    suspend fun updateBuktiFotoUrlOnly(id: String, url: String)

    @Query("UPDATE pupuk_terpakai SET isSynced = 1 WHERE id = :id AND updatedAt = :sentUpdatedAt")
    suspend fun markSyncedIfUnchanged(id: String, sentUpdatedAt: Long): Int

    @Query("SELECT * FROM pupuk_terpakai WHERE id = :id")
    suspend fun getById(id: String): PupukTerpakaiEntity?

    @Query("UPDATE pupuk_terpakai SET deletedAt = :deletedAt, updatedAt = :updatedAt, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long, updatedAt: Long)
}
