package com.sloy.sevibus.resources.syncadapter;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.resources.Debug;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import org.json.JSONException;
import org.json.JSONObject;

public class SyncAdapter extends AbstractThreadedSyncAdapter {


    private DBHelper mHelper;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mHelper = OpenHelperManager.getHelper(context, DBHelper.class);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mHelper = OpenHelperManager.getHelper(context, DBHelper.class);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(SyncUtils.TAG, "Beginning network synchronization");
        try {
            updateLineasYParadas(syncResult);

            getContext().sendBroadcast(new Intent(SyncUtils.ACTION_UPDATE_FINISH));
        } catch (MalformedURLException e) {
            Log.wtf(SyncUtils.TAG, "Feed URL is malformed", e);
            syncResult.stats.numParseExceptions++;
            return;
        } catch (IOException e) {
            Log.e(SyncUtils.TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
        } catch (JSONException e) {
            Log.e(SyncUtils.TAG, "Error parsing json: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
        }
        Log.i(SyncUtils.TAG, "Network synchronization complete");
    }

    private void updateLineasYParadas(SyncResult syncResult) throws IOException, JSONException {
        Log.i(SyncUtils.TAG, "Comenzando sincronización de líneas y paradas");
        // 1. Obtiene la información de los datos (el json con la versión, las urls y tal)
        final URL location = new URL(SyncUtils.URL_INFO);
        InputStream dataStream = SyncUtils.downloadUrl(location);
        JSONObject data = new JSONObject(SyncUtils.streamToString(dataStream));
        int dataVersion = data.getInt("data_version");
        SharedPreferences prefsDatos = getContext().getSharedPreferences("datos", Context.MODE_MULTI_PROCESS);
        int currentDataVersion = prefsDatos.getInt("data_version", getContext().getResources().getInteger(R.integer.data_version_assets));
        if (dataVersion > currentDataVersion) {
            // 2. Obtiene las URLs de los scripts SQL con los datos
            JSONObject dataLocation = data.getJSONObject("data_location");
            URL urlParadas = new URL(dataLocation.getString("paradas"));
            URL urlLineas = new URL(dataLocation.getString("lineas"));
            URL urlRelaciones = new URL(dataLocation.getString("relaciones"));
            URL urlSecciones = new URL(dataLocation.getString("secciones"));
            URL urlTipoLineas = new URL(dataLocation.getString("tipolineas"));

            // 3. Descarga los scripts SQL de cada tabla, desde las URLs obtenidas
            String sqlParadas = SyncUtils.streamToString(SyncUtils.downloadUrl(urlParadas));
            String sqlLineas = SyncUtils.streamToString(SyncUtils.downloadUrl(urlLineas));
            String sqlRelaciones = SyncUtils.streamToString(SyncUtils.downloadUrl(urlRelaciones));
            String sqlSecciones = SyncUtils.streamToString(SyncUtils.downloadUrl(urlSecciones));
            String sqlTipoLineas = SyncUtils.streamToString(SyncUtils.downloadUrl(urlTipoLineas));

            // 4. Actualiza la Base de Datos con esta información. Buena suerte, chaval xD
            try {
                mHelper.updateManual(sqlParadas, sqlLineas, sqlSecciones, sqlRelaciones, sqlTipoLineas);
                prefsDatos.edit().putInt("data_version", dataVersion).commit();
            } catch (SQLException e) {
                Log.e(SyncUtils.TAG, "Error actualizando los datos en la BBDD", e);
                Debug.registerHandledException(getContext(), e);
                mHelper.updateFromAssets();
            }
            Log.i(SyncUtils.TAG, "Terminada sincronización de líneas y paradas");
        } else {
            Log.i(SyncUtils.TAG, "Los datos están al día, no necesita actualización");
        }
        prefsDatos.edit().putLong("fecha_actualizacion", System.currentTimeMillis()).commit();
    }



}