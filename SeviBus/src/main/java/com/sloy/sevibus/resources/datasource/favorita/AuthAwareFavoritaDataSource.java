package com.sloy.sevibus.resources.datasource.favorita;

import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;

import java.util.List;

import rx.Observable;

public class AuthAwareFavoritaDataSource implements FavoritaDataSource {

    private final FavoritaDataSource authenticatedDataSource;
    private final UserDataSource userDataSource;

    public AuthAwareFavoritaDataSource(FavoritaDataSource authenticatedDataSource, UserDataSource userDataSource) {
        this.authenticatedDataSource = authenticatedDataSource;
        this.userDataSource = userDataSource;
    }

    @Override
    public Observable<List<Favorita>> getFavoritas() {
        return authenticatedDataSource.getFavoritas()
          .onErrorResumeNext(this::errorWhenLoggedIn);
    }

    @Override
    public Observable<Favorita> saveFavorita(Favorita favorita) {
        return authenticatedDataSource.saveFavorita(favorita)
          .onErrorResumeNext(this::errorWhenLoggedIn);
    }

    @Override
    public Observable<Favorita> getFavoritaById(Integer idParada) {
        return authenticatedDataSource.getFavoritaById(idParada)
          .onErrorResumeNext(this::errorWhenLoggedIn);
    }

    @Override
    public Observable<Integer> deleteFavorita(Integer idParada) {
        return authenticatedDataSource.deleteFavorita(idParada)
          .onErrorResumeNext(this::errorWhenLoggedIn);
    }

    @Override
    public Observable<List<Favorita>> saveFavoritas(List<Favorita> favoritas) {
        return authenticatedDataSource.saveFavoritas(favoritas)
          .onErrorResumeNext(this::errorWhenLoggedIn);
    }

    @Override
    public Observable<List<Favorita>> replaceFavoritas(List<Favorita> favoritas) {
        return authenticatedDataSource.replaceFavoritas(favoritas)
          .onErrorResumeNext(this::errorWhenLoggedIn);
    }

    <T> Observable<T> errorWhenLoggedIn(Throwable throwable) {
        if (throwable instanceof AuthException) {
            return userDataSource.getCurrentUser()
              .flatMap(user -> Observable.error(throwable));
        } else {
            return Observable.error(throwable);
        }
    }
}
