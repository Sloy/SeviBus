package com.sloy.sevibus.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.sloy.sevibus.R;
import com.sloy.sevibus.model.TweetHolder;
import com.sloy.sevibus.resources.AlertasManager;
import com.sloy.sevibus.ui.adapters.TwitterAdapter;
import java.sql.SQLException;
import java.util.List;

public class AlertasFragment extends BaseDBFragment {
    private static final String SCREEN_NAME = "AlertasFragment";
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
            actualizar(true);
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
        actualizar(false);
    }

    private void actualizar(boolean forceUpdate) {
        new AsyncTask<Boolean, Void, List<TweetHolder>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mList.setVisibility(View.GONE);
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected List<TweetHolder> doInBackground(Boolean... params) {
                try {
                    if (params[0]) {
                        AlertasManager.invalidarTweets(getActivity());
                    }
                    return AlertasManager.getAllTweets(getActivity(), getDBHelper());
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<TweetHolder> tweetHolders) {
                super.onPostExecute(tweetHolders);
                mProgress.setVisibility(View.GONE);
                if (tweetHolders != null) {
                    mAdapter.setTweets(tweetHolders);
                    mList.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                    mError.setVisibility(View.VISIBLE);
                }
            }
        }.execute(forceUpdate);
    }
}
