package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AyurvedicEmeraldPrimaryDark,
    onPrimary = AyurvedicEmeraldOnPrimaryDark,
    primaryContainer = AyurvedicEmeraldContainerDark,
    onPrimaryContainer = AyurvedicEmeraldOnContainerDark,
    secondary = TurmericGoldSecondaryDark,
    onSecondary = TurmericGoldOnSecondaryDark,
    secondaryContainer = TurmericGoldContainerDark,
    onSecondaryContainer = TurmericGoldOnContainerDark,
    tertiary = SageTertiaryDark,
    onTertiary = SageOnTertiaryDark,
    tertiaryContainer = SageContainerDark,
    onTertiaryContainer = SageOnContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = AyurvedicEmeraldPrimary,
    onPrimary = AyurvedicEmeraldOnPrimary,
    primaryContainer = AyurvedicEmeraldContainer,
    onPrimaryContainer = AyurvedicEmeraldOnContainer,
    secondary = TurmericGoldSecondary,
    onSecondary = TurmericGoldOnSecondary,
    secondaryContainer = TurmericGoldContainer,
    onSecondaryContainer = TurmericGoldOnContainer,
    tertiary = SageTertiary,
    onTertiary = SageOnTertiary,
    tertiaryContainer = SageContainer,
    onTertiaryContainer = SageOnContainer,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep distinct Ayurvedic branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
