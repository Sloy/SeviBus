package com.sloy.sevibus.resources.datasource;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.sloy.sevibus.model.tussam.Favorita;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;

public class FirebaseFavoritaDataSource implements FavoritaDataSource {

    private final Firebase firebaseFavoritas;

    public FirebaseFavoritaDataSource(Firebase firebase) {
        this.firebaseFavoritas = firebase.child("favoritas");
    }

    @Override
    public Observable<List<Favorita>> getFavoritas() {
        return Observable.create(new Observable.OnSubscribe<List<Favorita>>() {
            @Override
            public void call(Subscriber<? super List<Favorita>> subscriber) {
                firebaseFavoritas.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }
                        if (dataSnapshot.getValue() != null) {
                            Log.d("FIREBASE", "dataChange: " + dataSnapshot.getValue().toString());
                            Map<Integer, Favorita> favoritasMap = dataSnapshot.getValue(new GenericTypeIndicator<Map<Integer, Favorita>>() {
                            });
                            subscriber.onNext(new ArrayList<>(favoritasMap.values()));
                        }
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }
                        Log.e("FIREBASE", "error: " + firebaseError.getMessage());
                        subscriber.onError(firebaseError.toException());
                    }
                });
            }
        })
          .flatMap(Observable::from)
          .toSortedList((f1, f2) -> Integer.compare(f1.getOrden(), f2.getOrden()));
    }

    @Override
    public Observable<Void> saveFavorita(Favorita favorita) {
        return Observable.defer(() -> {
            String paradaKey = favorita.getParadaAsociada().getNumero().toString();
            firebaseFavoritas.child(paradaKey)
              .setValue(favorita);
            return Observable.empty();
        });
    }

    @Override
    public Observable<Favorita> getFavoritaById(Integer idParada) {
        return getFavoritas()
          .flatMap(Observable::from)
          .filter(favorita -> favorita.getParadaAsociada().getNumero().equals(idParada))
          .take(1);
    }

    @Override
    public Observable<Void> deleteFavorita(Integer idParada) {
        return Observable.defer(() -> {
            firebaseFavoritas.child(idParada.toString())
              .removeValue();
            return Observable.empty();
        });
    }

    @Override
    public Observable<Void> saveFavoritas(List<Favorita> favoritas) {
        return Observable.from(favoritas)
          .flatMap(this::saveFavorita);
    }
}
