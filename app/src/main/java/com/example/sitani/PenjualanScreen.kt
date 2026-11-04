package com.example.sitani

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sitani.data.PenjualanPanenEntity
import com.example.sitani.data.PenjualanViewModel
import com.example.sitani.ui.theme.*
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

private data class PenjualanFormSnapshot(
    val tanggalJual: String,
    val jenisTanaman: String,
    val volumeKg: String,
    val hargaPerKg: String,
    val namaPembeli: String,
    val keterangan: String,
    val buktiFotoLokal: String?
)

@Composable
fun PenjualanScreen(
    viewModel: PenjualanViewModel,
    onNavigateToAdd: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {}
) {
    val list by viewModel.allPenjualan.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    var isYearly by remember { mutableStateOf(false) }
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    val formatter = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")) }

    val filteredList = remember(list, searchQuery, isYearly, selectedCalendar) {
        val selYear = selectedCalendar.get(Calendar.YEAR)
        val selMonth = selectedCalendar.get(Calendar.MONTH)

        list.filter { item ->
            val parts = item.tanggalJual.split("-")
            val matchesPeriod = if (parts.size >= 2) {
                val itemYear = parts[0].toIntOrNull() ?: 0
                val itemMonth = parts[1].toIntOrNull()?.minus(1) ?: 0
                if (isYearly) {
                    itemYear == selYear
                } else {
                    itemYear == selYear && itemMonth == selMonth
                }
            } else true

            val matchesSearch = searchQuery.isBlank() ||
                    item.jenisTanaman.contains(searchQuery, ignoreCase = true) ||
                    (item.namaPembeli?.contains(searchQuery, ignoreCase = true) == true) ||
                    item.tanggalJual.contains(searchQuery, ignoreCase = true)

            matchesPeriod && matchesSearch
        }
    }

    val totalPenjualan = filteredList.sumOf { it.totalHarga }
    val totalVolume = filteredList.sumOf { it.volumeKg }
    val jumlahTransaksi = filteredList.size

    val periodLabel = remember(isYearly, selectedCalendar) {
        val y = selectedCalendar.get(Calendar.YEAR)
        if (isYearly) {
            "$y"
        } else {
            val months = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
            val m = selectedCalendar.get(Calendar.MONTH)
            "${months.getOrElse(m) { "" }} $y"
        }
    }

    Scaffold(
        topBar = { SitaniTopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = BrandPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Penjualan")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Penjualan Hasil Panen", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val cal = selectedCalendar.clone() as Calendar
                                if (isYearly) cal.add(Calendar.YEAR, -1) else cal.add(Calendar.MONTH, -1)
                                selectedCalendar = cal
                            }) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Sebelumnya")
                            }
                            Text(periodLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = {
                                val cal = selectedCalendar.clone() as Calendar
                                if (isYearly) cal.add(Calendar.YEAR, 1) else cal.add(Calendar.MONTH, 1)
                                selectedCalendar = cal
                            }) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Berikutnya")
                            }
                        }
                        Row {
                            TextButton(onClick = { isYearly = false }) {
                                Text("Bulan", color = if (!isYearly) BrandPrimary else TextSecondary, fontWeight = if (!isYearly) FontWeight.Bold else FontWeight.Normal)
                            }
                            TextButton(onClick = { isYearly = true }) {
                                Text("Tahun", color = if (isYearly) BrandPrimary else TextSecondary, fontWeight = if (isYearly) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        "Total Penjualan",
                        "Rp ${formatter.format(totalPenjualan)}",
                        "$jumlahTransaksi transaksi",
                        BrandPrimary,
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "Total Volume",
                        "${formatter.format(totalVolume)} kg",
                        "akumulasi panen",
                        BrandStrong,
                        Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari komoditas, petani, tanggal...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (filteredList.isEmpty()) {
                item {
                    AppCard {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Belum ada data penjualan", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Tambahkan transaksi panen untuk mulai mencatat.",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredList) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToEdit(item.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.jenisTanaman, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (item.isSynced) {
                                        StatusChip("Tersinkron", SuccessBg, BrandStrong)
                                    } else {
                                        StatusChip("Belum sinkron", WarningBg, WarningText)
                                    }
                                    Icon(
                                        Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${formatter.format(item.volumeKg)} kg × Rp ${formatter.format(item.hargaPerKg)}", fontSize = 13.sp, color = TextSecondary)
                                Text("Rp ${formatter.format(item.totalHarga)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandPrimary)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tgl: ${item.tanggalJual}", fontSize = 12.sp, color = TextSecondary)
                                if (!item.namaPembeli.isNullOrBlank()) {
                                    Text("Pembeli: ${item.namaPembeli}", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenjualanFormScreen(
    itemId: String?,
    viewModel: PenjualanViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val list by viewModel.allPenjualan.collectAsState()
    val existingItem = remember(itemId, list) { list.find { it.id == itemId } }

    val defaultTanggal = remember { java.time.LocalDate.now().toString() }
    val emptySnapshot = remember(defaultTanggal) {
        PenjualanFormSnapshot(defaultTanggal, "", "", "", "", "", null)
    }

    var tanggalJual by remember { mutableStateOf(defaultTanggal) }
    var jenisTanaman by remember { mutableStateOf("") }
    var volumeKgStr by remember { mutableStateOf("") }
    var hargaPerKgStr by remember { mutableStateOf("") }
    var namaPembeli by remember { mutableStateOf("") }
    var keterangan by remember { mutableStateOf("") }
    var buktiFotoLokal by remember { mutableStateOf<String?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }
    var initialForm by remember { mutableStateOf(emptySnapshot) }

    // Dropdown suggestions for Komoditas
    val commonKomoditas = listOf("Padi", "Jagung", "Kedelai", "Cabai", "Bawang Merah", "Tomat", "Kacang Tanah")
    var expandedKomoditas by remember { mutableStateOf(false) }

    val currentForm = PenjualanFormSnapshot(
        tanggalJual = tanggalJual,
        jenisTanaman = jenisTanaman,
        volumeKg = volumeKgStr,
        hargaPerKg = hargaPerKgStr,
        namaPembeli = namaPembeli,
        keterangan = keterangan,
        buktiFotoLokal = buktiFotoLokal
    )
    val hasChanges = currentForm != initialForm
    val requestExit: () -> Unit = {
        if (hasChanges) showDiscardConfirm = true else onBack()
    }

    BackHandler { requestExit() }

    LaunchedEffect(existingItem) {
        if (existingItem != null) {
            val snapshot = PenjualanFormSnapshot(
                tanggalJual = existingItem.tanggalJual,
                jenisTanaman = existingItem.jenisTanaman,
                volumeKg = existingItem.volumeKg.toString(),
                hargaPerKg = existingItem.hargaPerKg.toString(),
                namaPembeli = existingItem.namaPembeli ?: "",
                keterangan = existingItem.keterangan ?: "",
                buktiFotoLokal = existingItem.buktiFotoLokal
            )
            initialForm = snapshot
            tanggalJual = snapshot.tanggalJual
            jenisTanaman = snapshot.jenisTanaman
            volumeKgStr = snapshot.volumeKg
            hargaPerKgStr = snapshot.hargaPerKg
            namaPembeli = snapshot.namaPembeli
            keterangan = snapshot.keterangan
            buktiFotoLokal = snapshot.buktiFotoLokal
        }
    }

    val volume = volumeKgStr.toDoubleOrNull() ?: 0.0
    val harga = hargaPerKgStr.toLongOrNull() ?: 0L
    val total = (volume * harga).toLong()
    val formatter = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Data") },
            text = { Text("Yakin ingin menghapus data penjualan panen ini?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    existingItem?.let { viewModel.deletePenjualan(it.id) }
                    onBack()
                }) {
                    Text("Hapus", color = DangerStrong)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text("Buang perubahan?") },
            text = { Text("Perubahan yang belum disimpan akan hilang.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardConfirm = false
                    onBack()
                }) {
                    Text("Buang", color = DangerStrong)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(color = BrandDark) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(onClick = requestExit) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                        }
                        Text(if (itemId == null) "Tambah Penjualan Panen" else "Edit Penjualan Panen", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    if (existingItem != null) {
                        TextButton(onClick = { showDeleteConfirm = true }) {
                            Text("Hapus", color = DangerBg)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tanggal DateField
            DateField(
                label = "Tanggal Jual",
                dateString = tanggalJual,
                onDateSelected = { tanggalJual = it }
            )

            // Komoditas Dropdown / Editable
            ExposedDropdownMenuBox(
                expanded = expandedKomoditas,
                onExpandedChange = { expandedKomoditas = !expandedKomoditas },
                modifier = Modifier.fillMaxWidth()
            ) {
                FormField(
                    label = "Komoditas / Jenis Tanaman *",
                    value = jenisTanaman,
                    onValueChange = { jenisTanaman = it },
                    placeholder = "cth. Padi, Jagung",
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                val filteredSuggestions = commonKomoditas.filter { it.contains(jenisTanaman, ignoreCase = true) }
                if (filteredSuggestions.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expandedKomoditas,
                        onDismissRequest = { expandedKomoditas = false }
                    ) {
                        filteredSuggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    jenisTanaman = suggestion
                                    expandedKomoditas = false
                                }
                            )
                        }
                    }
                }
            }

            MoneyField(
                label = "Volume (kg) *",
                value = volumeKgStr,
                onValueChange = { volumeKgStr = it },
                placeholder = "cth. 1.500"
            )

            MoneyField(
                label = "Harga per kg (Rp) *",
                value = hargaPerKgStr,
                onValueChange = { hargaPerKgStr = it },
                placeholder = "cth. 5.000"
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = SuccessSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Harga Otomatis", fontSize = 12.sp, color = TextSecondary)
                    Text("Rp ${formatter.format(total)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                }
            }

            FormField(
                label = "Pembeli",
                value = namaPembeli,
                onValueChange = { namaPembeli = it },
                placeholder = "cth. Kelompok Tani Makmur"
            )

            FormField(
                label = "Keterangan (Opsional)",
                value = keterangan,
                onValueChange = { keterangan = it },
                placeholder = "cth. Panen musim tanam pertama",
                singleLine = false,
                minLines = 2
            )

            // PhotoPickerField reusable
            PhotoPickerField(
                photoPath = buktiFotoLokal,
                onPhotoChanged = { buktiFotoLokal = it },
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = requestExit,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceMuted, contentColor = TextPrimary),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Batal")
                }
                Button(
                    onClick = {
                        if (tanggalJual.isBlank() || jenisTanaman.isBlank() || volume <= 0 || harga <= 0) {
                            errorMessage = "Mohon lengkapi Tanggal, Komoditas, Volume, dan Harga dengan benar."
                        } else {
                            errorMessage = null
                            viewModel.savePenjualan(
                                id = existingItem?.id,
                                tanggalJual = tanggalJual,
                                jenisTanaman = jenisTanaman.trim(),
                                volumeKg = volume,
                                hargaPerKg = harga,
                                namaPembeli = namaPembeli.ifBlank { null },
                                keterangan = keterangan.ifBlank { null },
                                buktiFotoUrl = existingItem?.buktiFotoUrl,
                                buktiFotoLokal = buktiFotoLokal
                            )
                            onBack()
                        }
                    },
                    enabled = hasChanges,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Simpan")
                }
            }
        }
    }
}
