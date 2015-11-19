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

    private MapContainerFragment mMapContainerFragment;
    private boolean mIsMapaAbierto = false;
    private View mContenido;
    private View mMapTrigger;
    private float distanciaCentros;
    private boolean liteMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        liteMode = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH || getActivity().getSharedPreferences(PreferenciasActivity.PREFS_CONFIG_VALUES, Context.MODE_PRIVATE).getBoolean(PreferenciasActivity.PREF_LITE_MODE_ENABLED, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(liteMode ? R.layout.fragment_main_home_lite : R.layout.fragment_main_home, container, false);
        mContenido = v.findViewById(R.id.fragment_main_contenido);

        if (!liteMode) {
            mMapTrigger = mContenido.findViewById(R.id.fragment_main_map_trigger);

            View.OnClickListener mapClickListener;
            ((PullToRefreshScrollView) mContenido).setScrollListener(new ScrollListener() { // Método añadido a la librería
                @Override
                public void onScroll(int value) {
                    // Bajar el mapa la mitad de lo que baja el scrollview
                    View v = mMapContainerFragment.getView();
                    int translationY = (-1 * value) / 2;
                    v.setTranslationY(translationY);
                }
            });

            mapClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abrirCerrarMapa(!mIsMapaAbierto);
                }
            };
            mMapTrigger.setOnClickListener(mapClickListener);

        }

        return v;
    }

    public void abrirCerrarMapa(final boolean abrirMapa) {
        final View mapView = mMapContainerFragment.getView();
        final ViewGroup.MarginLayoutParams mapaLayoutParams = (ViewGroup.MarginLayoutParams) mapView.getLayoutParams();
        mapaLayoutParams.height = mapView.getHeight();

        if (liteMode || Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return;
        }
        mIsMapaAbierto = abrirMapa;
        long mapaAnimDuration = 500;
        long contentAnimDuration = 500;

        // Abrir y cerrar el mapa
        float toYMapa = abrirMapa ? distanciaCentros : -1 * distanciaCentros;
        ObjectAnimator mapAnim = ObjectAnimator.ofFloat(mapView, "translationY", 0f, toYMapa);
        mapAnim.setDuration(mapaAnimDuration);
        mapAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (!abrirMapa) {
                    mMapContainerFragment.showMapControls(false);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mapView.setTranslationY(0f);
                mapaLayoutParams.topMargin = abrirMapa ? 0 : (int) (-1 * distanciaCentros);
                mapView.requestLayout(); // Sí, muy necesario xD
                if (abrirMapa) {
                    mMapContainerFragment.showMapControls(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mapAnim.start();

        // Abrir y cerrar contenido
        float contenidoAlturaVisible = mContenido.getHeight() - mMapTrigger.getHeight();
        final float fromYContenido = abrirMapa ? 0 : contenidoAlturaVisible;
        final float toYContenido = abrirMapa ? contenidoAlturaVisible : 0;
        ObjectAnimator contentAnim = ObjectAnimator.ofFloat(mContenido, "translationY", fromYContenido, toYContenido);
        contentAnim.setDuration(contentAnimDuration);
        contentAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (!abrirMapa) {
                    mContenido.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (abrirMapa) {
                    mContenido.setVisibility(View.GONE);
                    mContenido.setTranslationY(toYContenido);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        contentAnim.start();
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
        if (!liteMode) {
            setupMapa(fm, trans);
        }
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

    private void setupMapa(FragmentManager fm, FragmentTransaction trans) {
        Fragment f = fm.findFragmentByTag(FRAG_MAPA);
        if (f == null) {
            f = MapContainerFragment.getInstance(false);
        }
        if (f.isAdded()) {
            trans.attach(f);
        } else {
            trans.add(R.id.fragment_main_mapa, f, FRAG_MAPA);
        }

        mMapContainerFragment = (MapContainerFragment) f;

        // Listener para ajustar el mapa cuando las vistas estén creadas y se pueda medir el espacio
        final View yourView = getView().findViewById(R.id.fragment_main_mapa);
        yourView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once :
                yourView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                // Here you can get the size :)
                float fullCenterY = mContenido.getMeasuredHeight() / 2;
                float smallCenterY = mMapTrigger.getMeasuredHeight() / 2;
                distanciaCentros = fullCenterY - smallCenterY;
                abrirCerrarMapa(false);

            }
        });
    }

    public boolean onBackPressed() {
        if (mIsMapaAbierto) {
            abrirCerrarMapa(false);
            return true;
        } else {
            return false;
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
