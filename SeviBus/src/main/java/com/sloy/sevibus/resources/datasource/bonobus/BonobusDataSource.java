package com.sloy.sevibus.resources.datasource.bonobus;

import com.sloy.sevibus.model.tussam.Bonobus;

import java.util.List;

public interface BonobusDataSource {

    List<Bonobus> obtainBonobuses();
}
