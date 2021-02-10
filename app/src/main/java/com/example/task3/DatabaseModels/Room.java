package com.example.task3.DatabaseModels;

public class Room  {
    public String name;
    public String roomId;
    public String creatorId;
    public String password;
    int isAvailable;

    public Room() {

    }

    public Room(String name,String roomId,String creatorId,String password, int isAvailable) {
        this.name = name;
        this.roomId = roomId;
        this.creatorId = creatorId;
        this.password = password;
        this.isAvailable = isAvailable;
    }
}
