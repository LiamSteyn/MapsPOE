package com.varsity.mapspoe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.varsity.mapspoe.data.DataRepository
import com.varsity.mapspoe.data.User

class RegisterActivity : Activity() {

    // Called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the layout of this activity to activity_register.xml
        setContentView(R.layout.activity_register)

        // Find references to the input fields and button in the layout
        val nameInput = findViewById<EditText>(R.id.nameInput)       // User's name input
        val emailInput = findViewById<EditText>(R.id.emailInput)     // User's email input
        val passwordInput = findViewById<EditText>(R.id.passwordInput) // User's password input
        val registerButton = findViewById<Button>(R.id.registerButton) // Button to submit registration

        // Set click listener for the register button
        registerButton.setOnClickListener {
            // Get text from input fields and trim whitespace
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Validate inputs: check if any field is empty
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop execution if validation fails
            }

            // Create a new User object with the input data
            val newUser = User(name, email, password)

            // Attempt to register the user using DataRepository
            val registered = DataRepository.registerUser(newUser)

            if (registered) {
                // Registration successful → show toast and navigate to LoginActivity
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish() // Close RegisterActivity
            } else {
                // Registration failed → user already exists
                Toast.makeText(this, "User already exists!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
