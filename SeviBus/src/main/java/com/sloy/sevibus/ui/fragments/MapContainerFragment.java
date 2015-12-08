package com.sloy.sevibus.ui.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.sloy.sevibus.R;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.ui.activities.HomeActivity;
import com.sloy.sevibus.ui.activities.LocationProviderActivity;
import com.sloy.sevibus.ui.fragments.main.ILocationSensitiveFragment;

public class MapContainerFragment extends BaseDBFragment implements ILocationSensitiveFragment {

    private static final String SHOW_INTERFACE = "show_interface";

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private boolean mShowInterface = true;

    public static MapContainerFragment getInstance(boolean showInterface) {
        Bundle arguments = new Bundle();
        arguments.putBoolean(SHOW_INTERFACE, showInterface);
        MapContainerFragment f = new MapContainerFragment();
        f.setArguments(arguments);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        return v;
    }

    private void setUpMapIfNeeded() {
        // confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = mMapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                Debug.setUseFakeLocationProvider(getActivity(), mMap);
                // The Map is verified. It is now safe to manipulate the map.
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.setMyLocationEnabled(true);
                //Config
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(37.3808828009948, -5.986958742141724), 13)));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setUpMapIfNeeded();
        if (mMap != null) {
            showMapControls(mShowInterface);
            asociarOpciones(true);
            ((LocationProviderActivity) getActivity()).suscribeForUpdates(this);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        asociarOpciones(false);
        if (mShowInterface) {
            showMapControls(false);
            mShowInterface = true;
        }
        ((LocationProviderActivity)getActivity()).unsuscribe(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            mShowInterface = getArguments().getBoolean(SHOW_INTERFACE, true);
        }
        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag("map");
        if (mMapFragment == null) {
            mMapFragment = SupportMapFragment.newInstance();
            SupportMapFragment.newInstance();
            Log.d("sevibus", "Instanciando fragment MainPageFragment");
        }
        if (!mMapFragment.isAdded()) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.map, mMapFragment, "map");
            transaction.commit();
        }
    }


    public void showMapControls(boolean show) {
        if (mMap != null) {
            UiSettings settings = mMap.getUiSettings();
            settings.setCompassEnabled(show);
            settings.setMyLocationButtonEnabled(show);
            settings.setZoomControlsEnabled(show);
            ((HomeActivity) getActivity()).lockMapOptions(!show);
            mShowInterface = show;
        } else {
            Debug.registerHandledException(new IllegalStateException("Aún no está creado el mapa, so capullo"));
        }
    }

    @Override
    public void updateLocation(Location location) {
        if (location != null && !mShowInterface) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        }
    }

    private void asociarOpciones(boolean activar) {
        if (activar) {
            ((HomeActivity) getActivity()).getMapOptions().setMapa(mMap);
        } else {
            ((HomeActivity) getActivity()).getMapOptions().releaseMapa();
        }
    }
}
