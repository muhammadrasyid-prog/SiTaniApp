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
        val fotoTurun: Int
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

class SyncRepository(private val context: Context) {
    private val dao = AppDatabase.getDatabase(context).penjualanPanenDao()

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

    suspend fun syncPenjualan(): SyncResult {
        if (!isNetworkAvailable()) {
            return SyncResult.TidakAdaKoneksi
        }

        val currentUser = SupabaseProvider.client.auth.currentUserOrNull()
        if (currentUser == null) {
            return SyncResult.BelumLogin
        }

        return try {
            val storageBucket = SupabaseProvider.client.storage["bukti"]
            val unsyncedList = dao.getUnsynced()
            var sentCount = 0
            var fotoNaikCount = 0

            // 1. PUSH baris per baris
            for (entity in unsyncedList) {
                var currentUrl = entity.buktiFotoUrl
                val localPath = entity.buktiFotoLokal
                var photoUploaded = false

                if (!localPath.isNullOrBlank()) {
                    val localFile = File(localPath)
                    if (localFile.exists()) {
                        val needsUpload = currentUrl == null || !currentUrl.contains(entity.id)
                        if (needsUpload) {
                            val storagePath = "penjualan_panen/${entity.id}_${System.currentTimeMillis()}.jpg"
                            val uploadSuccess = runCatching {
                                val bytes = localFile.readBytes()
                                storageBucket.upload(storagePath, bytes) {
                                    upsert = true
                                }
                                true
                            }.getOrElse { e ->
                                Log.e("Sync", "Gagal unggah foto untuk ${entity.id}", e)
                                false
                            }

                            if (uploadSuccess) {
                                currentUrl = storagePath
                                fotoNaikCount++
                                photoUploaded = true
                                // Simpan URL ke Room lewat query khusus (tanpa ubah isSynced/updatedAt)
                                dao.updateBuktiFotoUrlOnly(entity.id, storagePath)
                                Log.d("Sync", "id: ${entity.id}, foto diunggah ke $storagePath")
                            } else {
                                Log.e("Sync", "Gagal unggah foto untuk ${entity.id}, lewati upsert baris ini")
                                Log.d("Sync", "id: ${entity.id}, foto tidak diunggah, hasil upsert: gagal, hasil markSynced: false")
                                continue
                            }
                        } else {
                            Log.d("Sync", "id: ${entity.id}, foto tidak diunggah (sudah ada)")
                        }
                    } else {
                        Log.d("Sync", "id: ${entity.id}, foto tidak diunggah (file lokal tidak ada)")
                    }
                } else {
                    Log.d("Sync", "id: ${entity.id}, foto tidak diunggah (tidak ada lokal)")
                }

                // Baca ulang baris terbaru dari Room untuk mendapatkan updated_at dan buktiFotoUrl terkini
                val latestEntity = dao.getById(entity.id) ?: entity
                val dto = PenjualanRemoteDto(
                    id = latestEntity.id,
                    created_by = latestEntity.createdBy.ifBlank { currentUser.id },
                    bukti_foto_url = latestEntity.buktiFotoUrl,
                    created_at = Instant.ofEpochMilli(latestEntity.createdAt).toString(),
                    updated_at = Instant.ofEpochMilli(latestEntity.updatedAt).toString(),
                    deleted_at = latestEntity.deletedAt?.let { Instant.ofEpochMilli(it).toString() },
                    tanggal_jual = latestEntity.tanggalJual,
                    jenis_tanaman = latestEntity.jenisTanaman,
                    volume_kg = latestEntity.volumeKg,
                    harga_per_kg = latestEntity.hargaPerKg.toDouble(),
                    total_harga = latestEntity.totalHarga.toDouble(),
                    nama_pembeli = latestEntity.namaPembeli,
                    keterangan = latestEntity.keterangan
                )

                val upsertSuccess = runCatching {
                    SupabaseProvider.client.postgrest["penjualan_panen"].upsert(dto)
                    true
                }.getOrElse { e ->
                    Log.e("Sync", "Gagal upsert ${entity.id} ke Supabase", e)
                    false
                }

                if (upsertSuccess) {
                    val rowsUpdated = dao.markSyncedIfUnchanged(latestEntity.id, latestEntity.updatedAt)
                    if (rowsUpdated > 0) {
                        sentCount++
                        Log.d("Sync", "id: ${entity.id}, hasil upsert: sukses, hasil markSynced: sukses (1)")
                    } else {
                        Log.d("Sync", "id: ${entity.id}, hasil upsert: sukses, hasil markSynced: dilewati (updated_at berubah saat sync)")
                    }
                } else {
                    Log.d("Sync", "id: ${entity.id}, hasil upsert: gagal, hasil markSynced: false")
                }
            }
            Log.d("Sync", "Total baris sukses terkirim: $sentCount, Foto naik: $fotoNaikCount")

            // 2. PULL
            val remoteList = SupabaseProvider.client.postgrest["penjualan_panen"]
                .select()
                .decodeList<PenjualanRemoteDto>()

            Log.d("Sync", "Jumlah baris diterima dari Supabase: ${remoteList.size}")

            val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: File(context.filesDir, "Pictures").apply { mkdirs() }
            if (!picturesDir.exists()) picturesDir.mkdirs()

            var receivedInsertedOrUpdated = 0
            var fotoTurunCount = 0

            for (remote in remoteList) {
                val local = dao.getById(remote.id)
                val remoteCreatedAt = parseTimestamp(remote.created_at)
                val remoteUpdatedAt = parseTimestamp(remote.updated_at)
                val remoteDeletedAt = remote.deleted_at?.let { parseTimestamp(it) }

                val remoteHargaPerKg = remote.harga_per_kg.roundToLong()
                val remoteTotalHarga = remote.total_harga.roundToLong()

                var resolvedLocalPath = local?.buktiFotoLokal
                val remoteUrl = remote.bukti_foto_url

                if (!remoteUrl.isNullOrBlank()) {
                    val needDownload = resolvedLocalPath == null || !File(resolvedLocalPath).exists() || local?.buktiFotoUrl != remoteUrl
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
                            fotoTurunCount++
                        }
                    }
                }

                if (local == null) {
                    val newEntity = PenjualanPanenEntity(
                        id = remote.id,
                        createdBy = remote.created_by,
                        buktiFotoUrl = remoteUrl,
                        buktiFotoLokal = resolvedLocalPath,
                        isSynced = true,
                        createdAt = remoteCreatedAt,
                        updatedAt = remoteUpdatedAt,
                        deletedAt = remoteDeletedAt,
                        tanggalJual = remote.tanggal_jual,
                        jenisTanaman = remote.jenis_tanaman,
                        volumeKg = remote.volume_kg,
                        hargaPerKg = remoteHargaPerKg,
                        totalHarga = remoteTotalHarga,
                        namaPembeli = remote.nama_pembeli,
                        keterangan = remote.keterangan
                    )
                    dao.upsert(newEntity)
                    receivedInsertedOrUpdated++
                } else {
                    if (local.isSynced) {
                        if (remoteUpdatedAt > local.updatedAt) {
                            val updatedEntity = local.copy(
                                createdBy = remote.created_by,
                                buktiFotoUrl = remoteUrl,
                                buktiFotoLokal = resolvedLocalPath,
                                isSynced = true,
                                updatedAt = remoteUpdatedAt,
                                deletedAt = remoteDeletedAt,
                                tanggalJual = remote.tanggal_jual,
                                jenisTanaman = remote.jenis_tanaman,
                                volumeKg = remote.volume_kg,
                                hargaPerKg = remoteHargaPerKg,
                                totalHarga = remoteTotalHarga,
                                namaPembeli = remote.nama_pembeli,
                                keterangan = remote.keterangan
                            )
                            dao.upsert(updatedEntity)
                            receivedInsertedOrUpdated++
                        }
                    }
                }
            }
            Log.d("Sync", "Jumlah baris dimasukkan atau diperbarui di Room: $receivedInsertedOrUpdated, Foto turun: $fotoTurunCount")

            SyncResult.Berhasil(sentCount, remoteList.size, fotoNaikCount, fotoTurunCount)
        } catch (e: Exception) {
            Log.e("Sync", "Gagal sinkronisasi penjualan_panen", e)
            SyncResult.Gagal(e.localizedMessage ?: e.javaClass.simpleName)
        }
    }
}
