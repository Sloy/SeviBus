package com.sloy.sevibus.ui.fragments.main;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.ui.activities.HomeActivity;
import com.sloy.sevibus.ui.activities.LocationProviderActivity;
import com.sloy.sevibus.ui.fragments.BaseDBFragment;
import com.sloy.sevibus.ui.fragments.MainPageFragment;
import com.sloy.sevibus.ui.fragments.MapContainerFragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParadasCercanasMainFragment extends BaseDBFragment implements ILocationSensitiveFragment {

    private View mContenido;
    private TextView mMensaje;
    private MainPageFragment mMainPage;
    private List<Integer> mDistanciasTmp;

    public interface ParadasCercanasMainClickListener {
        void onParadaCercanaClick(int idParada);
        void onParadaCercanaMas();
    }

    public static ParadasCercanasMainFragment getInstance() {
        return new ParadasCercanasMainFragment();
    }

    private View mParada1View, mParada2View, mParada3View, mParada4View;
    private View mButtonMas;
    private View.OnClickListener mParadaListener;

    public void setMainPage(MainPageFragment fragment) {
        mMainPage = fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_paradas_cercanas, container, false);

        mMensaje = (TextView) v.findViewById(R.id.main_paradas_cercanas_mensaje);
        mContenido = v.findViewById(R.id.main_paradas_cercanas_contenido);

        mParada1View = mContenido.findViewById(R.id.main_paradas_cercanas_parada_1);
        mParada2View = mContenido.findViewById(R.id.main_paradas_cercanas_parada_2);
        mParada3View = mContenido.findViewById(R.id.main_paradas_cercanas_parada_3);
        mParada4View = mContenido.findViewById(R.id.main_paradas_cercanas_parada_4);

        mButtonMas = mContenido.findViewById(R.id.main_paradas_cercanas_boton_mas);
        mButtonMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity) getActivity()).getMapOptions().setMostrarCercanas(true);
                ((ParadasCercanasMainClickListener) getActivity()).onParadaCercanaMas();
            }
        });

        mParadaListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer parada = (Integer) v.getTag();
                ((ParadasCercanasMainClickListener) getActivity()).onParadaCercanaClick(parada);
            }
        };

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        muestraCargando();
        setupMapa();

    }

    private void setupMapa() {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction trans = fm.beginTransaction();
        Fragment f = fm.findFragmentByTag("mapa");
        if (f == null) {
            f = MapContainerFragment.getInstance(false);
        }
        if (f.isAdded()) {
            trans.attach(f);
        } else {
            trans.add(R.id.main_paradas_cercanas_mapa_content, f, "mapa");
        }

        getView().findViewById(R.id.main_paradas_cercanas_mapa_trigger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ParadasCercanasMainClickListener) getActivity()).onParadaCercanaMas();
            }
        });

        trans.commit();

    }

        @Override
    public void onStart() {
        super.onStart();
        ((LocationProviderActivity)getActivity()).suscribeForUpdates(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((LocationProviderActivity)getActivity()).unsuscribe(this);
    }

    private void muestraCargando() {
        mMensaje.setText(R.string.paradas_cercanas_cargando);
        mMensaje.setVisibility(View.VISIBLE);
        mContenido.setVisibility(View.GONE);
    }

    private void muestraNoDatos() {
        mMensaje.setText(R.string.paradas_cercanas_empty);
        mMensaje.setVisibility(View.VISIBLE);
        mContenido.setVisibility(View.GONE);
    }

    private void muestraError() {
        mMensaje.setText(R.string.ubicacion_error);
        mMensaje.setVisibility(View.VISIBLE);
        mContenido.setVisibility(View.GONE);
    }

    private void muestraParadas(final List<Parada> paradas) {
        mMensaje.setVisibility(View.GONE);
        mContenido.setVisibility(View.VISIBLE);

        Parada p1 = null, p2 = null, p3 = null, p4 = null;
        int d1 = 0, d2 = 0, d3 = 0, d4 = 0;

        int count = paradas.size();
        if (count >= 1) {
            p1 = paradas.get(0);
            d1 = mDistanciasTmp.get(0);
        }
        if (count >= 2) {
            p2 = paradas.get(1);
            d2 = mDistanciasTmp.get(1);
        }
        if (count >= 3) {
            p3 = paradas.get(2);
            d3 = mDistanciasTmp.get(2);
        }
        if (count >= 4) {
            p4 = paradas.get(3);
            d4 = mDistanciasTmp.get(3);
        }

        bindView(p1, d1, mParada1View);
        bindView(p2, d2, mParada2View);
        bindView(p3, d3,  mParada3View);
        bindView(p4, d4, mParada4View);
    }

    private void bindView(Parada p, int dist, View v) {
        if (p != null) {
            ((TextView) v.findViewById(R.id.item_parada_numero)).setText(p.getNumero().toString());
            ((TextView) v.findViewById(R.id.item_parada_nombre)).setText(p.getDescripcion());
            ((TextView) v.findViewById(R.id.item_parada_distancia)).setText(dist+"m");
            v.setTag(p.getNumero());
            v.setOnClickListener(mParadaListener);
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }


    @Override
    public void updateLocation(Location location) {
        if (location == null) {
            muestraError();
            Debug.registerHandledException(new NullPointerException("Ubicación nula recibida"));
            return;
        }

        final double latitud = location.getLatitude();
        final double longitud = location.getLongitude();

        new AsyncTask<Void, Void, List<Parada>>() {
            @Override
            protected List<Parada> doInBackground(Void... params) {
                List<Parada> paradas = null;
                try {
                    paradas = DBQueries.getParadasCercanas(getDBHelper(), latitud, longitud, true);
                    if (mDistanciasTmp == null) {
                        mDistanciasTmp = new ArrayList<Integer>(paradas.size());
                    } else {
                        mDistanciasTmp.clear();
                    }
                    float[] distRes = new float[1];
                    for (Parada p : paradas) {
                        Location.distanceBetween(latitud, longitud, p.getLatitud(), p.getLongitud(), distRes);
                        mDistanciasTmp.add(Math.round(distRes[0]));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    Debug.registerHandledException(e);
                }
                return paradas;
            }

            @Override
            protected void onPostExecute(List<Parada> paradas) {
                if (paradas == null || paradas.isEmpty()) {
                    muestraNoDatos();
                } else {
                    muestraParadas(paradas);
                }
            }
        }.execute();
    }

}
