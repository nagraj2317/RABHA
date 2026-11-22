package ai.rabha.monitor.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

data class RabhaPalette(
    val name: String,
    val primary: Color,
    val secondary: Color,
    val background: Color
)

fun hex(v: Long): Color = Color(v.toInt())

val palettes: List<RabhaPalette> = listOf(
    RabhaPalette("Amber Neon", hex(0xFFFFB020), hex(0xFF2F8CFF), hex(0xFF000000)),
    RabhaPalette("Electric Blue", hex(0xFF2F8CFF), hex(0xFF7FE7FF), hex(0xFF05070D)),
    RabhaPalette("Violet Pulse", hex(0xFF9B5CFF), hex(0xFFFF6BD6), hex(0xFF08060F)),
    RabhaPalette("Emerald", hex(0xFF00E08A), hex(0xFF7FE7FF), hex(0xFF00110C)),
    RabhaPalette("Crimson", hex(0xFFFF3B5C), hex(0xFFFFB020), hex(0xFF100005)),
    RabhaPalette("Cyber Lime", hex(0xFFB6FF3B), hex(0xFF00E08A), hex(0xFF060A00)),
    RabhaPalette("Sunset", hex(0xFFFF7A45), hex(0xFFFFC46B), hex(0xFF0D0503)),
    RabhaPalette("Ice", hex(0xFF7FE7FF), hex(0xFF2F8CFF), hex(0xFF02080C)),
    RabhaPalette("Rose Gold", hex(0xFFF2A0A0), hex(0xFFFFD9A0), hex(0xFF0C0606)),
    RabhaPalette("Mono", hex(0xFFE6E6E6), hex(0xFF8A8A94), hex(0xFF000000))
)

fun schemeFor(p: RabhaPalette) = darkColorScheme(
    primary = p.primary,
    onPrimary = Color.Black,
    secondary = p.secondary,
    onSecondary = Color.Black,
    tertiary = p.primary,
    background = p.background,
    onBackground = hex(0xFFF2F2F5),
    surface = p.background,
    onSurface = hex(0xFFF2F2F5),
    surfaceVariant = hex(0xFF111116),
    onSurfaceVariant = hex(0xFF9A9AA5),
    error = hex(0xFFFF4D5E),
    onError = Color.Black
)
