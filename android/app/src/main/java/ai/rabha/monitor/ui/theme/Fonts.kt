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
