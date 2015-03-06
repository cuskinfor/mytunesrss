package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class DataStoreStatementEvent extends DataStoreEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataStoreStatementEvent.class);

    private DataStoreStatement myStatement;
    private String myExLogMsg;
    private boolean myCheckpointRelevant;

    public DataStoreStatementEvent(DataStoreStatement statement, boolean checkpointRelevant) {
        myStatement = statement;
        myCheckpointRelevant = checkpointRelevant;
    }

    public DataStoreStatementEvent(DataStoreStatement statement, boolean checkpointRelevant, String exLogMsg) {
        myStatement = statement;
        myCheckpointRelevant = checkpointRelevant;
        myExLogMsg = exLogMsg;
    }

    @Override
    public boolean execute(DataStoreSession session) {
        try {
            session.executeStatement(myStatement);
        } catch (SQLException e) {
            if (StringUtils.isBlank(myExLogMsg)) {
                LOGGER.warn("Could not execute data store statement.", e);
            } else {
                LOGGER.warn(myExLogMsg, e);
            }
        }
        return true;
    }

    @Override
    public boolean isCheckpointRelevant() {
        return myCheckpointRelevant;
    }
}
