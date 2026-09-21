package com.dolcy.atmos;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class MainActivity extends AppCompatActivity {

    private BassBoost bassBoost;
    private Virtualizer virtualizer;
    private Equalizer equalizer;
    private Visualizer audioVisualizer;

    private SwitchCompat switchDolby;
    private Button btnDynamic, btnMovie, btnMusic, btnVoice;
    private SeekBar sbBass, sbSurround, sbClarity;
    private View[] bars = new View[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initAudioEffects();
        initLiveVisualizer();
        setupListeners();
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

    private void initLiveVisualizer() {
        try {
            audioVisualizer = new Visualizer(0);
            audioVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
            audioVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    if (waveform != null && waveform.length >= 7) {
                        runOnUiThread(() -> {
                            for (int i = 0; i < 7; i++) {
                                int raw = Math.abs((int) waveform[i * (waveform.length / 7)]);
                                int height = Math.max(25, Math.min(200, raw * 2));
                                ViewGroup.LayoutParams params = bars[i].getLayoutParams();
                                params.height = height;
                                bars[i].setLayoutParams(params);
                            }
                        });
                    }
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {}
            }, Visualizer.getMaxCaptureRate() / 2, true, false);

            audioVisualizer.setEnabled(true);
        } catch (Exception ignored) {}
    }

    private void setupListeners() {
        switchDolby.setOnCheckedChangeListener((btn, isChecked) -> {
            if (bassBoost != null) bassBoost.setEnabled(isChecked);
            if (virtualizer != null) virtualizer.setEnabled(isChecked);
            if (equalizer != null) equalizer.setEnabled(isChecked);
            if (audioVisualizer != null) audioVisualizer.setEnabled(isChecked);
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
    }
}
