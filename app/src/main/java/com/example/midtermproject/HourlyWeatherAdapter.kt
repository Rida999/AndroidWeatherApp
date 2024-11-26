package com.example.midtermproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HourlyWeatherAdapter(private var hourlyWeatherList: List<HourlyWeather>) :
    RecyclerView.Adapter<HourlyWeatherAdapter.HourlyWeatherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyWeatherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_weather, parent, false)
        return HourlyWeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyWeatherViewHolder, position: Int) {
        val hourlyWeather = hourlyWeatherList[position]
        holder.time.text = hourlyWeather.time
        holder.temperature.text = hourlyWeather.temperature
        holder.icon.setImageResource(hourlyWeather.iconResId)
    }

    override fun getItemCount(): Int = hourlyWeatherList.size

    // Update the list of hourly weather data and notify the adapter
    fun updateHourlyWeather(newHourlyWeatherList: List<HourlyWeather>) {
        hourlyWeatherList = newHourlyWeatherList
        notifyDataSetChanged()
    }

    inner class HourlyWeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val time: TextView = itemView.findViewById(R.id.hourly_weather_time)
        val temperature: TextView = itemView.findViewById(R.id.hourly_weather_temperature)
        val icon: ImageView = itemView.findViewById(R.id.hourly_weather_icon)
    }
}
