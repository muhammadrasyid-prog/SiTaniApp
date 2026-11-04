package com.example.sitani

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sitani.ui.theme.*
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(repo: AuthRepository) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(BrandDark)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f)
                .statusBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text("SITANI", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Sistem Informasi Pertanian", color = BrandLight, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Kelola pertanian dengan data yang lebih terarah.",
                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth().weight(0.65f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Masuk ke SITANI", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Gunakan akun organisasi Anda untuk melanjutkan.", fontSize = 13.sp, color = TextSecondary)

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Kata sandi") },
                    singleLine = true,
                    visualTransformation =
                        if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        TextButton(onClick = { showPassword = !showPassword }) {
                            Text(if (showPassword) "Sembunyi" else "Lihat", fontSize = 12.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }

                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            error = null
                            try {
                                repo.login(email.trim(), password)
                            } catch (e: RestException) {
                                error = "Email atau kata sandi salah."
                            } catch (e: Exception) {
                                error = "Tidak bisa terhubung. Periksa koneksi internet."
                            }
                            loading = false
                        }
                    },
                    enabled = !loading && email.isNotBlank() && password.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(if (loading) "Memproses..." else "Masuk")
                }
            }
        }
    }
}