package com.sloy.sevibus.resources.actions.user;

import com.sloy.sevibus.resources.datasource.user.UserDataSource;

import rx.Observable;

public class LogOutAction {

    private final UserDataSource userDataSource;

    public LogOutAction(UserDataSource userDataSource) {
        this.userDataSource = userDataSource;
    }

    public Observable<Void> logOut() {
        return userDataSource.removeCurrentUser();
    }
}
