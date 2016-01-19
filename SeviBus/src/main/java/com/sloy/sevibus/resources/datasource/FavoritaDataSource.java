package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.tussam.Favorita;

import java.util.List;

import rx.Observable;

public interface FavoritaDataSource {

    Observable<List<Favorita>> getFavoritas();

    void saveFavorita(Favorita favorita);
}
