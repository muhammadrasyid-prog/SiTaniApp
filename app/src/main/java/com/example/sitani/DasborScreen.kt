package com.example.sitani

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sitani.ui.theme.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DasborScreen(
    onNavigate: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {

    Column(modifier = Modifier.fillMaxSize()) {
        SitaniTopBar(onLogout = onLogout)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Dasbor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        "Total Pemasukan",
                        "Rp 57.160.000",
                        "dari penjualan panen",
                        BrandPrimary,
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "Total Pengeluaran",
                        "Rp 21.400.000",
                        "perawatan + umum",
                        WarningStrong,
                        Modifier.weight(1f)
                    )
                }
            }
            item {
                AppCard {
                    Text("Saldo Bersih", fontSize = 12.sp, color = TextSecondary)
                    Text("Rp 35.760.000", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Text("Pintasan", fontWeight = FontWeight.SemiBold)
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShortcutCard("Penjualan", { onNavigate("panen") }, Modifier.weight(1f))
                        ShortcutCard(
                            "Perawatan Lahan",
                            { onNavigate("lahan") },
                            Modifier.weight(1f)
                        )
                        ShortcutCard(
                            "Kepentingan Umum",
                            { onNavigate("menu") },
                            Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShortcutCard("Pupuk Masuk", { onNavigate("pupuk") }, Modifier.weight(1f))
                        ShortcutCard("Pupuk Terpakai", { onNavigate("pupuk") }, Modifier.weight(1f))
                        ShortcutCard("Export Data", { onNavigate("menu") }, Modifier.weight(1f))
                    }
                }
            }

            item {
                AppCard {
                    Text("Stok Pupuk · jumlah tersedia", fontWeight = FontWeight.SemiBold)
                    StockRow("Urea", "2.400 kg", TextSecondary)
                    StockRow("NPK Phonska", "1.800 kg", TextSecondary)
                    StockRow("SP-36", "950 kg", TextSecondary)
                }
            }

            item {
                AppCard {
                    Text("Transaksi Terbaru", fontWeight = FontWeight.SemiBold)
                    RecentRow(
                        "Penjualan · Padi",
                        "05 Jan 2026",
                        "+ Rp 12.500.000",
                        BrandPrimary,
                        true
                    )
                    RecentRow(
                        "Perawatan · Honor pengurus",
                        "08 Jan 2026",
                        "- Rp 1.500.000",
                        WarningStrong,
                        true
                    )
                    RecentRow(
                        "Kepentingan umum · Perbaikan jalan",
                        "10 Jan 2026",
                        "- Rp 3.000.000",
                        WarningStrong,
                        false
                    )
                }
            }
        }
    }
}

@Composable
fun ShortcutCard(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecentRow(title: String, date: String, amount: String, amountColor: Color, synced: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(date, fontSize = 11.sp, color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(amount, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = amountColor)
            if (synced) StatusChip("Tersinkron", SuccessBg, BrandStrong)
            else StatusChip("Belum sinkron", WarningBg, WarningText)
        }
    }
}

@Composable
fun AppCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) { content() }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    note: String,
    noteColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(note, fontSize = 10.sp, color = noteColor)
        }
    }
}

@Composable
fun StatusChip(text: String, bg: Color, fg: Color) {
    Text(
        text,
        color = fg,
        fontSize = 11.sp,
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 2.dp)
    )
}

@Composable
fun StockRow(name: String, amount: String, amountColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(name, fontSize = 13.sp, color = TextBody)
        Text(amount, fontSize = 13.sp, color = amountColor)
    }
}