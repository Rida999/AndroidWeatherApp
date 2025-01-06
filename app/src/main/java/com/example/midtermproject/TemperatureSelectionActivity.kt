package com.example.midtermproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.ImageButton
import android.widget.Toast

class TemperatureSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperature_selection)

        // Initialize the views
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val titleTextView: TextView = findViewById(R.id.titleTextView)
        val temperatureRadioGroup: RadioGroup = findViewById(R.id.temperatureRadioGroup)
        val saveButton: Button = findViewById(R.id.saveButton)
        val backButton: ImageButton = findViewById(R.id.back_button)

        // Set the toolbar as the action bar
        setSupportActionBar(toolbar)

        // Optional: Set title dynamically if needed
        supportActionBar?.title = ""

        // Handle the back button click (if needed)
        backButton.setOnClickListener {
            onBackPressed() // Calls onBackPressed method to go back to the previous screen
        }

        // Handle save button click
        saveButton.setOnClickListener {
            // Get the selected temperature unit
            val selectedId = temperatureRadioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedRadioButton: RadioButton = findViewById(selectedId)
                val selectedTemperatureUnit = selectedRadioButton.text.toString()

                // Create an intent to pass the data back
                val resultIntent = Intent(this, MainActivity::class.java)

                resultIntent.putExtra("SELECTED_TEMPERATURE_UNIT", selectedTemperatureUnit)
                startActivity(resultIntent)
            }
        }
    }

    // Function to show toast messages
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}