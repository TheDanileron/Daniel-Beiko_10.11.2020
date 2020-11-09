package com.state_animations.mapsweatherforecast.model

class TempHelper{
    fun kelvinToCelsius(kelvin: Int): Int {
        return (kelvin / 10 * 10) - 273
    }

    fun celsiusToKelvin(с: Int): Int {
        return (с / 10 * 10) + 273
    }
}