package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.Llegada;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;

import rx.Observable;
import rx.functions.Func1;

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
                .map(new Func1<ArrivalTimesApiModel, Llegada>() {
                    @Override
                    public Llegada call(ArrivalTimesApiModel arrival) {
                        return arrivalToLLegada(arrival);
                    }
                })
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends Llegada>>() {
                    @Override
                    public Observable<? extends Llegada> call(Throwable throwable) {
                        return fallbackDataSource.getLlegada(linea, parada);
                    }
                });
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
