package com.sloy.sevibus.resources;


import android.location.Location;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

public class LocationProvider {

    private final Subject<Location, Location> subject;

    public LocationProvider() {
        this.subject = BehaviorSubject.create();
    }

    public void sendNewLocation(Location location) {
        subject.onNext(location);
    }

    public Observable<Location> observe() {
        return subject;
    }
}
