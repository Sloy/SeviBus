package com.sloy.sevibus.resources.sync;

import android.content.Context;
import android.content.SharedPreferences;

import com.sloy.sevibus.BuildConfig;
import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.resources.datasource.StringDownloader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

import rx.Observable;
import rx.Subscriber;

import static com.google.common.base.Preconditions.checkNotNull;

public class UpdateDatabaseAction {

    public static final String URL_INFO = "https://dl.dropboxusercontent.com/u/1587994/SeviBus%20Data/info.json";

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
        int serverDataVersion = serverDataInfo.getInt("data_version");
        return serverDataVersion > getCurrentDataVersion();
    }

    private int getCurrentDataVersion() {
        return datosPreferences.getInt("data_version", context.getResources().getInteger(R.integer.data_version_assets));
    }

    private void storeUpdatedTimestamp() {
        datosPreferences.edit().putLong("fecha_actualizacion", System.currentTimeMillis()).apply();
    }

    private String downloadSqlFor(String table) throws JSONException, IOException {
        return stringDownloader.download(dataLocation.getString(table));
    }

    private void storeNewDataVersion() throws JSONException {
        datosPreferences.edit().putInt("data_version", serverDataInfo.getInt("data_version")).apply();
    }

    private JSONObject getDataInfo() throws IOException, JSONException {
        return new JSONObject(stringDownloader.download(URL_INFO));
    }


}
