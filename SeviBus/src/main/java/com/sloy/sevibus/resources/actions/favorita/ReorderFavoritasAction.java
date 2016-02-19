package com.sloy.sevibus.resources.actions.favorita;

import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.resources.datasource.favorita.FavoritaDataSource;

import java.util.List;

import rx.Observable;

public class ReorderFavoritasAction {

    private final FavoritaDataSource favoritaLocalDataSource;
    private final FavoritaDataSource favoritaRemoteDataSource;

    public ReorderFavoritasAction(FavoritaDataSource favoritaLocalDataSource, FavoritaDataSource favoritaRemoteDataSource) {
        this.favoritaLocalDataSource = favoritaLocalDataSource;
        this.favoritaRemoteDataSource = favoritaRemoteDataSource;
    }

    public Observable<Void> setNewOrder(List<Favorita> ordered) {
        return Observable.range(0, ordered.size())
          .zipWith(ordered, (i, fav) -> updateOrder(fav, i))
          .toList()
          .flatMap(favoritaLocalDataSource::saveFavoritas)
          .flatMap(favoritaRemoteDataSource::saveFavoritas)
          .flatMap(__ -> Observable.empty());
    }

    private Favorita updateOrder(Favorita fav, int order) {
        fav.setOrden(order);
        return fav;
    }

}
