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

public class SignIn extends AppCompatActivity {

    //Database
    private FirebaseAuth mAuth;

    //Views
    private EditText email, password;
    private TextView signIn;
    private AppCompatButton login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initDatabase();
        initViews();
        setAnimations();
        addListenerToLoginButton();
    }

    private void initDatabase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void initViews() {
        signIn = findViewById(R.id.signIn);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
    }

    private void setAnimations() {
        signIn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade));
    }

    private void addListenerToLoginButton() {
        login.setOnClickListener(view -> {
            String email = this.email.getText().toString();
            String password = this.password.getText().toString();

            if (!email.isEmpty() && !password.isEmpty())
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Signed in successfully!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(SignIn.this, MainMenu.class));
                        finish();
                    }
                    else {
                        Toast.makeText(this, "Login failed!", Toast.LENGTH_LONG).show();
                    }
                });
            else {
                Toast.makeText(this, "Error: Email / password cannot be empty.", Toast.LENGTH_LONG).show();
            }
        });
    }

}