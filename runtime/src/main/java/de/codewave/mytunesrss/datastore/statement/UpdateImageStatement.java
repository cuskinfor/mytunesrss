package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertImageStatement
 */
public class UpdateImageStatement extends InsertOrUpdateImageStatement {
    public UpdateImageStatement(String trackId, int size, byte[] data) {
        super(trackId, size, data);
    }

    protected String getStatementName() {
        return "updateImage";
    }
}