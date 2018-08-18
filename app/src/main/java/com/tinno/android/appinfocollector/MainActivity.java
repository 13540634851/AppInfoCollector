package com.tinno.android.appinfocollector;


import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.open.dialog.base.LoadingDialog;
import com.tinno.android.appinfocollector.adapter.AppAdapter;
import com.tinno.android.appinfocollector.adapter.AppRecyclerView;
import com.tinno.android.appinfocollector.tools.AppInfo;
import com.tinno.android.appinfocollector.tools.ApplicationInfoUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener
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

    private CheckBox showSys, showUninstall;
    AsyncTask<String, Integer, Boolean> appTask = new AsyncTask<String, Integer, Boolean>() {

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
            showUninstall = popupWindowView.findViewById(R.id.show_uninstall);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {

                }
            });

            showSys.setChecked(true);
            showUninstall.setChecked(true);
            showSys.setOnCheckedChangeListener(this);
            showUninstall.setOnCheckedChangeListener(this);
        }
        popupWindow.showAtLocation(popupWindowView, Gravity.TOP | Gravity.END, 0, toolbar.getMeasuredHeight());
        popupWindow.showAsDropDown(popupWindowView);


    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (showSys == buttonView || buttonView == showUninstall) {
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
        mCrimeRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                boolean isCurrentTop = !recyclerView.canScrollVertically(-1);
                if (isCurrentTop != isScrollToTop) {
                    isScrollToTop = isCurrentTop;
                    if (isScrollToTop) {
                        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
                    } else {
                        toolbar.setNavigationIcon(android.R.drawable.ic_menu_upload);
                    }
                }
            }
        });

        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (mAdapter == null) {
            mAdapter = new AppAdapter(appInfos, this);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }

        appTools.registerReceiver(this, this);
    }

    private void setUpView() {

        toolbar = findViewById(R.id.toolbar);

        toolbar.inflateMenu(R.menu.toobar_item);
        searchView = toolbar.findViewById(R.id.action_search_kl);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        searchView.setOnQueryTextListener(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isScrollToTop) {
                    mCrimeRecyclerView.scrollToPosition(0);
                } else {
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                }
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (popupWindow == null || !popupWindow.isShowing()) {
                    showAsPopWindow();
                } else if (popupWindow.isShowing()) {
                    hidePopWindow();
                }

                return true;
            }
        });
        Log.i("wangcan", "appTask.execute()");

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
}

