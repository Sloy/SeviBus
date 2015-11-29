package com.sloy.sevibus.resources;


import com.sloy.sevibus.model.PaletaColores;
import com.sloy.sevibus.model.tussam.Favorita;

public interface AnalyticsTracker {

    void paradaViewed(Integer paradaNumber);

    void searchPerformed(String searchQuery);

    void trackTiempoRecibido(Integer paradaNumber, String lineName, Long responseTime, String dataSource);

    void databaseUpdatedSuccessfuly(boolean success);

    void favoritaColorized(PaletaColores paleta, Integer numeroParada);
}
