package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.utils.MiscUtils;

/**
 * Service with transcoding related functions.
 */
public class TranscodingService {
    /**
     * Get the transcoding request parameter for the specified values.
     *
     * @param transcoderNames             Names of the transcoder to use, comma separated.
     * @return The transcoding request parameter in the form <code>key</code>=<code>value</code>.
     */
    public String getTranscodingParameter(String[] transcoderNames) throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return "tc=" + MiscUtils.getUtf8UrlEncoded(MyTunesRssWebUtils.createTranscodingParamValue(transcoderNames));
        }
        throw new IllegalAccessException("UNAUTHORIZED");
    }
}