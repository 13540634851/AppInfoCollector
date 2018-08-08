package com.tinno.android.appinfocollector;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

public class WellComeActivity extends Activity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_wellcome);
    }
}
