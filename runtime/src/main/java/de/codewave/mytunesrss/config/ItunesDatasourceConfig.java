/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.ImageImportType;
import de.codewave.mytunesrss.datastore.itunes.ItunesLoader;
import de.codewave.mytunesrss.datastore.itunes.ItunesPlaylistType;
import de.codewave.mytunesrss.datastore.itunes.LibraryListener;
import de.codewave.utils.xml.PListHandler;
import de.codewave.utils.xml.PListHandlerListener;
import de.codewave.utils.xml.XmlUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Pattern;

public class ItunesDatasourceConfig extends DatasourceConfig implements CommonTrackDatasourceConfig {

    private static class MusicFolderListener implements PListHandlerListener {
        private String myMusicFolder;

        public boolean beforeDictPut(Map dict, String key, Object value) {
            if ("Music Folder".equals(key)) {
                myMusicFolder = value.toString();
            }
            return true;
        }

        public boolean beforeArrayAdd(List array, Object value) {
            return false;
        }

        public String getMusicFolder() {
            return myMusicFolder;
        }
    }

    private Set<ReplacementRule> myPathReplacements = new HashSet<ReplacementRule>();
    private boolean myDeleteMissingFiles = true;
    private Set<ItunesPlaylistType> myIgnorePlaylists = new HashSet<ItunesPlaylistType>();
    private String myArtistDropWords;
    private String myDisabledMp4Codecs = "";
    private List<ReplacementRule> myTrackImageMappings = new ArrayList<ReplacementRule>();
    private ImageImportType myTrackImageImportType = ImageImportType.Auto;

    public ItunesDatasourceConfig(String id, String definition) {
        super(id, definition);
    }

    public ItunesDatasourceConfig(ItunesDatasourceConfig source) {
        super(source);
        myPathReplacements = new HashSet<ReplacementRule>(source.getPathReplacements());
        myIgnorePlaylists = new HashSet<ItunesPlaylistType>(source.getIgnorePlaylists());
        myDeleteMissingFiles = source.isDeleteMissingFiles();
        myArtistDropWords = source.getArtistDropWords();
        myDisabledMp4Codecs = source.getDisabledMp4Codecs();
        myTrackImageMappings = new ArrayList<ReplacementRule>(source.getTrackImageMappings());
        myTrackImageImportType = source.getTrackImageImportType();
    }

    @Override
    public DatasourceType getType() {
        return DatasourceType.Itunes;
    }

    public Set<ReplacementRule> getPathReplacements() {
        return new HashSet<ReplacementRule>(myPathReplacements);
    }

    public void clearPathReplacements() {
        myPathReplacements.clear();
    }

    public void addPathReplacement(ReplacementRule pathReplacement) {
        myPathReplacements.add(pathReplacement);
    }

    public boolean isDeleteMissingFiles() {
        return myDeleteMissingFiles;
    }

    public void setDeleteMissingFiles(boolean deleteMissingFiles) {
        myDeleteMissingFiles = deleteMissingFiles;
    }

    public Set<ItunesPlaylistType> getIgnorePlaylists() {
        return new HashSet<ItunesPlaylistType>(myIgnorePlaylists);
    }

    public void addIgnorePlaylist(ItunesPlaylistType type) {
        myIgnorePlaylists.add(type);
    }

    public void removeIgnorePlaylist(ItunesPlaylistType type) {
        myIgnorePlaylists.remove(type);
    }

    public void clearIgnorePlaylists() {
        myIgnorePlaylists.clear();
    }

    public String getArtistDropWords() {
        return myArtistDropWords;
    }

    public void setArtistDropWords(String artistDropWords) {
        myArtistDropWords = artistDropWords;
    }

    public String getDisabledMp4Codecs() {
        return myDisabledMp4Codecs;
    }

    public void setDisabledMp4Codecs(String disabledMp4Codecs) {
        myDisabledMp4Codecs = disabledMp4Codecs;
    }

    public List<ReplacementRule> getTrackImageMappings() {
        return new ArrayList<ReplacementRule>(myTrackImageMappings);
    }

    public void setTrackImageMappings(List<ReplacementRule> trackImageMappings) {
        this.myTrackImageMappings = new ArrayList<ReplacementRule>(trackImageMappings);
    }

    public ImageImportType getTrackImageImportType() {
        return myTrackImageImportType;
    }

    public void setTrackImageImportType(ImageImportType trackImageImportType) {
        myTrackImageImportType = trackImageImportType;
    }

    public List<FileType> getDefaultFileTypes() {
        List<FileType> types = new ArrayList<FileType>();
        types.add(new FileType(true, "m4a", "audio/x-m4a", MediaType.Audio, false));
        types.add(new FileType(true, "m4p", "audio/x-m4p", MediaType.Audio, true));
        types.add(new FileType(true, "mp4", "video/x-mp4", MediaType.Video, false));
        types.add(new FileType(true, "mov", "video/quicktime", MediaType.Video, false));
        types.add(new FileType(true, "mpg", "audio/mpeg", MediaType.Audio, false));
        types.add(new FileType(true, "mpeg", "audio/mpeg", MediaType.Audio, false));
        types.add(new FileType(true, "m4v", "video/x-m4v", MediaType.Video, false));
        types.add(new FileType(true, "m4b", "audio/x-m4b", MediaType.Audio, false));
        types.add(new FileType(true, "mp3", "audio/mp3", MediaType.Audio, false));
        Collections.sort(types, new Comparator<FileType>() {
            public int compare(FileType o1, FileType o2) {
                return o1.getSuffix().compareTo(o2.getSuffix());
            }
        });
        return types;
    }

    public File getAutoAddToItunesFolder() throws IOException, SAXException, ParserConfigurationException {
        PListHandler handler = new PListHandler();
        MusicFolderListener listener = new MusicFolderListener();
        handler.addListener("/plist/dict", listener);
        XmlUtils.parseApplePList(new File(getDefinition()).toURI().toURL(), handler);
        List<CompiledReplacementRule> pathReplacements = new ArrayList<CompiledReplacementRule>();
        for (ReplacementRule pathReplacement : getPathReplacements()) {
            pathReplacements.add(new CompiledReplacementRule(pathReplacement));
        }
        String musicFolderFilename = ItunesLoader.getFileNameForLocation(listener.getMusicFolder());
        for (CompiledReplacementRule pathReplacement : pathReplacements) {
            if (pathReplacement.matches(musicFolderFilename)) {
                return new File(pathReplacement.replace(musicFolderFilename));
            }
        }
        return new File(musicFolderFilename);
    }

}