package com.example.task3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.task3.Adapters.RoomsListViewAdapter;
import com.example.task3.Adapters.StatisticsListViewAdapter;
import com.example.task3.DatabaseModels.Room;
import com.example.task3.DatabaseModels.Statistic;
import com.example.task3.Game.GameState;
import com.example.task3.Game.Result;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private ListView statisticsListView;

    private Button clearStatisticsButton;
    private List<Statistic> userStatistics;
    private String userId;
    private String STATISTICS_KEY = "STATISTICS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_activity);
        clearStatisticsButton = findViewById(R.id.clearStatisticsButton);
        statisticsListView = findViewById(R.id.statisticsListView);
        userStatistics = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString("userId");
        }
        StatisticsListViewAdapter adapter = new StatisticsListViewAdapter(StatisticsActivity.this,
                R.layout.statistics_item, userStatistics);
        statisticsListView.setAdapter(adapter);
        DatabaseReference refStatistic = FirebaseDatabase.getInstance().getReference(STATISTICS_KEY + "/" + userId);

        refStatistic.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Statistic statistic = ds.getValue(Statistic.class);
                        userStatistics.add(statistic);

                    }
                    clearStatisticsButton.setEnabled(true);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(StatisticsActivity.this, "Статистика пуста", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        clearStatisticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refStatistic.removeValue();
                userStatistics.clear();
                adapter.notifyDataSetChanged();
            }
        });


    }
}
