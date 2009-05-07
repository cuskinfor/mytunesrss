package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;

/**
 * Service with transcoding related functions.
 */
public class TranscodingService {
    /**
     * Get the transcoding request parameter for the specified values.
     *
     * @param transcoderNames             Names of the transcoder to use, comma separated.
     * @param transcodingBitrate          The target bitrate if transcoding is enabled.
     * @param transcodingSamplerate       The target sample rate if transcoding is enabled.
     * @param transcodeOnTheFlyIfPossible <code>true</code> to enable on-the-fly transcoding or <code>false</code> to disable it.
     * @return The transcoding request parameter in the form <code>key</code>=<code>value</code>.
     */
    public String getTranscodingParameter(String[] transcoderNames, int transcodingBitrate,
                                          int transcodingSamplerate, boolean transcodeOnTheFlyIfPossible) throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {

            return "tc=" + MyTunesRssWebUtils.createTranscodingParamValue(transcoderNames,
                    transcodingBitrate,
                    transcodingSamplerate,
                    transcodeOnTheFlyIfPossible);
        }
        throw new IllegalAccessException("Unauthorized");
    }
}