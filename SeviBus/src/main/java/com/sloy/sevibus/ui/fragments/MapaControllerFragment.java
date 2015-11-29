package com.sloy.sevibus.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.sloy.sevibus.R;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.resources.BusLocation;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;
import com.sloy.sevibus.resources.maputils.BusesLayer;
import com.sloy.sevibus.resources.maputils.CercanasLayer;
import com.sloy.sevibus.resources.maputils.FavoritasLayer;
import com.sloy.sevibus.resources.maputils.Layer;
import com.sloy.sevibus.resources.maputils.LayerManager;
import com.sloy.sevibus.resources.maputils.LineaLayer;
import com.sloy.sevibus.ui.activities.LocationProviderActivity;
import com.sloy.sevibus.ui.fragments.main.ILocationSensitiveFragment;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MapaControllerFragment extends BaseDBFragment implements ILocationSensitiveFragment, LoaderManager.LoaderCallbacks<List<BusLocation>> {

    private static final String LOADER_EXTRA_LINEA = "linea";
    private static final long TIEMPO_ACTUALIZACION_BUSES = 30 * 1000;
    private static final String PREF_SHOW_WARNING_BUSES = "buses_warn";

    private CheckBox mCheckCercanas, mCheckFavoritas, mCheckBuses, mCheckSatelite;
    private ViewGroup mLineasContainer;
    private View mLineasAddButton;

    private GoogleMap mMapaAsociado;
    private LayerManager mLayerManager;

    private ConfigWraper mCurrentConfig;
    private Location mLastKnownLocation;
    private int mBusesLoading = 0;

    private SparseArray<LineaLayer> mLineasLayers;
    private SparseArray<Linea> mLineasMostradas;
    private SparseArray<Integer> mLineasColors;

    private SparseArray<BusesLayer> mBusesLayers;
    private CercanasLayer mCercanasLayer;
    private FavoritasLayer mFavoritasLayer;

    private Handler mHandler;
    private Runnable mRunnableUpdateBuses;

    public static Drawable colorearTodo(Drawable drawable, int color) {
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return drawable;
    }

    public static Drawable colorearSuperficial(Drawable drawable, int color) {
        drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        return drawable;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_options, container, false);

        mCheckCercanas = (CheckBox) v.findViewById(R.id.mapa_opciones_cercanas);
        mCheckFavoritas = (CheckBox) v.findViewById(R.id.mapa_opciones_favoritas);
        mCheckBuses = (CheckBox) v.findViewById(R.id.mapa_opciones_buses);
        mCheckSatelite = (CheckBox) v.findViewById(R.id.mapa_opciones_satelite);

        mLineasContainer = (ViewGroup) v.findViewById(R.id.mapa_opciones_lineas);
        mLineasAddButton = v.findViewById(R.id.mapa_opciones_lineas_add);

        mLineasAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeleccionaLineaDialogFragment dialog = new SeleccionaLineaDialogFragment();
                dialog.setListener(new SeleccionaLineaDialogFragment.LineaSelectedListener() {
                    @Override
                    public void onLineaSelecteded(Linea linea) {
                        addLinea(linea);
                    }
                });
                dialog.show(getChildFragmentManager(), "dialog");
            }
        });

        mCheckCercanas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setMostrarCercanas(isChecked);
            }
        });
        mCheckFavoritas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setMostrarFavoritas(isChecked);
            }
        });

        mCheckBuses.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCurrentConfig.mostrarBuses = isChecked;
                if (!isChecked) {
                    ocultarBuses();
                    detieneSeguimientoBuses();
                } else {
                    mostrarAvisoBuses();
                    cargarTodosBuses();
                    comienzaSeguimientoBuses();
                }
            }
        });

        mCheckSatelite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCurrentConfig.activadoSatelite = isChecked;
                if (mMapaAsociado != null) {
                    if (isChecked) {
                        mMapaAsociado.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    } else {
                        mMapaAsociado.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    }
                }
            }
        });


        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentConfig = ConfigWraper.fromBundle(savedInstanceState.getBundle(ConfigWraper.OPCIONES_KEY));
        } else {
            mCurrentConfig = new ConfigWraper();
        }

        mHandler = new Handler();

        mRunnableUpdateBuses = new Runnable() {
            @Override
            public void run() {
                if (mCurrentConfig.mostrarBuses) {
                    cargarTodosBuses();
                    mHandler.postDelayed(this, TIEMPO_ACTUALIZACION_BUSES);
                }
            }
        };

        if (mLayerManager == null) {
            mLayerManager = new LayerManager();
        }
        if (mLineasLayers == null) {
            mLineasLayers = new SparseArray<LineaLayer>();
        }
        if (mBusesLayers == null) {
            mBusesLayers = new SparseArray<BusesLayer>();
        }

        if (mLineasMostradas == null) {
            mLineasMostradas = new SparseArray<Linea>();
        }

        if (mLineasColors == null) {
            mLineasColors = new SparseArray<Integer>();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((LocationProviderActivity) getActivity()).suscribeForUpdates(this);
        if (mCurrentConfig.mostrarBuses) {
            comienzaSeguimientoBuses();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        ((LocationProviderActivity) getActivity()).unsuscribe(this);
        detieneSeguimientoBuses();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle(ConfigWraper.OPCIONES_KEY, ConfigWraper.toBundle(mCurrentConfig));
        super.onSaveInstanceState(outState);
    }

    public void setMapa(GoogleMap mapa) {
        mMapaAsociado = mapa;
        mLayerManager.setMap(mapa);
        applyConfig();
    }

    public void releaseMapa() {
        mMapaAsociado = null;
        mLayerManager.releaseMap();
        detieneSeguimientoBuses();
        mBusesLayers.clear(); // Evitar que al salir y volver al mapa haya cargas duplicadas.
    }

    private void applyConfig() {
        mCheckCercanas.setChecked(mCurrentConfig.mostrarCercanas);
        // Hago diferenciación si isChecked es igual o no a la configuración, porque si ya estaba activo necesito llamar a mano al metodo setMostrar_(), ya que el setCheck no lo hará
        if (mCheckFavoritas.isChecked() == mCurrentConfig.mostrarFavoritas) {
            setMostrarFavoritas(mCurrentConfig.mostrarFavoritas);
        } else {
            // Como es distinto, hago setCheck y éste se encarga de llamar al otro lado
            mCheckFavoritas.setChecked(mCurrentConfig.mostrarFavoritas);
        }

        mCheckBuses.setChecked(mCurrentConfig.mostrarBuses);
        mCheckSatelite.setChecked(mCurrentConfig.activadoSatelite);

        mLineasContainer.removeAllViews();
        ArrayList<Integer> lineasMostradas = new ArrayList<Integer>(mCurrentConfig.lineasMostradas);
        mCurrentConfig.lineasMostradas.clear();
        for (Integer id : lineasMostradas) {
            addLinea(id);
        }
    }

    private void updateTextoAddLinea() {
        int numLineas = mLineasMostradas.size();
        if (numLineas > 3) {
            ((TextView) mLineasAddButton).setText(String.format("¿Más de %d? ¿Seguro?", numLineas));
        } else {
            ((TextView) mLineasAddButton).setText(R.string.mapa_opciones_add_linea);
        }
    }

    private void mostrarAvisoBuses() {
        if (getSharedPreferences(getActivity()).getBoolean(PREF_SHOW_WARNING_BUSES, true)) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Aviso")
                    .setMessage(R.string.mapa_opciones_aviso_buses)
                    .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getSharedPreferences(getActivity()).edit().putBoolean(PREF_SHOW_WARNING_BUSES, false).commit();
                        }
                    })
                    .show();
        }
    }

    private void addLinea(final int id) {
        Linea l = DBQueries.getLineaById(getDBHelper(), id);
        addLinea(l);
    }

    private void addLinea(Linea l) {
        final int id = l.getId();
        if (mCurrentConfig.lineasMostradas.contains(id)) {
            Toast.makeText(getActivity(), "La línea " + l.getNumero() + " ya estaba en el mapa, ¿la ves?", Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentConfig.lineasMostradas.add(id);
        List<Parada> paradasDeLinea;
        try {
            paradasDeLinea = DBQueries.getParadasDeLinea(getDBHelper(), l.getId());


        } catch (SQLException e) {
            Debug.registerHandledException(getActivity(), e);
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error. Lo sentimos mucho ='(", Toast.LENGTH_SHORT).show();
            return;
        }

        // Genera La vista para el menú lateral
        final View viewLinea = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_mapa_opciones_linea, mLineasContainer, false);
        assert viewLinea != null;
        ((TextView) viewLinea.findViewById(R.id.item_mapa_opciones_linea_text)).setText(String.format("Línea %1s: %2s", l.getNumero(), l.getNombre()));
        ImageButton boton = (ImageButton) viewLinea.findViewById(R.id.item_mapa_opciones_linea_eliminar);
        int colorLinea = l.getColorInt();
        colorearTodo(boton.getDrawable().mutate(), colorLinea);
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeLinea(id, viewLinea);
            }
        });
        mLineasContainer.addView(viewLinea);

        // Carga en el mapa
        Drawable iconoLinea = colorearSuperficial(getResources().getDrawable(R.drawable.marker_parada).mutate(), colorLinea);
        LineaLayer layerLinea = new LineaLayer(iconoLinea, colorLinea, paradasDeLinea, getActivity(), getDBHelper());
        mLayerManager.addLayer(layerLinea);

        mLineasLayers.put(id, layerLinea);
        mLineasMostradas.put(id, l);
        mLineasColors.put(id, colorLinea);

        if (mCurrentConfig.mostrarBuses) {
            cargarBusesDeLinea(l);
        }
        updateTextoAddLinea();
    }

    private void removeLinea(int id, View view) {
        // Del mapa
        LineaLayer layer = mLineasLayers.get(id);
        mLayerManager.removeLayer(layer);
        mLineasLayers.delete(id);
        mLineasMostradas.delete(id);

        BusesLayer busesLayer = mBusesLayers.get(id);
        if (busesLayer != null) {
            mLayerManager.removeLayer(busesLayer);
            mBusesLayers.delete(id);
        }

        mLineasContainer.removeView(view);
        mCurrentConfig.lineasMostradas.remove(Integer.valueOf(id));
        updateTextoAddLinea();
    }


    public void setMostrarCercanas(boolean mostrar) {
        mCurrentConfig.mostrarCercanas = mostrar;
        if (mMapaAsociado == null) {
            return;
        }
        if (mostrar) {
            try {
                if (mLastKnownLocation == null) {
                    mCheckCercanas.setEnabled(false);
                    return;
                }
                List<Parada> cercanas = DBQueries.getParadasCercanas(getDBHelper(), mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), false);
                mCercanasLayer = new CercanasLayer(cercanas, getActivity(), getDBHelper());
                mLayerManager.addLayer(mCercanasLayer);
            } catch (SQLException e) {
                e.printStackTrace();
                Debug.registerHandledException(getActivity(), e);
            }
        } else {
            if (mCercanasLayer != null) {
                mLayerManager.removeLayer(mCercanasLayer);
                mCercanasLayer = null;
            }
        }
    }


    public void setMostrarFavoritas(boolean mostrar) {
        mCurrentConfig.mostrarFavoritas = mostrar;
        if (mMapaAsociado == null) {
            return;
        }
        if (mostrar) {
            try {
                List<Favorita> favoritas = DBQueries.getParadasFavoritas(getDBHelper());
                ArrayList<Parada> paradas = new ArrayList<Parada>();
                for (Favorita f : favoritas) {
                    paradas.add(f.getParadaAsociada());
                }
                mFavoritasLayer = new FavoritasLayer(paradas, getActivity(), getDBHelper());
                mLayerManager.addLayer(mFavoritasLayer);
            } catch (SQLException e) {
                e.printStackTrace();
                Debug.registerHandledException(getActivity(), e);
            }
        } else {
            if (mFavoritasLayer != null) {
                mLayerManager.removeLayer(mFavoritasLayer);
                mFavoritasLayer = null;
            }
        }
    }

    public void cargarTodosBuses() {
        for (int i = 0; i < mLineasMostradas.size(); i++) {
            Linea l = mLineasMostradas.valueAt(i);
            cargarBusesDeLinea(l);
        }
    }

    private void cargarBusesDeLinea(Linea l) {
        LoaderManager loaderManager = getLoaderManager();
        Bundle info = new Bundle();
        info.putString(LOADER_EXTRA_LINEA, l.getNumero());
        Loader<Object> loader = loaderManager.getLoader(l.getId());
        if (loader == null) {
            loaderManager.initLoader(l.getId(), info, this);
        } else {
            loaderManager.restartLoader(l.getId(), info, this);
        }
        mBusesLoading++;
        mostrarCargandoSiNecesario();
    }

    public void ocultarBuses() {
        for (int i = 0; i < mBusesLayers.size(); i++) {
            BusesLayer layer = mBusesLayers.valueAt(i);
            mLayerManager.removeLayer(layer);
        }
        mBusesLayers.clear();
    }

    private void comienzaSeguimientoBuses() {
        detieneSeguimientoBuses();
        mHandler.postDelayed(mRunnableUpdateBuses, TIEMPO_ACTUALIZACION_BUSES);
    }

    private void detieneSeguimientoBuses() {
        if (mHandler != null && mRunnableUpdateBuses != null) {
            mHandler.removeCallbacks(mRunnableUpdateBuses);
            mBusesLoading = 0;
        }
    }

    @Override
    public Loader<List<BusLocation>> onCreateLoader(int id, Bundle bundle) {
        return new BusLocationsLoader(getActivity(), bundle.getString(LOADER_EXTRA_LINEA));
    }

    @Override
    public void onLoadFinished(Loader<List<BusLocation>> loader, List<BusLocation> busLocations) {
        mBusesLoading--;
        if (!mCurrentConfig.mostrarBuses) {
            return;
        }
        if (busLocations != null) {
            int lineaId = loader.getId();
            // Si la línea ya no está en el mapa, pasa
            if (mLineasMostradas.indexOfKey(lineaId) < 0) {
                return;
            }
            // Crea el icono, coloreando el fondo y colocando el dibujo del bus encima
            Drawable iconoBus = getResources().getDrawable(R.drawable.marker_bus);
            iconoBus.setColorFilter(mLineasColors.get(lineaId), PorterDuff.Mode.MULTIPLY);
            Bitmap bmpFondo = Layer.getDrawableBitmap(iconoBus);
            Bitmap bmpBus = BitmapFactory.decodeResource(getResources(), R.drawable.marker_bus_overlay);
            Bitmap bmpIcono = Bitmap.createBitmap(bmpFondo.getWidth(), bmpFondo.getHeight(), bmpFondo.getConfig());
            Canvas canvas = new Canvas(bmpIcono);
            canvas.drawBitmap(bmpFondo, new Matrix(), null);
            canvas.drawBitmap(bmpBus, new Matrix(), null);
            // --

            BusesLayer newLayer = new BusesLayer(bmpIcono, busLocations);

            // Comprueba si ya hay una capa con esta línea, si es así la elimina primero.
            BusesLayer currentLayer = mBusesLayers.get(lineaId);
            if (currentLayer != null) {
                mLayerManager.removeLayer(currentLayer);
                mBusesLayers.remove(lineaId);
            }
            mBusesLayers.put(lineaId, newLayer);
            mLayerManager.addLayer(newLayer);
        } else {
            Log.e("SeviBus", "Buses nulos :(");
            Toast.makeText(getActivity(), "Error al cargar los buses", Toast.LENGTH_SHORT).show();
        }

        mostrarCargandoSiNecesario();
    }


    @Override
    public void onLoaderReset(Loader<List<BusLocation>> loader) {

    }

    private void mostrarCargandoSiNecesario() {
        if (mBusesLoading > 0) {
            getActivity().setProgressBarIndeterminate(true);
        } else {
            getActivity().setProgressBarIndeterminate(false);
            mBusesLoading = 0;
        }
    }

    @Override
    public void updateLocation(Location location) {
        mLastKnownLocation = location;
        mCheckCercanas.setEnabled(true);
        if (mCheckCercanas.isChecked()) {
            setMostrarCercanas(true);
        }
    }


    public static class BusLocationsLoader extends AsyncTaskLoader<List<BusLocation>> {

        private List<BusLocation> mData;

        private String linea;

        public BusLocationsLoader(Context context, String linea) {
            super(context);
            this.linea = linea;
        }

        @Override
        public List<BusLocation> loadInBackground() {
            try {
                return BusLocation.getBuses(linea);
            } catch (ServerErrorException e) {
                Log.e("SeviBus Mapa", "Error cargando buses de la línea " + linea);
                return null;
            }
        }

        @Override
        public void deliverResult(List<BusLocation> data) {
            mData = data;
            if (isStarted()) {
                super.deliverResult(data);
            }
        }

        @Override
        protected void onStartLoading() {
            if (mData != null) {
                deliverResult(mData);
            }

            if (takeContentChanged() || mData == null) {
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }

        @Override
        protected void onReset() {
            onStopLoading();
            mData = null;
        }

        @Override
        public void onCanceled(List<BusLocation> data) {
            super.onCanceled(data);
        }
    }

    private static class ConfigWraper {
        private static final String OPCIONES_KEY = "configwrapper";
        private static final String OPCIONES_FAVORITAS = "favoritas";
        private static final String OPCIONES_CERCANAS = "cercanas";
        private static final String OPCIONES_LINEAS = "lineas";
        private static final String OPCIONES_SATELITE = "satelite";
        private static final String OPCIONES_BUSES = "buses";

        boolean mostrarCercanas = true;
        boolean mostrarFavoritas = true;
        boolean activadoSatelite;
        boolean mostrarBuses;
        ArrayList<Integer> lineasMostradas = new ArrayList<Integer>();

        public static ConfigWraper fromBundle(Bundle bundle) {
            ConfigWraper config = new ConfigWraper();
            config.mostrarCercanas = bundle.getBoolean(OPCIONES_CERCANAS, false);
            config.mostrarFavoritas = bundle.getBoolean(OPCIONES_FAVORITAS, false);
            config.activadoSatelite = bundle.getBoolean(OPCIONES_SATELITE, false);
            config.mostrarBuses = bundle.getBoolean(OPCIONES_BUSES, false);
            config.lineasMostradas = bundle.getIntegerArrayList(OPCIONES_LINEAS);
            return config;
        }

        public static Bundle toBundle(ConfigWraper config) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(OPCIONES_CERCANAS, config.mostrarCercanas);
            bundle.putBoolean(OPCIONES_FAVORITAS, config.mostrarFavoritas);
            bundle.putBoolean(OPCIONES_SATELITE, config.activadoSatelite);
            bundle.putBoolean(OPCIONES_BUSES, config.mostrarBuses);
            bundle.putIntegerArrayList(OPCIONES_LINEAS, config.lineasMostradas);
            return bundle;
        }
    }
}
