package com.sloy.sevibus.resources.datasource;

import android.support.annotation.NonNull;
import android.util.Log;

import com.sloy.sevibus.model.ArrivalTime;
import com.sloy.sevibus.resources.TiemposHandler;
import com.sloy.sevibus.resources.exceptions.ServerErrorException;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import rx.Observable;
import rx.schedulers.Schedulers;

public class TussamLlegadaDataSource implements LlegadaDataSource {

    private static final String URL_SOAP_DINAMICA = "http://www.infobustussam.com:9001/services/dinamica.asmx";
    private static final String BODY_SOAP_TIEMPOS = "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><GetPasoParada xmlns=\"http://tempuri.org/\"><linea>%1s</linea><parada>%2s</parada><status>1</status></GetPasoParada></soap:Body></soap:Envelope>"; // 2.parada

    @Override
    public Observable<ArrivalTime> getLlegadas(Integer parada, List<String> lineas) throws ServerErrorException {
        return Observable.from(lineas)
          .flatMap(linea -> getLlegada(linea, parada));
    }

    private Observable<ArrivalTime> getLlegada(final String linea, final Integer parada) throws ServerErrorException {
        return Observable.defer(() -> Observable.just(getLlegadaFromTussam(linea, parada))).subscribeOn(Schedulers.io());
    }

    @NonNull
    private ArrivalTime getLlegadaFromTussam(String linea, Integer parada) {
        ArrivalTime res = createEmptyArrival(parada, linea);
        try {
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

    private InputStream getTiemposInputStream(String lineName, String stopNumber) throws IOException {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("text/xml");
        RequestBody body = RequestBody.create(mediaType, String.format(BODY_SOAP_TIEMPOS, lineName, stopNumber));
        Request request = new Request.Builder()
                .url(URL_SOAP_DINAMICA)
                .post(body)
                .addHeader("content-type", "text/xml")
                .addHeader("cache-control", "no-cache")
                .build();

        Response response = client.newCall(request).execute();
        return response.body().byteStream();
    }

    private ArrivalTime createEmptyArrival(Integer busStopNumber, String lineName) {
        ArrivalTime arrivalTimes = new ArrivalTime();
        arrivalTimes.setBusStopNumber(busStopNumber);
        arrivalTimes.setBusLineName(lineName);
        arrivalTimes.setDataSource("fallback");
        return arrivalTimes;
    }

}
