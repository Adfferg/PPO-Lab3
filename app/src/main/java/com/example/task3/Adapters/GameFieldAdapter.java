package com.example.task3.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.task3.Game.Field;
import com.example.task3.GameLogic;
import com.example.task3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GameFieldAdapter extends RecyclerView.Adapter<GameFieldAdapter.ViewHolder> {

    LayoutInflater inflater;
    Field field;
    Context context;
    String roomId, hostId;
    public GameFieldAdapter(Context context, Field field,String roomId,String hostId) {
        this.field = field;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.roomId = roomId;
        this.hostId = hostId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.cell_item, parent, false);
        return new ViewHolder(view,roomId);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int i = position/8;
        int j = position%8;
        int backgroundColor;
        if ((position / 8) % 2 == 0) {
            if (position % 2 == 1) {
                backgroundColor = ContextCompat.getColor(context, R.color.brown);
                holder.cellLayout.setBackgroundColor(backgroundColor);
            }
        } else {
            if (position % 2 == 0) {
                backgroundColor = ContextCompat.getColor(context, R.color.brown);
                holder.cellLayout.setBackgroundColor(backgroundColor);
            }
        }
        switch(field.getCell(i,j)){
            case BLACK_BISHOP:
                holder.cellImage.setImageResource(R.drawable.black_bishop);
                break;
            case BLACK_PAWN:
                holder.cellImage.setImageResource(R.drawable.black_pawn);
                break;
            case BLACK_ROOK:
                holder.cellImage.setImageResource(R.drawable.black_rook);
                break;
            case BLACK_KING:
                holder.cellImage.setImageResource(R.drawable.black_king);
                break;
            case BLACK_QUEEN:
                holder.cellImage.setImageResource(R.drawable.black_queen);
                break;
            case BLACK_KNIGHT:
                holder.cellImage.setImageResource(R.drawable.black_knight);
                break;
            case WHITE_BISHOP:
                holder.cellImage.setImageResource(R.drawable.white_bishop);
                break;
            case WHITE_PAWN:
                holder.cellImage.setImageResource(R.drawable.white_pawn);
                break;
            case WHITE_ROOK:
                holder.cellImage.setImageResource(R.drawable.white_rook);
                break;
            case WHITE_KING:
                holder.cellImage.setImageResource(R.drawable.white_king);
                break;
            case WHITE_QUEEN:
                holder.cellImage.setImageResource(R.drawable.white_queen);
                break;
            case WHITE_KNIGHT:
                holder.cellImage.setImageResource(R.drawable.white_knight);
                break;
            case EMPTY:
                holder.cellImage.setImageResource(0);
                break;

        }
    }

    @Override
    public int getItemCount() {
        return 64;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout cellLayout;
        ImageView cellImage;

        public ViewHolder(@NonNull View itemView, String gameId) {
            super(itemView);
            cellLayout = itemView.findViewById(R.id.cellLayout);
            cellImage = itemView.findViewById(R.id.cellImage);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GameLogic logic = new GameLogic(roomId,hostId,field);
                    logic.setChosenCell(getAdapterPosition());
                }
            });
        }

    }
}
