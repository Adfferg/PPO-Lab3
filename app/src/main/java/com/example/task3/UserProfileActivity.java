package com.example.task3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.task3.DatabaseModels.User;
import com.example.task3.ViewModels.ProfileViewModel;
import com.example.task3.ViewModels.RoomViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
    private TextView profileWinsTextView, profileLosesTextView,profileDateTextView,emailTextView,profileEmailTextView;
    private Button changeNameButton,deleteAccountButton,statisticsButton;
    private FirebaseAuth mAuth;

    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private User user;
    public Uri imageUri;
    private String userId;
    private boolean isOwner;
    ImageView profileAvatar;
    private ProfileViewModel profileViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);

        profileViewModel =  ViewModelProviders.of(this).get(ProfileViewModel.class);
        profileNameTextView = findViewById(R.id.profileNameTextView);
        profileWinsTextView = findViewById(R.id.profileWinsTextView);
        profileLosesTextView = findViewById(R.id.profileLosesTextView);
        changeNameButton = findViewById(R.id.changeNameButton);
        profileAvatar = findViewById(R.id.profileAvatar);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        profileDateTextView = findViewById(R.id.profileDateTextView);
        emailTextView = findViewById(R.id.emailTextView);
        profileEmailTextView = findViewById(R.id.profileEmailTextView);
        statisticsButton = findViewById(R.id.statisticsButton);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString("userId");
            isOwner = extras.getBoolean("isOwner");
        }
        if (!userId.equals(firebaseUser.getUid())){
            profileNameTextView.setEnabled(false);
            changeNameButton.setVisibility(View.GONE);
            deleteAccountButton.setVisibility(View.GONE);
            emailTextView.setVisibility(View.GONE);
            profileEmailTextView.setVisibility(View.GONE);
        }
        profileViewModel.setMyRef(userId);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        loadUsersProfile(userId);
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
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });
        statisticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(UserProfileActivity.this, StatisticsActivity.class);
                intent.putExtra("userId",userId);
                intent.putExtra("isOwner",isOwner);
                startActivity(intent);
            }
        });

    }

    private void loadUsersProfile(String userId) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {user = snapshot.getValue(User.class);
                    profileNameTextView.setText(user.name);
                    profileDateTextView.setText(user.registrationTime);
                    profileWinsTextView.setText(Integer.toString(user.wins));
                    profileLosesTextView.setText(Integer.toString(user.loses));
                    profileEmailTextView.setText(firebaseUser.getEmail());
                    changeNameButton.setEnabled(true);}
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        storageReference.child("avatars/"+userId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(profileAvatar);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Glide.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/task3-120a9.appspot.com/o/avatars%2Fno-avatar.png?alt=media&token=3cd19d45-030c-4cb8-935a-1598ed281d8e").into(profileAvatar);
            }
        });
        profileViewModel.addListener(valueEventListener);
    }

    private void changeUsersName() {
        if (profileNameTextView.getText().toString().length() != 0&& !profileNameTextView.getText().toString().equals(user.name)) {
            user.name = profileNameTextView.getText().toString();
            profileViewModel.changeName(user.name);
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

    public void deleteAccount(){
        String userId = firebaseUser.getUid();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Удаление аккаунта");
        alert.setMessage("Введите пароль");

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(input.getText().toString().length()!=0){
                String password = input.getText().toString();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(firebaseUser.getEmail(), password);

                firebaseUser.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                                //удаление из DataBase
                                profileViewModel.deleteData();
                                //удаление аватарки из Storage
                                storageReference.child("avatars/"+userId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Toast.makeText(UserProfileActivity.this, "Не удалось удалить аватарку "+exception, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                //удаление юзера
                                firebaseUser.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                    Toast.makeText(UserProfileActivity.this, "Аккаунт удалён", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(UserProfileActivity.this, "Ошибка. Не удалось удалить аккаунт", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserProfileActivity.this, "Ошибка. Не удалось удалить перезайти в аккаунт", Toast.LENGTH_SHORT).show();
                    }
                });
                }
                else{
                    Toast.makeText(UserProfileActivity.this, "Ошибка. Пустое поле", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }
}
