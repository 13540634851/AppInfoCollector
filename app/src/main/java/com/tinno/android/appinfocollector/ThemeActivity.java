package com.tinno.android.appinfocollector;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

public class ThemeActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private SeekBar redSeek, greenSeek, blueSeek;
    private FrameLayout themeSHow;
    private int red, blue, green;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_theme);

        redSeek = findViewById(R.id.red_sk);
        greenSeek = findViewById(R.id.green_sk);
        blueSeek = findViewById(R.id.blue_sk);
        themeSHow = findViewById(R.id.theme_show);

        redSeek.setMax(255);
        greenSeek.setMax(255);
        blueSeek.setMax(255);

        redSeek.setOnSeekBarChangeListener(this);
        blueSeek.setOnSeekBarChangeListener(this);
        greenSeek.setOnSeekBarChangeListener(this);


    }

    @Override
    protected void onResume() {
        int color = getSetting(THEME, getCurrentColor());
        red = (color & 0xff0000) >> 16;
        green = (color & 0x00ff00) >> 8;
        blue = (color & 0x0000ff);

        redSeek.setProgress(red);
        greenSeek.setProgress(green);
        blueSeek.setProgress(blue);

        themeSHow.setBackgroundColor(color);
        super.onResume();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == redSeek) {
            red = progress;
        } else if (seekBar == greenSeek) {
            green = progress;
        } else if (seekBar == blueSeek) {
            blue = progress;
        }
        themeSHow.setBackgroundColor(Color.rgb(red, green, blue));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void onclick(View v) {
        if (v.getId() == R.id.cancel) {

        } else {
            setSetting(THEME, Color.rgb(red, green, blue));
        }
        finish();
    }
}
