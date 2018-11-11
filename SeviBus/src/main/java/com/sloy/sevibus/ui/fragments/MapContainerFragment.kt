package com.sloy.sevibus.ui.fragments

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.sloy.sevibus.R
import com.sloy.sevibus.resources.Debug
import com.sloy.sevibus.resources.LocationProvider
import com.sloy.sevibus.ui.activities.HomeActivity
import com.sloy.sevibus.ui.activities.LocationProviderActivity
import rx.Subscription

class MapContainerFragment : BaseDBFragment() {

    private var mMap: GoogleMap? = null
    private var mMapFragment: SupportMapFragment? = null
    private var mShowInterface = true
    private val locationProvider: LocationProvider by lazy { (activity as LocationProviderActivity).locationProvider }
    private var locationSubscription: Subscription? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (arguments != null) {
            mShowInterface = arguments!!.getBoolean(SHOW_INTERFACE, true)
        }
        mMapFragment = childFragmentManager.findFragmentByTag("map") as SupportMapFragment?
                ?: SupportMapFragment.newInstance()
        if (!mMapFragment!!.isAdded) {
            childFragmentManager.beginTransaction()
                    .add(R.id.map, mMapFragment, "map")
                    .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        setUpMapIfNeeded()
        if (mMap != null) {
            showMapControls(mShowInterface)
            asociarOpciones(true)
            locationSubscription = locationProvider.observeAvailable()
                    .subscribe({ this.onLocationUpdated(it) },
                            { throwable ->
                                Debug.registerHandledException(throwable)
                                if (isAdded) {
                                    Snackbar.make(view!!, R.string.error_message_generic, Snackbar.LENGTH_SHORT).show()
                                }
                            })
        }
    }


    override fun onStop() {
        super.onStop()
        asociarOpciones(false)
        if (mShowInterface) {
            showMapControls(false)
            mShowInterface = true
        }
        if (locationSubscription != null) {
            locationSubscription!!.unsubscribe()
        }
    }

    @SuppressLint("MissingPermission")
    private fun setUpMapIfNeeded() {
        if (mMap == null) {
            mMapFragment?.getMapAsync { googleMap ->
                googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                googleMap.isMyLocationEnabled = true
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(LatLng(37.3808828009948, -5.986958742141724), 13f)))
                mMap = googleMap

            }
        }
    }


    private fun showMapControls(show: Boolean) {
        if (mMap != null) {
            val settings = mMap!!.uiSettings
            settings.isCompassEnabled = show
            settings.isMyLocationButtonEnabled = show
            settings.isZoomControlsEnabled = show
            (activity as HomeActivity).lockMapOptions(!show)
            mShowInterface = show
        } else {
            Debug.registerHandledException(IllegalStateException("Aún no está creado el mapa, so capullo"))
        }
    }

    private fun onLocationUpdated(location: Location?) {
        if (location != null && !mShowInterface) {
            val position = LatLng(location.latitude, location.longitude)
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
        }
    }

    private fun asociarOpciones(activar: Boolean) {
        if (activar) {
            (activity as HomeActivity).mapOptions.setMapa(mMap)
        } else {
            (activity as HomeActivity).mapOptions.releaseMapa()
        }
    }

    companion object {

        private const val SHOW_INTERFACE = "show_interface"

        @JvmStatic
        fun getInstance(showInterface: Boolean): MapContainerFragment {
            val arguments = Bundle()
            arguments.putBoolean(SHOW_INTERFACE, showInterface)
            val f = MapContainerFragment()
            f.arguments = arguments
            return f
        }
    }
}
