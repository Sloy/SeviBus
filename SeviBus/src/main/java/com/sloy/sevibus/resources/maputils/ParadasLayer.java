package com.sloy.sevibus.resources.maputils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.ui.activities.ParadaInfoActivity;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class ParadasLayer extends Layer {

    private List<Parada> mItems;
    private List<Marker> mMarkers;
    private View mBalloonView;
    private WeakReference<Context> mContext;
    private DBHelper mDbHelper;

    public ParadasLayer(List<Parada> items, Context context, DBHelper dbHelper) {
        mItems = items;
        mMarkers = new ArrayList<Marker>();
        mContext = new WeakReference<Context>(context);
        mDbHelper = dbHelper;
    }

    @Override
    public Object getItemFromMarker(Marker marker) {
        if (mMarkers != null && !mMarkers.isEmpty()) {
            int i = mMarkers.indexOf(marker);
            if (i >= 0) {
                return mItems.get(i);
            }
        }
        return null;
    }

    @Override
    public Marker getMarkerFromItem(Object item) {
        if (mMarkers != null && !mMarkers.isEmpty()) {
            return mMarkers.get(mItems.indexOf(item));
        } else {
            return null;
        }
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public Object getMarker(int position) {
        if (mMarkers != null && !mMarkers.isEmpty()) {
            return mMarkers.get(position);
        } else {
            return null;
        }
    }

    @Override
    public void addMarker(Marker marker) {
        mMarkers.add(marker);
    }

    @Override
    public void clearMarkers() {
        if (mMarkers != null && !mMarkers.isEmpty()) {
            for (Marker m : mMarkers) {
                m.remove();
            }
            mMarkers.clear();
        }
    }

    @Override
    public int getSize() {
        return mItems.size();
    }

    @Override
    public LatLng getLocationOfItem(int position) {
        Parada p = mItems.get(position);
        return new LatLng(p.getLatitud(), p.getLongitud());
    }

    @Override
    public View getInfoContents(Marker marker) {
        Parada p = (Parada) this.getItemFromMarker(marker);
        if (mBalloonView == null) {
            View content = LayoutInflater.from(mContext.get()).inflate(R.layout.map_balloon_parada, null);
            mBalloonView = content;
        }

        TextView numero = (TextView) mBalloonView.findViewById(R.id.item_parada_numero);
        TextView nombre = (TextView) mBalloonView.findViewById(R.id.item_parada_nombre);
        TextView lineas = (TextView) mBalloonView.findViewById(R.id.item_parada_lineas);

        numero.setText(String.valueOf(p.getNumero()));
        nombre.setText(p.getDescripcion());

        //TODO WTF!! Quita la llamada a la BBDD de aquí, pedazo de loco!!!
        List<Linea> lineasList = null;
        try {
            lineasList = DBQueries.getLineasDeParada(mDbHelper, p.getNumero());
        } catch (SQLException e) {
            Debug.registerHandledException(e);
            e.printStackTrace();
        }

        StringBuilder sbLineas = new StringBuilder();
        for (Linea l : lineasList) {
            sbLineas.append(l.getNumero());
            sbLineas.append("  ");
        }
        if (sbLineas.length() > 2) {
            sbLineas.setLength(sbLineas.length() - 2);
        }
        lineas.setText(sbLineas.toString());
        return mBalloonView;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Parada p = (Parada) this.getItemFromMarker(marker);
        try {
            int id = p.getNumero();
            mContext.get().startActivity(ParadaInfoActivity.getIntent(mContext.get(), id));
        } catch (Exception e) {
            Toast.makeText(mContext.get(), "Error desconocido =(", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            Debug.registerHandledException(e);
        }
    }
}
