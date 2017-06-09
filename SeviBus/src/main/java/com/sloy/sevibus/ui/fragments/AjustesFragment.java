package com.sloy.sevibus.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.sloy.sevibus.R;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.resources.sync.UpdateDatabaseAction;
import com.sloy.sevibus.resources.sync.UpdateDatabaseService;
import com.sloy.sevibus.ui.activities.AcercaDeActivity;
import com.sloy.sevibus.ui.activities.ContactoActivity;

import java.text.DateFormat;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AjustesFragment extends PreferenceFragment {

  public static final String PREFS_CONFIG_VALUES = "com.sloy.sevibus_preferences";

  private Preference actualizarManual;
  private UpdateDatabaseAction updateDatabaseAction;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);

    updateDatabaseAction = StuffProvider.getUpdateDatabaseAction(getActivity());

    addPreferencesFromResource(R.xml.preferences);
    if (Debug.isDebugEnabled(getActivity())) {
      addPreferencesFromResource(R.xml.preferences_debug);
    }

    // Datos de paradas y líneas
    actualizarManual = findPreference("pref_update_manual");
    assert actualizarManual != null;
    actualizarManual.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override public boolean onPreferenceClick(Preference preference) {
        updateDatabaseAction.update()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aVoid -> {},
            error -> {
              if (isAdded() && getView() != null) {
                Snackbar.make(getView(), "Algo fue mal :(", Snackbar.LENGTH_LONG).show();
              }
            },
            ()->{
              if (isAdded() && getView() != null) {
                Snackbar.make(getView(), "Datos actualizados! :)", Snackbar.LENGTH_LONG).show();
                actualizaInterfaz();
              }
            }
          );
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
}
