package com.tinno.android.appinfocollector;

import android.app.Dialog;
import android.app.ProgressDialog;
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

import com.tinno.android.appinfocollector.adapter.AppAdapter;
import com.tinno.android.appinfocollector.adapter.AppRecyclerView;
import com.tinno.android.appinfocollector.tools.AppInfo;
import com.tinno.android.appinfocollector.tools.ApplicationInfoUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "boot";
    private long bootTime = 0;
    private AppRecyclerView mCrimeRecyclerView;
    private AppAdapter mAdapter;
    private List<AppInfo> appInfos;
    private boolean isScrollToTop = true;
    private ProgressDialog progressDialog;
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
            appInfos.clear();

            if (showSys.isChecked()) {
                appInfos.addAll(appTools.getAppInfo(ApplicationInfoUtil.SYSTEM_APP));
            }
            if (showUninstall.isChecked()) {
                appInfos.addAll(appTools.getAppInfo(ApplicationInfoUtil.NONSYSTEM_APP));
            }

            mAdapter.notifyDataSetChanged();
        }

    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void hidePopWindow() {
        if (popupWindow == null) {
            return;
        }
        popupWindow.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (bootTime == 0) {
            bootTime = System.currentTimeMillis();
        }
        appInfos = new ArrayList<>();
        appTools = ApplicationInfoUtil.getIntance(this);
        testSpeedTime("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                    mCrimeRecyclerView.smoothScrollToPosition(0);
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

        appTask.execute();
    }

    private void testSpeedTime(String msg) {
        Log.i(TAG, msg + " " + (System.currentTimeMillis() - bootTime));
        bootTime = System.currentTimeMillis();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
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
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.hide();
        }
    }
}

