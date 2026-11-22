package com.rabha.privacymonitor.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object Formatters {

    fun bytes(value: Long): String {
        if (value < 0) return "—"
        if (value < 1024) return "$value B"
        val units = listOf("KB", "MB", "GB", "TB")
        var size = value.toDouble()
        var unit = -1
        while (size >= 1024 && unit < units.lastIndex) {
            size /= 1024
            unit++
        }
        val pattern = if (size >= 100) "%.0f" else "%.1f"
        return String.format(Locale.US, "$pattern %s", size, units[unit])
    }

    fun duration(millis: Long): String {
        if (millis <= 0) return "0m"
        val totalMinutes = millis / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    fun percent(part: Long, total: Long): String {
        if (total <= 0) return "—"
        val pct = (part.toDouble() / total.toDouble() * 100).roundToInt()
        return "$pct%"
    }

    fun time(millis: Long): String =
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(millis))

    fun shortTime(millis: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
}
