package com.sloy.sevibus.data.datasource

import com.sloy.sevibus.bbdd.DBHelper
import com.sloy.sevibus.bbdd.DBQueries

class DBLineaDataSource(private val dbHelper: DBHelper) {
    fun getById(lineaId: Int) = DBQueries.getLineaById(dbHelper, lineaId)
    fun getAll() = DBQueries.getTodasLineas(dbHelper)
    fun getByParada(paradaId: Int) = DBQueries.getLineasDeParada(dbHelper, paradaId)
}