package com.state_animations.mapsweatherforecast.presentation

import android.app.Application
import android.location.Geocoder
import androidx.arch.core.util.Function
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.state_animations.mapsweatherforecast.app_data.CacheHelper
import com.state_animations.mapsweatherforecast.model.Forecast
import com.state_animations.mapsweatherforecast.model.Result
import com.state_animations.mapsweatherforecast.model.RetrofitHelper
import java.io.IOException
import java.util.*

class ForecastViewModel(app: Application) : AndroidViewModel(app) {
    private var forecastListLiveData: MutableLiveData<MutableList<Forecast>> = MutableLiveData()
    private var currentTimestampLiveData: MutableLiveData<Long> = MutableLiveData()
    private var currentForecast: LiveData<Forecast> = Transformations.map(currentTimestampLiveData, Function<Long, Forecast> {
        getForecastByTimestamp(it)
    })
    private var address: String? = null
    // current day starting time for seekbar
    private var startTimestamp: Long = 0
    private var selectedDay: Int = 0
    private var latLng: LatLng? = null

    fun getSelectedDay() : Int{
        return selectedDay
    }

    fun setSelectedDay(day: Int) {
        selectedDay = day
    }

    fun getCurrentTimeLiveData(): MutableLiveData<Long>{
        return currentTimestampLiveData
    }

    fun setCurrentTimeLiveData(timestamp: Long) {
        currentTimestampLiveData.postValue(timestamp);
    }

    fun getCurrentForecastLiveData(): LiveData<Forecast>? {
        return currentForecast
    }

    fun getForecastsLiveData(): MutableLiveData<MutableList<Forecast>> {
        return forecastListLiveData
    }

    fun getForecasts(): MutableList<Forecast>? {
        return forecastListLiveData.value
    }

    fun updateCurrentForecast(timestampSeconds: Long, setStartTime: Boolean) {
        getForecasts()?.forEach {
            if (it.dt == timestampSeconds) {
                if (setStartTime)
                    startTimestamp = it.dt
                currentTimestampLiveData.postValue(timestampSeconds)
            }
        }
    }

    fun getForecastByTimestamp(timestampSeconds: Long): Forecast? {
        getForecasts()?.forEach {
            if (it.dt == timestampSeconds) {
                return it
            }
        }
        return null
    }

    fun getCurrentForecast(): Forecast? {
        return currentForecast.value
    }


    fun getAddress(): String? {
        return address
    }

    fun getStartTime(): Long {
        return startTimestamp
    }

    private val cacheHelper = CacheHelper(getApplication())

    fun parseAddressAndGetForecast(place: Place) {
        var addressStr = ""
        var addressComponents = place.addressComponents?.asList()
        addressComponents?.forEach {
            if (it.types.contains("locality")) {
                addressStr = it.name
            } else if (it.types.contains("administrative_area")) {
                addressStr = it.name
            }

            if (it.types.contains("country")) {
                addressStr += "," + it.name
            }
        }
        getForecast(addressStr)
    }

    fun getForecast(addressStr: String) {
        val retrofitHelper =
            RetrofitHelper()
        if (!isConnected()) {
            val result = cacheHelper.getCachedResult()
            forecastListLiveData.postValue(result?.list)
            return
        }
        address = addressStr
        retrofitHelper.getForecast(addressStr, object :
            ForecastCallback {
            override fun onResultReceived(result: Result) {
                forecastListLiveData.postValue(result.list)
                currentTimestampLiveData.postValue(result.list[0].dt)
                startTimestamp = result.list[0].dt
                selectedDay = 0
                cacheHelper.writeResultToFile(result)
            }

            override fun onFailure(error: String) {

            }
        })
    }

    fun getForecast(latLng: LatLng) {
        val retrofitHelper =
            RetrofitHelper()
        if (!isConnected()) {
            val result = cacheHelper.getCachedResult()
            forecastListLiveData.postValue(result?.list)
            return
        }
        address = latLngToAddress(latLng)
        retrofitHelper.getForecast(latLng, latLngToAddress(latLng), object :
            ForecastCallback {
            override fun onResultReceived(result: Result) {
                forecastListLiveData.postValue(result.list)
                currentTimestampLiveData.postValue(result.list[0].dt)
                startTimestamp = result.list[0].dt
                selectedDay = 0
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

    fun getLatLng():LatLng? {
        return latLng
    }

    fun setLatLng(latLng: LatLng?) {
        this.latLng = latLng
    }
}