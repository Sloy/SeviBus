package com.sloy.sevibus.resources.datasource.bonobus;

import com.sloy.sevibus.model.tussam.Bonobus;
import com.sloy.sevibus.resources.BonobusInfoReader;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

public class TussamApiBonobusSaldoDataSource implements BonobusSaldoDataSource {

    @Override
    public double getSaldo(long numeroBonobus) {
        Bonobus bonobus = new Bonobus();
        bonobus.setNumero(numeroBonobus);
        try {
            BonobusInfoReader.populateBonobusInfo(bonobus);
        } catch (IOException | ParserConfigurationException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        String saldoString = bonobus.getSaldo().replace("€", "");
        return Double.valueOf(saldoString);
    }
}
