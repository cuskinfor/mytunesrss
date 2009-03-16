package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.UpdateTrackAndImageStatement
 */
public class UpdateTrackAndImageStatement extends UpdateTrackStatement {
    public UpdateTrackAndImageStatement(TrackSource source) {
        super(source);
    }

    @Override
    protected String getStatementName() {
        return "updateTrackAndImage";
    }
}