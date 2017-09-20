package com.sloy.sevibus.resources.actions.favorita;

import com.sloy.sevibus.domain.model.ParadaCollection;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.resources.datasource.favorita.FavoritaDataSource;

import rx.Observable;

import static rx.observables.MathObservable.max;

public class SaveFavoritaAction {

    private final ParadaCollection paradaCollection;
    private final FavoritaDataSource favoritaLocalDataSource;
    private final FavoritaDataSource favoritaRemoteDataSource;

    public SaveFavoritaAction(ParadaCollection paradaCollection, FavoritaDataSource favoritaLocalDataSource, FavoritaDataSource favoritaRemoteDataSource) {
        this.paradaCollection = paradaCollection;
        this.favoritaLocalDataSource = favoritaLocalDataSource;
        this.favoritaRemoteDataSource = favoritaRemoteDataSource;
    }

    public Observable<Void> saveFavorita(int idParada, String nombrePropio, int color) {
        return favoritaLocalDataSource.getFavoritaById(idParada)
          .switchIfEmpty(createFavorita(idParada, nombrePropio, color))
          .flatMap(favoritaLocalDataSource::saveFavorita)
          .flatMap(favoritaRemoteDataSource::saveFavorita)
          .flatMap(__ -> Observable.empty());
    }

    private Observable<Favorita> createFavorita(int idParada, String nombrePropio, int color) {
        Observable<Integer> maxOrderObservable = max(
          favoritaLocalDataSource.getFavoritas()
            .flatMap(Observable::from)
            .map(Favorita::getOrden)
            .switchIfEmpty(Observable.just(0)));

        Observable<Favorita> newFavoritaObservable = Observable.just(idParada)
          .flatMap(id -> paradaCollection.getById(id).toObservable())
          .map(parada -> {
              Favorita f = new Favorita();
              f.setParadaAsociada(parada);
              f.setNombrePropio(nombrePropio);
              f.setColor(color);
              return f;
          });

        return Observable.zip(maxOrderObservable, newFavoritaObservable, (maxOrder, newFavorita) -> {
            newFavorita.setOrden(maxOrder + 1);
            return newFavorita;
        });
    }

}
