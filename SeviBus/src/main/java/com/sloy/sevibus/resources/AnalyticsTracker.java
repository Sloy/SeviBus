package com.sloy.sevibus.resources;


public interface AnalyticsTracker {

    void paradaViewed(Integer paradaNumber);

    void searchPerformed(String searchQuery);

    TimeTracker trackTiempoRecibido(Integer paradaNumber, String lineName);

}
