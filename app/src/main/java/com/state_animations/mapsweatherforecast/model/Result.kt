package com.state_animations.mapsweatherforecast.model

import java.io.Serializable

data class Result (
    var address: String,
    val cod: String,
    val message: String,
    val list: MutableList<Forecast>
) : Serializable