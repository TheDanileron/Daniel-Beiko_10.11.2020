package com.state_animations.mapsweatherforecast

import android.app.Application
import com.google.android.libraries.places.api.Places

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Places.initialize(applicationContext, "AIzaSyB9Wg2xgkxkcLOYQjfD-pUQbYVQQvkbuqU")

        // Create a new PlacesClient instance
        val placesClient = Places.createClient(this)
    }
}