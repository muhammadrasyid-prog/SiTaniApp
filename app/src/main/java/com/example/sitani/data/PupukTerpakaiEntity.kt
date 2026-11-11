package com.example.sitani.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pupuk_terpakai")
data class PupukTerpakaiEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val createdBy: String,
    val buktiFotoUrl: String?,
    val buktiFotoLokal: String?,
    val isSynced: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val tanggalPakai: String,
    val namaPupuk: String,
    val kuantitas: Double,
    val satuan: String,
    val tujuanPakai: String?,
    val keterangan: String?
)
