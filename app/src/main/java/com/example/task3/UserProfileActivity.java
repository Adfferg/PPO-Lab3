package com.example.task3;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

//import com.bumptech.glide.Glide;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UserProfileActivity extends AppCompatActivity {

    private EditText profileNameTextView;
    private TextView profileWinsTextView, profileLosesTextView;
    private Button changeNameButton;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private String PROFILE_KEY = "profile";
    private User user;
    public Uri imageUri;
    ImageView profileAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);
        profileNameTextView = findViewById(R.id.profileNameTextView);
        profileWinsTextView = findViewById(R.id.profileWinsTextView);
        profileLosesTextView = findViewById(R.id.profileLosesTextView);
        changeNameButton = findViewById(R.id.changeNameButton);
        profileAvatar = findViewById(R.id.profileAvatar);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        myRef = FirebaseDatabase.getInstance().getReference(PROFILE_KEY + "/" + firebaseUser.getUid());

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        loadUsersProfile();
        changeNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeUsersName();
            }
        });
        profileAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

    }

    private void loadUsersProfile() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Загрузка изображения...");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    user = ds.getValue(User.class);
                    profileNameTextView.setText(user.name);
                    profileWinsTextView.setText(Integer.toString(user.wins));
                    profileLosesTextView.setText(Integer.toString(user.loses));
                    changeNameButton.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        storageReference.child("avatars/"+firebaseUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(profileAvatar);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserProfileActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
            }
        });
        myRef.addValueEventListener(valueEventListener);
    }

    private void changeUsersName() {
        if (profileNameTextView.getText().toString().length() != 0&& !profileNameTextView.getText().toString().equals(user.name)) {
            user.name = profileNameTextView.getText().toString();
            myRef.removeValue();
            myRef.push().setValue(user);
            Toast.makeText(UserProfileActivity.this, "Имя изменено", Toast.LENGTH_SHORT).show();
        } else if(profileNameTextView.getText().toString().equals(user.name))
            Toast.makeText(UserProfileActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
    }

    private void choosePicture(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==1 &&  data!=null && resultCode == RESULT_OK && data.getData()!=null){
            imageUri = data.getData();
            profileAvatar.setImageURI(imageUri);
            uploadAvatar();
        }
    }

    private void uploadAvatar(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Загрузка изображения...");
        pd.show();
        StorageReference ref = storageReference.child("avatars/"+firebaseUser.getUid());
        ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
                Toast.makeText(UserProfileActivity.this, "Аватар изменён", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(UserProfileActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercent = (100.00* snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                pd.setMessage("Прогресс: "+(int)progressPercent+"%");
            }
        });
    }
}
