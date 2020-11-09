package com.state_animations.mapsweatherforecast.model

import com.state_animations.mapsweatherforecast.retrofit.RetrofitClient
import com.state_animations.mapsweatherforecast.retrofit.RetrofitInterface

object Common {
    private val BASE_URL = "https://api.openweathermap.org/"
    val retrofitInterface: RetrofitInterface
        get() = RetrofitClient.getClient(BASE_URL).create(RetrofitInterface::class.java)
}