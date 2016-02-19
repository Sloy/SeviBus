package com.sloy.sevibus.resources.actions;

import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.resources.datasource.FavoritaDataSource;

import java.util.List;

import rx.Observable;

public class ObtainFavoritasAction {

    private final FavoritaDataSource favoritaLocalDataSource;
    private final FavoritaDataSource favoritaRemoteDataSource;

    public ObtainFavoritasAction(FavoritaDataSource favoritaLocalDataSource, FavoritaDataSource favoritaRemoteDataSource) {
        this.favoritaLocalDataSource = favoritaLocalDataSource;
        this.favoritaRemoteDataSource = favoritaRemoteDataSource;
    }

    public Observable<List<Favorita>> getFavoritas(){
        return Observable.concat(
          favoritaLocalDataSource.getFavoritas(),
          favoritaRemoteDataSource.getFavoritas()
        );
    }

}
