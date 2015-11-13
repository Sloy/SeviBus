package com.sloy.sevibus.resources.maputils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Polyline;
import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.model.tussam.Parada;
import java.util.List;

/**
 * Created by rafa on 08/11/13.
 */
public class LineaLayer extends ParadasLayer implements IPolyLineLayer {

    private int mColor;
    private Polyline mPolyline;
    private float mPolylineWidth;
    private BitmapDescriptor mIcon;

    public LineaLayer(Drawable iconLinea, int colorLinea, List<Parada> paradasDeLinea, Context context, DBHelper dbHelper) {
        super(paradasDeLinea, context, dbHelper);
        mColor = colorLinea;
        mIcon = BitmapDescriptorFactory.fromBitmap(Layer.getDrawableBitmap(iconLinea));
        mPolylineWidth = context.getResources().getDimensionPixelSize(R.dimen.polyline_width);
    }

    @Override
    public int getPolyLineColor() {
        return mColor;
    }

    @Override
    public Polyline getPolyline() {
        return mPolyline;
    }

    @Override
    public float getPolylineWidth() {
        return mPolylineWidth;
    }

    @Override
    public void removePolyline() {
        mPolyline.remove();
        mPolyline = null;
    }

    @Override
    public void setPolyline(Polyline polyline) {
        mPolyline = polyline;
    }

    @Override
    public BitmapDescriptor getIcon() {
        return mIcon;
    }

    @Override
    public float[] getIconAnchor() {
        return new float[]{0.5f, 0.5f};
    }
}
