package com.example.task3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.task3.Adapters.RoomsListViewAdapter;
import com.example.task3.DatabaseModels.Room;
import com.example.task3.Game.GameState;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.dd4you.appsconfig.DD4YouConfig;

public class RoomsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button createRoomButton;
    private DatabaseReference myRef;
    private FirebaseUser firebaseUser;
    private ListView roomsListView;
    private String ROOM_KEY = "ROOMS";
    private List<Room> roomList = new ArrayList<>();
    private RoomsListViewAdapter roomsListViewAdapter;
    //генерация id для комнаты
    private DD4YouConfig dd4YouConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rooms_activity);

        dd4YouConfig = new DD4YouConfig(this);
        createRoomButton = findViewById(R.id.createRoomButton);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        roomsListView = findViewById(R.id.roomsListView);
        myRef = FirebaseDatabase.getInstance().getReference(ROOM_KEY);
        roomsListViewAdapter = new RoomsListViewAdapter(RoomsActivity.this,
                R.layout.room_item, roomList);
        roomsListView.setAdapter(roomsListViewAdapter);
        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewRoom();
            }
        });
        getDataFromDb();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change_user) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Выход из аккаунта");
            alert.setMessage("Вы действительно хотите выйти?");
            alert.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(RoomsActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            alert.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alert.show();
        } else if (item.getItemId() == R.id.users_profile) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("userId", firebaseUser.getUid());
            intent.putExtra("isOwner",true);
            startActivity(intent);
        }
        return true;
    }

    @SuppressLint("SetTextI18n")
    public void createNewRoom() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Создание комнаты");
        alert.setMessage("Заполните поля");

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText input = new EditText(this);
        final EditText input2 = new EditText(this);
        final TextView textView1 = new TextView(this);
        final TextView textView2 = new TextView(this);
        textView1.setText(" Имя комнаты");
        textView2.setText(" Пароль (если пустой, то без пароля)");
        layout.addView(textView1);
        layout.addView(input);
        layout.addView(textView2);
        layout.addView(input2);
        alert.setView(layout);

        alert.setPositiveButton("Создать", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                if (input.getText().toString().length() != 0) {
                    String password = "";
                    if (input2.getText().toString().length() != 0)
                        password = input2.getText().toString();
                    String roomId = dd4YouConfig.generateUniqueID(15);
                    Room room = new Room(roomId, input.getText().toString(), password, firebaseUser.getUid(), "",
                            true, GameState.WAITING_FOR_PLAYER, false, false);
                    myRef.child(roomId).setValue(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent = new Intent(RoomsActivity.this, GameActivity.class);
                            intent.putExtra("roomId", room.roomId);
                            intent.putExtra("isHost", true);
                            intent.putExtra("hostId",room.hostId);
                            startActivity(intent);
                        }

                    });
                } else {
                    Toast.makeText(RoomsActivity.this, "Ошибка. Пустое имя", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alert.show();
    }


    public void getDataFromDb() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (roomList.size() > 0) {
                    roomList.clear();
                }
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Room room = ds.getValue(Room.class);
                    roomList.add(room);
                }
                roomsListViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        myRef.addValueEventListener(valueEventListener);
    }
}
