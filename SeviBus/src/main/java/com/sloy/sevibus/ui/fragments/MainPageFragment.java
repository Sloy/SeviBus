package com.sloy.sevibus.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;

import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.handmark.pulltorefresh.library.ScrollListener;
import com.sloy.sevibus.R;
import com.sloy.sevibus.resources.syncadapter.IntentFactory;
import com.sloy.sevibus.ui.activities.PreferenciasActivity;
import com.sloy.sevibus.ui.fragments.main.FavoritasMainFragment;
import com.sloy.sevibus.ui.fragments.main.LineasCercanasMainFragment;
import com.sloy.sevibus.ui.fragments.main.NewVersionMainFragment;
import com.sloy.sevibus.ui.fragments.main.ParadasCercanasMainFragment;

public class MainPageFragment extends BaseDBFragment {

    private static final String FRAG_FAVORITAS = "f_favoritas";
    private static final String FRAG_PARADAS_CERCANAS = "f_p_cercanas";
    private static final String FRAG_LINEAS_CERCANAS = "f_l_cercanas";
    private static final String FRAG_MAPA = "f_mapa";
    private static final String FRAG_FIRST_RUN = "f_firstrun";
    private static final String FRAG_NEW_VERSION = "f_newversion";


    private static final String PREF_SHOW_FIRST_FUN_INFO = "show_firstrun";
    private static final String PREF_SHOW_NEW_VERSION_LATEST_SEEN = "newversion_last_seen";
    private static final String PREF_SHOW_NEW_VERSION = "newversion_show";

    private static final String PREF_LAUNCHES = "launches";
    private static final String PREF_COMPARTIR_DISCARDED = "compartir_discarded";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_home_lite, container, false);
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
        setupNewVersion(fm, trans);
        setupParadasCercanas(fm, trans);
        setupLineasCercanas(fm, trans);
        setupCompartir();
        trans.commit();
    }

    private void setupNewVersion(FragmentManager fm, FragmentTransaction trans) {
        int currentVersion = 0;
        try {
            currentVersion = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        final SharedPreferences prefs = getActivity().getSharedPreferences(PreferenciasActivity.PREFS_CONFIG_VALUES, Context.MODE_PRIVATE);
        int lastVersion = prefs.getInt(PREF_SHOW_NEW_VERSION_LATEST_SEEN, 0);

        if (prefs.getBoolean(PREF_SHOW_NEW_VERSION, true) && lastVersion < currentVersion && lastVersion > 0) {
            Fragment f = fm.findFragmentByTag(FRAG_NEW_VERSION);
            if (f == null) {
                f = NewVersionMainFragment.getInstance();
            }
            ((NewVersionMainFragment) f).setMainPage(this);
            if (f.isAdded()) {
                trans.attach(f);
            } else {
                trans.add(R.id.fragment_main_newversion, f, FRAG_NEW_VERSION);
            }
        } else if (lastVersion == 0) {
            prefs.edit().putInt(PREF_SHOW_NEW_VERSION_LATEST_SEEN, currentVersion).apply();
        }
    }

    private void setupCompartir() {
        final SharedPreferences prefs = getActivity().getSharedPreferences(PreferenciasActivity.PREFS_CONFIG_VALUES, Context.MODE_PRIVATE);
        boolean compartirDiscarded = prefs.getBoolean(PREF_COMPARTIR_DISCARDED, false);
        int launches = prefs.getInt(PREF_LAUNCHES, 0);
        prefs.edit().putInt(PREF_LAUNCHES, launches + 1).apply();

        if (!compartirDiscarded && launches > 5) {
            ViewStub stubWarningLite = (ViewStub) getView().findViewById(R.id.fragment_main_share_stub);
            if (stubWarningLite == null) {
                return;
            }
            stubWarningLite.inflate();
            final View warningLiteView = getView().findViewById(R.id.fragment_main_compartir);
            View botonDescartar = warningLiteView.findViewById(R.id.main_compartir_descartar);
            botonDescartar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    warningLiteView.setVisibility(View.GONE);
                    prefs.edit().putBoolean(PREF_COMPARTIR_DISCARDED, true).apply();
                }
            });
            View botonAceptar = warningLiteView.findViewById(R.id.main_compartir_aceptar);
            botonAceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(IntentFactory.shareApp());
                    warningLiteView.setVisibility(View.GONE);
                    prefs.edit().putBoolean(PREF_COMPARTIR_DISCARDED, true).apply();
                }
            });
        }
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

    public void dismissNewVersionCard(final boolean foreverAndEver) {
        final Fragment f = getChildFragmentManager().findFragmentByTag(FRAG_NEW_VERSION);
        final View card = f.getView();

        int currentVersion = 0;
        try {
            currentVersion = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        final SharedPreferences.Editor prefsEditor = getActivity().getSharedPreferences(PreferenciasActivity.PREFS_CONFIG_VALUES, Context.MODE_PRIVATE).edit();
        if (foreverAndEver) {
            prefsEditor.putBoolean(PREF_SHOW_NEW_VERSION, false); //Ojo: no commit
        }
        prefsEditor.putInt(PREF_SHOW_NEW_VERSION_LATEST_SEEN, currentVersion).apply();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            final ViewGroup.LayoutParams lp = card.getLayoutParams();
            final int cardHeight = card.getHeight();
            int cardWidth = card.getWidth();

            ObjectAnimator slideAnim = ObjectAnimator.ofFloat(card, "translationX", cardWidth);
            slideAnim.setDuration(500);
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(card, "alpha", 1f, 0f);
            alphaAnim.setDuration(500);

            ValueAnimator heightAnim = new ValueAnimator().ofInt(cardHeight, 0).setDuration(400);
            heightAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    lp.height = (Integer) animation.getAnimatedValue();
                    card.setLayoutParams(lp);
                }
            });
            heightAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // Reset view presentation
                    card.setAlpha(1f);
                    card.setTranslationY(0f);
                    lp.height = cardHeight;
                    card.setLayoutParams(lp);
                    //Quita el fragment
                    card.setVisibility(View.GONE);
                    getChildFragmentManager().beginTransaction().remove(f).commit();
                }
            });
            AnimatorSet dismissAnim = new AnimatorSet();
            dismissAnim.play(slideAnim).with(alphaAnim).before(heightAnim);
            dismissAnim.start();
        } else {
            card.setVisibility(View.GONE);
            getChildFragmentManager().beginTransaction().remove(f).commit();
        }
    }
}
