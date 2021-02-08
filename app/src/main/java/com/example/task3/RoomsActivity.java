package com.example.task3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoomsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button createRoomButton;
    private DatabaseReference myRef;
    private FirebaseUser firebaseUser;

    private String PROFILE_KEY ="profile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rooms_activity);

        createRoomButton = findViewById(R.id.createRoomButton);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser= mAuth.getCurrentUser();
        myRef = FirebaseDatabase.getInstance().getReference(PROFILE_KEY+"/"+firebaseUser.getUid());
      //  getDataFromDb();



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.change_user) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId() == R.id.users_profile){
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
        }
        return true;
    }

    public void getDataFromDb(){
        ValueEventListener valueEventListener = new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    createRoomButton.setText(user.name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        myRef.addValueEventListener(valueEventListener);
    }
}
