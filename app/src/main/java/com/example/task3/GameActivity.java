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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.task3.Adapters.GameFieldAdapter;
import com.example.task3.DatabaseModels.Statistic;
import com.example.task3.Game.Cell;
import com.example.task3.Game.Field;
import com.example.task3.Game.GameState;
import com.example.task3.Game.Result;
import com.example.task3.ViewModels.GameViewModel;
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
    private String hostId;


    private boolean isHost;


    private StorageReference storageReference;
    private GameViewModel viewModel;

    private TextView yourNameTextView, enemyNameTextView;
    private ImageView yourAvatarImageView, enemyAvatarImageView;
    private Button yourStartGameButton, enemyStartGameButton;
    private RecyclerView recyclerView;

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
        viewModel = ViewModelProviders.of(this).get(GameViewModel.class);
        yourNameTextView = findViewById(R.id.yourNameTextView);
        enemyNameTextView = findViewById(R.id.enemyNameTextView);
        yourAvatarImageView = findViewById(R.id.yourAvatarImageView);
        enemyAvatarImageView = findViewById(R.id.enemyAvatarImageView);
        yourStartGameButton = findViewById(R.id.yourStartGameButton);
        enemyStartGameButton = findViewById(R.id.enemyStartGameButton);


        viewModel.setRoomId(roomId);

        viewModel.setSomeRoomRefs(roomId);

        viewModel.setRoomRefsIfHost(isHost);

        loadUserInfo();
        yourStartGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.setRoomRefPlayerIsReady(true);
                yourStartGameButton.setEnabled(false);
            }
        });
        recyclerView = findViewById(R.id.game_field);
        GameFieldAdapter fieldAdapter = new GameFieldAdapter(this, viewModel.getField().getValue(), roomId, hostId);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 8));
        recyclerView.setAdapter(fieldAdapter);
        viewModel.setRoomRefGameField(viewModel.getField().getValue());

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isHost) {
            viewModel.deleteRoom();
        } else {
            viewModel.playerLeftRoom();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void loadUserInfo() {
        viewModel.roomRefPlayerIdListener();


        viewModel.roomRefEnemyIdListener();
        enemyAvatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!viewModel.getEnemyId().equals("")) {
                    Intent intent = new Intent(GameActivity.this, UserProfileActivity.class);
                    intent.putExtra("userId", viewModel.getEnemyId());
                    intent.putExtra("isOwner", false);
                    startActivity(intent);
                }
            }
        });

        viewModel.roomRefListener();
        viewModel.getRoomDeleted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean bool) {
                if (bool) {
                    Toast.makeText(GameActivity.this, "Комната была удалена", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        viewModel.roomGameStateListener(isHost);
        viewModel.getPlayerName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String str) {
                yourNameTextView.setText(str);
                storageReference.child("avatars/" + viewModel.getPlayerId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
        });
        viewModel.getEnemyName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String str) {
                enemyNameTextView.setText(str);
                storageReference.child("avatars/" + viewModel.getEnemyId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
        });
        viewModel.getGameState().observe(this, new Observer<GameState>() {
            @Override
            public void onChanged(GameState gameState) {
                if (gameState == GameState.WAITING_FOR_PLAYER) {
                    yourStartGameButton.setEnabled(false);
                    enemyStartGameButton.setEnabled(false);
                } else if (gameState == GameState.WAITING_FOR_CONFIRMATION) {
                    yourStartGameButton.setEnabled(true);
                    enemyStartGameButton.setEnabled(true);
                }
            }
        });
        viewModel.roomRefEnemyIsReadyListener();
        viewModel.getIsEnemyButtonEnabled().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean bool) {
                if (bool) {
                    enemyStartGameButton.setEnabled(false);
                } else {
                    enemyStartGameButton.setEnabled(true);
                }
            }
        });
        viewModel.getIsYourButtonEnabled().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean bool) {
                if (bool) {
                    yourStartGameButton.setEnabled(false);
                } else {
                    yourStartGameButton.setEnabled(true);
                }
            }
        });

        viewModel.roomRefPlayerIsReadyListener();
        viewModel.roomRefFieldListener(isHost,hostId);
        viewModel.roomRefTurnListener();
        viewModel.getTurn().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String str) {

                if (str.equals(viewModel.getPlayerId())&&viewModel.getResult().getValue()==Result.NONE) {
                    Toast.makeText(GameActivity.this, "Ваш ход!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        viewModel.getResult().observe(this, new Observer<Result>() {
            @Override
            public void onChanged(Result result) {

                if (result == Result.WHITE_WON) {
                    if (isHost)
                        Toast.makeText(GameActivity.this, "Вы выйграли!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(GameActivity.this, "Вы проиграли!", Toast.LENGTH_SHORT).show();
                } else if (result == Result.BLACK_WON) {
                    if (isHost)
                        Toast.makeText(GameActivity.this, "Вы проиграли!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(GameActivity.this, "Вы выйграли!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        viewModel.getField().observe(this, new Observer<Field>() {
            @Override
            public void onChanged(Field field2) {
                GameFieldAdapter adapter = new GameFieldAdapter(getApplicationContext(), field2, roomId, hostId);
                recyclerView.setAdapter(adapter);
            }
        });
    }
}

