package com.example.task3.ViewModels;

import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileViewModel extends ViewModel {
    private DatabaseReference myRef;
    private String PROFILE_KEY = "PROFILE";

    public void setMyRef(String userId){
        myRef = FirebaseDatabase.getInstance().getReference(PROFILE_KEY + "/" + userId);
    }

    public void addListener(ValueEventListener listener){
        myRef.addValueEventListener(listener);
    }

    public void changeName(String name){
        myRef.child("name").setValue(name);
    }

    public void deleteData(){
        myRef.removeValue();
    }
}
