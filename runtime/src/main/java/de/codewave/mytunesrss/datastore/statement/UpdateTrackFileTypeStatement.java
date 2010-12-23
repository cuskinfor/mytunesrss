package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.FileType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

public class UpdateTrackFileTypeStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateTrackFileTypeStatement.class);

    public static void execute(Collection<FileType> oldTypes, Collection<FileType> newTypes) {
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            session.executeStatement(new UpdateTrackFileTypeStatement(oldTypes, newTypes));
            session.executeStatement(new RefreshSmartPlaylistsStatement());
            session.commit();
        } catch (SQLException e) {
            LOGGER.error("Could not update file type information in database.", e);
        } finally {
            session.rollback();
        }
    }

    private Collection<FileType> myOldFileTypes;
    private Collection<FileType> myNewFileTypes;

    public UpdateTrackFileTypeStatement(Collection<FileType> oldFileTypes, Collection<FileType> newFileTypes) {
        myOldFileTypes = oldFileTypes;
        myNewFileTypes = newFileTypes;
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "updateTrackFileType");
        for (FileType fileType : myNewFileTypes) {
            if (isChanged(fileType)) {
                LOGGER.debug("Updating file type \"" + fileType.getSuffix().toLowerCase() + "\".");
                statement.clearParameters();
                statement.setString("suffix", fileType.getSuffix().toLowerCase());
                statement.setString("mediatype", fileType.getMediaType().name());
                statement.setBoolean("protected", fileType.isProtected());
                statement.execute();
            }
        }
    }

    private boolean isChanged(FileType fileType) {
        for (FileType oldType : myOldFileTypes) {
            if (oldType.getSuffix().equalsIgnoreCase(fileType.getSuffix())) {
                return oldType.getMediaType() != fileType.getMediaType() || oldType.isProtected() != fileType.isProtected();
            }
        }
        return true; // new file type
    }
}
