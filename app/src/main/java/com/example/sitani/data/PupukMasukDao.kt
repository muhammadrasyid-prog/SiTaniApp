package com.example.sitani.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PupukMasukDao {
    @Upsert
    suspend fun upsert(item: PupukMasukEntity)

    @Query("SELECT * FROM pupuk_masuk WHERE deletedAt IS NULL ORDER BY tanggal DESC")
    fun getAllActive(): Flow<List<PupukMasukEntity>>

    @Query("SELECT * FROM pupuk_masuk WHERE isSynced = 0 OR deletedAt IS NOT NULL AND isSynced = 0")
    suspend fun getUnsynced(): List<PupukMasukEntity>

    @Query("SELECT COUNT(*) FROM pupuk_masuk WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("UPDATE pupuk_masuk SET buktiFotoUrl = :url WHERE id = :id")
    suspend fun updateBuktiFotoUrlOnly(id: String, url: String)

    @Query("UPDATE pupuk_masuk SET isSynced = 1 WHERE id = :id AND updatedAt = :sentUpdatedAt")
    suspend fun markSyncedIfUnchanged(id: String, sentUpdatedAt: Long): Int

    @Query("SELECT * FROM pupuk_masuk WHERE id = :id")
    suspend fun getById(id: String): PupukMasukEntity?

    @Query("UPDATE pupuk_masuk SET deletedAt = :deletedAt, updatedAt = :updatedAt, isSynced = 0 WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long, updatedAt: Long)
}
