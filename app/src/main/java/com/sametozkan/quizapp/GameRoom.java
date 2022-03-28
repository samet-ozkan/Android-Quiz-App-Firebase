package com.sametozkan.quizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GameRoom extends AppCompatActivity implements View.OnClickListener {

    //Database
    private FirebaseDatabase database;
    private DatabaseReference availableRooms;
    private DatabaseReference roomReference;
    private DatabaseReference fullRooms;
    private String roomKey;
    private String currentUserID;
    private final int QUESTION_COUNT_IN_DATABASE = 10;

    //Views
    private TextView player1, player2, questionView, playerTurn;
    private ImageView p1_question1;
    private ImageView p1_question2;
    private ImageView p1_question3;
    private ImageView p1_question4;
    private ImageView p1_question5;
    private ImageView p2_question1;
    private ImageView p2_question2;
    private ImageView p2_question3;
    private ImageView p2_question4;
    private ImageView p2_question5;
    private AppCompatButton choiceA, choiceB, choiceC, choiceD;

    //Lists
    private ArrayList<Button> answerButtons;

    //Animations
    private Animation fadeAnimation;

    //Chronometer
    private Chronometer chronometer;
    private Handler handlerForChronometer;
    private Runnable runnableForChronometer;

    //True answer count
    private int p1_true;
    private int p2_true;

    //Booleans
    boolean stopped = false;
    boolean gameEnd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        initDatabase();
        initViews();
        setChronometer();
        setAnimations();
        setRoom();
    }

    @Override
    protected void onPause() {
        if (!stopped && !gameEnd) {
            DatabaseReference user = database.getReference("/Users").child(currentUserID);
            user.child("lose").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer lose = snapshot.getValue(Integer.class);
                    lose += 1;
                    user.child("lose").setValue(lose).addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e("lose", "Lose count couldn't be updated.");
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("lose", error.getDetails());
                }
            });
            stopped = true;
            chronometer.stop();
            handlerForChronometer.removeCallbacksAndMessages(null);
            user.child("currentRoom").removeValue().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e("currentRoom", "Current room couldn't be removed.");
                }
            });
            hasLeftPopupWindow(stopped);
        }
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        String chosenAnswer = button.getText().toString();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("chosenAnswer", chosenAnswer);
        roomReference.updateChildren(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i("chosenAnswer", "Chosen answer has been updated.");
            }
            else {
                Log.e("chosenAnswer", "Chosen answer hasn't been updated.");
            }
        });
    }

    private void initDatabase() {
        database = FirebaseDatabase.getInstance();
        availableRooms = database.getReference("Rooms").child("AvailableRooms");
        fullRooms = database.getReference("Rooms").child("FullRooms");
        if (getIntent() != null) {
            Intent intent = getIntent();
            roomKey = intent.getStringExtra("room");
            currentUserID = intent.getStringExtra("currentUserID");
        }
        else {
            Log.e("intent", "Intent is null!");
        }
    }

    private void initViews() {
        chronometer = findViewById(R.id.chronometer);
        player1 = findViewById(R.id.player1);
        player2 = findViewById(R.id.player2);
        questionView = findViewById(R.id.question);
        playerTurn = findViewById(R.id.playerTurn);
        choiceA = findViewById(R.id.a);
        choiceB = findViewById(R.id.b);
        choiceC = findViewById(R.id.c);
        choiceD = findViewById(R.id.d);
        p1_question1 = findViewById(R.id.p1_question1);
        p1_question2 = findViewById(R.id.p1_question2);
        p1_question3 = findViewById(R.id.p1_question3);
        p1_question4 = findViewById(R.id.p1_question4);
        p1_question5 = findViewById(R.id.p1_question5);
        p2_question1 = findViewById(R.id.p2_question1);
        p2_question2 = findViewById(R.id.p2_question2);
        p2_question3 = findViewById(R.id.p2_question3);
        p2_question4 = findViewById(R.id.p2_question4);
        p2_question5 = findViewById(R.id.p2_question5);
    }

    private void setChronometer() {
        handlerForChronometer = new Handler();
        runnableForChronometer = () -> {
            chronometer.stop();
            skipToNextQuestion(false);
            handlerForChronometer.removeCallbacksAndMessages(null);
        };
    }

    private void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        handlerForChronometer.postDelayed(runnableForChronometer, 30000);
        chronometer.start();
    }

    private void setAnimations() {
        fadeAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
    }

    private void setRoom() {
        roomReference = fullRooms.child(roomKey);
        HashMap<String, Object> map = setHashMapForRoom();
        roomReference.updateChildren(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                availableRooms.child(roomKey).removeValue().addOnCompleteListener(task1 -> {
                    if (!task1.isSuccessful()) {
                        Log.e("room", "Game room couldn't be deleted from Available Rooms!");
                    }
                });
                setPlayerInfo();
                setAnswerButtons();
                setQuestionListener();
                setHasLeftListener();
            }
            else {
                Log.e("room", "HashMap couldn't be set!");
            }
        });
    }

    private void setPlayerInfo() {
        roomReference.child("player1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String player1_key = snapshot.getValue(String.class);
                database.getReference("/Users/" + player1_key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String key = ds.getKey();
                            if (key != null && key.equals("username")) {
                                player1.setText(ds.getValue(String.class));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("user", error.getDetails());
                    }
                });
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
                database.getReference("/Users/" + player2_key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String key = ds.getKey();
                            if (key != null && key.equals("username")) {
                                player2.setText(ds.getValue(String.class));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("user", error.getDetails());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("player2", error.getDetails());
            }
        });
    }

    private void setAnswerButtons() {
        answerButtons = new ArrayList<>();
        answerButtons.add(choiceA);
        answerButtons.add(choiceB);
        answerButtons.add(choiceC);
        answerButtons.add(choiceD);
        choiceA.setOnClickListener(this);
        choiceB.setOnClickListener(this);
        choiceC.setOnClickListener(this);
        choiceD.setOnClickListener(this);
    }

    private void setQuestionListener() {
        roomReference.child("currentQuestion").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String question = snapshot.getValue(String.class);
                if (question != null) {
                    resetColorOfButtons();
                    setQuestionView(question);
                    setAnswerChoices(question);
                    playerTurn();
                    setAnswerListener(question);
                }
                else {
                    Log.e("currentQuestion", "Question is null.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("currentQuestion", error.getDetails());
            }
        });
    }

    private void resetColorOfButtons() {
        for (Button button : answerButtons) {
            button.setBackgroundColor(Color.MAGENTA);
        }
    }

    private void setQuestionView(String question) {
        database.getReference("/Questions/" + question + "/question").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questionView.setText(snapshot.getValue(String.class));
                questionView.startAnimation(fadeAnimation);
                startChronometer();
                System.out.println(questionView.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("question", error.getDetails());

            }
        });
    }

    private void setAnswerChoices(String question) {
        database.getReference("/Questions/" + question + "/answerChoices").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot answerChoice : snapshot.getChildren()) {
                        String key = answerChoice.getKey();
                        if (key != null) {
                            switch (key) {
                                case "0":
                                    choiceA.setText(answerChoice.getValue(String.class));
                                    choiceA.startAnimation(fadeAnimation);
                                    break;
                                case "1":
                                    choiceB.setText(answerChoice.getValue(String.class));
                                    choiceB.startAnimation(fadeAnimation);
                                    break;
                                case "2":
                                    choiceC.setText(answerChoice.getValue(String.class));
                                    choiceC.startAnimation(fadeAnimation);
                                    break;
                                case "3":
                                    choiceD.setText(answerChoice.getValue(String.class));
                                    choiceD.startAnimation(fadeAnimation);
                                    break;
                                default:
                                    Log.i("answerChoices", "Default case in answerChoices.");
                            }
                        }
                        else {
                            Log.e("answerChoice", "Key of answer choice is null.");
                        }
                    }
                }
                else {
                    Log.e("answerChoices", "Answer choices are null in database.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("answerChoices", error.getDetails());
            }
        });
    }

    private void playerTurn() {
        roomReference.child("player1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String player1_key = snapshot.getValue(String.class);
                    String current_player_key = FirebaseAuth.getInstance().getUid();
                    String player1_username = player1.getText().toString();
                    String player2_username = player2.getText().toString();
                    roomReference.child("index").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Integer value = snapshot.getValue(Integer.class);
                                if (value != null && current_player_key != null && player1_key != null) {
                                    int index = value;
                                    //Player1 turn
                                    if (current_player_key.equals(player1_key) && index % 2 == 0) {
                                        for (Button button : answerButtons) {
                                            button.setBackgroundColor(Color.MAGENTA);
                                        }
                                        playerTurn.setText(getString(R.string.turn, player1_username));
                                        lockAnswers(false);
                                        Log.i("answerChoices", "Answer choices have been unlocked for P1.");
                                    }
                                    //Player2 turn
                                    else if (!current_player_key.equals(player1_key) && index % 2 != 0) {
                                        for (Button button : answerButtons) {
                                            button.setBackgroundColor(Color.MAGENTA);
                                        }
                                        playerTurn.setText(getString(R.string.turn, player2_username));
                                        lockAnswers(false);
                                        Log.i("answerChoices", "Answer choices have been unlocked for P2.");
                                    }
                                    else {
                                        for (Button button : answerButtons) {
                                            button.setBackgroundColor(Color.GRAY);
                                        }
                                        if (index % 2 == 0)
                                            playerTurn.setText(getString(R.string.turn, player1_username));
                                        else
                                            playerTurn.setText(getString(R.string.turn, player2_username));
                                        lockAnswers(true);
                                        Log.i("answerChoices", "Answer choices have been locked.");
                                    }
                                }
                                else {
                                    Log.e("null", "Null pointer exception.");
                                }
                            }
                            else {
                                Log.e("room", "Index is null!");
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("index", error.getDetails());

                        }
                    });
                }
                else {
                    Log.e("player1", "Player1 is null in game room!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("player1", error.getDetails());
            }
        });
    }

    private void setAnswerListener(@NonNull String question) {
        database.getReference("/Questions/" + question + "/correctAnswer").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String correctAnswer = snapshot.getValue(String.class);
                if (correctAnswer != null) {
                    checkAnswer(correctAnswer);
                }
                else {
                    Log.e("correctAnswer", "correctAnswer is null!");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("correctAnswer", error.getDetails());
            }
        });
    }

    private void checkAnswer(@NonNull String correctAnswer) {
        roomReference.child("chosenAnswer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    chronometer.stop();
                    handlerForChronometer.removeCallbacksAndMessages(null);
                    lockAnswers(true);
                    String chosenAnswer = snapshot.getValue(String.class);
                    if (chosenAnswer != null) {
                        for (Button button : answerButtons) {
                            if (button.getText().toString().equals(correctAnswer)) {
                                button.setBackgroundColor(Color.GREEN);
                                break;
                            }
                        }
                        if (chosenAnswer.equals(correctAnswer)) {
                            Log.i("answer", "Correct answer!");
                            skipToNextQuestion(true);
                        }
                        else {
                            Log.i("answer", "Wrong answer!");
                            for (Button button : answerButtons) {
                                if (button.getText().toString().equals(chosenAnswer)) {
                                    button.setBackgroundColor(Color.RED);
                                    break;
                                }
                            }
                            skipToNextQuestion(false);
                        }
                        roomReference.child("chosenAnswer").removeEventListener(this);
                    }
                    else {
                        Log.i("chosenAnswer", "Chosen answer is null.");
                    }

                }
                else {
                    Log.i("chosenAnswer", "Chosen answer is null on database!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("chosenAnswer", error.getDetails());
            }
        });
    }

    private void skipToNextQuestion(boolean tick) {
        roomReference.child("player1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String player1_key = snapshot.getValue(String.class);
                String current_player_key = FirebaseAuth.getInstance().getUid();
                roomReference.child("index").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Integer value = snapshot.getValue(Integer.class);
                            if (value != null && player1_key != null && current_player_key != null) {
                                int index = value;
                                if (tick) {
                                    tick(index);
                                }
                                if (index != 9) {
                                    playerTurn.setText(R.string.next_question);
                                    playerTurn.startAnimation(fadeAnimation);
                                    //Player1 turn
                                    if (current_player_key.equals(player1_key) && index % 2 == 0) {
                                        invokeUpdateQuestion();
                                    }
                                    //Player2 turn
                                    else if (!current_player_key.equals(player1_key) && index % 2 != 0) {
                                        invokeUpdateQuestion();
                                    }
                                    else {
                                        Log.e("playerTurn", "Player turn error.");
                                    }
                                }
                                else {
                                    Log.i("gameResult", "gameResult() has been invoked.");
                                    playerTurn.setText(R.string.game_end);
                                    playerTurn.startAnimation(fadeAnimation);
                                    invokeGameResults();
                                }
                            }
                            else {
                                Log.e("null", "Null pointer exception.");
                            }
                        }
                        else {
                            Log.e("index", "Index is null!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("index", error.getDetails());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("player1", error.getDetails());
            }
        });
    }

    private void tick(int index) {
        int tickView = R.drawable.ic_baseline_check_circle_outline_24;
        if (index % 2 == 0) {
            p1_true++;
        }
        else {
            p2_true++;
        }
        switch (index) {
            //Tick for Player1
            case 0:
                p1_question1.setImageResource(tickView);
                break;
            case 2:
                p1_question2.setImageResource(tickView);
                break;
            case 4:
                p1_question3.setImageResource(tickView);
                break;
            case 6:
                p1_question4.setImageResource(tickView);
                break;
            case 8:
                p1_question5.setImageResource(tickView);
                break;

            //Tick for Player2
            case 1:
                p2_question1.setImageResource(tickView);
                break;
            case 3:
                p2_question2.setImageResource(tickView);
                break;
            case 5:
                p2_question3.setImageResource(tickView);
                break;
            case 7:
                p2_question4.setImageResource(tickView);
                break;
            case 9:
                p2_question5.setImageResource(tickView);
                break;
            default:
                //
        }
    }

    private void invokeUpdateQuestion() {
        Handler handler = new Handler();
        handler.postDelayed(this::updateQuestion, 2000);
    }

    private void invokeGameResults() {
        Handler handler = new Handler();
        handler.postDelayed(this::gameResults, 2000);
    }

    private void setHasLeftListener() {
        roomReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String player1_key = null;
                    String player2_key = null;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String key = ds.getKey();
                        if (key != null && key.equals("player1")) {
                            player1_key = ds.getValue(String.class);
                            if (player2_key != null) {
                                break;
                            }
                        }
                        else if (key != null && key.equals("player2")) {
                            player2_key = ds.getValue(String.class);
                            if (player1_key != null) {
                                break;
                            }
                        }
                        else {
                            Log.e("room", "Room child is null!");
                        }
                    }
                    if (player1_key != null && player2_key != null) {
                        if (currentUserID.equals(player1_key)) {
                            database.getReference("/Users").child(player2_key).child("currentRoom").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        hasLeftPopupWindow(false);
                                    }
                                    else {
                                        Log.e("player2", "Current room of P2 is null!");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("error", error.getDetails());
                                }
                            });
                        }
                        else {
                            database.getReference("/Users").child(player1_key).child("currentRoom").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        hasLeftPopupWindow(false);
                                    }
                                    else {
                                        Log.e("player1", "Current room of P1 is null!");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("player1", error.getDetails());
                                }
                            });
                        }
                    }

                }
                else {
                    Log.e("roomReference", "Room reference is null!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("roomReference", error.getDetails());
            }
        });
    }

    private void hasLeftPopupWindow(Boolean stopped) {
        gameEnd = true;
        lockAnswers(true);
        int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
        View popupView = View.inflate(GameRoom.this, R.layout.popup_hasleft, null);
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, false);
        popupWindow.showAtLocation(popupView.getRootView(), Gravity.CENTER, 0, 0);
        TextView result = popupView.findViewById(R.id.result);
        TextView description = popupView.findViewById(R.id.description);
        Button ok = popupView.findViewById(R.id.ok);
        if (stopped) {
            result.setText(R.string.you_lose);
            description.setText(R.string.lose_description);
        }
        else {
            result.setText(R.string.you_win);
            description.setText(R.string.win_description);
            chronometer.stop();
            handlerForChronometer.removeCallbacksAndMessages(null);
            DatabaseReference user = database.getReference("/Users").child(currentUserID);
            user.child("win").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer win = snapshot.getValue(Integer.class);
                    if (win != null)
                        user.child("win").setValue(++win).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.e("win", "Win couldn't be updated.");
                            }
                        });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("win", error.getDetails());
                }
            });

            user.child("point").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer point = snapshot.getValue(Integer.class);
                    point += 20;
                    user.child("point").setValue(point).addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e("point", "Point couldn't be updated.");
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("point", error.getDetails());
                }
            });
        }
        ok.setOnClickListener(view -> startActivity(new Intent(GameRoom.this, MainActivity.class)));
    }

    private ArrayList<Integer> getRandomQuestions() {
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= QUESTION_COUNT_IN_DATABASE; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        return numbers;
    }

    private HashMap<String, Object> setHashMapForRoom() {
        HashMap<String, Object> hashMap = new HashMap<>();
        ArrayList<Integer> numbers = getRandomQuestions();
        HashMap<String, Object> innerMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            String question = "Question" + numbers.get(i);
            innerMap.put(String.valueOf(i), question);
        }
        hashMap.put("questions", innerMap);
        hashMap.put("currentQuestion", "Question" + numbers.get(0));
        hashMap.put("chosenAnswer", null);
        hashMap.put("index", 0);
        return hashMap;
    }

    private void updateQuestion() {
        Log.i("question", "Question is being updated.");
        roomReference.child("index").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer index = snapshot.getValue(Integer.class);
                if (index != null) {
                    int newIndex = index + 1;
                    String indexOfNextQuestion = String.valueOf(newIndex);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("index", newIndex);
                    roomReference.updateChildren(map).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.i("index", "Index has been increased.");
                        }
                        else {
                            Log.e("index", "Index hasn't been increased.");
                        }
                    });
                    if (newIndex != 10) {
                        roomReference.child("questions").child(indexOfNextQuestion).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String question = snapshot.getValue(String.class);
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("currentQuestion", question);
                                hashMap.put("chosenAnswer", null);
                                roomReference.updateChildren(hashMap).addOnCompleteListener(task -> {
                                    if (!task.isSuccessful()) {
                                        Log.e("currentQuestion", "Current question couldn't be updated.");
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("question", error.getDetails());
                            }
                        });
                    }
                }
                else {
                    Log.e("index", "Index is null!");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("index", error.getDetails());
            }
        });
    }

    private void gameResults() {
        gameEnd = true;
        Intent intent = new Intent(this, GameResults.class);
        intent.putExtra("room", roomKey);
        intent.putExtra("p1_true", p1_true);
        intent.putExtra("p2_true", p2_true);
        startActivity(intent);
        finish();
    }

    private void lockAnswers(boolean bool) {
        choiceA.setClickable(!bool);
        choiceB.setClickable(!bool);
        choiceC.setClickable(!bool);
        choiceD.setClickable(!bool);
    }

}