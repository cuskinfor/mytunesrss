package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.utils.PrefsUtils;

import java.io.File;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public class AlacLameTranscoderStream extends AbstractTranscoderStream {
    public AlacLameTranscoderStream(File file, int outputBitRate, int outputSampleRate) throws IOException {
        super(file, MyTunesRss.CONFIG.getLameBinary(), MyTunesRss.CONFIG.getAlacBinary(), outputBitRate, outputSampleRate);
    }

    protected File getErrorLogFile(int pipelinePosition) throws IOException {
        switch (pipelinePosition) {
            case 1:
                return new File(PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/alac.log");
            case 2:
                return new File(PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/lame.log");
            default:
                throw new IllegalArgumentException("Illegal pipeline position \"" + pipelinePosition + "\" specified.");
        }
    }

    protected String getSourceName() {
        return "alac";
    }

    protected String getTargetName() {
        return "lame";
    }
}