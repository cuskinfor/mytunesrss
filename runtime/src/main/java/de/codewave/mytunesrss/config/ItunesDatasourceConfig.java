/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.ImageImportType;
import de.codewave.mytunesrss.datastore.itunes.ItunesLoader;
import de.codewave.mytunesrss.datastore.itunes.ItunesPlaylistType;
import de.codewave.utils.xml.PListHandler;
import de.codewave.utils.xml.PListHandlerListener;
import de.codewave.utils.xml.XmlUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

public class ItunesDatasourceConfig extends DatasourceConfig implements CommonTrackDatasourceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItunesDatasourceConfig.class);

    private static final String[] AUTO_ADD_NAMES = new String[] {
            "Automatically Add to iTunes",
            "Automatically Add to iTunes.localized"
    };

    private static class StopParsingException extends RuntimeException {
        // Exception thrown when the parser requests to stop parsing
    }

    private static class MusicFolderListener implements PListHandlerListener {
        private String myMusicFolder;

        public boolean beforeDictPut(Map dict, String key, Object value) {
            if ("Music Folder".equals(key)) {
                myMusicFolder = value.toString();
                throw new StopParsingException();
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
    private List<String> myTrackImagePatterns = new ArrayList<String>();
    private ImageImportType myTrackImageImportType = ImageImportType.Auto;
    private String myMusicFolderFilename;
    private boolean myUseSingleImageInFolder = false;

    public ItunesDatasourceConfig(String id, String name, String definition) {
        super(id, StringUtils.defaultIfBlank(name, "iTunes"), definition);
        myMusicFolderFilename = extractMusicFolderFilename();
    }

    public ItunesDatasourceConfig(ItunesDatasourceConfig source) {
        super(source);
        myPathReplacements = new HashSet<ReplacementRule>(source.getPathReplacements());
        myIgnorePlaylists = new HashSet<ItunesPlaylistType>(source.getIgnorePlaylists());
        myDeleteMissingFiles = source.isDeleteMissingFiles();
        myArtistDropWords = source.getArtistDropWords();
        myDisabledMp4Codecs = source.getDisabledMp4Codecs();
        myTrackImagePatterns = new ArrayList<String>(source.getTrackImagePatterns());
        myTrackImageImportType = source.getTrackImageImportType();
        myMusicFolderFilename = extractMusicFolderFilename();
        myUseSingleImageInFolder = source.isUseSingleImageInFolder();
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

    public List<String> getTrackImagePatterns() {
        return new ArrayList<String>(myTrackImagePatterns);
    }

    public void setTrackImagePatterns(List<String> trackImageMappings) {
        this.myTrackImagePatterns = new ArrayList<String>(trackImageMappings);
    }

    public ImageImportType getTrackImageImportType() {
        return myTrackImageImportType;
    }

    public void setTrackImageImportType(ImageImportType trackImageImportType) {
        myTrackImageImportType = trackImageImportType;
    }

    public File getAutoAddToItunesFolder() {
        List<CompiledReplacementRule> pathReplacements = new ArrayList<CompiledReplacementRule>();
        for (ReplacementRule pathReplacement : getPathReplacements()) {
            pathReplacements.add(new CompiledReplacementRule(pathReplacement));
        }
        String musicFolderFilename = myMusicFolderFilename;
        for (CompiledReplacementRule pathReplacement : pathReplacements) {
            if (pathReplacement.matches(musicFolderFilename)) {
                musicFolderFilename = pathReplacement.replace(musicFolderFilename);
                break;
            }
        }
        for (String name : AUTO_ADD_NAMES) {
            File file = new File(musicFolderFilename, name);
            if (file.isDirectory()) {
                LOGGER.debug("Found iTunes auto-add folder \"" + file.getAbsolutePath() + "\".");
                return file;
            }
        }
        LOGGER.debug("Could not find iTunes auto-add folder.");
        return null;
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

    /**
     * Get the "Automatically add to iTunes" folder.
     *
     * @return The file representing the auto-add folder or NULL if no such folder could be found.
     */
    private String extractMusicFolderFilename() {
        PListHandler handler = new PListHandler();
        MusicFolderListener listener = new MusicFolderListener();
        handler.addListener("/plist/dict", listener);
        try {
            LOGGER.debug("Parsing iTunes XML to find music folder.");
            XmlUtils.parseApplePList(new File(getDefinition()).toURI().toURL(), handler);
            LOGGER.debug("Finished parsing iTunes XML without stop-exception!");
        } catch (ParserConfigurationException e) {
            LOGGER.warn("Could not find iTunes auto-add folder.", e);
            return null;
        } catch (SAXException e) {
            LOGGER.warn("Could not find iTunes auto-add folder.", e);
            return null;
        } catch (MalformedURLException e) {
            LOGGER.warn("Could not find iTunes auto-add folder.", e);
            return null;
        } catch (IOException e) {
            LOGGER.warn("Could not find iTunes auto-add folder.", e);
            return null;
        } catch (StopParsingException e) {
            LOGGER.debug("Finished parsing iTunes XML with stop-exception!");
        }
        return ItunesLoader.getFileNameForLocation(listener.getMusicFolder());
    }

    @Override
    public boolean isUploadable() {
        return getAutoAddToItunesFolder() != null;
    }

    public boolean isUseSingleImageInFolder() {
        return myUseSingleImageInFolder;
    }

    public void setUseSingleImageInFolder(boolean useSingleImageInFolder) {
        myUseSingleImageInFolder = useSingleImageInFolder;
    }
}
