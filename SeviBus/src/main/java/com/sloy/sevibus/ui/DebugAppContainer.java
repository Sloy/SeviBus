package com.sloy.sevibus.ui;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.sloy.sevibus.R;
import com.sloydev.retrofitendpointmodule.CustomEndpoint;
import com.sloydev.retrofitendpointmodule.EndpointModule;
import com.sloy.sevibus.resources.StuffProvider;
import com.sloydev.retrofitendpointmodule.SimpleEndpoint;

import java.util.Arrays;

import io.palaima.debugdrawer.commons.BuildModule;
import io.palaima.debugdrawer.commons.DeviceModule;
import io.palaima.debugdrawer.commons.SettingsModule;
import io.palaima.debugdrawer.view.DebugView;


public class DebugAppContainer implements AppContainer {

    private DrawerLayout drawerLayout;
    private ViewGroup debugDrawer;
    private ViewGroup content;
    private DebugView debugView;

    @Override
    public ViewGroup get(Activity activity) {
        ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
        LayoutInflater.from(activity).inflate(R.layout.debug_activity_frame, rootView, true);

        drawerLayout = (DrawerLayout) activity.findViewById(R.id.debug_drawer_layout);
        content = (ViewGroup) activity.findViewById(R.id.debug_content);
        debugDrawer = (ViewGroup) activity.findViewById(R.id.debug_drawer);

        debugView = new DebugView(activity);
        debugView.modules(
          new EndpointModule(activity, Arrays.asList(
            new SimpleEndpoint("Production", StuffProvider.PRODUCTION_API_ENDPOINT),
            new SimpleEndpoint("Staging", StuffProvider.STAGING_API_ENDPOINT),
            CustomEndpoint.get()
          )),
          new DeviceModule(activity),
          new BuildModule(activity),
          new SettingsModule(activity)
        );

        debugDrawer.removeAllViews();
        debugDrawer.addView(debugView);


        return content;
    }

    @Override
    public void onStart() {
        debugView.onStart();
    }

    @Override
    public void onStop() {
        debugView.onStop();
    }
}
