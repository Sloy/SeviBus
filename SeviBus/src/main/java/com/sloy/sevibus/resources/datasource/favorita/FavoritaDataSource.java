package com.sloy.sevibus.resources.datasource.favorita;

import com.sloy.sevibus.model.tussam.Favorita;

import java.util.List;

import rx.Observable;

public interface FavoritaDataSource {

    Observable<List<Favorita>> getFavoritas();

    Observable<Favorita> saveFavorita(Favorita favorita);

    Observable<Favorita> getFavoritaById(Integer idParada);

    Observable<Integer> deleteFavorita(Integer idParada);

    Observable<List<Favorita>> saveFavoritas(List<Favorita> favoritas);
}
