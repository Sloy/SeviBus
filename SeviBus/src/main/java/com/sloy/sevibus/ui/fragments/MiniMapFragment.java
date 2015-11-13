package com.sloy.sevibus.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sloy.sevibus.R;

public class MiniMapFragment extends SupportMapFragment {
    
    private LatLng mPosFija;
    private GoogleMap.OnMapClickListener mListener;
    

    public MiniMapFragment() {
        super();
        
    }
    
    public static MiniMapFragment newInstance(GoogleMap.OnMapClickListener listener, LatLng posicion){
        MiniMapFragment frag = new MiniMapFragment();
        frag.mPosFija = posicion;
        frag.mListener = listener;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
        View v = super.onCreateView(arg0, arg1, arg2);
        initMap();
        return v;
    }
    
    private void initMap(){
        UiSettings settings = getMap().getUiSettings();
        settings.setAllGesturesEnabled(false);
        settings.setMyLocationButtonEnabled(false);
        
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(mPosFija,16));
        getMap().addMarker(new MarkerOptions().position(mPosFija).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_sevibus)));
        getMap().setOnMapClickListener(mListener);
    }
}
