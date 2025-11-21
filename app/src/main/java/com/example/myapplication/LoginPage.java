package com.example.myapplication;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import jp.wasabeef.blurry.Blurry;

public class LoginPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // Initialize text fields
        TextInputLayout mailLayout = findViewById(R.id.mailLayoutLogin);
        TextInputLayout passwordLayout = findViewById(R.id.passwordLayoutLogin);

        TextInputEditText mailET = findViewById(R.id.mailET);
        TextInputEditText passwordET = findViewById(R.id.passwordET);

        // Apply blur effect to the login container
        ViewGroup loginContainer = findViewById(R.id.loginContainerLogin);
        Blurry.with(this)
                .radius(25)              // Blur intensity (higher = more blur)
                .sampling(2)             // Down sampling (higher = faster but lower quality)
                .color(Color.argb(66, 255, 255, 255))  // White overlay with transparency
                .async()                 // Do it asynchronously for better performance
                .animate(500);           // Fade in animation (milliseconds)
    }
}
