package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.FileType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.datastore.statement.UpdateTrackFileTypeStatement;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;

import javax.management.NotCompliantMBeanException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Collections;

/**
 * de.codewave.mytunesrss.jmx.DataImportConfig
 */
public class DataImportConfig extends MyTunesRssMBean implements DataImportConfigMBean {

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
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getId3v2TrackComment());
    }

    public void setId3v2TagCommentPattern(String pattern) {
        MyTunesRss.CONFIG.setId3v2TrackComment(StringUtils.trimToNull(pattern));
        onChange();
    }

    public List<FileType> getFileTypes() {
        return MyTunesRss.CONFIG.getFileTypes();
    }

    public String addFileType(boolean active, String suffix, String mimeType, MediaType mediaType, boolean protect) throws SQLException {
        FileType type = new FileType(active, suffix, mimeType, mediaType, protect);
        if (FileType.isValid(type)) {
            if (!MyTunesRss.CONFIG.getFileTypes().contains(type)) {
                MyTunesRss.CONFIG.getFileTypes().add(type);
                UpdateTrackFileTypeStatement.execute(Collections.<FileType>emptySet(), Collections.singleton(type));
                onChange();
                return MyTunesRssUtils.getBundleString("jmx.fileTypeAdded", suffix);
            }
            return MyTunesRssUtils.getBundleString("jmx.fileTypeDuplicate", suffix);
        }
        return MyTunesRssUtils.getBundleString("jmx.fileTypeInvalid");
    }

    public String editFileType(String suffix, boolean active, String mimeType, MediaType mediaType, boolean protect) throws SQLException {
        FileType type = MyTunesRss.CONFIG.getFileType(suffix);
        if (type != null) {
            type.setActive(active);
            type.setMimeType(mimeType);
            type.setMediaType(mediaType);
            type.setProtected(protect);
            if (FileType.isValid(type)) {
                Collection<FileType> oldTypes = MyTunesRss.CONFIG.getDeepFileTypesClone();
                int i = MyTunesRss.CONFIG.getFileTypes().indexOf(type);
                MyTunesRss.CONFIG.getFileTypes().remove(i);
                MyTunesRss.CONFIG.getFileTypes().add(i, type);
                UpdateTrackFileTypeStatement.execute(oldTypes, Collections.singleton(type));
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

    public void resetToDefaults() throws SQLException {
        Collection<FileType> oldTypes = MyTunesRss.CONFIG.getDeepFileTypesClone();
        MyTunesRss.CONFIG.getFileTypes().clear();
        MyTunesRss.CONFIG.getFileTypes().addAll(FileType.getDefaults());
        onChange();
        UpdateTrackFileTypeStatement.execute(oldTypes, FileType.getDefaults());
    }

    public String getArtistDropWords() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getArtistDropWords());
    }

    public void setArtistDropWords(String artistDropWords) {
        MyTunesRss.CONFIG.setArtistDropWords(StringUtils.trimToNull(artistDropWords));
        onChange();
    }

    public boolean isIgnoreCoverArtworkFromFiles() {
        return MyTunesRss.CONFIG.isIgnoreArtwork();
    }

    public void setIgnoreCoverArtworkFromFiles(boolean ignoreCoverArtwork) {
        MyTunesRss.CONFIG.setIgnoreArtwork(ignoreCoverArtwork);
        onChange();
    }

    public String getDisabledMp4Codecs() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getDisabledMp4Codecs());
    }

    public void setDisabledMp4Codecs(String codecs) {
        MyTunesRss.CONFIG.setDisabledMp4Codecs(StringUtils.trimToNull(codecs));
        onChange();
    }
}