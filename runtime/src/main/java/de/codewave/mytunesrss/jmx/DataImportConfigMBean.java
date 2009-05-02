package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.FileType;
import de.codewave.mytunesrss.MediaType;

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

    String addFileType(boolean active, String suffix, String mimeType, MediaType mediaType, boolean protect) throws SQLException;

    String editFileType(String suffix, boolean active, String mimeType, MediaType mediaType, boolean protect) throws SQLException;

    String removeFileType(String suffix);

    void resetToDefaults() throws SQLException;

    String getDisabledMp4Codecs();

    void setDisabledMp4Codecs(String codecs);
}