package com.damyeoom.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.damyeoom.app.ui.DamyeoomNavGraph
import com.damyeoom.app.ui.theme.DamyeoomTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(this)   // ← 추가

        setContent {
            DamyeoomTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DamyeoomNavGraph(db = db)     // ← db 전달
                }
            }
        }
    }
}
