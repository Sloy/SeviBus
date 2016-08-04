package com.sloy.sevibus.resources.datasource;

import com.sloy.sevibus.model.tussam.Bonobus;

import java.util.List;

public interface BonobusDataSource {

    List<Bonobus> obtainBonobuses();
}
