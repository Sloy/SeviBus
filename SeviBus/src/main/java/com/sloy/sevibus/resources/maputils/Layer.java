package com.sloy.sevibus.resources.maputils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by rafa on 08/11/13.
 */
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

    // No usaremos este método, así que puede ser null para todas las capas
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

}
