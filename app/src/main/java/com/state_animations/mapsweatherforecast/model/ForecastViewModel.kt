package com.state_animations.mapsweatherforecast.model

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.state_animations.mapsweatherforecast.app_data.CacheHelper
import java.io.IOException
import java.util.*

class ForecastViewModel(app: Application) : AndroidViewModel(app) {
    private var forecastListLiveData: MutableLiveData<MutableList<Forecast>> = MutableLiveData()
    private var currentForecast: Forecast? = null
    private var address: String? = null
    // current day starting time for seekbar
    private var startTimestamp: Long = 0

    fun getForecastsLiveDate(): MutableLiveData<MutableList<Forecast>> {
        return forecastListLiveData
    }

    fun setCurrentStartTime(timestamp: Long) {
        startTimestamp = timestamp
    }

    fun getForecasts(): MutableList<Forecast>? {
        return forecastListLiveData.value
    }

    fun getCurrentForecast(): Forecast? {
        return currentForecast
    }

    fun setCurrentForecast(forecast: Forecast) {
        currentForecast = forecast
    }

    fun getAddress(): String? {
        return address
    }

    fun getStartTime(): Long {
        return startTimestamp
    }

    private val cacheHelper = CacheHelper(getApplication())

    fun getForecast(addressStr: String) {
        val retrofitHelper = RetrofitHelper()
        if (!isConnected()) {
            val result = cacheHelper.getCachedResult()
            forecastListLiveData.postValue(result?.list)
            return
        }
        retrofitHelper.getForecast(addressStr, object : ForecastCallback {
            override fun onResultReceived(result: Result) {
                forecastListLiveData.postValue(result.list)
                currentForecast = result.list[0]
                startTimestamp = result.list[0].dt
                cacheHelper.writeResultToFile(result)
            }

            override fun onFailure(error: String) {

            }
        })
    }

    fun getForecast(latLng: LatLng) {
        val retrofitHelper = RetrofitHelper()
        if (!isConnected()) {
            val result = cacheHelper.getCachedResult()
            forecastListLiveData.postValue(result?.list)
            return
        }
        retrofitHelper.getForecast(latLng, latLngToAddress(latLng), object : ForecastCallback {
            override fun onResultReceived(result: Result) {
                forecastListLiveData.postValue(result.list)
                currentForecast = result.list[0]
                startTimestamp = result.list[0].dt
                cacheHelper.writeResultToFile(result)
            }

            override fun onFailure(error: String) {

            }
        })
    }

    interface ForecastCallback {
        fun onResultReceived(result: Result)
        fun onFailure(error: String)
    }

    @Throws(InterruptedException::class, IOException::class)
    fun isConnected(): Boolean {
        val command = "ping -c 1 google.com"
        return Runtime.getRuntime().exec(command).waitFor() == 0
    }

    fun latLngToAddress(latLng: LatLng): String {
        val geocoder = Geocoder(getApplication(), Locale.ENGLISH)
        val list = geocoder.getFromLocation(latLng!!.latitude, latLng!!.longitude, 1)
        if (list.size > 0) {
            val it = list[0]
            var str = ""
            if (it.locality != null) {
                str += it.locality
            } else if (it.adminArea != null) {
                str += it.adminArea
            }
            if (it.countryCode != null) {
                str += "," + it.countryCode
            }
            return str
        }

        return "US"
    }
}