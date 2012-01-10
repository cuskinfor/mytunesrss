package de.codewave.mytunesrss.meta;

import de.codewave.camel.mp4.CoverAtom;
import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.meta.MyTunesRssMp3Utils
 */
public class MyTunesRssMp4Utils {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssMp4Utils.class);

    public static Image getImage(Track track) {
        File file = track.getFile();
        return getImage(file);
    }

    public static Image getImage(File file) {
        if (FileSupportUtils.isMp4(file)) {
            try {
                CoverAtom atom = (CoverAtom) MyTunesRss.MP4_PARSER.parseAndGet(file, "moov.udta.meta.ilst.covr");
                if (atom != null) {
                    return new Image(atom.getMimeType(), atom.getDataStream());
                }
            } catch (IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.warn("Could not extract artwork for \"" + file + "\".", e);
                }
            }
        }
        return null;
    }
}