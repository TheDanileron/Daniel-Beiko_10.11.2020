package com.state_animations.mapsweatherforecast.retrofit

import retrofit2.Call
import retrofit2.http.GET
import com.state_animations.mapsweatherforecast.model.Forecast
import com.state_animations.mapsweatherforecast.model.Result
import retrofit2.http.Query

interface RetrofitInterface {
    @GET("data/2.5/forecast")
    fun getWeatherForecast(@Query("lat") lat: String, @Query("lon") lon: String,@Query("appid") appId:String): Call<Result>

    @GET("data/2.5/forecast")
    fun getWeatherForecast(@Query("q") lat: String,@Query("appid") appId:String): Call<Result>
}