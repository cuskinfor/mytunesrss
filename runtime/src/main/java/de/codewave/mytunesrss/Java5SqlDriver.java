/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import java.sql.*;
import java.util.Properties;

public class Java5SqlDriver implements Driver {
    private Driver myDriver;

        public Java5SqlDriver(Class<Driver> driverClass) throws IllegalAccessException, InstantiationException {
            myDriver = driverClass.newInstance();
        }

        public Connection connect(String string, Properties properties) throws SQLException {
            return myDriver.connect(string, properties);
        }

        public boolean acceptsURL(String string) throws SQLException {
            return myDriver.acceptsURL(string);
        }

        public DriverPropertyInfo[] getPropertyInfo(String string, Properties properties) throws SQLException {
            return myDriver.getPropertyInfo(string, properties);
        }

        public int getMajorVersion() {
            return myDriver.getMajorVersion();
        }

        public int getMinorVersion() {
            return myDriver.getMinorVersion();
        }

        public boolean jdbcCompliant() {
            return myDriver.jdbcCompliant();
        }

        public java.util.logging.Logger getParentLogger()  {
            throw new RuntimeException("Getting a parent logger is not supported here!");
        }
    }