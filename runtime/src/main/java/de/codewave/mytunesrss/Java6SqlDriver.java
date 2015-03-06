/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import java.sql.*;
import java.util.Properties;

public class Java6SqlDriver implements Driver {
    private Driver myDriver;

    public Java6SqlDriver(Class<Driver> driverClass) throws IllegalAccessException, InstantiationException {
        myDriver = driverClass.newInstance();
    }

    @Override
    public Connection connect(String string, Properties properties) throws SQLException {
        return myDriver.connect(string, properties);
    }

    @Override
    public boolean acceptsURL(String string) throws SQLException {
        return myDriver.acceptsURL(string);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String string, Properties properties) throws SQLException {
        return myDriver.getPropertyInfo(string, properties);
    }

    @Override
    public int getMajorVersion() {
        return myDriver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return myDriver.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return myDriver.jdbcCompliant();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Getting a parent logger is not supported here!");
    }
}