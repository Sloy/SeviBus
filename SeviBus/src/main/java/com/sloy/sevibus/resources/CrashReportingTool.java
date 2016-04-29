package com.sloy.sevibus.resources;

import com.sloy.sevibus.ui.SevibusUser;

public interface CrashReportingTool {

    void regiterHandledException(Throwable t);

    void associateUser(SevibusUser user);
}
