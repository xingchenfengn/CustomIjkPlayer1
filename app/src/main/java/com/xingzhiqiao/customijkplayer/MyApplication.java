package com.xingzhiqiao.customijkplayer;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by xingzhiqiao on 2017/1/9.
 */

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
