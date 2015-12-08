package com.sloy.sevibus.resources;

import com.crashlytics.android.Crashlytics;

public class CrashlyticsReportingTool implements CrashReportingTool {

    @Override
    public void regiterHandledException(Throwable t) {
        Crashlytics.logException(t);
    }
}
