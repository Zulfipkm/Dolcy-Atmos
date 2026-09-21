package com.dolcy.atmos;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_AUDIO_PERMISSION = 101;

    private BassBoost bassBoost;
    private Virtualizer virtualizer;
    private Equalizer equalizer;
    private Visualizer audioVisualizer;
    private AudioManager audioManager;

    private SwitchCompat switchDolby;
    private Button btnDynamic, btnMovie, btnMusic, btnVoice;
    private SeekBar sbBass, sbSurround, sbClarity;
    private View[] bars = new View[7];

    private Handler animHandler = new Handler(Looper.getMainLooper());
    private Runnable waveRunnable;
    private Random random = new Random();
    private boolean isDolbyEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        initViews();
        initAudioEffects();
        setupListeners();
        requestAudioPermission();
        startMusicDetectionEngine();
    }

    private void initViews() {
        switchDolby = findViewById(R.id.switchDolby);
        btnDynamic = findViewById(R.id.btnDynamic);
        btnMovie = findViewById(R.id.btnMovie);
        btnMusic = findViewById(R.id.btnMusic);
        btnVoice = findViewById(R.id.btnVoice);

        sbBass = findViewById(R.id.sbBass);
        sbSurround = findViewById(R.id.sbSurround);
        sbClarity = findViewById(R.id.sbClarity);

        bars[0] = findViewById(R.id.bar1);
        bars[1] = findViewById(R.id.bar2);
        bars[2] = findViewById(R.id.bar3);
        bars[3] = findViewById(R.id.bar4);
        bars[4] = findViewById(R.id.bar5);
        bars[5] = findViewById(R.id.bar6);
        bars[6] = findViewById(R.id.bar7);

        resetBarsToFlat();
        applyProfile("Dynamic");
    }

    private void initAudioEffects() {
        try {
            bassBoost = new BassBoost(0, 0);
            bassBoost.setEnabled(true);

            virtualizer = new Virtualizer(0, 0);
            virtualizer.setEnabled(true);

            equalizer = new Equalizer(0, 0);
            equalizer.setEnabled(true);
        } catch (Exception ignored) {}
    }

    private void requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQ_AUDIO_PERMISSION);
        } else {
            attachSystemVisualizer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_AUDIO_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            attachSystemVisualizer();
        }
    }

    private void attachSystemVisualizer() {
        try {
            if (audioVisualizer != null) {
                audioVisualizer.release();
            }
            audioVisualizer = new Visualizer(0);
            audioVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
            audioVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    if (isDolbyEnabled && audioManager != null && audioManager.isMusicActive()) {
                        if (waveform != null && waveform.length >= 7) {
                            runOnUiThread(() -> updateBarsFromWave(waveform));
                        }
                    }
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {}
            }, Visualizer.getMaxCaptureRate() / 2, true, false);

            audioVisualizer.setEnabled(true);
        } catch (Exception ignored) {}
    }

    private void updateBarsFromWave(byte[] waveform) {
        for (int i = 0; i < 7; i++) {
            int raw = Math.abs((int) waveform[i * (waveform.length / 7)]);
            int targetHeight = Math.max(25, Math.min(220, raw * 3));
            ViewGroup.LayoutParams params = bars[i].getLayoutParams();
            params.height = targetHeight;
            bars[i].setLayoutParams(params);
        }
    }

    // Checks live playback: only dances when music is actually playing on the phone
    private void startMusicDetectionEngine() {
        waveRunnable = new Runnable() {
            @Override
            public void run() {
                boolean isMusicPlaying = audioManager != null && audioManager.isMusicActive();

                if (isDolbyEnabled && isMusicPlaying) {
                    for (int i = 0; i < 7; i++) {
                        int base = 35;
                        int variance = random.nextInt(165);
                        int targetHeight = base + variance;

                        ViewGroup.LayoutParams params = bars[i].getLayoutParams();
                        params.height = targetHeight;
                        bars[i].setLayoutParams(params);
                    }
                } else {
                    // Smoothly fall down to flat when music stops or paused
                    resetBarsToFlat();
                }
                animHandler.postDelayed(this, 100);
            }
        };
        animHandler.post(waveRunnable);
    }

    private void resetBarsToFlat() {
        for (View bar : bars) {
            if (bar != null) {
                ViewGroup.LayoutParams params = bar.getLayoutParams();
                if (params.height != 20) {
                    params.height = 20;
                    bar.setLayoutParams(params);
                }
            }
        }
    }

    private void setupListeners() {
        switchDolby.setOnCheckedChangeListener((btn, isChecked) -> {
            isDolbyEnabled = isChecked;
            if (bassBoost != null) bassBoost.setEnabled(isChecked);
            if (virtualizer != null) virtualizer.setEnabled(isChecked);
            if (equalizer != null) equalizer.setEnabled(isChecked);
            if (audioVisualizer != null) audioVisualizer.setEnabled(isChecked);

            if (!isChecked) {
                resetBarsToFlat();
            }
        });

        sbBass.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (bassBoost != null && bassBoost.getStrengthSupported()) {
                    try {
                        bassBoost.setStrength((short) progress);
                    } catch (Exception ignored) {}
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sbSurround.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (virtualizer != null && virtualizer.getStrengthSupported()) {
                    try {
                        virtualizer.setStrength((short) progress);
                    } catch (Exception ignored) {}
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnDynamic.setOnClickListener(v -> applyProfile("Dynamic"));
        btnMovie.setOnClickListener(v -> applyProfile("Movie"));
        btnMusic.setOnClickListener(v -> applyProfile("Music"));
        btnVoice.setOnClickListener(v -> applyProfile("Voice"));
    }

    private void applyProfile(String profile) {
        resetButtons();
        switch (profile) {
            case "Dynamic":
                highlightButton(btnDynamic);
                sbBass.setProgress(700);
                sbSurround.setProgress(800);
                sbClarity.setProgress(600);
                break;
            case "Movie":
                highlightButton(btnMovie);
                sbBass.setProgress(850);
                sbSurround.setProgress(1000);
                sbClarity.setProgress(750);
                break;
            case "Music":
                highlightButton(btnMusic);
                sbBass.setProgress(600);
                sbSurround.setProgress(500);
                sbClarity.setProgress(500);
                break;
            case "Voice":
                highlightButton(btnVoice);
                sbBass.setProgress(200);
                sbSurround.setProgress(300);
                sbClarity.setProgress(900);
                break;
        }
    }

    private void resetButtons() {
        int inactiveColor = Color.parseColor("#1F2235");
        int textInactive = Color.parseColor("#A0A0B5");

        Button[] buttons = {btnDynamic, btnMovie, btnMusic, btnVoice};
        for (Button btn : buttons) {
            btn.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
            btn.setTextColor(textInactive);
        }
    }

    private void highlightButton(Button btn) {
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5E5CE6")));
        btn.setTextColor(Color.WHITE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioVisualizer != null) audioVisualizer.release();
        if (animHandler != null && waveRunnable != null) {
            animHandler.removeCallbacks(waveRunnable);
        }
    }
}
