package com.sloy.sevibus.resources.awareness.bonobus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.TimeFence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.sloy.sevibus.R;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloy.sevibus.resources.actions.HasExpiringBonobusAction;
import com.sloy.sevibus.ui.activities.HomeActivity;

import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BonobusNotifier {

    private static final long TIME_8_AM = TimeUnit.HOURS.toMillis(8);
    private static final long TIME_12_PM = TimeUnit.HOURS.toMillis(12);

    private static final String BONOBUS_FENCE_KEY = "bonobusFence";

    public void checkBonobusAndNotify(Context context) {
        HasExpiringBonobusAction hasExpiringBonobusAction = StuffProvider.getHasExpiringBonobusesAction(context);
        hasExpiringBonobusAction.hasExpiringBonobus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(Boolean.TRUE::equals)
                .subscribe(__ -> {
                    showNotification(context);
                    disableFence(context);
                });
    }

    private void showNotification(Context context) {
        String message = "Ojocuidao, tienes un bonobús con menos de 1€. ¡Acuérdate de recargarlo!";
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context)
                .setContentTitle("Aviso de Bonobús")
                .setContentText("Tienes uno a punto de agotarse")
                .setStyle(new Notification.BigTextStyle().bigText(message))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_notification_default)
                .setLights(Color.RED, 500, 5000)
                .setColor(Color.RED)
                .setContentIntent(PendingIntent.getActivity(context, 1, HomeActivity.createIntentForSectionBonobus(context), 0))
                .setAutoCancel(true)
                .build();

        nm.notify(new Random().nextInt(1000), notification);
    }

    /**
     * Must be called each day to activate the notification fence
     *
     * @param context
     */
    public void enableFence(Context context) {
        GoogleApiClient client = connectGoogleClient(context);

        AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);
        AwarenessFence timeFence = TimeFence.inDailyInterval(TimeZone.getDefault(), TIME_8_AM, TIME_12_PM);

        AwarenessFence walkingAndTimeFence = AwarenessFence.and(walkingFence, timeFence);

        Awareness.FenceApi.updateFences(
                client,
                new FenceUpdateRequest.Builder()
                        .addFence(BONOBUS_FENCE_KEY, walkingAndTimeFence, PendingIntent.getBroadcast(context, 2, new Intent("com.sloy.sevibus.action.CHECK_BONOBUS_EXPIRING"), 0))
                        .build())
                .setResultCallback(status -> {
                    if (status.isSuccess()) {
                        Log.i("Fence", "Fence was successfully registered.");
                    } else {
                        Log.e("Fence", "Fence could not be registered: " + status);
                    }
                });
    }

    /**
     * Must be called after a notification is displayed to disable it until next schedule
     *
     * @param context
     */
    public void disableFence(Context context) {
        GoogleApiClient client = connectGoogleClient(context);

        Awareness.FenceApi.updateFences(
                client,
                new FenceUpdateRequest.Builder()
                        .removeFence(BONOBUS_FENCE_KEY)
                        .build())
                .setResultCallback(status -> {
                    if (status.isSuccess()) {
                        Log.i("Fence", "Fence was successfully removed.");
                    } else {
                        Log.e("Fence", "Fence could not be reomved: " + status);
                    }
                });
    }

    @NonNull
    private GoogleApiClient connectGoogleClient(Context context) {
        GoogleApiClient client = new GoogleApiClient.Builder(context)
                .addApi(Awareness.API)
                .build();
        client.connect();
        return client;
    }
}
