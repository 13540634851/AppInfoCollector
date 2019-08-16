package com.tinno.android.appinfocollector.model;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class WorkTask {
    public interface WorkCallback {
        public Object doInbackground(int workID, Object object);

        public void postResult(int workID, Object result);
    }

    private HandlerThread handlerThread;
    private Handler workHandler, mainHandler;
    private WorkCallback workCallback;

    private WorkTask() {
        if (handlerThread == null) {
            initWorkHander();
        }
        mainHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (workCallback != null) {
                    workCallback.postResult(msg.what, msg.obj);
                }
            }
        };

    }

    private void initWorkHander() {
        handlerThread = new HandlerThread("app_task");
        handlerThread.start();
        workHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (workCallback != null) {
                    Object object = workCallback.doInbackground(msg.what, msg.obj);
                    mainHandler.obtainMessage(msg.what, object);
                }
            }
        };
    }


    private static class SingletonHandle {
        private static WorkTask task = new WorkTask();
    }

    public static WorkTask getInstance() {
        return WorkTask.SingletonHandle.task;
    }

    public void close() {
        handlerThread.quit();
        workHandler = null;
        handlerThread = null;
    }

    public void setListitenr(WorkCallback workCallback) {
        this.workCallback = workCallback;
    }


    public void startWork(int workID, Object object) {
        if (handlerThread == null) {
            initWorkHander();
        }
        workHandler.obtainMessage(workID, object).sendToTarget();
    }
}
