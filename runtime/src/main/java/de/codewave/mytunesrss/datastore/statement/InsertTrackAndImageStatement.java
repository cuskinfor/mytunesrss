package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.UpdateTrackAndImageStatement
 */
public class InsertTrackAndImageStatement extends InsertTrackStatement {
    public InsertTrackAndImageStatement(TrackSource source, String sourceId) {
        super(source, sourceId);
    }

    @Override
    protected String getStatementName() {
        return "insertTrackAndImage";
    }

}