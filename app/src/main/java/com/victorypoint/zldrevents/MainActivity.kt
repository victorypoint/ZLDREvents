package com.victorypoint.zldrevents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.victorypoint.zldrevents.ui.navigation.ZldrNavGraph
import com.victorypoint.zldrevents.ui.theme.ZldrEventsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ZldrApplication
        setContent {
            ZldrEventsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ZldrNavGraph(app = app)
                }
            }
        }
    }
}
