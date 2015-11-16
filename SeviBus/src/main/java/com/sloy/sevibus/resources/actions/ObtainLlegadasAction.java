package com.sloy.sevibus.resources.actions;

import com.sloy.sevibus.model.ArrivalTime;
import com.sloy.sevibus.resources.datasource.LlegadaDataSource;

import rx.Observable;

public class ObtainLlegadasAction {

    private final LlegadaDataSource llegadaDataSource;

    public ObtainLlegadasAction(LlegadaDataSource llegadaDataSource) {
        this.llegadaDataSource = llegadaDataSource;
    }

    public Observable<ArrivalTime> getLlegada(final String linea, final Integer parada) {
        return Observable.defer(() -> llegadaDataSource.getLlegada(linea, parada));
    }
}
