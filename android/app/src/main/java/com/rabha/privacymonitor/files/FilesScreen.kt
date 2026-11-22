package com.rabha.privacymonitor.files

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
import com.rabha.privacymonitor.core.EmptyState
import com.rabha.privacymonitor.core.Formatters
import com.rabha.privacymonitor.core.GlassCard
import com.rabha.privacymonitor.core.RabhaColors
import com.rabha.privacymonitor.core.SectionHeader

@Composable
internal fun FilesScreen(viewModel: FilesViewModel = hiltViewModel()) {
    val files by viewModel.files.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.onFilePicked(uri)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GlassCard(accent = RabhaColors.Blue) {
                Text("Only what you pick", style = MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "RABHA records name, type, size and direction for files you choose in the system picker. It never scans storage and cannot read another app's private files — that needs root.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
                Row {
                    TextButton(onClick = { picker.launch(arrayOf("*/*")) }) {
                        Text("Pick a file", color = RabhaColors.Amber)
                    }
                    TextButton(onClick = viewModel::clear) {
                        Text("Clear list", color = RabhaColors.TextSecondary)
                    }
                }
                if (message != null) {
                    Text(message!!, style = MaterialTheme.typography.bodyMedium, color = RabhaColors.Success)
                }
            }
        }

        item { SectionHeader("Recorded metadata", "${files.size} entries") }

        if (files.isEmpty()) {
            item { EmptyState("No files yet", "Pick a file to record its metadata. Nothing is uploaded.") }
        } else {
            items(files) { file ->
                GlassCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(file.displayName, style = MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary)
                            Text(
                                text = "${file.mimeType} · ${Formatters.bytes(file.sizeBytes)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RabhaColors.TextSecondary
                            )
                        }
                        Text(
                            text = file.direction,
                            style = MaterialTheme.typography.labelSmall,
                            color = RabhaColors.Blue
                        )
                    }
                }
            }
        }
    }
}
