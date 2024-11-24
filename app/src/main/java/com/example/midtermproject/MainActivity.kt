package com.example.weatherapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.midtermproject.R

class MainActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var emailError: TextView
    private lateinit var passwordError: TextView
    private lateinit var loginButton: Button
    private lateinit var signUpLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        emailError = findViewById(R.id.emailError)
        passwordError = findViewById(R.id.passwordError)
        loginButton = findViewById(R.id.loginButton)
        signUpLink = findViewById(R.id.signUpLink)

        // Login Button Click Listener
        loginButton.setOnClickListener {
            if (validateInputs()) {
                // Logic for login (Placeholder for now)
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            }
        }

        // Sign Up Link Click Listener (Just a placeholder for now)
        signUpLink.setOnClickListener {
            Toast.makeText(this, "Navigating to Sign Up page", Toast.LENGTH_SHORT).show()
            // Here you can implement the navigation to the SignUpActivity
        }
    }

    // Validate email and password inputs
    private fun validateInputs(): Boolean {
        var isValid = true

        // Email validation
        if (emailInput.text.isNullOrEmpty()) {
            emailError.visibility = View.VISIBLE
            isValid = false
        } else {
            emailError.visibility = View.GONE
        }

        // Password validation
        if (passwordInput.text.isNullOrEmpty()) {
            passwordError.visibility = View.VISIBLE
            isValid = false
        } else {
            passwordError.visibility = View.GONE
        }

        return isValid
    }
}
