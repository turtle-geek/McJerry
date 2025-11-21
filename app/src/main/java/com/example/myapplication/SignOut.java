package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SignOut extends AppCompatActivity {
    private Button btnSignOut;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signout_page1);


        authManager = new AuthManager();

        btnSignOut = findViewById(R.id.button_logout);
        btnSignOut.setOnClickListener(v -> {
            authManager.signOut();
            navigateToLogin();
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginPage.class);
        startActivity(intent);
        finish();
    }
}