package com.example.sitani.data

import com.example.sitani.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class PenjualanRepository(private val dao: PenjualanPanenDao) {

    val allPenjualan: Flow<List<PenjualanPanenEntity>> = dao.getAllActive()

    suspend fun save(
        id: String?,
        tanggalJual: String,
        jenisTanaman: String,
        volumeKg: Double,
        hargaPerKg: Long,
        namaPembeli: String?,
        keterangan: String?,
        buktiFotoUrl: String?,
        buktiFotoLokal: String?
    ) {
        val now = System.currentTimeMillis()
        val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id ?: ""
        val totalHarga = (volumeKg * hargaPerKg).toLong()

        val existing = id?.let { dao.getById(it) }

        val entity = if (existing != null) {
            existing.copy(
                updatedAt = now,
                isSynced = false,
                tanggalJual = tanggalJual,
                jenisTanaman = jenisTanaman,
                volumeKg = volumeKg,
                hargaPerKg = hargaPerKg,
                totalHarga = totalHarga,
                namaPembeli = namaPembeli,
                keterangan = keterangan,
                buktiFotoUrl = buktiFotoUrl ?: existing.buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal
            )
        } else {
            PenjualanPanenEntity(
                id = UUID.randomUUID().toString(),
                createdBy = userId,
                buktiFotoUrl = buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal,
                isSynced = false,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
                tanggalJual = tanggalJual,
                jenisTanaman = jenisTanaman,
                volumeKg = volumeKg,
                hargaPerKg = hargaPerKg,
                totalHarga = totalHarga,
                namaPembeli = namaPembeli,
                keterangan = keterangan
            )
        }

        dao.upsert(entity)
    }

    suspend fun delete(id: String) {
        val now = System.currentTimeMillis()
        dao.softDelete(id, now, now)
    }
}
