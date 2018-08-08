package com.tinno.android.appinfocollector;

import android.graphics.drawable.Drawable;

/**
 * Created by weizhengliang on 17-3-16.
 */
public class AppInfo {
    private CharSequence appName;
    private CharSequence packageName;
    private CharSequence versionName;
    private int versionCode = 0;
    private Drawable appIcon = null;
    private CharSequence appDir;

    public void cleanHight(){
        setAppName(appName.toString());
        setPackageName(packageName.toString());
        setVersionName(versionName.toString());
        setAppDir(appDir.toString());
    }

    public void setVersionName(CharSequence versionName) {
        this.versionName = versionName;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void setPackageName(CharSequence packageName) {
        this.packageName = packageName;
    }

    public void setAppName(CharSequence appName) {
        this.appName = appName;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public void setAppDir(CharSequence appDir) {
        this.appDir = appDir;
    }

    public CharSequence getAppDir() {
        return appDir;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public CharSequence getAppName() {
        return appName;
    }

    public CharSequence getPackageName() {
        return packageName;
    }

    public CharSequence getVersionName() {
        return versionName;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
