package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.tussam.Favorita;

import java.util.List;

import rx.Observable;

public class EmptyFavoritaDataSource implements FavoritaDataSource {

    @Override
    public Observable<List<Favorita>> getFavoritas() {
        return Observable.empty();
    }

    @Override
    public Observable<Void> saveFavorita(Favorita favorita) {
        return Observable.empty();
    }

    @Override
    public Observable<Favorita> getFavoritaById(Integer idParada) {
        return Observable.empty();
    }

    @Override
    public Observable<Void> deleteFavorita(Integer idParada) {
        return Observable.empty();
    }

    @Override
    public Observable<Void> saveFavoritas(List<Favorita> favoritas) {
        return Observable.empty();
    }
}
