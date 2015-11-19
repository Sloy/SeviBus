package com.sloy.sevibus.resources.syncadapter;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {

    public static final String ACCOUNT_TYPE = "com.sloy.sevibus";
    public static final String ACCOUNT_NAME = "Líneas y paradas";

    private Authenticator mAuthenticator;

    public static Account getAccount() {
        final String accountName = ACCOUNT_NAME;
        return new Account(accountName, ACCOUNT_TYPE);
    }

    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}