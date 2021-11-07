package com.example.parentsupportapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class TimerActivity extends AppCompatActivity {

    private static final long DEFAULT_START_TIME = 300000;
    private static final long ONE_MIN = 60000;
    private static final long TWO_MIN = 120000;
    private static final long THREE_MIN = 180000;
    private static final long FIVE_MIN = 300000;
    private static final long TEN_MIN = 600000;

    private CountDownTimer countDownTimer;
    private TextView timerView;
    private Button startButton;
    private Button resetButton;
    private Button customMinButton;
    private Button oneMinButton;
    private Button twoMinButton;
    private Button threeMinButton;
    private Button fiveMinButton;
    private Button tenMinButton;
    private boolean isTicking;
    private long timeLeftInMill = DEFAULT_START_TIME;
    private long lastSelectedTime = DEFAULT_START_TIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        initializeButtons();
        setupButtonListeners();
        updateTimerTextView();
    }

    private void initializeButtons() {
        timerView = (TextView) findViewById(R.id.textViewTimer);
        startButton = (Button) findViewById(R.id.buttonStart);
        resetButton = (Button) findViewById(R.id.buttonStop);
        customMinButton = (Button) findViewById(R.id.buttonCustomMin);
        oneMinButton = (Button) findViewById(R.id.buttonOneMin);
        twoMinButton = (Button) findViewById(R.id.buttonTwoMin);
        threeMinButton = (Button) findViewById(R.id.buttonThreeMin);
        fiveMinButton = (Button) findViewById(R.id.buttonFiveMin);
        tenMinButton = (Button) findViewById(R.id.buttonTenMin);
    }

    private void setupButtonListeners() {

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTicking) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerReset();
            }
        });

        customMinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askForCustomTime();
            }
        });

        setOnClickForMinButton(oneMinButton, ONE_MIN);
        setOnClickForMinButton(twoMinButton, TWO_MIN);
        setOnClickForMinButton(threeMinButton, THREE_MIN);
        setOnClickForMinButton(fiveMinButton, FIVE_MIN);
        setOnClickForMinButton(tenMinButton, TEN_MIN);
    }

    private void askForCustomTime() {
        View dialogView = getLayoutInflater().inflate(R.layout.timer_alert_layout, null);

        EditText minuteText = (EditText)dialogView.findViewById(R.id.editTextTimerAlertMinute);
        EditText secondText = (EditText)dialogView.findViewById(R.id.editTextTimerAlertSecond);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Provide time");
        alert.setView(dialogView);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // TODO: Have to check returned value and have to adjust create custom view to
                // TURNS OUT everything there happens after the dialog has been destroyed.
                // need to extract data when the ok button is pressed for the edit text, then
                // i can use this function to store the data above.
                // pass to alert
                // need to validate newTime isn't too large (don't want the kids to starve)

                // need to pull time out here
                Boolean shouldStart = true;
                String minuteString = minuteText.getText().toString();
                String secondString = secondText.getText().toString();
                long extractedMinutes;
                long extractedSeconds;
                try {
                    extractedMinutes = Long.parseLong(minuteString);
                    extractedSeconds = Long.parseLong(secondString);
                    timeLeftInMill = (extractedMinutes * 60 * 1000) + (extractedSeconds * 1000);
                    lastSelectedTime = timeLeftInMill;
                }
                catch (NumberFormatException e) {
                    Toast.makeText(TimerActivity.this, "Please provide a valid time :)", Toast.LENGTH_SHORT).show();
                    shouldStart = false;
                }
                if (!isTicking && shouldStart) {
                    startTimer();
                }
                updateTimerTextView();
            }
        });
        alert.show();
    }

    private void timerReset() {
        if (isTicking) {
            Toast.makeText(TimerActivity.this, "Can't reset while running!", Toast.LENGTH_SHORT).show();
        } else {
            timeLeftInMill = lastSelectedTime;
            updateTimerTextView();
            updateUIShowButtons();
            startButton.setText(R.string.timerActivity_start);
        }
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        isTicking = false;
        startButton.setText(R.string.timerActivity_start);
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMill, 1000) {
            @Override
            public void onTick(long l) {
                timeLeftInMill = l;
                updateTimerTextView();
            }

            @Override
            public void onFinish() {
                // TODO: this (R.string.timerActivity_start) is where I need to alert through a notification
                Toast.makeText(TimerActivity.this, "TIMER COMPLETE!", Toast.LENGTH_SHORT).show();
                pauseTimer();
                timerReset();
                sendNotification();
            }
        }.start();

        startButton.setText(R.string.timerActivity_pause);
        isTicking = true;
        updateUIHideButtons();
    }

    public static final String CHANNEL_ID = "timerActivityChannel1";

    private void sendNotification() {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("Timer Complete!")
                .setContentText("You may free the child O_O")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void updateUIHideButtons() {
        buttonDisappear(oneMinButton);
        buttonDisappear(twoMinButton);
        buttonDisappear(threeMinButton);
        buttonDisappear(fiveMinButton);
        buttonDisappear(tenMinButton);
        buttonDisappear(customMinButton);
        Guideline guideline = (Guideline) findViewById(R.id.guidelineHorizontal);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
        params.guidePercent = (float) 0.7;
        guideline.setLayoutParams(params);
    }

    private void updateUIShowButtons() {
        Guideline guideline = (Guideline) findViewById(R.id.guidelineHorizontal);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
        params.guidePercent = (float) 0.4;
        guideline.setLayoutParams(params);
        buttonAppear(oneMinButton);
        buttonAppear(twoMinButton);
        buttonAppear(threeMinButton);
        buttonAppear(fiveMinButton);
        buttonAppear(tenMinButton);
        buttonAppear(customMinButton);
    }

    private void buttonAppear(Button button) {
        button.setClickable(true);
        button.setVisibility(View.VISIBLE);
    }

    private void buttonDisappear(Button button) {
        button.setClickable(false);
        button.setVisibility(View.GONE);
    }

    private void setOnClickForMinButton(Button btn, long time) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // safety check to make sure it doesn't start new timer while one is already ticking
                if (!isTicking) {
                    timeLeftInMill = time;
                    lastSelectedTime = time;
                    updateTimerTextView();
                    startTimer();
                }
            }
        });
    }

    private void updateTimerTextView() {
        int seconds = (int) timeLeftInMill / 1000 % 60;
        int minutes = (int) timeLeftInMill / 1000 / 60;
        String timeLeft = String.format(Locale.CANADA, "%02d:%02d", minutes, seconds);
        timerView.setText(timeLeft);
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, TimerActivity.class);
    }
}