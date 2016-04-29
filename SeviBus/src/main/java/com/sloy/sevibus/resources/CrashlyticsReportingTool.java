package com.sloy.sevibus.resources;

import com.crashlytics.android.Crashlytics;
import com.sloy.sevibus.ui.SevibusUser;

import static com.chernobyl.Chernobyl.checkNotNull;

public class CrashlyticsReportingTool implements CrashReportingTool {

    @Override
    public void regiterHandledException(Throwable t) {
        Crashlytics.logException(t);
    }

    @Override
    public void associateUser(SevibusUser user) {
        checkNotNull(user);
        Crashlytics.setUserName(user.getName());
        Crashlytics.setUserEmail(user.getEmail());
        Crashlytics.setUserIdentifier(user.getId());
    }
}
