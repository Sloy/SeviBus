package com.sloy.sevibus.resources.maputils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.util.Pair;
import android.view.View;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;

import java.util.List;

public abstract class Layer implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener{

    public abstract Object getItemFromMarker(Marker marker);

    public abstract Marker getMarkerFromItem(Object item);

    public abstract Object getItem(int position);

    public abstract Object getMarker(int position);

    public abstract void addMarker(Marker marker);

    public abstract void clearMarkers();

    public abstract int getSize();

    public abstract BitmapDescriptor getIcon();

    public abstract float[] getIconAnchor();

    public abstract LatLng getLocationOfItem(int position);

    public static Bitmap getDrawableBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

}
