package com.sloy.sevibus.resources;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.sloy.sevibus.BuildConfig;

public class Debug {

    private static final double FAKE_LATITUDE = 37.401994d;
    private static final double FAKE_LONGITUDE = -5.9841191d;

    public static final String FAKE_LOCATION_KEY = "pref_ubicacion_falsa";
    public static final String REPORTS_KEY = "pref_reports";

    public static boolean isDebugEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("debug", Context.MODE_MULTI_PROCESS);
        return BuildConfig.DEBUG || prefs.getBoolean("is_debug", false);
    }

    public static void setDebugEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences("debug", Context.MODE_MULTI_PROCESS);
        prefs.edit().putBoolean("is_debug", enabled).commit();
    }

    public static void useFakeLocation(Context context, Location location) {
        boolean forceFakeLocation = context.getSharedPreferences("debug", Context.MODE_MULTI_PROCESS).getBoolean(FAKE_LOCATION_KEY, false);
        if (BuildConfig.FLAVOR.equals("sevilla") && forceFakeLocation && isDebugEnabled(context)) {
            Log.w("Sevibus debug", "Usando ubicación falsa");
            location.setLatitude(FAKE_LATITUDE);
            location.setLongitude(FAKE_LONGITUDE);
        }
    }

    public static void setUseFakeLocationProvider(Context context, GoogleMap map) {
        boolean forceFakeLocation = context.getSharedPreferences("debug", Context.MODE_MULTI_PROCESS).getBoolean(FAKE_LOCATION_KEY, false);
        if (BuildConfig.FLAVOR.equals("sevilla") && forceFakeLocation && isDebugEnabled(context)) {
            map.setLocationSource(new Debug.FakeLocationSource());
        }
    }

    public static void registerHandledException(Throwable e) {
        StuffProvider.getCrashReportingTool().regiterHandledException(e);
        Log.e("Debug", "Handled Exception", e);
    }

    public static class FakeLocationSource implements LocationSource{

        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            Location fakeLocation = new Location("fake");
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

}
