package com.tinno.android.appinfocollector;


import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.open.dialog.base.LoadingDialog;
import com.tinno.android.appinfocollector.adapter.AppAdapter;
import com.tinno.android.appinfocollector.adapter.AppRecyclerView;
import com.tinno.android.appinfocollector.tools.AppInfo;
import com.tinno.android.appinfocollector.tools.ApplicationInfoUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends BaseActivity implements SearchView.OnQueryTextListener
        , CompoundButton.OnCheckedChangeListener
        , ApplicationInfoUtil.AppChangeCallback {
    private static final String TAG = "boot";
    private long bootTime = 0;
    private AppRecyclerView mCrimeRecyclerView;
    private AppAdapter mAdapter;
    private List<AppInfo> appInfos;
    private boolean isScrollToTop = true;
    private LoadingDialog loadingDialog;
    private Toolbar toolbar;
    private PopupWindow popupWindow;
    private View popupWindowView;
    private ApplicationInfoUtil appTools;
    private SearchView searchView;
    private Toast mToast;
    private CheckBox showSys, showUninstall, showMyos;
    private AsyncTask<String, Integer, Boolean> appTask = new AsyncTask<String, Integer, Boolean>() {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            appInfos.clear();
            Log.i("wangcan", "onPreExecute");
            showProgressDialog();
        }

        @Override
        protected Boolean doInBackground(String... Strings) {
            if (isCancelled()) {
                return false;
            }
            appTools.sync();
            Log.i("wangcan", "doInBackground");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (isCancelled()) {
                return;
            }
            Log.i("wangcan", "onPostExecute");

            MainActivity.this.appInfos.clear();
            MainActivity.this.appInfos.addAll(appTools.getAppInfo(ApplicationInfoUtil.DEFAULT));
            initRecycleView();
            hideProgressDialog();
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
        int currentColor = getCurrentColor();
        int color = getSetting(THEME, currentColor);
        if (currentColor != color) {
            adjustAppColor(color, true);
        }
        Log.i("wangcan", "onRestart");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("wangcan", "onNewIntent");
    }

    public void showAsPopWindow() {
        if (popupWindow == null || popupWindowView == null) {
            popupWindowView = LayoutInflater.from(this).inflate(R.layout.popwindow, null);
            popupWindow = new PopupWindow(popupWindowView,
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setTouchable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable());
            popupWindow.setAnimationStyle(R.style.PopupAnimation);
            showSys = popupWindowView.findViewById(R.id.show_sys_app);
            showMyos = popupWindowView.findViewById(R.id.show_myos);
            showUninstall = popupWindowView.findViewById(R.id.show_uninstall);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {

                }
            });

            showSys.setChecked(true);
            showMyos.setChecked(false);
            showUninstall.setChecked(true);
            showSys.setOnCheckedChangeListener(this);
            showMyos.setOnCheckedChangeListener(this);
            showUninstall.setOnCheckedChangeListener(this);
        }
        popupWindow.showAtLocation(popupWindowView, Gravity.TOP | Gravity.END, 0, toolbar.getMeasuredHeight());
        popupWindow.showAsDropDown(popupWindowView);


    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (showSys == buttonView || buttonView == showUninstall || buttonView == showMyos) {
            updateAppInfo();
        }

    }

    private void hidePopWindow() {
        if (popupWindow == null) {
            return;
        }
        popupWindow.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (bootTime == 0) {
            bootTime = System.currentTimeMillis();
        }
        appInfos = new ArrayList<>();
        appTools = ApplicationInfoUtil.getIntance(this);
        Log.i("wangcan", "onCreate");
        testSpeedTime("onCreate");


        setUpView();
    }

    private void initRecycleView() {
        mCrimeRecyclerView = findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setEmptyView(findViewById(R.id.empty_view));
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (mAdapter == null) {
            mAdapter = new AppAdapter(appInfos, this);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }

        mAdapter.setOnItemClickListener((View view, int pos) -> {
            AppInfo appInfo = appInfos.get(pos);
            showToast("长按进入" + appInfo.getAppName() + "应用信息界面");
        });

        mAdapter.setOnItemLongClickListener((View view, int pos) -> {
            AppInfo appInfo = appInfos.get(pos);
            Intent intent = new Intent();
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.parse("package:" + appInfo.getPackageName()));
            startActivity(intent);
            return true;
        });
        appTools.registerReceiver(this, this);
    }

    private void showToast(String msg){
        Log.i("wangcan",msg);
        if(mToast==null){
            mToast= Toast.makeText(this, msg, Toast.LENGTH_LONG);
        }else{
            mToast.setText(msg);
        }
        mToast.show();
    }


    private void setUpView() {

        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.toobar_item);
        searchView = toolbar.findViewById(R.id.action_search_kl);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCrimeRecyclerView.scrollToPosition(0);
            }
        });
        searchView.setOnQueryTextListener(this);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_filter) {
                    if (popupWindow == null || !popupWindow.isShowing()) {
                        showAsPopWindow();
                    } else if (popupWindow.isShowing()) {
                        hidePopWindow();
                    }
                } else if (item.getItemId() == R.id.menu_theme) {
                    startActivity(new Intent(MainActivity.this, ThemeActivity.class));
                } else if (item.getItemId() == R.id.menu_about) {
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                }


                return true;
            }
        });
        Log.i("wangcan", "appTask.execute()");
        adjustAppColor(getSetting(THEME, getColor(R.color.colorBackground)), false);
        setSetting(THEME, getCurrentColor());
        appTask.execute();
    }


    private void testSpeedTime(String msg) {
        Log.i(TAG, msg + " " + (System.currentTimeMillis() - bootTime));
        bootTime = System.currentTimeMillis();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("wangcan", "onstart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        appTools.unregisterReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("wangcan", "onResume");
        testSpeedTime("onResume");
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        Log.i("wangcan", "onQueryTextSubmit=" + s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        Log.i("wangcan", "onQueryTextChange=" + s);
        if (appInfos != null && appInfos.size() > 0) {
            appTools.search(appInfos, s);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();

            }
        }
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        appTask.cancel(true);
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    public void showProgressDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.setCanceledOnTouchOutside(false);
        }
        loadingDialog.show();
    }

    public void hideProgressDialog() {
        if (loadingDialog != null) {
            loadingDialog.hide();
        }
    }

    private void updateAppInfo() {
        if (appInfos == null
                || mAdapter == null
                || appTools == null
                || showUninstall == null
                || showSys == null) {
            return;
        }
        appInfos.clear();
        if (showSys.isChecked()) {
            appInfos.addAll(appTools.getAppInfo(ApplicationInfoUtil.SYSTEM_APP));
        }
        if (showUninstall.isChecked()) {
            appInfos.addAll(appTools.getAppInfo(ApplicationInfoUtil.NONSYSTEM_APP));
        }
        if (showMyos.isChecked()) {
            Iterator<AppInfo> allinfo = appInfos.iterator();
            AppInfo info;
            while (allinfo.hasNext()) {
                info = allinfo.next();
                if (!info.getAppDir().toString().toLowerCase().contains("/ape")) {
                    allinfo.remove();
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void installapp(AppInfo appInfo) {
        updateAppInfo();
    }

    @Override
    public void unstallapp(AppInfo appInfo) {
        updateAppInfo();
    }

    @Override
    protected void colorChange(int color) {
        super.colorChange(color);
        if (toolbar != null) {
            // toolbar.setBackgroundColor(color);
        }
    }
}

