package com.tinno.android.appinfocollector.presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.tinno.android.appinfocollector.model.AppInfo;
import com.tinno.android.appinfocollector.model.ApplicationInfoUtil;
import com.tinno.android.appinfocollector.model.WorkTask;

import java.util.List;

public class AppInfoTask {
    public interface AppLoadListener {
        public void preDone();

        public void done(List<AppInfo> appinfos);

        public void appChange();

        public void updateFail();
    }


    private AppLoadListener loadListener;
    private HandlerThread handlerThread;
    private Handler workHandler, mainHandler;
    private ApplicationInfoUtil applicationInfoUtil;
    private static final int UPDATE_APP = 0x777;

//    private WorkTask workTask;

    private AppInfoTask() {
        handlerThread = new HandlerThread("app_task");
        handlerThread.start();
        workHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == UPDATE_APP) {
                    boolean cacheF = msg.arg1 == 0;
                    if (applicationInfoUtil != null) {
                        applicationInfoUtil.sync(cacheF);
                    }
                    mainHandler.sendEmptyMessage(UPDATE_APP);
                }
            }
        };
        mainHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == UPDATE_APP) {
                    isUpdate = false;
                    if (loadListener != null && applicationInfoUtil != null) {
                        loadListener.done(applicationInfoUtil.getAppInfo(ApplicationInfoUtil.ALL));
                    }
                }
            }
        };


//        workTask = WorkTask.getInstance();
//        workTask.setListitenr(new WorkTask.WorkCallback() {
//            @Override
//            public Object doInbackground(int workID, Object object) {
//                if (workID == UPDATE_APP) {
//                    boolean cacheF = (boolean) object;
//                    if (applicationInfoUtil != null) {
//                        applicationInfoUtil.sync(cacheF);
//                    }
//                }
//                return true;
//            }
//
//            @Override
//            public void postResult(int workID, Object result) {
//                isUpdate = false;
//                if (loadListener != null && applicationInfoUtil != null) {
//                    loadListener.done(applicationInfoUtil.getAppInfo(ApplicationInfoUtil.ALL));
//                }
//            }
//        });
    }

    private static class SingletonHandle {
        private static AppInfoTask task = new AppInfoTask();
    }

    public static AppInfoTask getInstance() {
        return SingletonHandle.task;
    }

    public void setListener(AppLoadListener loadListener) {
        this.loadListener = loadListener;
    }

    private boolean isUpdate = false;

    public boolean clearCache() {
        return applicationInfoUtil.clearCache();
    }

    public void execute(Context context, boolean cacheFirst) {
        if (isUpdate) {
            if (loadListener != null) {
                loadListener.updateFail();
            }
            return;
        }
        isUpdate = true;
        if (loadListener != null) {
            loadListener.preDone();
        }
        if (applicationInfoUtil == null) {
            applicationInfoUtil = ApplicationInfoUtil.getIntance(context);
        }

//        workTask.startWork(UPDATE_APP,cacheFirst);

        Message message = workHandler.obtainMessage(UPDATE_APP);
        message.arg1 = (cacheFirst ? 0 : 1);
        workHandler.sendMessage(message);
    }

    class AppReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_PACKAGE_ADDED:
                case Intent.ACTION_PACKAGE_REMOVED:
                    if (loadListener != null) {
                        loadListener.appChange();
                    }
                    break;
                default:
            }
        }
    }

    private Context registerObj = null;

    public void onCreate(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        registerObj = context;
        registerObj.registerReceiver(appReceiver, intentFilter);
    }

    public void onDestory() {
        if (registerObj != null) {
            registerObj.unregisterReceiver(appReceiver);
        }
        registerObj = null;
    }


    private AppReceiver appReceiver = new AppReceiver();

    public void search(List<AppInfo> allApplist, final String k) {
        applicationInfoUtil.search(allApplist, k);
    }

    public void getAppInfo(int type) {
        if (loadListener != null) {
            loadListener.done(applicationInfoUtil.getAppInfo(type));
        }
    }
}
