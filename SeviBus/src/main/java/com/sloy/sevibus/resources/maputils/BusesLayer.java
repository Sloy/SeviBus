package com.sloy.sevibus.resources.maputils;

import android.graphics.Bitmap;
import android.view.View;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sloy.sevibus.resources.BusLocation;
import java.util.ArrayList;
import java.util.List;
import uk.me.jstott.jcoord.UTMRef;

/**
 * Created by rafa on 08/11/13.
 */
public class BusesLayer extends Layer {

    private List<BusLocation> mItems;
    private List<Marker> mMarkers;
    private BitmapDescriptor mIcon;

    public BusesLayer(Bitmap iconBus, List<BusLocation> items) {
        mItems = items;
        mIcon = BitmapDescriptorFactory.fromBitmap(iconBus);
        mMarkers = new ArrayList<Marker>();
    }

    @Override
    public Object getItemFromMarker(Marker marker) {
        if (mMarkers != null && !mMarkers.isEmpty()) {
            int i = mMarkers.indexOf(marker);
            if (i > 0) {
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
    public BitmapDescriptor getIcon() {
        return mIcon;
    }

    @Override
    public float[] getIconAnchor() {
        return new float[]{0.5f, 0.5f};
    }

    @Override
    public LatLng getLocationOfItem(int position) {
        BusLocation item = (BusLocation) getItem(position);
        // El formato de entrada es UTM, hay que convertirla a
        UTMRef utm = new UTMRef(item.xcoord, item.ycoord, 'N', 30);
        uk.me.jstott.jcoord.LatLng latlng = utm.toLatLng();

        //LatLng posCalibrada = new LatLng(latlng.getLat() - 200 * 10, latlng.getLng() - 130 * 10);
        LatLng posCalibrada = new LatLng(latlng.getLat() - 0.002, latlng.getLng() - 0.0013);
        //TODO quillo, que esto no está bien calibrado....
        return posCalibrada;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
    }
}
