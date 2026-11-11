package com.example.sitani.data

import com.example.sitani.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID
import kotlin.math.roundToLong

class PerawatanLahanRepository(private val dao: PerawatanLahanDao) {

    val allPerawatan: Flow<List<PerawatanLahanEntity>> = dao.getAllActive()

    suspend fun save(
        id: String?,
        tanggal: String,
        kategoriBiaya: String,
        deskripsi: String?,
        biaya: Long,
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
                kategoriBiaya = kategoriBiaya,
                deskripsi = deskripsi,
                biaya = biaya,
                buktiFotoUrl = buktiFotoUrl ?: existing.buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal
            )
        } else {
            PerawatanLahanEntity(
                id = UUID.randomUUID().toString(),
                createdBy = userId,
                buktiFotoUrl = buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal,
                isSynced = false,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
                tanggal = tanggal,
                kategoriBiaya = kategoriBiaya,
                deskripsi = deskripsi,
                biaya = biaya
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
data class PerawatanLahanRemoteDto(
    val id: String,
    val created_by: String,
    val bukti_foto_url: String?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val tanggal: String,
    val kategori_biaya: String,
    val deskripsi: String?,
    val biaya: Double
)

fun PerawatanLahanEntity.toRemoteDto(fallbackUserId: String): PerawatanLahanRemoteDto =
    PerawatanLahanRemoteDto(
        id = id,
        created_by = createdBy.ifBlank { fallbackUserId },
        bukti_foto_url = buktiFotoUrl,
        created_at = Instant.ofEpochMilli(createdAt).toString(),
        updated_at = Instant.ofEpochMilli(updatedAt).toString(),
        deleted_at = deletedAt?.let { Instant.ofEpochMilli(it).toString() },
        tanggal = tanggal,
        kategori_biaya = kategoriBiaya,
        deskripsi = deskripsi,
        biaya = biaya.toDouble()
    )

fun PerawatanLahanRemoteDto.toRemoteRow(parseTimestamp: (String?) -> Long): RemoteRow<PerawatanLahanEntity> {
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
            PerawatanLahanEntity(
                id = id,
                createdBy = created_by,
                buktiFotoUrl = bukti_foto_url,
                buktiFotoLokal = lokal,
                isSynced = true,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
                tanggal = tanggal,
                kategoriBiaya = kategori_biaya,
                deskripsi = deskripsi,
                biaya = biaya.roundToLong()
            )
        }
    )
}
