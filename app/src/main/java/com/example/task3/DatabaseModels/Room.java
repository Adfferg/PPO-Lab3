package com.example.task3.DatabaseModels;

import com.example.task3.Game.GameState;

public class Room  {
    public String roomId;

    public String roomName;

    public String roomPassword;

    public String hostId;

    public String secondPlayerId;

    public boolean isAvailable;

    public GameState gameState;

    public boolean hostIsReady;

    public boolean secondPlayerIsReady;


    public Room() {

    }

    public Room(String roomId,String roomName, String roomPassword, String hostId, String secondPlayerId, boolean isAvailable, GameState gameState,
                boolean hostIsReady,boolean secondPlayerIsReady) {

        this.roomId = roomId;
        this.roomName =roomName;
        this.roomPassword = roomPassword;
        this.hostId = hostId;
        this.secondPlayerId = secondPlayerId;
        this.isAvailable = isAvailable;
        this.gameState = gameState;
        this.hostIsReady= hostIsReady;
        this.secondPlayerIsReady = secondPlayerIsReady;
    }
}
