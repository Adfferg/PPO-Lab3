package com.example.task3.DatabaseModels;

public class User {
    public String name;
    public int wins;
    public int loses;

    public User() {

    }

    public User(String name,int wins,int loses) {
        this.name = name;
        this.wins = wins;
        this.loses = loses;

    }
}
