package com.sloy.sevibus.data.datasource

import com.sloy.sevibus.bbdd.DBHelper
import com.sloy.sevibus.bbdd.DBQueries
import com.sloy.sevibus.model.tussam.Parada

class DBParadaDataSource(private val dbHelper: DBHelper) {

    fun getById(paradaId: Int): Parada? {
        return dbHelper.daoParada.queryForId(paradaId)
    }

    fun getByLinea(lineaId: Int) = DBQueries.getParadasDeLinea(dbHelper, lineaId)

    fun getBySeccion(seccionId: Int) = DBQueries.getParadasDeSeccion(dbHelper, seccionId)

    fun getByQuery(query: String) = DBQueries.getParadasByQuery(dbHelper, query, 50L)
}