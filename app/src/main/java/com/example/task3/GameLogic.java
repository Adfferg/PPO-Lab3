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
    private Field field;

    public GameLogic(String roomId, String hostId, Field field) {
        this.roomId = roomId;
        this.field = field;
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
                    //если очередь этого игрока
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
                                                if ((isHost && isWhite(snapshot.getValue(Cell.class))) || (!isHost && isBlack(snapshot.getValue(Cell.class))))
                                                    //выбираем фигуру
                                                    refChosenCell.setValue(Integer.toString(position));
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });


                                    } else if (Integer.toString(position).equals(snapshot.getValue(String.class))) {
                                        refChosenCell.setValue("");
                                    } else {
                                        int prevPosition = Integer.parseInt(snapshot.getValue(String.class));
                                        //откуда ходим
                                        DatabaseReference refCell1 = FirebaseDatabase.getInstance().getReference("ROOMS/" + roomId + "/gameField/" + snapshot.getValue(String.class));
                                        //куда ходим
                                        DatabaseReference refCell2 = FirebaseDatabase.getInstance().getReference("ROOMS/" + roomId + "/gameField/" + position);
                                        refCell2.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    Cell oldFigure = snapshot.getValue(Cell.class);
                                                    //если не ходит на свою фигура
                                                    if ((isHost && !isWhite(snapshot.getValue(Cell.class))) || (!isHost && !isBlack(snapshot.getValue(Cell.class)))) {
                                                        refCell1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.exists()) {
                                                                    //фигура, которая ходит
                                                                    Cell figure = snapshot.getValue(Cell.class);
                                                                    if (checkFigure(prevPosition, position, figure, oldFigure)) {
                                                                        if (figure == Cell.WHITE_PAWN && position < 8)
                                                                            figure = Cell.WHITE_QUEEN;
                                                                        else if (figure == Cell.BLACK_PAWN && position > 56)
                                                                            figure = Cell.BLACK_QUEEN;
                                                                        refCell2.setValue(figure);
                                                                        refCell1.setValue(Cell.EMPTY);
                                                                        refChosenCell.setValue("");
                                                                        refTurn.setValue(enemyId);
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

    public boolean checkFigure(int prevPosition, int newPosition, Cell figure, Cell oldFigure) {

        boolean bool = false;
        switch (figure) {
            case BLACK_PAWN:
                bool = blackPawn(prevPosition, newPosition, oldFigure);
                break;
            case WHITE_PAWN:
                bool = whitePawn(prevPosition, newPosition, oldFigure);
                break;
            case WHITE_KNIGHT:
            case BLACK_KNIGHT:
                bool = knight(prevPosition, newPosition);
                break;
            case WHITE_KING:
            case BLACK_KING:
                bool = king(prevPosition, newPosition);
                break;
            case WHITE_ROOK:
            case BLACK_ROOK:
                bool = rook(prevPosition, newPosition, field);
                break;
            case WHITE_BISHOP:
            case BLACK_BISHOP:
                bool = bishop(prevPosition, newPosition, field);
                break;
            case WHITE_QUEEN:
            case BLACK_QUEEN:
                bool = bishop(prevPosition,newPosition,field)||rook(prevPosition, newPosition, field);
                break;
        }
        return bool;
    }

    public boolean blackPawn(int prevPosition, int newPosition, Cell oldFigure) {

        boolean bool = false;
        //с левого края
        if (prevPosition % 8 == 0) {
            if (((newPosition == prevPosition + 8)) && oldFigure == Cell.EMPTY || newPosition == prevPosition + 9) {
                bool = true;
            }
        }
        //с правого края
        else if ((prevPosition - 7 % 8) == 0) {
            if ((newPosition == prevPosition + 8 && oldFigure == Cell.EMPTY) || newPosition == prevPosition + 7) {
                bool = true;
            }
        } else {
            if ((newPosition == prevPosition + 8 && oldFigure == Cell.EMPTY) || newPosition == prevPosition + 7 || newPosition == prevPosition + 9) {
                bool = true;
            }
        }
        return bool;
    }

    public boolean whitePawn(int prevPosition, int newPosition, Cell oldFigure) {

        boolean bool = false;
        //с левого края
        if (prevPosition % 8 == 0) {
            if ((newPosition == prevPosition - 8 && oldFigure == Cell.EMPTY) || newPosition == prevPosition - 7) {
                bool = true;
            }
        }
        //с правого края
        else if ((prevPosition - 7 % 8) == 0) {
            if ((newPosition == prevPosition - 8 && oldFigure == Cell.EMPTY) || newPosition == prevPosition - 9) {
                bool = true;
            }
        } else {
            if ((newPosition == prevPosition - 8 && oldFigure == Cell.EMPTY) || newPosition == prevPosition - 7 || newPosition == prevPosition - 9) {
                bool = true;
            }
        }
        return bool;
    }

    public boolean knight(int prevPosition, int newPosition) {
        boolean bool = false;
        if (newPosition == prevPosition - 17 && isLeft(prevPosition, newPosition))
            bool = true;
        else if (newPosition == prevPosition - 15 && !isLeft(prevPosition, newPosition))
            bool = true;
        else if (newPosition == prevPosition - 6 && !isLeft(prevPosition, newPosition))
            bool = true;
        else if (newPosition == prevPosition + 10 && !isLeft(prevPosition, newPosition))
            bool = true;
        else if (newPosition == prevPosition + 17 && !isLeft(prevPosition, newPosition))
            bool = true;
        else if (newPosition == prevPosition + 15 && isLeft(prevPosition, newPosition))
            bool = true;
        else if (newPosition == prevPosition + 6 && isLeft(prevPosition, newPosition))
            bool = true;
        else if (newPosition == prevPosition - 10 && isLeft(prevPosition, newPosition))
            bool = true;
        return bool;
    }

    public boolean king(int prevPosition, int newPosition) {
        boolean bool = false;

        if (newPosition == prevPosition - 9 & isLeft(prevPosition, newPosition))
            bool = true;
        else if (newPosition == prevPosition - 8)
            bool = true;
        else if (newPosition == prevPosition - 7 & !isLeft(prevPosition, newPosition))
            bool = true;
        else if (newPosition == prevPosition + 1 & !isLeft(prevPosition, newPosition))
            bool = true;
        else if (newPosition == prevPosition + 9 & !isLeft(prevPosition, newPosition))
            bool = true;
        else if (newPosition == prevPosition + 8)
            bool = true;
        else if (newPosition == prevPosition + 7 & isLeft(prevPosition, newPosition))
            bool = true;
        else if (newPosition == prevPosition - 1 & isLeft(prevPosition, newPosition))
            bool = true;
        return bool;
    }

    public boolean rook(int prevPosition, int newPosition, Field field) {
        boolean bool = false;
        int step = 0;
        if (prevPosition % 8 == newPosition % 8)
            if (isUpper(prevPosition, newPosition))
                step = 8;
            else step = -8;
        if (isFigureOnRookWAY(prevPosition, newPosition, field, step))
            bool = true;
        else if (prevPosition / 8 == newPosition / 8)
            if (isLeft(prevPosition, newPosition))
                step = -1;
            else step = 1;
        if (isFigureOnRookWAY(prevPosition, newPosition, field, step))
            bool = true;
        return bool;
    }

    public boolean bishop(int prevPosition, int newPosition, Field field) {
        boolean bool = false;
        if (Math.abs(prevPosition / 8 - newPosition / 8) == Math.abs(prevPosition % 8 - newPosition % 8) && isFigureOnBishopWAY(prevPosition, newPosition, field))
            bool = true;
        return bool;
    }

    public boolean isLeft(int prevPosition, int newPosition) {
        boolean bool = false;
        if (prevPosition % 8 > newPosition % 8)
            bool = true;
        return bool;
    }

    private boolean isUpper(int prevPosition, int newPosition) {
        boolean bool = false;
        if (prevPosition / 8 < newPosition / 8)
            bool = true;
        return bool;
    }

    public boolean isFigureOnRookWAY(int prevPosition, int newPosition, Field field, int step) {
        boolean bool = true;
        for (int i = prevPosition + step; i != newPosition; i += step) {
            if (field.getCell(i / 8, i % 8) != Cell.EMPTY) {
                bool = false;
                break;
            }

        }
        return bool;
    }

    public boolean isFigureOnBishopWAY(int prevPosition, int newPosition, Field field) {
        boolean bool = true;
        int stepUp = 0;
        int stepLeft = 0;
        if (isUpper(prevPosition, newPosition))
            stepUp = 8;
        else
            stepUp = -8;
        if (isLeft(prevPosition, newPosition))
            stepLeft = -1;
        else stepLeft = 1;
        for (int i = prevPosition + stepUp + stepLeft; i != newPosition; i += stepUp + stepLeft) {
            if (field.getCell(i / 8, i % 8) != Cell.EMPTY) {
                bool = false;
                break;
            }

        }
        return bool;
    }

}
