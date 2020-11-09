package com.state_animations.mapsweatherforecast.gui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.state_animations.mapsweatherforecast.R
import com.state_animations.mapsweatherforecast.model.DateHelper
import com.state_animations.mapsweatherforecast.model.Day
import com.state_animations.mapsweatherforecast.model.Forecast
import com.state_animations.mapsweatherforecast.model.TempHelper
import kotlinx.android.synthetic.main.fragment_forecast.*
import kotlinx.android.synthetic.main.fragment_forecast.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FragmentForecast : Fragment() {
    private var forecastList: MutableList<Forecast>? = null
    private val progressStepSize = 3
    private val progressMax = 24
    private lateinit var address:String
    private var startTimestamp:Long = 0
    private var hourInMillis:Long = 3600000
    val format = SimpleDateFormat("EEE HH:mm", Locale.ENGLISH)
    private var currentForecast: Forecast? = null
    lateinit var adapter: RecyclerAdapterDays
    private var timeSeekBar: SeekBar? = null
    private var addressTV: TextView? = null
    private var dateTV: TextView? = null
    private var windTV: TextView? = null
    private var humidityTV: TextView? = null
    private var tempTV: TextView? = null

    companion object{
        fun newInstance(list: MutableList<Forecast>, address: String, timestamp: Long) : FragmentForecast{
            val fragment = FragmentForecast()
            fragment.forecastList = list
            fragment.address = address
            fragment.startTimestamp = timestamp
            fragment.currentForecast = list[0]
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forecast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        retainInstance = true
        super.onViewCreated(view, savedInstanceState)
        timeSeekBar = view.findViewById(R.id.timeSeekBar)
        addressTV = view.findViewById(R.id.addressTV)
        dateTV = view.findViewById(R.id.dateTV)
        humidityTV = view.findViewById(R.id.humidityTV)
        tempTV = view.findViewById(R.id.tempTV)
        windTV = view.findViewById(R.id.windTV)
        timeSeekBar?.post {
            timeSeekBar?.incrementProgressBy(progressStepSize)
            timeSeekBar?.max = progressMax
            timeSeekBar?.progress = 0
            timeSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val newProgress = progress.div(progressStepSize).times(progressStepSize)
                    timeSeekBar?.progress = newProgress
                    val newTimestamp = startTimestamp + (newProgress * hourInMillis)
                    currentForecast = getForecastByTimestamp(newTimestamp)
                    updateForecastInfo()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }

            })
        }
        adapter = RecyclerAdapterDays(getDays(), object : OnDaySelectedListener{
            override fun onSelected(day: Day, position: Int) {
                adapter.setSelected(selected = position)
                if(position == 0){
                    currentForecast = forecastList?.get(0)
                } else {
                    currentForecast = getForecastByTimestamp(day.timestamp)
                }
                timeSeekBar?.progress = 0
                updateForecastInfo()
            }
        })
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerDays.layoutManager = layoutManager
        recyclerDays.adapter = adapter
        addressTV?.text = address
        updateForecastInfo()
    }

    fun getDays():MutableList<Day> {
        val days = ArrayList<Day>()
        if(forecastList?.get(0) != null){
            days.add(Day(forecastList!![0].dt, forecastList!![0]))
        }
        val timeInSeconds = System.currentTimeMillis() / 1000
        val currentDayTimestamp: Long = (timeInSeconds / 86400) * 86400
        var increment = 86400
        for(i in 1..4) {
            val timestamp = currentDayTimestamp + increment
            days.add(Day(DateHelper().fixTimestampLength(timestamp), getForecastByTimestamp(DateHelper().fixTimestampLength(timestamp))))
            increment += 86400
        }

        return days
    }

    private fun updateForecastInfo() {
        dateTV?.text = format.format(DateHelper().fixTimestampLength(currentForecast?.dt!!))
        tempTV?.text = getString(R.string.temp, TempHelper().kelvinToCelsius((currentForecast!!.main["temp"] as Double).toInt()).toString())
        windTV?.text = getString(R.string.wind, currentForecast!!.wind["speed"].toString())
        humidityTV?.text = getString(R.string.humidity, currentForecast!!.main["humidity"].toString())
    }

    private fun getForecastByTimestamp(timestamp: Long):Forecast? {
        forecastList?.forEach {
            if(DateHelper().fixTimestampLength(it.dt) == timestamp){
                currentForecast = it
            }
        }
        return currentForecast;
    }

    interface OnDaySelectedListener {
        fun onSelected(day: Day, position: Int);
    }

    class RecyclerAdapterDays(var daysList: MutableList<Day>?, var onDayClick: OnDaySelectedListener) : RecyclerView.Adapter<DayViewHolder>() {
        private val daysCount = daysList?.size ?: 0
        private var selected = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            return DayViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_day, parent, false))
        }

        override fun getItemCount(): Int {
            return daysCount
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            daysList?.get(position)?.let { holder.bind(it, selected == position) }
            daysList?.get(position)?.let { holder.setClickListener(onDayClick, position) }
        }

        fun setSelected(selected: Int) {
            val prevIndex= this.selected
            this.selected = selected
            notifyItemChanged(selected)
            notifyItemChanged(prevIndex)
        }
    }

    class DayViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        private val format: SimpleDateFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
        private var dayTV: TextView = view.findViewById(R.id.tvWeekDay)
        private var dayTemp: TextView = view.findViewById(R.id.tvDayTemp)
        private var day: Day? = null

        fun bind (day: Day, isSelected: Boolean) {
            this.day = day
            dayTV.text = (format.format(day.timestamp))
            dayTemp.text = TempHelper().kelvinToCelsius((day.forecast?.main?.get("temp") as Double).toInt()).toString()
            if(isSelected){
                view.background = ContextCompat.getDrawable(view.context, R.drawable.selected_background)
            } else {
                view.background = null
            }
        }

        fun setClickListener(onDayClick: OnDaySelectedListener, position: Int) {
            view.setOnClickListener { v ->
                day?.let { onDayClick.onSelected(it, position) }
            }
        }
    }
}