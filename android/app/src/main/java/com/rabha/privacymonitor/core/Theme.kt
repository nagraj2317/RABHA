package com.rabha.privacymonitor.core

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object RabhaColors {
    val Black = Color(0xFF000000)
    val Surface = Color(0xFF0A0A0C)
    val SurfaceHigh = Color(0xFF141418)
    val Amber = Color(0xFFFFB020)
    val Blue = Color(0xFF00D1FF)
    val Purple = Color(0xFF9B5CFF)
    val TextPrimary = Color(0xFFF2F2F5)
    val TextSecondary = Color(0xFF9A9AA6)
    val Success = Color(0xFF3DDC97)
    val Warning = Color(0xFFFFC857)
    val Danger = Color(0xFFFF5C7A)
    val GlassStroke = Color(0x33FFFFFF)
    val GlassFill = Color(0x14FFFFFF)
}

private val RabhaColorScheme = darkColorScheme(
    primary = RabhaColors.Amber,
    onPrimary = RabhaColors.Black,
    secondary = RabhaColors.Blue,
    onSecondary = RabhaColors.Black,
    tertiary = RabhaColors.Purple,
    background = RabhaColors.Black,
    onBackground = RabhaColors.TextPrimary,
    surface = RabhaColors.Surface,
    onSurface = RabhaColors.TextPrimary,
    surfaceVariant = RabhaColors.SurfaceHigh,
    onSurfaceVariant = RabhaColors.TextSecondary,
    error = RabhaColors.Danger,
    outline = RabhaColors.GlassStroke
)

private val RabhaTypography = Typography(
    displaySmall = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
)

@Composable
fun RabhaTheme(content: @Composable () -> Unit) {
    // The product is dark-only by design: AMOLED black is part of the identity.
    MaterialTheme(
        colorScheme = RabhaColorScheme,
        typography = RabhaTypography,
        content = content
    )
}
