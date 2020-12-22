package com.state_animations.mapsweatherforecast.presentation.activity

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.state_animations.mapsweatherforecast.R
import com.state_animations.mapsweatherforecast.presentation.fragments.FragmentForecast
import com.state_animations.mapsweatherforecast.presentation.ForecastViewModel
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
            viewModel.getForecast(viewModel.getLatLng())
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
                viewModel.parseAddressAndGetForecast(place)
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })
        viewModel.getForecastsLiveData().observe(this, Observer {
            if(it != null && savedInstanceState == null){
                supportFragmentManager.beginTransaction().replace(R.id.container, FragmentForecast()).addToBackStack("FORECAST").commit()
            }
        })
        viewModel.getErrorLiveData().observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })
    }

    override fun onMapReady(map: GoogleMap?) {
        if (map != null) {
            mMap = map
            mMap.setOnMapClickListener {
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(it))
                viewModel.setLatLng(it)
            }
        }
    }

}