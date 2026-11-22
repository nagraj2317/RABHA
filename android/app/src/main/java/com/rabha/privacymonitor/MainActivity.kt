package com.rabha.privacymonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rabha.privacymonitor.core.RabhaColors
import com.rabha.privacymonitor.core.RabhaTheme
import com.rabha.privacymonitor.navigation.RabhaApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RabhaTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = RabhaColors.Black) {
                    RabhaApp()
                }
            }
        }
    }
}
