package com.sloy.sevibus.resources.actions.user;

import com.firebase.client.Firebase;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;

import rx.Observable;

public class LogOutAction {

    private final UserDataSource userDataSource;
    private final Firebase firebase;

    public LogOutAction(UserDataSource userDataSource, Firebase firebase) {
        this.userDataSource = userDataSource;
        this.firebase = firebase;
    }

    public Observable<Void> logOut() {
        return Observable.concat(userDataSource.removeCurrentUser(), logoutFromFirebase());
    }

    private Observable<Void> logoutFromFirebase() {
        return Observable.defer(() -> {
            firebase.unauth();
            return Observable.empty();
        });
    }
}
