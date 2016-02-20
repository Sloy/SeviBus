package com.sloy.sevibus.resources.actions.user;

import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.sloy.sevibus.resources.Session;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;
import com.sloy.sevibus.ui.SevibusUser;

import rx.Observable;

public class ObtainUserAction {

    private final UserDataSource userDataSource;
    private final Session session;

    public ObtainUserAction(UserDataSource userDataSource, Session session) {
        this.userDataSource = userDataSource;
        this.session = session;
    }

    public Observable<Optional<SevibusUser>> obtainUser() {
        return Observable.concat(
          fromSession(),
          fromDataSource()
            .flatMap(this::saveInSession)
        )
          .take(1)
          .map(Optional::of)
          .switchIfEmpty(Observable.just(Optional.absent()));
    }


    @NonNull
    private Observable<SevibusUser> saveInSession(SevibusUser user) {
        return Observable.defer(() -> {
            System.out.println("saveInSession");
            session.setUser(user);
            return Observable.just(user);
        });
    }

    @NonNull
    private Observable<SevibusUser> fromDataSource() {
        return Observable.defer(() -> {
            System.out.println("fromDataSource");
            return userDataSource.getCurrentUser();
        });
    }

    @NonNull
    private Observable<SevibusUser> fromSession() {
        System.out.println("fromSession");
        return Observable.defer(() -> {
            if (session.getUser().isPresent()) {
                return Observable.just(session.getUser().get());
            } else {
                return Observable.empty();
            }
        });
    }

}
