package com.sloy.sevibus.resources.awareness;

import com.sloy.sevibus.resources.awareness.model.ParadaVisualization;
import com.sloy.sevibus.resources.awareness.model.ParadaVisualizationDataSource;

import java.util.Date;

import rx.Observable;
import rx.schedulers.Schedulers;

public class TrackParadaVisualizationAction {

    private final ParadaVisualizationDataSource paradaVisualizationDataSource;

    public TrackParadaVisualizationAction(ParadaVisualizationDataSource paradaVisualizationDataSource) {
        this.paradaVisualizationDataSource = paradaVisualizationDataSource;
    }

    public Observable<Void> track(Integer paradaNumero) {
        return Observable.defer(() -> {
            ParadaVisualization visualization = new ParadaVisualization(paradaNumero, new Date().getTime());
            paradaVisualizationDataSource.saveVisualization(visualization);
            return Observable.<Void>empty();
        }).subscribeOn(Schedulers.io());
    }
}