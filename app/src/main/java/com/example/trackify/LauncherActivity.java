package com.example.trackify;
 import androidx.appcompat.app.AppCompatActivity;
 import android.os.Bundle;
 import android.content.Intent;

public class LauncherActivity  extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, loginactivity.class);
        startActivity(intent);
        finish();
    }
}
