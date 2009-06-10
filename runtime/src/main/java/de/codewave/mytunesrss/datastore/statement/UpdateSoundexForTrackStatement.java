package de.codewave.mytunesrss.datastore.statement;

import java.util.Collection;

/**
 * de.codewave.mytunesrss.datastore.statement.UpdateSoundexForTrackStatement
 */
public class UpdateSoundexForTrackStatement extends InsertSoundexForTrackStatement {

    public UpdateSoundexForTrackStatement(String trackId, Collection<String> soundexCodes) {
        super(trackId, soundexCodes);
    }

    @Override
    protected String getStatementName() {
        return "updateSoundexForTrack";
    }
}