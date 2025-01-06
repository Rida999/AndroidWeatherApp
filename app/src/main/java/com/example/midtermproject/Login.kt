package com.example.midtermproject

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var emailError: TextView
    private lateinit var passwordError: TextView
    private lateinit var loginButton: Button
    private lateinit var signUpLink: TextView
    private lateinit var changePasswordLink: TextView
    private lateinit var resetPasswordLink: TextView

    private lateinit var auth: FirebaseAuth

    private var failedAttempts = 0
    private var lockoutTime = 0L
    private val lockoutDuration = 60000L  // 1 minute lockout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Views
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        emailError = findViewById(R.id.emailError)
        passwordError = findViewById(R.id.passwordError)
        loginButton = findViewById(R.id.loginButton)
        signUpLink = findViewById(R.id.signUpLink)
        changePasswordLink = findViewById(R.id.changePasswordLink)
        resetPasswordLink = findViewById(R.id.resetPasswordLink)

        // Login Button Click Listener
        loginButton.setOnClickListener {
            // Check if the user is currently locked out
            if (System.currentTimeMillis() < lockoutTime) {
                Toast.makeText(this, "Too many failed attempts. Please try again later.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (validateInputs()) {
                val email = emailInput.text.toString()
                val password = passwordInput.text.toString()

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Reset failed attempts and proceed to the next activity
                            failedAttempts = 0
                            lockoutTime = 0  // Remove lockout
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, SearchActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            failedAttempts++
                            if (failedAttempts >= 5) {
                                // Lock the user out for 1 minute
                                lockoutTime = System.currentTimeMillis() + lockoutDuration
                                Toast.makeText(this, "Too many failed attempts. You are locked out for 1 minute.", Toast.LENGTH_SHORT).show()
                            } else {
                                // Show remaining attempts
                                val remainingAttempts = 5 - failedAttempts
                                Toast.makeText(this, "Invalid credentials. You have $remainingAttempts attempts left.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }

        // Sign Up Link Click Listener
        signUpLink.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }

        changePasswordLink.setOnClickListener {
            val intent = Intent(this, ChangePassword::class.java)
            startActivity(intent)
        }

        resetPasswordLink.setOnClickListener {
            val intent = Intent(this, ResetPassword::class.java)
            startActivity(intent)
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