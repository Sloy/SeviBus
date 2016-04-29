package com.sloy.sevibus.resources.actions.user;

import com.google.common.base.Optional;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;
import com.sloy.sevibus.ui.SevibusUser;

import rx.Observable;

public class ObtainUserAction {

    private final UserDataSource userDataSource;

    public ObtainUserAction(UserDataSource userDataSource) {
        this.userDataSource = userDataSource;
    }

    public Observable<Optional<SevibusUser>> obtainUser() {
        return userDataSource.getCurrentUser()
          .map(Optional::of)
          .switchIfEmpty(Observable.just(Optional.absent()));
    }
}
