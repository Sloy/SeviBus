package com.sloy.sevibus.resources.actions.user;

import com.sloy.sevibus.resources.CrashReportingTool;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;
import com.sloy.sevibus.ui.SevibusUser;
import com.sloydev.gallego.Optional;

import rx.Observable;

public class ObtainUserAction {

    private final UserDataSource userDataSource;
    private final CrashReportingTool crashReportingTool;

    public ObtainUserAction(UserDataSource userDataSource, CrashReportingTool crashReportingTool) {
        this.userDataSource = userDataSource;
        this.crashReportingTool = crashReportingTool;
    }

    public Observable<Optional<SevibusUser>> obtainUser() {
        return userDataSource.getCurrentUser()
          .map(this::associateWithCrashReporting)
          .map(Optional::of)
          .switchIfEmpty(Observable.just(Optional.absent()));
    }

    private SevibusUser associateWithCrashReporting(SevibusUser sevibusUser) {
        crashReportingTool.associateUser(sevibusUser);
        return sevibusUser;
    }
}
