package com.sloy.sevibus.ui.activities;

import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sloy.sevibus.R;

public class BaseToolbarActivity extends BaseActivity {

    private ViewGroup containerView;

    public Toolbar getToolbar() {
        return toolbar;
    }

    private Toolbar toolbar;

    @Override public void setContentView(int layoutResID) {
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        View toolbarDecorator = LayoutInflater.from(this).inflate(R.layout.toolbar_decorator, root, true);
        containerView = (ViewGroup) toolbarDecorator.findViewById(R.id.action_bar_activity_content);

        LayoutInflater.from(this).inflate(layoutResID, containerView, true);

        toolbar = (Toolbar) toolbarDecorator.findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public ViewGroup getContainerView() {
        return containerView;
    }

}
