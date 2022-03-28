package com.sametozkan.quizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {

    //Database
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String currentUserID;
    private DatabaseReference currentUserReference;
    private DatabaseReference availableRooms;

    //Views
    private TextView usernameText;
    private TextView winText;
    private TextView loseText;
    private TextView emailText;
    private TextView waitText;
    private TextView levelText;
    private TextView mainMenuText;
    private Chronometer chronometer;
    private ImageView cancel;
    private AppCompatButton play;
    private AppCompatButton logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        initDatabase();
        initViews();
        setAnimations();
        addListenerToButtons();
        setVisibility();
        setUserInfo();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == play.getId()) {
            play();
        }
        else {
            logout();
        }
    }

    private void initDatabase() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUserID = currentUser.getUid();
        }
        else {
            Log.e("currentUser", "Current user is null.");
        }
        currentUserReference = database.getReference("Users").child(currentUserID);
        availableRooms = database.getReference("Rooms").child("AvailableRooms");
    }

    private void initViews() {
        usernameText = findViewById(R.id.username);
        winText = findViewById(R.id.win);
        loseText = findViewById(R.id.lose);
        levelText = findViewById(R.id.level);
        play = findViewById(R.id.play);
        logout = findViewById(R.id.logout);
        emailText = findViewById(R.id.email);
        mainMenuText = findViewById(R.id.mainMenu);
        waitText = findViewById(R.id.waiting);
        chronometer = findViewById(R.id.chronometer);
        cancel = findViewById(R.id.cancel);
    }

    private void setAnimations() {
        mainMenuText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade));
    }

    private void addListenerToButtons() {
        play.setOnClickListener(this);
        logout.setOnClickListener(this);
    }

    private void setVisibility() {
        chronometer.stop();
        chronometer.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);
        waitText.setVisibility(View.INVISIBLE);
    }

    private void setUserInfo() {
        currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String key = ds.getKey();
                        if (key != null) {
                            switch (key) {
                                case "username":
                                    String username = ds.getValue(String.class);
                                    usernameText.setText(username);
                                    break;
                                case "win":
                                    Integer win = ds.getValue(Integer.class);
                                    winText.setText(getString(R.string.win, String.valueOf(win)));
                                    break;
                                case "lose":
                                    Integer lose = ds.getValue(Integer.class);
                                    loseText.setText(getString(R.string.lose, String.valueOf(lose)));
                                    break;
                                case "email":
                                    String email = ds.getValue(String.class);
                                    emailText.setText(email);
                                    break;
                                case "point":
                                    Integer point = ds.getValue(Integer.class);
                                    if (point != null)
                                        levelText.setText(getResources().getString(R.string.level, String.valueOf(point / 100)));
                                    break;
                                default:
                                    Log.i("currentUser", "Default case in setUserInfo() method.");
                            }
                        }
                        else {
                            Log.e("currentUser", "Key of children is null!");
                        }
                    }
                }
                else {
                    Log.e("currentUser", "Current user hasn't children.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("setUserInfo", error.getDetails());
            }
        });
    }

    private void setPlayer1ForGameRoom() {
        DatabaseReference roomReference = availableRooms.push();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("player1", currentUser.getUid());
        hashMap.put("player2", null);
        roomReference.setValue(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i("player1", "Player1's id has been set.");
            }
            else {
                Log.e("player1", "Player1's id couldn't be set in game room!");
            }
        });
        database.getReference("/Users").child(currentUserID).child("currentRoom").setValue(roomReference.getKey()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i("currentRoom", "Current room of Player1 has been set.");
            }
            else {
                Log.e("currentRoom", "Current room of Player1 couldn't be set.");
            }
        });
        String roomKey = roomReference.getKey();
        waitForMatching(roomReference, roomKey);
    }

    private void setPlayer2ForGameRoom(DataSnapshot snapshot) {
        for (DataSnapshot room : snapshot.getChildren()) {
            DatabaseReference roomReference = room.getRef();
            String roomKey = roomReference.getKey();
            if (roomKey != null) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("player2", currentUser.getUid());
                database.getReference("/Users").child(currentUserID).child("currentRoom").setValue(roomReference.getKey()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        roomReference.updateChildren(map).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                roomReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                        database.getReference("/Rooms/FullRooms")
                                                .child(roomKey).setValue(snapshot1.getValue()).addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                startGame(roomKey);
                                            }
                                            else {
                                                Log.e("room", "The room couldn't be moved to Full Rooms from Available Rooms");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("room", error.getDetails());
                                    }
                                });

                            }
                            else {
                                Log.e("player2", "Player2 couldn't be set in game room.");
                            }
                        });
                    }
                    else {
                        Log.e("currentRoom", "Current room couldn't be set.");
                    }
                });

            }
            else {
                Log.e("setPlayer2InGameRoom", "Room key is null!");
            }

        }
    }

    private void waitForMatching(DatabaseReference roomReference, String roomKey) {
        play.setClickable(false);
        logout.setClickable(false);
        waitText.setVisibility(View.VISIBLE);
        chronometer.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        roomReference.child("player2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(String.class) != null) {
                    startGame(roomKey);
                }
                else {
                    Log.e("player2", "Player2 is null in game room!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("player2", error.getDetails());
            }
        });
        cancel.setOnClickListener(view -> database.getReference("/Rooms/AvailableRooms").child(roomKey).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chronometer.stop();
                chronometer.setVisibility(View.INVISIBLE);
                cancel.setVisibility(View.INVISIBLE);
                waitText.setVisibility(View.INVISIBLE);
                play.setClickable(true);
                logout.setClickable(true);
            }
            else {
                Log.e("room", "Game room couldn't be deleted from Available Rooms");
            }
        }));
    }

    private void play() {
        availableRooms.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //If there is available room, join it.
                if (snapshot.exists()) {
                    setPlayer2ForGameRoom(snapshot);
                }
                //Else, create new room.
                else {
                    setPlayer1ForGameRoom();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("availableRooms", error.getDetails());
            }
        });
    }

    private void logout() {
        auth.signOut();
        startActivity(new Intent(MainMenu.this, MainActivity.class));
        finish();
    }

    private void startGame(String roomKey) {
        Intent intent = new Intent(this, GameRoom.class);
        intent.putExtra("room", roomKey);
        intent.putExtra("currentUserID", currentUserID);
        startActivity(intent);
        finish();
    }

}