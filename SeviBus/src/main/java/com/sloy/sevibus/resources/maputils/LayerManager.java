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

/**
 * Created by rafa on 08/11/13.
 */
public class LayerManager implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private List<Layer> mCurrentLayers;

    public void setMap(GoogleMap map) {
        // Si había otro mapa asociado al LayerManager lo limpia primero.
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


    // Añade una capa y la pinta
    public void addLayer(Layer newLayer) {
        mCurrentLayers.add(newLayer);
        paintSingleLayer(newLayer);
    }

    private void paintSingleLayer(Layer layer) {
        PolylineOptions polilyneOptions = new PolylineOptions();
        // Nos aseguramos de que no hay marcadores ya pintados
        layer.clearMarkers();

        // Pintamos item a item
        for(int i = 0; i<layer.getSize(); i++) {
            LatLng locationOfItem = layer.getLocationOfItem(i);
            //TODO hacer algo para evitar marcadores duplicados?
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
        // Elimina los marcadores de la línea
        layer.clearMarkers();

        // Elimina la polylínea si tiene
        if (layer instanceof IPolyLineLayer) {
            ((IPolyLineLayer) layer).removePolyline();
        }

        // Quita la capa de la lista interna.
        mCurrentLayers.remove(layer);
    }

    public void removeAllLayers() {
        // Opción 1: Limpiar el mapa entero
        mMap.clear();
        mCurrentLayers.clear();

        // Opción 2: Eliminar capa a capa
        /*for (Layer<Object> layer : mCurrentLayers) {
            removeLayer(layer);
        }*/
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // No lo usamos nunca
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        // Buscar capa a capa a quién pertenece el marker
        for (Layer layer : mCurrentLayers) {
            Object item = layer.getItemFromMarker(marker);
            if (item != null) {
                return layer.getInfoContents(marker); //TODO usar un listener propio para pasarle el Item directamente
            }
        }
        return null; // No se encontró el marker en ninguna capa :(
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // Buscar capa a capa a quién pertenece el marker
        for (Layer layer : mCurrentLayers) {
            Object item = layer.getItemFromMarker(marker);
            if (item != null) {
                layer.onInfoWindowClick(marker); //TODO usar un listener propio para pasarle el Item directamente
            }
        }
    }
}
