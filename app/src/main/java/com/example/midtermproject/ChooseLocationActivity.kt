package com.example.midtermproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

class ChooseLocationActivity : AppCompatActivity() {

    private lateinit var countrySpinner: Spinner
    private lateinit var selectedCountriesRecyclerView: RecyclerView
    private lateinit var selectedCountriesAdapter: SelectedCountriesAdapter
    private val selectedCountries = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_location)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Disable default title

        toolbar.setNavigationOnClickListener {
            super.onBackPressed()
            finish()
        }

        countrySpinner = findViewById(R.id.countrySpinner)
        selectedCountriesRecyclerView = findViewById(R.id.selectedCountriesRecyclerView)

        // Set up RecyclerView
        selectedCountriesAdapter = SelectedCountriesAdapter(selectedCountries) { country ->
            selectedCountries.remove(country)
            selectedCountriesAdapter.notifyDataSetChanged()
        }
        selectedCountriesRecyclerView.layoutManager = LinearLayoutManager(this)
        selectedCountriesRecyclerView.adapter = selectedCountriesAdapter

        // Fetch countries from API
        fetchCountries()
    }

    private fun fetchCountries() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://restcountries.com/v3.1/all")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.use { responseBody ->
                    val responseString = responseBody.string()
                    val countryListType = object : TypeToken<List<Country>>() {}.type
                    val countries: List<Country> = Gson().fromJson(responseString, countryListType)
                    val countryNames = countries.map { it.name.common }
                        .sorted()

                    runOnUiThread {
                        val adapter = ArrayAdapter(
                            this@ChooseLocationActivity,
                            android.R.layout.simple_spinner_item,
                            countryNames
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        countrySpinner.adapter = adapter

                        countrySpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View,
                                    position: Int,
                                    id: Long
                                ) {
                                    val selectedCountry = countryNames[position]

                                    // Navigate to MainActivity and pass the selected country
                                    if (selectedCountry.isNotEmpty()) {
                                        val intent = Intent(
                                            this@ChooseLocationActivity,
                                            MainActivity::class.java
                                        ).apply {
                                            putExtra("CITY_NAME", selectedCountry)
                                        }
                                        startActivity(intent)
                                        finish() // Optional: Close ChooseLocationActivity
                                    } else {
                                        Toast.makeText(
                                            this@ChooseLocationActivity,
                                            "Please select a country",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onNothingSelected(parent: AdapterView<*>) {
                                    // Do nothing
                                }
                            }
                    }
                }
            }
        })
    }
}

class SelectedCountriesAdapter(
    private val countries: List<String>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<SelectedCountriesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val countryName: TextView = view.findViewById(R.id.countryName)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val country = countries[position]
        holder.countryName.text = country
        holder.deleteButton.setOnClickListener {
            onDeleteClick(country)
        }
    }

    override fun getItemCount(): Int = countries.size
}

data class Country(val name: Name)
data class Name(val common: String)
