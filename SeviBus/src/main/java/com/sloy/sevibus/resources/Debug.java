package com.sloy.sevibus.resources;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.sloy.sevibus.BuildConfig;

import java.io.IOException;
import java.io.RandomAccessFile;

import io.fabric.sdk.android.Fabric;

/**
 * Created by rafa on 02/09/13.
 */
public class Debug {

    private static final double FAKE_LATITUDE = 37.401994d;
    private static final double FAKE_LONGITUDE = -5.9841191d;

    public static final String FAKE_LOCATION_KEY = "pref_ubicacion_falsa";
    public static final String REPORTS_KEY = "pref_reports";

    public static final int LITE_MODE_RAM_THRESHOLD = 450;

    public static boolean isDebugEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("debug", Context.MODE_MULTI_PROCESS);
        return BuildConfig.DEBUG || prefs.getBoolean("is_debug", false);
    }

    public static void setDebugEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences("debug", Context.MODE_MULTI_PROCESS);
        prefs.edit().putBoolean("is_debug", enabled).commit();
    }

    public static void activateReports(Context context) {
        boolean doReports = context.getSharedPreferences("debug", Context.MODE_MULTI_PROCESS).getBoolean(REPORTS_KEY, false);
        boolean isDebug = isDebugEnabled(context);
        if (!isDebug || (isDebug && doReports)) {
            Fabric.with(context, new Crashlytics());
        } else {
            Log.d("Sevibus debug", "Evitando reporte de errores");
        }
    }

    public static void disableAnalyticsOnDebug(Context context) {
        boolean doReports = context.getSharedPreferences("debug", Context.MODE_MULTI_PROCESS).getBoolean(REPORTS_KEY, false);
        boolean isDebug = isDebugEnabled(context);
    }

    public static void useFakeLocation(Context context, Location location) {
        boolean forceFakeLocation = context.getSharedPreferences("debug", Context.MODE_MULTI_PROCESS).getBoolean(FAKE_LOCATION_KEY, false);
        if (forceFakeLocation && isDebugEnabled(context)) {
            Log.w("Sevibus debug", "Usando ubicación falsa");
            location.setLatitude(FAKE_LATITUDE);
            location.setLongitude(FAKE_LONGITUDE);
        }
    }

    public static void setUseFakeLocationProvider(Context context, GoogleMap map) {
        boolean forceFakeLocation = context.getSharedPreferences("debug", Context.MODE_MULTI_PROCESS).getBoolean(FAKE_LOCATION_KEY, false);
        if (forceFakeLocation && isDebugEnabled(context)) {
            map.setLocationSource(new Debug.FakeLocationSource());
        }
    }

    public static void registerHandledException(Context context, Exception e){
        // Crashlytics
        Crashlytics.logException(e);
    }

    public static class FakeLocationSource implements LocationSource{

        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            Location fakeLocation = new Location("gps");
            fakeLocation.setLatitude(FAKE_LATITUDE);
            fakeLocation.setLongitude(FAKE_LONGITUDE);
            fakeLocation.setAccuracy(100f);
            fakeLocation.setTime(System.currentTimeMillis());
            onLocationChangedListener.onLocationChanged(fakeLocation);
        }
        @Override
        public void deactivate() {
            // Evil laugh
        }
    }

    public static synchronized int readTotalRam() {
        int tm = 1000;
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r");
            String load = reader.readLine();
            String[] totrm = load.split(" kB");
            String[] trm = totrm[0].split(" ");
            tm = Integer.parseInt(trm[trm.length - 1]);
            tm = Math.round(tm / 1024);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return tm;
    }
}
