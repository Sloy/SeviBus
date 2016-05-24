package com.sloy.sevibus.resources.actions.user;

import com.google.firebase.auth.FirebaseAuth;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;

import rx.Observable;

public class LogOutAction {

    private final UserDataSource userDataSource;
    private final FirebaseAuth firebaseAuth;

    public LogOutAction(UserDataSource userDataSource, FirebaseAuth firebaseAuth) {
        this.userDataSource = userDataSource;
        this.firebaseAuth = firebaseAuth;
    }

    public Observable<Void> logOut() {
        return Observable.concat(userDataSource.removeCurrentUser(), logoutFromFirebase());
    }

    private Observable<Void> logoutFromFirebase() {
        return Observable.defer(() -> {
            firebaseAuth.signOut();
            return Observable.empty();
        });
    }
}
