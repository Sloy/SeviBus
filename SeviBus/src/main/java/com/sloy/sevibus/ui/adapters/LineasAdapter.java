package com.sloy.sevibus.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.sloy.sevibus.R;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Seccion;
import com.sloy.sevibus.model.tussam.TipoLinea;
import com.sloy.sevibus.ui.widgets.LineaBadge;

import java.util.List;

public class LineasAdapter extends BaseAdapter {
    
    private Context mContext;
    private List<Object> mItems;
    
    public LineasAdapter(Context context) {
        mContext = context;
    }
    
    public int getCount() {
        return mItems.size();
    }
    
    @Override
    public int getViewTypeCount() {
        return 2;
    }
    
    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof TipoLinea) {
            return 0;
        } else if (item instanceof Linea) {
            return 1;
        } else {
            return -1;
        }
    }
    
    public Object getItem(int position) {
        return mItems.get(position);
    }
    
    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        Object item = getItem(position);
        
        switch (getItemViewType(position)) {
            case 0: // TipoLinea
                if (convertView == null) {
                    convertView = View.inflate(mContext, R.layout.list_item_linea_tipo, null);
                }
                ((TextView) convertView).setText(((TipoLinea) item).getNombre());
                break;
            
            case 1: // Línea
                if (convertView == null) {
                    convertView = View.inflate(mContext, R.layout.list_item_linea_horarios, null);
                }
                Linea l = (Linea) item;
                LineaBadge numero = (LineaBadge) convertView.findViewById(R.id.item_linea_numero);
                TextView nombre = (TextView) convertView.findViewById(R.id.item_linea_nombre);
                TextView horaInicio = (TextView) convertView.findViewById(R.id.item_linea_hora_inicio);
                TextView horaFin = (TextView) convertView.findViewById(R.id.item_linea_hora_fin);
                
                numero.setLinea(l);
                nombre.setText(l.getNombre());
                Seccion seccion = l.getSecciones().iterator().next(); //Una porque sí
                horaInicio.setText(seccion.getHoraInicio());
                horaFin.setText(seccion.getHoraFin());
                break;
            default:
                break;
        }
        
        return convertView;
    }
    
    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) > 0;
    }
    
    public void setItems(List<Object> items) {
        this.mItems = items;
        notifyDataSetChanged();
    }
    
}