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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*

class ForecastActivity : AppCompatActivity() {

    private lateinit var forecastRecyclerView: RecyclerView
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var cityNameTextView: TextView
    private lateinit var loadingAnimationView: LottieAnimationView
    private lateinit var selectedUnit: String



    // Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Create an API service
    private val weatherApiService = retrofit.create(WeatherApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forecast)

        // Set up profile picture dropdown
        val profilePicture: ImageView = findViewById(R.id.profile_picture_forecast)
        profilePicture.setOnClickListener { view ->
            showProfileMenu(view)
        }

        loadingAnimationView = findViewById(R.id.animationView)
        cityNameTextView = findViewById(R.id.Country)
        // Initialize RecyclerView
        forecastRecyclerView = findViewById(R.id.forecast_recycler_view)
        forecastRecyclerView.layoutManager = LinearLayoutManager(this)
        forecastAdapter = ForecastAdapter(emptyList())
        forecastRecyclerView.adapter = forecastAdapter

        // Get the city name from the Intent (default to "Beirut" if not provided)
        val cityName = intent.getStringExtra("CITY_NAME") ?: "Beirut"
        selectedUnit = intent.getStringExtra("SELECTED_TEMPERATURE_UNIT") ?: "celsius"

        fetchForecastData(cityName,selectedUnit)
        cityNameTextView.text="$cityName";
    }

    private fun fetchForecastData(city: String, degree: String) {

        loadingAnimationView.visibility = View.VISIBLE

        // Use a coroutine to call the API asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiKey = "9a01a377a7698cdced82a1e9e1e43aec"
                val response = weatherApiService.getForecastByCity(city, apiKey)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val forecastList = processForecastData(response.body()!!.list,degree)
                        forecastAdapter.updateForecast(forecastList)
                        loadingAnimationView.visibility = View.GONE
                    } else {
                        loadingAnimationView.visibility = View.GONE
                        // Handle error
                        println("Failed to fetch data or empty response")
                    }
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
                loadingAnimationView.visibility = View.GONE
            }
        }
    }

    // Process the API response to aggregate 5 days of forecast data
    private fun processForecastData(forecastApiList: List<ForecastDetailApi>,degree: String): List<ForecastDetail> {
        val forecastList = mutableListOf<ForecastDetail>()
        val days = mutableMapOf<String, MutableList<ForecastDetailApi>>()

        // Group forecast data by day
        for (forecast in forecastApiList) {
            val day = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(forecast.dt * 1000))
            if (!days.containsKey(day)) {
                days[day] = mutableListOf()
            }
            days[day]?.add(forecast)
        }

        // Pick one forecast per day (e.g., take the first forecast of the day)
        for ((day, forecasts) in days) {
            val forecast = forecasts.first()
            val temp = if (degree=="fahrenheit"){
                val fahrenheit = ((forecast.main.temp - 273.15) * 9/5) + 32
                "${fahrenheit.toInt()}°F"            }
            else{
                "${(forecast.main.temp - 273.15).toInt()}°C"
            }
            forecastList.add(
                ForecastDetail(
                    day = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(forecast.dt * 1000)),
                    condition = forecast.weather.first().description,
                    temperature = "${temp}"
                )
            )
        }

        return forecastList.take(5) // Limit to 5 days
    }

    // Drop down button
    private fun showProfileMenu(anchor: View) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.profile_forecast_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.Forecast -> {
                    // Navigate to ForecastActivity
                    val cityName = cityNameTextView.text.toString() // Get the current city name
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("CITY_NAME", cityName) // Pass the city name
                    intent.putExtra("SELECTED_TEMPERATURE_UNIT", selectedUnit) // Pass the city name
                    startActivity(intent)
                    true
                }
                R.id.logout -> {
                    // Handle logout
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


    // Retrofit API Service
    interface WeatherApiService {
        @GET("data/2.5/forecast")
        suspend fun getForecastByCity(
            @Query("q") city: String,
            @Query("appid") apiKey: String
        ): retrofit2.Response<ForecastResponse>
    }

    // Data Models
    data class ForecastResponse(
        val list: List<ForecastDetailApi>,
    )

    data class ForecastDetailApi(
        val dt: Long,
        val weather: List<Weather>,
        val main: Main,
    )

    data class Weather(
        val description: String,
        val icon: String,
    )

    data class Main(
        val temp: Float,
    )

    // Data Model for RecyclerView
    data class ForecastDetail(
        val day: String,
        val condition: String,
        val temperature: String
    )


    // Adapter for RecyclerView
    class ForecastAdapter(private var forecastList: List<ForecastDetail>) :
        RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forecast, parent, false)
            return ForecastViewHolder(view)
        }

        override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
            val forecast = forecastList[position]
            holder.bind(forecast)
        }

        override fun getItemCount(): Int = forecastList.size

        fun updateForecast(newForecastList: List<ForecastDetail>) {
            forecastList = newForecastList
            notifyDataSetChanged()
        }

        class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val dayTextView: TextView = itemView.findViewById(R.id.forecast_day)
            private val iconImageView: ImageView = itemView.findViewById(R.id.forecast_icon)
            private val conditionTextView: TextView = itemView.findViewById(R.id.forecast_condition)
            private val tempTextView: TextView = itemView.findViewById(R.id.forecast_temperature)

            fun bind(forecast: ForecastDetail) {
                dayTextView.text = forecast.day
                iconImageView.setImageResource(getWeatherIcon(forecast.condition)) // Use condition here
                conditionTextView.text = forecast.condition
                tempTextView.text = forecast.temperature
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

            // New function to map condition to icon
            private fun getWeatherIcon(condition: String): Int {
                return when {
                    condition.contains("Clear", ignoreCase = true) -> R.drawable.ic_sunny
                    condition.contains("Rain", ignoreCase = true) -> R.drawable.ic_rain
                    condition.contains("Snow", ignoreCase = true) -> R.drawable.ic_snowy
                    condition.contains("Clouds", ignoreCase = true) -> R.drawable.ic_cloudy
                    else -> R.drawable.ic_sunny // Default icon if no condition matches
                }
            }
        }
    }
}
