package com.sloy.sevibus.resources;

import com.google.common.base.Optional;
import com.sloy.sevibus.ui.SevibusUser;

public class Session {

    private SevibusUser sevibusUser;

    public Optional<SevibusUser> getUser() {
        return Optional.fromNullable(sevibusUser);
    }

    public void setUser(SevibusUser sevibusUser) {
        this.sevibusUser = sevibusUser;
    }
}
