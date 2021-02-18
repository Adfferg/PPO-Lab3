package com.example.task3.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.task3.GameActivity;
import com.example.task3.R;
import com.example.task3.DatabaseModels.Room;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class RoomsListViewAdapter extends ArrayAdapter<Room> {
    private LayoutInflater inflater;
    private List<Room> roomList;
    private Context context;
    private int layout;
    private String userId;

    private String ROOM_KEY = "ROOMS";

    public RoomsListViewAdapter(@NonNull Context context, int resource, List<Room> roomList, String userId) {
        super(context, resource, roomList);
        this.roomList = roomList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.layout = resource;
        this.userId = userId;

    }

    public View getView(int pos, View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder") View view = inflater.inflate(this.layout, parent, false);
        TextView roomItem = (TextView) view.findViewById(R.id.roomItem);
        Room room = roomList.get(pos);
        roomItem.setText(room.roomName);
        int backgroundColor;
        if (room.isAvailable) {
            backgroundColor = ContextCompat.getColor(context, R.color.green);
        } else {
            backgroundColor = ContextCompat.getColor(context, R.color.red);
        }
        roomItem.setBackgroundColor(backgroundColor);
        roomItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (room.isAvailable||room.hostId.equals(userId)) {
                    {
                        if (room.hostId.equals(userId)) {
                            Intent intent = new Intent(context, GameActivity.class);
                            intent.putExtra("roomId", room.roomId);
                            intent.putExtra("isHost", true);
                            intent.putExtra("hostId", room.hostId);
                            intent.putExtra("yourId", room.hostId);
                            context.startActivity(intent);
                        } else if (room.roomPassword.equals("")) {
                            Intent intent = new Intent(context, GameActivity.class);
                            intent.putExtra("roomId", room.roomId);
                            intent.putExtra("isHost", false);
                            intent.putExtra("hostId", room.hostId);
                            intent.putExtra("yourId", userId);
                            context.startActivity(intent);
                        } else {
                            AlertDialog.Builder alert = new AlertDialog.Builder(context);

                            alert.setTitle("Вход в комнату");
                            alert.setMessage("Введите пароль");

                            final EditText input = new EditText(context);
                            alert.setView(input);

                            alert.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if (input.getText().toString().length() > 0 && input.getText().toString().equals(room.roomPassword)) {
                                        Intent intent = new Intent(context, GameActivity.class);
                                        intent.putExtra("roomId", room.roomId);
                                        intent.putExtra("isHost", false);
                                        intent.putExtra("hostId", room.hostId);
                                        intent.putExtra("yourId", userId);
                                        context.startActivity(intent);
                                    } else
                                        Toast.makeText(context, "Не верный пароль ", Toast.LENGTH_SHORT).show();
                                }
                            });
                            alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            alert.show();
                        }
                    }
                } else {
                    Toast.makeText(context, "Комната занята", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }


}
