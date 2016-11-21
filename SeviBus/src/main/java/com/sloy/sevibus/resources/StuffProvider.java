package com.sloy.sevibus.resources;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.answers.Answers;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sloy.sevibus.BuildConfig;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.resources.actions.ObtainCercanasAction;
import com.sloy.sevibus.resources.actions.ObtainLineasCercanasAction;
import com.sloy.sevibus.resources.actions.favorita.DeleteFavoritaAction;
import com.sloy.sevibus.resources.actions.favorita.ObtainFavoritasAction;
import com.sloy.sevibus.resources.actions.favorita.ObtainSingleFavoritaAction;
import com.sloy.sevibus.resources.actions.favorita.ReorderFavoritasAction;
import com.sloy.sevibus.resources.actions.favorita.SaveFavoritaAction;
import com.sloy.sevibus.resources.actions.llegada.ObtainLlegadasAction;
import com.sloy.sevibus.resources.actions.user.LogInAction;
import com.sloy.sevibus.resources.actions.user.LogOutAction;
import com.sloy.sevibus.resources.actions.user.ObtainUserAction;
import com.sloy.sevibus.resources.datasource.ApiErrorHandler;
import com.sloy.sevibus.resources.datasource.LineaDataSource;
import com.sloy.sevibus.resources.datasource.StringDownloader;
import com.sloy.sevibus.resources.datasource.favorita.AuthAwareFavoritaDataSource;
import com.sloy.sevibus.resources.datasource.favorita.DBFavoritaDataSource;
import com.sloy.sevibus.resources.datasource.favorita.FavoritaDataSource;
import com.sloy.sevibus.resources.datasource.favorita.FirebaseFavoritaDataSource;
import com.sloy.sevibus.resources.datasource.llegada.ApiLlegadaDataSource;
import com.sloy.sevibus.resources.datasource.llegada.LlegadaDataSource;
import com.sloy.sevibus.resources.datasource.SevibusApi;
import com.sloy.sevibus.resources.datasource.llegada.TussamLlegadaDataSource;
import com.sloy.sevibus.resources.datasource.user.PreferencesUserDataSource;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;
import com.sloy.sevibus.resources.services.LoginService;
import com.sloy.sevibus.resources.sync.UpdateDatabaseAction;
import com.sloy.sevibus.ui.mvp.presenter.UserInfoHeaderPresenter;

import retrofit.RestAdapter;

public class StuffProvider {

    private static final String PRODUCTION_API_ENDPOINT = "http://api.sevibus.sloydev.com";
    public static final String STAGING_API_ENDPOINT = "https://sevibus-staging.herokuapp.com/";
    private static final String API_ENDPOINT = PRODUCTION_API_ENDPOINT;

    private static CrashReportingTool crashReportingToolInstance;

    private static Gson getGson() {
        return new Gson();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static UpdateDatabaseAction getUpdateDatabaseAction(Context context) {
        return new UpdateDatabaseAction(context, getDbHelper(context), getStringDownloader());
    }

    private static DBHelper getDbHelper(Context context) {
        return OpenHelperManager.getHelper(context, DBHelper.class);
    }

    static StringDownloader getStringDownloader() {
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

    public static SevibusApi getSevibusApi() {
        return new RestAdapter.Builder()
          .setEndpoint(API_ENDPOINT)
          .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
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
        return new ObtainFavoritasAction(getLocalFavoritaDataSource(context), getRemoteFavoritaDataSource(context));
    }

    public static ObtainSingleFavoritaAction getObtainSingleFavoritaAction(Context context) {
        return new ObtainSingleFavoritaAction(getLocalFavoritaDataSource(context));
    }

    public static DeleteFavoritaAction getDeleteFavoritaAction(Context context) {
        return new DeleteFavoritaAction(getLocalFavoritaDataSource(context), getRemoteFavoritaDataSource(context));
    }

    public static SaveFavoritaAction getSaveFavoritaAction(Context context) {
        return new SaveFavoritaAction(getLocalFavoritaDataSource(context), getRemoteFavoritaDataSource(context), getDbHelper(context));
    }

    public static ReorderFavoritasAction getReorderFavoritasAction(Context context) {
        return new ReorderFavoritasAction(getLocalFavoritaDataSource(context), getRemoteFavoritaDataSource(context));
    }

    private static FavoritaDataSource getLocalFavoritaDataSource(Context context) {
        return new DBFavoritaDataSource(getDbHelper(context));
    }

    private static FavoritaDataSource getRemoteFavoritaDataSource(Context context) {
        UserDataSource userDataSource = getUserDataSource(context);
        return new AuthAwareFavoritaDataSource(new FirebaseFavoritaDataSource(getFirebaseDatabase(), userDataSource), userDataSource);
    }

    private static FirebaseAuth getFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    private static FirebaseDatabase getFirebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }

    public static ObtainCercanasAction getObtainCercanasAction(Context context) {
        return new ObtainCercanasAction(getDbHelper(context));
    }

    public static ObtainLineasCercanasAction getObtainLineasCercanasAction(Context context) {
        return new ObtainLineasCercanasAction(new LineaDataSource(getDbHelper(context)), getObtainCercanasAction(context));
    }

    public static ObtainUserAction getObtainUserAction(Context context) {
        return new ObtainUserAction(getUserDataSource(context), getCrashReportingTool());
    }

    private static UserDataSource getUserDataSource(Context context) {
        return new PreferencesUserDataSource(getSharedPreferences(context), getGson());
    }

    public static LogInAction getLoginAction(Context context) {
        return new LogInAction(getUserDataSource(context), getLoginService(), getFirebaseDatabase(), getCrashReportingTool());
    }

    private static LoginService getLoginService() {
        return new LoginService(getFirebaseAuth());
    }

    public static LogOutAction getLogoutAction(Context context) {
        return new LogOutAction(getUserDataSource(context), getFirebaseAuth());
    }

    public static RemoteConfiguration getRemoteConfiguration() {
        return new FirebaseRemoteConfiguration(FirebaseRemoteConfig.getInstance());
    }

    public static UserInfoHeaderPresenter getUserInfoHeaderPresenter(Context context, GoogleApiClient googleApiClient) {
        return new UserInfoHeaderPresenter(getObtainUserAction(context), getLogoutAction(context), getAnalyticsTracker(), getCrashReportingTool(), googleApiClient);
    }
}
