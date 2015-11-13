package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.Llegada;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;

import rx.Observable;
import rx.schedulers.Schedulers;

public class ApiLlegadaDataSource implements LlegadaDataSource {

    private final SevibusApi sevibusApi;
    private final LlegadaDataSource fallbackDataSource;

    public ApiLlegadaDataSource(SevibusApi sevibusApi, LlegadaDataSource fallbackDataSource) {
        this.sevibusApi = sevibusApi;
        this.fallbackDataSource = fallbackDataSource;
    }

    @Override
    public Observable<Llegada> getLlegada(final String linea, final Integer parada) throws ServerErrorException {
        return sevibusApi.getArrival(parada, linea)
                .retry(1)
                .map(this::arrivalToLLegada)
                .onErrorResumeNext(e -> getFallbackLlegada(linea, parada));
    }

    private Observable<Llegada> getFallbackLlegada(String linea, Integer parada) {
        return fallbackDataSource.getLlegada(linea, parada).subscribeOn(Schedulers.io());
    }

    private Llegada arrivalToLLegada(ArrivalTimesApiModel arrival) {
        Llegada llegada = new Llegada(arrival.getBusLineName());
        llegada.setBus1(mapBus(arrival.getNextBus()));
        llegada.setBus2(mapBus(arrival.getSecondBus()));
        return llegada;
    }

    private Llegada.Bus mapBus(ArrivalTimesApiModel.BusArrival nextBus) {
        return new Llegada.Bus(nextBus.getTimeInMinutes(), nextBus.getDistanceInMeters());
    }
}
