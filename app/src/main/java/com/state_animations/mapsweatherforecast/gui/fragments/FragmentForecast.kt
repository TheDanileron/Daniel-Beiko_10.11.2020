package com.state_animations.mapsweatherforecast.gui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.state_animations.mapsweatherforecast.R
import com.state_animations.mapsweatherforecast.model.*
import kotlinx.android.synthetic.main.fragment_forecast.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FragmentForecast : Fragment() {
    private lateinit var viewModel: ForecastViewModel
    private val progressStepSize = 3
    private val progressMax = 24
    private var hourInMillis: Long = 3600
    val format = SimpleDateFormat("EEE HH:mm", Locale.ENGLISH)
    lateinit var adapter: RecyclerAdapterDays
    private var timeSeekBar: SeekBar? = null
    private var addressTV: TextView? = null
    private var dateTV: TextView? = null
    private var windTV: TextView? = null
    private var humidityTV: TextView? = null
    private var tempTV: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forecast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(requireActivity()).get(ForecastViewModel::class.java)
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
            timeSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val newProgress = progress.div(progressStepSize).times(progressStepSize)
                    timeSeekBar?.progress = newProgress
                    val newTimestampSeconds = viewModel.getStartTime() + (newProgress * hourInMillis)
                    getForecastByTimestamp(newTimestampSeconds)?.let {
                        viewModel.setCurrentForecast(it)
                    }
                    updateForecastInfo()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }

            })
        }
        adapter = RecyclerAdapterDays(getDays(), object : OnDaySelectedListener {
            override fun onSelected(day: Day, position: Int) {
                adapter.setSelected(selected = position)

                getForecastByTimestamp(day.timestamp)?.let {
                    viewModel.setCurrentForecast(it)
                    viewModel.setCurrentStartTime(it.dt)
                }

                timeSeekBar?.progress = 0
                updateForecastInfo()
            }
        })
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerDays.layoutManager = layoutManager
        recyclerDays.adapter = adapter
        addressTV?.text = viewModel.getAddress()
        updateForecastInfo()
    }

    fun getDays(): MutableList<Day> {
        val days = ArrayList<Day>()
        if (viewModel.getForecasts()?.get(0) != null) {
            days.add(
                Day(
                    viewModel.getForecasts()!![0].dt,
                    viewModel.getForecasts()!![0]
                )
            )
        }
        val timeInSeconds = System.currentTimeMillis() / 1000
        val currentDayTimestamp: Long = (timeInSeconds / 86400) * 86400
        var increment = 86400
        for (i in 1..4) {
            val timestamp = currentDayTimestamp + increment
            days.add(
                Day(
                    timestamp,
                    getForecastByTimestamp(timestamp)
                )
            )
            increment += 86400
        }

        return days
    }

    private fun updateForecastInfo() {
        dateTV?.text =
            format.format(DateHelper().fixTimestampLength(viewModel.getCurrentForecast()?.dt!!))
        tempTV?.text = getString(
            R.string.temp,
            TempHelper().kelvinToCelsius((viewModel.getCurrentForecast()!!.main["temp"] as Double).toInt())
                .toString()
        )
        windTV?.text =
            getString(R.string.wind, viewModel.getCurrentForecast()!!.wind["speed"].toString())
        humidityTV?.text = getString(
            R.string.humidity,
            viewModel.getCurrentForecast()!!.main["humidity"].toString()
        )
    }

    private fun getForecastByTimestamp(timestampSeconds: Long): Forecast? {
        viewModel.getForecasts()?.forEach {
            if (it.dt == timestampSeconds) {
                return it
            }
        }
        return viewModel.getCurrentForecast()
    }

    interface OnDaySelectedListener {
        fun onSelected(day: Day, position: Int);
    }

    class RecyclerAdapterDays(
        var daysList: MutableList<Day>?,
        var onDayClick: OnDaySelectedListener
    ) : RecyclerView.Adapter<DayViewHolder>() {
        private val daysCount = daysList?.size ?: 0
        private var selected = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            return DayViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.view_holder_day, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return daysCount
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            daysList?.get(position)?.let { holder.bind(it, selected == position) }
            daysList?.get(position)?.let { holder.setClickListener(onDayClick, position) }
        }

        fun setSelected(selected: Int) {
            val prevIndex = this.selected
            this.selected = selected
            notifyItemChanged(selected)
            notifyItemChanged(prevIndex)
        }
    }

    class DayViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val format: SimpleDateFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
        private var dayTV: TextView = view.findViewById(R.id.tvWeekDay)
        private var dayTemp: TextView = view.findViewById(R.id.tvDayTemp)
        private var day: Day? = null

        fun bind(day: Day, isSelected: Boolean) {
            this.day = day
            dayTV.text = (format.format(DateHelper().fixTimestampLength(day.timestamp)))
            dayTemp.text =
                TempHelper().kelvinToCelsius((day.forecast?.main?.get("temp") as Double).toInt())
                    .toString()
            if (isSelected) {
                view.background =
                    ContextCompat.getDrawable(view.context, R.drawable.selected_background)
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