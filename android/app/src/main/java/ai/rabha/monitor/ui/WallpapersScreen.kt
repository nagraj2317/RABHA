package ai.rabha.monitor.ui

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ai.rabha.monitor.wallpaper.RabhaWallpaperService
import ai.rabha.monitor.wallpaper.WallpaperArt
import kotlinx.coroutines.delay

@Composable
private fun LivePreview(mode: Int, accent: Int, modifier: Modifier) {
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(mode) {
        while (true) {
            t = System.currentTimeMillis() / 1000f
            delay(50)
        }
    }
    Canvas(modifier = modifier) {
        WallpaperArt.draw(
            drawContext.canvas.nativeCanvas,
            size.width,
            size.height,
            mode,
            t,
            accent
        )
    }
}

@Composable
fun WallpapersScreen() {
    val context = LocalContext.current
    val accent = MaterialTheme.colorScheme.primary.value.toLong().let { 0xFFFFB020.toInt() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(WallpaperArt.names) { index, name ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    LivePreview(
                        mode = index,
                        accent = accent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                        Button(onClick = { applyLiveWallpaper(context, index) }) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}

private fun applyLiveWallpaper(context: Context, index: Int) {
    context.getSharedPreferences("rabha", Context.MODE_PRIVATE)
        .edit()
        .putInt("wallpaper_mode", index)
        .apply()
    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
    intent.putExtra(
        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
        ComponentName(context, RabhaWallpaperService::class.java)
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
