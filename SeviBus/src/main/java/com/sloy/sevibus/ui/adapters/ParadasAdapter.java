package com.sloy.sevibus.ui.adapters;

import android.support.v4.util.Pair;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.Debug;

import java.util.List;

public class ParadasAdapter extends BaseAdapter {

    private List<Pair<Parada, List<Linea>>> paradasAndLineas;

    private boolean trayecto = false;

    public ParadasAdapter(List<Pair<Parada, List<Linea>>> paradasAndLineas) {
        this.paradasAndLineas = paradasAndLineas;
    }

    public int getCount() {
        return paradasAndLineas.size();
    }

    public Pair<Parada, List<Linea>> getItem(int position) {
        return paradasAndLineas.get(position);
    }

    public long getItemId(int position) {
        return getItem(position).first.getNumero();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Pair<Parada, List<Linea>> item = getItem(position);
        Parada parada = item.first;
        List<Linea> lineas = item.second;

        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.list_item_parada_extended, null);
            if (trayecto) {
                convertView.findViewById(R.id.item_parada_indicador_trayecto).setVisibility(View.VISIBLE);
            } else {
                convertView.findViewById(R.id.item_parada_indicador_trayecto).setVisibility(View.GONE);
            }
        }
        TextView numeroText = (TextView) convertView.findViewById(R.id.item_parada_numero_text);
        TextView nombre = (TextView) convertView.findViewById(R.id.item_parada_nombre);
        TextView lineasText = (TextView) convertView.findViewById(R.id.item_parada_lineas);

        numeroText.setText(Html.fromHtml(String.format("Parada nº <b>%d</b>", parada.getNumero())));
        nombre.setText(parada.getDescripcion());

        StringBuilder sbLineas = new StringBuilder();
        if (lineas.size() > 0) {
            for (Linea l : lineas) {
                sbLineas.append(l.getNumero());
                sbLineas.append("  ");
            }
            sbLineas.setLength(sbLineas.length() - 2);
            lineasText.setText(sbLineas.toString());
        } else {
            Log.wtf("SeviBus", "¿Una parada sin líneas? ¿Cómo va a ser eso? No puede ser!! :S");
            Debug.registerHandledException(new IllegalStateException("La parada " + parada.getNumero() + " no parece tener líneas."));
        }

        return convertView;
    }

    public void changeParadas(List<Pair<Parada, List<Linea>>> paradasAndLineas) {
        this.paradasAndLineas = paradasAndLineas;
        notifyDataSetChanged();
    }

    public void setTrayecto(boolean esTrayecto) {
        this.trayecto = esTrayecto;
    }

    public boolean isTrayecto() {
        return trayecto;
    }
}