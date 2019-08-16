package com.tinno.android.appinfocollector.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.tinno.android.appinfocollector.view.MyLog;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppDataBase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AppInfo.db";
    private static final String TB_NAME = "appinfo";
    private static final int DATABASE_VERSION = 1;
    private static final String SQL = "creatToaste table " + TB_NAME + " ( " +
            "isSystem INTEGER," +
            "appname TEXT," +
            "packageName TEXT," +
            "versionName TEXT," +
            "versionCode INTEGER," +
            "launcherlist TEXT," +
            "appIcon BLOB," +
            "appDir TEXT" +
            ")";


    public AppDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL);
        MyLog.i(SQL + " start");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static AppDataBase appDataBase;
    private static SQLiteDatabase sqLiteDatabase;
    private static String STRING_EMPTY = "";

    public static synchronized AppDataBase getInstance(Context context) {
        if (appDataBase == null) {
            appDataBase = new AppDataBase(context);
            sqLiteDatabase = appDataBase.getWritableDatabase();
        }
        return appDataBase;
    }

    private String covertLuncher(List<String> list) {
        if (list == null || list.size() == 0) {
            return STRING_EMPTY;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            stringBuilder.append(list.get(i));
            if (i < list.size() - 1) {
                stringBuilder.append(",");
            }
        }

        return stringBuilder.toString();
    }

    private List<String> stringTolist(String str) {
        if (str == null || str.length() == 0) {
            return new ArrayList<>();
        }
        String[] listEntry = str.split(",");
        List<String> resultList = new ArrayList<>(Arrays.asList(listEntry));
        return resultList;
    }


    private synchronized byte[] drawableToByte(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        // 创建一个字节数组输出流,流的大小为size
        ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
        // 设置位图的压缩格式，质量为100%，并放入字节数组输出流中
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        // 将字节数组输出流转化为字节数组byte[]
        byte[] imagedata = baos.toByteArray();
        return imagedata;
    }

    private synchronized Drawable byteToDrawable(byte[] img) {
        Bitmap bitmap;
        if (img != null) {
            bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
            Drawable drawable = new BitmapDrawable(bitmap);
            return drawable;
        }
        return null;
    }


    public boolean clearCache() {
        int count = sqLiteDatabase.delete(TB_NAME, null, null);
        return count != 0;
    }

    public boolean deleteAppinfo(AppInfo appInfo) {
        return deleteAppinfo(appInfo.getPackageName().toString());
    }

    public boolean deleteAppinfo(String packagename) {
        int count = sqLiteDatabase.delete(TB_NAME, "packageName=?",
                new String[]{packagename});
        return count != 0;
    }

    public List<AppInfo> loadAppinfos(boolean isASystem) {
        List<AppInfo> appInfos = new ArrayList<>();
        String isSystem = isASystem ? "0" : "1";
        Cursor cursor = sqLiteDatabase.query(TB_NAME,
                null, "isSystem=?",
                new String[]{isSystem}, null,
                null, null);
        if (cursor != null) {
            AppInfo appInfo;
            while (cursor.moveToNext()) {
                String appname = cursor.getString(cursor.getColumnIndex("appname"));
                String packageName = cursor.getString(cursor.getColumnIndex("packageName"));
                String versionName = cursor.getString(cursor.getColumnIndex("versionName"));
                String appDir = cursor.getString(cursor.getColumnIndex("appDir"));
                List<String> launcherlist = stringTolist(cursor.getString(cursor.getColumnIndex("launcherlist")));
                int versionCode = cursor.getInt(cursor.getColumnIndex("versionCode"));
                Drawable appIcon = byteToDrawable(cursor.getBlob(cursor.getColumnIndex("appIcon")));

                appInfo = new AppInfo();
                appInfo.setAppName(appname);
                appInfo.setPackageName(packageName);
                appInfo.setAppDir(appDir);
                appInfo.setVersionName(versionName);
                appInfo.setVersionCode(versionCode);
                appInfo.setAppIcon(appIcon);
                appInfo.addLauncher(launcherlist);
                appInfos.add(appInfo);

            }
        }

        return appInfos;
    }

    public boolean saveAppinfo(AppInfo appInfo, boolean isASystem) {
        int isSystem = isASystem ? 0 : 1;
        String appname = (String) appInfo.getAppName();
        String packageName = (String) appInfo.getPackageName();
        String versionName = (String) appInfo.getVersionName();
        String launcherlist = covertLuncher(appInfo.getLauncherlist());
        String appDir = (String) appInfo.getAppDir();
        int versionCode = appInfo.getVersionCode();
        byte[] appIcon = drawableToByte(appInfo.getAppIcon());

        ContentValues cv = new ContentValues();
        cv.put("isSystem", isSystem);
        cv.put("appname", appname);
        cv.put("packageName", packageName);
        cv.put("versionName", versionName);
        cv.put("versionCode", versionCode);
        cv.put("launcherlist", launcherlist);
        cv.put("appDir", appDir);
        cv.put("appIcon", appIcon);


        Cursor cursor = sqLiteDatabase.query(TB_NAME,
                null, "packageName=?",
                new String[]{packageName}, null,
                null, null);

        long result = 0;
        if (cursor != null && cursor.moveToNext()) {
            cursor.close();

            MyLog.i("update " + packageName);
            sqLiteDatabase.update(TB_NAME, cv, "packageName=?", new String[]{"+packageName+"});
        } else {
            MyLog.i("insert " + packageName);
            result = sqLiteDatabase.insert(TB_NAME, null, cv);
        }


        return result != 0;
    }


}
