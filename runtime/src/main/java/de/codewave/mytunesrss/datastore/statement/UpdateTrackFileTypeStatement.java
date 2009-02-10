package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.FileType;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

public class UpdateTrackFileTypeStatement implements DataStoreStatement {
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
                statement.clearParameters();
                statement.setString("suffix", fileType.getSuffix().toLowerCase());
                statement.setBoolean("video", fileType.isVideo());
                statement.setBoolean("protected", fileType.isProtected());
                statement.execute();
            }
        }
    }

    private boolean isChanged(FileType fileType) {
        for (FileType oldType : myOldFileTypes) {
            if (oldType.getSuffix().equalsIgnoreCase(fileType.getSuffix())) {
                return oldType.isVideo() != fileType.isVideo() || oldType.isProtected() != fileType.isProtected();
            }
        }
        return true; // new file type
    }
}
