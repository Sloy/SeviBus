package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Linea;

import java.sql.SQLException;
import java.util.List;

import rx.Observable;

public class LineaDataSource {

    private final DBHelper dbHelper;

    public LineaDataSource(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    //FIXME this is just a legacy wrapper
    public Observable<List<Linea>> getFromParada(int numero) {
        return Observable.defer(() -> {
            try {
                return Observable.just(DBQueries.getLineasDeParada(dbHelper, numero));
            } catch (SQLException e) {
                return Observable.error(e);
            }
        });
    }
}
