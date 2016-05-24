package com.sloy.sevibus.resources;


import com.sloy.sevibus.model.PaletaColores;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Linea;

public interface AnalyticsTracker {

    void paradaViewed(Integer paradaNumber);

    void searchPerformed(String searchQuery);

    void trackTiempoRecibido(Integer paradaNumber, String lineName, Long responseTime, String dataSource);

    void databaseUpdatedSuccessfuly(boolean success);

    void favoritaColorized(PaletaColores paleta, Integer numeroParada);

    void lineaAddedToMap(Linea linea, int totalCount);

    void betaSignInConfirmationAccepted();

    void betaSignInConfirmationRejected();

    void betaSignInConfirmationMoreInfo();

    void betaSignInFeedbackGplus();

    void betaSignInFeedbackTwitter();

    void betaSignInFeedbackMail();

    void signInSuccess(long waitingMillis);

    void signInFailure();

    void signInLogout();
}
