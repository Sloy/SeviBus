package com.sloy.sevibus.resources;

import com.sloy.sevibus.model.PaletaColores;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.ui.other.CardWizardManager;

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

    @Override
    public void lineaAddedToMap(Linea linea, int totalCount) {
        /* no-op */
    }

    @Override
    public void signInSuccess(CardWizardManager cardManager) {
        /* no-op */
    }

    @Override
    public void signInFailure(CardWizardManager cardManager) {
        /* no-op */
    }

    @Override
    public void signInLogout() {
        /* no-op */
    }

}
