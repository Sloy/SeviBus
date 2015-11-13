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


    private static final long SYNC_FREQUENCY = 24 * 60 * 60;  // 48 hour (in seconds)
    private static final String CONTENT_AUTHORITY = BuildConfig.PROVIDER_AUTHORITY;
    private static final String PREF_SETUP_COMPLETE = "setup_complete";

    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 15000;  // 15 seconds


    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    public static void createSyncAccount(Context context) {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = AuthenticatorService.getAccount();
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            if (context.getSharedPreferences("sync", Context.MODE_MULTI_PROCESS).getBoolean("pref_update_auto", true)) {
                ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
                // Recommend a schedule for automatic synchronization. The system may modify this based
                // on other scheduled syncs and network utilization.
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                    ContentResolver.addPeriodicSync(account, CONTENT_AUTHORITY, new Bundle(), SYNC_FREQUENCY);
                }
            }
            newAccount = true;
        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount || !setupComplete) {
            // triggerRefresh(); // Quita quita, cuando programas la sincronización periódiga ya lo hace solo
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
    }


    /**
     * Helper method to trigger an immediate sync ("refresh").
     * <p/>
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     * <p/>
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public static void triggerRefresh() {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        Account account = AuthenticatorService.getAccount();
        ContentResolver.requestSync(
                account,      // Sync account
                CONTENT_AUTHORITY, // Content authority
                b);                                      // Extras
    }

    public static void setSyncAutomatically(boolean syncActive) {
        ContentResolver.setSyncAutomatically(AuthenticatorService.getAccount(), CONTENT_AUTHORITY, syncActive);
        Log.i(TAG, "Auto Sync is now: " + (syncActive ? "on" : "off"));
    }

    /**
     * Given a string representation of a URL, sets up a connection and gets an input stream.
     */
    public static InputStream downloadUrl(final URL url) throws IOException {
        Log.d(SyncUtils.TAG, "Downloading "+url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
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
