package ai.rabha.monitor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AmoledBlack = Color(0xFF000000.toInt())
val SurfaceBlack = Color(0xFF0A0A0C.toInt())
val NeonAmber = Color(0xFFFFB020.toInt())
val ElectricBlue = Color(0xFF2F8CFF.toInt())
val PurpleAccent = Color(0xFF9B5CFF.toInt())
val TextPrimary = Color(0xFFF2F2F5.toInt())
val TextMuted = Color(0xFF8A8A94.toInt())
val ErrorRed = Color(0xFFFF4D5E.toInt())

private val RabhaColors = darkColorScheme(
    primary = NeonAmber,
    onPrimary = AmoledBlack,
    secondary = ElectricBlue,
    onSecondary = AmoledBlack,
    tertiary = PurpleAccent,
    onTertiary = AmoledBlack,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = SurfaceBlack,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceBlack,
    onSurfaceVariant = TextMuted,
    error = ErrorRed,
    onError = AmoledBlack
)

@Composable
fun RabhaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RabhaColors, content = content)
}
