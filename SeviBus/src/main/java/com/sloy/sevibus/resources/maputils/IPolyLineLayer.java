package com.sloy.sevibus.resources.maputils;

import com.google.android.gms.maps.model.Polyline;

/**
 * Created by rafa on 08/11/13.
 */
public interface IPolyLineLayer {

    public int getPolyLineColor();

    public Polyline getPolyline();

    public float getPolylineWidth();

    public void removePolyline();

    public void setPolyline(Polyline polyline);
}
