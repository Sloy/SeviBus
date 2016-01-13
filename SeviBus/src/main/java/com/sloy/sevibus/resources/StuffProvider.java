package com.sloy.sevibus.resources;

import android.content.Context;

import com.crashlytics.android.answers.Answers;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sloy.sevibus.BuildConfig;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloydev.retrofitendpointmodule.EndpointModule;
import com.sloy.sevibus.resources.actions.ObtainLlegadasAction;
import com.sloy.sevibus.resources.datasource.ApiErrorHandler;
import com.sloy.sevibus.resources.datasource.ApiLlegadaDataSource;
import com.sloy.sevibus.resources.datasource.LlegadaDataSource;
import com.sloy.sevibus.resources.datasource.SevibusApi;
import com.sloy.sevibus.resources.datasource.StringDownloader;
import com.sloy.sevibus.resources.datasource.TussamLlegadaDataSource;
import com.sloy.sevibus.resources.sync.UpdateDatabaseAction;
import com.sloy.sevibus.ui.AppContainer;
import com.sloy.sevibus.ui.DebugAppContainer;
import com.sloydev.retrofitendpointmodule.SimpleEndpoint;

import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RestAdapter;

public class StuffProvider {

    public static final String PRODUCTION_API_ENDPOINT = "http://api.sevibus.sloydev.com";
    public static final String STAGING_API_ENDPOINT = "https://sevibus-staging.herokuapp.com/";

    private static CrashReportingTool crashReportingToolInstance;

    public static AppContainer getAppContainer() {
        if (BuildConfig.DEBUG) {
            return new DebugAppContainer();
        } else {
            return AppContainer.DEFAULT;
        }
    }

    public static UpdateDatabaseAction getUpdateDatabaseAction(Context context) {
        return new UpdateDatabaseAction(context, getDbHelper(context), getStringDownloader());
    }

    private static DBHelper getDbHelper(Context context) {
        return OpenHelperManager.getHelper(context, DBHelper.class);
    }

    public static StringDownloader getStringDownloader() {
        return new StringDownloader();
    }

    public static AnalyticsTracker getAnalyticsTracker() {
        if (BuildConfig.DEBUG) {
            return new EmptyAnalyticsTracker();
        } else {
            return new AnswersAnalyticsTracker(Answers.getInstance());
        }
    }

    public static ObtainLlegadasAction getObtainLlegadaAction(Context context) {
        return new ObtainLlegadasAction(getLlegadaDataSource(context));
    }

    private static LlegadaDataSource getLlegadaDataSource(Context context) {
        return new ApiLlegadaDataSource(getSevibusApi(context), getLegacyLlegadaDataSource());
    }

    private static LlegadaDataSource getLegacyLlegadaDataSource() {
        return new TussamLlegadaDataSource();
    }

    private static SevibusApi getSevibusApi(Context context) {
        return new RestAdapter.Builder()
                .setEndpoint(getEndpoint(context))
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL: RestAdapter.LogLevel.NONE)
                .setErrorHandler(new ApiErrorHandler())
                .build()
                .create(SevibusApi.class);
    }

    public static CrashReportingTool getCrashReportingTool() {
        if (crashReportingToolInstance == null) {
            if (BuildConfig.DEBUG) {
                crashReportingToolInstance = new EmptyCrashReportingTool();
            } else {
                crashReportingToolInstance = new CrashlyticsReportingTool();
            }
        }
        return crashReportingToolInstance;
    }

    private static Endpoint getEndpoint(Context context) {
        if (BuildConfig.DEBUG) {
            return EndpointModule.getSelectedEndpoint(context);
        } else {
            return Endpoints.newFixedEndpoint(PRODUCTION_API_ENDPOINT);
        }
    }

}
