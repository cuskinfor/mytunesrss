package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class DataStoreStatementEvent implements TransactionalEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataStoreStatementEvent.class);

    private DataStoreStatement myStatement;
    private String myExLogMsg;

    public DataStoreStatementEvent(DataStoreStatement statement) {
        myStatement = statement;
    }

    public DataStoreStatementEvent(DataStoreStatement statement, String exLogMsg) {
        myStatement = statement;
        myExLogMsg = exLogMsg;
    }

    public DataStoreSession execute(DataStoreSession session) {
        try {
            session.executeStatement(myStatement);
        } catch (SQLException e) {
            if (StringUtils.isBlank(myExLogMsg)) {
                LOGGER.warn("Could not execute data store statement.", e);
            } else {
                LOGGER.warn(myExLogMsg, e);
            }
        }
        return session;
    }

    public boolean isIgnoreWithoutTransaction() {
        return false;
    }
}
