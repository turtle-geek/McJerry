package com.example.myapplication.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.example.myapplication.auth.SessionManager;
import com.example.myapplication.models.Child;
import com.example.myapplication.models.HealthProfile;
import com.example.myapplication.models.User;

public class BaseChildActivity extends AppCompatActivity {

    HealthProfile hp;
    Child currentChild;
    int peakFlowValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Checks if the current user is a child. If not, finish the activity.
     */
    void checkUserType(){
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (!(currentUser instanceof Child)){
            finish();
        } else {
            currentChild = (Child) currentUser;
            hp = currentChild.getHealthProfile();
        }
    }
}