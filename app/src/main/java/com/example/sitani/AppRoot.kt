package com.example.sitani

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@Composable
fun AppRoot() {
    val repo = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val status by repo.sessionStatus.collectAsState()
    var everAuthenticated by rememberSaveable { mutableStateOf(false) }

    Log.d("Auth", "status=$status everAuthenticated=$everAuthenticated")

    LaunchedEffect(status) {
        when (status) {
            is SessionStatus.Authenticated -> everAuthenticated = true
            is SessionStatus.NotAuthenticated -> everAuthenticated = false
            SessionStatus.Initializing,
            is SessionStatus.RefreshFailure -> Unit
        }
    }

    when (status) {
        is SessionStatus.Initializing -> {
            if (everAuthenticated) {
                MainScreen(
                    onLogout = { scope.launch { repo.logout() } }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
        }

        is SessionStatus.Authenticated -> MainScreen(
            onLogout = { scope.launch { repo.logout() } }
        )

        else -> {
            if (everAuthenticated) {
                MainScreen(
                    onLogout = { scope.launch { repo.logout() } }
                )
            } else {
                LoginScreen(repo)
            }
        }
    }
}