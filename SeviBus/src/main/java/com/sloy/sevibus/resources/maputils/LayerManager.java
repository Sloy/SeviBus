package com.sloy.sevibus.resources.maputils;

import android.util.Log;
import android.view.View;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;

public class LayerManager implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private List<Layer> mCurrentLayers;

    public void setMap(GoogleMap map) {
        if (mMap != null && mMap != map) {
            Log.d("SeviBus LayerManager", "Asociando un mapa nuevo cuando ya había uno asociado. Limpiando antes...");
            releaseMap();
        }
        this.mMap = map;
        this.mCurrentLayers = new ArrayList<Layer>();

        mMap.setInfoWindowAdapter(this);
        mMap.setOnInfoWindowClickListener(this);
    }

    public void releaseMap() {
        if (mMap != null) {
            mMap.clear();
            mMap = null;
        }
        if (mCurrentLayers != null) {
            mCurrentLayers.clear();
            mCurrentLayers = null;
        }
    }


    public void addLayer(Layer newLayer) {
        mCurrentLayers.add(newLayer);
        paintSingleLayer(newLayer);
    }

    private void paintSingleLayer(Layer layer) {
        PolylineOptions polilyneOptions = new PolylineOptions();
        layer.clearMarkers();

        for(int i = 0; i<layer.getSize(); i++) {
            LatLng locationOfItem = layer.getLocationOfItem(i);
            float[] anchor = layer.getIconAnchor();
            Marker m = mMap.addMarker(new MarkerOptions().position(locationOfItem).icon(layer.getIcon()).anchor(anchor[0], anchor[1]));
            layer.addMarker(m);
            polilyneOptions.add(locationOfItem);
        }
        if (layer instanceof IPolyLineLayer) {
            polilyneOptions.color(((IPolyLineLayer) layer).getPolyLineColor());
            polilyneOptions.width(((IPolyLineLayer) layer).getPolylineWidth());
            ((IPolyLineLayer) layer).setPolyline(mMap.addPolyline(polilyneOptions));
        }
    }

    public void removeLayer(Layer layer) {
        if (!mCurrentLayers.contains(layer)) {
            Log.w("SeviBus LayerManager", "La capa que intentas borrar no está en la lista de capas actuales. La voy a eliminar de todas formas, pero el resultado es inpredecible");
        }
        layer.clearMarkers();

        if (layer instanceof IPolyLineLayer) {
            ((IPolyLineLayer) layer).removePolyline();
        }

        mCurrentLayers.remove(layer);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        for (Layer layer : mCurrentLayers) {
            Object item = layer.getItemFromMarker(marker);
            if (item != null) {
                return layer.getInfoContents(marker);
            }
        }
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        for (Layer layer : mCurrentLayers) {
            Object item = layer.getItemFromMarker(marker);
            if (item != null) {
                layer.onInfoWindowClick(marker);
            }
        }
    }
}
