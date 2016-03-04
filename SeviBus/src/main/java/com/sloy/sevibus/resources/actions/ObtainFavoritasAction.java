package com.sloy.sevibus.resources.actions;

import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.resources.datasource.FavoritaDataSource;

import java.util.List;

import rx.Observable;

public class ObtainFavoritasAction {

    private final FavoritaDataSource favoritaLocalDataSource;

    public ObtainFavoritasAction(FavoritaDataSource favoritaLocalDataSource) {
        this.favoritaLocalDataSource = favoritaLocalDataSource;
    }

    public Observable<List<Favorita>> getFavoritas(){
        return favoritaLocalDataSource.getFavoritas();
    }

}
