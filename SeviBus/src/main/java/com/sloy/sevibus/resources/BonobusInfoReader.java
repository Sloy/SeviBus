package com.sloy.sevibus.resources;

import android.text.TextUtils;

import com.sloy.sevibus.model.tussam.Bonobus;
import com.sloy.sevibus.resources.datasource.SevibusApi;
import com.sloy.sevibus.resources.datasource.bonobus.BonobusApiModel;

public class BonobusInfoReader {

    private final SevibusApi sevibusApi;

    public BonobusInfoReader(SevibusApi sevibusApi) {
        this.sevibusApi = sevibusApi;
    }

    public Bonobus populateBonobusInfo(Bonobus original) {
        BonobusApiModel apiModel = sevibusApi.getBonobus(original.getNumero())
          .toBlocking().single();

        original.setTipo(getTipo(apiModel));

        Bonobus.TIPO tipo = original.getTipo();
        if (Bonobus.TIPO.SALDO == tipo || Bonobus.TIPO.SALDO_TRANSBORDO == tipo) {
            Double credit = apiModel.getCredit();
            if (credit != null) {
                original.setSaldo(credit + " €");
            }
        } else if (Bonobus.TIPO.JOVEN == tipo || Bonobus.TIPO.MENSUAL == tipo) {
            String caducidad = apiModel.getExpirationDate();
            if (caducidad != null && !TextUtils.isEmpty(caducidad)) {
                original.setCaducidad(caducidad);
            }
        }

        original.setRelleno(true);
        original.setError(false);

        return original;
    }

    private Bonobus.TIPO getTipo(BonobusApiModel apiModel) {
        String type = apiModel.getType().toLowerCase();
        if (type.contains("saldo sin transbordo")) {
            return Bonobus.TIPO.SALDO;
        } else if (type.contains("saldo con transbordo")) {
            return Bonobus.TIPO.SALDO_TRANSBORDO;
        } else if (type.contains("tarjeta joven")) {
            return Bonobus.TIPO.JOVEN;
        } else if (type.contains("tarjeta 30")) {
            return Bonobus.TIPO.MENSUAL;
        } else {
            return Bonobus.TIPO.UNKNOWN;
        }
    }
}
