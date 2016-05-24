package com.sloy.sevibus.resources.sync;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;
import com.sloy.sevibus.BuildConfig;
import com.sloy.sevibus.R;
import com.sloy.sevibus.resources.AnalyticsTracker;
import com.sloy.sevibus.resources.Debug;
import com.sloy.sevibus.resources.StuffProvider;

import java.util.Random;

import rx.schedulers.Schedulers;

public class UpdateDatabaseService extends GcmTaskService {

    public static final String ACTION_UPDATE_FINISH = "com.sloy.sevibus.update.finish";
    private static final long INTERVAL_48_HOURS_SECONDS = 48 * 60 * 60;
    private static final long INTERVAL_24_HOURS_SECONDS = 24 * 60 * 60;
    private static final long INTERVAL_12_HOURS_SECONDS = 12 * 60 * 60;
    private static final long INTERVAL_30_MINUTES_SECONDS = 30 * 60;
    private static final long INTERVAL_5_MINUTES_SECONDS = 5 * 60;
    private static final long INTERVAL_1_MINUTE_SECONDS = 60;
    private static final long INTERVAL_30_SECONDS = 30;

    private UpdateDatabaseAction updateDatabaseAction;
    private AnalyticsTracker analyticsTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        updateDatabaseAction = StuffProvider.getUpdateDatabaseAction(getApplicationContext());
        analyticsTracker = StuffProvider.getAnalyticsTracker(this);
    }

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        Log.d("Sync", "InitializeTasks!");
        setupPeriodicSync();
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.d("Sync", "onRunTask :D");
        updateDatabaseAction.update()
          .subscribeOn(Schedulers.io())
          .subscribe(aVoid -> {/*no-op*/},
            (error) -> {
                notifyTaskRun(error);
                analyticsTracker.databaseUpdatedSuccessfuly(false);
                Debug.registerHandledException(error);
            },
            () -> {
                notifyTaskRun(null);
                analyticsTracker.databaseUpdatedSuccessfuly(true);
            }
          );
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void notifyTaskRun(@Nullable Throwable error) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        NotificationManager nm = getApplicationContext().getSystemService(NotificationManager.class);

        Notification notification = new Notification.Builder(getApplicationContext())
          .setContentTitle("Sevibus Sync Run")
          .setContentText(error == null ? "Success!" : error.getMessage())
          .setPriority(Notification.PRIORITY_LOW)
          .setStyle(new Notification.BigTextStyle().bigText(error == null ? "Success!" : error.getMessage()))
          .setSmallIcon(R.drawable.ic_launcher)
          .setLights(Color.RED, 500, 5000)
          .setColor(Color.RED)
          .build();

        nm.notify(new Random().nextInt(1000), notification);
    }

    public void setupPeriodicSync() {
        PeriodicTask syncTask = new PeriodicTask.Builder()
          .setTag("sync_database")
          .setService(UpdateDatabaseService.class)
          .setPeriod(INTERVAL_48_HOURS_SECONDS)
          .setFlex(INTERVAL_24_HOURS_SECONDS)
          .setRequiresCharging(true)
          .setRequiredNetwork(Task.NETWORK_STATE_UNMETERED)
          .setPersisted(true)
          .setUpdateCurrent(true)
          .build();

        GcmNetworkManager.getInstance(getApplicationContext()).schedule(syncTask);
    }
}
