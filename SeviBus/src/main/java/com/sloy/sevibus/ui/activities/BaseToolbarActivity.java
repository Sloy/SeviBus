package com.sloy.sevibus.ui.activities;

import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sloy.sevibus.R;

import static com.google.common.base.Preconditions.checkNotNull;

public class BaseToolbarActivity extends BaseActivity {

    private ViewGroup containerView;

    public Toolbar getToolbar() {
        return toolbar;
    }

    private Toolbar toolbar;

    @Override public void setContentView(int layoutResID) {
        ViewGroup root = getRootView();

        if (needsToolbarDecorator()) {
            View toolbarDecorator = LayoutInflater.from(this).inflate(R.layout.toolbar_decorator, root, true);
            toolbar = (Toolbar) toolbarDecorator.findViewById(R.id.toolbar_actionbar);

            containerView = (ViewGroup) toolbarDecorator.findViewById(R.id.action_bar_activity_content);
            LayoutInflater.from(this).inflate(layoutResID, containerView, true);
        } else {
            LayoutInflater.from(this).inflate(layoutResID, root, true);
            toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            checkNotNull(toolbar, "needsToolbarDecorator() es false, pero no se encontró toolbar en el layout");
            containerView = getRootView();
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    protected boolean needsToolbarDecorator() {
        return true;
    }

    public ViewGroup getContainerView() {
        return containerView;
    }

}
