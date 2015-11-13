package com.sloy.sevibus.ui.adapters;

import android.content.Context;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.sloy.sevibus.R;
import com.sloy.sevibus.model.TweetHolder;
import com.squareup.picasso.Picasso;
import java.util.List;

public class TwitterAdapter extends BaseAdapter {

    public static final String DATE_FORMAT = "dd MMM k:mm";

    private List<TweetHolder> mListTweets;
    private Context mCtx;

    public TwitterAdapter(Context context, List<TweetHolder> tweets) {
        mCtx = context;
        mListTweets = tweets;
    }

    public void setTweets(List<TweetHolder> tweets) {
        mListTweets = tweets;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mListTweets != null) {
            return mListTweets.size();
        } else {
            return 0;
        }
    }

    @Override
    public TweetHolder getItem(int position) {
        return mListTweets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TweetHolder th = getItem(position);
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(mCtx).inflate(R.layout.list_item_tweet, parent, false);
        }

        TextView nombre = (TextView) v.findViewById(R.id.item_novedades_twitter_nombre);
        TextView texto = (TextView) v.findViewById(R.id.item_novedades_twitter_texto);
        ImageView avatar = (ImageView) v.findViewById(R.id.item_novedades_twitter_avatar);

        texto.setText(th.getTexto());
        nombre.setText(Html.fromHtml(String.format("<b>@%1s</b> - %2s", th.getUsername(), DateFormat.format(DATE_FORMAT, th.getFecha()))));
        Picasso.with(mCtx).load(th.getAvatarUrl()).into(avatar);


        if (th.isNuevo()) {
            texto.setTypeface(null, 1);
        } else {
            texto.setTypeface(null, 0);
        }
        return v;
    }
}
