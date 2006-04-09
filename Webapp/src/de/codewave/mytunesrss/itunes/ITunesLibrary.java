package de.codewave.mytunesrss.itunes;

import de.codewave.mytunesrss.musicfile.*;
import de.codewave.utils.xml.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.itunes.ITunesLibrary
 */
public class ITunesLibrary implements Serializable {
    private static final Log LOG = LogFactory.getLog(ITunesLibrary.class);

    private List<MusicFile> myTitles = new ArrayList<MusicFile>();

    public void load(URL iTunesLibraryXml) throws IOException, SAXException, ParserConfigurationException {
        Map plist = (Map)XmlUtils.parseApplePList(iTunesLibraryXml);
        Map<String, Map<String, String>> tracks = (Map<String, Map<String, String>>)plist.get("Tracks");
        for (Iterator<Map<String, String>> trackIterator = tracks.values().iterator(); trackIterator.hasNext();) {
            Map<String, String> track = trackIterator.next();
            MusicFile musicFile = new MusicFile();
            musicFile.setAlbum(track.get("Album"));
            musicFile.setArtist(track.get("Artist"));
            musicFile.setId(track.get("Track ID"));
            musicFile.setName(track.get("Name"));
            String trackNumber = track.get("Track Number");
            musicFile.setTrackNumber(StringUtils.isNotEmpty(trackNumber) ? Integer.parseInt(trackNumber) : 0);
            String location = track.get("Location").substring("file://localhost".length());
            try {
                musicFile.setFile(new File(URLDecoder.decode(location, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not create file for \"" + location + "\".", e);
                }
            }
            if (musicFile.isValid()) {
                myTitles.add(musicFile);
            }
        }
        Collections.sort(myTitles, new MusicFileComparator());
    }

    public List<MusicFile> getMatchingFiles(MusicFileSearch... searches) {
        List<MusicFile> matchingFiles = new ArrayList<MusicFile>();
        if (searches != null && searches.length > 0) {
            for (Iterator<MusicFile> iterator = myTitles.iterator(); iterator.hasNext();) {
                MusicFile file = iterator.next();
                boolean match = true;
                for (int i = 0; i < searches.length; i++) {
                    if (!searches[i].matches(file)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    matchingFiles.add(file);
                }
            }
        }
        return matchingFiles;
    }

    private static class MusicFileComparator implements Comparator<MusicFile> {
        public int compare(MusicFile m1, MusicFile m2) {
            int value = m1.getArtist().compareTo(m2.getArtist());
            if (value == 0) {
                value = m1.getAlbum().compareTo(m2.getAlbum());
                if (value == 0) {
                    value = m1.getTrackNumber() - m2.getTrackNumber();
                    if (value == 0) {
                        value = m1.getName().compareTo(m2.getName());
                    }
                }
            }
            return value;
        }
    }
}
