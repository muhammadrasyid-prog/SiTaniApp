package com.example.sitani.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import android.util.Log
import com.example.sitani.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable
import java.io.File
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.math.roundToLong

sealed class SyncResult {
    data class Berhasil(
        val jumlahTerkirim: Int,
        val jumlahDiterima: Int,
        val fotoNaik: Int,
        val fotoTurun: Int,
        val galatPerTabel: Map<String, String> = emptyMap()
    ) : SyncResult()
    object TidakAdaKoneksi : SyncResult()
    object BelumLogin : SyncResult()
    data class Gagal(val pesan: String) : SyncResult()
}

@Serializable
data class PenjualanRemoteDto(
    val id: String,
    val created_by: String,
    val bukti_foto_url: String?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val tanggal_jual: String,
    val jenis_tanaman: String,
    val volume_kg: Double,
    val harga_per_kg: Double,
    val total_harga: Double,
    val nama_pembeli: String?,
    val keterangan: String?
)

fun PenjualanPanenEntity.toRemoteDto(fallbackUserId: String): PenjualanRemoteDto =
    PenjualanRemoteDto(
        id = id,
        created_by = createdBy.ifBlank { fallbackUserId },
        bukti_foto_url = buktiFotoUrl,
        created_at = Instant.ofEpochMilli(createdAt).toString(),
        updated_at = Instant.ofEpochMilli(updatedAt).toString(),
        deleted_at = deletedAt?.let { Instant.ofEpochMilli(it).toString() },
        tanggal_jual = tanggalJual,
        jenis_tanaman = jenisTanaman,
        volume_kg = volumeKg,
        harga_per_kg = hargaPerKg.toDouble(),
        total_harga = totalHarga.toDouble(),
        nama_pembeli = namaPembeli,
        keterangan = keterangan
    )

fun PenjualanRemoteDto.toRemoteRow(parseTimestamp: (String?) -> Long): RemoteRow<PenjualanPanenEntity> {
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
            PenjualanPanenEntity(
                id = id,
                createdBy = created_by,
                buktiFotoUrl = bukti_foto_url,
                buktiFotoLokal = lokal,
                isSynced = true,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
                tanggalJual = tanggal_jual,
                jenisTanaman = jenis_tanaman,
                volumeKg = volume_kg,
                hargaPerKg = harga_per_kg.roundToLong(),
                totalHarga = total_harga.roundToLong(),
                namaPembeli = nama_pembeli,
                keterangan = keterangan
            )
        }
    )
}

class RemoteRow<E>(
    val id: String,
    val buktiFotoUrl: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val buildEntity: (buktiFotoLokal: String?) -> E
)

private class TableSyncStats(
    var terkirim: Int = 0,
    var diterima: Int = 0,
    var fotoNaik: Int = 0,
    var fotoTurun: Int = 0
)

class SyncRepository(private val context: Context) {
    private val penjualanDao = AppDatabase.getDatabase(context).penjualanPanenDao()
    private val perawatanDao = AppDatabase.getDatabase(context).perawatanLahanDao()
    private val kepentinganDao = AppDatabase.getDatabase(context).kepentinganUmumDao()
    private val pupukMasukDao = AppDatabase.getDatabase(context).pupukMasukDao()
    private val pupukTerpakaiDao = AppDatabase.getDatabase(context).pupukTerpakaiDao()

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun parseTimestamp(raw: String?): Long {
        if (raw.isNullOrBlank()) return System.currentTimeMillis()
        return runCatching {
            OffsetDateTime.parse(raw).toInstant().toEpochMilli()
        }.recoverCatching {
            Instant.parse(raw).toEpochMilli()
        }.getOrDefault(System.currentTimeMillis())
    }

    suspend fun syncAll(): SyncResult {
        if (!isNetworkAvailable()) {
            return SyncResult.TidakAdaKoneksi
        }

        val currentUser = SupabaseProvider.client.auth.currentUserOrNull()
        if (currentUser == null) {
            return SyncResult.BelumLogin
        }

        val jobs: List<Pair<String, suspend () -> TableSyncStats>> = listOf(
            "penjualan_panen" to { syncPenjualanPanen(currentUser.id) },
            "perawatan_lahan" to { syncPerawatanLahan(currentUser.id) },
            "kepentingan_umum" to { syncKepentinganUmum(currentUser.id) },
            "pupuk_masuk" to { syncPupukMasuk(currentUser.id) },
            "pupuk_terpakai" to { syncPupukTerpakai(currentUser.id) }
        )

        var totalTerkirim = 0
        var totalDiterima = 0
        var totalFotoNaik = 0
        var totalFotoTurun = 0
        val galatPerTabel = mutableMapOf<String, String>()

        for ((namaTabel, aksi) in jobs) {
            runCatching {
                val stats = aksi()
                totalTerkirim += stats.terkirim
                totalDiterima += stats.diterima
                totalFotoNaik += stats.fotoNaik
                totalFotoTurun += stats.fotoTurun
            }.onFailure { e ->
                Log.e("Sync", "Gagal sinkronisasi $namaTabel", e)
                galatPerTabel[namaTabel] = e.localizedMessage ?: e.javaClass.simpleName
            }
        }

        return SyncResult.Berhasil(totalTerkirim, totalDiterima, totalFotoNaik, totalFotoTurun, galatPerTabel)
    }

    private suspend inline fun <reified D : Any, E> syncSatuTabel(
        tableName: String,
        getUnsynced: suspend () -> List<E>,
        getById: suspend (String) -> E?,
        upsertRow: suspend (E) -> Unit,
        markSyncedIfUnchanged: suspend (String, Long) -> Int,
        updateBuktiFotoUrlOnly: suspend (String, String) -> Unit,
        idOf: (E) -> String,
        localPathOf: (E) -> String?,
        entityUrlOf: (E) -> String?,
        updatedAtOf: (E) -> Long,
        isSyncedOf: (E) -> Boolean,
        toRemoteDto: (E) -> D,
        decodeRemoteList: suspend () -> List<RemoteRow<E>>
    ): TableSyncStats {
        val stats = TableSyncStats()
        val storageBucket = SupabaseProvider.client.storage["bukti"]
        val unsyncedList = getUnsynced()

        // 1. PUSH baris per baris
        for (entity in unsyncedList) {
            val entityId = idOf(entity)
            val currentUrl = entityUrlOf(entity)
            val localPath = localPathOf(entity)

            if (!localPath.isNullOrBlank()) {
                val localFile = File(localPath)
                if (localFile.exists()) {
                    val needsUpload = currentUrl == null || !currentUrl.contains(entityId)
                    if (needsUpload) {
                        val storagePath = "$tableName/${entityId}_${System.currentTimeMillis()}.jpg"
                        val uploadSuccess = runCatching {
                            val bytes = localFile.readBytes()
                            storageBucket.upload(storagePath, bytes) {
                                upsert = true
                            }
                            true
                        }.getOrElse { e ->
                            Log.e("Sync", "Gagal unggah foto untuk $entityId", e)
                            false
                        }

                        if (uploadSuccess) {
                            stats.fotoNaik++
                            // Simpan URL ke Room lewat query khusus (tanpa ubah isSynced/updatedAt)
                            updateBuktiFotoUrlOnly(entityId, storagePath)
                            Log.d("Sync", "id: $entityId, foto diunggah ke $storagePath")
                        } else {
                            Log.e("Sync", "Gagal unggah foto untuk $entityId, lewati upsert baris ini")
                            Log.d("Sync", "id: $entityId, foto tidak diunggah, hasil upsert: gagal, hasil markSynced: false")
                            continue
                        }
                    } else {
                        Log.d("Sync", "id: $entityId, foto tidak diunggah (sudah ada)")
                    }
                } else {
                    Log.d("Sync", "id: $entityId, foto tidak diunggah (file lokal tidak ada)")
                }
            } else {
                Log.d("Sync", "id: $entityId, foto tidak diunggah (tidak ada lokal)")
            }

            // Baca ulang baris terbaru dari Room untuk mendapatkan updated_at dan buktiFotoUrl terkini
            val latestEntity = getById(entityId) ?: entity
            val dto = toRemoteDto(latestEntity)

            val upsertSuccess = runCatching {
                SupabaseProvider.client.postgrest[tableName].upsert(dto)
                true
            }.getOrElse { e ->
                Log.e("Sync", "Gagal upsert $entityId ke Supabase", e)
                false
            }

            if (upsertSuccess) {
                val rowsUpdated = markSyncedIfUnchanged(idOf(latestEntity), updatedAtOf(latestEntity))
                if (rowsUpdated > 0) {
                    stats.terkirim++
                    Log.d("Sync", "id: $entityId, hasil upsert: sukses, hasil markSynced: sukses (1)")
                } else {
                    Log.d("Sync", "id: $entityId, hasil upsert: sukses, hasil markSynced: dilewati (updated_at berubah saat sync)")
                }
            } else {
                Log.d("Sync", "id: $entityId, hasil upsert: gagal, hasil markSynced: false")
            }
        }
        Log.d("Sync", "$tableName: total baris sukses terkirim: ${stats.terkirim}, foto naik: ${stats.fotoNaik}")

        // 2. PULL
        val remoteList = decodeRemoteList()
        stats.diterima = remoteList.size

        Log.d("Sync", "$tableName: jumlah baris diterima dari Supabase: ${remoteList.size}")

        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: File(context.filesDir, "Pictures").apply { mkdirs() }
        if (!picturesDir.exists()) picturesDir.mkdirs()

        for (remote in remoteList) {
            val local = getById(remote.id)
            var resolvedLocalPath = local?.let { localPathOf(it) }
            val remoteUrl = remote.buktiFotoUrl

            if (!remoteUrl.isNullOrBlank()) {
                val needDownload = resolvedLocalPath == null || !File(resolvedLocalPath).exists() ||
                    (local?.let { entityUrlOf(it) } != remoteUrl)
                if (needDownload) {
                    val downloadSuccess = runCatching {
                        val bytes = storageBucket.downloadAuthenticated(remoteUrl)
                        val targetFile = File(picturesDir, "download_${remote.id}_${System.currentTimeMillis()}.jpg")
                        targetFile.writeBytes(bytes)

                        if (!resolvedLocalPath.isNullOrBlank() && resolvedLocalPath != targetFile.absolutePath) {
                            File(resolvedLocalPath).delete()
                        }
                        resolvedLocalPath = targetFile.absolutePath
                        true
                    }.getOrElse { e ->
                        Log.e("Sync", "Gagal unduh foto $remoteUrl", e)
                        false
                    }

                    if (downloadSuccess) {
                        stats.fotoTurun++
                    }
                }
            }

            if (local == null) {
                upsertRow(remote.buildEntity(resolvedLocalPath))
            } else {
                if (isSyncedOf(local)) {
                    if (remote.updatedAt > updatedAtOf(local)) {
                        upsertRow(remote.buildEntity(resolvedLocalPath))
                    }
                }
            }
        }
        Log.d("Sync", "$tableName: jumlah baris dimasukkan atau diperbarui di Room: terima ${remoteList.size}, foto turun: ${stats.fotoTurun}")

        return stats
    }

    private suspend fun syncPenjualanPanen(fallbackUserId: String): TableSyncStats = syncSatuTabel<PenjualanRemoteDto, PenjualanPanenEntity>(
        tableName = "penjualan_panen",
        getUnsynced = { penjualanDao.getUnsynced() },
        getById = { penjualanDao.getById(it) },
        upsertRow = { penjualanDao.upsert(it) },
        markSyncedIfUnchanged = { id, sentUpdatedAt -> penjualanDao.markSyncedIfUnchanged(id, sentUpdatedAt) },
        updateBuktiFotoUrlOnly = { id, url -> penjualanDao.updateBuktiFotoUrlOnly(id, url) },
        idOf = { it.id },
        localPathOf = { it.buktiFotoLokal },
        entityUrlOf = { it.buktiFotoUrl },
        updatedAtOf = { it.updatedAt },
        isSyncedOf = { it.isSynced },
        toRemoteDto = { entity -> entity.toRemoteDto(fallbackUserId) },
        decodeRemoteList = {
            SupabaseProvider.client.postgrest["penjualan_panen"]
                .select()
                .decodeList<PenjualanRemoteDto>()
                .map { it.toRemoteRow(::parseTimestamp) }
        }
    )

    private suspend fun syncPerawatanLahan(fallbackUserId: String): TableSyncStats = syncSatuTabel<PerawatanLahanRemoteDto, PerawatanLahanEntity>(
        tableName = "perawatan_lahan",
        getUnsynced = { perawatanDao.getUnsynced() },
        getById = { perawatanDao.getById(it) },
        upsertRow = { perawatanDao.upsert(it) },
        markSyncedIfUnchanged = { id, sentUpdatedAt -> perawatanDao.markSyncedIfUnchanged(id, sentUpdatedAt) },
        updateBuktiFotoUrlOnly = { id, url -> perawatanDao.updateBuktiFotoUrlOnly(id, url) },
        idOf = { it.id },
        localPathOf = { it.buktiFotoLokal },
        entityUrlOf = { it.buktiFotoUrl },
        updatedAtOf = { it.updatedAt },
        isSyncedOf = { it.isSynced },
        toRemoteDto = { entity -> entity.toRemoteDto(fallbackUserId) },
        decodeRemoteList = {
            SupabaseProvider.client.postgrest["perawatan_lahan"]
                .select()
                .decodeList<PerawatanLahanRemoteDto>()
                .map { it.toRemoteRow(::parseTimestamp) }
        }
    )

    private suspend fun syncKepentinganUmum(fallbackUserId: String): TableSyncStats = syncSatuTabel<KepentinganUmumRemoteDto, KepentinganUmumEntity>(
        tableName = "kepentingan_umum",
        getUnsynced = { kepentinganDao.getUnsynced() },
        getById = { kepentinganDao.getById(it) },
        upsertRow = { kepentinganDao.upsert(it) },
        markSyncedIfUnchanged = { id, sentUpdatedAt -> kepentinganDao.markSyncedIfUnchanged(id, sentUpdatedAt) },
        updateBuktiFotoUrlOnly = { id, url -> kepentinganDao.updateBuktiFotoUrlOnly(id, url) },
        idOf = { it.id },
        localPathOf = { it.buktiFotoLokal },
        entityUrlOf = { it.buktiFotoUrl },
        updatedAtOf = { it.updatedAt },
        isSyncedOf = { it.isSynced },
        toRemoteDto = { entity -> entity.toRemoteDto(fallbackUserId) },
        decodeRemoteList = {
            SupabaseProvider.client.postgrest["kepentingan_umum"]
                .select()
                .decodeList<KepentinganUmumRemoteDto>()
                .map { it.toRemoteRow(::parseTimestamp) }
        }
    )

    private suspend fun syncPupukMasuk(fallbackUserId: String): TableSyncStats = syncSatuTabel<PupukMasukRemoteDto, PupukMasukEntity>(
        tableName = "pupuk_masuk",
        getUnsynced = { pupukMasukDao.getUnsynced() },
        getById = { pupukMasukDao.getById(it) },
        upsertRow = { pupukMasukDao.upsert(it) },
        markSyncedIfUnchanged = { id, sentUpdatedAt -> pupukMasukDao.markSyncedIfUnchanged(id, sentUpdatedAt) },
        updateBuktiFotoUrlOnly = { id, url -> pupukMasukDao.updateBuktiFotoUrlOnly(id, url) },
        idOf = { it.id },
        localPathOf = { it.buktiFotoLokal },
        entityUrlOf = { it.buktiFotoUrl },
        updatedAtOf = { it.updatedAt },
        isSyncedOf = { it.isSynced },
        toRemoteDto = { entity -> entity.toRemoteDto(fallbackUserId) },
        decodeRemoteList = {
            SupabaseProvider.client.postgrest["pupuk_masuk"]
                .select()
                .decodeList<PupukMasukRemoteDto>()
                .map { it.toRemoteRow(::parseTimestamp) }
        }
    )

    private suspend fun syncPupukTerpakai(fallbackUserId: String): TableSyncStats = syncSatuTabel<PupukTerpakaiRemoteDto, PupukTerpakaiEntity>(
        tableName = "pupuk_terpakai",
        getUnsynced = { pupukTerpakaiDao.getUnsynced() },
        getById = { pupukTerpakaiDao.getById(it) },
        upsertRow = { pupukTerpakaiDao.upsert(it) },
        markSyncedIfUnchanged = { id, sentUpdatedAt -> pupukTerpakaiDao.markSyncedIfUnchanged(id, sentUpdatedAt) },
        updateBuktiFotoUrlOnly = { id, url -> pupukTerpakaiDao.updateBuktiFotoUrlOnly(id, url) },
        idOf = { it.id },
        localPathOf = { it.buktiFotoLokal },
        entityUrlOf = { it.buktiFotoUrl },
        updatedAtOf = { it.updatedAt },
        isSyncedOf = { it.isSynced },
        toRemoteDto = { entity -> entity.toRemoteDto(fallbackUserId) },
        decodeRemoteList = {
            SupabaseProvider.client.postgrest["pupuk_terpakai"]
                .select()
                .decodeList<PupukTerpakaiRemoteDto>()
                .map { it.toRemoteRow(::parseTimestamp) }
        }
    )
}
