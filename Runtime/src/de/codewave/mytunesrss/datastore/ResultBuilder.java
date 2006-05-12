/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.ResultBuilder
 */
public interface ResultBuilder<T> {
    T create(ResultSet resultSet) throws SQLException;
}