package de.codewave.mytunesrss.datastore.statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.codewave.utils.sql.SmartStatement;
import de.codewave.mytunesrss.MyTunesRssUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertImageStatement
 */
public class InsertImageStatement {
    private static final Log LOG = LogFactory.getLog(InsertImageStatement.class);

    private String myHash;
    private int mySize;
    private byte[] myData;
    private SmartStatement myStatement;

    public InsertImageStatement(String hash, int size, byte[] data) {
        myHash = hash;
        mySize = size;
        myData = data;
        if (LOG.isDebugEnabled()) {
            if (data != null) {
                LOG.debug("Image data size is " + data.length + " bytes.");
            } else {
                LOG.debug("Image data is NULL.");
            }
        }
    }

    public void setData(byte[] data) {
        myData = data;
    }

    public void setSize(int size) {
        mySize = size;
    }

    public void setHash(String hash) {
        myHash = hash;
    }

    public synchronized void execute(Connection connection) throws SQLException {
        if (myData != null) {
            try {
                if (myStatement == null) {
                    myStatement = MyTunesRssUtils.createStatement(connection, "insertImage");
                }
                myStatement.clearParameters();
                myStatement.setString("hash", myHash);
                myStatement.setInt("size", mySize);
                myStatement.setObject("data", myData);
                myStatement.execute();
            } catch (SQLException e) {
                    LOG.error("Could not insert image for hash \"" + myHash + "\".", e);
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping image insert for hash \"" + myHash + "\" because image data is NULL.");
            }
        }
    }

    public void clear() {
        mySize = 0;
        myData = null;
    }
}