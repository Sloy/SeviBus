package com.sloy.sevibus.bbdd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.sloy.sevibus.model.LineaWarning;
import com.sloy.sevibus.resources.awareness.model.ParadaVisualization;
import com.sloy.sevibus.model.TweetHolder;
import com.sloy.sevibus.model.tussam.Bonobus;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.model.tussam.ParadaSeccion;
import com.sloy.sevibus.model.tussam.Reciente;
import com.sloy.sevibus.model.tussam.Seccion;
import com.sloy.sevibus.model.tussam.TipoLinea;
import com.sloy.sevibus.resources.Debug;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class DBHelper extends OrmLiteSqliteOpenHelper {

    private static final String DB_NAME = "sevibus.db";
    private static final int DB_VERSION = 10;

    private RuntimeExceptionDao<Linea, Integer> lineaDao;
    private RuntimeExceptionDao<Seccion, Integer> seccionDao;
    private RuntimeExceptionDao<Parada, Integer> paradaDao;
    private RuntimeExceptionDao<ParadaSeccion, Integer> paradaSeccionDao;
    private RuntimeExceptionDao<TipoLinea, Integer> tipoLineaDao;
    private RuntimeExceptionDao<Favorita, Integer> favoritaDao;
    private RuntimeExceptionDao<Reciente, Integer> recienteDao;
    private RuntimeExceptionDao<Bonobus, Long> bonobusDao;
    private RuntimeExceptionDao<ParadaVisualization, Integer> paradaRequestedSnapshotDao;

    private RuntimeExceptionDao<TweetHolder, Long> tweetHolderDao;
    private RuntimeExceptionDao<LineaWarning, Long> lineaWarningDao;

    private Context mContext;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
        try {
            TableUtils.createTable(cs, Parada.class);
            TableUtils.createTable(cs, Linea.class);
            TableUtils.createTable(cs, TipoLinea.class);
            TableUtils.createTable(cs, Seccion.class);
            TableUtils.createTable(cs, ParadaSeccion.class);

            TableUtils.createTableIfNotExists(cs, Favorita.class);
            TableUtils.createTableIfNotExists(cs, Reciente.class);
            TableUtils.createTableIfNotExists(cs, Bonobus.class);
            TableUtils.createTableIfNotExists(cs, TweetHolder.class);
            TableUtils.createTableIfNotExists(cs, LineaWarning.class);
            TableUtils.createTableIfNotExists(cs, ParadaVisualization.class);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource cs, int oldVersion, int newVersion) {
        try {

            // Elimina las tablas de la versión antigua
            if (oldVersion <= 4) {
                db.execSQL("drop table if exists paradas");
                db.execSQL("drop table if exists lineas");
                db.execSQL("drop table if exists relaciones");
                db.execSQL("drop table if exists favoritas");
                db.execSQL("drop table if exists recientes");
                db.execSQL("drop table if exists tweets");
                db.execSQL("drop table if exists tweetsSevibus");
            }

            TableUtils.dropTable(cs, Parada.class, true);
            TableUtils.dropTable(cs, Linea.class, true);
            TableUtils.dropTable(cs, ParadaSeccion.class, true);
            TableUtils.dropTable(cs, TipoLinea.class, true);
            TableUtils.dropTable(cs, Seccion.class, true);

            onCreate(db, cs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateFromAssets() {
        Log.i("sevibus", "Actualizando base de datos...");
        String sqlLineas = null, sqlParadas = null, sqlSecciones = null, sqlRelaciones = null, sqlTipoLineas = null;
        InputStream in;
        BufferedReader reader;
        StringBuilder stringBuilder;
        String line;

        try {
            in = mContext.getAssets().open("lineas.sql");
            reader = new BufferedReader(new InputStreamReader(in));
            stringBuilder = new StringBuilder();
            line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            sqlLineas = stringBuilder.toString();
        } catch (IOException e) {
            Log.e("SeviBus3", "Error al actualizar la tabla de lÔøΩneas", e);
        }

        try {
            in = mContext.getAssets().open("paradas.sql");
            reader = new BufferedReader(new InputStreamReader(in));
            stringBuilder = new StringBuilder();
            line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            sqlParadas = stringBuilder.toString();
        } catch (IOException e) {
            Log.e("SeviBus3", "Error al actualizar la tabla de paradas", e);
        }

        try {
            in = mContext.getAssets().open("relaciones.sql");
            reader = new BufferedReader(new InputStreamReader(in));
            stringBuilder = new StringBuilder();
            line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            sqlRelaciones = stringBuilder.toString();
        } catch (IOException e) {
            Log.e("SeviBus3", "Error al actualizar la tabla de relaciones", e);
        }

        try {
            in = mContext.getAssets().open("secciones.sql");
            reader = new BufferedReader(new InputStreamReader(in));
            stringBuilder = new StringBuilder();
            line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            sqlSecciones = stringBuilder.toString();
        } catch (IOException e) {
            Log.e("SeviBus3", "Error al actualizar la tabla de secciones", e);
        }

        try {
            in = mContext.getAssets().open("tipolineas.sql");
            reader = new BufferedReader(new InputStreamReader(in));
            stringBuilder = new StringBuilder();
            line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            sqlTipoLineas = stringBuilder.toString();
        } catch (IOException e) {
            Log.e("SeviBus3", "Error al actualizar la tabla de TipoLinea", e);
        }

        try {
            updateManual(sqlParadas, sqlLineas, sqlSecciones, sqlRelaciones, sqlTipoLineas);
        } catch (SQLException e) {
            e.printStackTrace();
            Debug.registerHandledException(e);
        }
    }

    /**
     * Actualiza el contenido de las tablas de paradas, líneas y sus
     * relaciones con el código SQL pasado directamente por los parÔøΩmetros.
     * El código SQL debe constar de una serie de Inserts con los nuevos
     * datos.
     *
     * @param sqlParadas
     * @param sqlLineas
     * @param sqlRelaciones
     */
    public void updateManual(final String sqlParadas, final String sqlLineas, final String sqlSecciones, final String sqlRelaciones, final String sqlTipoLineas) throws SQLException {

        TableUtils.clearTable(this.getConnectionSource(), Parada.class);
        TableUtils.clearTable(this.getConnectionSource(), Linea.class);
        TableUtils.clearTable(this.getConnectionSource(), Seccion.class);
        TableUtils.clearTable(this.getConnectionSource(), ParadaSeccion.class);
        TableUtils.clearTable(this.getConnectionSource(), TipoLinea.class);

        getDaoLinea().callBatchTasks(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                for (String s : sqlLineas.split(";")) {
                    if (!TextUtils.isEmpty(s.trim())) {
                        getDaoLinea().updateRaw(s);
                    }
                }
                return null;
            }
        });
        getDaoParada().callBatchTasks(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                for (String s : sqlParadas.split(";")) {
                    if (!TextUtils.isEmpty(s.trim())) {
                        getDaoParada().updateRaw(s);
                    }
                }
                return null;
            }
        });
        getDaoSeccion().callBatchTasks(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                for (String s : sqlSecciones.split(";")) {
                    if (!TextUtils.isEmpty(s.trim())) {
                        getDaoSeccion().updateRaw(s);
                    }
                }
                return null;
            }
        });
        getDaoParadaSeccion().callBatchTasks(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                for (String s : sqlRelaciones.split(";")) {
                    if (!TextUtils.isEmpty(s.trim())) {
                        getDaoParadaSeccion().updateRaw(s);
                    }
                }
                return null;
            }
        });

        getDaoTipoLinea().callBatchTasks(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                for (String s : sqlTipoLineas.split(";")) {
                    if (!TextUtils.isEmpty(s.trim())) {
                        getDaoTipoLinea().updateRaw(s);
                    }
                }
                return null;
            }
        });

    }

    public RuntimeExceptionDao<Parada, Integer> getDaoParada() {
        if (paradaDao == null) {
            paradaDao = getRuntimeExceptionDao(Parada.class);
        }
        return paradaDao;
    }

    public RuntimeExceptionDao<Linea, Integer> getDaoLinea() {
        if (lineaDao == null) {
            lineaDao = getRuntimeExceptionDao(Linea.class);
        }
        return lineaDao;
    }

    public RuntimeExceptionDao<Seccion, Integer> getDaoSeccion() {
        if (seccionDao == null) {
            seccionDao = getRuntimeExceptionDao(Seccion.class);
        }
        return seccionDao;
    }

    public RuntimeExceptionDao<ParadaSeccion, Integer> getDaoParadaSeccion() {
        if (paradaSeccionDao == null) {
            paradaSeccionDao = getRuntimeExceptionDao(ParadaSeccion.class);
        }
        return paradaSeccionDao;
    }

    public RuntimeExceptionDao<TipoLinea, Integer> getDaoTipoLinea() {
        if (tipoLineaDao == null) {
            tipoLineaDao = getRuntimeExceptionDao(TipoLinea.class);
        }
        return tipoLineaDao;
    }

    public RuntimeExceptionDao<Favorita, Integer> getDaoFavorita() {
        if (favoritaDao == null) {
            favoritaDao = getRuntimeExceptionDao(Favorita.class);
        }
        return favoritaDao;
    }

    public RuntimeExceptionDao<Reciente, Integer> getDaoReciente() {
        if (recienteDao == null) {
            recienteDao = getRuntimeExceptionDao(Reciente.class);
        }
        return recienteDao;
    }

    public RuntimeExceptionDao<TweetHolder, Long> getDaoTweetHolder() {
        if (tweetHolderDao == null) {
            tweetHolderDao = getRuntimeExceptionDao(TweetHolder.class);
        }
        return tweetHolderDao;
    }

    public RuntimeExceptionDao<Bonobus, Long> getDaoBonobus() {
        if (bonobusDao == null) {
            bonobusDao = getRuntimeExceptionDao(Bonobus.class);
        }
        return bonobusDao;
    }

    public RuntimeExceptionDao<LineaWarning, Long> getDaoLineaWarning() {
        if (lineaWarningDao == null) {
            lineaWarningDao = getRuntimeExceptionDao(LineaWarning.class);
        }
        return lineaWarningDao;
    }

    public RuntimeExceptionDao<ParadaVisualization, Integer> getDaoParadaVisualization() {
        if (paradaRequestedSnapshotDao == null) {
            paradaRequestedSnapshotDao = getRuntimeExceptionDao(ParadaVisualization.class);
        }
        return paradaRequestedSnapshotDao;
    }

}
