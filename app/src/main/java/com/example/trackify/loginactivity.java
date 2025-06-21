package com.example.trackify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class loginactivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText etUsername, etPassword;
    Button btnLogin, btnRegister;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this); // Initialize once

        // Check if session exists
        int userId = SessionManager.getUserId(this);
        if (userId != -1) {
            String username = SessionManager.getUserName(this);
            String joinedDate = dbHelper.getDateOfJoining(userId);
            String lastLogin = dbHelper.getLastLogin(userId);

            SessionManager.saveUserSession(this, userId, username, joinedDate, lastLogin);

            // User is logged in, go to MainActivity directly
            startActivity(new Intent(loginactivity.this, MainActivity.class));
            finish();
            return;
        }

        // Setup UI
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            } else {
                int loggedUserId = dbHelper.loginUser(username, password);
                if (loggedUserId != -1) {
                    String joinedDate = dbHelper.getDateOfJoining(loggedUserId);
                    String lastLogin = dbHelper.getLastLogin(loggedUserId);

                    SessionManager.saveUserSession(this, loggedUserId, username, joinedDate, lastLogin);

                    startActivity(new Intent(loginactivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Invalid credentials!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}
