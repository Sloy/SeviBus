package com.sloy.sevibus.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.domain.model.LineaCollection;
import com.sloy.sevibus.domain.model.ParadaCollection;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.model.tussam.Seccion;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.ui.fragments.ParadasFragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class ParadasDeLineaActivity extends BaseToolbarActivity {

    public static Intent getIntent(Context context, int id) {
        Intent i = new Intent(context, ParadasDeLineaActivity.class);
        i.putExtra("linea_id", id);
        return i;
    }

    private ParadaCollection paradaCollection;
    private LineaCollection lineaCollection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paradas_de_linea);

        paradaCollection = StuffProvider.getParadaCollection(this);
        lineaCollection = StuffProvider.getLineaCollection(this);

        int lineaID = getIntent().getIntExtra("linea_id", 0);

        lineaCollection.getById(lineaID)
          .doOnSuccess(linea -> getSupportActionBar().setTitle(String.format("Línea %s", linea.getNumero())))
          .map(Linea::getSecciones)
          .flatMapObservable(Observable::from)
          .flatMap(seccion -> paradaCollection.getBySeccion(seccion.getId())
            .toList()
            .map(paradas -> new SeccionParadasPair(seccion, paradas)))
          .toList()
          .subscribe(this::setupFragments);
    }


    private void setupFragments(List<SeccionParadasPair> secciones) {
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), secciones);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
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

}
