package com.example.midtermproject

import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
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
    private lateinit var weatherIcon: ImageView
    private lateinit var loadingAnimationView: LottieAnimationView
    private lateinit var AppBackground: androidx.constraintlayout.widget.ConstraintLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        loadingAnimationView = findViewById(R.id.animationView)

        // Initialize Views
        cityNameTextView = findViewById(R.id.country_name)
        currentTempTextView = findViewById(R.id.temperature)
        weatherIcon = findViewById(R.id.weather_icon)
        AppBackground=findViewById(R.id.main)

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
        val cityName = intent.getStringExtra("CITY_NAME") ?: "Beirut"
        fetchWeatherData(cityName)

        // Set up the button to navigate to the search page
        val navButton: ImageView = findViewById(R.id.select_country_icon)
        navButton.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchWeatherData(city: String) {
        // Show the loading animation at the start
        loadingAnimationView.visibility = View.VISIBLE

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
                        val currentTemp = weatherData.list[0].main.temp
                        val weatherDetails = listOf(
                            WeatherDetail(R.drawable.ic_min_temp, "Min Temp", "${weatherData.list[0].main.temp_min}°C"),
                            WeatherDetail(R.drawable.ic_max_temp, "Max Temp", "${weatherData.list[0].main.temp_max}°C"),
                            WeatherDetail(R.drawable.ic_wind, "Wind", "${weatherData.list[0].wind.speed} m/s"),
                            WeatherDetail(R.drawable.ic_humidity, "Humidity", "${weatherData.list[0].main.humidity}%"),
                            WeatherDetail(R.drawable.ic_pressure, "Pressure", "${weatherData.list[0].main.pressure} hPa"),
                            WeatherDetail(R.drawable.ic_sunrise, "Sunrise", formatUnixTime(weatherData.city.sunrise))
                        )

                        val weatherCondition = weatherData.list[0].weather.firstOrNull()?.main ?: "Clear"
                        val currentWeatherIcon = getWeatherIcon(weatherCondition)
                        val currentAppBg = getAppBackground(weatherCondition)

                        val hourlyWeather = generateHourlyWeather(weatherData.list)

                        withContext(Dispatchers.Main) {
                            cityNameTextView.text = "$cityName, ${weatherData.city.country}"
                            currentTempTextView.text = "${currentTemp}°C"
                            weatherIcon.setImageResource(currentWeatherIcon)
                            weatherDetailsAdapter.updateWeatherDetails(weatherDetails)
                            hourlyWeatherAdapter.updateHourlyWeather(hourlyWeather)
                            AppBackground.setBackgroundResource(currentAppBg)

                            // Hide the loading animation after data is fetched
                            loadingAnimationView.visibility = View.GONE
                        }
                    }
                } else {
                    // Handle API failure (non-200 response)
                    withContext(Dispatchers.Main) {
                        showToast("Failed to fetch weather data")
                        loadingAnimationView.visibility = View.GONE // Hide loading animation on failure
                    }
                }
            } catch (e: Exception) {
                // Handle network failure or any other exception
                withContext(Dispatchers.Main) {
                    showToast("Error: ${e.message}")
                    loadingAnimationView.visibility = View.GONE // Hide loading animation on error
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
            val weatherCondition = forecast.weather.firstOrNull()?.main ?: "Clear" // Default to "Clear" if no condition is found
            val iconResId = getWeatherIcon(weatherCondition)

            HourlyWeather(time, "${temp.toInt()}°C", iconResId)
        }
    }

    private fun getWeatherIcon(weatherCondition: String): Int {
        return when (weatherCondition) {
            "Clear" -> R.drawable.ic_sunny
            "Rain" -> R.drawable.ic_rain
            "Snow" -> R.drawable.ic_snowy
            "Clouds" -> R.drawable.ic_cloudy
            else -> R.drawable.ic_sunny // Default to sunny if the condition is not recognized
        }
    }

    private fun getAppBackground(weatherCondition: String): Int {
        return when (weatherCondition) {
            "Clear" -> R.drawable.sunny_bg
            "Rain" -> R.drawable.rainy_bg
            "Snow" -> R.drawable.snow_bg
            "Clouds" -> R.drawable.cloudy_bg
            else -> R.drawable.sunny_bg // Default to sunny if the condition is not recognized
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
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()  // Close the current activity if you no longer need it
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

    data class Weather(val description: String, val main: String)

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
