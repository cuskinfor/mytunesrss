package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.FileType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.UpdateTrackFileTypeStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.NotCompliantMBeanException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * de.codewave.mytunesrss.jmx.DataImportConfig
 */
public class DataImportConfig extends MyTunesRssMBean implements DataImportConfigMBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataImportConfig.class);

    public DataImportConfig() throws NotCompliantMBeanException {
        super(DataImportConfigMBean.class);
    }

    public boolean isIgnoreTimestampsOnUpdate() {
        return MyTunesRss.CONFIG.isIgnoreTimestamps();
    }

    public void setIgnoreTimestampsOnUpdate(boolean ignoreTimestamps) {
        MyTunesRss.CONFIG.setIgnoreTimestamps(ignoreTimestamps);
        onChange();
    }

    public String getId3v2TagCommentPattern() {
        return MyTunesRss.CONFIG.getId3v2TrackComment();
    }

    public void setId3v2TagCommentPattern(String pattern) {
        MyTunesRss.CONFIG.setId3v2TrackComment(pattern);
        onChange();
    }

    public List<FileType> getFileTypes() {
        return MyTunesRss.CONFIG.getFileTypes();
    }

    public String addFileType(boolean active, String suffix, String mimeType, boolean video, boolean protect) {
        FileType type = new FileType(active, suffix, mimeType, video, protect);
        if (FileType.isValid(type)) {
            if (!MyTunesRss.CONFIG.getFileTypes().contains(type)) {
                MyTunesRss.CONFIG.getFileTypes().add(type);
                onChange();
                return MyTunesRssUtils.getBundleString("jmx.fileTypeAdded", suffix);
            }
            return MyTunesRssUtils.getBundleString("jmx.fileTypeDuplicate", suffix);
        }
        return MyTunesRssUtils.getBundleString("jmx.fileTypeInvalid");
    }

    public String editFileType(String suffix, boolean active, String mimeType, boolean video, boolean protect) {
        FileType type = MyTunesRss.CONFIG.getFileType(suffix);
        if (type != null) {
            type.setActive(active);
            type.setMimeType(mimeType);
            type.setVideo(video);
            type.setProtected(protect);
            if (FileType.isValid(type)) {
                int i = MyTunesRss.CONFIG.getFileTypes().indexOf(type);
                MyTunesRss.CONFIG.getFileTypes().remove(i);
                MyTunesRss.CONFIG.getFileTypes().add(i, type);
                onChange();
                return MyTunesRssUtils.getBundleString("jmx.fileTypeChanged", suffix);
            }
            return MyTunesRssUtils.getBundleString("jmx.fileTypeInvalid");
        }
        return MyTunesRssUtils.getBundleString("jmx.fileTypeNotFound", suffix);
    }

    public String removeFileType(String suffix) {
        FileType type = MyTunesRss.CONFIG.getFileType(suffix);
        if (type != null) {
            MyTunesRss.CONFIG.getFileTypes().remove(type);
            onChange();
            return MyTunesRssUtils.getBundleString("jmx.fileTypeRemoved", suffix);
        }
        return MyTunesRssUtils.getBundleString("jmx.fileTypeNotFound", suffix);
    }

    public void resetToDefaults() {
        Collection<FileType> oldTypes = new HashSet<FileType>(MyTunesRss.CONFIG.getFileTypes());
        MyTunesRss.CONFIG.getFileTypes().clear();
        MyTunesRss.CONFIG.getFileTypes().addAll(FileType.getDefaults());
        onChange();
        try {
            MyTunesRss.STORE.getTransaction().executeStatement(new UpdateTrackFileTypeStatement(oldTypes, FileType.getDefaults()));
        } catch (SQLException e) {
            LOGGER.error("Could not update file type information in database.", e);
        }
    }

    public String getArtistDropWords() {
        return MyTunesRss.CONFIG.getArtistDropWords();
    }

    public void setArtistDropWords(String artistDropWords) {
        MyTunesRss.CONFIG.setArtistDropWords(artistDropWords);
        onChange();
    }

    public boolean isIgnoreCoverArtworkFromFiles() {
        return MyTunesRss.CONFIG.isIgnoreArtwork();
    }

    public void setIgnoreCoverArtworkFromFiles(boolean ignoreCoverArtwork) {
        MyTunesRss.CONFIG.setIgnoreArtwork(ignoreCoverArtwork);
        onChange();
    }
}