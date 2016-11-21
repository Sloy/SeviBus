package com.sloy.sevibus.resources.datasource.bonobus;


public class BonobusApiModel {

    private final Integer number;
    private final String type;
    private final String lastOperationDate;
    private final String expirationDate;
    private final Double credit;

    public BonobusApiModel(Integer number, String type, String lastOperationDate, String expirationDate, Double credit) {
        this.number = number;
        this.type = type;
        this.lastOperationDate = lastOperationDate;
        this.expirationDate = expirationDate;
        this.credit = credit;
    }

    public Integer getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }

    public String getLastOperationDate() {
        return lastOperationDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public Double getCredit() {
        return credit;
    }
}
