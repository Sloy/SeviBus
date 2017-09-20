package com.sloy.sevibus.resources.actions;

import android.location.Location;

import com.sloy.sevibus.domain.model.LineaCollection;
import com.sloy.sevibus.model.ParadaCercana;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;

import rx.Observable;


public class ObtainLineasCercanasAction {

    private final LineaCollection lineaCollection;
    private final ObtainCercanasAction obtainCercanasAction;

    public ObtainLineasCercanasAction(LineaCollection lineaCollection, ObtainCercanasAction obtainCercanasAction) {
        this.lineaCollection = lineaCollection;
        this.obtainCercanasAction = obtainCercanasAction;
    }

    public Observable<Linea> obtainLineas(Location location) {
        return obtainCercanasAction.obtainCercanas(location)
          .map(ParadaCercana::getParada)
          .map(Parada::getNumero)
          .flatMap(lineaCollection::getByParada)
          .distinct()
          .toSortedList()
          .flatMap(Observable::from);
    }
}
