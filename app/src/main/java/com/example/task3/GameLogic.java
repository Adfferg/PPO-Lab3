package com.example.task3;

import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.task3.Game.Cell;
import com.example.task3.Game.Field;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class GameLogic {

    private String roomId;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    private boolean isHost;
    private String enemyId;
    private String userId;

    public GameLogic(String roomId, String hostId) {
        this.roomId = roomId;
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        userId = firebaseUser.getUid();
        if (hostId.equals(userId)) {
            DatabaseReference refEnemy = FirebaseDatabase.getInstance().getReference("ROOMS/" + roomId + "/secondPlayerId");
            refEnemy.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        enemyId = snapshot.getValue(String.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            isHost = true;
        } else {
            enemyId = hostId;
            isHost = false;
        }

    }

    public void setChosenCell(int position) {
        DatabaseReference refTurn = FirebaseDatabase.getInstance().getReference("ROOMS/" + roomId + "/turn");
        refTurn.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.getValue(String.class).equals(userId)) {
                        DatabaseReference refChosenCell = FirebaseDatabase.getInstance().getReference("ROOMS/" + roomId + "/chosenCell");
                        refChosenCell.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    if (snapshot.getValue(String.class).equals("")) {
                                        DatabaseReference refCell = FirebaseDatabase.getInstance().getReference("ROOMS/" + roomId + "/gameField/" + position);
                                        refCell.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                //хост ходит белыми, а гость чёрными
                                                 if ((isHost && isWhite(snapshot.getValue(Cell.class)))||(!isHost&&isBlack(snapshot.getValue(Cell.class))))
                                                     refChosenCell.setValue(Integer.toString(position));
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });


                                    } else if(Integer.toString(position).equals(snapshot.getValue(String.class))){
                                        refChosenCell.setValue("");
                                    }
                                    else{
                                        DatabaseReference refCell1 = FirebaseDatabase.getInstance().getReference("ROOMS/" + roomId + "/gameField/" + snapshot.getValue(String.class));
                                        DatabaseReference refCell2 = FirebaseDatabase.getInstance().getReference("ROOMS/" + roomId + "/gameField/" + position);
                                        refCell1.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    Cell cell = snapshot.getValue(Cell.class);
                                                    refCell2.setValue(cell);
                                                    refCell1.setValue(Cell.EMPTY);
                                                    refChosenCell.setValue("");
                                                    refTurn.setValue(enemyId);
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

                        // DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ROOMS/" + roomId + "/gameField/"+position);
                        // ref.setValue(Cell.BLACK_KING);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public boolean isBlack(Cell cell) {
        boolean bool = true;
        switch (cell) {
            case WHITE_BISHOP:
            case WHITE_KING:
            case WHITE_KNIGHT:
            case WHITE_PAWN:
            case WHITE_QUEEN:
            case WHITE_ROOK:
            case EMPTY:
                bool = false;
                break;
        }
        return bool;
    }

    public boolean isWhite(Cell cell) {
        boolean bool = true;
        switch (cell) {
            case BLACK_BISHOP:
            case BLACK_KING:
            case BLACK_KNIGHT:
            case BLACK_PAWN:
            case BLACK_QUEEN:
            case BLACK_ROOK:
            case EMPTY:
                bool = false;
                break;
        }
        return bool;
    }


    public void tryToPlaceFigure(DatabaseReference ref){

    }
}
