package com.sloy.sevibus.ui.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.TipoLinea;
import com.sloy.sevibus.ui.adapters.LineasAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class SeleccionaLineaDialogFragment extends DialogFragment {

    private DBHelper dbHelper;

    protected DBHelper getDBHelper() {
        if (dbHelper == null) {
            dbHelper = OpenHelperManager.getHelper(getActivity(), DBHelper.class);
        }
        return dbHelper;
    }
    //

    private ListView mList;
    private LineasAdapter mAdapter;
    private View mProgressBar;
    private TreeMap<TipoLinea, List<Linea>> mLineasOrganizadas;
    private LineaSelectedListener mListener;

    public void setListener(LineaSelectedListener listener) {
        this.mListener = listener;
    }

    public interface LineaSelectedListener {
        void onLineaSelecteded(Linea linea);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_list_default, container, false);

        mProgressBar = v.findViewById(R.id.list_progress);
        mList = (ListView) v.findViewById(android.R.id.list);

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
                Linea l = (Linea) mAdapter.getItem(pos);
                if (mListener != null) {
                    mListener.onLineaSelecteded(l);
                    dismiss();
                } else {
                    ((LineaSelectedListener) getActivity()).onLineaSelecteded(l);
                }
            }
        });

        getDialog().setTitle("Elige línea para mostrar");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            v.setBackgroundColor(getResources().getColor(R.color.window_background));
        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter = new LineasAdapter(getActivity());
        AsyncTask<Void, Void, Boolean> descargaLineas = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                Boolean res = false;
                try {
                    TreeMap<TipoLinea, List<Linea>> lineasOrganizadas = new TreeMap<TipoLinea, List<Linea>>();
                    // Primero obtengo todas las líneas, y luego las ordeno en
                    // un mapa según su tipo
                    List<Linea> todasLineas = DBQueries.getTodasLineas(getDBHelper());
                    for (Linea l : todasLineas) {
                        TipoLinea tipo = l.getTipo();
                        if (lineasOrganizadas.containsKey(tipo)) {
                            lineasOrganizadas.get(tipo).add(l);
                        } else {
                            List<Linea> nuevaListaLineas = new ArrayList<Linea>();
                            nuevaListaLineas.add(l);
                            lineasOrganizadas.put(tipo, nuevaListaLineas);
                        }
                    }
                    mLineasOrganizadas = lineasOrganizadas;
                    res = true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return res;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    onLineasCargadas();
                }
            }
        };
        descargaLineas.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void selectTodasLineas() {
        // Creo una lista ordenada de objects conteniendo el tipo como cabecera
        // acompañado por sus líneas
        List<Object> listaConEncabezados = new ArrayList<Object>();
        for (TipoLinea tipo : mLineasOrganizadas.keySet()) {
            listaConEncabezados.add(tipo);
            for (Linea linea : mLineasOrganizadas.get(tipo)) {
                listaConEncabezados.add(linea);
            }
        }
        mAdapter.setItems(listaConEncabezados);
    }

    private void onLineasCargadas() {
        selectTodasLineas();

        mList.setAdapter(mAdapter);
        mProgressBar.setVisibility(View.GONE);
        mList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle("Líneas");
    }
}
