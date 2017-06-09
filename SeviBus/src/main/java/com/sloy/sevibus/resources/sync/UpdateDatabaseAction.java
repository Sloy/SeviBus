package com.sloy.sevibus.resources.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.sloy.sevibus.BuildConfig;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.resources.datasource.StringDownloader;
import java.io.IOException;
import java.sql.SQLException;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;

public class UpdateDatabaseAction {

    public static final String URL_INFO = "https://raw.githubusercontent.com/Sloy/sevibus-data/master/info.json";
    public static final Long PRELOADED_DATABASE_VERSION = 201706090921L;

    private final Context context;
    private final DBHelper dbHelper;
    private final StringDownloader stringDownloader;

    private JSONObject dataLocation;
    private SharedPreferences datosPreferences;
    private JSONObject serverDataInfo;

    public UpdateDatabaseAction(Context context, DBHelper dbHelper, StringDownloader stringDownloader) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.stringDownloader = stringDownloader;
        this.datosPreferences = context.getSharedPreferences("datos", Context.MODE_MULTI_PROCESS);
    }

    public Observable<Void> update() {
        return Observable.create(subscriber -> {
            try {
                if (BuildConfig.FLAVOR.equals("sevilla")) {
                    updateData();
                }
                subscriber.onCompleted();
            } catch (Exception e) {
                Log.e("UpdateDatabaseAction", "Failed trying to update database", e);
                subscriber.onError(e);
            }
        });
    }

    private void updateData() throws JSONException, IOException {
        serverDataInfo = getDataInfo();

        if (hasNewerData()) {
            dataLocation = checkNotNull(serverDataInfo.getJSONObject("data_location"));

            String sqlParadas = downloadSqlFor("paradas");
            String sqlLineas = downloadSqlFor("lineas");
            String sqlRelaciones = downloadSqlFor("relaciones");
            String sqlSecciones = downloadSqlFor("secciones");
            String sqlTipoLineas = downloadSqlFor("tipolineas");

            try {
                dbHelper.updateManual(sqlParadas, sqlLineas, sqlSecciones, sqlRelaciones, sqlTipoLineas);
                storeNewDataVersion();
                storeUpdatedTimestamp();
            } catch (SQLException e) {
                dbHelper.updateFromAssets();
            }
        } else {
            storeUpdatedTimestamp();
        }
    }

    private boolean hasNewerData() throws JSONException {
        long serverDataVersion = serverDataInfo.getLong("data_version");
        return serverDataVersion > getCurrentDataVersion();
    }

    private long getCurrentDataVersion() {
        return datosPreferences.getLong("data_version", UpdateDatabaseAction.PRELOADED_DATABASE_VERSION);
    }

    private void storeUpdatedTimestamp() {
        datosPreferences.edit().putLong("fecha_actualizacion", System.currentTimeMillis()).apply();
    }

    private String downloadSqlFor(String table) throws JSONException, IOException {
        return stringDownloader.download(dataLocation.getString(table));
    }

    private void storeNewDataVersion() throws JSONException {
        datosPreferences.edit().putLong("data_version", serverDataInfo.getLong("data_version")).apply();
    }

    private JSONObject getDataInfo() throws IOException, JSONException {
        return new JSONObject(stringDownloader.download(URL_INFO));
    }


}
