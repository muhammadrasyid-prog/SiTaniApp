package com.example.sitani

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sitani.data.PerawatanViewModel
import com.example.sitani.ui.components.ConfirmDialog
import com.example.sitani.ui.components.DateField
import com.example.sitani.ui.components.EmptyState
import com.example.sitani.ui.components.FormField
import com.example.sitani.ui.components.MoneyField
import com.example.sitani.ui.components.PhotoPickerField
import com.example.sitani.ui.components.RecordCard
import com.example.sitani.ui.theme.*
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

private data class PerawatanFormSnapshot(
    val tanggal: String,
    val kategoriBiaya: String,
    val deskripsi: String,
    val biaya: String,
    val buktiFotoLokal: String?
)

private val PerawatanFormSnapshotSaver = Saver<PerawatanFormSnapshot, Any>(
    save = { snapshot ->
        arrayOf(
            snapshot.tanggal,
            snapshot.kategoriBiaya,
            snapshot.deskripsi,
            snapshot.biaya,
            snapshot.buktiFotoLokal ?: ""
        )
    },
    restore = { values ->
        val arr = values as Array<String>
        PerawatanFormSnapshot(
            tanggal = arr[0],
            kategoriBiaya = arr[1],
            deskripsi = arr[2],
            biaya = arr[3],
            buktiFotoLokal = arr[4].takeIf { it.isNotBlank() }
        )
    }
)

@Composable
fun PerawatanScreen(
    viewModel: PerawatanViewModel,
    onNavigateToAdd: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {}
) {
    val list by viewModel.allPerawatan.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    var isYearly by remember { mutableStateOf(false) }
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    val formatter = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")) }

    val filteredList = remember(list, searchQuery, isYearly, selectedCalendar) {
        val selYear = selectedCalendar.get(Calendar.YEAR)
        val selMonth = selectedCalendar.get(Calendar.MONTH)

        list.filter { item ->
            val parts = item.tanggal.split("-")
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
                    item.kategoriBiaya.contains(searchQuery, ignoreCase = true) ||
                    (item.deskripsi?.contains(searchQuery, ignoreCase = true) == true)

            matchesPeriod && matchesSearch
        }
    }

    val totalBiaya = filteredList.sumOf { it.biaya }
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
                Icon(Icons.Default.Add, contentDescription = "Tambah Perawatan")
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
                Text("Perawatan Lahan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                        "Total Biaya",
                        "Rp ${formatter.format(totalBiaya)}",
                        "$jumlahTransaksi transaksi",
                        WarningStrong,
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "Jumlah Transaksi",
                        "$jumlahTransaksi",
                        "catatan perawatan",
                        TextSecondary,
                        Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari kategori, deskripsi...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (filteredList.isEmpty()) {
                item {
                    EmptyState(
                        title = "Belum ada data perawatan lahan",
                        subtitle = "Tambahkan catatan perawatan lahan untuk mulai mencatat."
                    )
                }
            } else {
                items(filteredList) { item ->
                    RecordCard(
                        title = item.kategoriBiaya,
                        subtitle = "Tgl: ${item.tanggal}",
                        amount = "Rp ${formatter.format(item.biaya)}",
                        amountColor = WarningStrong,
                        infoLeft = item.deskripsi,
                        isSynced = item.isSynced,
                        onClick = { onNavigateToEdit(item.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerawatanFormScreen(
    itemId: String?,
    viewModel: PerawatanViewModel,
    onBack: () -> Unit
) {
    val list by viewModel.allPerawatan.collectAsState()
    val existingItem = remember(itemId, list) { list.find { it.id == itemId } }

    val defaultTanggal = remember { java.time.LocalDate.now().toString() }
    val emptySnapshot = remember(defaultTanggal) {
        PerawatanFormSnapshot(defaultTanggal, "", "", "", null)
    }

    var tanggal by rememberSaveable { mutableStateOf(defaultTanggal) }
    var kategoriBiaya by rememberSaveable { mutableStateOf("") }
    var deskripsi by rememberSaveable { mutableStateOf("") }
    var biayaStr by rememberSaveable { mutableStateOf("") }
    var buktiFotoLokal by rememberSaveable { mutableStateOf<String?>(null) }

    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var showDiscardConfirm by rememberSaveable { mutableStateOf(false) }
    var initialForm by rememberSaveable(stateSaver = PerawatanFormSnapshotSaver) { mutableStateOf(emptySnapshot) }
    var formInitialized by rememberSaveable { mutableStateOf(false) }

    val currentForm = PerawatanFormSnapshot(
        tanggal = tanggal,
        kategoriBiaya = kategoriBiaya,
        deskripsi = deskripsi,
        biaya = biayaStr,
        buktiFotoLokal = buktiFotoLokal
    )
    val hasChanges = currentForm != initialForm
    val requestExit: () -> Unit = {
        if (hasChanges) showDiscardConfirm = true else onBack()
    }

    BackHandler { requestExit() }

    LaunchedEffect(existingItem) {
        if (existingItem != null && !formInitialized) {
            val snapshot = PerawatanFormSnapshot(
                tanggal = existingItem.tanggal,
                kategoriBiaya = existingItem.kategoriBiaya,
                deskripsi = existingItem.deskripsi ?: "",
                biaya = existingItem.biaya.toString(),
                buktiFotoLokal = existingItem.buktiFotoLokal
            )
            initialForm = snapshot
            tanggal = snapshot.tanggal
            kategoriBiaya = snapshot.kategoriBiaya
            deskripsi = snapshot.deskripsi
            biayaStr = snapshot.biaya
            buktiFotoLokal = snapshot.buktiFotoLokal
            formInitialized = true
        }
    }

    val biaya = biayaStr.toLongOrNull() ?: 0L

    ConfirmDialog(
        showDelete = showDeleteConfirm,
        showDiscard = showDiscardConfirm,
        onDeleteConfirm = {
            showDeleteConfirm = false
            existingItem?.let { viewModel.deletePerawatan(it.id) }
            onBack()
        },
        onDeleteDismiss = { showDeleteConfirm = false },
        onDiscardConfirm = {
            showDiscardConfirm = false
            onBack()
        },
        onDiscardDismiss = { showDiscardConfirm = false },
        deleteMessage = "Yakin ingin menghapus data perawatan lahan ini?"
    )

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
                        Text(if (itemId == null) "Tambah Perawatan Lahan" else "Edit Perawatan Lahan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            DateField(
                label = "Tanggal",
                dateString = tanggal,
                onDateSelected = { tanggal = it }
            )

            FormField(
                label = "Kategori Biaya *",
                value = kategoriBiaya,
                onValueChange = { kategoriBiaya = it },
                placeholder = "cth. honor pengurus lahan"
            )

            FormField(
                label = "Deskripsi (Opsional)",
                value = deskripsi,
                onValueChange = { deskripsi = it },
                placeholder = "cth. perbaikan saluran irigasi",
                singleLine = false,
                minLines = 2
            )

            MoneyField(
                label = "Biaya (Rp) *",
                value = biayaStr,
                onValueChange = { biayaStr = it },
                placeholder = "cth. 500.000"
            )

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
                        if (tanggal.isBlank() || kategoriBiaya.isBlank() || biaya <= 0) {
                            errorMessage = "Mohon lengkapi Tanggal, Kategori Biaya, dan Biaya dengan benar."
                        } else {
                            errorMessage = null
                            viewModel.savePerawatan(
                                id = existingItem?.id,
                                tanggal = tanggal,
                                kategoriBiaya = kategoriBiaya.trim(),
                                deskripsi = deskripsi.ifBlank { null },
                                biaya = biaya,
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
