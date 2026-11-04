package com.example.sitani

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sitani.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                if (placeholder.isNotBlank()) {
                    Text(placeholder, color = TextPlaceholder, fontSize = 14.sp)
                }
            },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandPrimary,
                unfocusedBorderColor = BorderChart,
                errorBorderColor = DangerStrong,
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                errorContainerColor = SurfaceWhite
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DateField(
    label: String,
    dateString: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val context = LocalContext.current
    val formattedDisplay = remember(dateString) {
        runCatching {
            val parsed = LocalDate.parse(dateString)
            parsed.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID")))
        }.getOrDefault(dateString)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceWhite)
                .border(
                    width = 1.dp,
                    color = if (isError) DangerStrong else BorderChart,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                    val calendar = Calendar.getInstance()
                    runCatching {
                        val parsed = LocalDate.parse(dateString)
                        calendar.set(parsed.year, parsed.monthValue - 1, parsed.dayOfMonth)
                    }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val m = String.format("%02d", month + 1)
                            val d = String.format("%02d", dayOfMonth)
                            onDateSelected("$year-$m-$d")
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDisplay.ifBlank { "Pilih tanggal" },
                    color = if (formattedDisplay.isNotBlank()) TextPrimary else TextPlaceholder,
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Pilih Tanggal",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun MoneyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "cth. 5.000",
    isError: Boolean = false
) {
    FormField(
        label = label,
        value = value,
        onValueChange = { input ->
            val cleaned = input.filter { it.isDigit() }
            onValueChange(cleaned)
        },
        modifier = modifier,
        placeholder = placeholder,
        keyboardType = KeyboardType.Number,
        isError = isError
    )
}
