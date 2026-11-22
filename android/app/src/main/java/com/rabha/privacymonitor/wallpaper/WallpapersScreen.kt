package com.rabha.privacymonitor.wallpaper

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rabha.privacymonitor.ads.AdSlot
import com.rabha.privacymonitor.core.EmptyState
import com.rabha.privacymonitor.core.Formatters
import com.rabha.privacymonitor.core.GlassCard
import com.rabha.privacymonitor.core.MetricRow
import com.rabha.privacymonitor.core.RabhaColors
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.SectionHeader
import com.rabha.privacymonitor.core.UnavailableState

@Composable
internal fun WallpapersScreen(viewModel: WallpaperViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val htmlPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.importFile(uri, WallpaperKind.HTML)
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.importFile(uri, WallpaperKind.VIDEO)
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.importFile(uri, WallpaperKind.IMAGE)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GlassCard(accent = RabhaColors.Purple) {
                Text("Three engines, one screen", style = MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Android never lets an app switch the live wallpaper silently. Each Apply button opens the system picker with the engine preselected, and you confirm there.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
            }
        }

        item { SectionHeader("HTML wallpaper", "Uploaded page, offline, no JS bridge") }

        item {
            GlassCard(accent = RabhaColors.Amber) {
                MetricRow("Entry file", state.htmlEntry ?: "none")
                Text(
                    text = "Runs from app-private storage with file access between pages off and network loads blocked. JavaScript is on, but there is no addJavascriptInterface bridge — the page cannot call into the app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
                Row {
                    TextButton(onClick = { htmlPicker.launch(arrayOf("text/html", "application/zip", "*/*")) }) {
                        Text("Import HTML", color = RabhaColors.Blue)
                    }
                    TextButton(onClick = viewModel::applyHtml) {
                        Text("Apply", color = RabhaColors.Amber)
                    }
                }
            }
        }

        item { SectionHeader("Video wallpaper", "Media3, muted, loops") }

        item {
            GlassCard(accent = RabhaColors.Blue) {
                MetricRow("Selected video", state.videoPath?.substringAfterLast('/') ?: "none")
                Row {
                    TextButton(onClick = { videoPicker.launch(arrayOf("video/*")) }) {
                        Text("Import video", color = RabhaColors.Blue)
                    }
                    TextButton(onClick = viewModel::applyVideo) {
                        Text("Apply", color = RabhaColors.Amber)
                    }
                }
            }
        }

        item { SectionHeader("Animated wallpaper", "Canvas, battery aware") }

        item {
            GlassCard(accent = RabhaColors.Purple) {
                MetricRow("Style", state.animatedStyle)
                Row {
                    TextButton(onClick = { viewModel.selectAnimated("particles") }) {
                        Text("Particles", color = RabhaColors.Purple)
                    }
                    TextButton(onClick = { viewModel.selectAnimated("neon") }) {
                        Text("Neon", color = RabhaColors.Purple)
                    }
                    TextButton(onClick = viewModel::applyAnimated) {
                        Text("Apply", color = RabhaColors.Amber)
                    }
                }
                Text(
                    text = "Frame rate drops automatically below 20% battery. Nothing is decoded, so it costs less than a video wallpaper.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
            }
        }

        item { SectionHeader("Your files", "${state.local.size} imported") }

        if (state.local.isEmpty()) {
            item { EmptyState("Nothing imported", "Import an image, a video or an HTML page above.") }
        } else {
            items(state.local) { item ->
                GlassCard {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.file.name, style = MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary)
                            Text(
                                text = "${item.kind.label} · ${Formatters.bytes(item.sizeBytes)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RabhaColors.TextSecondary
                            )
                        }
                        if (item.kind == WallpaperKind.IMAGE) {
                            TextButton(onClick = { viewModel.applyStatic(item) }) {
                                Text("Set", color = RabhaColors.Amber)
                            }
                        }
                        TextButton(onClick = { viewModel.delete(item) }) {
                            Text("Delete", color = RabhaColors.Danger)
                        }
                    }
                }
            }
        }

        item { SectionHeader("Gallery", "From Supabase campaigns") }

        when (val remote = state.remote) {
            is RabhaResult.Available -> if (remote.value.isEmpty()) {
                item { EmptyState("Gallery is empty", "No wallpapers have been published yet.") }
            } else {
                items(remote.value) { wallpaper ->
                    GlassCard {
                        Text(wallpaper.title, style = MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary)
                        Text(
                            text = "${wallpaper.kind}${if (wallpaper.isPremium) " · premium" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RabhaColors.TextSecondary
                        )
                    }
                }
            }

            is RabhaResult.Unavailable -> item { UnavailableState("Gallery unavailable", remote.detail) }
            is RabhaResult.Failed -> item { UnavailableState("Gallery read failed", remote.message) }
            null -> item { EmptyState("Loading gallery", "Contacting Supabase…") }
        }

        item { AdSlot(placement = "wallpaper_gallery") }

        if (state.message != null) {
            item {
                GlassCard(accent = RabhaColors.Success) {
                    Text(state.message!!, style = MaterialTheme.typography.bodyMedium, color = RabhaColors.TextPrimary)
                    TextButton(onClick = viewModel::clearMessage) {
                        Text("Dismiss", color = RabhaColors.TextSecondary)
                    }
                }
            }
        }
    }
}
