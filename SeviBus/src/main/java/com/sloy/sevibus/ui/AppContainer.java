package com.sloy.sevibus.ui;


import android.app.Activity;
import android.view.ViewGroup;

/**
 * An indirection which allows controlling the root container used for each activity.
 */
public interface AppContainer {
    /**
     * The root {@link android.view.ViewGroup} into which the activity should place its contents.
     */
    ViewGroup get(Activity activity);

    void onStart();

    void onStop();

    /**
     * An {@link AppContainer} which returns the normal activity content view.
     */
    AppContainer DEFAULT = new AppContainer() {
        @Override
        public ViewGroup get(Activity activity) {
            return (ViewGroup) activity.findViewById(android.R.id.content);
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onStop() {
        }
    };
}