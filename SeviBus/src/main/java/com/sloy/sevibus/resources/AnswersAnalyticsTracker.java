package com.sloy.sevibus.resources;

import android.os.Build;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.crashlytics.android.answers.SearchEvent;
import com.sloy.sevibus.model.PaletaColores;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.ui.other.CardWizardManager;

public class AnswersAnalyticsTracker implements AnalyticsTracker {

    private final Answers answers;

    public AnswersAnalyticsTracker(Answers answers) {
        this.answers = answers;
    }

    @Override
    public void paradaViewed(Integer paradaNumber) {
        answers.logContentView(new ContentViewEvent()
          .putContentName("Parada vista")
          .putContentType("Parada")
          .putContentId(String.valueOf(paradaNumber)));
    }

    @Override
    public void searchPerformed(String searchQuery) {
        answers.logSearch(new SearchEvent()
          .putQuery(searchQuery));
    }

    @Override
    public void trackTiempoRecibido(Integer paradaNumber, String lineName, Long responseTime, String dataSource) {
        answers.logCustom(new CustomEvent("Tiempo recibido")
          .putCustomAttribute("Parada", String.valueOf(paradaNumber))
          .putCustomAttribute("Linea", lineName)
          .putCustomAttribute("Fuente de datos", dataSource)
          .putCustomAttribute("Tiempo de respuesta ms", responseTime));
    }

    @Override
    public void databaseUpdatedSuccessfuly(boolean success) {
        answers.logCustom(new CustomEvent("Datos actualizados")
            .putCustomAttribute("Éxito", String.valueOf(success))
        );
    }

    @Override
    public void favoritaColorized(PaletaColores paleta, Integer numeroParada) {
        answers.logCustom(new CustomEvent("Parada colorizada")
          .putCustomAttribute("Color paleta", paleta.name())
          .putCustomAttribute("Parada", String.valueOf(numeroParada))
          .putCustomAttribute("Versión OS", String.valueOf(Build.VERSION.SDK_INT))
        );
    }

    @Override
    public void lineaAddedToMap(Linea linea, int totalCount) {
        answers.logCustom(new CustomEvent("Añade línea al mapa")
          .putCustomAttribute("Total count", Integer.toString(totalCount))
          .putCustomAttribute("Linea", linea.getNumero())
        );
    }

    @Override
    public void signInSuccess(CardWizardManager cardManager) {
        answers.logLogin(new LoginEvent()
          .putSuccess(true)
          .putCustomAttribute("Card Manager", cardManager.getDescription())
        );
    }

    @Override
    public void signInFailure(CardWizardManager cardManager) {
        answers.logLogin(new LoginEvent()
          .putSuccess(false)
          .putCustomAttribute("Card Manager", cardManager.getDescription())
        );
    }

    @Override
    public void signInLogout() {
        answers.logCustom(new CustomEvent("Logout"));
    }

}
