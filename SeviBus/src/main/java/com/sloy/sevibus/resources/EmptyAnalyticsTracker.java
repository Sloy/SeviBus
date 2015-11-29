package com.sloy.sevibus.resources;

import com.sloy.sevibus.model.PaletaColores;

public class EmptyAnalyticsTracker implements AnalyticsTracker {
    @Override
    public void paradaViewed(Integer paradaNumber) {
        /* no-op */
    }

    @Override
    public void searchPerformed(String searchQuery) {
        /* no-op */
    }

    @Override
    public void trackTiempoRecibido(Integer paradaNumber, String lineName, Long responseTime, String dataSource) {
        /* no-op */
    }

    @Override
    public void databaseUpdatedSuccessfuly(boolean success) {
        /* no-op */
    }

    @Override
    public void favoritaColorized(PaletaColores paleta, Integer numeroParada) {
        /* no-op */
    }

}
