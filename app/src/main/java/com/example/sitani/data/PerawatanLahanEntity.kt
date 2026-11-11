package com.example.sitani.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "perawatan_lahan")
data class PerawatanLahanEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val createdBy: String,
    val buktiFotoUrl: String?,
    val buktiFotoLokal: String?,
    val isSynced: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val tanggal: String,
    val kategoriBiaya: String,
    val deskripsi: String?,
    val biaya: Long
)
