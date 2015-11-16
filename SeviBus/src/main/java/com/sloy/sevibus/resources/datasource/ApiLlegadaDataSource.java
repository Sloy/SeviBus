package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.ArrivalTime;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;

import rx.Observable;

public class ApiLlegadaDataSource implements LlegadaDataSource {

    private final SevibusApi sevibusApi;
    private final LlegadaDataSource fallbackDataSource;

    public ApiLlegadaDataSource(SevibusApi sevibusApi, LlegadaDataSource fallbackDataSource) {
        this.sevibusApi = sevibusApi;
        this.fallbackDataSource = fallbackDataSource;
    }

    @Override
    public Observable<ArrivalTime> getLlegada(final String linea, final Integer parada) throws ServerErrorException {
        return sevibusApi.getArrival(parada, linea)
                .retry(1)
                .onErrorResumeNext(e -> getFallbackLlegada(linea, parada));
    }

    private Observable<ArrivalTime> getFallbackLlegada(String linea, Integer parada) {
        return fallbackDataSource.getLlegada(linea, parada);
    }
}
