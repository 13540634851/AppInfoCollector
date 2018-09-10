package com.tinno.android.appinfocollector;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

public class AboutActivity extends BaseActivity {


    private int[] color;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        adjustAppColor(Color.WHITE, false);
        color = new int[]{
                getColor(R.color.colorGreen),
                getColor(R.color.colorRed),
                getColor(R.color.colorBlue),
        };
    }

    int currentColor = 0;

    private Handler mainHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            adjustAppColor(color[currentColor], true);
            currentColor = (currentColor + 1) % 3;
            mainHandle.sendEmptyMessageDelayed(0x777, 3000);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mainHandle.sendEmptyMessage(0x777);
    }

    @Override
    protected void onPause() {
        mainHandle.removeMessages(0x777);
        super.onPause();
    }
}

