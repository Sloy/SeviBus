package com.sloy.sevibus.resources.awareness.model;

import com.sloy.sevibus.bbdd.DBHelper;

public class DatabaseParadaVisualizationDataSource implements ParadaVisualizationDataSource {

    private final DBHelper dbHelper;

    public DatabaseParadaVisualizationDataSource(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public void saveVisualization(ParadaVisualization paradaVisualization) {
        dbHelper.getDaoParadaVisualization()
                .create(paradaVisualization);
    }
}
