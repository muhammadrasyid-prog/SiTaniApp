package com.example.sitani.ui.components

import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sitani.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDialog(
    showDelete: Boolean,
    showDiscard: Boolean,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onDiscardConfirm: () -> Unit,
    onDiscardDismiss: () -> Unit,
    deleteTitle: String = "Hapus Data",
    deleteMessage: String = "Yakin ingin menghapus data penjualan panen ini?",
    discardTitle: String = "Buang perubahan?",
    discardMessage: String = "Perubahan yang belum disimpan akan hilang.",
    modifier: Modifier = Modifier
) {
    if (showDelete) {
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = { Text(deleteTitle) },
            text = { Text(deleteMessage) },
            confirmButton = {
                TextButton(onClick = onDeleteConfirm) {
                    Text("Hapus", color = DangerStrong)
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismiss) {
                    Text("Batal")
                }
            }
        )
    }

    if (showDiscard) {
        AlertDialog(
            onDismissRequest = onDiscardDismiss,
            title = { Text(discardTitle) },
            text = { Text(discardMessage) },
            confirmButton = {
                TextButton(onClick = onDiscardConfirm) {
                    Text("Buang", color = DangerStrong)
                }
            },
            dismissButton = {
                TextButton(onClick = onDiscardDismiss) {
                    Text("Batal")
                }
            }
        )
    }
}
