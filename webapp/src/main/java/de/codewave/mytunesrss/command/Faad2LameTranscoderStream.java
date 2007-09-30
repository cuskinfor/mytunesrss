package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;

import java.io.*;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public class Faad2LameTranscoderStream extends AbstractTranscoderStream {
    public Faad2LameTranscoderStream(File file, int outputBitRate, int outputSampleRate) throws IOException {
        super(file, MyTunesRss.CONFIG.getLameBinary(), MyTunesRss.CONFIG.getFaad2Binary(), outputBitRate, outputSampleRate);
    }

    protected String getSourceName() {
        return "faad2";
    }

    protected String getTargetName() {
        return "lame";
    }

    protected String getSourceArguments() {
        return "-f 2 -w {infile}";
    }

    protected String getTargetArguments() {
        return "--quiet -b {bitrate} --resample {samplerate} - -";
    }
}