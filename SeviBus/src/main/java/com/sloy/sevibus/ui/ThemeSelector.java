package com.sloy.sevibus.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sloy.sevibus.R;

public class ThemeSelector {
    public static final String USE_V_THEME = "use_v_theme";

    private static ThemeSelector instance;

    private SharedPreferences preferences;

    public ThemeSelector(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static ThemeSelector getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeSelector(context);
        }
        return instance;
    }

    public static void setV() {
        if (instance != null) {
            instance.setVInstance();
        }
    }

    public static void selectTheme(Context context) {
        getInstance(context).selectThemeInstance(context);
    }

    private void setVInstance() {
        boolean currentV = preferences.getBoolean(USE_V_THEME, false);
        preferences.edit().putBoolean(USE_V_THEME, !currentV).apply();
    }

    private void selectThemeInstance(Context context) {
        if (preferences.getBoolean(USE_V_THEME, false)) {
            selectV(context);
        } else {
            selectDefault(context);
        }
    }

    private void selectDefault(Context context) {
        /* no-op */
    }

    private void selectV(Context context) {
        context.setTheme(R.style.Theme_Sevibus_V);
    }
}
