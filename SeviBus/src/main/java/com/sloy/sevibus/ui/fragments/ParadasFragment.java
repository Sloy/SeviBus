package com.sloy.sevibus.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.ui.activities.ParadaInfoActivity;
import com.sloy.sevibus.ui.adapters.ParadasAdapter;

import java.util.List;

public class ParadasFragment extends BaseDBFragment {

    private ListView mList;
    private ParadasAdapter mAdapter;
    private List<Parada> mParadas;

    //TODO no es aconsejable usar un constructor con parámetros, mejor usar un método estático que reciba Bundle y eliminar ambos constructores
    public ParadasFragment(List<Parada> paradas) {
        mParadas = paradas;
    }

    public ParadasFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_list_default, container, false);

        v.findViewById(R.id.list_progress).setVisibility(View.GONE);
        mList = (ListView) v.findViewById(android.R.id.list);
        mList.setVisibility(View.VISIBLE);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = mList.getItemAtPosition(position);
                if (itemAtPosition != null) {
                    Integer numero = ((Parada) itemAtPosition).getNumero();
                    startActivity(ParadaInfoActivity.getIntent(getActivity(), numero));
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        mAdapter = new ParadasAdapter(getActivity(), mParadas, getDBHelper());
        mAdapter.setTrayecto(true);
        if (mAdapter.isTrayecto()) {
            View header = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_parada_inicio_trayecto, mList, false);
            View footer = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_parada_fin_trayecto, mList, false);
            mList.addHeaderView(header, null, false);
            mList.addFooterView(footer, null, false);
        }
        mList.setAdapter(mAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.paradas_de_linea, menu);
    }
}
