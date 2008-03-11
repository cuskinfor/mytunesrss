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

    protected File getErrorLogFile(int pipelinePosition) throws IOException {
        switch (pipelinePosition) {
            case 1:
                return new File(PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/faad2.log");
            case 2:
                return new File(PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/lame.log");
            default:
                throw new IllegalArgumentException("Illegal pipeline position \"" + pipelinePosition + "\" specified.");
        }
    }

    protected String getSourceName() {
        return "faad2";
    }

    protected String getTargetName() {
        return "lame";
    }
}