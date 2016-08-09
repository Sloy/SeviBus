package com.sloy.sevibus;

import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.facebook.stetho.Stetho;
import com.sloy.sevibus.resources.awareness.AlarmManagerWrapper;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.resources.awareness.BonobusFenceSetupScheduler;

import net.danlew.android.joda.JodaTimeAndroid;

import io.fabric.sdk.android.Fabric;

public class SevibusApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        Fabric.with(this, new Crashlytics(), new Answers());

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }

        StuffProvider.getRemoteConfiguration().init();
        new BonobusFenceSetupScheduler(new AlarmManagerWrapper(this)).schedule();
    }
}
