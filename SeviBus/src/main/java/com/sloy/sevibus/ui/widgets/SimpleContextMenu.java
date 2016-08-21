package com.sloy.sevibus.ui.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.List;

import static com.chernobyl.Chernobyl.checkArgument;

public class SimpleContextMenu {

    private final Context context;
    private final String[] titles;
    private final Runnable[] callbacks;

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    private SimpleContextMenu(Context context, List<MenuAction> actions) {
        this.context = context;
        titles = new String[actions.size()];
        callbacks = new Runnable[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            MenuAction action = actions.get(i);
            titles[i] = action.title;
            callbacks[i] = action.callback;
        }
    }

    public void show() {
        new AlertDialog.Builder(context).setItems(titles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callbacks[which].run();
            }
        }).show();
    }

    public static class Builder {

        private Context context;
        private final ArrayList<MenuAction> actions;

        private Builder(Context context) {
            this.context = context;
            this.actions = new ArrayList<>();
        }

        public Builder addAction(MenuAction menuAction) {
            actions.add(menuAction);
            return this;
        }

        public Builder addAction(String title, Runnable callback) {
            return addAction(new MenuAction(title, callback));
        }

        public SimpleContextMenu build() {
            checkArgument(!actions.isEmpty(), "Can't create a menu with empty list of actions. Use addAction method first.");
            return new SimpleContextMenu(context, actions);
        }

        public void show() {
            build().show();
        }

    }

    public static class MenuAction {

        private String title;
        private Runnable callback;

        public MenuAction(String title, Runnable callback) {
            this.title = title;
            this.callback = callback;
        }
    }
}