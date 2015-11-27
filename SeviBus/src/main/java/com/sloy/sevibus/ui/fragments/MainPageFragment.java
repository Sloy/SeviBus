package com.sloy.sevibus.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sloy.sevibus.BuildConfig;
import com.sloy.sevibus.R;
import com.sloy.sevibus.ui.activities.PreferenciasActivity;
import com.sloy.sevibus.ui.fragments.main.FavoritasMainFragment;
import com.sloy.sevibus.ui.fragments.main.LineasCercanasMainFragment;
import com.sloy.sevibus.ui.fragments.main.ParadasCercanasMainFragment;

import de.cketti.library.changelog.ChangeLog;

public class MainPageFragment extends BaseDBFragment {

    private static final String FRAG_FAVORITAS = "f_favoritas";
    private static final String FRAG_PARADAS_CERCANAS = "f_p_cercanas";
    private static final String FRAG_LINEAS_CERCANAS = "f_l_cercanas";

    private static final String PREF_SHOW_NEW_VERSION_LATEST_SEEN = "newversion_last_seen";
    private static final int NEW_VERSION_SNACKBAR_DURATION = 10000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_home, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupUI();
    }

    private void setupUI() {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction trans = fm.beginTransaction();
        setupFavoritas(fm, trans);
        setupParadasCercanas(fm, trans);
        setupLineasCercanas(fm, trans);
        trans.commit();
        setupNewVersion();
    }

    private void setupNewVersion() {
        final SharedPreferences prefs = getActivity().getSharedPreferences(PreferenciasActivity.PREFS_CONFIG_VALUES, Context.MODE_PRIVATE);
        int lastVersion = prefs.getInt(PREF_SHOW_NEW_VERSION_LATEST_SEEN, 0);
        int currentVersion = BuildConfig.VERSION_CODE;

        if (lastVersion < currentVersion) {
            new Handler().postDelayed(() -> {
                //noinspection ResourceType
                Snackbar.make(getView(), "SeviBus se ha actualizado!", NEW_VERSION_SNACKBAR_DURATION)
                  .setAction("Ver cambios", v -> {
                      ChangeLog cl = new ChangeLog(getActivity());
                      cl.getFullLogDialog().show();
                  })
                  .show();
            }, 1000);
        }
        
        prefs.edit().putInt(PREF_SHOW_NEW_VERSION_LATEST_SEEN, currentVersion).apply();
    }

    private void setupFavoritas(FragmentManager fm, FragmentTransaction trans) {
        Fragment f = fm.findFragmentByTag(FRAG_FAVORITAS);
        if (f == null) {
            f = FavoritasMainFragment.getInstance();
        }
        if (f.isAdded()) {
            trans.attach(f);
        } else {
            trans.add(R.id.fragment_main_favoritas, f, FRAG_FAVORITAS);
        }
    }

    private void setupParadasCercanas(FragmentManager fm, FragmentTransaction trans) {
        Fragment f = fm.findFragmentByTag(FRAG_PARADAS_CERCANAS);
        if (f == null) {
            f = ParadasCercanasMainFragment.getInstance();
        }
        ((ParadasCercanasMainFragment) f).setMainPage(this);
        if (f.isAdded()) {
            trans.attach(f);
        } else {
            trans.add(R.id.fragment_main_paradas_cercanas, f, FRAG_PARADAS_CERCANAS);
        }
    }

    private void setupLineasCercanas(FragmentManager fm, FragmentTransaction trans) {
        Fragment f = fm.findFragmentByTag(FRAG_LINEAS_CERCANAS);
        if (f == null) {
            f = LineasCercanasMainFragment.getInstance();
        }
        if (f.isAdded()) {
            trans.attach(f);
        } else {
            trans.add(R.id.fragment_main_lineas_cercanas, f, FRAG_LINEAS_CERCANAS);
        }
    }

}
