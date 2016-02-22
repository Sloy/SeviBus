package com.sloy.sevibus.resources.datasource.favorita;

import android.util.Log;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.sloy.sevibus.model.tussam.Favorita;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;

public class FirebaseFavoritaDataSource implements FavoritaDataSource {

    private final Firebase firebase;
    private final UserDataSource userDataSource;

    public FirebaseFavoritaDataSource(Firebase firebase, UserDataSource userDataSource) {
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
          .toSortedList((f1, f2) -> Integer.compare(f1.getOrden(), f2.getOrden()))
          .onErrorResumeNext(throwable -> Observable.empty());
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
          .flatMap(__ -> saveFavoritas(favoritas));
    }

    /*private Observable<Firebase> favoritasFirebaseNode(Firebase firebase, UserDataSource userDataSource) {
        return Observable.zip(
          Observable.just(firebase),
          userDataSource.getCurrentUser()
            .switchIfEmpty(Observable.error(new AuthException())),
          (rootNode, user) -> rootNode.child(user.getId()))
          .map(authNode -> authNode.child("favoritas"));
    }*/

    @RxLogObservable
    private Observable<Firebase> favoritasFirebaseNode(Firebase firebase, UserDataSource userDataSource) {
        return Observable.just(firebase.getAuth())
          .filter(auth -> auth != null)
          .switchIfEmpty(Observable.error(new AuthException()))
          .map(authData -> firebase.child(authData.getUid()))
          .map(authNode -> authNode.child("favoritas"));
    }

    private Observable<DataSnapshot> observeSingleValue(Firebase firebaseNode) {
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
                    public void onCancelled(FirebaseError firebaseError) {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }
                        Log.e("FIREBASE", "error: " + firebaseError.getMessage());
                        subscriber.onError(firebaseError.toException());
                    }
                });
            }
        });
    }

    private class FavoritaMapFirebaseTypeIndicator extends GenericTypeIndicator<Map<Integer, Favorita>> {
    }

    private class AuthException extends Exception {
    }
}
