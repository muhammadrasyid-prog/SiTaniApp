package com.example.sitani.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SyncViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val syncRepository = SyncRepository(application)

    val unsyncedCount: StateFlow<Int> = combine(
        db.penjualanPanenDao().getUnsyncedCount(),
        db.perawatanLahanDao().getUnsyncedCount(),
        db.kepentinganUmumDao().getUnsyncedCount(),
        db.pupukMasukDao().getUnsyncedCount(),
        db.pupukTerpakaiDao().getUnsyncedCount()
    ) { penjualan, perawatan, kepentingan, masuk, terpakai ->
        penjualan + perawatan + kepentingan + masuk + terpakai
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    private val _isSyncing = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _syncMessage = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage

    init {
        syncAll(isAutomatic = true)
    }

    fun syncAll(isAutomatic: Boolean = false) {
        viewModelScope.launch {
            if (!isAutomatic) {
                _isSyncing.value = true
                _syncMessage.value = null
            }

            val result = syncRepository.syncAll()
            val msg = when (result) {
                is SyncResult.Berhasil -> {
                    val dasar = "Terkirim ${result.jumlahTerkirim}, diterima ${result.jumlahDiterima}, foto naik ${result.fotoNaik}, foto turun ${result.fotoTurun}"
                    if (result.galatPerTabel.isEmpty()) {
                        dasar
                    } else {
                        val galat = result.galatPerTabel.entries.joinToString("; ") { "${it.key}: ${it.value}" }
                        "$dasar. Sebagian gagal ($galat)"
                    }
                }
                is SyncResult.TidakAdaKoneksi -> if (isAutomatic) null else "Gagal: Tidak ada koneksi internet"
                is SyncResult.BelumLogin -> if (isAutomatic) null else "Gagal: Pengguna belum login"
                is SyncResult.Gagal -> if (isAutomatic) null else "Gagal: ${result.pesan}"
            }

            if (!isAutomatic || msg != null) {
                _syncMessage.value = msg
            }

            if (!isAutomatic) {
                _isSyncing.value = false
            }
        }
    }
}
