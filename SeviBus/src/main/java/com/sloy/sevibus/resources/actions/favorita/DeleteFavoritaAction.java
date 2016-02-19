package com.sloy.sevibus.resources.actions.favorita;

import com.sloy.sevibus.resources.datasource.favorita.FavoritaDataSource;

import rx.Observable;

public class DeleteFavoritaAction {

    private final FavoritaDataSource favoritaLocalDataSource;
    private final FavoritaDataSource favoritaRemoteDataSource;

    public DeleteFavoritaAction(FavoritaDataSource favoritaLocalDataSource, FavoritaDataSource favoritaRemoteDataSource) {
        this.favoritaLocalDataSource = favoritaLocalDataSource;
        this.favoritaRemoteDataSource = favoritaRemoteDataSource;
    }

    public Observable<Void> deleteFavorita(Integer numeroParada) {
        return Observable.just(numeroParada)
          .flatMap(favoritaLocalDataSource::deleteFavorita)
          .flatMap(favoritaRemoteDataSource::deleteFavorita)
          .flatMap(__ -> Observable.empty());
    }

}
