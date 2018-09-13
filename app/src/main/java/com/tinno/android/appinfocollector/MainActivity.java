package com.tinno.android.appinfocollector;


import android.app.AlertDialog;
import android.content.DialogInterface;
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
    private long bootTime = 0;
    private AppRecyclerView mCrimeRecyclerView;
    private AppAdapter mAdapter;
    private List<AppInfo> appInfos;
    private LoadingDialog loadingDialog;
    private Toolbar toolbar;
    private PopupWindow popupWindow;
    private View popupWindowView;
    private ApplicationInfoUtil appTools;
    private SearchView searchView;
    private Toast mToast;
    private AlertDialog appLauchInfoDialog;
    private CheckBox showSys, showUninstall, showMyos, showLunch;
    private AsyncTask<String, Integer, Boolean> appTask = new AsyncTask<String, Integer, Boolean>() {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            appInfos.clear();
            showProgressDialog();
        }

        @Override
        protected Boolean doInBackground(String... Strings) {
            if (isCancelled()) {
                return false;
            }
            appTools.sync();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (isCancelled()) {
                return;
            }
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
            showLunch = popupWindowView.findViewById(R.id.show_launch);
            showSys.setChecked(true);
            showMyos.setChecked(false);
            showUninstall.setChecked(true);
            showLunch.setChecked(false);
            showSys.setOnCheckedChangeListener(this);
            showMyos.setOnCheckedChangeListener(this);
            showUninstall.setOnCheckedChangeListener(this);
            showLunch.setOnCheckedChangeListener(this);
        }
        popupWindow.showAtLocation(popupWindowView, Gravity.TOP | Gravity.END, 0, toolbar.getMeasuredHeight());
        popupWindow.showAsDropDown(popupWindowView);


    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (showLunch == buttonView
                || showSys == buttonView
                || buttonView == showUninstall
                || buttonView == showMyos) {
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
            List<String> listLunch = appInfo.getLauncherlist();
            if (appLauchInfoDialog == null) {
                appLauchInfoDialog = new AlertDialog.Builder(MainActivity.this).create();
            }
            appLauchInfoDialog.setTitle("启动App");
            StringBuilder sb = new StringBuilder();
            if (listLunch.size() == 0) {
                sb.append("没有启动入口");
            } else {
                sb.append("启动\n");
                for (String s : listLunch) {
                    sb.append(s + "\n");
                }
            }
            appLauchInfoDialog.setMessage(sb.toString());
            appLauchInfoDialog.setButton("返回", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    appLauchInfoDialog.dismiss();
                }
            });
            appLauchInfoDialog.show();
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

    private void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        } else {
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
                } else if (item.getItemId() == R.id.menu_export) {
                    for (AppInfo appInfo : appInfos) {
                        Log.i("appabout", "\n"+appInfo.toString());
                        showToast("输入adb logcat -s appabout，打印");
                    }
                }


                return true;
            }
        });
        adjustAppColor(getSetting(THEME, getColor(R.color.colorBackground)), false);
        setSetting(THEME, getCurrentColor());
        appTask.execute();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
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
        appTools.unregisterReceiver();
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
                || showLunch == null
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

        if (showLunch.isChecked()) {
            Iterator<AppInfo> allinfo = appInfos.iterator();
            AppInfo info;
            while (allinfo.hasNext()) {
                info = allinfo.next();
                if (info.getLauncherlist().size() == 0) {
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

