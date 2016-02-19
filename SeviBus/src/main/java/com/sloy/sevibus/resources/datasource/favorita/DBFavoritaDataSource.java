package com.sloy.sevibus.resources.datasource.favorita;

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
                return Observable.just(favoritas);
            } catch (SQLException e) {
                return Observable.error(e);
            }
        });
    }

    @Override
    public Observable<Favorita> saveFavorita(Favorita favorita) {
        return Observable.just(favorita)
          .map(favorita1 -> dbHelper.getDaoFavorita().createOrUpdate(favorita))
          .flatMap(__ -> Observable.just(favorita));
    }

    @Override
    public Observable<Favorita> getFavoritaById(Integer numeroParada) {
        return Observable.defer(() -> {
            try {
                QueryBuilder<Favorita, Integer> favoriteQuery = dbHelper.getDaoFavorita().queryBuilder();
                List<Favorita> queryResult = favoriteQuery.where().eq("paradaAsociada_id", numeroParada).query();

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
    public Observable<Integer> deleteFavorita(Integer idParada) {
        return getFavoritaById(idParada)
          .flatMap(favorita -> {
              dbHelper.getDaoFavorita().delete(favorita);
              return Observable.just(idParada);
          });
    }

    @Override
    public Observable<List<Favorita>> saveFavoritas(List<Favorita> favoritas) {
        return Observable.defer(() -> {
            dbHelper.getDaoFavorita().callBatchTasks(() -> {
                for (Favorita f : favoritas) {
                    dbHelper.getDaoFavorita().createOrUpdate(f);
                }
                return true;
            });
            return Observable.just(favoritas);
        });
    }

}
