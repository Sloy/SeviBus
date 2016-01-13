package com.sloydev.retrofitendpointmodule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import retrofit.Endpoint;

public class EndpointAdapter extends BaseAdapter {

    private final List<Endpoint> endpoints;

    public EndpointAdapter(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public int getCount() {
        return endpoints.size();
    }

    @Override
    public Endpoint getItem(int position) {
        return endpoints.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dd_debug_drawer_module_endpoint_spinner_item, parent, false);
        }
        Endpoint item = getItem(position);
        TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
        tv.setText(item.getName());
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        }
        Endpoint item = getItem(position);
        TextView nameTv = (TextView) convertView.findViewById(android.R.id.text1);
        TextView valueTv = (TextView) convertView.findViewById(android.R.id.text2);
        nameTv.setText(item.getName());
        if (item.getUrl() != null) {
            valueTv.setText(item.getUrl());
        } else {
            valueTv.setText("<custom url>");
        }
        return convertView;
    }
}
