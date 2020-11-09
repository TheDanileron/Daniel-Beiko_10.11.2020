package com.state_animations.mapsweatherforecast.model

class DateHelper{
    val TIMESTAMP_LENGTH = 13

    fun fixTimestampLength(timestamp: Long): Long {
        var newTimestamp = timestamp
        while (newTimestamp.toString().length < TIMESTAMP_LENGTH){
            newTimestamp = newTimestamp * 10
        }
        while (newTimestamp.toString().length > TIMESTAMP_LENGTH){
            newTimestamp = newTimestamp / 10
        }
        return newTimestamp
    }
}