package com.example.midtermproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Signup : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var nameError: TextView
    private lateinit var phoneError: TextView
    private lateinit var emailError: TextView
    private lateinit var passwordError: TextView
    private lateinit var confirmPasswordError: TextView
    private lateinit var signupButton: Button
    private lateinit var signUpLink: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)


        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize Views
        nameInput = findViewById(R.id.nameInput)
        phoneInput = findViewById(R.id.phoneInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        nameError = findViewById(R.id.nameError)
        phoneError = findViewById(R.id.phoneError)
        emailError = findViewById(R.id.emailError)
        passwordError = findViewById(R.id.passwordError)
        confirmPasswordError = findViewById(R.id.confirmPasswordError)
        signupButton = findViewById(R.id.signupButton)
        signUpLink = findViewById(R.id.signUpLink)

        // Signup Button Click Listener
        signupButton.setOnClickListener {
            if (validateInputs()) {
                val email = emailInput.text.toString()
                val password = passwordInput.text.toString()
                val name = nameInput.text.toString()
                val phone = phoneInput.text.toString()

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userId = user?.uid
                            val userMap = mapOf(
                                "name" to name,
                                "phone" to phone,
                                "email" to email
                            )
                            userId?.let {
                                database.reference.child("users").child(it).setValue(userMap)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                                            finish()
                                        } else {
                                            Toast.makeText(this, "Database error: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        signUpLink.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    // Validate inputs
    private fun validateInputs(): Boolean {
        var isValid = true

        // Name validation
        if (nameInput.text.isNullOrEmpty()) {
            nameError.text = getString(R.string.name_required)
            nameError.visibility = View.VISIBLE
            isValid = false
        } else if (!nameInput.text.toString().matches(Regex("^[a-zA-Z]+$"))) {
            nameError.text = getString(R.string.invalid_name_format)
            nameError.visibility = View.VISIBLE
            isValid = false
        } else {
            nameError.visibility = View.GONE
        }

        // Phone validation
        if (phoneInput.text.isNullOrEmpty() || !phoneInput.text.toString().matches(Regex("^[0-9]+$"))) {
            phoneError.visibility = View.VISIBLE
            isValid = false
        } else {
            phoneError.visibility = View.GONE
        }

        // Email validation
        if (emailInput.text.isNullOrEmpty()) {
            emailError.text = getString(R.string.email_required)
            emailError.visibility = View.VISIBLE
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.text.toString()).matches()) {
            emailError.text = getString(R.string.invalid_email_format)
            emailError.visibility = View.VISIBLE
            isValid = false
        } else {
            emailError.visibility = View.GONE
        }

        // Password validation
        if (passwordInput.text.isNullOrEmpty()) {
            passwordError.text = getString(R.string.password_required)
            passwordError.visibility = View.VISIBLE
            isValid = false
        } else if (passwordInput.text.toString().length < 8) {
            passwordError.text = getString(R.string.small_password)
            passwordError.visibility = View.VISIBLE
            isValid = false
        } else if (!passwordInput.text.toString().matches(Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}$"))) {
            passwordError.text = getString(R.string.weak_password)
            passwordError.visibility = View.VISIBLE
            isValid = false
        } else {
            passwordError.visibility = View.GONE
        }

        // Confirm Password validation
        if (confirmPasswordInput.text.isNullOrEmpty()) {
            confirmPasswordError.text = getString(R.string.password_required)
            confirmPasswordError.visibility = View.VISIBLE
            isValid = false
        } else if (confirmPasswordInput.text.toString() != passwordInput.text.toString()) {
            confirmPasswordError.text = getString(R.string.confirm_password_required)
            confirmPasswordError.visibility = View.VISIBLE
            isValid = false
        } else {
            confirmPasswordError.visibility = View.GONE
        }

        return isValid
    }
}