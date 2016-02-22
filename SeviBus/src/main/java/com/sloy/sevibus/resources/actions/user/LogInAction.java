package com.sloy.sevibus.resources.actions.user;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;
import com.sloy.sevibus.ui.SevibusUser;

import rx.Observable;

public class LogInAction {

    private final UserDataSource userDataSource;

    public LogInAction(UserDataSource userDataSource) {
        this.userDataSource = userDataSource;
    }

    @RxLogObservable
    public Observable<SevibusUser> logIn(SevibusUser user) {
        return Observable.just(user)
          .flatMap(userDataSource::setCurrentUser);
    }
}
