package com.example.sitani.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.sitani.ui.components.FormField
import com.example.sitani.ui.theme.*

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
