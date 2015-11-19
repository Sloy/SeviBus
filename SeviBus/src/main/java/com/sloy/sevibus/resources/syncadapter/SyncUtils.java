package com.sloy.sevibus.resources.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sloy.sevibus.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SyncUtils {

    public static final String TAG = "SeviBus Sync";
    public static final String URL_INFO = "https://dl.dropboxusercontent.com/u/1587994/SeviBus%20Data/info.json";
    public static final String ACTION_UPDATE_FINISH = "com.sloy.sevibus.update.finish";


    private static final long SYNC_FREQUENCY_SECONDS = 24 * 60 * 60;  // 48 hour
    private static final String CONTENT_AUTHORITY = BuildConfig.PROVIDER_AUTHORITY;
    private static final String PREF_SETUP_COMPLETE = "setup_complete";

    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    private static final int NET_READ_TIMEOUT_MILLIS = 15000;  // 15 seconds


    public static void createSyncAccount(Context context) {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        Account account = AuthenticatorService.getAccount();
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
            if (context.getSharedPreferences("sync", Context.MODE_MULTI_PROCESS).getBoolean("pref_update_auto", true)) {
                ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                    ContentResolver.addPeriodicSync(account, CONTENT_AUTHORITY, new Bundle(), SYNC_FREQUENCY_SECONDS);
                }
            }
            newAccount = true;
        }

        if (newAccount || !setupComplete) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
    }


    public static void triggerRefresh() {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        Account account = AuthenticatorService.getAccount();
        ContentResolver.requestSync(
                account,
                CONTENT_AUTHORITY,
                b);
    }

    public static void setSyncAutomatically(boolean syncActive) {
        ContentResolver.setSyncAutomatically(AuthenticatorService.getAccount(), CONTENT_AUTHORITY, syncActive);
        Log.i(TAG, "Auto Sync is now: " + (syncActive ? "on" : "off"));
    }

    public static InputStream downloadUrl(final URL url) throws IOException {
        Log.d(SyncUtils.TAG, "Downloading "+url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }

    public static String streamToString(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line + "\n");
        }
        stream.close();
        return stringBuilder.toString();
    }
}
