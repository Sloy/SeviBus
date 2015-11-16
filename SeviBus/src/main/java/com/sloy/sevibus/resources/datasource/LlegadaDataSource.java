package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.ArrivalTime;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;

import rx.Observable;

public interface LlegadaDataSource {

    Observable<ArrivalTime> getLlegada(String linea, Integer parada) throws ServerErrorException;
}
