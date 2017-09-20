package com.sloy.sevibus.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sloy.sevibus.R;
import com.sloy.sevibus.model.TweetHolder;
import com.sloy.sevibus.ui.adapters.TwitterAdapter;

public class AlertasFragment extends BaseDBFragment {
    private ListView mList;
    private View mError;
    private View mProgress;

    private TwitterAdapter mAdapter;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.alertas, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_actualizar) {
            actualizar();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_alertas, container, false);
        assert v != null;

        mList = (ListView) v.findViewById(R.id.alertas_lista);
        mError = v.findViewById(R.id.alertas_error);
        mProgress = v.findViewById(R.id.alertas_progress);

        mAdapter = new TwitterAdapter(getActivity(), null);
        mList.setAdapter(mAdapter);

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TweetHolder tweet = mAdapter.getItem(i);
                String url = String.format("https://twitter.com/%s/status/%d", tweet.getUsername(), tweet.getId());
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));
            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        actualizar();
    }

    private void actualizar() {
        //TODO Deleted for now. Let's re-implement it, maybe
    }
}
