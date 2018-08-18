package com.tinno.android.appinfocollector;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

public class AboutActivity extends AppCompatActivity {
    private ColorDrawable mBackGround;
    private ValueAnimator valueAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        adjustAppColor(Color.WHITE, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adjustAppColor(getColor(R.color.colorPrimary), true);
    }

    protected void adjustAppColor(@ColorInt int color, boolean animal) {
        if (mBackGround == null) {
            mBackGround = new ColorDrawable(color);
            getWindow().setBackgroundDrawable(mBackGround);
            getWindow().setStatusBarColor(color);
            if (hasNavigationBar()) {
                getWindow().setNavigationBarColor(color);
            }
            return;
        }
        int currentColor = mBackGround.getColor();
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator = null;
        }
        if (currentColor != color) {
            if (animal) {
                valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), currentColor, color);
                valueAnimator.setDuration(3000);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int color = (int) animation.getAnimatedValue();
                        mBackGround.setColor(color);
                        getWindow().setStatusBarColor(color);
                        if (hasNavigationBar()) {
                            getWindow().setNavigationBarColor(color);
                        }
                    }
                });

                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        valueAnimator = null;
                    }
                });
                valueAnimator.start();
            } else {
                mBackGround.setColor(color);
                getWindow().setStatusBarColor(color);
                if (hasNavigationBar()) {
                    getWindow().setNavigationBarColor(color);
                }
            }


        }
    }

    public boolean hasNavigationBar() {

        //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
        boolean hasMenuKey = ViewConfiguration.get(this)
                .hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);

        if (!hasMenuKey && !hasBackKey) {
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator = null;
        }
    }
}

