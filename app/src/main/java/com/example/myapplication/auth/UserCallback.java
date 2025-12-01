package com.example.myapplication.auth;

import com.example.myapplication.models.User;

public interface UserCallback {
    void onUserFetched(User user);

}
