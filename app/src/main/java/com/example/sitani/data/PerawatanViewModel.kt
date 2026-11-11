package com.example.sitani.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PerawatanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PerawatanLahanRepository

    init {
        val dao = AppDatabase.getDatabase(application).perawatanLahanDao()
        repository = PerawatanLahanRepository(dao)
    }

    val allPerawatan: StateFlow<List<PerawatanLahanEntity>> = repository.allPerawatan
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun savePerawatan(
        id: String?,
        tanggal: String,
        kategoriBiaya: String,
        deskripsi: String?,
        biaya: Long,
        buktiFotoUrl: String?,
        buktiFotoLokal: String?
    ) {
        viewModelScope.launch {
            repository.save(
                id = id,
                tanggal = tanggal,
                kategoriBiaya = kategoriBiaya,
                deskripsi = deskripsi,
                biaya = biaya,
                buktiFotoUrl = buktiFotoUrl,
                buktiFotoLokal = buktiFotoLokal
            )
        }
    }

    fun deletePerawatan(id: String) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
