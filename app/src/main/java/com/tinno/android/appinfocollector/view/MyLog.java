package com.tinno.android.appinfocollector.view;

import android.util.Log;

public class MyLog {
    private static String TAG_DEFAULT = "wangcan";
    private static boolean DEBUG = true;

    public static void i(String TAG, String message) {
        if (DEBUG) {
            Log.i(TAG, message);
        }
    }

    public static void i(String message) {
        i(TAG_DEFAULT, message);
    }
}
