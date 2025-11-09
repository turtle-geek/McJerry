package com.example.myapplication.models;

public class User {
    int id;
    String name;
    String role;
    String email;
    String password;

    public User(int id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

}
