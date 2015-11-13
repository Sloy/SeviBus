package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.Llegada;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;

public class ApiLlegadaDataSource implements LlegadaDataSource {

    private final SevibusApi sevibusApi;

    public ApiLlegadaDataSource(SevibusApi sevibusApi) {
        this.sevibusApi = sevibusApi;
    }

    @Override
    public Llegada getLlegada(String linea, Integer parada) throws ServerErrorException {
        ArrivalTimesApiModel arrival = sevibusApi.getArrival(parada, linea);
        Llegada llegada = new Llegada(linea);
        llegada.setBus1(mapBus(arrival.getNextBus()));
        llegada.setBus2(mapBus(arrival.getSecondBus()));
        return llegada;
    }

    private Llegada.Bus mapBus(ArrivalTimesApiModel.BusArrival nextBus) {
        return new Llegada.Bus(nextBus.getTimeInMinutes(), nextBus.getDistanceInMeters());
    }
}
