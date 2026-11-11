package com.example.sitani.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KepentinganUmumViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KepentinganUmumRepository

    init {
        val dao = AppDatabase.getDatabase(application).kepentinganUmumDao()
        repository = KepentinganUmumRepository(dao)
    }

    val allKepentingan: StateFlow<List<KepentinganUmumEntity>> = repository.allKepentingan
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveKepentinganUmum(
        id: String?,
        tanggal: String,
        kategoriKegiatan: String,
        deskripsi: String?,
        jumlah: Long,
        buktiFotoUrl: String?,
        buktiFotoLokal: String?
    ) {
        viewModelScope.launch {
            repository.save(
                id = id,
                tanggal = tanggal,
                kategoriKegiatan = kategoriKegiatan,
                deskripsi = deskripsi,
                jumlah = jumlah,
                buktiFotoUrl = buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal
            )
        }
    }

    fun deleteKepentinganUmum(id: String) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
