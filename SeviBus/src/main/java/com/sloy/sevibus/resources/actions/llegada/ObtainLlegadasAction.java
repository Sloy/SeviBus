package com.sloy.sevibus.resources.actions.llegada;

import com.sloy.sevibus.model.ArrivalTime;
import com.sloy.sevibus.resources.datasource.llegada.LlegadaDataSource;

import java.util.List;

import rx.Observable;

public class ObtainLlegadasAction {

    private final LlegadaDataSource llegadaDataSource;

    public ObtainLlegadasAction(LlegadaDataSource llegadaDataSource) {
        this.llegadaDataSource = llegadaDataSource;
    }

    public Observable<ArrivalTime> getLlegadas(final Integer parada, final List<String> lineas) {
        return Observable.defer(() -> llegadaDataSource.getLlegadas(parada, lineas));
    }
}
