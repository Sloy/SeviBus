package com.sloy.sevibus.resources.datasource.user;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.sloy.sevibus.ui.SevibusUser;

import rx.Observable;

public interface UserDataSource {
    Observable<SevibusUser> getCurrentUser();

    @RxLogObservable
    Observable<SevibusUser> setCurrentUser(SevibusUser user);

    Observable<Void> removeCurrentUser();
}
