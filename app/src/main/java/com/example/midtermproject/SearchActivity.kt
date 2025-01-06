package com.example.midtermproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchResultsText: TextView
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Initialize views
        searchEditText = findViewById(R.id.search_edit_text)
        backButton = findViewById(R.id.back_button)

        // Set listener for the search EditText
        searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }

        // Set back button listener
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun performSearch(query: String) {
        // Here you can make an API call or query your local data
        // For demonstration, we will show dummy data
        val dummyData = listOf("City 1", "City 2", "City 3", "City 4")
        val filteredResults = dummyData.filter { it.contains(query, ignoreCase = true) }

        // Update the TextView with the search results
        if (filteredResults.isNotEmpty()) {
            searchResultsText.text = filteredResults.joinToString("\n")
        } else {
            searchResultsText.text = "No results found for \"$query\"."
        }
    }

    // Handle the Search button click
    fun onSearchClicked(view: View) {
        val cityName = searchEditText.text.toString()
        if (cityName.isNotEmpty()) {
            // Pass the city name back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("CITY_NAME", cityName)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()  // Go back to MainActivity
    }

    fun onBackPressed(view: View) {}
}
