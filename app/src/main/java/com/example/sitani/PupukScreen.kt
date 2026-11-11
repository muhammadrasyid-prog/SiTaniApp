package com.example.sitani

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.sitani.data.PupukMasukViewModel
import com.example.sitani.ui.theme.BrandPrimary
import com.example.sitani.ui.theme.TextSecondary

@Composable
fun PupukScreen(
    viewModel: PupukMasukViewModel,
    onNavigateToAdd: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        topBar = { SitaniTopBar() },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onNavigateToAdd,
                    containerColor = BrandPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Pupuk Masuk")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Masuk") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Terpakai") }
                )
            }

            when (selectedTab) {
                0 -> PupukMasukScreen(
                    viewModel = viewModel,
                    onNavigateToEdit = onNavigateToEdit
                )
                else -> SegeraHadir()
            }
        }
    }
}

@Composable
private fun SegeraHadir() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Segera hadir",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
    }
}
