package com.sloy.sevibus.ui.adapters;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.Debug;
import java.sql.SQLException;
import java.util.List;

public class ParadasAdapter extends BaseAdapter {

    private List<Parada> mItems;
    private Context mContext;
    private DBHelper dbHelper;


    private boolean trayecto = false;

    public ParadasAdapter(Context context, List<Parada> items, DBHelper helper) {
        mItems = items;
        mContext = context;
        dbHelper = helper;
    }

    public int getCount() {
        return mItems.size();
    }

    public Parada getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return getItem(position).getNumero();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Parada item = getItem(position);
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_parada_extended, null);
            if(trayecto){
              convertView.findViewById(R.id.item_parada_indicador_trayecto).setVisibility(View.VISIBLE);
            }else{
                convertView.findViewById(R.id.item_parada_indicador_trayecto).setVisibility(View.GONE);
            }
        }
        TextView numeroText = (TextView) convertView.findViewById(R.id.item_parada_numero_text);
        TextView nombre = (TextView) convertView.findViewById(R.id.item_parada_nombre);
        TextView lineas = (TextView) convertView.findViewById(R.id.item_parada_lineas);

        numeroText.setText(Html.fromHtml(String.format("Parada nº <b>%d</b>", item.getNumero())));
        nombre.setText(item.getDescripcion());

        //TODO WTF!! Quita la llamada a la BBDD de aquí, pedazo de loco!!!
        List<Linea> lineasList = null;
        try {
            lineasList = DBQueries.getLineasDeParada(dbHelper, item.getNumero());
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("sevibus", "Error cargando las l√≠neas de la parada " + item.getNumero(), e);
        }

        StringBuilder sbLineas = new StringBuilder();
        if (lineasList.size() > 0) {
            for (Linea l : lineasList) {
                sbLineas.append(l.getNumero());
                sbLineas.append("  ");
            }
            sbLineas.setLength(sbLineas.length() - 2);
            lineas.setText(sbLineas.toString());
        } else {
            Log.wtf("SeviBus", "¿Una parada sin líneas? ¿Cómo va a ser eso? No puede ser!! :S");
            Debug.registerHandledException(mContext, new IllegalStateException("La parada " + item.getNumero() + " no parece tener líneas."));
        }

        return convertView;
    }

    public void changeParadas(List<Parada> paradas) {
        mItems = paradas;
        notifyDataSetChanged();
    }

    public void setTrayecto(boolean esTrayecto) {
        this.trayecto = esTrayecto;
    }

    public boolean isTrayecto() {
        return trayecto;
    }
}