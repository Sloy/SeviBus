package com.sloy.sevibus.data.datasource

import android.util.Log
import com.j256.ormlite.stmt.SelectArg
import com.sloy.sevibus.bbdd.DBHelper
import com.sloy.sevibus.bbdd.DBQueries
import com.sloy.sevibus.model.tussam.Parada

class DBParadaDataSource(private val dbHelper: DBHelper) {

    fun getById(paradaId: Int): Parada? {
        return dbHelper.daoParada.queryForId(paradaId)
    }

    fun getByLinea(lineaId: Int): List<Parada> {
        val seccionQb = dbHelper.daoSeccion.queryBuilder()
        val lineaSelectArg = SelectArg()
        lineaSelectArg.setValue(lineaId)
        seccionQb.where().eq("linea_id", lineaSelectArg)

        val paradaSeccionQb = dbHelper.daoParadaSeccion.queryBuilder()
        paradaSeccionQb.join(seccionQb)

        val paradaQb = dbHelper.daoParada.queryBuilder()
        paradaQb.join(paradaSeccionQb)

        return paradaQb.query()
    }

    fun getBySeccion(seccionId: Int): List<Parada> {
        val paradaSeccionQb = dbHelper.daoParadaSeccion.queryBuilder()
        val seccionSelectArg = SelectArg()
        seccionSelectArg.setValue(seccionId)
        paradaSeccionQb.where().eq("seccion_id", seccionSelectArg)

        val paradaQb = dbHelper.daoParada.queryBuilder()
        paradaQb.join(paradaSeccionQb)

        return paradaQb.query()
    }

    fun getByQuery(query: String): List<Parada> {
        val arg1 = SelectArg("%$query%")
        val arg2 = SelectArg("%$query%")
        val qb = dbHelper.daoParada.queryBuilder()
        qb.limit(50)
        val where = qb.where().like("numero", arg1).or().like("descripcion", arg2)
        Log.d("Sevibus DB", where.statement)

        return where.query()
    }
}