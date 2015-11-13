package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.Llegada;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;

public interface LlegadaDataSource {

    Llegada getLlegada(String linea, Integer parada) throws ServerErrorException;
}
