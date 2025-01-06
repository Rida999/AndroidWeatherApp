package com.example.midtermproject

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ChangePassword : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var currentPasswordInput: EditText
    private lateinit var newPasswordInput: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var loginLink: TextView
    private lateinit var emailError: TextView
    private lateinit var currentPasswordError: TextView
    private lateinit var newPasswordError: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_password)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Views
        emailInput = findViewById(R.id.emailInput)
        currentPasswordInput = findViewById(R.id.currentPasswordInput)
        newPasswordInput = findViewById(R.id.newPasswordInput)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        loginLink = findViewById(R.id.loginLink)

        val emailError = findViewById<TextView>(R.id.emailError)
        val currentPasswordError = findViewById<TextView>(R.id.currentPasswordError)
        val newPasswordError = findViewById<TextView>(R.id.newPasswordError)

        // Change Password Button Click Listener
        changePasswordButton.setOnClickListener {
            if (validateInputs(emailError, currentPasswordError, newPasswordError)) {
                val email = emailInput.text.toString()
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()

                // Reauthenticate User
                auth.signInWithEmailAndPassword(email, currentPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Validate New Password Format
                            val passwordPattern =
                                Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}$")
                            if (newPassword.matches(passwordPattern)) {
                                // Update Password
                                auth.currentUser?.updatePassword(newPassword)
                                    ?.addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful) {
                                            Toast.makeText(
                                                this,
                                                "Password changed successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(this, Login::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Failed to change password. Try again.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(
                                    this,
                                    "New password format is invalid.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "Invalid email or current password.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        // Login Link Click Listener
        loginLink.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    // Validate inputs and show error messages
    private fun validateInputs(
        emailError: TextView,
        currentPasswordError: TextView,
        newPasswordError: TextView
    ): Boolean {
        var isValid = true

        // Email validation
        if (emailInput.text.isNullOrEmpty()) {
            emailError.visibility = View.VISIBLE
            isValid = false
        } else {
            emailError.visibility = View.GONE
        }

        // Current password validation
        if (currentPasswordInput.text.isNullOrEmpty()) {
            currentPasswordError.visibility = View.VISIBLE
            isValid = false
        } else {
            currentPasswordError.visibility = View.GONE
        }

        // New password validation
        if (newPasswordInput.text.isNullOrEmpty()) {
            newPasswordError.visibility = View.VISIBLE
            isValid = false
        } else {
            newPasswordError.visibility = View.GONE
        }

        return isValid
    }
}