package com.state_animations.mapsweatherforecast.model

class TempHelper{
    fun kelvinToCelsius(kelvin: Int): Int {
        return kelvin - 273
    }

    fun celsiusToKelvin(с: Int): Int {
        return с + 273
    }
}