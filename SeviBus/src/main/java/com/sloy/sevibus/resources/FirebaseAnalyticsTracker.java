package com.sloy.sevibus.resources;


import android.os.Build;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.sloy.sevibus.model.PaletaColores;
import com.sloy.sevibus.model.tussam.Linea;

public class FirebaseAnalyticsTracker implements AnalyticsTracker {

    private final FirebaseAnalytics firebaseAnalytics;

    public FirebaseAnalyticsTracker(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

    @Override
    public void paradaViewed(Integer paradaNumber) {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(paradaNumber));
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Parada");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, params);
    }

    @Override
    public void searchPerformed(String searchQuery) {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.SEARCH_TERM, searchQuery);
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Parada");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, params);
    }

    @Override
    public void trackTiempoRecibido(Integer paradaNumber, String lineName, Long responseTime, String dataSource) {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(paradaNumber));
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Tiempo recibido");
        params.putString("fuente_datos", dataSource);
        params.putString("tiempo_respuesta_ms", String.valueOf(responseTime));
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, params);
    }

    @Override
    public void databaseUpdatedSuccessfuly(boolean success) {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Datos actualizados");
        params.putString("success", String.valueOf(success));
        firebaseAnalytics.logEvent("database_updated", params);
    }

    @Override
    public void favoritaColorized(PaletaColores paleta, Integer numeroParada) {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(numeroParada));
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Parada colorizada");
        params.putString("color_paleta", paleta.name());
        params.putString("version_os", String.valueOf(Build.VERSION.SDK_INT));
        firebaseAnalytics.logEvent("favorita_colorized", params);
    }

    @Override
    public void lineaAddedToMap(Linea linea, int totalCount) {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(linea.getNumero()));
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Añade línea al mapa");
        params.putString("total_count", String.valueOf(totalCount));
        firebaseAnalytics.logEvent("linea_added_to_map", params);
    }

    @Override
    public void betaSignInConfirmationAccepted() {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "(beta) SignIn Confirmation");
        params.putString("option", "accepted");
        firebaseAnalytics.logEvent("beta_signin_confirmation_", params);
    }

    @Override
    public void betaSignInConfirmationRejected() {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "(beta) SignIn Confirmation");
        params.putString("option", "rejected");
        firebaseAnalytics.logEvent("beta_signin_confirmation", params);
    }

    @Override
    public void betaSignInConfirmationMoreInfo() {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "(beta) SignIn Confirmation");
        params.putString("option", "info");
        firebaseAnalytics.logEvent("beta_signin_confirmation", params);
    }

    @Override
    public void betaSignInFeedbackGplus() {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "(beta) SignIn Feedback");
        params.putString("method", "gplus");
        firebaseAnalytics.logEvent("beta_signin_feedback", params);
    }

    @Override
    public void betaSignInFeedbackTwitter() {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "(beta) SignIn Feedback");
        params.putString("method", "twitter");
        firebaseAnalytics.logEvent("beta_signin_feedback", params);
    }

    @Override
    public void betaSignInFeedbackMail() {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "(beta) SignIn Feedback");
        params.putString("method", "mail");
        firebaseAnalytics.logEvent("beta_signin_feedback", params);
    }

    @Override
    public void signInSuccess(long waitingMillis) {
        Bundle params = new Bundle();
        params.putString("delay", String.valueOf(waitingMillis));
        params.putString("success", "true");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, params);
    }

    @Override
    public void signInFailure() {
        Bundle params = new Bundle();
        params.putString("success", "false");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, params);
    }

    @Override
    public void signInLogout() {
        firebaseAnalytics.logEvent("logout", null);
    }
}
