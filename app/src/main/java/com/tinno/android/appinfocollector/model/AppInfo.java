package com.tinno.android.appinfocollector.model;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weizhengliang on 17-3-16.
 */
public class AppInfo {
    private CharSequence appName;
    private CharSequence packageName;
    private CharSequence versionName;
    private int versionCode = 0;
    private List<String> launcherlist;
    private Drawable appIcon = null;
    private CharSequence appDir;

    public void cleanHight() {
        setAppName(getAppName().toString());
        setPackageName(getPackageName().toString());
        setVersionName(getVersionName().toString());
        setAppDir(getAppDir().toString());
    }

    public void addLauncher(String luncher) {
        if (launcherlist == null) {
            launcherlist = new ArrayList<>();
        }
        if (!launcherlist.contains(luncher)) {
            launcherlist.add(luncher);
        }
    }

    public List<String> getLauncherlist() {
        return launcherlist;
    }

    public void addLauncher(List<String> luncher) {
        if (launcherlist == null) {
            launcherlist = new ArrayList<>();
        }
        launcherlist.clear();
        launcherlist.addAll(luncher);
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
        if(appDir==null){
            return "NULL";
        }
        return appDir;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public CharSequence getAppName() {
        if(appName==null){
            return "NULL";
        }
        return appName;
    }

    public CharSequence getPackageName() {
        if(packageName==null){
            return "NULL";
        }
        return packageName;
    }

    public CharSequence getVersionName() {
        if(versionName==null){
            return "NULL";
        }
        return versionName;
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        if(launcherlist!=null&&launcherlist.size()>0){
            for(String l:launcherlist){
                sb.append("\n    "+l);
            }
        }
        return "应用名称:" +appName+
                "\n  包名:" +packageName+
                "\n  启动Activity:" +sb.toString()+
                "\n  路径：" +appDir+
                "\n  版本信息:"+versionName+"["+versionCode+"]";
    }
}
