package com.sloy.sevibus;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.sloy.sevibus.resources.Debug;

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
