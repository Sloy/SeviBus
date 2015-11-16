package com.sloy.sevibus.resources;

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
    public TimeTracker trackTiempoRecibido(Integer paradaNumber, String lineName) {
        return TimeTracker.NULL;
    }
}
