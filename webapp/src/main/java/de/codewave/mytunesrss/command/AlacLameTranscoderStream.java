package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;

import java.io.File;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public class AlacLameTranscoderStream extends AbstractTranscoderStream {
    public AlacLameTranscoderStream(File file, int outputBitRate, int outputSampleRate) throws IOException {
        super(file, MyTunesRss.CONFIG.getMp3Binary(), MyTunesRss.CONFIG.getAlacBinary(), outputBitRate, outputSampleRate);
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
        return MyTunesRss.CONFIG.getMp3TargetOptions();
    }
}