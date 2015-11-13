package com.sloy.sevibus.resources.actions;

import com.sloy.sevibus.model.Llegada;
import com.sloy.sevibus.resources.datasource.LlegadaDataSource;

public class ObtainLlegadasAction {

    private final LlegadaDataSource llegadaDataSource;

    public ObtainLlegadasAction(LlegadaDataSource llegadaDataSource) {
        this.llegadaDataSource = llegadaDataSource;
    }

    public Llegada getLlegada(String linea, Integer parada) {
        return llegadaDataSource.getLlegada(linea, parada);
    }
}
