package com.example.midtermproject

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var weatherDetailsRecyclerView: RecyclerView
    private lateinit var weatherDetailsAdapter: WeatherDetailsAdapter
    private lateinit var hourlyWeatherRecyclerView: RecyclerView
    private lateinit var hourlyWeatherAdapter: HourlyWeatherAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up the RecyclerView for weather details
        weatherDetailsRecyclerView = findViewById(R.id.weather_details_recycler_view)
        val gridLayoutManager = GridLayoutManager(this, 2)
        weatherDetailsRecyclerView.layoutManager = gridLayoutManager

        // Sample weather data for weather details
        val weatherDetails = listOf(
            WeatherDetail(R.drawable.ic_wind, "Wind", "12 km/h"),
            WeatherDetail(R.drawable.ic_humidity, "Humidity", "60%"),
            WeatherDetail(R.drawable.ic_rain, "Rain", "10%"),
            WeatherDetail(R.drawable.ic_uv, "UV Index", "5 (Moderate)")
        )

        // Set the adapter for weather details
        weatherDetailsAdapter = WeatherDetailsAdapter(weatherDetails)
        weatherDetailsRecyclerView.adapter = weatherDetailsAdapter

        // Set up the RecyclerView for hourly weather
        hourlyWeatherRecyclerView = findViewById(R.id.hourly_weather_recycler_view)
        hourlyWeatherRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Sample hourly weather data
        val hourlyWeather = listOf(
            HourlyWeather("10 AM", "25°C", R.drawable.ic_sunny),
            HourlyWeather("11 AM", "26°C", R.drawable.ic_sunny),
            HourlyWeather("12 PM", "28°C", R.drawable.ic_sunny),
            HourlyWeather("1 PM", "30°C", R.drawable.ic_sunny),
            HourlyWeather("2 PM", "29°C", R.drawable.ic_rain),
            HourlyWeather("3 PM", "27°C", R.drawable.ic_sunny),
            HourlyWeather("4 PM", "27°C", R.drawable.ic_sunny),
            HourlyWeather("5 PM", "27°C", R.drawable.ic_sunny),
            HourlyWeather("6 PM", "27°C", R.drawable.ic_sunny),
            HourlyWeather("7 PM", "27°C", R.drawable.ic_sunny),
            HourlyWeather("8 PM", "27°C", R.drawable.ic_sunny),
            HourlyWeather("9 PM", "27°C", R.drawable.ic_sunny),
            HourlyWeather("10 PM", "27°C", R.drawable.ic_sunny)
        )

        // Set the adapter for hourly weather
        hourlyWeatherAdapter = HourlyWeatherAdapter(hourlyWeather)
        hourlyWeatherRecyclerView.adapter = hourlyWeatherAdapter

        // Set up the profile picture dropdown
        val profilePicture: ImageView = findViewById(R.id.profile_picture)
        profilePicture.setOnClickListener { view ->
            showProfileMenu(view)
        }
    }

    // Function to display the popup menu for the profile picture
    private fun showProfileMenu(anchor: View) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.logout -> {
                    // Perform logout action
                    logoutUser()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    // Function to handle user logout
    private fun logoutUser() {
        // Add your logout logic here
        finish() // Placeholder for now (closes the activity)
    }
}
