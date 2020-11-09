package com.state_animations.mapsweatherforecast.model

import java.io.Serializable

data class Forecast(
    val dt: Long,
    val coord: Map<String, Float>,
    val main: Map<String, Any>,
    val visibility: Int,
    val wind: Map<String, Any>,
    val clouds: Map<String, Any>,
    val dt_txt: String
) : Serializable