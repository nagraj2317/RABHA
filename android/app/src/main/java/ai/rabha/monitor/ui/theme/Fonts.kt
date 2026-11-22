package ai.rabha.monitor.ui.theme

import androidx.compose.ui.text.font.FontFamily
import android.graphics.Typeface

data class RabhaFont(val name: String, val family: FontFamily)

fun fontFor(androidName: String): FontFamily =
    FontFamily(Typeface.create(androidName, Typeface.NORMAL))

val fontOptions: List<RabhaFont> = listOf(
    RabhaFont("Default", fontFor("sans-serif")),
    RabhaFont("Serif", fontFor("serif")),
    RabhaFont("Mono", fontFor("monospace")),
    RabhaFont("Condensed", fontFor("sans-serif-condensed")),
    RabhaFont("Light", fontFor("sans-serif-light")),
    RabhaFont("Medium", fontFor("sans-serif-medium")),
    RabhaFont("Black", fontFor("sans-serif-black")),
    RabhaFont("Thin", fontFor("sans-serif-thin")),
    RabhaFont("Casual", fontFor("casual")),
    RabhaFont("Cursive", fontFor("cursive"))
)

fun typographyWith(font: FontFamily): androidx.compose.material3.Typography {
    val base = androidx.compose.material3.Typography()
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = font),
        displayMedium = base.displayMedium.copy(fontFamily = font),
        displaySmall = base.displaySmall.copy(fontFamily = font),
        headlineLarge = base.headlineLarge.copy(fontFamily = font),
        headlineMedium = base.headlineMedium.copy(fontFamily = font),
        headlineSmall = base.headlineSmall.copy(fontFamily = font),
        titleLarge = base.titleLarge.copy(fontFamily = font),
        titleMedium = base.titleMedium.copy(fontFamily = font),
        titleSmall = base.titleSmall.copy(fontFamily = font),
        bodyLarge = base.bodyLarge.copy(fontFamily = font),
        bodyMedium = base.bodyMedium.copy(fontFamily = font),
        bodySmall = base.bodySmall.copy(fontFamily = font),
        labelLarge = base.labelLarge.copy(fontFamily = font),
        labelMedium = base.labelMedium.copy(fontFamily = font),
        labelSmall = base.labelSmall.copy(fontFamily = font)
    )
}
