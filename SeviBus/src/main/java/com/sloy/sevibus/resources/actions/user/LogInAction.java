package com.sloy.sevibus.resources.actions.user;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.database.FirebaseDatabase;
import com.sloy.sevibus.resources.CrashReportingTool;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;
import com.sloy.sevibus.resources.services.LoginService;
import com.sloy.sevibus.ui.SevibusUser;

import rx.Observable;

public class LogInAction {

    private final UserDataSource userDataSource;
    private final LoginService loginService;
    private final FirebaseDatabase firebaseDatabase;
    private final CrashReportingTool crashReportingTool;

    public LogInAction(UserDataSource userDataSource, LoginService loginService, FirebaseDatabase firebaseDatabase, CrashReportingTool crashReportingTool) {
        this.userDataSource = userDataSource;
        this.loginService = loginService;
        this.firebaseDatabase = firebaseDatabase;
        this.crashReportingTool = crashReportingTool;
    }

    public Observable<SevibusUser> logIn(AuthCredential credential) {
        return loginService.logUserIn(credential)
          .flatMap(userDataSource::setCurrentUser)
          .map(this::sendUserToFirebase)
          .map(this::associateWithCrashReporting);
    }

    private SevibusUser sendUserToFirebase(SevibusUser sevibusUser) {
        firebaseDatabase.getReference(sevibusUser.getId())
          .child("user")
          .setValue(sevibusUser);
        return sevibusUser;
    }

    private SevibusUser associateWithCrashReporting(SevibusUser sevibusUser) {
        crashReportingTool.associateUser(sevibusUser);
        return sevibusUser;
    }
}
