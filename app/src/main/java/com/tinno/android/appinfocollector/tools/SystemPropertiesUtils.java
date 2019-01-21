package com.tinno.android.appinfocollector.tools;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemPropertiesUtils {

    private static final String TAG = "MainActivity";
    private static HashMap<String, String> keyValMap;

    private static void loadSystemProperties() {
        if (keyValMap == null) {
            keyValMap = new HashMap<>(30);
        }
        try {
            Process p = Runtime.getRuntime().exec("getprop");
            p.waitFor();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String temp = "";
            int keyStart, keyStop, valStart, valStop;
            String key, val;
            while ((temp = stdInput.readLine()) != null) {
                //[sys.ylog.svc.ftrace]: [stopped]
                if (!TextUtils.isEmpty(temp)) {
                    keyStart = temp.indexOf("[") + 1;
                    keyStop = temp.indexOf("]");
                    valStart = temp.lastIndexOf("[") + 1;
                    valStop = temp.lastIndexOf("]");
                    key = temp.substring(keyStart, keyStop);
                    val = temp.substring(valStart, valStop);
                    keyValMap.put(key, val);
                    Log.d("HHH", "key=" + key + " val=" + val);
                }
            }
        } catch (InterruptedException e) {

        } catch (IOException e) {

        }


    }


    public static String get(String key, String def) {
        if (keyValMap == null) {
            loadSystemProperties();
        }
        String ret = keyValMap.get(key);
        return ret == null ? def : ret;
    }

    public static int getInt(String key, int def) {
        if (keyValMap == null) {
            loadSystemProperties();
        }
        String ret = keyValMap.get(key);
        if (isNumeric(ret)) {
            return Integer.valueOf(ret);
        } else {
            return def;
        }
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }


    public static boolean getBoolean(String key, boolean def) {
        if (keyValMap == null) {
            loadSystemProperties();
        }
        String ret = keyValMap.get(key);
        return ret.equals("true");
    }
}