package com.example.task3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    EditText registrationEmailEditText;
    EditText usersNameEditText;
    EditText registrationPasswordEditText;
    EditText registrationPasswordEditText2;
    Button registrationActivityButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity);
        mAuth = FirebaseAuth.getInstance();
        registrationEmailEditText = findViewById(R.id.registrationEmailEditText);
        usersNameEditText = findViewById(R.id.usersNameEditText);
        registrationPasswordEditText = findViewById(R.id.registrationPasswordEditText);
        registrationPasswordEditText2 = findViewById(R.id.registrationPasswordEditText2);
        registrationActivityButton = findViewById(R.id.registrationActivityButton);
        registrationActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (registrationEmailEditText.getText() != null && usersNameEditText.getText() != null &&
                        registrationPasswordEditText.getText() != null && registrationPasswordEditText2.getText() != null) {
                    if (registrationPasswordEditText.getText().toString().equals(registrationPasswordEditText2.getText().toString())) {
                        registration(registrationEmailEditText.getText().toString(), registrationPasswordEditText.getText().toString(),
                                usersNameEditText.getText().toString());
                    }
                    else
                        Toast.makeText(RegistrationActivity.this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();

                }
                else
                    Toast.makeText(RegistrationActivity.this, "Не все поля заполнены", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void registration(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegistrationActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    finish();
                } else
                    Toast.makeText(RegistrationActivity.this, "Регистрация  провалена", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
