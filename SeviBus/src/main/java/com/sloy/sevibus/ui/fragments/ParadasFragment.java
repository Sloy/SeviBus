package com.sloy.sevibus.ui.fragments;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.domain.model.LineaCollection;
import com.sloy.sevibus.domain.model.ParadaCollection;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.resources.actions.ObtainParadasWithLineasAction;
import com.sloy.sevibus.ui.activities.ParadaInfoActivity;
import com.sloy.sevibus.ui.adapters.ParadasAdapter;

public class ParadasFragment extends BaseDBFragment {

    private static final String EXTRA_SECCION = "seccion_id";

    private ListView mList;

    private ParadaCollection paradaCollection;
    private ObtainParadasWithLineasAction obtainParadasWithLineasAction;

    public static ParadasFragment newInstance(Integer seccionId) {
        ParadasFragment paradasFragment = new ParadasFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_SECCION, seccionId);
        paradasFragment.setArguments(args);
        return paradasFragment;
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
        paradaCollection = StuffProvider.getParadaCollection(getActivity());
        obtainParadasWithLineasAction = StuffProvider.getObtainParadasWithLineasAction(getActivity());

        int seccionId = getArguments().getInt(EXTRA_SECCION);

        paradaCollection.getBySeccion(seccionId)
          .toList()
          .flatMap(paradas ->  obtainParadasWithLineasAction.obtain(paradas))
          .toList()
          .subscribe(paradasAndLineas -> {
              ParadasAdapter adapter = new ParadasAdapter(paradasAndLineas);
              adapter.setTrayecto(true);
              if (adapter.isTrayecto()) {
                  View header = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_parada_inicio_trayecto, mList, false);
                  View footer = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_parada_fin_trayecto, mList, false);
                  mList.addHeaderView(header, null, false);
                  mList.addFooterView(footer, null, false);
              }
              mList.setAdapter(adapter);
          });


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
