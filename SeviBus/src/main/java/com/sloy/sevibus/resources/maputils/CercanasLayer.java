package com.sloy.sevibus.resources.maputils;

import android.content.Context;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.model.tussam.Parada;
import java.util.List;

/**
 * Created by rafa on 09/11/13.
 */
public class CercanasLayer extends ParadasLayer {
    private BitmapDescriptor mIcon;

    public CercanasLayer(List<Parada> paradasCercanas, Context context, DBHelper dbHelper) {
        super(paradasCercanas, context, dbHelper);
        mIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker_cercanas);
    }

    @Override
    public BitmapDescriptor getIcon() {
        return mIcon;
    }

    @Override
    public float[] getIconAnchor() {
        return new float[]{0.5f, 1f};
    }
}
