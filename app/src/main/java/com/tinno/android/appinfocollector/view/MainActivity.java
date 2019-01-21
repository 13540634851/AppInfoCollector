package com.tinno.android.appinfocollector.view;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.open.dialog.base.LoadingDialog;
import com.tinno.android.appinfocollector.R;
import com.tinno.android.appinfocollector.model.AppInfo;
import com.tinno.android.appinfocollector.model.ApplicationInfoUtil;
import com.tinno.android.appinfocollector.presenter.AppAdapter;
import com.tinno.android.appinfocollector.presenter.AppInfoTask;
import com.tinno.android.appinfocollector.presenter.AppRecyclerView;
import com.tinno.android.appinfocollector.tools.SystemPropertiesUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener
        , CompoundButton.OnCheckedChangeListener, AppInfoTask.AppLoadListener {
    private long bootTime = 0;
    private AppRecyclerView mCrimeRecyclerView;
    private AppAdapter mAdapter;
    private List<AppInfo> appInfos;
    private Toolbar toolbar;
    private PopupWindow popupWindow;
    private View popupWindowView;
    private SearchView searchView;
    private Toast mToast;
    private AlertDialog appLauchInfoDialog;
    private CheckBox showUninstall, showMyos, showLunch;
    private AppInfoTask appInfoTask;
    private LoadingDialog loadingDialog;
    private static final String TAG = "MainActivity";
    private boolean immediateLoad = false;

    public void showAsPopWindow() {
        if (popupWindow == null || popupWindowView == null) {
            popupWindowView = LayoutInflater.from(this).inflate(R.layout.popwindow, null);
            popupWindow = new PopupWindow(popupWindowView,
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setTouchable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable());
            popupWindow.setAnimationStyle(R.style.PopupAnimation);
            showMyos = popupWindowView.findViewById(R.id.show_myos);
            showUninstall = popupWindowView.findViewById(R.id.show_uninstall);
            showLunch = popupWindowView.findViewById(R.id.show_launch);
            showMyos.setChecked(false);
            showUninstall.setChecked(false);
            showLunch.setChecked(false);
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
        //show when power up;
        final Window win = getWindow();
        final WindowManager.LayoutParams params = win.getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        Intent intent = getIntent();
        String cmd = intent.getStringExtra("cmd");
        Log.i(TAG, "cmd = " + cmd);
        if (cmd != null) {
            if ("immediateload".equals(cmd)) {
                immediateLoad = true;
            } else if ("compare".equals(cmd)) {
                //nothing to do
            }
        }

        setContentView(R.layout.activity_main);
        if (bootTime == 0) {
            bootTime = System.currentTimeMillis();
        }
        appInfoTask = AppInfoTask.getInstance();
        appInfoTask.setListener(this);
        appInfoTask.execute(this, true);
        appInfos = new ArrayList<>();
        setUpView();
    }

    private void initRecycleView() {
        appInfoTask.onCreate(this);
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
            appLauchInfoDialog.setButton("返回", (DialogInterface dialog, int which) -> {
                appLauchInfoDialog.dismiss();
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
                } else if (item.getItemId() == R.id.menu_export) {
                    for (AppInfo appInfo : appInfos) {
                        Log.i("appabout", "\n" + appInfo.toString());


                    }
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        new ExportAsyncTask().execute(appInfos);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x717);
                    }


                } else if (item.getItemId() == R.id.menu_update) {
                    appInfoTask.execute(MainActivity.this, false);
                    if (showUninstall != null) {
                        showUninstall.setChecked(false);
                    }
                    if (showMyos != null) {
                        showMyos.setChecked(false);
                    }
                    if (showLunch != null) {
                        showLunch.setChecked(false);
                    }
                }
                return true;
            }
        });
        initRecycleView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0x717) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ExportAsyncTask().execute(appInfos);
            } else {
                showToast("Error:no premission");
            }
        }
    }

    private class ExportAsyncTask extends AsyncTask<List<AppInfo>, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (loadingDialog == null) {
                loadingDialog = new LoadingDialog(MainActivity.this);
            }
            loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(List<AppInfo>... infos) {
            if (infos.length != 1) {
                return false;
            }
            Collections.sort(infos[0], new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    return o1.getAppDir().toString().compareTo(o2.getAppDir().toString());
                }
            });


            File file = new File("/sdcard/DCIM/appinfos.txt");
            BufferedWriter bfw = null;
            FileWriter fw = null;
            try {
                fw = new FileWriter(file, false);
                bfw = new BufferedWriter(fw);

                bfw.write((infos[0].size()) + " app have been install\n");
                bfw.write("Version:" + SystemPropertiesUtils.get("ro.internal.build.version", "unknown"));

                bfw.write("\n");
                for (AppInfo appInfo : infos[0]) {
                    bfw.write(appInfo.toString());
                    bfw.write("\n");
                }
                //bfw.write();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (bfw != null) {
                        bfw.flush();
                        bfw.close();
                    }
                    if (fw != null) {
                        fw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean siSucceed) {
            super.onPostExecute(siSucceed);
            String title = "命令:导出";
            if (siSucceed) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(title)
                        .setMessage(SystemPropertiesUtils.get("ro.internal.build.version", "") + "\n成功导出:/sdcard/DCIM/appinfos.txt")
                        .create().show();

            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(title)
                        .setMessage(SystemPropertiesUtils.get("ro.internal.build.version", "") + "\n导出失败")
                        .create().show();
            }
            if (loadingDialog != null) {
                loadingDialog.hide();
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (appInfos != null && appInfos.size() > 0) {
            appInfoTask.search(appInfos, s);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appInfoTask.onDestory();
    }


    private void updateAppInfo() {
        if (appInfos == null
                || mAdapter == null
                || showLunch == null
                || showUninstall == null) {
            return;
        }

        int type = 0;
        if (showUninstall.isChecked()) {
            type = type | ApplicationInfoUtil.UNINSTALL;
        }
        if (showLunch.isChecked()) {
            type = type | ApplicationInfoUtil.LAUNCH_APP;
        }
        if (showMyos.isChecked()) {
            type = type | ApplicationInfoUtil.MYOS_APP;
        }
        appInfoTask.getAppInfo(type);

    }


    @Override
    public void preDone() {
        Log.i(TAG, "update start");
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(MainActivity.this);
            loadingDialog.setCancelable(false);
        }
        loadingDialog.show();
    }

    @Override
    public void done(List<AppInfo> appInfos) {
        Log.i(TAG, "update done");
        this.appInfos.clear();
        this.appInfos.addAll(appInfos);
        mAdapter.notifyDataSetChanged();
        if (loadingDialog != null) {
            loadingDialog.hide();
        }
        if (immediateLoad) {
            immediateLoad = false;
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                new ExportAsyncTask().execute(appInfos);
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x717);
            }
        }
    }

    @Override
    public void appChange() {
        Log.i(TAG, "app change");
        showToast("有应用已经被安装或卸载，注意更新一下");
    }


    @Override
    public void updateFail() {
        showToast("正在更新，稍后再试");
    }
}



