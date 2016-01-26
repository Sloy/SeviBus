package com.sloy.sevibus.resources.datasource;

import com.google.common.base.Optional;
import com.sloy.sevibus.model.tussam.Favorita;

import java.util.List;

import rx.Observable;

public interface FavoritaDataSource {

    Observable<List<Favorita>> getFavoritas();

    void saveFavorita(Favorita favorita);

    Observable<Optional<Favorita>> getFavoritaById(Integer idParada);

    void deleteFavorita(Integer idParada);

    void deleteAll();

    void saveFavoritas(List<Favorita> favoritas);
}
