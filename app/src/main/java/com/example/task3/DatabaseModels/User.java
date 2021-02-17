package com.example.task3.DatabaseModels;

public class User {
    public String name;
    public int wins;
    public int loses;
    public String registrationTime;

    public User() {

    }

    public User(String name,int wins,int loses,String registrationTime) {
        this.name = name;
        this.wins = wins;
        this.loses = loses;
        this.registrationTime = registrationTime;
    }
}
