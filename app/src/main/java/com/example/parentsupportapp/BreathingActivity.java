package com.example.parentsupportapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parentsupportapp.breathingModel.ExhaleState;
import com.example.parentsupportapp.breathingModel.FinishState;
import com.example.parentsupportapp.breathingModel.IdleState;
import com.example.parentsupportapp.breathingModel.InhaleState;
import com.example.parentsupportapp.breathingModel.State;
import com.example.parentsupportapp.breathingModel.WaitingState;

public class BreathingActivity extends AppCompatActivity {
    private static final String EXTRA_NUM_OF_BREATHS = "breathingActivity.num_of_breaths";
    private static final int SECONDS_TO_MILLISECONDS = 1000;
    public final State inhaleState = new InhaleState(this);
    public final State exhaleState = new ExhaleState(this);
    public final State waitingState = new WaitingState(this);
    public final State finishState = new FinishState(this);
    private State currentState = new IdleState(this);
    public int numberOfBreaths;
    public Button btnBreathe;
    public TextView txtMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing);
        setupViews();
        setupButtons();
        numberOfBreaths = getIntent().getIntExtra(EXTRA_NUM_OF_BREATHS, 1);
        setState(inhaleState);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupButtons() {
        btnBreathe.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (view.isPressed() && motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    long timeHeld = motionEvent.getEventTime() - motionEvent.getDownTime();
                    Toast.makeText(BreathingActivity.this, Long.toString(timeHeld), Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });
    }

    public void setupViews() {
        btnBreathe = findViewById(R.id.btnBreathe);
        txtMain = findViewById(R.id.tvDemo);
    }

    public void setState(State newState) {
        currentState.handleExit();
        currentState = newState;
        currentState.handleEnter();
    }

    public static Intent makeIntent(Context c, int numOfBreaths) {
        Intent intent = new Intent(c, BreathingActivity.class);
        intent.putExtra(EXTRA_NUM_OF_BREATHS, numOfBreaths);
        return intent;
    }
}