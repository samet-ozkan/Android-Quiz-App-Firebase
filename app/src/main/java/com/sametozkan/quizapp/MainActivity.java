package com.sametozkan.quizapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Database
    private FirebaseAuth mAuth;

    //Views
    private AppCompatButton signUp, signIn;
    private TextView quizApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDatabase();
        initViews();
        setAnimations();
        addListenerToButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == signUp.getId()) {
            startActivity(new Intent(this, SignUp.class));
            finish();
        }
        else if (id == signIn.getId()) {
            startActivity(new Intent(this, SignIn.class));
            finish();
        }
        else {
            Toast.makeText(this, R.string.invalid_button, Toast.LENGTH_SHORT).show();
            Log.e("invalidId", "Invalid button ID!");
        }
    }

    private void initDatabase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void initViews() {
        quizApp = findViewById(R.id.quizApp);
        signUp = findViewById(R.id.signUp);
        signIn = findViewById(R.id.signIn);
        signUp.setOnClickListener(this);
        signIn.setOnClickListener(this);
    }

    private void setAnimations() {
        quizApp.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade));
    }

    private void addListenerToButtons() {
        quizApp.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade));
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            startActivity(new Intent(getApplicationContext(), MainMenu.class));
            finish();
        }
    }
}