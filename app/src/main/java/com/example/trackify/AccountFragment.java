package com.example.trackify;



import android.content.Intent;
import android.os.Bundle;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AccountFragment extends AppCompatActivity {

    private TextView userNameText, activeSinceText, dateJoinedText;
    private Button logoutButton;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_account);

        userNameText = findViewById(R.id.usernameText);
        dateJoinedText = findViewById(R.id.joinedDateText);
        logoutButton = findViewById(R.id.logoutButton);
        profileImage = findViewById(R.id.profileImage);
        activeSinceText = findViewById(R.id.activeSinceText);
        Button backButton = findViewById(R.id.backButton); // Replace with your actual button ID
        backButton.setOnClickListener(v -> {
            this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()) // Replace fragment_container with your layout ID
                    .addToBackStack(null) // Optional: Add this if you want to keep the AccountFragment in the backstack
                    .commit();
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class); // Replace HomeActivity with your target activity class
            startActivity(intent);
            this.finish(); // Optionally finish the current activity
        });
        // Load user data
        String username = SessionManager.getUserName(this); // Added 'this' context
        String joinedDate = SessionManager.getJoinedDate(this); // Added 'this' context

        userNameText.setText(username != null ? username : "User");
        dateJoinedText.setText(joinedDate != null ? "Joined: " + joinedDate : "Joined: Unknown");

        if (joinedDate != null && !joinedDate.equals("Not Available")) {
            String activeSince = getTimeSinceJoined(joinedDate);
            activeSinceText.setText(activeSince);
        } else {
            activeSinceText.setText("Activity status unavailable");
        }

        logoutButton.setOnClickListener(v -> {
            SessionManager.logout(this); // Added 'this' context for logout
            Intent intent = new Intent(AccountFragment.this, loginactivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
            startActivity(intent);
            finish();
        });
    }

    private String getTimeSinceJoined(String joinedDateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date joinedDate = sdf.parse(joinedDateString);
            Date currentDate = new Date();

            long diffInMillis = currentDate.getTime() - joinedDate.getTime();
            long days = diffInMillis / (1000 * 60 * 60 * 24);

            if (days < 30) {
                return days + " days ago";
            } else if (days < 365) {
                return (days / 30) + " months ago";
            } else {
                return (days / 365) + " years ago";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}