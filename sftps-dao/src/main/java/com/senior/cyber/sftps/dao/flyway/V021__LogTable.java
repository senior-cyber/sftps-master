package com.senior.cyber.sftps.dao.flyway;

import com.senior.cyber.sftps.dao.LiquibaseMigration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Arrays;
import java.util.List;

public class V021__LogTable extends LiquibaseMigration {

    @Override
    protected List<String> getXmlChecksum() {
        return Arrays.asList("V021__LogTable.xml");
    }

    @Override
    protected void doMigrate(NamedParameterJdbcTemplate named) throws Exception {
        updateLiquibase("V021__LogTable.xml");
    }

}