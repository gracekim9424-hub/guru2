package com.team.travelmap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// 지도에서 선택한 위치 정보를 전달받는 Activity
class RecordActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        // MapActivity에서 전달한 위도와 경도
        val lat =
            intent.getDoubleExtra(
                IntentKeys.EXTRA_LATITUDE,
                0.0
            )

        val lon =
            intent.getDoubleExtra(
                IntentKeys.EXTRA_LONGITUDE,
                0.0
            )
    }
}