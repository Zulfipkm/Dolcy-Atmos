package com.dolcy.atmos;

import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Equalizer equalizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("Dolcy Atmos Equalizer");
        title.setTextSize(22);
        layout.addView(title);

        try {
            equalizer = new Equalizer(0, 0);
            equalizer.setEnabled(true);

            short bands = equalizer.getNumberOfBands();
            final short minLevel = equalizer.getBandLevelRange()[0];
            final short maxLevel = equalizer.getBandLevelRange()[1];

            for (short i = 0; i < bands; i++) {
                final short band = i;
                TextView freqView = new TextView(this);
                freqView.setText((equalizer.getCenterFreq(band) / 1000) + " Hz");
                layout.addView(freqView);

                SeekBar seekBar = new SeekBar(this);
                seekBar.setMax(maxLevel - minLevel);
                seekBar.setProgress(equalizer.getBandLevel(band) - minLevel);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        equalizer.setBandLevel(band, (short) (progress + minLevel));
                    }
                    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                });
                layout.addView(seekBar);
            }
        } catch (Exception e) {
            TextView errorView = new TextView(this);
            errorView.setText("No global audio session available.");
            layout.addView(errorView);
        }

        setContentView(layout);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (equalizer != null) {
            equalizer.release();
        }
    }
}
