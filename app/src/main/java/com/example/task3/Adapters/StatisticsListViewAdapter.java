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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.task3.DatabaseModels.Statistic;
import com.example.task3.Game.Result;
import com.example.task3.GameActivity;
import com.example.task3.R;
import com.example.task3.DatabaseModels.Room;
import com.example.task3.UserProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class StatisticsListViewAdapter extends ArrayAdapter<Statistic> {
    private LayoutInflater inflater;
    private List<Statistic> statisticsList;
    private int layout;
    private Context context;

    public StatisticsListViewAdapter(@NonNull Context context, int resource, List<Statistic> statisticsList) {
        super(context, resource, statisticsList);
        this.context = context;
        this.statisticsList = statisticsList;
        this.inflater = LayoutInflater.from(context);
        this.layout = resource;
    }

    public View getView(int pos, View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder") View view = inflater.inflate(this.layout, parent, false);
        TextView whiteNameTextView = (TextView) view.findViewById(R.id.whiteNameTextView);
        TextView blackNameTextView = (TextView) view.findViewById(R.id.blackNameTextView);
        ImageView wonImageView = view.findViewById(R.id.wonImageView);
        Statistic statistic = statisticsList.get(pos);
        whiteNameTextView.setText(statistic.whiteName);
        blackNameTextView.setText(statistic.blackName);
        String result;
        if (statistic.result == Result.BLACK_WON)
            wonImageView.setImageResource(R.drawable.black_king);
        else
            wonImageView.setImageResource(R.drawable.white_king);
            blackNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("userId", statistic.blackId);
                    intent.putExtra("isOwner",false);
                    context.startActivity(intent);
                }
            });
            whiteNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("userId", statistic.whiteId);
                    intent.putExtra("isOwner",false);
                    context.startActivity(intent);
                }
            });
            return view;
    }


}
