package com.example.sitani.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "penjualan_panen")
data class PenjualanPanenEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val createdBy: String,
    val buktiFotoUrl: String?,
    val buktiFotoLokal: String?,
    val isSynced: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val tanggalJual: String,
    val jenisTanaman: String,
    val volumeKg: Double,
    val hargaPerKg: Long,
    val totalHarga: Long,
    val namaPembeli: String?,
    val keterangan: String?
)
