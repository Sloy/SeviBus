package com.sloy.sevibus.resources.actions.favorita;

import com.sloy.sevibus.resources.datasource.favorita.FavoritaDataSource;

import rx.Observable;

public class DeleteFavoritaAction {

    private final FavoritaDataSource favoritaDataSource;

    public DeleteFavoritaAction(FavoritaDataSource favoritaDataSource) {
        this.favoritaDataSource = favoritaDataSource;
    }

    public Observable<Void> deleteFavorita(Integer numeroParada) {
        return favoritaDataSource.deleteFavorita(numeroParada)
          .flatMap(__ -> Observable.empty());
    }

}
