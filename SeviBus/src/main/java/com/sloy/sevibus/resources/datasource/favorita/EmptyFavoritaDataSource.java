package com.sloy.sevibus.resources.datasource.favorita;

import com.sloy.sevibus.model.tussam.Favorita;

import java.util.List;

import rx.Observable;

public class EmptyFavoritaDataSource implements FavoritaDataSource {

    @Override
    public Observable<List<Favorita>> getFavoritas() {
        return Observable.empty();
    }

    @Override
    public Observable<Favorita> saveFavorita(Favorita favorita) {
        return Observable.empty();
    }

    @Override
    public Observable<Favorita> getFavoritaById(Integer idParada) {
        return Observable.empty();
    }

    @Override
    public Observable<Integer> deleteFavorita(Integer idParada) {
        return Observable.empty();
    }

    @Override
    public Observable<List<Favorita>> saveFavoritas(List<Favorita> favoritas) {
        return Observable.empty();
    }

    @Override
    public Observable<List<Favorita>> replaceFavoritas(List<Favorita> favoritas) {
        return Observable.empty();
    }
}
