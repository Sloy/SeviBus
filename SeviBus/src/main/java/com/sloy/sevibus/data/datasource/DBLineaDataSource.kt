package com.sloy.sevibus.data.datasource

import com.sloy.sevibus.bbdd.DBHelper
import com.sloy.sevibus.bbdd.DBQueries
import com.sloy.sevibus.model.tussam.Linea

class DBLineaDataSource(private val dbHelper: DBHelper) {
    fun getById(lineaId: Int) = dbHelper.daoLinea.queryForId(lineaId)
    fun getAll(): MutableList<Linea>? {
        val queryBuilder = dbHelper.daoLinea.queryBuilder()
        queryBuilder.orderBy("numero", true)

        return queryBuilder.query()
    }
    fun getByParada(paradaId: Int) = DBQueries.getLineasDeParada(dbHelper, paradaId)
}