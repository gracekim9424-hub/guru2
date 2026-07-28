package com.damyeoom.app

import android.app.Application
import com.naver.maps.map.NaverMapSdk

// 앱 실행 시 필요한 초기 설정
class DamyeoomApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 네이버 지도 SDK 인증 정보 설정
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NcpKeyClient("1p93zfjw09")
    }
}