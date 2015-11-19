package com.sloy.sevibus.resources.exceptions;

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
