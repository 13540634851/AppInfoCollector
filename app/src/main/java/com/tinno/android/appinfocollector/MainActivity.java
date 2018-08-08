package com.tinno.android.appinfocollector;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.tinno.android.appinfocollector.adapter.AppAdapter;
import com.tinno.android.appinfocollector.adapter.AppRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = "boot";
    private long bootTime = 0;
    private AppRecyclerView mCrimeRecyclerView;
    private AppAdapter mAdapter;
    private List<AppInfo> appInfos = new ArrayList<AppInfo>();
    private boolean isScrollToTop = true;
    private ProgressDialog progressDialog;
    private Toolbar toolbar;
    AsyncTask<String, Integer, Boolean> appTask = new AsyncTask<String, Integer, Boolean>() {
        private List<AppInfo> appInfos;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            appInfos = new ArrayList<AppInfo>();
            showProgressDialog();
        }

        @Override
        protected Boolean doInBackground(String... Strings) {
            if (isCancelled()) {
                return false;
            }
            ApplicationInfoUtil.getAllProgramInfo(appInfos, MainActivity.this);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (isCancelled()) {
                return;
            }

            MainActivity.this.appInfos.clear();
            MainActivity.this.appInfos.addAll(appInfos);
            initRecycleView();
            hideProgressDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (bootTime == 0) {
            bootTime = System.currentTimeMillis();
        }
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
        SearchView searchView = toolbar.findViewById(R.id.action_search_kl);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        ApplicationInfoUtil.search(appInfos, null);
        searchView.setOnQueryTextListener(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isScrollToTop) {
                    mCrimeRecyclerView.smoothScrollToPosition(0);
                }
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
            ApplicationInfoUtil.search(appInfos, s);
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

