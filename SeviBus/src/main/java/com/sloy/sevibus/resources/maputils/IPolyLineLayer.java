package com.sloy.sevibus.resources.maputils;

import com.google.android.gms.maps.model.Polyline;

public interface IPolyLineLayer {

   int getPolyLineColor();

   Polyline getPolyline();

   float getPolylineWidth();

   void removePolyline();

   void setPolyline(Polyline polyline);
}
