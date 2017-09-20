package com.sloy.sevibus.resources.actions;

import android.location.Location;

import com.sloy.sevibus.domain.model.ParadaCollection;
import com.sloy.sevibus.model.ParadaCercana;
import com.sloy.sevibus.model.tussam.Parada;

import rx.Observable;

public class ObtainCercanasAction {

    private final ParadaCollection paradaCollection;

    public ObtainCercanasAction(ParadaCollection paradaCollection) {
        this.paradaCollection = paradaCollection;
    }

    public Observable<ParadaCercana> obtainCercanas(Location location) {
        double margen = 0.005;
        double maxLatitud = location.getLatitude() + margen;
        double minLatitud = location.getLatitude() - margen;
        double maxLongitud = location.getLongitude() + margen;
        double minLongitud = location.getLongitude() - margen;

        return paradaCollection.getByLocation(minLatitud, maxLatitud, minLongitud, maxLongitud)
          .map(parada -> new ParadaCercana(parada, distancia(parada, location)))
          .toSortedList((first, second) -> first.getDistancia() - second.getDistancia())
          .flatMap(Observable::from);
    }

    private static int distancia(Parada parada, Location location) {
        float[] distanceResult = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(), parada.getLatitud(), parada.getLongitud(), distanceResult);
        return Math.round(distanceResult[0]);
    }
}
