package com.sloy.sevibus.resources.datasource.user;

import android.content.SharedPreferences;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.google.gson.Gson;
import com.sloy.sevibus.ui.SevibusUser;

import rx.Observable;

public class PreferencesUserDataSource implements UserDataSource {

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public PreferencesUserDataSource(SharedPreferences sharedPreferences, Gson gson) {
        this.sharedPreferences = sharedPreferences;
        this.gson = gson;
    }

    @Override
    public Observable<SevibusUser> getCurrentUser() {
        return Observable.defer(() -> Observable
          .just(sharedPreferences.getString("user", ""))
          .filter(serializedUser -> !serializedUser.isEmpty())
          .map(s -> gson.fromJson(s, SevibusUser.class)));
    }

    @Override
    public Observable<SevibusUser> setCurrentUser(SevibusUser user) {
        return Observable.just(user)
          .map(gson::toJson)
          .flatMap(serializedUser -> {
              sharedPreferences.edit().putString("user", serializedUser).apply();
              return Observable.just(user);
          });
    }

    @Override
    public Observable<Void> removeCurrentUser() {
        return Observable.defer(() -> {
            sharedPreferences.edit().remove("user").apply();
            return Observable.empty();
        });
    }
}
