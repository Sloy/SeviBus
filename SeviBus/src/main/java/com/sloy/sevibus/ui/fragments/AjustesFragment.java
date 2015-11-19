package com.sloy.sevibus.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;
import com.sloy.sevibus.R;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.resources.syncadapter.SyncUtils;
import com.sloy.sevibus.ui.activities.AcercaDeActivity;
import com.sloy.sevibus.ui.activities.ContactoActivity;
import java.text.DateFormat;

public class AjustesFragment extends PreferenceFragment {

  public static final String PREFS_CONFIG_VALUES = "com.sloy.sevibus_preferences";

  private Preference actualizarManual;
  private BroadcastReceiver mUpdateFinishReceiver;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);

    mUpdateFinishReceiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        actualizaInterfaz();
      }
    };

    addPreferencesFromResource(R.xml.preferences);
    if (Debug.isDebugEnabled(getActivity())) {
      addPreferencesFromResource(R.xml.preferences_debug);
    }

    // Interfaz

    CheckBoxPreference liteMode = (CheckBoxPreference) findPreference("pref_lite_mode");
    liteMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
        Boolean liteMode = (Boolean) newValue;
        getActivity().getSharedPreferences(PREFS_CONFIG_VALUES, Activity.MODE_PRIVATE)
            .edit()
            .putBoolean("pref_lite_mode", liteMode)
            .commit();

        Toast.makeText(getActivity(), "Puede que necesites reiniciar la aplicación",
            Toast.LENGTH_SHORT).show();
        return true;
      }
    });

    CheckBoxPreference alertas = (CheckBoxPreference) findPreference("pref_alertas");
    alertas.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
        Boolean activar = (Boolean) newValue;
        getActivity().getSharedPreferences(PREFS_CONFIG_VALUES, Activity.MODE_PRIVATE)
            .edit()
            .putBoolean("pref_alertas", activar)
            .commit();
        return true;
      }
    });

    // Datos de paradas y líneas

    CheckBoxPreference actualizarAutomatica =
        (CheckBoxPreference) findPreference("pref_update_auto");
    assert actualizarAutomatica != null;
    actualizarAutomatica.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
        Boolean updateAuto = (Boolean) newValue;
        getActivity().getSharedPreferences("sync", Context.MODE_MULTI_PROCESS)
            .edit()
            .putBoolean("pref_update_auto", updateAuto)
            .commit();
        SyncUtils.setSyncAutomatically(updateAuto);
        return true;
      }
    });

    actualizarManual = findPreference("pref_update_manual");
    assert actualizarManual != null;
    actualizarManual.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override public boolean onPreferenceClick(Preference preference) {
        Toast.makeText(getActivity(), "Vale vale, ahora ya yo actualizo", Toast.LENGTH_SHORT)
            .show();
        SyncUtils.triggerRefresh();
        return true;
      }
    });
    actualizaInterfaz();

    // Información
    Preference preguntasFrecuentes = findPreference("pref_faq");
    assert preguntasFrecuentes != null;
    preguntasFrecuentes.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override public boolean onPreferenceClick(Preference preference) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_faq)));
        startActivity(intent);
        return true;
      }
    });

    Preference contacto = findPreference("pref_contacto");
    assert contacto != null;
    contacto.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override public boolean onPreferenceClick(Preference preference) {
        startActivity(new Intent(getActivity(), ContactoActivity.class));
        return true;
      }
    });

    Preference acerca = findPreference("pref_acerca");
    assert acerca != null;
    acerca.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override public boolean onPreferenceClick(Preference preference) {
        startActivity(new Intent(getActivity(), AcercaDeActivity.class));
        return true;
      }
    });

    // De-bug
    CheckBoxPreference ubicacionFalsa = (CheckBoxPreference) findPreference("pref_ubicacion_falsa");
    if (ubicacionFalsa != null) {
      ubicacionFalsa.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
          // Cambio el valor, porque el que realmente uso está en otro archivo distinto
          getActivity().getSharedPreferences("debug", Context.MODE_MULTI_PROCESS).edit()
              .putBoolean(Debug.FAKE_LOCATION_KEY, (Boolean) newValue)
              .commit();
          Toast.makeText(getActivity(), "Puede que necesites reiniciar la aplicación",
              Toast.LENGTH_SHORT).show();
          return true;
        }
      });
    }

    CheckBoxPreference enviarReportes = (CheckBoxPreference) findPreference("pref_reports");
    if (enviarReportes != null) {
      enviarReportes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
          // Cambio el valor, porque el que realmente uso está en otro archivo distinto
          getActivity().getSharedPreferences("debug", Context.MODE_MULTI_PROCESS).edit()
              .putBoolean(Debug.REPORTS_KEY, (Boolean) newValue)
              .commit();
          Toast.makeText(getActivity(), "Puede que necesites reiniciar la aplicación",
              Toast.LENGTH_SHORT).show();
          return true;
        }
      });
    }

    Preference salirDebug = findPreference("pref_salir_debug");
    if (salirDebug != null) {
      salirDebug.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override public boolean onPreferenceClick(Preference preference) {
          Debug.setDebugEnabled(getActivity(), false);
          Toast.makeText(getActivity(), "Seguramente debas reiniciar la aplicación",
              Toast.LENGTH_SHORT).show();
          return true;
        }
      });
    }
  }

  private void actualizaInterfaz() {
    // Botón de actualización manual:
    long lastUpdate =
        getActivity().getSharedPreferences("datos", Context.MODE_MULTI_PROCESS).getLong(
            "fecha_actualizacion", 0L);
    CharSequence lastUpdateText = lastUpdate == 0L ? "Nunca"
        : DateUtils.formatSameDayTime(lastUpdate, System.currentTimeMillis(), DateFormat.DEFAULT,
            DateFormat.SHORT);
    String summary = "Última actualización: " + lastUpdateText;
    actualizarManual.setSummary(summary);
    Log.d("SeviBus prefs", "Last update: " + lastUpdateText);
  }

  @Override public void onResume() {
    super.onResume();
    getActivity().registerReceiver(mUpdateFinishReceiver, new IntentFilter(SyncUtils.ACTION_UPDATE_FINISH));
  }

  @Override public void onPause() {
    super.onPause();
    getActivity().unregisterReceiver(mUpdateFinishReceiver);
  }
}
