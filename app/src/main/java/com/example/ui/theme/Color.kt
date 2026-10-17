package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Dark palette (default) - exact design tokens
val DarkBackground = Color(0xFF0A0A0B)
val DarkBackgroundSoft = Color(0xFF0F0F11)
val DarkSurface = Color(0xFF141416)
val DarkSurface2 = Color(0xFF1C1C20)
val DarkSurface3 = Color(0xFF242428)
val DarkBorder = Color(0x14FFFFFF)       // white @ 8% alpha
val DarkBorderStrong = Color(0x24FFFFFF) // white @ 14% alpha
val DarkText = Color(0xFFEDEDF0)
val DarkTextDim = Color(0xFF9A9AA2)
val DarkTextFaint = Color(0xFF6B6B73)
val DarkAccent = Color(0xFFE8703A)       // the orange. Used sparingly.
val DarkAccentSoft = Color(0xFFF2A65A)
val DarkAccentInk = Color(0xFF1A0D05)    // text ON the accent
val DarkAccentDim = Color(0x24E8703A)    // accent @ 14% alpha
val DarkDanger = Color(0xFFE5484D)
val DarkOK = Color(0xFF3FB950)
val DarkCallBackground = Color(0xFF08080A)

// Light palette (secondary)
val LightBackground = Color(0xFFF6F6F7)
val LightBackgroundSoft = Color(0xFFEEEEF0)
val LightSurface = Color(0xFFFFFFFF)
val LightSurface2 = Color(0xFFF1F1F3)
val LightSurface3 = Color(0xFFE7E7EA)
val LightBorder = Color(0x14000000)
val LightBorderStrong = Color(0x24000000)
val LightText = Color(0xFF16161A)
val LightTextDim = Color(0xFF5C5C66)
val LightTextFaint = Color(0xFF8E8E98)
val LightAccent = Color(0xFFE8703A)
val LightAccentSoft = Color(0xFFF2A65A)
val LightAccentInk = Color(0xFFFFFFFF)
val LightAccentDim = Color(0x1FE8703A)
val LightDanger = Color(0xFFE5484D)
val LightOK = Color(0xFF3FB950)
val LightCallBackground = Color(0xFF08080A)

@Immutable
data class PortfolioColorTokens(
    val background: Color,
    val backgroundSoft: Color,
    val surface: Color,
    val surface2: Color,
    val surface3: Color,
    val border: Color,
    val borderStrong: Color,
    val text: Color,
    val textDim: Color,
    val textFaint: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentInk: Color,
    val accentDim: Color,
    val danger: Color,
    val ok: Color,
    val callBackground: Color
)

val DarkTokens = PortfolioColorTokens(
    background = DarkBackground,
    backgroundSoft = DarkBackgroundSoft,
    surface = DarkSurface,
    surface2 = DarkSurface2,
    surface3 = DarkSurface3,
    border = DarkBorder,
    borderStrong = DarkBorderStrong,
    text = DarkText,
    textDim = DarkTextDim,
    textFaint = DarkTextFaint,
    accent = DarkAccent,
    accentSoft = DarkAccentSoft,
    accentInk = DarkAccentInk,
    accentDim = DarkAccentDim,
    danger = DarkDanger,
    ok = DarkOK,
    callBackground = DarkCallBackground
)

val LightTokens = PortfolioColorTokens(
    background = LightBackground,
    backgroundSoft = LightBackgroundSoft,
    surface = LightSurface,
    surface2 = LightSurface2,
    surface3 = LightSurface3,
    border = LightBorder,
    borderStrong = LightBorderStrong,
    text = LightText,
    textDim = LightTextDim,
    textFaint = LightTextFaint,
    accent = LightAccent,
    accentSoft = LightAccentSoft,
    accentInk = LightAccentInk,
    accentDim = LightAccentDim,
    danger = LightDanger,
    ok = LightOK,
    callBackground = LightCallBackground
)

val LocalPortfolioColors = staticCompositionLocalOf { DarkTokens }
