package com.sloy.sevibus.resources.datasource.llegada;

import com.sloy.sevibus.model.ArrivalTime;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;

import java.util.List;

import rx.Observable;

public interface LlegadaDataSource {

    Observable<ArrivalTime> getLlegadas(Integer parada, List<String> lineas) throws ServerErrorException;
}
