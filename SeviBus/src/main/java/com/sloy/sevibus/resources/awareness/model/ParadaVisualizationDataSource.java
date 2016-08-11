package com.sloy.sevibus.resources.awareness.model;

import rx.Observable;

public interface ParadaVisualizationDataSource {

    void saveVisualization(ParadaVisualization requestSnapshot);

    Observable<ParadaVisualization> obtainVisualizations();

}
