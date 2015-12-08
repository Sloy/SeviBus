package com.sloy.sevibus.resources;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.LineaWarning;
import com.sloy.sevibus.model.TweetHolder;
import com.sloy.sevibus.model.tussam.Linea;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.sloy.sevibus.bbdd.DBQueries.getTodasLineas;

public class AlertasManager {

    public static final String URL_TWEETS_SEVIBUS = "http://sevibus.sloydev.com/twitterapi/sevibus.php";
    public static final String URL_TWEETS_TUSSAM = "http://sevibus.sloydev.com/twitterapi/tussam.php";
    public static final String PREF_ALERTS = "alertas";
    private static final String PREF_TIMESTAMP_TUSSAM = "timestamp_tussam";
    private static final String PREF_TIMESTAMP_SEVIBUS = "timestamp_sevibus";
    private static final long THRESHOLD_TUSSAM = 5 * 60 * 1000; //5 minutos
    private static final long THRESHOLD_SEVIBUS = 60 * 60 * 1000; //1 hora

    public static List<TweetHolder> getAllTweets(Context context, DBHelper dbHelper) throws SQLException {
        if (context == null) return null;
        if (necesitaActualizarTussam(context)) {
            try {
                Log.d("Sevibus", "actualizando Tweets Tussam");
                actualizaTweetsTussam(context, dbHelper);
            } catch (IOException | JSONException | SQLException | ParseException e) {
                Log.e("SeviBus", "Error actualizando tweets de Tussam", e);
                Debug.registerHandledException(e);
            }
        }
        if (necesitaActualizarSevibus(context)) {
            try {
                Log.d("Sevibus", "actualizando Tweets Sevibus");
                actualizaTweetsSevibus(context, dbHelper);
            } catch (IOException | JSONException | SQLException | ParseException e) {
                Log.e("SeviBus", "Error actualizando tweets de Sevibus", e);
                Debug.registerHandledException(e);
            }
        }
        return DBQueries.getAllTweets(dbHelper);
    }

    public static List<TweetHolder> getTweetsTussam(Context context, DBHelper dbHelper) throws SQLException {
        if (context == null) return null;
        if (necesitaActualizarTussam(context)) {
            try {
                actualizaTweetsTussam(context, dbHelper);
            } catch (IOException | JSONException | SQLException | ParseException e) {
                Log.e("SeviBus", "Error actualizando tweets de Tussam", e);
                Debug.registerHandledException(e);
            }
        }
        return DBQueries.getTweetsFromTussam(dbHelper);
    }

    public static List<TweetHolder> getTweetsSevibus(Context context, DBHelper dbHelper) throws SQLException {
        if (context == null) return null;
        if (necesitaActualizarSevibus(context)) {
            try {
                actualizaTweetsSevibus(context, dbHelper);
            } catch (IOException | JSONException | SQLException | ParseException e) {
                Log.e("SeviBus", "Error actualizando tweets de Sevibus", e);
                Debug.registerHandledException(e);
            }
        }
        return DBQueries.getTweetsFromSevibus(dbHelper);
    }

    public static List<LineaWarning> getWarnings(Context context, DBHelper dbHelper, Linea linea) throws SQLException {
        if (context == null) return null;
        if (necesitaActualizarTussam(context)) {
            try {
                actualizaTweetsTussam(context, dbHelper);
            } catch (IOException | JSONException | SQLException | ParseException e) {
                Log.e("SeviBus", "Error actualizando tweets de Tussam", e);
                Debug.registerHandledException(e);
            }
        }
        return DBQueries.getLineaWarnings(dbHelper, linea.getId());
    }

    public static void invalidarTweets(Context context) {
        if(context!=null) {
            SharedPreferences.Editor edit = context.getSharedPreferences(PREF_ALERTS, Context.MODE_PRIVATE).edit();
            edit.remove(PREF_TIMESTAMP_SEVIBUS);
            edit.remove(PREF_TIMESTAMP_TUSSAM);
            edit.commit();
        }
    }

    private static boolean necesitaActualizarSevibus(Context context) {
        // Actualiza las alertas de Sevibus si han pasado 2 horas desde la última actualización
        long lastTimestamp = context.getSharedPreferences(PREF_ALERTS, Context.MODE_PRIVATE).getLong(PREF_TIMESTAMP_SEVIBUS, 0L);
        long current = System.currentTimeMillis();
        return current - lastTimestamp > THRESHOLD_SEVIBUS || lastTimestamp == 0L;
    }

    private static boolean necesitaActualizarTussam(Context context) {
        // Actualiza las alertas de Tussam si han pasado 5 minutos desde la última actualización
        if (context == null) return false;
        long lastTimestamp = context.getSharedPreferences(PREF_ALERTS, Context.MODE_PRIVATE).getLong(PREF_TIMESTAMP_TUSSAM, 0L);
        long current = System.currentTimeMillis();
        return current - lastTimestamp > THRESHOLD_TUSSAM || lastTimestamp == 0L;
    }

    private static void actualizaTweetsTussam(Context context, DBHelper dbHelper) throws IOException, JSONException, SQLException, ParseException {
        List<TweetHolder> tweetHolders = descargaTweets(URL_TWEETS_TUSSAM); // Hago esto primero para que, si peta descargando tweets, no borre los ya existentes
        DBQueries.clearLineaWarnings(dbHelper);
        DBQueries.deleteTweetsFromTussam(dbHelper);
        List<LineaWarning> warnings = generaWarnings(dbHelper, tweetHolders);
        DBQueries.saveLineaWarning(dbHelper, warnings);
        DBQueries.saveTweets(dbHelper, tweetHolders);

        context.getSharedPreferences(PREF_ALERTS, Context.MODE_PRIVATE).edit().putLong(PREF_TIMESTAMP_TUSSAM, System.currentTimeMillis()).commit();
    }

    private static void actualizaTweetsSevibus(Context context, DBHelper dbHelper) throws IOException, JSONException, SQLException, ParseException {
        List<TweetHolder> tweetHolders = descargaTweets(URL_TWEETS_SEVIBUS); // Hago esto primero para que, si peta descargando tweets, no borre los ya existentes
        DBQueries.deleteTweetsFromSevibus(dbHelper);
        DBQueries.saveTweets(dbHelper, tweetHolders);

        context.getSharedPreferences(PREF_ALERTS, Context.MODE_PRIVATE).edit().putLong(PREF_TIMESTAMP_SEVIBUS, System.currentTimeMillis()).commit();
    }

    private static List<TweetHolder> descargaTweets(String url) throws IOException, JSONException, ParseException {
        List<TweetHolder> result = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
        dateFormat.setLenient(true);
        String rawResult = StuffProvider.getStringDownloader().download(url);
        JSONArray resultArray = new JSONArray(rawResult);
        for (int i = 0; i < resultArray.length(); i++) {
            JSONObject t = resultArray.getJSONObject(i);
            TweetHolder tweetHolder = new TweetHolder(t.getLong("id"), t.getString("text"), t.getString("author"), t.getString("username"), t.getString("avatar"), dateFormat.parse(t.getString("date")), false);
            result.add(tweetHolder);
        }
        return result;
    }

    private static List<LineaWarning> generaWarnings(DBHelper dbHelper, List<TweetHolder> tweetsTussam) throws SQLException {
        long t1 = System.currentTimeMillis();

        List<Linea> todasLineas = getTodasLineas(dbHelper);
        String[] numeros = new String[todasLineas.size()];
        for (int i = 0; i < todasLineas.size(); i++) {
            numeros[i] = todasLineas.get(i).getNumero();
        }

        // Recorre los tweets y los compara con los números de línea. Tamaño n*m!!
        List<LineaWarning> warnings = new ArrayList<>();
        for (TweetHolder tweet : tweetsTussam) {
            // Sólo cuenta si hace menos de 24 horas de la alerta
            if (tweet.getFecha().after(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))) {
                for (int i = 0; i < numeros.length; i++) {
                    String numero = numeros[i];
                    if (tweet.getTexto().contains(numero)) {
                        warnings.add(new LineaWarning(tweet, todasLineas.get(i)));
                    }
                }
            }
        }

        long t2 = System.currentTimeMillis();
        Log.d("SeviBus", "Tiempo generando warnings: " + (t2 - t1) + " ms");
        return warnings;
    }

}
