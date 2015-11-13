package com.sloy.sevibus.resources.exceptions;

/**
 * Created by rafa on 17/07/13.
 */
public class ServerErrorException extends RuntimeException {

    public ServerErrorException() {
    }

    public ServerErrorException(String detailMessage) {
        super(detailMessage);
    }

    public ServerErrorException(Throwable throwable) {
        super(throwable);
    }
}
