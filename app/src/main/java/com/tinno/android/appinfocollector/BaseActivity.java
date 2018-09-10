package com.tinno.android.appinfocollector;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

public class BaseActivity extends AppCompatActivity {

    private ColorDrawable mBackGround;
    private ValueAnimator valueAnimator;
    public final static String THEME="theme";
    protected int getCurrentColor() {
        if (mBackGround != null) {
            return mBackGround.getColor();
        } else {
            return 0;
        }
    }

    public final String getSetting(String key, String def) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(key, def);
    }

    public final int getSetting(String key, int def) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getInt(key, def);
    }

    public final void setSetting(String key, String val) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, val);
        editor.commit();
    }

    public final void setSetting(String key, int val) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, val);
        editor.commit();
    }

    protected void adjustAppColor(@ColorInt int color, boolean animal) {
        if (mBackGround == null) {
            mBackGround = new ColorDrawable(color);
            getWindow().setBackgroundDrawable(mBackGround);
//            getWindow().setStatusBarColor(color);
            colorChange(color);
            if (hasNavigationBar()) {
                getWindow().setNavigationBarColor(color);
            }
            return;
        }
        int currentColor = getCurrentColor();
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
                        colorChange(color);
//                        getWindow().setStatusBarColor(color);
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
//                getWindow().setStatusBarColor(color);
                colorChange(color);
                if (hasNavigationBar()) {
                    getWindow().setNavigationBarColor(color);
                }
            }


        }
    }

    protected void colorChange(int color) {
        Log.i("UUUU", "" + color);
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
