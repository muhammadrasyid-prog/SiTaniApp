package com.example.sitani

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sitani.ui.components.RecordCard

@Composable
fun MenuScreen(
    onNavigateToKepentinganUmum: () -> Unit = {}
) {
    val context = LocalContext.current

    val comingSoon: (String) -> Unit = { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = { SitaniTopBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Menu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            item {
                RecordCard(
                    title = "Kepentingan Umum",
                    subtitle = "Catat pengeluaran untuk kepentingan umum",
                    showSyncChip = false,
                    onClick = onNavigateToKepentinganUmum
                )
            }

            item {
                RecordCard(
                    title = "Export Data",
                    subtitle = "Unduh rekap keuangan dan panen",
                    showSyncChip = false,
                    showChevron = false,
                    onClick = { comingSoon("Segera hadir") }
                )
            }

            item {
                RecordCard(
                    title = "Sinkronisasi",
                    subtitle = "Kirim dan tarik data ke server",
                    showSyncChip = false,
                    showChevron = false,
                    onClick = { comingSoon("Segera hadir") }
                )
            }
        }
    }
}
