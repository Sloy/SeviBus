package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.Llegada;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;

import rx.Observable;

public interface LlegadaDataSource {

    Observable<Llegada> getLlegada(String linea, Integer parada) throws ServerErrorException;
}
