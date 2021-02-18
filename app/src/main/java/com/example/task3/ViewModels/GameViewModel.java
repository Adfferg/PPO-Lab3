package com.example.task3.ViewModels;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bumptech.glide.Glide;
import com.example.task3.DatabaseModels.Statistic;
import com.example.task3.Game.Cell;
import com.example.task3.Game.Field;
import com.example.task3.Game.GameState;
import com.example.task3.Game.Result;
import com.example.task3.GameActivity;
import com.example.task3.UserProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class GameViewModel extends ViewModel {

    private String playerId;
    private String enemyId;
    private MutableLiveData<String> playerName = new MutableLiveData<>("");
    private MutableLiveData<String> enemyName = new MutableLiveData<>("");
    private String roomId;
    private boolean isEnemyReady = false, isPlayerReady = false;
    private MutableLiveData<Boolean> roomDeleted = new MutableLiveData<>(false);
    private MutableLiveData<GameState> gameState = new MutableLiveData<>();
    private MutableLiveData<Boolean> isEnemyButtonEnabled = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isYourButtonEnabled = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> yourTurn = new MutableLiveData<>();
    private MutableLiveData<Result> result = new MutableLiveData<>(Result.NONE);
    private MutableLiveData<String> turn = new MutableLiveData<>("");
    private MutableLiveData<Field> field = new MutableLiveData<>(new Field());

    private String ROOM_KEY = "ROOMS";
    private String PROFILE_KEY = "PROFILE";
    private String STATISTICS_KEY = "STATISTICS";

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
    private DatabaseReference profile = FirebaseDatabase.getInstance().getReference(PROFILE_KEY);


    public void setRoomId(String roomId, boolean isHost, String yourId) {
        this.roomId = roomId;
        roomRef = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId);
        if(!isHost)
        {
            roomRef = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId);
            roomRef.child("secondPlayerId").setValue(yourId);
        }
    }

    public MutableLiveData<String> getPlayerName() {
        return playerName;
    }

    public MutableLiveData<String> getEnemyName() {
        return enemyName;
    }

    public void setPlayerName(String name) {
        playerName.setValue(name);
    }

    public void setEnemyName(String name) {
        enemyName.setValue(name);
    }

    public void setSomeRoomRefs(String roomId) {
        roomRefChosenCell = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "chosenCell");
        roomRefTurn = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "turn");
        roomRefGameState = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "gameState");
        roomRefIsAvailable = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "isAvailable");
        roomRefGameField = FirebaseDatabase.getInstance().getReference(ROOM_KEY + "/" + roomId + "/" + "gameField");
    }

    public void setRoomRefsIfHost(boolean isHost) {
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
    }

    public void setRoomRefPlayerIsReady(boolean bool) {
        roomRefPlayerIsReady.setValue(bool);
    }

    public void setRoomRefGameField(Field field) {
        roomRefGameField.setValue(field.getField());
    }

    public void deleteRoom() {
        roomRef.removeValue();
    }

    public void playerLeftRoom() {
        roomRefGameState.setValue(GameState.WAITING_FOR_PLAYER);
        roomRefIsAvailable.setValue(true);
        roomRefPlayerId.setValue("");
    }

    public String getEnemyId() {
        return enemyId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void roomRefPlayerIdListener() {
        roomRefPlayerId.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    playerId = snapshot.getValue(String.class);
                    profile.child(playerId).child("name").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            setPlayerName(snapshot.getValue(String.class));
                            //yourNameTextView.setText(playerName);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void roomRefEnemyIdListener() {
        roomRefEnemyId.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    enemyId = snapshot.getValue(String.class);
                    profile.child(enemyId).child("name").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            setEnemyName(snapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public MutableLiveData<Boolean> getRoomDeleted() {
        return roomDeleted;
    }

    public void setRoomDeleted(boolean bool) {
        roomDeleted.setValue(bool);
    }

    public void roomRefListener() {
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    roomDeleted.setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public MutableLiveData<GameState> getGameState() {
        return gameState;
    }

    public void setGameState(GameState state) {
        gameState.setValue(state);
    }

    public MutableLiveData<Result> getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result.setValue(result);
    }

    public void roomGameStateListener(boolean isHost) {
        roomRefGameState.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.getValue(GameState.class) == GameState.WAITING_FOR_PLAYER) {
                        roomRefChosenCell.setValue("");
                        roomRefPlayerIsReady.setValue(false);
                        roomRefEnemyIsReady.setValue(false);
                        isEnemyReady = isPlayerReady = false;
                        if (isHost) {
                            roomRefTurn.setValue("");
                            roomRefChosenCell.setValue("");
                        }
                        setGameState(GameState.WAITING_FOR_PLAYER);
                    } else if (snapshot.getValue(GameState.class) == GameState.WAITING_FOR_CONFIRMATION) {
                        roomRefChosenCell.setValue("");
                        setGameState(GameState.WAITING_FOR_CONFIRMATION);
                    } else if (snapshot.getValue(GameState.class) == GameState.IN_WORK) {
                        if (isHost)
                            roomRefTurn.setValue(playerId);
                    } else if (snapshot.getValue(GameState.class) == GameState.ENDED) {

                        roomRef.child("result").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    if (isHost) {
                                        setResult(snapshot.getValue(Result.class));
                                        DatabaseReference refStat1 = FirebaseDatabase.getInstance().getReference(STATISTICS_KEY + "/" + playerId);
                                        DatabaseReference refStat2 = FirebaseDatabase.getInstance().getReference(STATISTICS_KEY + "/" + enemyId);
                                        refStat1.push().setValue(new Statistic(roomId, enemyId, getEnemyName().getValue(), playerId, getPlayerName().getValue(), getResult().getValue()));
                                        refStat2.push().setValue(new Statistic(roomId, enemyId, getEnemyName().getValue(), playerId, getPlayerName().getValue(), getResult().getValue()));
                                        roomRefTurn.setValue("");
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
    }

    public MutableLiveData<Boolean> getIsEnemyButtonEnabled() {
        return isEnemyButtonEnabled;
    }

    public void setIsEnemyButtonEnabled(boolean bool) {
        isEnemyButtonEnabled.setValue(bool);
    }

    public void roomRefEnemyIsReadyListener() {
        roomRefEnemyIsReady.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue(Boolean.class)) {
                    setIsEnemyButtonEnabled(true);
                    isEnemyReady = true;
                    if (isPlayerReady)
                        roomRefGameState.setValue(GameState.IN_WORK);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public MutableLiveData<Boolean> getIsYourButtonEnabled() {
        return isYourButtonEnabled;
    }

    public void setIsYourButtonEnabled(boolean bool) {
        isYourButtonEnabled.setValue(bool);
    }

    public void roomRefPlayerIsReadyListener() {
        roomRefPlayerIsReady.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue(Boolean.class)) {
                    setIsYourButtonEnabled(true);
                    isPlayerReady = true;
                    if (isEnemyReady)
                        roomRefGameState.setValue(GameState.IN_WORK);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public MutableLiveData<String> getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn.setValue(turn);
    }

    public void roomRefTurnListener() {
        roomRefTurn.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    setTurn(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setGameField(List<Cell> list) {
        Field temp = new Field();
        temp.setField(list);
        field.setValue(temp);
    }

    public MutableLiveData<Field> getField() {
        return field;
    }

    public void roomRefFieldListener(boolean isHost, String hostId) {
        roomRefGameField.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Cell> list = new ArrayList<>();
                    for (DataSnapshot cell : snapshot.getChildren()) {
                        list.add(cell.getValue(Cell.class));
                    }
                    setGameField(list);
                    //проверить, не победил ли кто ещё

                    int res = getField().getValue().gameEnded();
                    if (res == 1) {
                        setResult(Result.WHITE_WON);
                        if (isHost) {
                            roomRef.child("result").setValue(Result.WHITE_WON);
                            roomRefGameState.setValue(GameState.ENDED);
                            profile.child(hostId).child("wins").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    profile.child(hostId).child("wins").setValue(snapshot.getValue(Integer.class) + 1);
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
                        }

                    } else if (res == 2) {
                        setResult(Result.BLACK_WON);
                        if (isHost) {
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
    }

}
