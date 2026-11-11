package com.example.sitani.data

import com.example.sitani.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID
import kotlin.math.roundToLong

class PupukMasukRepository(private val dao: PupukMasukDao) {

    val allPupukMasuk: Flow<List<PupukMasukEntity>> = dao.getAllActive()

    suspend fun save(
        id: String?,
        tanggal: String,
        namaPupuk: String,
        kuantitas: Double,
        satuan: String,
        hargaSatuan: Long?,
        supplier: String?,
        keterangan: String?,
        buktiFotoUrl: String?,
        buktiFotoLokal: String?
    ) {
        val now = System.currentTimeMillis()
        val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id ?: ""
        val totalHarga = hargaSatuan?.let { (kuantitas * it).toLong() }

        val existing = id?.let { dao.getById(it) }

        val entity = if (existing != null) {
            existing.copy(
                updatedAt = now,
                isSynced = false,
                tanggal = tanggal,
                namaPupuk = namaPupuk,
                kuantitas = kuantitas,
                satuan = satuan,
                hargaSatuan = hargaSatuan,
                totalHarga = totalHarga,
                supplier = supplier,
                keterangan = keterangan,
                buktiFotoUrl = buktiFotoUrl ?: existing.buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal
            )
        } else {
            PupukMasukEntity(
                id = UUID.randomUUID().toString(),
                createdBy = userId,
                buktiFotoUrl = buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal,
                isSynced = false,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
                tanggal = tanggal,
                namaPupuk = namaPupuk,
                kuantitas = kuantitas,
                satuan = satuan,
                hargaSatuan = hargaSatuan,
                totalHarga = totalHarga,
                supplier = supplier,
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
data class PupukMasukRemoteDto(
    val id: String,
    val created_by: String,
    val bukti_foto_url: String?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val tanggal: String,
    val nama_pupuk: String,
    val kuantitas: Double,
    val satuan: String,
    val harga_satuan: Double?,
    val total_harga: Double?,
    val supplier: String?,
    val keterangan: String?
)

fun PupukMasukEntity.toRemoteDto(fallbackUserId: String): PupukMasukRemoteDto =
    PupukMasukRemoteDto(
        id = id,
        created_by = createdBy.ifBlank { fallbackUserId },
        bukti_foto_url = buktiFotoUrl,
        created_at = Instant.ofEpochMilli(createdAt).toString(),
        updated_at = Instant.ofEpochMilli(updatedAt).toString(),
        deleted_at = deletedAt?.let { Instant.ofEpochMilli(it).toString() },
        tanggal = tanggal,
        nama_pupuk = namaPupuk,
        kuantitas = kuantitas,
        satuan = satuan,
        harga_satuan = hargaSatuan?.toDouble(),
        total_harga = totalHarga?.toDouble(),
        supplier = supplier,
        keterangan = keterangan
    )

fun PupukMasukRemoteDto.toRemoteRow(parseTimestamp: (String?) -> Long): RemoteRow<PupukMasukEntity> {
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
            PupukMasukEntity(
                id = id,
                createdBy = created_by,
                buktiFotoUrl = bukti_foto_url,
                buktiFotoLokal = lokal,
                isSynced = true,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
                tanggal = tanggal,
                namaPupuk = nama_pupuk,
                kuantitas = kuantitas,
                satuan = satuan,
                hargaSatuan = harga_satuan?.roundToLong(),
                totalHarga = total_harga?.roundToLong(),
                supplier = supplier,
                keterangan = keterangan
            )
        }
    )
}
