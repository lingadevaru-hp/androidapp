package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkMaterialColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkAccentInk,
    primaryContainer = DarkSurface2,
    onPrimaryContainer = DarkAccentSoft,
    secondary = DarkAccentSoft,
    onSecondary = DarkAccentInk,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = DarkTextDim,
    outline = DarkBorderStrong,
    outlineVariant = DarkBorder,
    error = DarkDanger,
    onError = DarkText
)

private val LightMaterialColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightAccentInk,
    primaryContainer = LightSurface2,
    onPrimaryContainer = LightAccent,
    secondary = LightAccentSoft,
    onSecondary = LightAccentInk,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurface2,
    onSurfaceVariant = LightTextDim,
    outline = LightBorderStrong,
    outlineVariant = LightBorder,
    error = LightDanger,
    onError = LightBackground
)

object PortfolioTheme {
    val colors: PortfolioColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalPortfolioColors.current

    val typography = PortfolioTypography
}

@Composable
fun ThoshanAssistantTheme(
    darkTheme: Boolean = true, // Dark theme is default
    content: @Composable () -> Unit
) {
    val colorTokens = if (darkTheme) DarkTokens else LightTokens
    val materialColorScheme = if (darkTheme) DarkMaterialColorScheme else LightMaterialColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorTokens.background.toArgb()
            window.navigationBarColor = colorTokens.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            // Light icons on dark status bar when dark theme
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalPortfolioColors provides colorTokens
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = PortfolioTypography,
            content = content
        )
    }
}
