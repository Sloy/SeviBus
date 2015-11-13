package com.sloy.sevibus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sloy.sevibus.model.tussam.Linea;

@DatabaseTable
public class LineaWarning {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private TweetHolder tweet;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Linea linea;

    public LineaWarning() {}

    public LineaWarning(TweetHolder tweet, Linea linea) {
        this.tweet = tweet;
        this.linea = linea;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TweetHolder getTweet() {
        return tweet;
    }

    public void setTweet(TweetHolder tweet) {
        this.tweet = tweet;
    }

    public Linea getLinea() {
        return linea;
    }

    public void setLinea(Linea linea) {
        this.linea = linea;
    }
}
