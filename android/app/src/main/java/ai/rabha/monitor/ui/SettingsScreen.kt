package ai.rabha.monitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ai.rabha.monitor.ui.theme.fontOptions
import ai.rabha.monitor.ui.theme.palettes

@Composable
fun SettingsScreen(
    themeIndex: Int,
    fontIndex: Int,
    onTheme: (Int) -> Unit,
    onFont: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Themes", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        palettes.forEachIndexed { index, palette ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { onTheme(index) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (index == themeIndex)
                        MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(26.dp)
                            .background(palette.primary, CircleShape)
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        palette.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (index == themeIndex) {
                        Text("Selected", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Fonts", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        fontOptions.forEachIndexed { index, option ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { onFont(index) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (index == fontIndex)
                        MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Aa 123",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = option.family),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        option.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = option.family),
                        modifier = Modifier.weight(1f)
                    )
                    if (index == fontIndex) {
                        Text("Selected", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
