package com.state_animations.mapsweatherforecast.app_data

import android.content.Context
import com.state_animations.mapsweatherforecast.model.Result
import java.io.*

class CacheHelper(val ctx: Context) {

    fun writeResultToFile( result: Result) {
        val fileName = ctx.cacheDir.path + "/" + "cached_forecast.dat"
        val f = File(fileName)
        if(f.exists()) {
            f.delete()
        }
        val oos = ObjectOutputStream(FileOutputStream(fileName))
        oos.writeObject(result)
    }

    fun getCachedResult(): Result? {
        val fileName = ctx.cacheDir.path + "/" + "cached_forecast.dat"
        if(File(fileName).exists()) {
            val ois = ObjectInputStream(FileInputStream(fileName))
            return ois.readObject() as Result
        } else {
            return null
        }
    }
}