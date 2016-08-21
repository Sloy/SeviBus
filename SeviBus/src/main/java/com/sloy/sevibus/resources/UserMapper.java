package com.sloy.sevibus.resources;


import com.google.firebase.auth.FirebaseUser;
import com.sloy.sevibus.ui.SevibusUser;

public class UserMapper {

    public static SevibusUser mapFirebaseUser(FirebaseUser firebaseUser) {
        SevibusUser user = new SevibusUser();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setName(firebaseUser.getDisplayName());
        if (firebaseUser.getPhotoUrl() != null) {
            user.setPhotoUrl(firebaseUser.getPhotoUrl().toString());
        }
        return user;
    }
}
