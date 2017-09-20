package com.sloy.sevibus.data.datasource

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

    fun getBySeccion(seccionId: Int): MutableList<Parada> {
        val paradaSeccionQb = dbHelper.daoParadaSeccion.queryBuilder()
        val seccionSelectArg = SelectArg()
        seccionSelectArg.setValue(seccionId)
        paradaSeccionQb.where().eq("seccion_id", seccionSelectArg)

        val paradaQb = dbHelper.daoParada.queryBuilder()
        paradaQb.join(paradaSeccionQb)

        return paradaQb.query()
    }

    fun getByQuery(query: String) = DBQueries.getParadasByQuery(dbHelper, query, 50L)
}