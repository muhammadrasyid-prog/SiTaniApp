package com.example.sitani

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sitani.data.PupukMasukViewModel
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

private val satuanPupukOptions = listOf("Karung", "Kol", "Kg", "Lainnya")
private const val SATUAN_LAINNYA = "Lainnya"

private data class PupukMasukFormSnapshot(
    val tanggal: String,
    val namaPupuk: String,
    val kuantitas: String,
    val satuan: String,
    val satuanLainnya: String,
    val hargaSatuan: String,
    val supplier: String,
    val keterangan: String,
    val buktiFotoLokal: String?
)

private val PupukMasukFormSnapshotSaver = Saver<PupukMasukFormSnapshot, Any>(
    save = { snapshot ->
        arrayOf(
            snapshot.tanggal,
            snapshot.namaPupuk,
            snapshot.kuantitas,
            snapshot.satuan,
            snapshot.satuanLainnya,
            snapshot.hargaSatuan,
            snapshot.supplier,
            snapshot.keterangan,
            snapshot.buktiFotoLokal ?: ""
        )
    },
    restore = { values ->
        val arr = values as Array<String>
        PupukMasukFormSnapshot(
            tanggal = arr[0],
            namaPupuk = arr[1],
            kuantitas = arr[2],
            satuan = arr[3],
            satuanLainnya = arr[4],
            hargaSatuan = arr[5],
            supplier = arr[6],
            keterangan = arr[7],
            buktiFotoLokal = arr[8].takeIf { it.isNotBlank() }
        )
    }
)

/** Membolehkan angka desimal dengan satu pemisah (titik atau koma). */
private fun sanitizeKuantitas(input: String): String {
    val cleaned = input.filter { it.isDigit() || it == '.' || it == ',' }
    val normalized = cleaned.replace(',', '.')
    val firstDot = normalized.indexOf('.')
    if (firstDot == -1) return normalized
    return normalized.substring(0, firstDot + 1) + normalized.substring(firstDot + 1).replace(".", "")
}

/** Menampilkan kuantitas tanpa desimal yang tidak perlu, mis. 30.0 -> "30". */
private fun formatKuantitas(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
}

@Composable
fun PupukMasukScreen(
    viewModel: PupukMasukViewModel,
    onNavigateToEdit: (String) -> Unit = {}
) {
    val list by viewModel.allPupukMasuk.collectAsState()
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
                    item.namaPupuk.contains(searchQuery, ignoreCase = true) ||
                    (item.supplier?.contains(searchQuery, ignoreCase = true) == true)

            matchesPeriod && matchesSearch
        }
    }

    val jumlahTransaksi = filteredList.size
    val jumlahJenisPupuk = filteredList.map { it.namaPupuk }.distinct().size

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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Pupuk Masuk", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                    "Jumlah Transaksi",
                    "$jumlahTransaksi",
                    "jumlah catatan",
                    TextSecondary,
                    Modifier.weight(1f)
                )
                StatCard(
                    "Jumlah Jenis Pupuk",
                    "$jumlahJenisPupuk",
                    "varian pupuk",
                    TextSecondary,
                    Modifier.weight(1f)
                )
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama pupuk, supplier...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (filteredList.isEmpty()) {
            item {
                EmptyState(
                    title = "Belum ada data pupuk masuk",
                    subtitle = "Tambahkan catatan Pupuk Masuk untuk mulai mencatat."
                )
            }
        } else {
            items(filteredList) { item ->
                RecordCard(
                    title = item.namaPupuk,
                    subtitle = "Tgl: ${item.tanggal}",
                    amount = "${formatKuantitas(item.kuantitas)} ${item.satuan}",
                    amountColor = BrandStrong,
                    infoLeft = item.supplier?.let { "Supplier: $it" },
                    infoRight = item.totalHarga?.let { "Rp ${formatter.format(it)}" },
                    isSynced = item.isSynced,
                    onClick = { onNavigateToEdit(item.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PupukMasukFormScreen(
    itemId: String?,
    viewModel: PupukMasukViewModel,
    onBack: () -> Unit
) {
    val list by viewModel.allPupukMasuk.collectAsState()
    val existingItem = remember(itemId, list) { list.find { it.id == itemId } }
    val namaPupukSuggestions by viewModel.distinctNamaPupuk.collectAsState()

    val defaultTanggal = remember { java.time.LocalDate.now().toString() }
    val emptySnapshot = remember(defaultTanggal) {
        PupukMasukFormSnapshot(defaultTanggal, "", "", "", "", "", "", "", null)
    }

    var tanggal by rememberSaveable { mutableStateOf(defaultTanggal) }
    var namaPupuk by rememberSaveable { mutableStateOf("") }
    var kuantitasStr by rememberSaveable { mutableStateOf("") }
    var satuan by rememberSaveable { mutableStateOf("") }
    var satuanLainnya by rememberSaveable { mutableStateOf("") }
    var hargaSatuanStr by rememberSaveable { mutableStateOf("") }
    var supplier by rememberSaveable { mutableStateOf("") }
    var keterangan by rememberSaveable { mutableStateOf("") }
    var buktiFotoLokal by rememberSaveable { mutableStateOf<String?>(null) }

    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var showDiscardConfirm by rememberSaveable { mutableStateOf(false) }
    var initialForm by rememberSaveable(stateSaver = PupukMasukFormSnapshotSaver) { mutableStateOf(emptySnapshot) }
    var formInitialized by rememberSaveable { mutableStateOf(false) }

    var expandedNamaPupuk by remember { mutableStateOf(false) }
    var expandedSatuan by remember { mutableStateOf(false) }

    val currentForm = PupukMasukFormSnapshot(
        tanggal = tanggal,
        namaPupuk = namaPupuk,
        kuantitas = kuantitasStr,
        satuan = satuan,
        satuanLainnya = satuanLainnya,
        hargaSatuan = hargaSatuanStr,
        supplier = supplier,
        keterangan = keterangan,
        buktiFotoLokal = buktiFotoLokal
    )
    val hasChanges = currentForm != initialForm
    val requestExit: () -> Unit = {
        if (hasChanges) showDiscardConfirm = true else onBack()
    }

    BackHandler { requestExit() }

    LaunchedEffect(existingItem) {
        if (existingItem != null && !formInitialized) {
            val satuanTersimpan = existingItem.satuan
            val satuanTerpilih = if (satuanPupukOptions.contains(satuanTersimpan)) {
                satuanTersimpan
            } else {
                SATUAN_LAINNYA
            }
            val satuanKustom = if (satuanTerpilih == SATUAN_LAINNYA && satuanTersimpan != SATUAN_LAINNYA) {
                satuanTersimpan
            } else {
                ""
            }
            val snapshot = PupukMasukFormSnapshot(
                tanggal = existingItem.tanggal,
                namaPupuk = existingItem.namaPupuk,
                kuantitas = formatKuantitas(existingItem.kuantitas),
                satuan = satuanTerpilih,
                satuanLainnya = satuanKustom,
                hargaSatuan = existingItem.hargaSatuan?.toString() ?: "",
                supplier = existingItem.supplier ?: "",
                keterangan = existingItem.keterangan ?: "",
                buktiFotoLokal = existingItem.buktiFotoLokal
            )
            initialForm = snapshot
            tanggal = snapshot.tanggal
            namaPupuk = snapshot.namaPupuk
            kuantitasStr = snapshot.kuantitas
            satuan = snapshot.satuan
            satuanLainnya = snapshot.satuanLainnya
            hargaSatuanStr = snapshot.hargaSatuan
            supplier = snapshot.supplier
            keterangan = snapshot.keterangan
            buktiFotoLokal = snapshot.buktiFotoLokal
            formInitialized = true
        }
    }

    val kuantitas = kuantitasStr.toDoubleOrNull() ?: 0.0
    val hargaSatuan = hargaSatuanStr.toLongOrNull()
    val satuanEfektif = if (satuan == SATUAN_LAINNYA) satuanLainnya.trim() else satuan
    val totalHarga = if (hargaSatuan != null) (kuantitas * hargaSatuan).toLong() else null
    val formatter = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")) }

    ConfirmDialog(
        showDelete = showDeleteConfirm,
        showDiscard = showDiscardConfirm,
        onDeleteConfirm = {
            showDeleteConfirm = false
            existingItem?.let { viewModel.deletePupukMasuk(it.id) }
            onBack()
        },
        onDeleteDismiss = { showDeleteConfirm = false },
        onDiscardConfirm = {
            showDiscardConfirm = false
            onBack()
        },
        onDiscardDismiss = { showDiscardConfirm = false },
        deleteMessage = "Yakin ingin menghapus data pupuk masuk ini?"
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
                        Text(if (itemId == null) "Tambah Pupuk Masuk" else "Edit Pupuk Masuk", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

            // Nama Pupuk dengan saran dari data yang pernah dicatat
            ExposedDropdownMenuBox(
                expanded = expandedNamaPupuk,
                onExpandedChange = { expandedNamaPupuk = !expandedNamaPupuk },
                modifier = Modifier.fillMaxWidth()
            ) {
                FormField(
                    label = "Nama Pupuk *",
                    value = namaPupuk,
                    onValueChange = { namaPupuk = it },
                    placeholder = "cth. Kotoran sapi",
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                val filteredSuggestions = namaPupukSuggestions.filter { it.contains(namaPupuk, ignoreCase = true) }
                if (filteredSuggestions.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expandedNamaPupuk,
                        onDismissRequest = { expandedNamaPupuk = false }
                    ) {
                        filteredSuggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    namaPupuk = suggestion
                                    expandedNamaPupuk = false
                                }
                            )
                        }
                    }
                }
            }

            FormField(
                label = "Kuantitas *",
                value = kuantitasStr,
                onValueChange = { kuantitasStr = sanitizeKuantitas(it) },
                placeholder = "cth. 30",
                keyboardType = KeyboardType.Decimal
            )

            // Satuan: Karung / Kol / Kg / Lainnya
            ExposedDropdownMenuBox(
                expanded = expandedSatuan,
                onExpandedChange = { expandedSatuan = !expandedSatuan },
                modifier = Modifier.fillMaxWidth()
            ) {
                FormField(
                    label = "Satuan *",
                    value = satuan,
                    onValueChange = {},
                    placeholder = "Pilih satuan",
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedSatuan,
                    onDismissRequest = { expandedSatuan = false }
                ) {
                    satuanPupukOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                satuan = option
                                expandedSatuan = false
                            }
                        )
                    }
                }
            }

            if (satuan == SATUAN_LAINNYA) {
                FormField(
                    label = "Satuan (lainnya) *",
                    value = satuanLainnya,
                    onValueChange = { satuanLainnya = it },
                    placeholder = "cth. Paket"
                )
            }

            MoneyField(
                label = "Harga Satuan (Rp)",
                value = hargaSatuanStr,
                onValueChange = { hargaSatuanStr = it },
                placeholder = "cth. 50.000"
            )

            if (totalHarga != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Harga Otomatis", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            "Rp ${formatter.format(totalHarga)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningStrong
                        )
                    }
                }
            }

            FormField(
                label = "Supplier (Opsional)",
                value = supplier,
                onValueChange = { supplier = it },
                placeholder = "cth. UD Sumber Padi"
            )

            FormField(
                label = "Keterangan (Opsional)",
                value = keterangan,
                onValueChange = { keterangan = it },
                placeholder = "cth. Pembelian bulan ini",
                singleLine = false,
                minLines = 2
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
                        if (tanggal.isBlank() || namaPupuk.isBlank() || kuantitas <= 0 || satuanEfektif.isBlank()) {
                            errorMessage = "Mohon lengkapi Tanggal, Nama Pupuk, Kuantitas, dan Satuan dengan benar."
                        } else {
                            errorMessage = null
                            viewModel.savePupukMasuk(
                                id = existingItem?.id,
                                tanggal = tanggal,
                                namaPupuk = namaPupuk.trim(),
                                kuantitas = kuantitas,
                                satuan = satuanEfektif,
                                hargaSatuan = hargaSatuan,
                                supplier = supplier.ifBlank { null },
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
