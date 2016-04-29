package com.sloy.sevibus.resources.actions;

import com.sloydev.gallego.Optional;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.resources.datasource.FavoritaDataSource;

import rx.Observable;

public class ObtainSingleFavoritaAction {

    private final FavoritaDataSource favoritaDataSource;

    public ObtainSingleFavoritaAction(FavoritaDataSource favoritaDataSource) {
        this.favoritaDataSource = favoritaDataSource;
    }

    public Observable<Optional<Favorita>> obtainFavorita(Integer paradaNumero) {
        return favoritaDataSource.getFavoritaById(paradaNumero)
          .map(Optional::of)
          .defaultIfEmpty(Optional.absent());
    }

}
