package com.damyeoom.app

import android.app.Application
import com.naver.maps.map.NaverMapSdk

class DamyeoomApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NcpKeyClient("1p93zfjw09")
    }
}