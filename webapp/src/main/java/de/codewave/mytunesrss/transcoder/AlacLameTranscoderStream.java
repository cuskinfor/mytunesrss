package de.codewave.mytunesrss.transcoder;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;

import java.io.IOException;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public class AlacLameTranscoderStream extends AbstractTranscoderStream {
    public AlacLameTranscoderStream(Track track, int outputBitRate, int outputSampleRate) throws IOException {
        super(track, MyTunesRss.CONFIG.getLameBinary(), MyTunesRss.CONFIG.getAlacBinary(), outputBitRate, outputSampleRate);
    }

    protected String getSourceName() {
        return "alac";
    }

    protected String getTargetName() {
        return "lame";
    }

    protected String getSourceArguments() {
        return MyTunesRss.CONFIG.getAlacSourceOptions();
    }

    protected String getTargetArguments() {
        return MyTunesRss.CONFIG.getLameTargetOptions();
    }
}