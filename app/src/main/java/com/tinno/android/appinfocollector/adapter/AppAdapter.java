package com.tinno.android.appinfocollector.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tinno.android.appinfocollector.R;
import com.tinno.android.appinfocollector.tools.AppInfo;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppHolder> {
    private List<AppInfo> mAppInfos;
    private Context mContext;
    private OnItemClickListener onItemClickListener = null;
    private OnItemLongClickListener onItemLongClickListener = null;


    public interface OnItemClickListener {
        public void onItemClick(View view, int pos);
    }

    public interface OnItemLongClickListener {
        public boolean onItemLongClick(View view, int pos);
    }

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

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }


    @Override
    public void onBindViewHolder(AppHolder holder, int position) {
        AppInfo info = mAppInfos.get(position);
        holder.bindCrime(info);
        holder.bindListener(onItemClickListener, onItemLongClickListener);
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mAppInfos.size();
    }


}

class AppHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    private ImageView mAppIcon;
    private TextView mAppName;
    private TextView mPkgName;
    private TextView mAppDir;
    private TextView mAppVer;
    private AppAdapter.OnItemClickListener onItemClickListener = null;
    private AppAdapter.OnItemLongClickListener onItemLongClickListener = null;
    private AppInfo mAppInfo;

    public AppHolder(View itemView) {
        super(itemView);


        mAppIcon = itemView.findViewById(R.id.list_item_app_icon);
        mAppName = itemView.findViewById(R.id.list_item_app_name);
        mPkgName = itemView.findViewById(R.id.list_item_package_name);
        mAppDir = itemView.findViewById(R.id.list_item_package_dir);
        mAppVer = itemView.findViewById(R.id.list_item_package_version);


        itemView.setOnLongClickListener(this);
        itemView.setOnClickListener(this);
    }

    public void bindCrime(AppInfo appinfo) {
        mAppInfo = appinfo;
        mAppIcon.setImageDrawable(mAppInfo.getAppIcon());
        mAppName.setText(mAppInfo.getAppName());
        mPkgName.setText(mAppInfo.getPackageName());
        mAppDir.setText(mAppInfo.getAppDir());
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(mAppInfo.getVersionName());
        spannableStringBuilder.append("[" + mAppInfo.getVersionCode() + "]");
        mAppVer.setText(spannableStringBuilder);
    }


    public void bindListener(AppAdapter.OnItemClickListener onItemClickListener
            , AppAdapter.OnItemLongClickListener onItemLongClickListener) {
        this.onItemClickListener = onItemClickListener;
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public void onClick(View v) {
        Log.i("wangcan","onItemClickListener not null?"+(onItemClickListener!=null));
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }


    @Override
    public boolean onLongClick(View v) {
        Log.i("wangcan","onItemLongClickListener not null?"+(onItemLongClickListener!=null));
        boolean result = false;
        if (onItemLongClickListener != null) {
            result = onItemLongClickListener.onItemLongClick(v, getAdapterPosition());
        }
        return result;
    }
}