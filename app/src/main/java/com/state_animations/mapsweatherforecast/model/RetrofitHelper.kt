package com.state_animations.mapsweatherforecast.model

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.state_animations.mapsweatherforecast.presentation.ForecastViewModel
import com.state_animations.mapsweatherforecast.retrofit.RetrofitInterface
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback

class RetrofitHelper {
    private lateinit var retrofitInterface: RetrofitInterface
    private val TAG = RetrofitHelper::javaClass.name

    fun getForecast(addressStr: String, callback: ForecastViewModel.ForecastCallback) {
        retrofitInterface = Common.retrofitInterface
        var call = retrofitInterface.getWeatherForecast(
            addressStr,
            "aeef079dba7b844f811a6321985d93ec"
        )

        call.enqueue(object : Callback,
            retrofit2.Callback<Result> {
            override fun onFailure(
                call: Call<Result>,
                t: Throwable
            ) {
                callback.onFailure(t.localizedMessage)
            }

            override fun onResponse(
                call: Call<Result>,
                response: Response<Result>
            ) {
                Log.e(TAG, "onResponse: $response")
                val result = response.body()
                val forecastList = result?.list
                result?.address = addressStr
                result?.let { callback.onResultReceived(it) }

                Log.e(TAG, "forecast list: $forecastList")

            }
        })
    }

    fun getForecast(latLng: LatLng, addressStr: String, callback: ForecastViewModel.ForecastCallback) {
        retrofitInterface = Common.retrofitInterface
        var call = retrofitInterface.getWeatherForecast(
            latLng.latitude.toString(),
            latLng.longitude.toString(),
            "aeef079dba7b844f811a6321985d93ec"
        )

        call.enqueue(object : Callback,
            retrofit2.Callback<Result> {
            override fun onFailure(
                call: Call<Result>,
                t: Throwable
            ) {
                callback.onFailure(t.localizedMessage)
            }

            override fun onResponse(
                call: Call<Result>,
                response: Response<Result>
            ) {
                Log.e(TAG, "onResponse: $response")
                val result = response.body()
                val forecastList = result?.list
                result?.address = addressStr
                result?.let { callback.onResultReceived(it) }

                Log.e(TAG, "forecast list: $forecastList")

            }
        })
    }

}