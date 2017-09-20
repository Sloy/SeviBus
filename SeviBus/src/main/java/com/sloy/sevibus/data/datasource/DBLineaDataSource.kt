package com.sloy.sevibus.data.datasource

import com.j256.ormlite.stmt.SelectArg
import com.sloy.sevibus.bbdd.DBHelper
import com.sloy.sevibus.model.tussam.Linea

class DBLineaDataSource(private val dbHelper: DBHelper) {
    fun getById(lineaId: Int) = dbHelper.daoLinea.queryForId(lineaId)
    fun getAll(): MutableList<Linea>? {
        val queryBuilder = dbHelper.daoLinea.queryBuilder()
        queryBuilder.orderBy("numero", true)

        return queryBuilder.query()
    }

    fun getByParada(paradaId: Int): List<Linea>? {
        // Selecciono las relaciones con esta parada
        val paradaseccionQb = dbHelper.daoParadaSeccion.queryBuilder()
        val paradaSelectArg = SelectArg()
        paradaSelectArg.setValue(paradaId)
        paradaseccionQb.where().eq("parada_id", paradaSelectArg)

        // Selecciono las secciones que contienen esta relación
        val seccionQb = dbHelper.daoSeccion.queryBuilder()
        seccionQb.join(paradaseccionQb)

        // Selecciono las líneas que contienen dichas secciones
        val lineaQb = dbHelper.daoLinea.queryBuilder()
        lineaQb.join(seccionQb)
        lineaQb.orderBy("numero", true)
        lineaQb.distinct()
        return lineaQb.query()
    }
}