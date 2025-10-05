package com.varsity.mapspoe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.varsity.mapspoe.data.DataRepository

class LoginActivity : Activity() {

    // Called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the layout of this activity to activity_login.xml
        setContentView(R.layout.activity_login)

        // Find references to input fields and buttons in the layout
        val email = findViewById<EditText>(R.id.emailInput)       // User email input
        val password = findViewById<EditText>(R.id.passwordInput) // User password input
        val loginButton = findViewById<Button>(R.id.loginButton)  // Button to log in
        val registerButton = findViewById<Button>(R.id.registerButton) // Button to go to registration

        // Set up click listener for login button
        loginButton.setOnClickListener {
            // Attempt login using email and password from the input fields
            val success = DataRepository.login(email.text.toString(), password.text.toString())

            if (success) {
                // Login successful → navigate to DashboardActivity
                startActivity(Intent(this, DashboardActivity::class.java))
                finish() // Close LoginActivity so user cannot go back to it
            } else {
                // Login failed → show a simple toast message
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up click listener for register button
        registerButton.setOnClickListener {
            // Navigate to RegisterActivity so the user can create an account
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
