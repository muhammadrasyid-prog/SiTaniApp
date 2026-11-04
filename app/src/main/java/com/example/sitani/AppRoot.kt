package com.example.sitani

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

@Composable
fun AppRoot() {
    val repo = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val status by repo.sessionStatus.collectAsState()

    when (status) {
        is SessionStatus.Initializing -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        is SessionStatus.Authenticated -> MainScreen(
            onLogout = { scope.launch { repo.logout() } }
        )

        else -> LoginScreen(repo)
    }
}