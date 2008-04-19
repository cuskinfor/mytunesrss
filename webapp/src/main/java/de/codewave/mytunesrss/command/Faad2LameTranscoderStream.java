package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.utils.PrefsUtils;

import java.io.File;
import java.io.IOException;

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
}