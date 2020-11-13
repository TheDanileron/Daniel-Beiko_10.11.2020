package com.state_animations.mapsweatherforecast.gui.activity

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import com.state_animations.mapsweatherforecast.gui.fragments.FragmentForecast
import com.state_animations.mapsweatherforecast.model.ForecastViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var mMap: GoogleMap
    private lateinit var viewModel: ForecastViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(ForecastViewModel::class.java)
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
        viewModel.getForecastsLiveDate().observe(this, Observer {
            if(it != null){
                supportFragmentManager.beginTransaction().replace(R.id.container, FragmentForecast()).addToBackStack("FORECAST").commit()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun getForecast(addressStr: String?) {
        if(addressStr != null){
            viewModel.getForecast(addressStr)
        } else if(latLng != null) {
            viewModel.getForecast(latLng!!)
        }
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