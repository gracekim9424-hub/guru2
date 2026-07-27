package com.damyeoom.app.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeocodeRetrofitClient {

    const val CLIENT_ID = "1p93zfjw09"
    const val CLIENT_SECRET = "56dRdJnofoOqohOuIzimRLA69gP7ii6MJkQpw75p"

    val instance: GeocodeApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://maps.apigw.ntruss.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodeApiService::class.java)
    }
}