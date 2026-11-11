package com.example.sitani.data

import com.example.sitani.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

class PupukTerpakaiRepository(private val dao: PupukTerpakaiDao) {

    val allPupukTerpakai: Flow<List<PupukTerpakaiEntity>> = dao.getAllActive()

    suspend fun save(
        id: String?,
        tanggalPakai: String,
        namaPupuk: String,
        kuantitas: Double,
        satuan: String,
        tujuanPakai: String?,
        keterangan: String?,
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
                tanggalPakai = tanggalPakai,
                namaPupuk = namaPupuk,
                kuantitas = kuantitas,
                satuan = satuan,
                tujuanPakai = tujuanPakai,
                keterangan = keterangan,
                buktiFotoUrl = buktiFotoUrl ?: existing.buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal
            )
        } else {
            PupukTerpakaiEntity(
                id = UUID.randomUUID().toString(),
                createdBy = userId,
                buktiFotoUrl = buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal,
                isSynced = false,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
                tanggalPakai = tanggalPakai,
                namaPupuk = namaPupuk,
                kuantitas = kuantitas,
                satuan = satuan,
                tujuanPakai = tujuanPakai,
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

@Serializable
data class PupukTerpakaiRemoteDto(
    val id: String,
    val created_by: String,
    val bukti_foto_url: String?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val tanggal_pakai: String,
    val nama_pupuk: String,
    val kuantitas: Double,
    val satuan: String,
    val tujuan_pakai: String?,
    val keterangan: String?
)

fun PupukTerpakaiEntity.toRemoteDto(fallbackUserId: String): PupukTerpakaiRemoteDto =
    PupukTerpakaiRemoteDto(
        id = id,
        created_by = createdBy.ifBlank { fallbackUserId },
        bukti_foto_url = buktiFotoUrl,
        created_at = Instant.ofEpochMilli(createdAt).toString(),
        updated_at = Instant.ofEpochMilli(updatedAt).toString(),
        deleted_at = deletedAt?.let { Instant.ofEpochMilli(it).toString() },
        tanggal_pakai = tanggalPakai,
        nama_pupuk = namaPupuk,
        kuantitas = kuantitas,
        satuan = satuan,
        tujuan_pakai = tujuanPakai,
        keterangan = keterangan
    )

fun PupukTerpakaiRemoteDto.toRemoteRow(parseTimestamp: (String?) -> Long): RemoteRow<PupukTerpakaiEntity> {
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
            PupukTerpakaiEntity(
                id = id,
                createdBy = created_by,
                buktiFotoUrl = bukti_foto_url,
                buktiFotoLokal = lokal,
                isSynced = true,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
                tanggalPakai = tanggal_pakai,
                namaPupuk = nama_pupuk,
                kuantitas = kuantitas,
                satuan = satuan,
                tujuanPakai = tujuan_pakai,
                keterangan = keterangan
            )
        }
    )
}
