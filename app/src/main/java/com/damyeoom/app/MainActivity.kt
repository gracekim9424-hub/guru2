package com.damyeoom.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.data.loadPlacesFromAssets
import com.damyeoom.app.ui.DamyeoomNavGraph
import com.damyeoom.app.ui.theme.DamyeoomTheme
import kotlinx.coroutines.launch

// 앱의 메인 화면을 실행하는 Activity
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 화면을 전체 영역까지 확장
        enableEdgeToEdge()

        // Room 데이터베이스 객체 생성
        val db = AppDatabase.getDatabase(this)

        // places.json의 장소 정보를 DB에 저장
        lifecycleScope.launch {
            val placeDao = db.placeDao()
            val places = loadPlacesFromAssets(applicationContext)
            placeDao.insertAll(places)
        }

        // Compose 화면 시작
        setContent {
            DamyeoomTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    DamyeoomNavGraph()
                }
            }
        }
    }
}