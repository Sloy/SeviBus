package com.sloy.sevibus.ui;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.sloy.sevibus.R;

import io.palaima.debugdrawer.DebugView;
import io.palaima.debugdrawer.module.BuildModule;
import io.palaima.debugdrawer.module.DeviceModule;
import io.palaima.debugdrawer.module.SettingsModule;

public class DebugAppContainer implements AppContainer {

    private DrawerLayout drawerLayout;
    private ViewGroup debugDrawer;
    private ViewGroup content;

    @Override
    public ViewGroup get(Activity activity) {
        ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
        LayoutInflater.from(activity).inflate(R.layout.debug_activity_frame, rootView, true);

        drawerLayout = (DrawerLayout) activity.findViewById(R.id.debug_drawer_layout);
        content = (ViewGroup) activity.findViewById(R.id.debug_content);
        debugDrawer = (ViewGroup) activity.findViewById(R.id.debug_drawer);

        final DebugView debugView = new DebugView(activity);
        debugView.init(
          new DeviceModule(activity),
          new BuildModule(activity),
          new SettingsModule(activity)
        );

        debugDrawer.addView(debugView);


        return content;
    }
}
