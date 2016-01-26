package com.sloy.sevibus.resources.datasource;

import com.google.common.base.Optional;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.model.tussam.Favorita;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

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

    @Override
    public Observable<Optional<Favorita>> getFavoritaById(Integer idParada) {
        try {
            QueryBuilder<Favorita, Integer> favoriteQuery = dbHelper.getDaoFavorita().queryBuilder();
            List<Favorita> queryResult = favoriteQuery.where().eq("paradaAsociada_id", idParada).query();

            Optional<Favorita> result;
            if (queryResult != null && queryResult.size() > 0) {
                result = Optional.of(queryResult.get(0));
            } else {
                result = Optional.absent();
            }
            return Observable.defer(() -> Observable.just(result));
        } catch (SQLException e) {
            return Observable.error(e);
        }
    }

    @Override
    public void deleteFavorita(Integer idParada) {
        getFavoritaById(idParada)
          .subscribe(current -> {
              if (current.isPresent()) {
                  dbHelper.getDaoFavorita().delete(current.get());
              }
          });
    }

    @Override
    public void deleteAll() {
        try {
            TableUtils.clearTable(dbHelper.getConnectionSource(), Favorita.class);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void saveFavoritas(List<Favorita> favoritas) {
        dbHelper.getDaoFavorita().callBatchTasks(() -> {
            for (Favorita f : favoritas) {
                dbHelper.getDaoFavorita().createOrUpdate(f);
            }
            return true;
        });
    }
}
