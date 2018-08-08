package com.tinno.android.appinfocollector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by weizhengliang on 17-3-16.
 */
public class ApplicationInfoUtil {
    public static final int DEFAULT = 0; // 默认 所有应用
    public static final int SYSTEM_APP = DEFAULT + 1; // 系统应用
    public static final int NONSYSTEM_APP = DEFAULT + 2; // 非系统应用


    public static String getProgramNameByPackageName(Context context,
                                                     String packageName) {
        PackageManager pm = context.getPackageManager();
        String name = null;
        try {
            name = pm.getApplicationLabel(
                    pm.getApplicationInfo(packageName,
                            PackageManager.GET_META_DATA)).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }


    public static void getAllProgramInfo(List<AppInfo> allApplist,
                                         Context context) {
        getAllProgramInfo(allApplist, context, DEFAULT);
    }

    public static void getAllProgramInfo(List<AppInfo> applist,
                                         Context context, int type) {
        ArrayList<AppInfo> appList = new ArrayList<AppInfo>(); // 用来存储获取的应用信息数据
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
            switch (type) {
                case NONSYSTEM_APP:
                    if (!isSystemAPP(packageInfo)) {
                        applist.add(tmpInfo);
                    }
                    break;
                case SYSTEM_APP:
                    if (isSystemAPP(packageInfo)) {
                        applist.add(tmpInfo);
                    }
                    break;
                default:
                    applist.add(tmpInfo);
                    break;
            }

        }
    }

    public static List<AppInfo> getAllSystemProgramInfo(Context context) {
        List<AppInfo> systemAppList = new ArrayList<AppInfo>();
        getAllProgramInfo(systemAppList, context, SYSTEM_APP);
        return systemAppList;
    }

    public static List<AppInfo> getAllNonsystemProgramInfo(Context context) {
        List<AppInfo> nonsystemAppList = new ArrayList<AppInfo>();
        getAllProgramInfo(nonsystemAppList, context, NONSYSTEM_APP);
        return nonsystemAppList;
    }

    public static Boolean isSystemAPP(PackageInfo packageInfo) {
        if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { // 非系统应用
            return false;
        } else { // 系统应用
            return true;
        }
    }

    public static void search(List<AppInfo> allApplist, final String k) {
        Log.i("wangcan", "k=" + k);
        if(allApplist.size()==0){
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
                boolean o1ok = isAppinfoContainKey(o1, k);
                boolean o2ok = isAppinfoContainKey(o2, k);
                if (o1ok == o2ok) {
                    o1.cleanHight();
                    o2.cleanHight();
                    return o1.getAppName().toString().compareToIgnoreCase(o2.getAppName().toString());
                } else {
                    return o1ok ? -1 : 1;
                }
            }
        });

    }

    private static boolean isAppinfoContainKey(AppInfo info, String onekey) {
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
        return isContainKey;

    }

    public static SpannableString highlight(String text, String target) {
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
}
