package com.sloy.sevibus.resources;

import com.sloy.sevibus.ui.SevibusUser;

public interface CrashReportingTool {

    void associateUser(SevibusUser user);

    void registerHandledException(Throwable t);
}
