package com.sloy.sevibus.resources;


import android.location.Location;

import com.google.common.base.Optional;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

public class LocationProvider {

    private final Subject<Optional<Location>, Optional<Location>> subject;

    public LocationProvider() {
        this.subject = BehaviorSubject.create();
    }

    public void sendNewLocation(Location location) {
        subject.onNext(Optional.fromNullable(location));
    }

    public Observable<Optional<Location>> observe() {
        return subject;
    }

    public Observable<Location> observeAvailable() {
        return observe().filter(Optional::isPresent)
          .map(Optional::get);
    }

}
