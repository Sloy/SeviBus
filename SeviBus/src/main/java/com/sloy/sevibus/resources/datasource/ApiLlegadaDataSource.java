package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.Llegada;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;

import rx.Observable;
import rx.functions.Func1;

public class ApiLlegadaDataSource implements LlegadaDataSource {

    private final SevibusApi sevibusApi;

    public ApiLlegadaDataSource(SevibusApi sevibusApi) {
        this.sevibusApi = sevibusApi;
    }

    @Override
    public Observable<Llegada> getLlegada(String linea, Integer parada) throws ServerErrorException {
        return sevibusApi.getArrival(parada, linea)
                .retry(1)
                .map(new Func1<ArrivalTimesApiModel, Llegada>() {
                    @Override
                    public Llegada call(ArrivalTimesApiModel arrival) {
                        return arrivalToLLegada(arrival);
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
