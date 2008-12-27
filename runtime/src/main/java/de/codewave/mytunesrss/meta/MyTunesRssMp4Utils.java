package de.codewave.mytunesrss.meta;

import de.codewave.camel.mp4.Mp4Atom;
import de.codewave.camel.mp4.Mp4Utils;
import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

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
                Mp4Atom atom = Mp4Utils.getAtoms(file, Collections.singletonList("moov.udta.meta.ilst.covr.data"))
                        .get("moov.udta.meta.ilst.covr.data");
                if (atom != null) {
                    byte type = atom.getData()[3];
                    return new Image(type == 0x0d ? "image/jpeg" : "image/png", ArrayUtils.subarray(atom.getData(), 8, atom.getData().length - 8));
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