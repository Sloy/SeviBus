package com.sloy.sevibus.model.notifications;

import com.sloydev.gallego.Optional;

/**
 * Created by serrodcal on 29/5/16.
 */
public class Notification {

    private Optional<String> message;

    public Notification(Optional<String> message) {
        this.message = message;
    }

    public void setMessage(Optional<String> message) {
        this.message = message;
    }

    public Optional<String> getMessage() {
        return message;
    }

    public boolean exist() {
        this.message.isPresent();
    }
}
