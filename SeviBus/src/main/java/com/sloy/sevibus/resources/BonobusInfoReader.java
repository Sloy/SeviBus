package com.sloy.sevibus.resources;

import android.text.TextUtils;

import com.sloy.sevibus.model.tussam.Bonobus;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class BonobusInfoReader {

    private static final String URL_CONSULTA = "http://recargas.tussam.es/TPW/Common/cardStatus.do?swNumber=%d";

    public static final String XPATH_NUMERO = "//b[text()=\"Num Tarjeta:\"]/following-sibling::span[1]";
    public static final String XPATH_TITULO = "//b[text()=\"Título:\"]/following-sibling::span[1]";
    public static final String XPATH_SALDO = "//b[text()=\"Saldo monedero:\"]/following-sibling::span[1]";
    public static final String XPATH_CADUCIDAD = "//b[text()=\"Caducidad Tarjeta:\"]/following-sibling::span[1]";
    public static final String XPATH_FECHA_FIN = "//b[text()=\"Fecha fin actual\"]/following-sibling::span[1]";


    public static Bonobus populateBonobusInfo(Bonobus bonobus) throws IOException, ParserConfigurationException, XPathExpressionException, NumberFormatException {
        Document document = getDocument(String.format(URL_CONSULTA, bonobus.getNumero()));

        String numero = getNumero(document);
        Long numeroLeido = Long.parseLong(numero);
        if (numeroLeido != bonobus.getNumero()) {
            throw new RuntimeException("El número del bonobús no coincide con el recibido, tú");
        }

        String titulo = getTitulo(document);
        if (titulo != null && !TextUtils.isEmpty(titulo)) {
            bonobus.setTipoTexto(titulo);
        } else {
            bonobus.setError(true);
            return bonobus;
        }

        if (titulo.contains("saldo sin transbordo")) {
            bonobus.setTipo(Bonobus.TIPO.SALDO);
        }else if (titulo.contains("saldo con transbordo")) {
            bonobus.setTipo(Bonobus.TIPO.SALDO_TRANSBORDO);
        }else if (titulo.contains("Tarjeta joven")) {
            bonobus.setTipo(Bonobus.TIPO.JOVEN);
        }else if (titulo.contains("Tarjeta 30")) {
            bonobus.setTipo(Bonobus.TIPO.MENSUAL);
        } else {
            bonobus.setTipo(Bonobus.TIPO.UNKNOWN);
        }

        switch (bonobus.getTipo()) {
            case SALDO:
            case SALDO_TRANSBORDO:
                String saldo = getSaldo(document);
                if (saldo != null && !TextUtils.isEmpty(saldo)) {
                    bonobus.setSaldo(saldo);
                }
                break;
            case JOVEN:
                String caducidad = getCaducidad(document);
                if (caducidad != null && !TextUtils.isEmpty(caducidad)) {
                    bonobus.setCaducidad(caducidad);
                }
                break;
            case MENSUAL:
                String fechaFin = getFechaFin(document);
                if (fechaFin != null && !TextUtils.isEmpty(fechaFin)) {
                    bonobus.setCaducidad(fechaFin);
                }
                break;
            case UNKNOWN:
                break;
        }


        bonobus.setRelleno(true);
        bonobus.setError(false);
        return bonobus;
    }

    private static String getNumero(Document doc) throws XPathExpressionException {
        XPathExpression xpathNumero = XPathFactory.newInstance().newXPath().compile(XPATH_NUMERO);
        String numero = (String) xpathNumero.evaluate(doc, XPathConstants.STRING);
        if (numero != null && !TextUtils.isEmpty(numero)) {
            numero = numero.substring(1);
        }
        return numero;
    }

    private static String getTitulo(Document doc) throws XPathExpressionException {
        XPathExpression xpathTitulo = XPathFactory.newInstance().newXPath().compile(XPATH_TITULO);
        return (String) xpathTitulo.evaluate(doc, XPathConstants.STRING);
    }

    private static String getSaldo(Document doc) throws XPathExpressionException {
        XPathExpression xpathSaldo = XPathFactory.newInstance().newXPath().compile(XPATH_SALDO);
        String saldo = (String) xpathSaldo.evaluate(doc, XPathConstants.STRING);
        saldo = saldo.replace("\n","").replace(" ","");
        return saldo;
    }

    private static String getCaducidad(Document doc) throws XPathExpressionException {
        XPathExpression xpathCaducidad = XPathFactory.newInstance().newXPath().compile(XPATH_CADUCIDAD);
        String caducidad = (String) xpathCaducidad.evaluate(doc, XPathConstants.STRING);
        caducidad = caducidad.replace("\n","").replace(" ","");
        return caducidad;
    }

    private static String getFechaFin(Document doc) throws XPathExpressionException {
        XPathExpression xpathFechaFin = XPathFactory.newInstance().newXPath().compile(XPATH_FECHA_FIN);
        String fechaFin = (String) xpathFechaFin.evaluate(doc, XPathConstants.STRING);
        fechaFin = fechaFin.replace("\n","").replace(" ","");
        return fechaFin;
    }

    private static Document getDocument(String urlTussam) throws IOException, ParserConfigurationException {
        String html = StuffProvider.getStringDownloader().download(urlTussam);

        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();
        props.setAllowHtmlInsideAttributes(false);
        props.setAllowMultiWordAttributes(false);
        props.setRecognizeUnicodeChars(true);
        props.setOmitComments(true);

        TagNode node = cleaner.clean(html);

        return new DomSerializer(new CleanerProperties()).createDOM(node);
    }
}
