package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.resources.exceptions.ServerErrorException;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;

public class ApiErrorHandler implements ErrorHandler {

    @Override
    public Throwable handleError(RetrofitError cause) {
        return new ServerErrorException(cause);
    }
}
