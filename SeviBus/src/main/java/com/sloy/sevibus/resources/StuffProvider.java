package com.sloy.sevibus.resources;

import android.content.Context;

import com.crashlytics.android.answers.Answers;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sloy.sevibus.BuildConfig;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.resources.actions.DeleteFavoritaAction;
import com.sloy.sevibus.resources.actions.ObtainFavoritasAction;
import com.sloy.sevibus.resources.actions.ObtainLlegadasAction;
import com.sloy.sevibus.resources.actions.ObtainSingleFavoritaAction;
import com.sloy.sevibus.resources.actions.ReorderFavoritasAction;
import com.sloy.sevibus.resources.actions.SaveFavoritaAction;
import com.sloy.sevibus.resources.datasource.ApiErrorHandler;
import com.sloy.sevibus.resources.datasource.ApiLlegadaDataSource;
import com.sloy.sevibus.resources.datasource.DBFavoritaDataSource;
import com.sloy.sevibus.resources.datasource.FavoritaDataSource;
import com.sloy.sevibus.resources.datasource.LlegadaDataSource;
import com.sloy.sevibus.resources.datasource.SevibusApi;
import com.sloy.sevibus.resources.datasource.StringDownloader;
import com.sloy.sevibus.resources.datasource.TussamLlegadaDataSource;
import com.sloy.sevibus.resources.sync.UpdateDatabaseAction;

import retrofit.RestAdapter;

public class StuffProvider {

    public static final String PRODUCTION_API_ENDPOINT = "http://api.sevibus.sloydev.com";
    public static final String STAGING_API_ENDPOINT = "https://sevibus-staging.herokuapp.com/";
    public static final String API_ENDPOINT = PRODUCTION_API_ENDPOINT;

    private static CrashReportingTool crashReportingToolInstance;

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
                .setEndpoint(API_ENDPOINT)
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

    public static ObtainFavoritasAction getObtainFavoritasAction(Context context) {
        return new ObtainFavoritasAction(getFavoritaDataSource(context));
    }

    public static ObtainSingleFavoritaAction getObtainSingleFavoritaAction(Context context) {
        return new ObtainSingleFavoritaAction(getFavoritaDataSource(context));
    }

    public static DeleteFavoritaAction getDeleteFavoritaAction(Context context) {
        return new DeleteFavoritaAction(getFavoritaDataSource(context));
    }

    public static SaveFavoritaAction getSaveFavoritaAction(Context context) {
        return new SaveFavoritaAction(getFavoritaDataSource(context), getDbHelper(context));
    }

    public static ReorderFavoritasAction getReorderFavoritasAction(Context context) {
        return new ReorderFavoritasAction(getFavoritaDataSource(context));
    }

    public static FavoritaDataSource getFavoritaDataSource(Context context) {
        return new DBFavoritaDataSource(getDbHelper(context));
    }
}
