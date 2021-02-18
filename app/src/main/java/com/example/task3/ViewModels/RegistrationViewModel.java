package com.example.task3.ViewModels;

import android.view.View;

import androidx.lifecycle.ViewModel;

import com.example.task3.DatabaseModels.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationViewModel extends ViewModel {

    private String PROFILE_KEY ="PROFILE";

    private DatabaseReference myRef;

    public void saveUser(String userId, User user){
        myRef = FirebaseDatabase.getInstance().getReference(PROFILE_KEY+"/"+userId);
        myRef.setValue(user);
    }
}
