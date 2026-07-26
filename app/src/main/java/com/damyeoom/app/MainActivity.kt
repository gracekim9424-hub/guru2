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
import com.damyeoom.app.data.database.AppDatabase
import androidx.lifecycle.lifecycleScope
import com.damyeoom.app.data.loadPlacesFromAssets
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val placeDao = db.placeDao()

            if (placeDao.getPlaceCount() == 0) {
                val places = loadPlacesFromAssets(applicationContext)
                placeDao.insertAll(places)
            }
        }

        setContent {
            DamyeoomTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DamyeoomNavGraph(db = db)
                }
            }
        }
    }
}
