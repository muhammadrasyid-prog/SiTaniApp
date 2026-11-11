package com.example.sitani.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PupukMasukViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PupukMasukRepository

    init {
        val dao = AppDatabase.getDatabase(application).pupukMasukDao()
        repository = PupukMasukRepository(dao)
    }

    val allPupukMasuk: StateFlow<List<PupukMasukEntity>> = repository.allPupukMasuk
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val distinctNamaPupuk: StateFlow<List<String>> = repository.distinctNamaPupuk
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun savePupukMasuk(
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
        viewModelScope.launch {
            repository.save(
                id = id,
                tanggal = tanggal,
                namaPupuk = namaPupuk,
                kuantitas = kuantitas,
                satuan = satuan,
                hargaSatuan = hargaSatuan,
                supplier = supplier,
                keterangan = keterangan,
                buktiFotoUrl = buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal
            )
        }
    }

    fun deletePupukMasuk(id: String) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
