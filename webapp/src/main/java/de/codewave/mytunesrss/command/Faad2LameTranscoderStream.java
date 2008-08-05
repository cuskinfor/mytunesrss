package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;

import java.io.File;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public class Faad2LameTranscoderStream extends AbstractTranscoderStream {
    public Faad2LameTranscoderStream(File file, int outputBitRate, int outputSampleRate) throws IOException {
        super(file, MyTunesRss.CONFIG.getMp3Binary(), MyTunesRss.CONFIG.getAacBinary(), outputBitRate, outputSampleRate);
    }

    protected String getSourceName() {
        return "faad2";
    }

    protected String getTargetName() {
        return "lame";
    }

    protected String getSourceArguments() {
        return MyTunesRss.CONFIG.getAacSourceOptions();
    }

    protected String getTargetArguments() {
        return MyTunesRss.CONFIG.getMp3TargetOptions();
    }
}