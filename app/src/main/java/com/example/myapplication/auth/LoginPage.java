package com.example.myapplication.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import jp.wasabeef.blurry.Blurry;

public class LoginPage extends AppCompatActivity {

    private TextInputEditText mailET, passwordET;
    private Button loginButton;
    private CheckBox rememberMeCheckBox;
    private TextView registerTextView;
    private FirebaseAuth fAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // Initialize Firebase Auth
        fAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize UI components
        TextInputLayout mailLayout = findViewById(R.id.mailLayoutLogin);
        TextInputLayout passwordLayout = findViewById(R.id.passwordLayoutLogin);

        mailET = findViewById(R.id.mailET);
        passwordET = findViewById(R.id.passwordET);
        loginButton = findViewById(R.id.loginButton);
        rememberMeCheckBox = findViewById(R.id.checkBox);
        registerTextView = findViewById(R.id.loginLayout);

        // Load saved credentials if "Remember Me" was checked
        loadSavedCredentials();

        // Apply blur effect to the login container
        ViewGroup loginContainer = findViewById(R.id.loginContainer);
        Blurry.with(this)
                .radius(25)              // Blur intensity (higher = more blur)
                .sampling(2)             // Down sampling (higher = faster but lower quality)
                .color(Color.argb(66, 255, 255, 255))  // White overlay with transparency
                .async()                 // Do it asynchronously for better performance
                .animate(500);           // Fade in animation (milliseconds)

        // Login button click listener
        loginButton.setOnClickListener(v -> loginUser());

        // Register text click listener
        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPage.this, Register.class);
            startActivity(intent);
        });
    }

    private void loadSavedCredentials() {
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER, false);

        if (rememberMe) {
            String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
            String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");

            mailET.setText(savedEmail);
            passwordET.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
        }
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (rememberMeCheckBox.isChecked()) {
            editor.putString(KEY_EMAIL, email);
            editor.putString(KEY_PASSWORD, password);
            editor.putBoolean(KEY_REMEMBER, true);
        } else {
            // Clear saved credentials if unchecked
            editor.remove(KEY_EMAIL);
            editor.remove(KEY_PASSWORD);
            editor.putBoolean(KEY_REMEMBER, false);
        }

        editor.apply();
    }

    private void loginUser() {
        String email = mailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(email)) {
            mailET.setError("Email is required");
            mailET.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordET.setError("Password is required");
            passwordET.requestFocus();
            return;
        }

        // Authenticate with Firebase
        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        Toast.makeText(LoginPage.this, "Login successful!", Toast.LENGTH_SHORT).show();

                        // Save credentials if "Remember Me" is checked
                        saveCredentials(email, password);

                        // Navigate to MainActivity (which will check role and redirect)
                        Intent intent = new Intent(LoginPage.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed
                        Toast.makeText(LoginPage.this, "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

}
