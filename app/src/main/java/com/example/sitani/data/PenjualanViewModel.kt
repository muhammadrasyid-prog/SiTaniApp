package com.example.sitani.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PenjualanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PenjualanRepository

    init {
        val dao = AppDatabase.getDatabase(application).penjualanPanenDao()
        repository = PenjualanRepository(dao)
    }

    val allPenjualan: StateFlow<List<PenjualanPanenEntity>> = repository.allPenjualan
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun savePenjualan(
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
        viewModelScope.launch {
            repository.save(
                id = id,
                tanggalJual = tanggalJual,
                jenisTanaman = jenisTanaman,
                volumeKg = volumeKg,
                hargaPerKg = hargaPerKg,
                namaPembeli = namaPembeli,
                keterangan = keterangan,
                buktiFotoUrl = buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal
            )
        }
    }

    fun deletePenjualan(id: String) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
