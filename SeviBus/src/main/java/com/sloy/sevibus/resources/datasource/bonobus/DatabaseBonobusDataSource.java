package com.sloy.sevibus.resources.datasource.bonobus;

import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Bonobus;

import java.util.List;

public class DatabaseBonobusDataSource implements BonobusDataSource {
    private final DBHelper dbHelper;

    public DatabaseBonobusDataSource(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public List<Bonobus> obtainBonobuses() {
        //TODO Rx
        return DBQueries.getBonobuses(dbHelper);
    }
}
