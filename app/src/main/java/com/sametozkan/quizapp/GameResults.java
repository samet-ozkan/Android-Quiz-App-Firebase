package com.sametozkan.quizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GameResults extends AppCompatActivity implements View.OnClickListener {

    //Database
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser currentUser = auth.getCurrentUser();
    private final String currentUserID;
    private final DatabaseReference fullRooms = database.getReference("Rooms").child("FullRooms");
    private DatabaseReference roomReference;

    {
        assert currentUser != null;
        currentUserID = currentUser.getUid();
    }

    //Views
    private TextView levelText, pointText, win, lose, gameResult, p1_username, p2_username, p1_score, p2_score;
    private ProgressBar levelBar;

    //True count
    private int p1_true;
    private int p2_true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_results);
        if (getIntent() != null) {
            roomReference = fullRooms.child(getRoomKey());
            p1_true = getIntent().getIntExtra("p1_true", -1);
            p2_true = getIntent().getIntExtra("p2_true", -1);
            initViews();
            setDisplay();
        }
        else {
            Log.e("gameResults", "Intent is null!");
        }
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        if (button.getId() == R.id.mainMenu) {
            startActivity(new Intent(getApplicationContext(), MainMenu.class));
            finish();
        }
    }

    private void initViews() {
        levelBar = findViewById(R.id.levelBar);
        levelText = findViewById(R.id.level);
        pointText = findViewById(R.id.point);
        win = findViewById(R.id.win);
        lose = findViewById(R.id.lose);
        gameResult = findViewById(R.id.gameResult);
        p1_username = findViewById(R.id.p1_username);
        p2_username = findViewById(R.id.p2_username);
        p1_score = findViewById(R.id.p1_score);
        p2_score = findViewById(R.id.p2_score);
        AppCompatButton mainMenu = findViewById(R.id.mainMenu);
        mainMenu.setOnClickListener(this);
    }

    private void setDisplay() {
        setPlayerInfo();
        p1_score.setText(String.valueOf(p1_true));
        p2_score.setText(String.valueOf(p2_true));
    }

    private void setPlayerInfo() {
        roomReference.child("player1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(String.class) != null) {
                    String player1_key = snapshot.getValue(String.class);
                    if (player1_key != null)
                        setGameResultText(player1_key, "player1", "player2");
                    database.getReference("/Users/" + player1_key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                if (ds.getKey() != null && ds.getKey().equals("username")) {
                                    p1_username.setText(ds.getValue(String.class));
                                    if (currentUserID.equals(player1_key)) {
                                        updateStats(player1_key, "player1", snapshot);
                                    }
                                    break;
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("player1", error.getDetails());
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("player1", error.getDetails());
            }
        });

        roomReference.child("player2").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String player2_key = snapshot.getValue(String.class);
                if (player2_key != null)
                    setGameResultText(player2_key, "player2", "player1");
                else {
                    Log.e("player2", "Key of Player2 is null in room!");
                }
                database.getReference("/Users/" + player2_key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.getKey() != null && ds.getKey().equals("username")) {
                                p2_username.setText(ds.getValue(String.class));
                                if (currentUserID.equals(player2_key)) {
                                    updateStats(player2_key, "player2", snapshot);
                                }
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("player2", error.getDetails());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("player2", error.getDetails());
            }
        });
    }

    private void updateStats(String player_key, String player, DataSnapshot snapshot) {
        String gameResult = getGameResult();
        for (DataSnapshot ds : snapshot.getChildren()) {
            if (ds.getKey() != null) {
                switch (ds.getKey()) {
                    case "win":
                        if (ds.getValue(Integer.class) != null) {
                            Integer winCount = ds.getValue(Integer.class);
                            if (gameResult.equals(player)) {
                                //Win
                                winCount += 1;
                                database.getReference("/Users/" + player_key).child("win").setValue(winCount);
                            }
                            win.setText(getResources().getString(R.string.win, String.valueOf(winCount)));
                        }
                        break;
                    case "lose":
                        if (ds.getValue(Integer.class) != null) {
                            Integer loseCount = ds.getValue(Integer.class);
                            if (!gameResult.equals(player) && !gameResult.equals("draw")) {
                                //Lose
                                loseCount += 1;
                                database.getReference("/Users/" + player_key).child("lose").setValue(loseCount);
                            }
                            lose.setText(getResources().getString(R.string.lose, String.valueOf(loseCount)));
                        }
                        break;
                    case "point":
                        if (ds.getValue(Integer.class) != null) {
                            Integer totalPoint = ds.getValue(Integer.class);
                            Integer level = 0;
                            Integer progressStatus = 0;
                            int point;
                            if (getGameResult().equals(player)) {
                                //Win
                                point = 20;
                                totalPoint += 20;
                                database.getReference("/Users/" + player_key).child("point").setValue(totalPoint);
                            }
                            else if (getGameResult().equals("draw")) {
                                //Draw
                                point = 10;
                                totalPoint += 10;
                                database.getReference("/Users/" + player_key).child("point").setValue(totalPoint);
                            }
                            else {
                                //Lose
                                point = 0;
                            }
                            if (totalPoint != null) {
                                level = totalPoint / 100;
                                progressStatus = totalPoint % 100;
                            }
                            levelText.setText(getResources().getString(R.string.level, String.valueOf(level)));
                            levelBar.setProgress(progressStatus);
                            pointText.setText(getResources().getString(R.string.point, String.valueOf(point), String.valueOf(progressStatus)));
                        }
                        break;
                }
            }

        }
    }

    private String getGameResult() {
        if (p1_true > p2_true) {
            //Player1 won
            return "player1";
        }
        else if (p1_true < p2_true) {
            //Player2 won
            return "player2";
        }
        else {
            //Draw
            return "draw";
        }
    }

    private void setGameResultText(String player_key, String player_a, String player_b) {
        if (player_key.equals(currentUserID)) {
            if (getGameResult().equals(player_a)) {
                gameResult.setText(R.string.you_win);
            }
            else if (getGameResult().equals(player_b)) {
                gameResult.setText(R.string.you_lose);
            }
            else {
                gameResult.setText(R.string.draw);
            }
        }
    }

    private String getRoomKey() {
        return getIntent().getStringExtra("room");
    }

}