package com.tinno.android.appinfocollector.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tinno.android.appinfocollector.tools.AppInfo;
import com.tinno.android.appinfocollector.R;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppHolder> {
    private List<AppInfo> mAppInfos;
    private Context mContext;

    public AppAdapter(List<AppInfo> appInfos, Context context) {
        mAppInfos = appInfos;
        this.mContext = context;
    }

    @Override
    public AppHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.list_item_appinfo, parent, false);
        return new AppHolder(view);
    }


    @Override
    public void onBindViewHolder(AppHolder holder, int position) {
        AppInfo info = mAppInfos.get(position);
        holder.bindCrime(info);
    }

    @Override
    public int getItemCount() {
        return mAppInfos.size();
    }


}

class AppHolder extends RecyclerView.ViewHolder {
    private ImageView mAppIcon;
    private TextView mAppName;
    private TextView mPkgName;
    private TextView mAppDir;
    private TextView mAppVer;

    private AppInfo mAppInfo;

    public AppHolder(View itemView) {
        super(itemView);

        mAppIcon = itemView.findViewById(R.id.list_item_app_icon);
        mAppName = itemView.findViewById(R.id.list_item_app_name);
        mPkgName = itemView.findViewById(R.id.list_item_package_name);
        mAppDir = itemView.findViewById(R.id.list_item_package_dir);
        mAppVer = itemView.findViewById(R.id.list_item_package_version);


        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                return true;
            }
        });
    }

    public void bindCrime(AppInfo appinfo) {
        mAppInfo = appinfo;
        mAppIcon.setImageDrawable(mAppInfo.getAppIcon());
        mAppName.setText(mAppInfo.getAppName());
        mPkgName.setText(mAppInfo.getPackageName());
        mAppDir.setText(mAppInfo.getAppDir());
        mAppVer.setText(mAppInfo.getVersionName() + "[" + mAppInfo.getVersionCode() + "]");
    }

}