package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.UpdateTrackAndImageStatement
 */
public class InsertTrackAndImageStatement extends InsertTrackStatement {
    public InsertTrackAndImageStatement(TrackSource source) {
        super(source);
    }

    @Override
    protected String getStatementName() {
        return "insertTrackAndImage";
    }

}