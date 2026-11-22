package com.rabha.privacymonitor.core

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun GlassCard(
    modifier: Modifier = Modifier,
    accent: Color = RabhaColors.GlassStroke,
    content: @Composable ColumnScopeAlias.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(RabhaColors.GlassFill, Color(0x08FFFFFF))
                )
            )
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(content = content)
    }
}

/** Small alias so callers can use ColumnScope-like receivers without importing it everywhere. */
typealias ColumnScopeAlias = androidx.compose.foundation.layout.ColumnScope

@Composable
internal fun SectionHeader(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary)
            if (subtitle != null) {
                Text(text = subtitle, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = RabhaColors.TextSecondary)
            }
        }
        action?.invoke()
    }
}

@Composable
internal fun StatTile(
    label: String,
    value: String,
    hint: String? = null,
    accent: Color = RabhaColors.Amber,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, accent = accent) {
        Text(text = label.uppercase(), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = RabhaColors.TextSecondary)
        Spacer(Modifier.height(6.dp))
        Text(text = value, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = RabhaColors.TextPrimary)
        if (hint != null) {
            Spacer(Modifier.height(4.dp))
            Text(text = hint, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = RabhaColors.TextSecondary)
        }
    }
}

@Composable
internal fun MetricRow(label: String, value: String, accent: Color = RabhaColors.TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, color = RabhaColors.TextSecondary)
        Text(
            text = value,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            color = accent,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun LoadingState(message: String = "Reading device data…") {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = RabhaColors.Amber, strokeWidth = 2.dp, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(14.dp))
            Text(text = message, color = RabhaColors.TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun EmptyState(title: String, message: String, icon: ImageVector = Icons.Filled.Inbox) {
    StateBlock(icon = icon, title = title, message = message, accent = RabhaColors.TextSecondary)
}

@Composable
internal fun ErrorState(title: String, message: String, onRetry: (() -> Unit)? = null) {
    StateBlock(
        icon = Icons.Filled.ErrorOutline,
        title = title,
        message = message,
        accent = RabhaColors.Danger,
        onRetry = onRetry
    )
}

@Composable
internal fun OfflineState(onRetry: (() -> Unit)? = null) {
    StateBlock(
        icon = Icons.Filled.CloudOff,
        title = "You are offline",
        message = "Live panels are paused. Device data collected on this phone still works.",
        accent = RabhaColors.Warning,
        onRetry = onRetry
    )
}

@Composable
internal fun UnavailableState(reason: String, detail: String, onAction: (() -> Unit)? = null, actionLabel: String = "Fix this") {
    StateBlock(
        icon = Icons.Filled.Info,
        title = reason,
        message = detail,
        accent = RabhaColors.Warning,
        onRetry = onAction,
        retryLabel = actionLabel
    )
}

@Composable
private fun StateBlock(
    icon: ImageVector,
    title: String,
    message: String,
    accent: Color,
    onRetry: (() -> Unit)? = null,
    retryLabel: String = "Try again"
) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 12.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(10.dp))
            Text(text = title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = RabhaColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            if (onRetry != null) {
                TextButton(onClick = onRetry) { Text(retryLabel, color = RabhaColors.Amber) }
            }
        }
    }
}

@Composable
internal fun SponsoredBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(RabhaColors.Amber.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "SPONSORED", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = RabhaColors.Amber)
    }
}

@Composable
internal fun AccentDivider(accent: Color = RabhaColors.GlassStroke) {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(accent.copy(alpha = 0.25f))
    )
}

@Composable
internal fun AccentDot(color: Color, size: Int = 8) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
    )
    Spacer(Modifier.width(6.dp))
}
