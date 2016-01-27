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
        return Observable.defer(() -> {
            try {
                QueryBuilder<Favorita, Integer> favQb = dbHelper.getDaoFavorita().queryBuilder();
                favQb.orderBy("orden", true);
                List<Favorita> favoritas = favQb.query();
                return Observable.defer(() -> Observable.just(favoritas));
            } catch (SQLException e) {
                return Observable.error(e);
            }
        });
    }

    @Override
    public Observable<Void> saveFavorita(Favorita favorita) {
        return Observable.just(favorita)
          .map(this::withCalculatedOrder)
          .map(favorita1 -> dbHelper.getDaoFavorita().createOrUpdate(favorita))
          .flatMap(status -> Observable.empty());
    }

    @Override
    public Observable<Favorita> getFavoritaById(Integer numeroParada) {
        return Observable.just(numeroParada)
          .flatMap(idParada -> {
              try {
                  QueryBuilder<Favorita, Integer> favoriteQuery = dbHelper.getDaoFavorita().queryBuilder();
                  List<Favorita> queryResult = favoriteQuery.where().eq("paradaAsociada_id", idParada).query();

                  if (queryResult != null && queryResult.size() > 0) {
                      return Observable.just(queryResult.get(0));
                  } else {
                      return Observable.empty();
                  }
              } catch (SQLException e) {
                  return Observable.error(e);
              }
          });
    }

    @Override
    public Observable<Void> deleteFavorita(Integer idParada) {
        return getFavoritaById(idParada)
          .flatMap(favorita -> {
              dbHelper.getDaoFavorita().delete(favorita);
              return Observable.empty();
          });
    }

    @Override
    public Observable<Void> saveFavoritas(List<Favorita> favoritas) {
        return Observable.defer(() -> {
            dbHelper.getDaoFavorita().callBatchTasks(() -> {
                for (Favorita f : favoritas) {
                    dbHelper.getDaoFavorita().createOrUpdate(f);
                }
                return true;
            });
            return Observable.empty();
        });
    }

    private Favorita withCalculatedOrder(Favorita favorita) {
        int count = (int) dbHelper.getDaoFavorita().countOf();
        favorita.setOrden(count + 1);
        return favorita;
    }
}
