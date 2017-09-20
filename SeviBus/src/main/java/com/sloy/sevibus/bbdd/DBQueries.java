package com.sloy.sevibus.bbdd;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.sloy.sevibus.model.tussam.Bonobus;
import com.sloy.sevibus.model.tussam.Linea;
import com.sloy.sevibus.model.tussam.ParadaSeccion;
import com.sloy.sevibus.model.tussam.Reciente;
import com.sloy.sevibus.model.tussam.Seccion;

import java.sql.SQLException;
import java.util.List;

@Deprecated
public class DBQueries {

    /* -- Líneas -- */
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
    public static List<Reciente> getParadasRecientes(DBHelper dbHelper) throws SQLException {
        return dbHelper.getDaoReciente().queryBuilder().orderBy("id", false).query();
    }

    public static void setParadaReciente(DBHelper dbHelper, Reciente reciente) throws SQLException {
        RuntimeExceptionDao<Reciente, Integer> daoReciente = dbHelper.getDaoReciente();
        DeleteBuilder<Reciente, Integer> delBuilder = daoReciente.deleteBuilder();
        delBuilder.where().eq("paradaAsociada_id", reciente.getParadaAsociada().getNumero());
        delBuilder.delete();

        daoReciente.create(reciente);
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
}
