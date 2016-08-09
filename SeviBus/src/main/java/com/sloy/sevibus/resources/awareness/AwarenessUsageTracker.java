package com.sloy.sevibus.resources.awareness;

import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.model.ParadaRequestSnapshot;

import java.util.Date;

import rx.Observable;
import rx.schedulers.Schedulers;

public class AwarenessUsageTracker {

    private final DBHelper dbHelper;

    public AwarenessUsageTracker(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public Observable<Void> trackParadaRequested(Integer paradaNumero) {
        return Observable.defer(() -> {
            ParadaRequestSnapshot snapshot = new ParadaRequestSnapshot(paradaNumero, new Date().getTime());
            dbHelper.getDaoParadaRequestSnapshot()
                    .create(snapshot);
            return Observable.<Void>empty();
        }).subscribeOn(Schedulers.io());
    }
}