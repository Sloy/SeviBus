package com.sloy.sevibus.resources.actions;

import com.sloy.sevibus.model.Llegada;
import com.sloy.sevibus.resources.datasource.LlegadaDataSource;

import rx.Observable;
import rx.functions.Func0;

public class ObtainLlegadasAction {

    private final LlegadaDataSource llegadaDataSource;

    public ObtainLlegadasAction(LlegadaDataSource llegadaDataSource) {
        this.llegadaDataSource = llegadaDataSource;
    }

    public Observable<Llegada> getLlegada(final String linea, final Integer parada) {
        return Observable.defer(() -> llegadaDataSource.getLlegada(linea, parada));
    }
}
