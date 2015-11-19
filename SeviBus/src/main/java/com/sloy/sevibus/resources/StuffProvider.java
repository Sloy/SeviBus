package com.sloy.sevibus.resources;

import android.content.Context;

import com.crashlytics.android.answers.Answers;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.resources.actions.ObtainLlegadasAction;
import com.sloy.sevibus.resources.datasource.ApiErrorHandler;
import com.sloy.sevibus.resources.datasource.ApiLlegadaDataSource;
import com.sloy.sevibus.resources.datasource.LlegadaDataSource;
import com.sloy.sevibus.resources.datasource.SevibusApi;
import com.sloy.sevibus.resources.datasource.StringDownloader;
import com.sloy.sevibus.resources.datasource.TussamLlegadaDataSource;
import com.sloy.sevibus.resources.sync.UpdateDatabaseAction;

import retrofit.RestAdapter;

public class StuffProvider {

    public static final String API_ENDPOING = "http://api.sevibus.sloydev.com";

    public static UpdateDatabaseAction getUpdateDatabaseAction(Context context) {
        return new UpdateDatabaseAction(context, getDbHelper(context), getStringDownloader());
    }

    private static DBHelper getDbHelper(Context context) {
        return OpenHelperManager.getHelper(context, DBHelper.class);
    }

    public static StringDownloader getStringDownloader() {
        return new StringDownloader();
    }

    public static AnalyticsTracker getAnalyticsTracker(Context context) {
        if (Debug.isReportsEnabled(context)) {
            return new AnswersAnalyticsTracker(Answers.getInstance());
        } else {
            return new EmptyAnalyticsTracker();
        }
    }

    public static ObtainLlegadasAction getObtainLlegadaAction() {
        return new ObtainLlegadasAction(getLlegadaDataSource());
    }

    private static LlegadaDataSource getLlegadaDataSource() {
        return new ApiLlegadaDataSource(getSevibusApi(), getLegacyLlegadaDataSource());
    }

    private static LlegadaDataSource getLegacyLlegadaDataSource() {
        return new TussamLlegadaDataSource();
    }

    private static SevibusApi getSevibusApi() {
        return new RestAdapter.Builder()
                .setEndpoint(API_ENDPOING)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setErrorHandler(new ApiErrorHandler())
                .build()
                .create(SevibusApi.class);
    }

}
