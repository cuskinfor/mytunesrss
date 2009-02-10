package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.FileType;

import java.util.List;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.jmx.DataImportMBean
 */
public interface DataImportConfigMBean {
    String getArtistDropWords();

    void setArtistDropWords(String artistDropWords);

    boolean isIgnoreCoverArtworkFromFiles();

    void setIgnoreCoverArtworkFromFiles(boolean ignoreCoverArtwork);

    boolean isIgnoreTimestampsOnUpdate();

    void setIgnoreTimestampsOnUpdate(boolean ignoreTimestamps);

    String getId3v2TagCommentPattern();

    void setId3v2TagCommentPattern(String pattern);

    List<FileType> getFileTypes();

    String addFileType(boolean active, String suffix, String mimeType, boolean video, boolean protect) throws SQLException;

    String editFileType(String suffix, boolean active, String mimeType, boolean video, boolean protect) throws SQLException;

    String removeFileType(String suffix);

    void resetToDefaults() throws SQLException;
}