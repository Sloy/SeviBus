package com.sloy.sevibus.resources;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.SearchEvent;

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

}
