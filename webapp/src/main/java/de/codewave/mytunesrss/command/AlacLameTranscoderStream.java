package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;

import java.io.*;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public class AlacLameTranscoderStream extends AbstractTranscoderStream {
    public AlacLameTranscoderStream(File file, int outputBitRate, int outputSampleRate) throws IOException {
        super(file, MyTunesRss.CONFIG.getLameBinary(), MyTunesRss.CONFIG.getFaad2Binary(), outputBitRate, outputSampleRate); // todo ALAC binary
    }

    protected String getSourceName() {
        return "alac";
    }

    protected String getTargetName() {
        return "lame";
    }

    protected String getSourceArguments() {
        return "{infile}";
    }

    protected String getTargetArguments() {
        return "--quiet -b {bitrate} --resample {samplerate} - -";
    }
}