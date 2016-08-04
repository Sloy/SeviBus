package com.sloy.sevibus.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.sloy.sevibus.R;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.resources.syncadapter.IntentFactory;
import com.sloy.sevibus.ui.fragments.AlertasFragment;
import com.sloy.sevibus.ui.fragments.BonobusListaFragment;
import com.sloy.sevibus.ui.fragments.FavoritasListFragment;
import com.sloy.sevibus.ui.fragments.InitialFragment;
import com.sloy.sevibus.ui.fragments.ListaLineasFragment;
import com.sloy.sevibus.ui.fragments.MainPageFragment;
import com.sloy.sevibus.ui.fragments.MapContainerFragment;
import com.sloy.sevibus.ui.fragments.MapaControllerFragment;
import com.sloy.sevibus.ui.mvp.view.LineasCercanasViewContainer;
import com.sloy.sevibus.ui.mvp.view.ParadasCercanasMainViewContainer;

public class HomeActivity extends LocationProviderActivity implements IMainController, InitialFragment.ApplicationReadyListener, ListaLineasFragment.LineaSelectedListener, ParadasCercanasMainViewContainer.ParadasCercanasMainClickListener, LineasCercanasViewContainer.LineasCercanasMainClickListener {


    public static final String EXTRA_DRAWER_ID = "drawer_position";
    public static final String EXTRA_CURRENT_TITLE = "mCurrentTitle";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private SharedPreferences mPrefs;
    private String mCurrentTitle;
    private int mCurrentDrawerId;
    private boolean arrancado = false;
    private boolean mIsRightDrawerAvailable;

    private boolean prefIsFirstTimeMap;
    public static final String PREF_FIRST_TIME_MAP = "map_1st";
    private NavigationView navigationView;
    private SparseArray<String> drawerFragments;

    @Override
    protected boolean needsToolbarDecorator() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (getDBHelper().getDaoParada().countOf() > 0) {
            arrancaNormal(savedInstanceState);
        } else {
            arrancaPrimeraVez();
        }

        StuffProvider.getRemoteConfiguration().update();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (arrancado) {
            int id = intent.getIntExtra(EXTRA_DRAWER_ID, R.id.nav_inicio);
            doSelectDrawerItemById(id);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_CURRENT_TITLE, mCurrentTitle);
        outState.putInt(EXTRA_DRAWER_ID, mCurrentDrawerId);
        super.onSaveInstanceState(outState);
    }

    private void arrancaNormal(Bundle savedInstanceState) {
        arrancado = true;
        setupNavigationDrawer();

        /*
         * Vía https://plus.google.com/+AndroidDevelopers/posts/3exHM3ZuCYM (Protip de Bruno Olivieira
         */
        if (savedInstanceState == null) {
            doShowMainFragment();
        } else {
            doUpdateTitle(savedInstanceState.getString(EXTRA_CURRENT_TITLE));
        }
    }

    private void arrancaPrimeraVez() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_container, new InitialFragment());
        transaction.commit();
        doUpdateTitle(getString(R.string.titulo_inicial));
    }

    @Override
    public void onApplicationReady() {
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        arrancaNormal(null);
        mDrawerToggle.syncState();
    }

    private void setupNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, getToolbar(), R.string.drawer_open, R.string.drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        doSelectDrawerItemById(menuItem.getItemId());
                        return true;
                    }
                });

        drawerFragments = new SparseArray<>();
        drawerFragments.append(R.id.nav_inicio, MainPageFragment.class.getName());
        drawerFragments.append(R.id.nav_favoritas, FavoritasListFragment.class.getName());
        drawerFragments.append(R.id.nav_mapa, MapContainerFragment.class.getName());
        drawerFragments.append(R.id.nav_lineas, ListaLineasFragment.class.getName());
        drawerFragments.append(R.id.nav_bonobus, BonobusListaFragment.class.getName());
        drawerFragments.append(R.id.nav_alertas, AlertasFragment.class.getName());

        prefIsFirstTimeMap = mPrefs.getBoolean(PREF_FIRST_TIME_MAP, true);
        lockMapOptions(true);
    }

    private void doSelectDrawerItemById(int itemId) {
        switch (itemId) {
            case R.id.menu_compartir:
                startActivity(IntentFactory.shareApp());
                return;
            case R.id.menu_acerca:
                startActivity(new Intent(this, AcercaDeActivity.class));
                return;
            case R.id.menu_ajustes:
                startActivity(new Intent(this, PreferenciasActivity.class));
                return;
        }

        doReplaceFragment(drawerFragments.get(itemId), null);
        if (itemId != R.id.nav_inicio) {
            doUpdateTitle(navigationView.getMenu().findItem(itemId).getTitle().toString());
        } else {
            doUpdateTitle("SeviBus");
        }
        navigationView.getMenu().findItem(itemId).setChecked(true);
        mCurrentDrawerId = itemId;
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    private void doShowMainFragment() {
        doSelectDrawerItemById(R.id.nav_inicio);
    }

    private void doUpdateTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        mCurrentTitle = title;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (arrancado) {
            getMenuInflater().inflate(R.menu.home, menu);
            menu.findItem(R.id.menu_mapa_abrir).setVisible(mIsRightDrawerAvailable);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_mapa_abrir:
                toggleMapOptions();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (f instanceof InitialFragment) {
            super.onBackPressed();
        } else if (f instanceof MainPageFragment) {
            super.onBackPressed();
        } else {
            doShowMainFragment();
        }
    }

    private void doReplaceFragment(String fname, Bundle extras) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (current != null) {
            transaction.detach(current);
        }

        Fragment f = getSupportFragmentManager().findFragmentByTag(fname);
        if (f == null) {
            f = Fragment.instantiate(this, fname, extras);
            transaction.add(R.id.main_container, f, fname);
        } else {
            transaction.attach(f);
        }
        try {
            transaction.commit();
        } catch (IllegalStateException e) {
            Log.w("SeviBus", "Oops, la aplicación se cerró antes terminar la carga inicial. No pasa nada, cuando la abras estará bien.");
        }
    }

    public MapaControllerFragment getMapOptions() {
        return (MapaControllerFragment) getSupportFragmentManager().findFragmentById(R.id.right_drawer);
    }

    public void toggleMapOptions() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.END);
        }
    }

    public void lockMapOptions(boolean lock) {
        if (lock) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            if (prefIsFirstTimeMap) {
                prefIsFirstTimeMap = false;
                toggleMapOptions();
                mPrefs.edit().putBoolean(PREF_FIRST_TIME_MAP, false).commit();
                Snackbar.make(getContainerView(), "A la derecha tienes las opciones del mapa, ¿ves?", Snackbar.LENGTH_LONG).show();
            }
        }
        mIsRightDrawerAvailable = !lock;
        supportInvalidateOptionsMenu();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onLineaSelecteded(Linea linea) {
        startActivity(ParadasDeLineaActivity.getIntent(this, linea.getId()));
    }

    @Override
    public void onParadaCercanaClick(int idParada) {
        startActivity(ParadaInfoActivity.getIntent(this, idParada));
    }

    @Override
    public void onParadaCercanaMas() {
        doSelectDrawerItemById(R.id.nav_mapa);
    }

    @Override
    public void onLineaCercanaClick(int idLinea) {
        startActivity(ParadasDeLineaActivity.getIntent(this, idLinea));
    }

    @Override
    public void onLineaCercanaMas() {
        doSelectDrawerItemById(R.id.nav_lineas);
    }

    @Override
    public void abrirFavoritas() {
        doSelectDrawerItemById(R.id.nav_favoritas);
    }
}
