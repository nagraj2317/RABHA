package com.rabha.privacymonitor.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Two icons the core Material icon set does not ship. They are drawn here as vectors so the app
 * does not need `material-icons-extended`, which adds thousands of classes for five icons.
 */
object RabhaIcons {

    val Monitor: ImageVector by lazy {
        ImageVector.Builder(
            name = "Monitor",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 4f)
                lineTo(21f, 4f)
                lineTo(21f, 18f)
                lineTo(3f, 18f)
                close()
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f
            ) {
                moveTo(4f, 5f)
                lineTo(20f, 5f)
                lineTo(20f, 17f)
                lineTo(4f, 17f)
                close()
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f
            ) {
                moveTo(6.5f, 14f)
                lineTo(10f, 9.5f)
                lineTo(13f, 12.5f)
                lineTo(17.5f, 7.5f)
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f
            ) {
                moveTo(9f, 20f)
                lineTo(15f, 20f)
            }
        }.build()
    }

    val Wallpaper: ImageVector by lazy {
        ImageVector.Builder(
            name = "Wallpaper",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f
            ) {
                moveTo(4f, 5f)
                lineTo(20f, 5f)
                lineTo(20f, 19f)
                lineTo(4f, 19f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(7.5f, 9.5f)
                lineTo(9.5f, 9.5f)
                lineTo(9.5f, 11.5f)
                lineTo(7.5f, 11.5f)
                close()
            }
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f
            ) {
                moveTo(5f, 17f)
                lineTo(10f, 11.5f)
                lineTo(14f, 16f)
                lineTo(16.5f, 13.5f)
                lineTo(19.5f, 17f)
            }
        }.build()
    }
}
