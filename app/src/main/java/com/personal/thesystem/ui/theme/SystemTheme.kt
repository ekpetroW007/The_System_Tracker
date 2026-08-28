package com.personal.thesystem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF090A0C)
val MorningInk = Color(0xFF0D141A)
val NightInk = Color(0xFF050608)
val Surface = Color(0xFF111317)
val SurfaceRaised = Color(0xFF171A1F)
val SurfaceSoft = Color(0xFF1D2026)
val Acid = Color(0xFFD9D9D6)
val AcidDim = Color(0xFF92969A)
val Paper = Color(0xFFF2F3EC)
val Muted = Color(0xFF949A9F)
val Hairline = Color(0xFF2A2E34)
val Danger = Color(0xFFFF7468)
val Amber = Color(0xFFB2B6BA)

private val SystemColors = darkColorScheme(
    primary = Acid,
    onPrimary = Ink,
    secondary = Paper,
    onSecondary = Ink,
    background = Ink,
    onBackground = Paper,
    surface = Surface,
    onSurface = Paper,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = Muted,
    error = Danger,
    outline = Hairline,
)

private val SystemTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 56.sp,
        lineHeight = 56.sp,
        letterSpacing = (-2).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 42.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.2).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 29.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 21.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.7.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp,
    ),
)

@Composable
fun TheSystemTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SystemColors,
        typography = SystemTypography,
        content = content,
    )
}
