package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.SaveITunesPlaylistStatement
 */
public class SaveM3uFilePlaylistStatement extends SavePlaylistStatement {

    public SaveM3uFilePlaylistStatement(String sourceId) {
        super(sourceId);
        setType(PlaylistType.M3uFile);
    }

}