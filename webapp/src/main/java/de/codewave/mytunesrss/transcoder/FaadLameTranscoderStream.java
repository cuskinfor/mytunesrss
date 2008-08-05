package de.codewave.mytunesrss.transcoder;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;

import java.io.File;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public class FaadLameTranscoderStream extends AbstractTranscoderStream {
    public FaadLameTranscoderStream(Track track, int outputBitRate, int outputSampleRate) throws IOException {
        super(track, MyTunesRss.CONFIG.getLameBinary(), MyTunesRss.CONFIG.getFaadBinary(), outputBitRate, outputSampleRate);
    }

    protected String getSourceName() {
        return "faad";
    }

    protected String getTargetName() {
        return "lame";
    }

    protected String getSourceArguments() {
        return MyTunesRss.CONFIG.getFaadSourceOptions();
    }

    protected String getTargetArguments() {
        return MyTunesRss.CONFIG.getLameTargetOptions();
    }
}