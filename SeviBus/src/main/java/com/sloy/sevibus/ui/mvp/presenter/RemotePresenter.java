package com.sloy.sevibus.ui.mvp.presenter;

/**
 * Created by serrodcal on 28/5/16.
 */
public class RemotePresenter implements Presenter<RemotePresenter.View> {

    private final ObtainNotificationAction obtainNotificationAction;
    private View view;

    public RemotePresenter(NotificationAction obtainNotificationAction) {
        this.obtainNotificationAction = obtainNotificationAction;
    }
0
    @Override
    public void initialize(View view) {
        this.view = view;
        view.showLoading();
        getNotification();
    }

    @Override
    public void update() {
        getNotification();
    }

    @Override
    public void pause() {

    }

    private void getNotification() {
        this.obtainNotificationAction.getNotification()
                .suscribe( notification -> {
                    view.hideLoading();
                    if(notification.exists()) {
                        view.hideEmpty();
                        view.showNotification(notification);
                    } else {
                        view.showEmpty();
                        view.hideNotification();
                    }
                });
    }

    public interface View {

        void showLoading();

        void hideLoading();

        void showEmpty();

        void hideEmpty();

        void showNotification(Notification notification);

        void hideNotification();

        void showError();
    }

}