package com.sloy.sevibus.ui.fragments.main;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.ui.activities.LocationProviderActivity;
import com.sloy.sevibus.ui.fragments.BaseDBFragment;
import com.sloy.sevibus.ui.widgets.LineaBadge;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by rafa on 17/07/13.
 */
public class LineasCercanasMainFragment extends BaseDBFragment implements ILocationSensitiveFragment{

    private static final String SCREEN_NAME = "Líneas cercanas";
    private TextView mMensaje;
    private View mContenido;

    public static interface LineasCercanasMainClickListener {
        public void onLineaCercanaClick(int idParada);

        public void onLineaCercanaMas();
    }

    public static LineasCercanasMainFragment getInstance() {
        return new LineasCercanasMainFragment();
    }

    private View mLinea1View, mLinea2View, mLinea3View, mLinea4View;
    private View mButtonTodas;
    private View.OnClickListener mLineaListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_lineas_cercanas, container, false);

        // Vistas para ocultar/mostrar
        mMensaje = (TextView) v.findViewById(R.id.main_lineas_cercanas_mensaje);
        mContenido = v.findViewById(R.id.main_lineas_cercanas_contenido);

        mLinea1View = v.findViewById(R.id.main_lineas_cercanas_linea_1);
        mLinea2View = v.findViewById(R.id.main_lineas_cercanas_linea_2);
        mLinea3View = v.findViewById(R.id.main_lineas_cercanas_linea_3);
        mLinea4View = v.findViewById(R.id.main_lineas_cercanas_linea_4);

        mButtonTodas = v.findViewById(R.id.main_lineas_cercanas_boton_todas);
        mButtonTodas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LineasCercanasMainClickListener) getActivity()).onLineaCercanaMas();
            }
        });

        mLineaListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Linea linea = (Linea) v.getTag();
                ((LineasCercanasMainClickListener) getActivity()).onLineaCercanaClick(linea.getId());
            }
        };
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        muestraCargando();
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
        mMensaje.setText(R.string.lineas_cercanas_cargando);
        mMensaje.setVisibility(View.VISIBLE);
        mContenido.setVisibility(View.GONE);
    }

    private void muestraNoDatos() {
        //FIXME si ya hay paradas cercanas cargadas, las deja y ya está
        mMensaje.setText(R.string.lineas_cercanas_empty);
        mMensaje.setVisibility(View.VISIBLE);
        mContenido.setVisibility(View.GONE);
    }

    private void muestraError() {
        //FIXME si ya hay paradas cercanas cargadas, las deja y ya está
        mMensaje.setText(R.string.ubicacion_error);
        mMensaje.setVisibility(View.VISIBLE);
        mContenido.setVisibility(View.GONE);
    }

    private void muestraLineas(List<Linea> lineas) {
        mMensaje.setVisibility(View.GONE);
        mContenido.setVisibility(View.VISIBLE);

        //TODO crea las vistas y las añade dinámicamente
        Linea l1 = null, l2 = null, l3 = null, l4 = null;
        int count = lineas.size();
        if (count > 0) {
            l1 = lineas.get(0);
        }
        if (count > 1) {
            l2 = lineas.get(1);
        }
        if (count > 2) {
            l3 = lineas.get(2);
        }
        if (count > 3) {
            l4 = lineas.get(3);
        }

        bindView(l1, mLinea1View);
        bindView(l2, mLinea2View);
        bindView(l3, mLinea3View);
        bindView(l4, mLinea4View);
    }

    private void bindView(Linea l, View v) {
        if (l != null) {
            ((LineaBadge) v.findViewById(R.id.item_linea_numero)).setLinea(l);
            ((TextView) v.findViewById(R.id.item_linea_nombre)).setText(l.getNombre());
            v.setOnClickListener(mLineaListener);
            v.setTag(l);
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateLocation(Location location) {
        if (location == null) {
            muestraError();
            Debug.registerHandledException(getActivity(), new NullPointerException("Ubicación nula recibida"));
            return;
        }

        final double latitud = location.getLatitude();
        final double longitud = location.getLongitude();

        new AsyncTask<Void, Void, List<Linea>>() {
            @Override
            protected List<Linea> doInBackground(Void... params) {
                List<Linea> lineas = null;
                try {
                    lineas = DBQueries.getLineasCercanas(getDBHelper(), latitud, longitud);
                } catch (SQLException e) {
                    e.printStackTrace();
                    Debug.registerHandledException(getActivity(), e);
                }
                return lineas;
            }

            @Override
            protected void onPostExecute(List<Linea> lineas) {
                if (lineas == null || lineas.isEmpty()) {
                    muestraNoDatos();
                } else {
                    muestraLineas(lineas);
                }
            }
        }.execute();
    }
}
