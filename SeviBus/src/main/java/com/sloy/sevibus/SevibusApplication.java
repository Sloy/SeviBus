package com.sloy.sevibus;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.crashlytics.android.Crashlytics;
import com.sloy.sevibus.resources.Debug;

import io.fabric.sdk.android.Fabric;

public class SevibusApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Debug.activateReports(this);
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }
}
