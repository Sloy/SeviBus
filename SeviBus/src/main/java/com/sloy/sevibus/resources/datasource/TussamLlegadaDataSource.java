package com.sloy.sevibus.resources.datasource;

import android.util.Log;

import com.sloy.sevibus.model.Llegada;
import com.sloy.sevibus.resources.TiemposHandler;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class TussamLlegadaDataSource implements LlegadaDataSource {

    private static final String URL_SOAP_DINAMICA = "http://www.infobustussam.com:9001/services/dinamica.asmx";
    private static final String BODY_SOAP_TIEMPOS = "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><GetPasoParada xmlns=\"http://tempuri.org/\"><linea>%1s</linea><parada>%2s</parada><status>1</status></GetPasoParada></soap:Body></soap:Envelope>"; // 2.parada

    @Override
    public Llegada getLlegada(String linea, Integer parada) throws ServerErrorException {
        Llegada res;
        try {
            res = new Llegada(linea);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            TiemposHandler handler = new TiemposHandler();
            InputStream is = getTiemposInputStream(linea, parada.toString());
            if (is == null) {
                throw new ServerErrorException("Error al obtener el InputStream");
            }
            parser.parse(is, handler);
            handler.configurarLlegada(res);
        } catch (ServerErrorException e) {
            throw e;
        } catch (Exception e) {
            Log.e("sevibus", "Error desconocido", e);
            throw new ServerErrorException(e);
        }
        return res;
    }

    private InputStream getTiemposInputStream(String linea, String parada) {
        InputStream res = null;
        try {
            URL url = new URL(URL_SOAP_DINAMICA);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST");
            c.setReadTimeout(15 * 1000);
            c.setDoOutput(true);
            c.setUseCaches(false);
            c.setRequestProperty("Content-Type", "text/xml");
            c.connect();

            OutputStreamWriter wr = new OutputStreamWriter(c.getOutputStream());
            String data = String.format(BODY_SOAP_TIEMPOS, linea, parada);
            wr.write(data);
            wr.flush();

            res = c.getInputStream();
        } catch (IOException e) {
            Log.e("sevibus", "Error al obtener la fuente de los tiempos", e);
        }
        return res;

    }

}
