package com.example.sitani

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.sitani.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun PhotoPickerField(
    photoPath: String?,
    onPhotoChanged: (String?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Foto Bukti"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latestPhotoPath by rememberUpdatedState(photoPath)

    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }
    var processing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showFullScreen by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val tempPath = pendingPath
        pendingPath = null

        if (success && tempPath != null) {
            scope.launch {
                processing = true
                val compressedPath = withContext(Dispatchers.IO) { compressPhoto(tempPath) }
                if (compressedPath != null) {
                    val oldPath = latestPhotoPath
                    if (!oldPath.isNullOrBlank() && oldPath != compressedPath) {
                        withContext(Dispatchers.IO) { File(oldPath).delete() }
                    }
                    onPhotoChanged(compressedPath)
                } else {
                    withContext(Dispatchers.IO) { File(tempPath).delete() }
                    errorMessage = "Foto gagal diproses. Coba ambil ulang."
                }
                processing = false
            }
        } else if (tempPath != null) {
            File(tempPath).delete()
        }
    }

    fun openCamera() {
        errorMessage = null
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: File(context.filesDir, "Pictures").apply { mkdirs() }
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "bukti_${UUID.randomUUID()}_${System.currentTimeMillis()}.jpg")
        runCatching {
            file.createNewFile()
            pendingPath?.let { File(it).delete() }
            pendingPath = file.absolutePath
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            launcher.launch(uri)
        }.onFailure {
            file.delete()
            pendingPath = null
            errorMessage = "Kamera tidak tersedia di perangkat ini."
        }
    }

    fun deletePhoto() {
        val oldPath = photoPath
        onPhotoChanged(null)
        if (!oldPath.isNullOrBlank()) File(oldPath).delete()
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        if (photoPath == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceSubtle)
                    .border(
                        width = 1.5.dp,
                        color = BorderChart,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { if (!processing) openCamera() }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(BrandPrimary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Ketuk untuk ambil foto bukti",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Opsional · Tersimpan di perangkat",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable { showFullScreen = true }
                    ) {
                        PhotoImage(
                            path = photoPath,
                            maxSize = 900,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (processing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { if (!processing) openCamera() }, enabled = !processing) {
                            Text("Ganti")
                        }
                        TextButton(onClick = { if (!processing) deletePhoto() }, enabled = !processing) {
                            Text("Hapus", color = DangerStrong)
                        }
                    }
                }
            }
        }

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
    }

    if (showFullScreen && photoPath != null) {
        Dialog(
            onDismissRequest = { showFullScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                PhotoImage(
                    path = photoPath,
                    maxSize = 2048,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { showFullScreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun PhotoImage(
    path: String,
    maxSize: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, path, maxSize) {
        value = withContext(Dispatchers.IO) { decodeSampledBitmap(path, maxSize) }
    }

    val current = bitmap
    if (current != null) {
        Image(
            bitmap = current.asImageBitmap(),
            contentDescription = "Pratinjau foto bukti",
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Foto tidak dapat dibuka", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

private fun compressPhoto(path: String): String? {
    val file = File(path)
    if (!file.exists()) return null

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    val longestSide = maxOf(bounds.outWidth, bounds.outHeight)
    while (longestSide / sampleSize > 2560) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val original = BitmapFactory.decodeFile(path, options) ?: return null

    val rotated = applyExifOrientation(original, path)
    val scale = minOf(
        1280f / rotated.width,
        1280f / rotated.height,
        1f
    )
    val scaled = if (scale < 1f) {
        Bitmap.createScaledBitmap(
            rotated,
            (rotated.width * scale).roundToInt().coerceAtLeast(1),
            (rotated.height * scale).roundToInt().coerceAtLeast(1),
            true
        )
    } else {
        rotated
    }

    val saved = runCatching {
        FileOutputStream(file).use { output ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, output)
        }
    }.getOrDefault(false)

    if (scaled !== rotated) scaled.recycle()
    if (rotated !== original) rotated.recycle()
    original.recycle()

    return if (saved) file.absolutePath else null
}

private fun applyExifOrientation(bitmap: Bitmap, path: String): Bitmap {
    val orientation = runCatching {
        ExifInterface(path).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(270f)
            matrix.postScale(-1f, 1f)
        }
    }

    return if (matrix.isIdentity) {
        bitmap
    } else {
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}

private fun decodeSampledBitmap(path: String, maxSize: Int): Bitmap? {
    val file = File(path)
    if (!file.exists()) return null

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > maxSize || bounds.outHeight / sampleSize > maxSize) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return BitmapFactory.decodeFile(path, options)
}
