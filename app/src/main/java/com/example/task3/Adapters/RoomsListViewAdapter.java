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

import com.example.task3.R;
import com.example.task3.DatabaseModels.Room;
import com.example.task3.RoomDescriptionActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class RoomsListViewAdapter extends ArrayAdapter<Room> {
    private LayoutInflater inflater;
    private List<Room> roomList;
    private Context context;
    private int layout;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;


    public RoomsListViewAdapter(@NonNull Context context, int resource, List<Room> roomList) {
        super(context, resource, roomList);
        this.roomList = roomList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.layout = resource;
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
    }

    public View getView(int pos, View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder") View view = inflater.inflate(this.layout, parent, false);
        TextView roomItem = (TextView) view.findViewById(R.id.roomItem);
        Room room = roomList.get(pos);
        roomItem.setText(room.name);
        roomItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (room.password.equals("") || room.creatorId.equals(firebaseUser.getUid())) {
                    Intent intent = new Intent(context, RoomDescriptionActivity.class);
                    intent.putExtra("id", room.roomId);
                    context.startActivity(intent);
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);

                    alert.setTitle("Вход в комнату");
                    alert.setMessage("Введите пароль");

                    final EditText input = new EditText(context);
                    alert.setView(input);

                    alert.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if(input.getText().toString().length()>0&&input.getText().toString().equals(room.password)){
                                Intent intent = new Intent(context, RoomDescriptionActivity.class);
                                intent.putExtra("id", room.roomId);
                                context.startActivity(intent);
                            }
                            else
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
        });
        return view;
    }
}
