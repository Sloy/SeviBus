package com.sloy.sevibus.resources;

import android.util.Log;

import com.sloy.sevibus.ui.SevibusUser;

public class EmptyCrashReportingTool implements CrashReportingTool {

    @Override
    public void associateUser(SevibusUser user) {
        Log.i("EmptyCrashReporting", "Associating user: " + user);
    }

    @Override
    public void registerHandledException(Throwable t) {
        Log.e("EmptyCrashReporting", "Register handled exception", t);
    }
}
