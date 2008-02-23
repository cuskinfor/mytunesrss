package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.UpdateTrackAndImageStatement
 */
public class UpdateTrackAndImageStatement extends UpdateTrackStatement {
    @Override
    protected String getStatementName() {
        return "updateTrackAndImage";
    }
}