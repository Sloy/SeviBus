package com.sloy.sevibus.resources.datasource.favorita;

import com.sloy.sevibus.model.tussam.Favorita;

import java.util.List;

import rx.Observable;

public interface FavoritaDataSource {

    Observable<List<Favorita>> getFavoritas();

    Observable<Void> saveFavorita(Favorita favorita);

    Observable<Favorita> getFavoritaById(Integer idParada);

    Observable<Void> deleteFavorita(Integer idParada);

    Observable<Void> saveFavoritas(List<Favorita> favoritas);
}
