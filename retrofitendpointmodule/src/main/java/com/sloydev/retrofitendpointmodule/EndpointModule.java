package com.sloydev.retrofitendpointmodule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.jakewharton.processphoenix.ProcessPhoenix;

import java.util.List;

import io.palaima.debugdrawer.base.DebugModule;
import retrofit.Endpoint;

public class EndpointModule implements DebugModule {

    private final Activity activity;
    private final SelectableEndpoint endpoints;
    private final StringPreference selectedEndpointPreference;
    private final StringPreference customEndpointPreference;

    private Spinner endpointView;

    public static String getSelectedEndpointUrl(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("endpoint_module", Context.MODE_PRIVATE);
        return new StringPreference(preferences, "selected_endpoint").get();
    }

    public EndpointModule(Activity activity, SelectableEndpoint endpoints) {
        this(activity, endpoints, activity.getSharedPreferences("endpoint_module", Context.MODE_PRIVATE));
    }

    private EndpointModule(Activity activity, SelectableEndpoint endpoints, SharedPreferences sharedPreferences) {
        this.activity = activity;
        this.endpoints = endpoints;
        this.selectedEndpointPreference = new StringPreference(sharedPreferences, "selected_endpoint", endpoints.getDefault().getUrl());
        this.customEndpointPreference = new StringPreference(sharedPreferences, "custom_endpoint", "http://192.168.1.");
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View moduleView = inflater.inflate(R.layout.debug_drawer_module_endpoint, parent, false);
        endpointView = (Spinner) moduleView.findViewById(R.id.debug_network_endpoint);
        return moduleView;
    }

    @Override
    public void onStart() {
        final Endpoint currentEndpoint = findEndpoint(selectedEndpointPreference.get());

        EndpointAdapter endpointAdapter = new EndpointAdapter(endpoints);
        endpointView.setAdapter(endpointAdapter);

        final int currentEndpointPosition = endpoints.indexOf(currentEndpoint);
        endpointView.setSelection(currentEndpointPosition);

        endpointView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Endpoint selected = endpoints.get(position);
                if (selected != currentEndpoint) {
                    boolean isCustomEndpoint = selected.getUrl() == null;
                    if (isCustomEndpoint) {
                        Log.d("EndpointModule", "Custom network endpoint selected. Prompting for URL.");

                        showCustomEndpointDialog(currentEndpointPosition, customEndpointPreference.get());
                    } else {
                        setEndpointAndRelaunch(selected.getUrl());
                    }
                } else {
                    Log.d("EndpointModule", "Ignoring re-selection of network endpoint " + selected.getName());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public void onOpened() {

    }

    @Override
    public void onClosed() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    private void setEndpointAndRelaunch(String endpoint) {
        selectedEndpointPreference.set(endpoint);
        Toast.makeText(endpointView.getContext(), "Restarting application...", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ProcessPhoenix.triggerRebirth(endpointView.getContext());
            }
        }, 500);
    }

    private Endpoint findEndpoint(String url) {
        for (Endpoint endpoint : endpoints) {
            if (endpoint.getUrl() != null && endpoint.getUrl().equals(url)) {
                return endpoint;
            }
        }
        return CustomEndpoint.get();
    }

    private void showCustomEndpointDialog(final int originalSelection, String defaultUrl) {
        View view = LayoutInflater.from(endpointView.getContext()).inflate(R.layout.debug_drawer_module_endpoint_custom, null);
        final EditText url = (EditText) view.findViewById(R.id.debug_drawer_network_endpoint_url);
        url.setText(defaultUrl);
        url.setSelection(url.length());

        new AlertDialog.Builder(endpointView.getContext()) //
          .setTitle("Set Network Endpoint")
          .setView(view)
          .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int i) {
                  endpointView.setSelection(originalSelection);
                  dialog.cancel();
              }
          })
          .setPositiveButton("Use", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int i) {
                  String theUrl = url.getText().toString();
                  if (!TextUtils.isEmpty(theUrl)) {
                      customEndpointPreference.set(theUrl);
                      setEndpointAndRelaunch(theUrl);
                  } else {
                      endpointView.setSelection(originalSelection);
                  }
              }
          })
          .setOnCancelListener(new DialogInterface.OnCancelListener() {
              @Override
              public void onCancel(DialogInterface dialogInterface) {
                  endpointView.setSelection(originalSelection);
              }
          })
          .show();
    }

    @Override
    public void onStop() {
    }


}
