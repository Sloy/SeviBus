package com.sloy.sevibus.resources.actions

import android.support.v4.util.Pair
import com.sloy.sevibus.domain.model.LineaCollection
import com.sloy.sevibus.model.tussam.Linea
import com.sloy.sevibus.model.tussam.Parada
import rx.Observable


class ObtainParadasWithLineasAction(private val lineaCollection: LineaCollection) {

    fun obtain(paradas: List<Parada>): Observable<Pair<Parada, MutableList<Linea>>>? {
        return Observable.from(paradas)
                .flatMap { parada ->
                    lineaCollection.getByParada(parada.numero)
                            .toList()
                            .map { lineas -> Pair.create(parada, lineas) }
                }
    }
}