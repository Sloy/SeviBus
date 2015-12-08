package com.sloy.sevibus.resources;

import android.util.Log;

public class EmptyCrashReportingTool implements CrashReportingTool {

    @Override
    public void regiterHandledException(Throwable t) {
        Log.e("TAG", "Register handled exception", t);
    }
}
