package com.sloy.sevibus.bbdd;

import android.util.Log;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.sloy.sevibus.model.LineaWarning;
import com.sloy.sevibus.model.TweetHolder;
import com.sloy.sevibus.model.tussam.Bonobus;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.Parada;
import com.sloy.sevibus.model.tussam.ParadaSeccion;
import com.sloy.sevibus.model.tussam.Reciente;
import com.sloy.sevibus.model.tussam.Seccion;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Deprecated
public class DBQueries {

    /* -- Líneas -- */

    @Deprecated
    public static Linea getLineaById(DBHelper dbHelper, int id) {
        return dbHelper.getDaoLinea().queryForId(id);
    }

    @Deprecated
    public static List<Linea> getTodasLineas(DBHelper dbHelper) throws SQLException {
        QueryBuilder<Linea, Integer> queryBuilder = dbHelper.getDaoLinea().queryBuilder();
        queryBuilder.orderBy("numero", true);

        return queryBuilder.query();
    }

    @Deprecated
    public static List<Linea> getLineasDeParada(DBHelper dbHelper, int parada_id) throws SQLException {
        // Selecciono las relaciones con esta parada
        QueryBuilder<ParadaSeccion, Integer> paradaseccionQb = dbHelper.getDaoParadaSeccion().queryBuilder();
        SelectArg paradaSelectArg = new SelectArg();
        paradaSelectArg.setValue(parada_id);
        paradaseccionQb.where().eq("parada_id", paradaSelectArg);

        // Selecciono las secciones que contienen esta relación
        QueryBuilder<Seccion, Integer> seccionQb = dbHelper.getDaoSeccion().queryBuilder();
        seccionQb.join(paradaseccionQb);

        // Selecciono las líneas que contienen dichas secciones
        QueryBuilder<Linea, Integer> lineaQb = dbHelper.getDaoLinea().queryBuilder();
        lineaQb.join(seccionQb);
        lineaQb.orderBy("numero", true);
        lineaQb.distinct();
        return lineaQb.query();
    }

    /* -- Paradas -- */

    @Deprecated
    public static List<Parada> getParadasDeSeccion(DBHelper dbHelper, int seccion_id) throws SQLException {
        QueryBuilder<ParadaSeccion, Integer> paradaSeccionQb = dbHelper.getDaoParadaSeccion().queryBuilder();
        SelectArg seccionSelectArg = new SelectArg();
        seccionSelectArg.setValue(seccion_id);
        paradaSeccionQb.where().eq("seccion_id", seccionSelectArg);

        QueryBuilder<Parada, Integer> paradaQb = dbHelper.getDaoParada().queryBuilder();
        paradaQb.join(paradaSeccionQb);

        return paradaQb.query();
    }

    @Deprecated //There's an action for that
    public static List<Parada> getParadasCercanas(DBHelper dbHelper, double latitud, double longitud, boolean orderByDistance) throws SQLException {

        double margen = 0.005;

        double maxLatitud = latitud + margen;
        double minLatitud = latitud - margen;
        double maxLongitud = longitud + margen;
        double minLongitud = longitud - margen;

        QueryBuilder<Parada, Integer> qb = dbHelper.getDaoParada().queryBuilder();
        Where<Parada, Integer> where = qb.where().lt("latitud", maxLatitud).and().gt("latitud", minLatitud).and().lt("longitud", maxLongitud).and().gt("longitud", minLongitud);
        Log.d("Sevibus", "Query cercanas -> " + where.getStatement());
        List<Parada> res = where.query();

        if (orderByDistance) {
            Collections.sort(res, new ParadaDistanciaComparator(latitud, longitud));
        }
        return res;

    }

    @Deprecated
    public static List<Parada> getParadasByQuery(DBHelper dbHelper, String query, long limit) throws SQLException {
        SelectArg arg1 = new SelectArg("%" + query + "%");
        SelectArg arg2 = new SelectArg("%" + query + "%");
        QueryBuilder<Parada, Integer> qb = dbHelper.getDaoParada().queryBuilder();
        qb.limit(limit);
        Where<Parada, Integer> where = qb.where().like("numero", arg1).or().like("descripcion", arg2);
        Log.d("Sevibus DB", where.getStatement());

        return where.query();
    }

    public static List<Reciente> getParadasRecientes(DBHelper dbHelper) throws SQLException {
        return dbHelper.getDaoReciente().queryBuilder().orderBy("id", false).query();
    }

    public static void setParadaReciente(DBHelper dbHelper, Reciente reciente) throws SQLException {
        RuntimeExceptionDao<Reciente,Integer> daoReciente = dbHelper.getDaoReciente();
        DeleteBuilder<Reciente,Integer> delBuilder = daoReciente.deleteBuilder();
        delBuilder.where().eq("paradaAsociada_id", reciente.getParadaAsociada().getNumero());
        delBuilder.delete();

        daoReciente.create(reciente);
    }

    /* -- Twitter -- */

    public static List<TweetHolder> getAllTweets(DBHelper dbHelper) throws SQLException {
        QueryBuilder<TweetHolder, Long> queryBuilder = dbHelper.getDaoTweetHolder().queryBuilder();
        queryBuilder.orderBy("fecha", false);
        return queryBuilder.query();
    }

    public static List<TweetHolder> getTweetsFromSevibus(DBHelper dbHelper) throws SQLException {
        List<TweetHolder> res = null;

        QueryBuilder<TweetHolder, Long> tweetsQb = dbHelper.getDaoTweetHolder().queryBuilder();
        tweetsQb.where().like("username", "sevibus");
        tweetsQb.orderBy("fecha", false);
        res = tweetsQb.query();
        return res;
    }

    public static List<TweetHolder> getTweetsFromTussam(DBHelper dbHelper) throws SQLException {
        List<TweetHolder> res = null;
        QueryBuilder<TweetHolder, Long> tweetsQb = dbHelper.getDaoTweetHolder().queryBuilder();
        tweetsQb.where().like("username", "ayto_tussam");
        tweetsQb.orderBy("fecha", false);
        res = tweetsQb.query();
        return res;
    }

    public static void deleteTweetsFromTussam(DBHelper dbHelper) throws SQLException {
        DeleteBuilder<TweetHolder, Long> deleteBuilder = dbHelper.getDaoTweetHolder().deleteBuilder();
        deleteBuilder.where().like("username", "ayto_tussam");
        deleteBuilder.delete();
    }

    public static void deleteTweetsFromSevibus(DBHelper dbHelper) throws SQLException {
        DeleteBuilder<TweetHolder, Long> deleteBuilder = dbHelper.getDaoTweetHolder().deleteBuilder();
        deleteBuilder.where().like("username", "sevibus");
        deleteBuilder.delete();
    }

    public static void saveTweets(DBHelper dbHelper, List<TweetHolder> tweets) {
        for (TweetHolder t : tweets) {
            dbHelper.getDaoTweetHolder().createOrUpdate(t);
        }
    }

    public static List<LineaWarning> getAllLineaWarnings(DBHelper dbHelper) {
        return dbHelper.getDaoLineaWarning().queryForAll();
    }

    public static List<LineaWarning> getLineaWarnings(DBHelper dbHelper, int linea_id) throws SQLException {
        QueryBuilder<LineaWarning, Long> paradaSeccionQb = dbHelper.getDaoLineaWarning().queryBuilder();
        SelectArg seccionSelectArg = new SelectArg();
        seccionSelectArg.setValue(linea_id);
        paradaSeccionQb.where().eq("linea_id", seccionSelectArg);
        return paradaSeccionQb.query();
    }

    public static void saveLineaWarning(DBHelper dbHelper, List<LineaWarning> warnings) {
        for (LineaWarning warning : warnings) {
            dbHelper.getDaoLineaWarning().create(warning);
        }
    }

    public static void clearLineaWarnings(DBHelper dbHelper) throws SQLException {
        DeleteBuilder<LineaWarning, Long> deleteBuilder = dbHelper.getDaoLineaWarning().deleteBuilder();
        deleteBuilder.delete();
    }

    /* -- Bonobús -- */
    public static List<Bonobus> getBonobuses(DBHelper dbHelper) {
        return dbHelper.getDaoBonobus().queryForAll();
    }

    public static void saveBonobus(DBHelper dbHelper, Bonobus bonobus) {
        dbHelper.getDaoBonobus().createOrUpdate(bonobus);
    }

    public static void deleteBonobus(DBHelper dbHelper, Bonobus bonobus) {
        dbHelper.getDaoBonobus().delete(bonobus);
    }

    private static class ParadaDistanciaComparator implements Comparator<Parada> {

        private int mLatitud;
        private int mLongitud;

        private ParadaDistanciaComparator(double latitud, double longitud) {
            mLatitud = (int) (latitud * 1E6);
            mLongitud = (int) (longitud * 1E6);
        }

        @Override
        public int compare(Parada p1, Parada p2) {
            int distanciaP1 = distancia(p1);
            int distanciaP2 = distancia(p2);
            return distanciaP1 - distanciaP2;
        }

        private int distancia(Parada parada) {
            int distanciaX = Math.abs(mLatitud - (int) (parada.getLatitud() * 1E6));
            int distanciaY = Math.abs(mLongitud - (int) (parada.getLongitud() * 1E6));
            return distanciaX + distanciaY;
        }
    }

}
