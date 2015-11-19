package com.sloy.sevibus.resources.datasource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StringDownloader {

    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds
    private static final int NET_READ_TIMEOUT_MILLIS = 15000;  // 15 seconds


    public String download(String url) {
        try {
            return streamToString(downloadUrl(new URL(url)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream downloadUrl(final URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }

    private String streamToString(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        stream.close();
        return stringBuilder.toString();
    }
}
