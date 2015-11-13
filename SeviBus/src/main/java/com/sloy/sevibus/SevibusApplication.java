package com.sloy.sevibus;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class SevibusApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }
}
