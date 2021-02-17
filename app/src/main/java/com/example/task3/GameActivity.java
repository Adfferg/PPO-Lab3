package com.example.task3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.task3.Adapters.GameFieldAdapter;
import com.example.task3.DatabaseModels.Statistic;
import com.example.task3.Game.Cell;
import com.example.task3.Game.Field;
import com.example.task3.Game.GameState;
import com.example.task3.Game.Result;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class GameActivity extends AppCompatActivity {

    private String roomId;
    private String playerId;
    private String enemyId;
    private String playerName;
    private String enemyName;
    private String hostId;
    private Result result;

    private boolean isHost, isAvailable, gameState, isEnemyReady = false, isPlayerReady = false;

    private StorageReference storageReference;
    private DatabaseReference roomRefPlayerId;
    private DatabaseReference roomRefEnemyId;
    private DatabaseReference roomRef;
    private DatabaseReference roomRefGameState;
    private DatabaseReference roomRefIsAvailable;
    private DatabaseReference roomRefPlayerIsReady;
    private DatabaseReference roomRefEnemyIsReady;
    private DatabaseReference roomRefGameField;
    private DatabaseReference roomRefTurn;
    private DatabaseReference roomRefChosenCell;

    private DatabaseReference profile;

    private TextView yourNameTextView, enemyNameTextView;
    private ImageView yourAvatarImageView, enemyAvatarImageView;
    private Button yourStartGameButton, enemyStartGameButton;
    private RecyclerView recyclerView;
    private GameFieldAdapter fieldAdapter;

    private String ROOM_KEY = "ROOMS";
    private String PROFILE_KEY = "PROFILE";
    private String STATISTICS_KEY = "STATISTICS";

    private Field field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            roomId = extras.getString("roomId");
            isHost = extras.getBoolean("isHost");
            hostId = extras.getString("hostId");
        }
        storageReference = FirebaseStorage.getInstance().getReference();

        yourNameTextView = findViewById(R.id.yourNameTextView);
        enemyNameTextView = findViewById(R.id.enemyNameTextView);
        yourAvatarImageView = findViewById(R.id.yourAvatarImageView);
        enemyAvatarImageView = findViewById(R.id.enemyAvatarImageView);
        yourStartGameButton = findViewById(R.id.yourStartGameButton);
        enemyStartGameButton = findViewById(R.id.enemyStartGameButton);

        profile = FirebaseDatabase.getInstance().getReference(PROFILE_KEY);

        roomRef = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId);

        roomRefChosenCell = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "chosenCell");
        roomRefTurn = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "turn");
        roomRefGameState = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "gameState");
        roomRefIsAvailable = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "isAvailable");
        roomRefGameField = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "gameField");

        if (isHost) {
            roomRefPlayerIsReady = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "hostIsReady");
            roomRefEnemyIsReady = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "secondPlayerIsReady");
            roomRefPlayerId = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "hostId");
            roomRefEnemyId = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "secondPlayerId");
        } else {
            roomRefPlayerIsReady = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "secondPlayerIsReady");
            roomRefEnemyIsReady = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "hostIsReady");
            roomRefEnemyId = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "hostId");
            roomRefPlayerId = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "secondPlayerId");
            roomRefIsAvailable.setValue(false);
            roomRefGameState.setValue(GameState.WAITING_FOR_CONFIRMATION);
        }
        loadUserInfo();
        yourStartGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomRefPlayerIsReady.setValue(true);
                yourStartGameButton.setEnabled(false);
            }
        });
        recyclerView = findViewById(R.id.game_field);
        field = new Field();
        fieldAdapter = new GameFieldAdapter(this, field, roomId, hostId);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 8));
        recyclerView.setAdapter(fieldAdapter);
        roomRefGameField.setValue(field.getField());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isHost) {
            roomRef.removeValue();
        } else {
            roomRefGameState.setValue(GameState.WAITING_FOR_PLAYER);
            roomRefIsAvailable.setValue(true);
            roomRefPlayerId.setValue("");
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void loadUserInfo() {
        roomRefPlayerId.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    playerId = snapshot.getValue(String.class);
                    profile.child(playerId).child("name").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            playerName = snapshot.getValue(String.class);
                            yourNameTextView.setText(playerName);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    storageReference.child("avatars/" + playerId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(getApplicationContext()).load(uri).into(yourAvatarImageView);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Glide.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/task3-120a9.appspot.com/o/avatars%2Fno-avatar.png?alt=media&token=3cd19d45-030c-4cb8-935a-1598ed281d8e").into(yourAvatarImageView);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        roomRefEnemyId.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    enemyId = snapshot.getValue(String.class);
                    profile.child(enemyId).child("name").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            enemyName = snapshot.getValue(String.class);
                            enemyNameTextView.setText(enemyName);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    storageReference.child("avatars/" + enemyId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(getApplicationContext()).load(uri).into(enemyAvatarImageView);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Glide.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/task3-120a9.appspot.com/o/avatars%2Fno-avatar.png?alt=media&token=3cd19d45-030c-4cb8-935a-1598ed281d8e").into(enemyAvatarImageView);
                        }
                    });
                }
                enemyAvatarImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GameActivity.this, UserProfileActivity.class);
                        intent.putExtra("userId", enemyId);
                        intent.putExtra("isOwner",false);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(GameActivity.this, "Комната была удалена", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        roomRefGameState.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.getValue(GameState.class) == GameState.WAITING_FOR_PLAYER) {
                        yourStartGameButton.setEnabled(false);
                        enemyStartGameButton.setEnabled(false);
                        roomRefPlayerIsReady.setValue(false);
                        roomRefEnemyIsReady.setValue(false);
                        isEnemyReady = isPlayerReady = false;
                        if (isHost) {
                            roomRefTurn.setValue("");
                            roomRefChosenCell.setValue("");
                        }

                    } else if (snapshot.getValue(GameState.class) == GameState.WAITING_FOR_CONFIRMATION) {
                        yourStartGameButton.setEnabled(true);
                        enemyStartGameButton.setEnabled(true);
                    } else if (snapshot.getValue(GameState.class) == GameState.IN_WORK) {
                        if (isHost)
                            roomRefTurn.setValue(playerId);
                    }
                    else if (snapshot.getValue(GameState.class)==GameState.ENDED){
                        roomRefTurn.setValue("");
                        roomRef.child("result").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    if(isHost){
                                        if(snapshot.getValue(Result.class)==Result.WHITE_WON)
                                            Toast.makeText(GameActivity.this, "Вы выйграли!", Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(GameActivity.this, "Вы проиграли!", Toast.LENGTH_SHORT).show();

                                        DatabaseReference refStat1 = FirebaseDatabase.getInstance().getReference(STATISTICS_KEY+"/"+playerId);
                                        DatabaseReference refStat2 = FirebaseDatabase.getInstance().getReference(STATISTICS_KEY+"/"+enemyId);
                                        refStat1.push().setValue(new Statistic(roomId,enemyId,enemyName,playerId,playerName,result));
                                        refStat2.push().setValue(new Statistic(roomId,enemyId,enemyName,playerId,playerName,result));
                                    }
                                    else{
                                        if(snapshot.getValue(Result.class)==Result.BLACK_WON)
                                            Toast.makeText(GameActivity.this, "Вы выйграли!", Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(GameActivity.this, "Вы проиграли!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        roomRefEnemyIsReady.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue(Boolean.class)) {
                    enemyStartGameButton.setEnabled(false);
                    isEnemyReady = true;
                    if (isPlayerReady)
                        roomRefGameState.setValue(GameState.IN_WORK);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        roomRefPlayerIsReady.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue(Boolean.class)) {
                    yourStartGameButton.setEnabled(false);
                    isPlayerReady = true;
                    if (isEnemyReady)
                        roomRefGameState.setValue(GameState.IN_WORK);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        roomRefGameField.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Cell> list = new ArrayList<>();
                    for (DataSnapshot cell : snapshot.getChildren()) {
                        list.add(cell.getValue(Cell.class));
                    }
                    field.setField(list);
                    fieldAdapter.notifyDataSetChanged();
                    //проверить, не победил ли кто ещё
                    if(isHost)
                    {
                    int res = field.gameEnded();
                    if (res == 1) {
                        result= Result.WHITE_WON;
                        roomRef.child("result").setValue(Result.WHITE_WON);
                        roomRefGameState.setValue(GameState.ENDED);
                        profile.child(hostId).child("wins").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                profile.child(hostId).child("wins").setValue(snapshot.getValue(Integer.class) + 1).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(GameActivity.this, hostId+" "+enemyId, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        profile.child(enemyId).child("loses").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                profile.child(enemyId).child("loses").setValue(snapshot.getValue(Integer.class) + 1);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    } else if (res == 2) {
                        result= Result.BLACK_WON;
                        roomRef.child("result").setValue(Result.BLACK_WON);
                        roomRefGameState.setValue(GameState.ENDED);
                        profile.child(enemyId).child("wins").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                profile.child(enemyId).child("wins").setValue(snapshot.getValue(Integer.class) + 1);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        profile.child(hostId).child("loses").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                profile.child(hostId).child("loses").setValue(snapshot.getValue(Integer.class) + 1);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        roomRefTurn.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    String turn = snapshot.getValue(String.class);
                    if(turn.equals(playerId))
                        Toast.makeText(GameActivity.this, "Ваш ход!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

