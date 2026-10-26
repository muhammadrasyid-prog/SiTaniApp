package com.example.sitani.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SitaniColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = SuccessBg,
    onPrimaryContainer = BrandStrong,
    secondary = BrandDark,
    onSecondary = Color.White,
    background = SurfacePage,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    outline = BorderDefault,
    error = DangerStrong,
    errorContainer = DangerBg
)

@Composable
fun SiTaniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SitaniColors,
        typography = SitaniTypography,
        content = content
    )
}