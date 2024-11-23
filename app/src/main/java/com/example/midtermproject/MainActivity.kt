package com.example.midtermproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var cityNameTextView: TextView
    private lateinit var currentTempTextView: TextView
    private lateinit var weatherDetailsRecyclerView: RecyclerView
    private lateinit var weatherDetailsAdapter: WeatherDetailsAdapter
    private lateinit var hourlyWeatherRecyclerView: RecyclerView
    private lateinit var hourlyWeatherAdapter: HourlyWeatherAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        // Initialize Views
        cityNameTextView = findViewById(R.id.country_name)
        currentTempTextView = findViewById(R.id.temperature)

        // Initialize weather details RecyclerView
        weatherDetailsRecyclerView = findViewById(R.id.weather_details_recycler_view)
        weatherDetailsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        weatherDetailsAdapter = WeatherDetailsAdapter(emptyList())
        weatherDetailsRecyclerView.adapter = weatherDetailsAdapter

        // Initialize hourly weather RecyclerView
        hourlyWeatherRecyclerView = findViewById(R.id.hourly_weather_recycler_view)
        hourlyWeatherRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        hourlyWeatherAdapter = HourlyWeatherAdapter(emptyList())
        hourlyWeatherRecyclerView.adapter = hourlyWeatherAdapter

        // Set up profile picture dropdown
        val profilePicture: ImageView = findViewById(R.id.profile_picture)
        profilePicture.setOnClickListener { view ->
            showProfileMenu(view)
        }

        // Fetch weather data
        fetchWeatherData("Beirut")
    }

    private fun fetchWeatherData(city: String) {
        val apiKey = "9a01a377a7698cdced82a1e9e1e43aec"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val weatherService = retrofit.create(WeatherService::class.java)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response: Response<WeatherResponse> = weatherService.getWeather(city, apiKey)
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    if (weatherData != null) {
                        val cityName = weatherData.city.name
                        val currentTemp = weatherData.list[0].main.temp // Get the temperature of the first forecast period
                        val weatherDetails = listOf(
                            WeatherDetail(R.drawable.ic_min_temp, "Min Temp", "${weatherData.list[0].main.temp_min}°C"),
                            WeatherDetail(R.drawable.ic_max_temp, "Max Temp", "${weatherData.list[0].main.temp_max}°C"),
                            WeatherDetail(R.drawable.ic_wind, "Wind", "${weatherData.list[0].wind.speed} m/s"),
                            WeatherDetail(R.drawable.ic_humidity, "Humidity", "${weatherData.list[0].main.humidity}%"),
                            WeatherDetail(R.drawable.ic_pressure, "Pressure", "${weatherData.list[0].main.pressure} hPa"),
                            WeatherDetail(R.drawable.ic_sunrise, "Sunrise", formatUnixTime(weatherData.city.sunrise))
                        )

                        // Generate hourly weather list from the API response
                        val hourlyWeather = generateHourlyWeather(weatherData.list)

                        withContext(Dispatchers.Main) {
                            cityNameTextView.text = "$cityName, ${weatherData.city.country}"
                            currentTempTextView.text = "${currentTemp}°C"
                            weatherDetailsAdapter.updateWeatherDetails(weatherDetails)
                            hourlyWeatherAdapter.updateHourlyWeather(hourlyWeather)
                        }
                    }
                } else {
                    showToast("Failed to fetch weather data")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error: ${e.message}")
                }
            }
        }
    }

    private fun generateHourlyWeather(forecastList: List<HourlyForecast>): List<HourlyWeather> {
        // Filter out duplicates based on time and get only the first 8 unique hours
        val uniqueHourlyWeather = forecastList
            .distinctBy { formatUnixTime(it.dt) } // Remove duplicate times
            .take(8) // Limit to the first 8 hours

        return uniqueHourlyWeather.map { forecast ->
            val time = formatUnixTime(forecast.dt)
            val temp = forecast.main.temp
            HourlyWeather(time, "${temp.toInt()}°C", R.drawable.ic_sunny)
        }
    }

    private fun formatUnixTime(unixTime: Long): String {
        val date = Date(unixTime * 1000)
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(date)
    }

    private fun showProfileMenu(anchor: View) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.logout -> {
                    logoutUser()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun logoutUser() {
        finish() // Placeholder for now
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Retrofit Service and Response Classes
    interface WeatherService {
        @GET("forecast")
        suspend fun getWeather(
            @Query("q") city: String,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): Response<WeatherResponse>
    }

    data class WeatherResponse(
        val city: City,
        val list: List<HourlyForecast>
    )

    data class City(val name: String, val country: String, val sunrise: Long)

    data class HourlyForecast(
        val dt: Long,
        val main: Main,
        val weather: List<Weather>,
        val wind: Wind
    )

    data class Main(val temp: Float, val temp_min: Float, val temp_max: Float, val humidity: Int, val pressure: Int)

    data class Weather(val description: String)

    data class Wind(val speed: Float)

    // HourlyWeather model for RecyclerView
    data class HourlyWeather(val time: String, val temperature: String, val iconResId: Int)

    // Adapter for HourlyWeather
    class HourlyWeatherAdapter(private var hourlyWeatherList: List<HourlyWeather>) : RecyclerView.Adapter<HourlyWeatherAdapter.HourlyWeatherViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyWeatherViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hourly_weather, parent, false)
            return HourlyWeatherViewHolder(view)
        }

        override fun onBindViewHolder(holder: HourlyWeatherViewHolder, position: Int) {
            val hourlyWeather = hourlyWeatherList[position]
            holder.bind(hourlyWeather)
        }

        override fun getItemCount(): Int = hourlyWeatherList.size

        fun updateHourlyWeather(newHourlyWeatherList: List<HourlyWeather>) {
            hourlyWeatherList = newHourlyWeatherList
            notifyDataSetChanged()
        }

        class HourlyWeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val timeTextView: TextView = itemView.findViewById(R.id.hourly_weather_time)
            private val tempTextView: TextView = itemView.findViewById(R.id.hourly_weather_temperature)
            private val weatherIcon: ImageView = itemView.findViewById(R.id.hourly_weather_icon)

            fun bind(hourlyWeather: HourlyWeather) {
                timeTextView.text = hourlyWeather.time
                tempTextView.text = hourlyWeather.temperature
                weatherIcon.setImageResource(hourlyWeather.iconResId)
            }
        }
    }
}
