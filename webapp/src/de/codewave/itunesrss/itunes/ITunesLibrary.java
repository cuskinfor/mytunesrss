package de.codewave.itunesrss.itunes;

import de.codewave.utils.xml.*;
import de.codewave.itunesrss.musicfile.*;
import org.apache.commons.jxpath.*;
import org.apache.commons.logging.*;
import org.apache.commons.lang.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * de.codewave.itunesrss.itunes.ITunesLibrary
 */
public class ITunesLibrary implements Serializable {
    private static final Log LOG = LogFactory.getLog(ITunesLibrary.class);

    private static final String TOKEN_ALBUM = "Album";
    private static final String TOKEN_ARTIST = "Artist";
    private static final String TOKEN_NAME = "Name";
    private static final String TOKEN_TRACK_NUMBER = "Track Number";
    private static final String TOKEN_LOCATION = "Locationfile://localhost";
    private static final String TOKEN_ID = "Track ID";

    private List<MusicFile> myTitles = new ArrayList<MusicFile>();
    private Set<String> myUsedIds = new HashSet<String>();

    public void load(URL iTunesLibraryXml) {
        JXPathContext rootContext = JXPathUtils.getContext(iTunesLibraryXml);
        for (Iterator<JXPathContext> iterator = JXPathUtils.getContextIterator(rootContext, "/plist/dict/dict/dict"); iterator.hasNext();) {
            JXPathContext titleContext = iterator.next();
            MusicFile title = new MusicFile();
            for (StringTokenizer tokenizer = new StringTokenizer((String)titleContext.getValue("."), "\n"); tokenizer.hasMoreTokens();) {
                String line = tokenizer.nextToken().trim();
                if (line.startsWith(TOKEN_ALBUM)) {
                    title.setAlbum(line.substring(TOKEN_ALBUM.length()));
                }
                if (line.startsWith(TOKEN_ARTIST)) {
                    title.setArtist(line.substring(TOKEN_ARTIST.length()));
                }
                if (line.startsWith(TOKEN_NAME)) {
                    title.setName(line.substring(TOKEN_NAME.length()));
                }
                if (line.startsWith(TOKEN_TRACK_NUMBER)) {
                    String trackNumber = line.substring(TOKEN_TRACK_NUMBER.length());
                    if (!StringUtils.isEmpty(trackNumber)) {
                        title.setTrackNumber(Integer.parseInt(trackNumber));
                    }
                }
                if (line.startsWith(TOKEN_LOCATION)) {
                    try {
                        title.setFile(new File(URLDecoder.decode(line.substring(TOKEN_LOCATION.length()), "UTF-8")));
                    } catch (UnsupportedEncodingException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not create file for \"" + line.substring(TOKEN_LOCATION.length()) + "\".", e);
                        }
                    }
                }
                if (line.startsWith(TOKEN_ID)) {
                    title.setId(line.substring(TOKEN_ID.length()));
                }
            }
            if (title.isComplete()) {
                if (!myUsedIds.contains(title.getId())) {
                    myUsedIds.add(title.getId());
                    myTitles.add(title);
                }
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
