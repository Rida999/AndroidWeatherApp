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

class ResetPassword : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var resetPasswordButton: Button
    private lateinit var loginLink: TextView
    private lateinit var emailError: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reset_password)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Views
        emailInput = findViewById(R.id.emailInput)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)
        loginLink = findViewById(R.id.loginLink)
        emailError = findViewById(R.id.emailError)

        // Reset Password Button Click Listener
        resetPasswordButton.setOnClickListener {
            if (validateInput()) {
                val email = emailInput.text.toString()
                checkEmailExists(email)
            }
        }

        // Login Link Click Listener
        loginLink.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun validateInput(): Boolean {
        return if (emailInput.text.isNullOrEmpty()) {
            emailError.visibility = View.VISIBLE
            false
        } else {
            emailError.visibility = View.GONE
            true
        }
    }

    private fun checkEmailExists(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset link sent to your email!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val error = task.exception?.message
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                }
            }
    }
}