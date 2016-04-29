package com.sloy.sevibus.resources;

import android.util.Log;

import com.sloy.sevibus.ui.SevibusUser;

public class EmptyCrashReportingTool implements CrashReportingTool {

    @Override
    public void regiterHandledException(Throwable t) {
        Log.e("EmptyCrashReporting", "Register handled exception", t);
    }

    @Override
    public void associateUser(SevibusUser user) {
        Log.i("EmptyCrashReporting", "Associating user: " + user);
    }
}
