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
import java.util.Random;

/**
 * Created by rafa on 01/09/13.
 */
public class MapaControllerFragment extends BaseDBFragment implements ILocationSensitiveFragment, LoaderManager.LoaderCallbacks<List<BusLocation>> {

    private static final String LOADER_EXTRA_LINEA = "linea";
    private static final long TIEMPO_ACTUALIZACION_BUSES = 30 * 1000; //TODO Ajustable en preferencias
    private static final String PREF_SHOW_WARNING_BUSES = "buses_warn";

    private static final String SCREEN_NAME = "Mapa";

    private CheckBox mCheckCercanas, mCheckFavoritas, mCheckBuses, mCheckSatelite;
    private ViewGroup mLineasContainer;
    private View mLineasAddButton;

    private LineaColorSelector mColorSelector;
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
            mCurrentConfig = new ConfigWraper(); // Default config
            //TODO preferencias guardadas?
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

        // Crea el (o los) LayerManager
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

    /**
     * Llamar este método cuando queramos controlar un mapa con el controlador.
     *
     * @param mapa
     */
    public void setMapa(GoogleMap mapa) {
        mColorSelector = new LineaColorSelector();
        mMapaAsociado = mapa;
        mLayerManager.setMap(mapa);
        //actualiza el estado del mapa según las opciones actuales
        applyConfig();
    }

    public void releaseMapa() {
        mMapaAsociado = null;
        mLayerManager.releaseMap();
        detieneSeguimientoBuses();
        mBusesLayers.clear(); // Vacía las capas de buses, para evitar que al salir y volver al mapa haya cargas duplicadas.
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

        mLineasContainer.removeAllViews(); //limpia primero, porsiaca
        ArrayList<Integer> lineasMostradas = new ArrayList<Integer>(mCurrentConfig.lineasMostradas);
        mCurrentConfig.lineasMostradas.clear(); // La limpio antes de añadir las líneas, porque addLinea() ya las mete de nuevo
        //TODO hacer todo en un método para que no se líe parda
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
            //TODO ajustar mapa a la línea
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
        //TODO vista propia?
        assert viewLinea != null;
        ((TextView) viewLinea.findViewById(R.id.item_mapa_opciones_linea_text)).setText(String.format("Línea %1s: %2s", l.getNumero(), l.getNombre()));
        ImageButton boton = (ImageButton) viewLinea.findViewById(R.id.item_mapa_opciones_linea_eliminar);
        int colorLinea = l.getColorInt();
        LineaColorSelector.colorearTodo(boton.getDrawable().mutate(), colorLinea);
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeLinea(id, viewLinea);
            }
        });
        mLineasContainer.addView(viewLinea);

        // Carga en el mapa
        Drawable iconoLinea = LineaColorSelector.colorearSuperficial(getResources().getDrawable(R.drawable.marker_template).mutate(), colorLinea);
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

    //TODO repensar la interfaz de este método... Debería pasarle sólo el id, y él se encargue de buscar la vista...
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
        mColorSelector.releaseColor(id);
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
        // Creo el bundle con el número de la línea, me hará falta para hacer la petición
        Bundle info = new Bundle();
        info.putString(LOADER_EXTRA_LINEA, l.getNumero());
        // Compruebo si existe el loader. Si es así lo reinicio, si no lo creo
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
            // Loaders may be used across multiple Activitys (assuming they aren't
            // bound to the LoaderManager), so NEVER hold a reference to the context
            // directly. Doing so will cause you to leak an entire Activity's context.
            // The superclass constructor will store a reference to the Application
            // Context instead, and can be retrieved with a call to getContext().
            super(context);

            this.linea = linea;
        }


        /****************************************************/
        /** (1) A task that performs the asynchronous load **/
        /**
         * ************************************************
         */
        @Override
        public List<BusLocation> loadInBackground() {
            // This method is called on a background thread and should generate a
            // new set of data to be delivered back to the client.
            try {
                return BusLocation.getBuses(linea);
            } catch (ServerErrorException e) {
                Log.e("SeviBus Mapa", "Error cargando buses de la línea " + linea);
                return null;
            }
        }

        /********************************************************/
        /** (2) Deliver the results to the registered listener **/
        /**
         * ****************************************************
         */

        @Override
        public void deliverResult(List<BusLocation> data) {
            if (isReset()) {
                // The Loader has been reset; ignore the result and invalidate the data.
                releaseResources(data);
                return;
            }

            // Hold a reference to the old data so it doesn't get garbage collected.
            // We must protect it until the new data has been delivered.
            List<BusLocation> oldData = mData;
            mData = data;

            if (isStarted()) {
                // If the Loader is in a started state, deliver the results to the
                // client. The superclass method does this for us.
                super.deliverResult(data);
            }

            // Invalidate the old data as we don't need it any more.
            if (oldData != null && oldData != data) {
                releaseResources(oldData);
            }
        }

        /*********************************************************/
        /** (3) Implement the Loader’s state-dependent behavior **/
        /**
         * *****************************************************
         */

        @Override
        protected void onStartLoading() {
            if (mData != null) {
                // Deliver any previously loaded data immediately.
                deliverResult(mData);
            }

            // Begin monitoring the underlying data source.
            /*if (mObserver == null) {
                mObserver = new SampleObserver();
                // TODO: register the observer
            }*/

            if (takeContentChanged() || mData == null) {
                // When the observer detects a change, it should call onContentChanged()
                // on the Loader, which will cause the next call to takeContentChanged()
                // to return true. If this is ever the case (or if the current data is
                // null), we force a new load.
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            // The Loader is in a stopped state, so we should attempt to cancel the
            // current load (if there is one).
            cancelLoad();

            // Note that we leave the observer as is. Loaders in a stopped state
            // should still monitor the data source for changes so that the Loader
            // will know to force a new load if it is ever started again.
        }

        @Override
        protected void onReset() {
            // Ensure the loader has been stopped.
            onStopLoading();

            // At this point we can release the resources associated with 'mData'.
            if (mData != null) {
                releaseResources(mData);
                mData = null;
            }

            // The Loader is being reset, so we should stop monitoring for changes.
            /*if (mObserver != null) {
                mObserver = null;
            }*/
        }

        @Override
        public void onCanceled(List<BusLocation> data) {
            // Attempt to cancel the current asynchronous load.
            super.onCanceled(data);

            // The load has been canceled, so we should release the resources
            // associated with 'data'.
            releaseResources(data);
        }

        private void releaseResources(List<BusLocation> data) {
            // For a simple List, there is nothing to do. For something like a Cursor, we
            // would close it in this method. All resources associated with the Loader
            // should be released here.
        }
    }

    private static class LineaColorSelector {

        private Random random;
        private SparseArray<Color> coloresUsados;
        private ArrayList<Color> coloresDisponibles;

        public LineaColorSelector() {
            random = new Random();
            int length = Color.values().length;
            coloresUsados = new SparseArray<Color>(length);
            coloresDisponibles = new ArrayList<Color>(length);
            for (int i = 0; i < length; i++) {
                coloresDisponibles.add(Color.values()[i]);
            }
        }

        public enum Color {
            NARANJA(0xffFF8800), MORADO(0xff9933CC), VERDE(0xff669900), AZUL(0xff0099CC),;

            private int colorValue;

            Color(int colorValue) {
                this.colorValue = colorValue;
            }

            public int getColorValue() {
                return colorValue;
            }
        }

        public Color getRandomColor() {
            Color[] colors = Color.values();
            int i = random.nextInt(colors.length);
            return colors[i];
        }

        /**
         * Asumimos que no se puede cargar una línea ya usada.
         *
         * @param lineaID
         * @return
         */
        public Color getNextRandomColor(int lineaID) {
            if (coloresDisponibles.size() > 0) {
                int i = random.nextInt(coloresDisponibles.size());
                Color color = coloresDisponibles.get(i);
                coloresDisponibles.remove(i);
                coloresUsados.put(lineaID, color);
                return color;
            } else {
                return getRandomColor();
            }
        }

        public void releaseColor(int lineaID) {
            Color color = coloresUsados.get(lineaID);
            if (color != null) {
                coloresUsados.remove(lineaID);
                coloresDisponibles.add(color);
            }
        }

        public static Drawable colorearTodo(Drawable drawable, Color color) {
            drawable.setColorFilter(color.getColorValue(), PorterDuff.Mode.SRC_ATOP);
            return drawable;
        }

        public static Drawable colorearTodo(Drawable drawable, int color) {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            return drawable;
        }

        public static Drawable colorearSuperficial(Drawable drawable, Color color) {
            drawable.setColorFilter(color.getColorValue(), PorterDuff.Mode.MULTIPLY);
            return drawable;
        }

        public static Drawable colorearSuperficial(Drawable drawable, int color) {
            drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            return drawable;
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
