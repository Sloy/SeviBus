package com.sloy.sevibus.resources.actions;

import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.datasource.FavoritaDataSource;

import rx.Observable;

public class SaveFavoritaAction {

    private final FavoritaDataSource favoritaLocalDataSource;
    private final DBHelper dbHelper; //TODO use parada datasource!!!

    public SaveFavoritaAction(FavoritaDataSource favoritaLocalDataSource, DBHelper dbHelper) {
        this.favoritaLocalDataSource = favoritaLocalDataSource;
        this.dbHelper = dbHelper;
    }

    public Observable<Void> saveFavorita(int idParada, String nombrePropio, int color) {
        return favoritaLocalDataSource.getFavoritaById(idParada)
          .switchIfEmpty(Observable.defer(() -> createFavorita(idParada, nombrePropio, color)))
          .single()
          .flatMap(favoritaLocalDataSource::saveFavorita);
    }

    private Observable<Favorita> createFavorita(int idParada, String nombrePropio, int color) {
        Parada parada = DBQueries.getParadaById(dbHelper, idParada);
        Favorita f = new Favorita();
        f.setParadaAsociada(parada);
        f.setNombrePropio(nombrePropio);
        f.setColor(color);
        return Observable.just(f);
    }

}
