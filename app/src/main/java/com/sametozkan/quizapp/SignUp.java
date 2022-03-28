package com.sametozkan.quizapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class SignUp extends AppCompatActivity {

    //Database
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    //Views
    private EditText username, email, password;
    private AppCompatButton register;
    private TextView signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initDatabase();
        initViews();
        setAnimations();
        addListenerToRegisterButton();
    }

    private void initDatabase() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    private void initViews() {
        signUp = findViewById(R.id.signUp);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
    }

    private void setAnimations() {
        signUp.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade));
    }

    private void addListenerToRegisterButton() {
        register.setOnClickListener(view -> {
            String username = this.username.getText().toString();
            String email = this.email.getText().toString();
            String password = this.password.getText().toString();

            if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty())
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Signed up successfully.", Toast.LENGTH_LONG).show();
                        DatabaseReference reference = database.getReference("Users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("email", email);
                        hashMap.put("username", username);
                        hashMap.put("win", 0);
                        hashMap.put("lose", 0);
                        hashMap.put("point", 0);
                        hashMap.put("currentRoom", null);
                        reference.setValue(hashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                startActivity(new Intent(getApplicationContext(), MainMenu.class));
                                finish();
                            }
                            else {
                                Toast.makeText(SignUp.this, "Database error!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else {
                        Toast.makeText(SignUp.this, "Register failed!", Toast.LENGTH_LONG).show();
                    }
                });
            else {
                Toast.makeText(this, "Username / email / password cannot be empty.", Toast.LENGTH_LONG).show();
            }
        });
    }

}