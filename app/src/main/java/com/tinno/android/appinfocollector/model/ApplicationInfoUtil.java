package com.tinno.android.appinfocollector.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.tinno.android.appinfocollector.view.MyLog;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by weizhengliang on 17-3-16.
 */
public class ApplicationInfoUtil {
    //Launch(1,0,0)  Myos(0,1,0)  uninstall_APP(0,0,1) all(0,0,0)
    public static final int UNINSTALL = 1; // uninstall应用
    public static final int ALL = 0; // all应用
    public static final int MYOS_APP = UNINSTALL << 1; // myos应用
    public static final int LAUNCH_APP = UNINSTALL << 2; // launch应用

    private AppDataBase appDataBase;
    private static ApplicationInfoUtil intance;
    private Context context;
    private List<AppInfo> sysApplist, nonsysApplist;

    public static ApplicationInfoUtil getIntance(Context context) {
        if (intance == null) {
            synchronized (ApplicationInfoUtil.class) {
                intance = new ApplicationInfoUtil(context.getApplicationContext());
            }
        }
        return intance;
    }

    private ApplicationInfoUtil(Context context) {
        this.context = context;
        sysApplist = new ArrayList<>();
        nonsysApplist = new ArrayList<>();
        appDataBase = AppDataBase.getInstance(context);
    }

    public void setCache() {
        for (AppInfo appInfo : sysApplist) {
            appDataBase.saveAppinfo(appInfo, true);
        }
        for (AppInfo appInfo : nonsysApplist) {
            appDataBase.saveAppinfo(appInfo, false);
        }
    }


    public boolean getCache() {
        List<AppInfo> sysApps = appDataBase.loadAppinfos(true);
        if (sysApps != null) {
            sysApplist.clear();
            sysApplist.addAll(sysApps);
        }

        List<AppInfo> nosysApps = appDataBase.loadAppinfos(false);
        if (nosysApps != null) {
            nonsysApplist.clear();
            nonsysApplist.addAll(nosysApps);
        }

        MyLog.i("sysApplist :" + sysApplist.size());
        MyLog.i("nonsysApplist :" + nonsysApplist.size());
        return false;
    }

    public void sync(boolean cacheFirst) {

        if (!cacheFirst) {
            sysApplist.clear();
            nonsysApplist.clear();
        } else {
            boolean isOk = getCache();
            MyLog.i("get databae " + isOk);
            if (isOk) {
                return;
            }
        }
        if (sysApplist != null && sysApplist.size() > 0) {
            return;
        }
        List<PackageInfo> packages = context.getPackageManager()
                .getInstalledPackages(0);

        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            AppInfo tmpInfo = new AppInfo();
            tmpInfo.setAppName(packageInfo.applicationInfo.loadLabel(
                    context.getPackageManager()).toString());
            tmpInfo.setAppDir(packageInfo.applicationInfo.sourceDir);

            tmpInfo.setPackageName(packageInfo.packageName);
            tmpInfo.setVersionName(packageInfo.versionName);
            tmpInfo.setVersionCode(packageInfo.versionCode);
            tmpInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(context
                    .getPackageManager()));

            tmpInfo.addLauncher(getLaunchActivities(context, tmpInfo.getPackageName().toString()));

            if (isSystemAPP(packageInfo)) {
                sysApplist.add(tmpInfo);
            } else {
                nonsysApplist.add(tmpInfo);
            }
        }
        setCache();
    }

    public AppInfo getAppInfoByPackageName(String packageName) {
        PackageManager pm = context.getPackageManager();
        AppInfo appInfo = new AppInfo();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            appInfo.setAppName(packageInfo.applicationInfo.loadLabel(
                    context.getPackageManager()).toString());
            appInfo.setAppDir(packageInfo.applicationInfo.sourceDir);

            appInfo.setPackageName(packageInfo.packageName);
            appInfo.setVersionName(packageInfo.versionName);
            appInfo.setVersionCode(packageInfo.versionCode);
            appInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(context
                    .getPackageManager()));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return appInfo;
    }


    public List<AppInfo> getAppInfo(int type) {
        List<AppInfo> rtrAppInfos = new ArrayList<>();
        Log.i("wangcan", "应用：" + Integer.toBinaryString(type));
        if ((type & UNINSTALL) == UNINSTALL) {
            rtrAppInfos.addAll(nonsysApplist);
            Log.i("wangcan", "可卸载的应用");
        } else {
            rtrAppInfos.addAll(sysApplist);
            rtrAppInfos.addAll(nonsysApplist);
            Log.i("wangcan", "所有的应用");
        }

        if ((type & MYOS_APP) == MYOS_APP) {
            Log.i("wangcan", "myos的应用");
            Iterator<AppInfo> allinfo = rtrAppInfos.iterator();
            AppInfo info;
            while (allinfo.hasNext()) {
                info = allinfo.next();
                if (!info.getAppDir().toString().toLowerCase().contains("/ape")) {
                    allinfo.remove();
                }
            }
        } else {
            Log.i("wangcan", "非myos的应用");
        }

        if ((type & LAUNCH_APP) == LAUNCH_APP) {
            Log.i("wangcan", "launch的应用");
            Iterator<AppInfo> allinfo = rtrAppInfos.iterator();
            AppInfo info;
            while (allinfo.hasNext()) {
                info = allinfo.next();
                if (info.getLauncherlist().size() == 0) {
                    allinfo.remove();
                }
            }
        } else {
            Log.i("wangcan", "非launch的应用");
        }
        return rtrAppInfos;
    }

    public Boolean isSystemAPP(PackageInfo packageInfo) {
        if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { // 非系统应用
            return false;
        } else { // 系统应用
            return true;
        }
    }

    public void search(List<AppInfo> allApplist, final String k) {
        if (allApplist.size() == 0) {
            return;
        }
        String key = (k == null) ? null : k.trim();
        if (k == null || k.length() == 0) {
            Collections.sort(allApplist, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    o1.cleanHight();
                    o2.cleanHight();
                    return o1.getAppName().toString().compareToIgnoreCase(o2.getAppName().toString());
                }
            });
            return;
        }

        Collections.sort(allApplist, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                o2.cleanHight();
                o1.cleanHight();
                boolean o1ok = isAppinfoContainKey(o1, k);
                boolean o2ok = isAppinfoContainKey(o2, k);

                if (o1ok == o2ok) {
                    return o1.getAppName().toString().compareToIgnoreCase(o2.getAppName().toString());
                } else {
                    return o1ok ? -1 : 1;
                }
            }
        });

    }

    private boolean isAppinfoContainKey(AppInfo info, String onekey) {
        boolean isContainKey = false;
        String key = onekey.toLowerCase();
        if (info.getAppDir().toString().toLowerCase().contains(key)) {
            info.setAppDir(highlight(info.getAppDir().toString(), key));
            isContainKey = true;
        }
        if (info.getAppName().toString().toLowerCase().contains(key)) {
            info.setAppName(highlight(info.getAppName().toString(), key));
            isContainKey = true;
        }

        if (info.getPackageName().toString().toLowerCase().contains(key)) {
            info.setPackageName(highlight(info.getPackageName().toString(), key));
            isContainKey = true;
        }

        if (info.getVersionName().toString().toLowerCase().contains(key)) {
            info.setVersionName(highlight(info.getVersionName().toString(), key));
            isContainKey = true;
        }

        return isContainKey;

    }

    public SpannableString highlight(String text, String target) {
        return highlight(text, target, Color.RED);
    }

    public static SpannableString highlight(String text, String target, int color) {
        SpannableString spannableString = new SpannableString(text);
        Pattern pattern = Pattern.compile(target.toLowerCase());
        Matcher matcher = pattern.matcher(text.toLowerCase());
        while (matcher.find()) {
            ForegroundColorSpan span = new ForegroundColorSpan(color);
            spannableString.setSpan(span, matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }


    private List<String> getLaunchActivities(Context context, String packageName) {
        Intent localIntent = new Intent("android.intent.action.MAIN", null);
        localIntent.addCategory("android.intent.category.LAUNCHER");
        localIntent.addCategory("android.intent.category.DEFAULT");
        List<ResolveInfo> appList = context.getPackageManager().queryIntentActivities(localIntent, 0);
        List<String> activitys = new ArrayList<>();
        for (int i = 0; i < appList.size(); i++) {
            ResolveInfo resolveInfo = appList.get(i);
            String packageStr = resolveInfo.activityInfo.packageName;
            if (packageStr.equals(packageName)) {
                //这个就是你想要的那个Activity
                activitys.add(resolveInfo.activityInfo.name);
            }
        }
        return activitys;
    }
}
