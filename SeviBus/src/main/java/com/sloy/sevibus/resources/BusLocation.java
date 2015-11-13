package com.sloy.sevibus.resources;

import android.util.Log;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class BusLocation {

    public int xcoord;
    public int ycoord;

    public BusLocation(int x, int y) {
        xcoord = x;
        ycoord = y;
    }

    public BusLocation() {
    }


    private static final String URL_SOAP_DINAMICA = "http://www.infobustussam.com:9001/services/dinamica.asmx";
    private static final String BODY_SOAP_BUSES = "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><GetVehiculos xmlns=\"http://tempuri.org/\"><linea>%1s</linea></GetVehiculos></soap:Body></soap:Envelope>";

    public static List<BusLocation> getBuses(String linea) throws ServerErrorException {
        List<BusLocation> res = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            BusesHandler handler = new BusesHandler();
            InputStream is = getBusesInputStream(linea);
            parser.parse(is, handler);
            res = handler.getBuses();
        } catch (IllegalArgumentException e) {
            Log.e("sevibus", "Error con el InputStream", e);
            throw new ServerErrorException();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return res;

    }

    private static InputStream getBusesInputStream(String linea) {
        InputStream res = null;
        try {
            URL url = new URL(URL_SOAP_DINAMICA);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST");
            c.setReadTimeout(15 * 1000);
            c.setDoOutput(true);
            // c.setFixedLengthStreamingMode(contentLength)
            c.setUseCaches(false);
            c.setRequestProperty("Content-Type", "text/xml");
            c.connect();

            OutputStreamWriter wr = new OutputStreamWriter(c.getOutputStream());
            String data = String.format(BODY_SOAP_BUSES, linea);
            wr.write(data);
            wr.flush();

            res = c.getInputStream();
        } catch (MalformedURLException e) {
            Log.e("sevibus", "Error al obtener la fuente de los autobuses", e);
        } catch (IOException e) {
            Log.e("sevibus", "Error al obtener la fuente de los autobuses", e);
        }
        return res;

    }

}
