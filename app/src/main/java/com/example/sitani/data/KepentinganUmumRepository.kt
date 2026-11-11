package com.example.sitani.data

import com.example.sitani.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID
import kotlin.math.roundToLong

class KepentinganUmumRepository(private val dao: KepentinganUmumDao) {

    val allKepentingan: Flow<List<KepentinganUmumEntity>> = dao.getAllActive()

    suspend fun save(
        id: String?,
        tanggal: String,
        kategoriKegiatan: String,
        deskripsi: String?,
        jumlah: Long,
        buktiFotoUrl: String?,
        buktiFotoLokal: String?
    ) {
        val now = System.currentTimeMillis()
        val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id ?: ""

        val existing = id?.let { dao.getById(it) }

        val entity = if (existing != null) {
            existing.copy(
                updatedAt = now,
                isSynced = false,
                tanggal = tanggal,
                kategoriKegiatan = kategoriKegiatan,
                deskripsi = deskripsi,
                jumlah = jumlah,
                buktiFotoUrl = buktiFotoUrl ?: existing.buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal
            )
        } else {
            KepentinganUmumEntity(
                id = UUID.randomUUID().toString(),
                createdBy = userId,
                buktiFotoUrl = buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal,
                isSynced = false,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
                tanggal = tanggal,
                kategoriKegiatan = kategoriKegiatan,
                deskripsi = deskripsi,
                jumlah = jumlah
            )
        }

        dao.upsert(entity)
    }

    suspend fun delete(id: String) {
        val now = System.currentTimeMillis()
        dao.softDelete(id, now, now)
    }
}

@Serializable
data class KepentinganUmumRemoteDto(
    val id: String,
    val created_by: String,
    val bukti_foto_url: String?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val tanggal: String,
    val kategori_kegiatan: String,
    val deskripsi: String?,
    val jumlah: Double
)

fun KepentinganUmumEntity.toRemoteDto(fallbackUserId: String): KepentinganUmumRemoteDto =
    KepentinganUmumRemoteDto(
        id = id,
        created_by = createdBy.ifBlank { fallbackUserId },
        bukti_foto_url = buktiFotoUrl,
        created_at = Instant.ofEpochMilli(createdAt).toString(),
        updated_at = Instant.ofEpochMilli(updatedAt).toString(),
        deleted_at = deletedAt?.let { Instant.ofEpochMilli(it).toString() },
        tanggal = tanggal,
        kategori_kegiatan = kategoriKegiatan,
        deskripsi = deskripsi,
        jumlah = jumlah.toDouble()
    )

fun KepentinganUmumRemoteDto.toRemoteRow(parseTimestamp: (String?) -> Long): RemoteRow<KepentinganUmumEntity> {
    val createdAt = parseTimestamp(created_at)
    val updatedAt = parseTimestamp(updated_at)
    val deletedAt = deleted_at?.let { parseTimestamp(it) }
    return RemoteRow(
        id = id,
        buktiFotoUrl = bukti_foto_url,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        buildEntity = { lokal ->
            KepentinganUmumEntity(
                id = id,
                createdBy = created_by,
                buktiFotoUrl = bukti_foto_url,
                buktiFotoLokal = lokal,
                isSynced = true,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
                tanggal = tanggal,
                kategoriKegiatan = kategori_kegiatan,
                deskripsi = deskripsi,
                jumlah = jumlah.roundToLong()
            )
        }
    )
}
