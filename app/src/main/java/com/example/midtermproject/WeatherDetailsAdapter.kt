package com.example.midtermproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeatherDetailsAdapter(private var weatherDetails: List<WeatherDetail>) :
    RecyclerView.Adapter<WeatherDetailsAdapter.WeatherDetailsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherDetailsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather_detail, parent, false)
        return WeatherDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherDetailsViewHolder, position: Int) {
        val detail = weatherDetails[position]
        holder.icon.setImageResource(detail.iconResId)
        holder.title.text = detail.title
        holder.value.text = detail.value
    }

    override fun getItemCount(): Int = weatherDetails.size

    fun updateWeatherDetails(newDetails: List<WeatherDetail>) {
        weatherDetails = newDetails
        notifyDataSetChanged()
    }

    inner class WeatherDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.weather_detail_icon)
        val title: TextView = itemView.findViewById(R.id.weather_detail_title)
        val value: TextView = itemView.findViewById(R.id.weather_detail_value)
    }
}

