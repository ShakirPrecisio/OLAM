package com.kemar.olam.utility;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import io.realm.Realm;


public class MyApplication extends MultiDexApplication {
    private static Context context;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        SharedPref.init(context);
        SharedPrefLang.init(context);
        MultiDex.install(this);
        Realm.init(this);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
