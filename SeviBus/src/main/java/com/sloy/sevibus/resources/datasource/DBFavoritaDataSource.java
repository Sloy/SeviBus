package com.sloy.sevibus.resources.datasource;

import com.j256.ormlite.stmt.QueryBuilder;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.model.tussam.Favorita;

import java.sql.SQLException;
import java.util.List;

import rx.Observable;

public class DBFavoritaDataSource implements FavoritaDataSource {

    private final DBHelper dbHelper;

    public DBFavoritaDataSource(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public Observable<List<Favorita>> getFavoritas() {
        try {
            QueryBuilder<Favorita, Integer> favQb = dbHelper.getDaoFavorita().queryBuilder();
            favQb.orderBy("orden", true);
            List<Favorita> favoritas = favQb.query();
            return Observable.defer(() -> Observable.just(favoritas));
        } catch (SQLException e) {
            return Observable.error(e);
        }
    }

    @Override
    public void saveFavorita(Favorita favorita) {
        int count = (int) dbHelper.getDaoFavorita().countOf();
        favorita.setOrden(count + 1);
        dbHelper.getDaoFavorita().createOrUpdate(favorita);
    }
}
