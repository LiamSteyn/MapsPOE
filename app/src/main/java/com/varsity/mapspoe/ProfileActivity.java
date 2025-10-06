package com.varsity.mapspoe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import com.varsity.mapspoe.data.legacy.User;
import com.varsity.mapspoe.data.DataRepository;


import java.util.List;

public class ProfileActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Get logged-in user email passed via Intent
        String userEmail = getIntent().getStringExtra("userEmail");

        // Get list of users from DataRepository Kotlin object
        List<User> users = DataRepository.INSTANCE.getUsers();

        User user = null;
        if (users != null && !users.isEmpty()) {
            for (User u : users) {
                if (u.getEmail().equals(userEmail)) {
                    user = u;
                    break;
                }
            }
            if (user == null) {
                user = users.get(0);
            }
        }


        // Initialize TextViews
        TextView userNameText = findViewById(R.id.userNameText);
        TextView userEmailText = findViewById(R.id.userEmailText);

        // Display user info
        if (user != null) {
            // Name = first 5 letters of email or full email if shorter
            String displayName = user.getEmail().length() >= 5
                    ? user.getEmail().substring(0, 5)
                    : user.getEmail();
            userNameText.setText("Name: " + displayName);
            userEmailText.setText("Email: " + user.getEmail());
        }

        // Switches for settings
        Switch locationSwitch = findViewById(R.id.locationSwitch);
        Switch biometricSwitch = findViewById(R.id.biometricSwitch);

        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Enable/disable location feature
        });

        biometricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Enable/disable biometric login
        });

        // Logout button
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });
    }
}
