package com.team.travelmap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class RecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lat = intent.getDoubleExtra(IntentKeys.EXTRA_LATITUDE, 0.0)
        val lon = intent.getDoubleExtra(IntentKeys.EXTRA_LONGITUDE, 0.0)
        // TODO: UI 담당(개발 A)이 이어서 화면 구성
    }
}