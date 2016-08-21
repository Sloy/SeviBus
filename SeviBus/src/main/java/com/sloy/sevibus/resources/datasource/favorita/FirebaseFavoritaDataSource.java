package com.sloy.sevibus.resources.datasource.favorita;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;

public class FirebaseFavoritaDataSource implements FavoritaDataSource {

    private final FirebaseDatabase firebase;
    private final UserDataSource userDataSource;

    public FirebaseFavoritaDataSource(FirebaseDatabase firebase, UserDataSource userDataSource) {
        this.firebase = firebase;
        this.userDataSource = userDataSource;
    }

    @Override
    public Observable<List<Favorita>> getFavoritas() {
        return favoritasFirebaseNode(firebase, userDataSource)
          .flatMap(this::observeSingleValue)
          .filter(snapshot -> snapshot.getValue() != null)
          .map(snapshot -> snapshot.getValue(new FavoritaMapFirebaseTypeIndicator()))
          .map(integerFavoritaMap -> new ArrayList<>(integerFavoritaMap.values()))
          .flatMap(Observable::from)
          .toSortedList((f1, f2) -> Integer.valueOf(f1.getOrden()).compareTo(f2.getOrden()));
    }

    @Override
    public Observable<Favorita> saveFavorita(Favorita favorita) {
        return favoritasFirebaseNode(firebase, userDataSource)
          .flatMap(favsNode -> {
              String paradaKey = favorita.getParadaAsociada().getNumero().toString();
              favsNode.child(paradaKey).setValue(favorita);
              return Observable.just(favorita);
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
    public Observable<Integer> deleteFavorita(Integer idParada) {
        return favoritasFirebaseNode(firebase, userDataSource)
          .flatMap(favsNode -> {
              favsNode.child(idParada.toString()).removeValue();
              return Observable.just(idParada);
          });
    }

    @Override
    public Observable<List<Favorita>> saveFavoritas(List<Favorita> favoritas) {
        return Observable.from(favoritas)
          .flatMap(this::saveFavorita)
          .flatMap(__ -> Observable.just(favoritas));
    }

    @Override
    public Observable<List<Favorita>> replaceFavoritas(List<Favorita> favoritas) {
        return getFavoritas()
          .flatMap(Observable::from)
          .map(favorita -> favorita.getParadaAsociada().getNumero())
          .flatMap(this::deleteFavorita)
          .defaultIfEmpty(0)
          .flatMap(__ -> saveFavoritas(favoritas));
    }

    private Observable<DatabaseReference> favoritasFirebaseNode(FirebaseDatabase firebase, UserDataSource userDataSource) {
        return Observable.zip(
          Observable.just(firebase.getReference()),
          userDataSource.getCurrentUser()
            .switchIfEmpty(Observable.error(new AuthException("Empty current user"))),
          (rootNode, user) -> rootNode.child(user.getId()))
          .map(authNode -> authNode.child("favoritas"));
    }

    private Observable<DataSnapshot> observeSingleValue(DatabaseReference firebaseNode) {
        return Observable.create(new Observable.OnSubscribe<DataSnapshot>() {
            @Override
            public void call(Subscriber<? super DataSnapshot> subscriber) {
                firebaseNode.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }
                        subscriber.onNext(dataSnapshot);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("FIREBASE", "error: " + databaseError.getMessage());
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }
                        subscriber.onError(databaseError.toException());

                    }
                });
            }
        });
    }

    private class FavoritaMapFirebaseTypeIndicator extends GenericTypeIndicator<Map<String, Favorita>> {
    }

}
