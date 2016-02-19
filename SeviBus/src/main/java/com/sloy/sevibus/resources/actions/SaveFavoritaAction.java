package com.sloy.sevibus.resources.actions;

import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.datasource.FavoritaDataSource;

import rx.Observable;

import static rx.observables.MathObservable.max;

public class SaveFavoritaAction {

    private final FavoritaDataSource favoritaLocalDataSource;
    private final DBHelper dbHelper; //TODO use parada datasource!!!

    public SaveFavoritaAction(FavoritaDataSource favoritaLocalDataSource, DBHelper dbHelper) {
        this.favoritaLocalDataSource = favoritaLocalDataSource;
        this.dbHelper = dbHelper;
    }

    public Observable<Void> saveFavorita(int idParada, String nombrePropio, int color) {
        return favoritaLocalDataSource.getFavoritaById(idParada)
          .switchIfEmpty(createFavorita(idParada, nombrePropio, color))
          .flatMap(favoritaLocalDataSource::saveFavorita);
    }

    private Observable<Favorita> createFavorita(int idParada, String nombrePropio, int color) {
        Observable<Integer> maxOrderObservable = max(
          favoritaLocalDataSource.getFavoritas()
            .flatMap(Observable::from)
            .map(Favorita::getOrden)
            .switchIfEmpty(Observable.just(0)));

        Observable<Favorita> newFavoritaObservable = Observable.just(idParada)
          .map(this::getParadaById)
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

    protected Parada getParadaById(Integer id) {
        return DBQueries.getParadaById(dbHelper, id);
    }

}
