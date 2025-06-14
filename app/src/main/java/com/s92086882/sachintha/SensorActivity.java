package com.s92086882.sachintha;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ValueAnimator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.appbar.MaterialToolbar;

import android.view.View;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor temperatureSensor;
    private TextView textViewTemp;
    private final float THRESHOLD = 82f; // My SID is: S92086882. Temperature threshold = SID last two digits
    private MediaPlayer mediaPlayer;
    private boolean hasPlayed = false;
    private TextView textViewStatus;
    private LottieAnimationView lottieView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sensor);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find TextView
        textViewTemp = findViewById(R.id.textViewTemp);

        textViewStatus = findViewById(R.id.textViewStatus);

        lottieView = findViewById(R.id.lottieView);

        // Load audio
        mediaPlayer = MediaPlayer.create(this, R.raw.soundtrack_cosmos);

        // Get temperature sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            if (temperatureSensor == null) {
                Toast.makeText(this, "Ambient temperature sensor not available", Toast.LENGTH_LONG).show();
            }
        }

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> {
            finish(); // Go back to the previous activity (MapActivity)
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (temperatureSensor != null) {
            sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        // Re-create mediaPlayer if released
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.soundtrack_cosmos);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (temperatureSensor != null) {
            sensorManager.unregisterListener(this);
        }

        // Fade out audio
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            ValueAnimator fadeOut = ValueAnimator.ofFloat(1f, 0f);
            fadeOut.setDuration(1500); // 1.5 second fade
            fadeOut.addUpdateListener(animation -> {
                float volume = (float) animation.getAnimatedValue();
                mediaPlayer.setVolume(volume, volume);
            });
            fadeOut.start();

            // After fade completes, stop and release mediaPlayer
            fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    hasPlayed = false;
                }
            });
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float temp = event.values[0];
        textViewTemp.setText("Temperature: " + temp + "°C");

        if (temp >= THRESHOLD && !hasPlayed) {
            mediaPlayer.start();
            hasPlayed = true;
            textViewStatus.setText("Audio Playing 🎵");
            textViewStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            lottieView.setVisibility(View.VISIBLE);
            lottieView.playAnimation();
        } else if (temp < THRESHOLD && hasPlayed) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause(); // or mediaPlayer.stop();
                mediaPlayer.seekTo(0); // Reset to start
            }
            hasPlayed = false;
            textViewStatus.setText("No Audio");
            textViewStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            lottieView.pauseAnimation();
            lottieView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}