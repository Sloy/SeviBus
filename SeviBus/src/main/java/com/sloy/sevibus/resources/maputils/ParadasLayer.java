package com.sloy.sevibus.resources.maputils;

import android.content.Context;
import android.support.v4.util.Pair;
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

    private List<Pair<Parada, List<Linea>>> mItems;
    private List<Marker> mMarkers;
    private View mBalloonView;
    private WeakReference<Context> mContext;

    public ParadasLayer(List<Pair<Parada, List<Linea>>> items, Context context) {
        mItems = items;
        mMarkers = new ArrayList<Marker>();
        mContext = new WeakReference<Context>(context);
    }

    @Override
    public Pair<Parada, List<Linea>> getItemFromMarker(Marker marker) {
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
        Parada p = mItems.get(position).first;
        return new LatLng(p.getLatitud(), p.getLongitud());
    }

    @Override
    public View getInfoContents(Marker marker) {
        Pair<Parada, List<Linea>> pair = this.getItemFromMarker(marker);
        Parada p = pair.first;
        List<Linea> lineas = pair.second;

        if (mBalloonView == null) {
            View content = LayoutInflater.from(mContext.get()).inflate(R.layout.map_balloon_parada, null);
            mBalloonView = content;
        }

        TextView numero = (TextView) mBalloonView.findViewById(R.id.item_parada_numero);
        TextView nombre = (TextView) mBalloonView.findViewById(R.id.item_parada_nombre);
        TextView lineasText = (TextView) mBalloonView.findViewById(R.id.item_parada_lineas);

        numero.setText(String.valueOf(p.getNumero()));
        nombre.setText(p.getDescripcion());

        StringBuilder sbLineas = new StringBuilder();
        for (Linea l : lineas) {
            sbLineas.append(l.getNumero());
            sbLineas.append("  ");
        }
        if (sbLineas.length() > 2) {
            sbLineas.setLength(sbLineas.length() - 2);
        }
        lineasText.setText(sbLineas.toString());
        return mBalloonView;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Parada p = this.getItemFromMarker(marker).first;
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
