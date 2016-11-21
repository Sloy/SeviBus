package com.sloy.sevibus.resources;

import android.os.Handler;

import android.util.Log;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import com.sloy.sevibus.BuildConfig;
import com.sloy.sevibus.R;
import java.util.concurrent.TimeUnit;


public class FirebaseRemoteConfiguration implements RemoteConfiguration {

    private static final long FIREBASE_CACHE_TTL_SECONDS = BuildConfig.DEBUG ? 0 : TimeUnit.DAYS.toSeconds(1);
    public static final String DEFAULT_INDICATOR = "default";

    private final FirebaseRemoteConfig firebaseRemoteConfig;

    public FirebaseRemoteConfiguration(FirebaseRemoteConfig firebaseRemoteConfig) {
        this.firebaseRemoteConfig = firebaseRemoteConfig;
    }

    @Override
    public void init() {
        firebaseRemoteConfig.setDefaults(R.xml.firebase_remote_config_defaults);
        if (BuildConfig.DEBUG) {
            enableDeveloperMode();
        }
    }

    private void enableDeveloperMode() {
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);
    }

    @Override
    public void update() {
        new Handler().postDelayed(() ->
                firebaseRemoteConfig.fetch(FIREBASE_CACHE_TTL_SECONDS)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.i("Sevibus", "Firebase fetch succeeded");
                                firebaseRemoteConfig.activateFetched();
                            } else {
                                Log.w("Sevibus", "Firebase fetch failed", task.getException());
                            }
                        }), 0);
    }

    @Override
    public String getString(String key, String defaultValue) {
        String value = firebaseRemoteConfig.getString(key);
        Log.d("Sevibus",String.format("Firebase value: %s", value));
        if (DEFAULT_INDICATOR.equals(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }

    @Override
    public boolean isLoginSuggestionEnabled() {
        boolean feature_login_suggestion_enabled = firebaseRemoteConfig.getBoolean("feature_login_suggestion_enabled");
        return feature_login_suggestion_enabled;
    }
}
