package com.sloy.sevibus.resources.actions;

import android.location.Location;

import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.ParadaCercana;
import com.sloy.sevibus.model.tussam.Parada;

import java.sql.SQLException;
import java.util.List;

import rx.Observable;

public class ObtainCercanasAction {

    private final DBHelper dbHelper;

    public ObtainCercanasAction(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    //TODO legacy wrapper
    public Observable<ParadaCercana> obtainCercanas(Location location) {
        return Observable.defer(() -> {
            try {
                List<Parada> cercanas = DBQueries.getParadasCercanas(dbHelper, location.getLatitude(), location.getLongitude(), true);
                return Observable.just(cercanas);
            } catch (SQLException e) {
                return Observable.error(e);
            }
        }).flatMap(Observable::from)
          .map(parada -> new ParadaCercana(parada, distancia(parada, location)));
    }

    private int distancia(Parada parada, Location location) {
        float[] distanceResult = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(), parada.getLatitud(), parada.getLongitud(), distanceResult);
        return Math.round(distanceResult[0]);
    }
}
