package com.example.midtermproject

import android.os.Bundle
import android.view.MenuItem
import android.view.View
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
        // Fetch weather data with sleep to load API
        fetchWeatherData("Beirut")
        Thread.sleep(2000)

        setContentView(R.layout.activity_main)

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                        val country = weatherData.sys.country
                        val currentTemp = weatherData.main.temp // Fetch real temperature
                        val location = "$country, $city"

                        val weatherDetails = listOf(
                            WeatherDetail(R.drawable.ic_min_temp, "Min Temp", "${weatherData.main.temp_min}°C"),
                            WeatherDetail(R.drawable.ic_max_temp, "Max Temp", "${weatherData.main.temp_max}°C"),
                            WeatherDetail(R.drawable.ic_wind, "Wind", "${weatherData.wind.speed} m/s"),
                            WeatherDetail(R.drawable.ic_humidity, "Humidity", "${weatherData.main.humidity}%"),
                            WeatherDetail(R.drawable.ic_pressure, "Pressure", "${weatherData.main.pressure} hPa"),
                            WeatherDetail(R.drawable.ic_sunrise, "Sunrise", formatUnixTime(weatherData.sys.sunrise))
                        )

                        val hourlyWeather = generateHourlyWeather()

                        withContext(Dispatchers.Main) {
                            cityNameTextView.text = location
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

    private fun generateHourlyWeather(): List<HourlyWeather> {
        return listOf(
            HourlyWeather("10 AM", "25°C", R.drawable.ic_sunny),
            HourlyWeather("11 AM", "26°C", R.drawable.ic_sunny),
            HourlyWeather("12 PM", "28°C", R.drawable.ic_sunny),
            HourlyWeather("1 PM", "30°C", R.drawable.ic_sunny)
        )
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
        @GET("weather")
        suspend fun getWeather(
            @Query("q") city: String,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): Response<WeatherResponse>
    }

    data class WeatherResponse(val main: Main, val wind: Wind, val sys: Sys)

    data class Main(val temp: Float, val temp_min: Float, val temp_max: Float, val humidity: Int, val pressure: Int)

    data class Wind(val speed: Float)

    data class Sys(val sunrise: Long, val country: String)
}
