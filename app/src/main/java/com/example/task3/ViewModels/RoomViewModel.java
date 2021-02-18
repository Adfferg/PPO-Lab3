package com.example.task3.ViewModels;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.ViewModel;

import com.example.task3.DatabaseModels.Room;
import com.example.task3.GameActivity;
import com.example.task3.RoomsActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoomViewModel extends ViewModel {
    private String ROOM_KEY = "ROOMS";

    private DatabaseReference myRef= FirebaseDatabase.getInstance().getReference(ROOM_KEY);

    public void myRefListener(String roomId, Room room, Context context){
        myRef.child(roomId).setValue(room);
    }

    public void myRefAddListener(ValueEventListener listener){
        myRef.addValueEventListener(listener);
    }
}
