package com.example.task3.DatabaseModels;

import com.example.task3.Game.Result;

public class Statistic {

    public String roomId;
    public String blackId;
    public String blackName;
    public String whiteId;
    public String whiteName;
    public Result result;

    public Statistic() {

    }

    public Statistic(String roomId, String blackId, String blackName, String whiteId, String whiteName, Result result) {
        this.roomId = roomId;
        this.blackId = blackId;
        this.blackName = blackName;
        this.whiteId = whiteId;
        this.whiteName = whiteName;
        this.result = result;
    }
}
