package com.sloy.sevibus.resources.actions;

import android.location.Location;

import com.sloy.sevibus.model.ParadaCercana;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.datasource.LineaDataSource;

import rx.Observable;


public class ObtainLineasCercanasAction {

    private final LineaDataSource lineaDataSource;
    private final ObtainCercanasAction obtainCercanasAction;

    public ObtainLineasCercanasAction(LineaDataSource lineaDataSource, ObtainCercanasAction obtainCercanasAction) {
        this.lineaDataSource = lineaDataSource;
        this.obtainCercanasAction = obtainCercanasAction;
    }

    public Observable<Linea> obtainLineas(Location location) {
        return obtainCercanasAction.obtainCercanas(location)
          .map(ParadaCercana::getParada)
          .map(Parada::getNumero)
          .flatMap(lineaDataSource::getFromParada)
          .flatMap(Observable::from)
          .distinct()
          .toSortedList()
          .flatMap(Observable::from);
    }
}
