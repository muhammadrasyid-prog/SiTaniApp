package com.example.sitani

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.flow.StateFlow

object SupabaseProvider {
    val client = createSupabaseClient(
        supabaseUrl = "https://huvcxszfyzdpbjunixrz.supabase.co",
        supabaseKey = "sb_publishable_JerLlgBiRTF-UA1ojahMUg_T_QaFiXa"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}

class AuthRepository {
    private val auth get() = SupabaseProvider.client.auth

    val sessionStatus: StateFlow<SessionStatus> get() = auth.sessionStatus

    suspend fun login(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun logout() {
        auth.signOut()
    }
}