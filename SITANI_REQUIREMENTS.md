# SITANI Mobile: Kebutuhan Sistem dan Konteks Proyek

Dokumen ini ditujukan untuk AI coding agent (opencode) dan anggota tim sebagai sumber konteks proyek. Baca seluruhnya sebelum mengubah kode. Bagian bertanda **[Keputusan]** adalah keputusan tim yang tidak boleh diubah tanpa konfirmasi. Bagian bertanda **[Saran]** adalah rekomendasi yang boleh disesuaikan. Bagian **Pertanyaan terbuka** belum diputuskan: jangan berasumsi, tanyakan dulu.

## 1. Ringkasan proyek

- **Nama:** SITANI (Sistem Informasi Pengelolaan Lahan Pertanian), versi Android.
- **Konteks:** tugas mata kuliah PPPBD (pemrograman perangkat bergerak), Kelompok 4. Konsep dan kliennya sama dengan proyek web SITANI di mata kuliah PAD, tetapi database dan akun pengguna mobile terpisah dari versi web.
- **Klien:** Yayasan Arrofi'i. Project owner: Bapak Umar Taufiq.
- **Latar belakang:** yayasan memiliki dan mengelola lahan pertanian, dan hasilnya dipakai sebagai dana umum bagi masyarakat sekitar. Pengelolaannya mencakup penjualan hasil panen, perawatan lahan, dan penggunaan dana umum, sehingga semuanya harus dicatat terstruktur, mudah direkap, dan dapat dipertanggungjawabkan secara transparan.
- **Tujuan:** aplikasi Android pencatatan lahan pertanian yang terstruktur, bisa dipakai **offline** di lapangan (input data dan foto bukti), dan tersinkronisasi ke database cloud (Supabase) lewat REST API.
- **Pengguna:** pengurus yayasan (lebih dari satu orang, masing-masing dengan HP sendiri), semuanya berstatus setara.

## 2. Ruang lingkup

### Fitur (dari proposal)

| No | Fitur | Deskripsi | Prioritas |
|---|---|---|---|
| 1 | Login Admin | Autentikasi pengguna untuk masuk ke aplikasi | Wajib |
| 2 | Pencatatan Dana Penjualan | Mencatat volume hasil panen, harga dinamis, jenis tanaman, dan tanggal tanam | Wajib |
| 3 | Pengeluaran Perawatan Lahan | Mencatat biaya operasional lahan dan honorarium tenaga pengurus lahan | Wajib |
| 4 | Pengeluaran Kepentingan Umum | Mencatat pengeluaran sosial (dukungan sekolah, fasilitas jalan) | Wajib |
| 5 | Upload Evidence Foto dan Pupuk | Mengunggah foto nota/kegiatan dan mencatat stok pupuk masuk | Wajib |
| 6 | Penyimpanan Data Lokal | Data tersimpan di HP dan tetap bisa dipakai tanpa internet | Wajib |
| 7 | Sinkronisasi ke Server | Data dikirim ke server saat terhubung internet | Wajib |
| 8 | Export Rekap (Excel/CSV) | Mengunduh laporan keuangan/panen per beberapa bulan atau tahun | Opsional |
| 9 | Status Sinkronisasi Data | Tiap data menampilkan status sudah/belum tersinkron | Opsional |

### Di luar lingkup (jangan dikerjakan)

- Manajemen user dan role di dalam aplikasi. Semua user setara, tidak ada superadmin. **[Keputusan]**
- Layar registrasi dan "Lupa kata sandi". Akun dibuat manual lewat dashboard Supabase.
- Kartu "Kondisi Lahan" dari desain awal (tidak punya data pendukung di rancangan). **[Keputusan]**
- Fitur khusus web: halaman landing/galeri publik, `tampil_di_landing`, banyak foto per data.
- Pemisahan data per pengguna. Data milik yayasan dan dipakai bersama.

## 3. Aktor, use case, dan alur

**Aktor tunggal:** Admin (Pengurus), yang memegang kendali penuh atas pencatatan aktivitas di lapangan.

**Use case:**

- Login
- Kelola Data Penjualan Hasil Panen
- Kelola Data Perawatan Lahan
- Kelola Data Kepentingan Umum
- Kelola Data Pupuk Masuk
- Kelola Data Pupuk Terpakai
- Upload Bukti Foto (Evidence)
- Export Laporan

Relasi: semua "Kelola Data ..." `<<include>>` Login dan Upload Bukti Foto. "Export Laporan" `<<extend>>` (opsional).

**Alur pengguna:**

1. Login, lalu Dasbor sebagai pintu masuk ke empat modul: Penjualan Panen, Perawatan Lahan, Pupuk (tab Pupuk Masuk dan Pupuk Terpakai), dan Kepentingan Umum (lewat Menu).
2. Setiap modul bermuara ke langkah yang sama: pengurus mengisi form dan mengambil foto bukti.
3. Data langsung disimpan ke database lokal (Room).
4. Jika ada internet, data dikirim ke Supabase dan berstatus tersinkron. Jika tidak, berstatus belum sinkron dan dikirim saat aplikasi dibuka kembali dalam kondisi online.
5. Pengurus kembali ke daftar data beserta status sinkronnya. Export rekap CSV bersifat opsional.

## 4. Teknologi dan lingkungan

| Komponen | Pilihan |
|---|---|
| Bahasa | Kotlin (project ditargetkan ke Kotlin 2.4.0; sebelumnya 2.2.0 menyebabkan error metadata saat memakai library Supabase) |
| UI | Jetpack Compose + Material 3 (tema terang saja, dynamic color dimatikan) |
| Database lokal | Room (SQLite) |
| Backend | Supabase: PostgreSQL, Storage, Auth, REST API |
| Klien Supabase | supabase-kt (BOM 3.x; modul `auth-kt`, nanti `postgrest-kt` dan `storage-kt`) + Ktor client engine Android (versi Ktor harus sama dengan yang ditarik supabase-kt) |
| Min SDK | 26 (syarat supabase-kt) |
| Package | `com.example.sitani` |
| Nama tema Compose | `SiTaniTheme` |
| Font | Plus Jakarta Sans (file di `res/font`) |
| IDE | Android Studio; uji coba lewat HP fisik via USB (tanpa emulator) |

**Catatan lingkungan:** PC pengembang berspesifikasi rendah (hardisk HDD). Hindari menambah dependency berat tanpa alasan, dan jaga build tetap ringan.

**Struktur file yang sudah ada** (semua di package `com.example.sitani` kecuali tema):

- `MainActivity.kt`: memanggil `SiTaniTheme { AppRoot() }`
- `AppRoot.kt`: memilih layar berdasarkan `SessionStatus` (Initializing, Authenticated, lainnya)
- `AuthRepository.kt`: `SupabaseProvider` (client) dan `AuthRepository` (login, logout, sessionStatus)
- `LoginScreen.kt`: layar Login
- `MainScreen.kt`: Scaffold, bottom navigation 5 tab, NavHost (baru Dasbor yang berisi layar asli, tab lain masih placeholder)
- `DasborScreen.kt`: Dasbor dengan data dummy, beserta komponen `AppCard`, `StatCard`, `StatusChip`, `StockRow`, `ShortcutCard`, `RecentRow`
- `SitaniTopBar.kt`: app bar (logo, nama, avatar dengan dropdown Profil dan Keluar)
- `ui/theme/Color.kt`, `Theme.kt`, `Type.kt`

## 5. Autentikasi

- Login memakai **Supabase Auth (email + password)**. Tidak ada registrasi publik; pendaftaran mandiri dimatikan di Supabase. **[Keputusan]**
- Semua akun berhak sama. Akun dibuat lewat dashboard Supabase.
- Login pertama kali butuh internet. Setelah sesi tersimpan di HP, aplikasi tetap bisa dipakai offline.
- Navigasi Login/Utama mengikuti status sesi, bukan `navigate` manual.
- Kunci API: hanya **Publishable (anon) key** yang boleh ada di aplikasi. **Dilarang** memakai secret/service_role key. Jangan commit URL dan key ke repositori publik; letakkan di `local.properties` atau konfigurasi yang di-ignore.
- Avatar di top bar memakai huruf awal email akun yang login (belum diimplementasi; saat ini masih teks tetap "UT").
- **Perlu diuji:** perilaku saat offline dengan token yang kedaluwarsa. Pengguna tidak boleh terlempar ke layar Login hanya karena tidak ada internet.

## 6. Model data

Database mobile terdiri dari tabel `users` (milik Supabase Auth, `auth.users`, tidak dibuat sendiri) dan lima tabel transaksi di bawah. Nama tabel dan kolom sama di Room dan Supabase kecuali kolom yang ditandai "hanya Room".

### Kolom umum semua tabel transaksi

| Kolom | Tipe | Keterangan |
|---|---|---|
| `id` | uuid, PK | Dibuat di HP (UUID acak), bukan auto-increment, agar tidak bentrok saat sinkron dari banyak HP |
| `created_by` | uuid, not null, FK ke `users.id` | Akun yang menginput. Di Room disimpan sebagai string biasa tanpa foreign key |
| `bukti_foto_url` | varchar(500), null | URL file di Supabase Storage, kosong sebelum upload |
| `bukti_foto_lokal` | varchar(500), null | Path file foto di HP. **Hanya Room** |
| `is_synced` | boolean, not null, default false | **Hanya Room** |
| `created_at`, `updated_at` | timestamptz, not null | `updated_at` diperbarui setiap edit (dipakai untuk konflik sinkron) |
| `deleted_at` | timestamptz, null | Soft delete, supaya penghapusan ikut tersinkron ke HP lain |

### Kolom khusus per tabel

**penjualan_panen**: `tanggal_jual` date not null; `jenis_tanaman` varchar(255) not null; `volume_kg` numeric(10,2) not null; `harga_per_kg` numeric(15,2) not null; `total_harga` numeric(15,2) not null (volume x harga); `nama_pembeli` varchar(255) null; `keterangan` text null.

**perawatan_lahan**: `tanggal` date not null; `kategori_biaya` varchar(255) not null (**teks bebas**, mis. "honor pengurus lahan"); `deskripsi` text null; `biaya` numeric(15,2) not null.

**kepentingan_umum**: `tanggal` date not null; `kategori_kegiatan` varchar(255) not null (**teks bebas**); `deskripsi` text null; `jumlah` numeric(15,2) not null.

**pupuk_masuk**: `tanggal` date not null; `nama_pupuk` varchar(255) not null; `kuantitas` numeric(10,2) not null; `satuan` varchar(100) not null; `harga_satuan` numeric(15,2) null; `total_harga` numeric(15,2) null; `supplier` varchar(255) null; `keterangan` text null.

**pupuk_terpakai**: `tanggal_pakai` date not null; `nama_pupuk` varchar(255) not null (pilihan dari `pupuk_masuk`); `kuantitas` numeric(10,2) not null; `satuan` varchar(100) not null (pilihan dari `pupuk_masuk`); `tujuan_pakai` varchar(255) null; `keterangan` text null.

### Aturan data penting

- **Pupuk** berupa kotoran hewan ternak yang dibeli per **karung** atau **kol (colt)** dan jarang ditimbang. Satuan tidak boleh dipaksa kg. **[Keputusan]**
- **Stok pupuk** = total `pupuk_masuk.kuantitas` dikurangi total `pupuk_terpakai.kuantitas`, dikelompokkan per `nama_pupuk` dan `satuan`. **Tidak ada konversi antar satuan.** Tampilan contoh: "Kotoran sapi: 30 karung, 2 kol". Normalisasi (trim, huruf kecil) saat mengelompokkan.
- **Penjualan hasil panen** memakai satuan kg (sesuai form).
- Semua query tampilan dan perhitungan harus mengabaikan baris yang `deleted_at`-nya terisi.
- Foto: satu foto bukti per data. Diambil lewat kamera bawaan perangkat, disimpan lokal dulu, lalu diunggah ke Supabase Storage saat online dan `bukti_foto_url` diisi.

### Saran pemetaan tipe ke Room **[Saran]**

| Postgres | Room/Kotlin |
|---|---|
| uuid | `String` (`UUID.randomUUID().toString()`) |
| numeric rupiah | `Long` (rupiah tanpa desimal) atau `Double` |
| numeric volume/kuantitas | `Double` |
| date | `String` ISO-8601 (`yyyy-MM-dd`) |
| timestamptz | `Long` epoch millis di Room, dikonversi ke ISO-8601 saat dikirim ke Supabase |
| boolean | `Boolean` |

## 7. Sinkronisasi

**[Keputusan]** Sinkronisasi **dua arah**, karena banyak pengurus memakai HP masing-masing dan semuanya harus melihat data yang sama.

Setiap kali aplikasi dibuka dalam kondisi online (dan setelah login):

1. **Kirim (push):** ambil semua baris dengan `is_synced = false`, unggah fotonya dulu bila ada, lalu kirim ke Supabase dengan **upsert berdasarkan `id`**, dan tandai `is_synced = true` bila berhasil.
2. **Tarik (pull):** ambil data terbaru dari Supabase dan upsert ke Room. Datanya kecil, boleh ambil semua dulu.
3. **Konflik:** jika satu baris diedit di dua HP, yang `updated_at`-nya paling baru menang.
4. **Hapus:** jangan hapus baris sungguhan. Isi `deleted_at` dan set `is_synced = false`, sehingga penghapusan ikut tersebar.
5. **Edit:** setiap edit memperbarui `updated_at` dan mengubah `is_synced` menjadi false.
6. Antarmuka menampilkan status per data: "Tersinkron" atau "Belum sinkron".

Keamanan di Supabase: aktifkan **Row Level Security** di semua tabel, dengan satu policy: pengguna yang sudah login (`authenticated`) boleh membaca dan menulis semua baris. Jika sinkronisasi gagal tanpa alasan jelas, cek policy RLS dulu.

## 8. Spesifikasi layar

Navigasi utama: bottom navigation dengan 5 tab, yaitu **Dasbor, Panen, Lahan, Pupuk, Menu**. Kepentingan Umum dan Export Data diakses dari Menu atau pintasan Dasbor.

**Login** (Figma): area hero hijau tua dengan nama dan tagline, panel putih membulat di bawahnya dengan judul "Masuk ke SITANI", kolom Email, kolom Kata sandi (dengan tombol lihat/sembunyi), dan tombol Masuk. Pesan galat: "Email atau kata sandi salah." dan "Tidak bisa terhubung. Periksa koneksi internet."

**Dasbor:** top bar (logo, "SITANI", avatar dengan dropdown Profil dan Keluar); kartu Total Pemasukan (jumlah `total_harga` penjualan); kartu Total Pengeluaran (jumlah `biaya` perawatan + `jumlah` kepentingan umum); kartu Saldo Bersih (pemasukan dikurangi pengeluaran); pintasan ke Penjualan, Perawatan Lahan, Kepentingan Umum, Pupuk Masuk, Pupuk Terpakai, Export Data; kartu Stok Pupuk; kartu Transaksi Terbaru dengan chip status sinkronisasi. Saat ini semua angka masih dummy.

**Penjualan Panen:** kartu ringkasan (total penjualan, jumlah transaksi), kolom cari komoditas, filter komoditas (dropdown), daftar transaksi berupa kartu (komoditas, tanggal, volume, harga, total, nama, tombol edit dan hapus), tombol Tambah. Chip status pada desain awal ("Selesai/Proses") **diganti status sinkronisasi**.

**Tambah Penjualan Panen:** top bar dengan tombol kembali; field Tanggal (DatePicker), Komoditas (dropdown), Volume (kg), Harga per kg, Total (otomatis, hanya tampil), Nama pembeli/petani, dan pengambilan foto bukti; tombol Batal dan Simpan. Form yang bisa discroll.

**Perawatan Lahan, Kepentingan Umum, Pupuk Masuk, Pupuk Terpakai:** pola sama dengan Penjualan (daftar + form tambah/edit), dengan field sesuai tabel di bagian 6. Layar Pupuk memiliki dua tab (Masuk dan Terpakai). Setiap form input memiliki pengambilan foto bukti.

**Export (opsional):** hasilkan rekap CSV untuk rentang tanggal tertentu.

## 9. Desain (design system dari Figma)

Gaya: bersih, kartu putih membulat di atas latar hijau muda. Teks antarmuka berbahasa Indonesia. Nominal ditampilkan format rupiah dengan titik pemisah ribuan (contoh: Rp 12.500.000). Warna harus diambil dari `Color.kt`/tema, **jangan hardcode** di layar.

| Token | Hex |
|---|---|
| brand/primary | #2E7A4D |
| brand/dark (app bar) | #0F2419 |
| brand/strong | #1F5C38 |
| brand/light | #8CC79E |
| surface/page | #F4F6F2 |
| text/primary | #101828 |
| text/secondary | #6B7280 |
| border/default | #E5E7EB |
| success background | #DCFCE7 |
| warning strong / background | #B45309 / #FDE68A |
| danger strong / background | #DC2626 / #FEE2E2 |

Radius kartu sekitar 12dp. Font Plus Jakarta Sans (Regular, Medium, SemiBold, Bold).

## 10. Kebutuhan non-fungsional

- **Offline-first:** semua input, daftar, dan perhitungan dasbor bekerja tanpa internet. UI selalu membaca dari Room, bukan langsung dari Supabase.
- **Tidak boleh kehilangan data:** data dan foto tersimpan lokal sampai berhasil terkirim.
- **Ringan:** berjalan baik di HP kelas menengah ke bawah; minim dependency.
- **Keamanan:** RLS aktif, hanya publishable key di aplikasi, pendaftaran publik dimatikan.
- **Keterbacaan:** UI berbahasa Indonesia, label jelas, ramah pengguna non-teknis (pengurus yayasan).

## 11. Konvensi kerja untuk agent **[Saran]**

- Jawab dan tulis teks antarmuka dalam bahasa Indonesia; komentar kode boleh Indonesia atau Inggris, tetap konsisten.
- Jangan mengubah skema tabel, nama kolom, atau keputusan di dokumen ini tanpa konfirmasi.
- Satu perubahan fokus per langkah; jelaskan file mana yang diubah.
- Gunakan komponen dan warna yang sudah ada sebelum membuat yang baru.
- Jangan menambah dependency besar tanpa alasan (PC pengembang lemah). Sebutkan versi yang dipakai dan alasannya.
- Jangan menaruh URL atau key Supabase di berkas yang di-commit.
- Saat menambah library yang memakai Kotlin plugin (Room dengan KSP, serialization), pastikan versinya cocok dengan versi Kotlin project.

## 12. Status saat ini

Konteks jadwal: timeline 8 minggu dihitung dari UTS. Minggu 1 = analisis kebutuhan; 2 = riset UX, wireframe, user flow, rancangan database; 3 = Hi-Fi, setup project, navigasi, tema; 4 = implementasi UI (Login, Dasbor, form input); 5 = Room dan CRUD; 6 = integrasi Supabase (sinkronisasi, upload foto); 7 = export CSV, testing, perbaikan bug; 8 = dokumentasi akhir dan demo.

**Selesai:** desain Hi-Fi dan design system di Figma; ERD mobile (versi di dokumen ini); project Android, tema, font, bottom navigation; layar Dasbor (data dummy) dan top bar.

**Sedang dikerjakan:** login memakai Supabase Auth (kode `AuthRepository`, `LoginScreen`, `AppRoot` sudah ditulis tetapi belum diuji; build sempat gagal karena versi Kotlin 2.2.0 tidak cocok dengan library yang butuh 2.4.0, sedang dinaikkan).

**Berikutnya (urutan disarankan):** uji login end-to-end; layar Penjualan Panen (daftar + form, data dummy); entity, DAO, dan database Room untuk `penjualan_panen`; layar modul lain; pengambilan foto; tabel dan RLS di Supabase; sinkronisasi dua arah; export CSV.

## 13. Pertanyaan terbuka (jangan berasumsi)

1. Nama kolom `nama_pembeli` di ERD, tetapi form Figma berlabel "Nama Petani". Mana yang benar?
2. `tanggal_tanam` ada di deskripsi fitur proposal tetapi tidak ada di tabel `penjualan_panen`. Ditambahkan atau tidak?
3. `harga_satuan` dan `total_harga` di pupuk masuk: wajib diisi atau tetap opsional?
4. Satuan pupuk: pilihan dropdown tetap (karung, kol, kg, lainnya) atau teks bebas?
5. Perilaku login saat offline dengan token kedaluwarsa (lihat bagian 5).
6. Logo resmi yayasan belum ada; saat ini memakai ikon sementara (`ic_eco`).
7. Nama bucket Supabase Storage dan pola penamaan file foto belum ditentukan.
