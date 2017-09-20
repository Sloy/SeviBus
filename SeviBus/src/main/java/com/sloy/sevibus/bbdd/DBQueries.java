package com.sloy.sevibus.bbdd;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.sloy.sevibus.model.tussam.Bonobus;
import com.sloy.sevibus.model.tussam.Reciente;

import java.sql.SQLException;
import java.util.List;

@Deprecated
public class DBQueries {

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
