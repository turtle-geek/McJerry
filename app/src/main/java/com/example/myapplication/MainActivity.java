package com.example.myapplication;

import android.content.Intent;
import android.widget.Toast;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.auth.LoginPage;
import com.example.myapplication.callbacks.RoleCallback;
import com.example.myapplication.ui.ChildHomeActivity;
import com.example.myapplication.ui.ParentHomeActivity;
import com.example.myapplication.ui.ProviderHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Check if user is logged in
        FirebaseUser currentUser = fAuth.getCurrentUser();

        if (currentUser == null) {
            // No user logged in, redirect to LoginPage
            Intent intent = new Intent(this, LoginPage.class);
            startActivity(intent);
            finish();
        } else {
            // User is logged in, check their role and redirect to appropriate page
            checkRole();
        }
    }
    private void checkRole(){
        FirebaseUser user = fAuth.getCurrentUser();
        //if user does not exist
        if(user == null){
            Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        // use users' id to find their own documents
        // if document exists, find users' role on it, then depending on users' role land them on specific home page
        //If system cannot operate, show failure messages
        DocumentReference userDoc = fStore.collection("users").document(userId);
                userDoc.get().addOnSuccessListener(documentInfo -> {
                    if(documentInfo.exists()){
                        String role = documentInfo.getString("role");
                        landonSpecificPage(role);
                    }
                    else{
                        Toast.makeText(this, "Cannot find user information", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(this, "Cannot process: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }

    // fetches the user role
    private void fetchUserRole(RoleCallback callback) {
        // These two lines are often already initialized in onCreate, but it's safer to ensure they are available here if not passed in.
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();

        FirebaseUser user = fAuth.getCurrentUser();
        if (user == null) {
            callback.onFailure("User is not logged in.");
            return;
        }

        String userId = user.getUid();

        // Reference the user document in Firestore
        DocumentReference userDoc = fStore.collection("users").document(userId);

        // Fetch the document and extract the role asynchronously
        userDoc.get().addOnSuccessListener(documentInfo -> {
                    if (documentInfo.exists()) {
                        String role = documentInfo.getString("role");
                        if (role != null) {
                            // SUCCESS: Pass the role to the onRoleFetched method of the callback
                            callback.onRoleFetched(role);
                        } else {
                            callback.onFailure("User document found, but 'role' field is missing.");
                        }
                    } else {
                        callback.onFailure("Cannot find user information in Firestore.");
                    }
                })
                .addOnFailureListener(exception -> {
                    // FAILURE: Pass the error message to the onFailure method of the callback
                    callback.onFailure("Firestore operation failed: " + exception.getMessage());
                });
    }
        //helper function: depends on users' role, land user on their specific page
        private void landonSpecificPage(String role){
            Intent intent;
            if(role.equals("child")){
                intent = new Intent(this, ChildHomeActivity.class);
            }
            else if(role.equals("parent")){
                intent = new Intent(this, ParentHomeActivity.class);
            }
            else if(role.equals("provider")){
                intent = new Intent(this, ProviderHomeActivity.class);
            }
            else{
                Toast.makeText(this, "Unknown Character", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(intent);
            finish();
        }
}