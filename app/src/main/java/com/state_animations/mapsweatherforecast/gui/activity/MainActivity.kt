package com.state_animations.mapsweatherforecast.gui.activity

import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.state_animations.mapsweatherforecast.R
import com.state_animations.mapsweatherforecast.app_data.CacheHelper
import com.state_animations.mapsweatherforecast.gui.fragments.FragmentForecast
import com.state_animations.mapsweatherforecast.model.Common
import com.state_animations.mapsweatherforecast.model.DateHelper
import com.state_animations.mapsweatherforecast.retrofit.RetrofitInterface
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.*
import javax.security.auth.callback.Callback

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var mMap: GoogleMap
    private lateinit var retrofitInterface: RetrofitInterface
    private val cacheHelper = CacheHelper(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        forecastBtn.setOnClickListener {
            getForecast(null)
        }
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        val searchBtn =
            autocompleteFragment.view?.findViewById<ImageView>(R.id.places_autocomplete_search_button)
        searchBtn?.drawable?.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(this, (R.color.white)),
            PorterDuff.Mode.SRC_IN
        );
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS
            )
        )
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
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
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }

    private fun getForecast(addressStr: String?) {
        if (!isConnected()) {
            val result = cacheHelper.getCachedResult()
            val fragment = FragmentForecast.newInstance(
                result?.list!!,
                result.address,
                DateHelper().fixTimestampLength(result.list[0].dt)
            )
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment)
                .addToBackStack("").commit()
            return
        }
        retrofitInterface = Common.retrofitInterface
        progress?.visibility = View.VISIBLE
        var call: Call<com.state_animations.mapsweatherforecast.model.Result>? = null
        if (addressStr != null) {
            call = retrofitInterface.getWeatherForecast(
                addressStr,
                "aeef079dba7b844f811a6321985d93ec"
            )
        } else {
            call = retrofitInterface.getWeatherForecast(
                latLng?.latitude.toString(),
                latLng?.longitude.toString(),
                "aeef079dba7b844f811a6321985d93ec"
            )
        }
        call.enqueue(object : Callback,
            retrofit2.Callback<com.state_animations.mapsweatherforecast.model.Result> {
            override fun onFailure(
                call: Call<com.state_animations.mapsweatherforecast.model.Result>,
                t: Throwable
            ) {
                progress?.visibility = View.GONE
                Log.e(TAG, "fail: " + t.localizedMessage)
            }

            override fun onResponse(
                call: Call<com.state_animations.mapsweatherforecast.model.Result>,
                response: Response<com.state_animations.mapsweatherforecast.model.Result>
            ) {
                Log.e(TAG, "onResponse: $response")
                progress?.visibility = View.GONE

                val result = response.body()
                val forecastList = result?.list
                val address = addressStr ?: getLocationString()
                result?.address = address

                Log.e(TAG, "forecast list: $forecastList")
                result?.let { cacheHelper.writeResultToFile(it) }
                forecastList?.let {
                    val fragment = FragmentForecast.newInstance(
                        it,
                        address,
                        DateHelper().fixTimestampLength(forecastList[0].dt)
                    )
                    supportFragmentManager.beginTransaction().replace(R.id.container, fragment)
                        .addToBackStack("").commit()
                }
            }

        })
    }

    @Throws(InterruptedException::class, IOException::class)
    fun isConnected(): Boolean {
        val command = "ping -c 1 google.com"
        return Runtime.getRuntime().exec(command).waitFor() == 0
    }


    fun getLocationString(): String {
        if (latLng != null) {
            val geocoder = Geocoder(this, Locale.ENGLISH)
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
        }
        return "US"
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private var latLng: LatLng? = null
    override fun onMapReady(map: GoogleMap?) {
        if (map != null) {
            mMap = map
            mMap.setOnMapClickListener {
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(it))
                latLng = it
            }
        }
    }

}