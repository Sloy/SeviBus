package com.sloy.sevibus.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.model.tussam.Seccion;
import com.sloy.sevibus.ui.fragments.ParadasFragment;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParadasDeLineaActivity extends BaseToolbarActivity {

    public static Intent getIntent(Context context, int id) {
        Intent i = new Intent(context, ParadasDeLineaActivity.class);
        i.putExtra("linea_id", id);
        return i;
    }

    private ViewPager mViewPager;
    private TabLayout tabLayout;

    private ActionBar mActionBar;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private Linea mLinea;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paradas_de_linea);

        mActionBar = getSupportActionBar();

        int lineaID = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lineaID = extras.getInt("linea_id");
        }

        if (lineaID < 1) {
            Log.e("SeviBus", "No se recibió línea");
            finish();
        }

        List<SeccionParadasPair> secciones = new ArrayList<SeccionParadasPair>();
        try {
            mLinea = DBQueries.getLineaById(getDBHelper(), lineaID);
            for (Seccion c : mLinea.getSecciones()) {
                List<Parada> paradasDeSeccion = DBQueries.getParadasDeSeccion(getDBHelper(), c.getId());
                secciones.add(new SeccionParadasPair(c, paradasDeSeccion));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mActionBar.setTitle(String.format("Línea %s", mLinea.getNumero()));

        setupFragments(secciones);
    }


    private void setupFragments(List<SeccionParadasPair> secciones) {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), secciones);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        if (mSectionsPagerAdapter.getCount() > 1) {
            tabLayout.setVisibility(View.VISIBLE);
        } else {
            tabLayout.setVisibility(View.GONE);
        }
    }

    private static class SeccionParadasPair {
        Seccion seccion;
        List<Parada> paradas;

        public SeccionParadasPair(Seccion s, List<Parada> ps) {
            seccion = s;
            paradas = ps;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private List<SeccionParadasPair> mSecciones;
        private ParadasFragment[] mFragments;

        public SectionsPagerAdapter(FragmentManager fm, List<SeccionParadasPair> secciones) {
            super(fm);
            mSecciones = secciones;
            mFragments = new ParadasFragment[getCount()];
        }

        @Override
        public ParadasFragment getItem(int position) {
            ParadasFragment fragment = mFragments[position];
            if (fragment == null) {
                fragment = new ParadasFragment(mSecciones.get(position).paradas);
                mFragments[position] = fragment;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return mSecciones.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "A " + mSecciones.get(position).seccion.getNombreSeccion();
        }
    }

    private DBHelper dbHelper;

    protected DBHelper getDBHelper() {
        if (dbHelper == null) {
            dbHelper = OpenHelperManager.getHelper(this, DBHelper.class);
        }
        return dbHelper;
    }

}
