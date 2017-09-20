package com.sloy.sevibus.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.domain.model.LineaCollection;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.TipoLinea;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.ui.adapters.LineasAdapter;

import java.util.Collection;
import java.util.Map;

public class SeleccionaLineaDialogFragment extends DialogFragment {

    private LineaCollection lineaCollection;

    public SeleccionaLineaDialogFragment() {
    }

    private ListView mList;
    private LineasAdapter mAdapter;
    private View mProgressBar;
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
        lineaCollection.getAll()
          .toMultimap(Linea::getTipo)
          .subscribe(this::onLineasCargadas);
    }

    private void onLineasCargadas(Map<TipoLinea, Collection<Linea>> lineasOrganizadas) {
        mAdapter.setItems(lineasOrganizadas);

        mList.setAdapter(mAdapter);
        mProgressBar.setVisibility(View.GONE);
        mList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("Líneas");
        lineaCollection = StuffProvider.getLineaCollection(getActivity());
    }
}
