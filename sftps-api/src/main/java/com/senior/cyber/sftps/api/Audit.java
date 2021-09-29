package com.senior.cyber.sftps.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Audit {

    private static final Logger LOGGER = LoggerFactory.getLogger(Audit.class);

    public static void log(String format, Object... arguments) {
        LOGGER.info(format, arguments);
    }

    public void info(String format, Object arg) {
        LOGGER.info(format, arg);
    }

    public void info(String format, Object arg1, Object arg2) {
        LOGGER.info(format, arg1, arg2);
    }

}
