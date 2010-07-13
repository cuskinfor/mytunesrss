package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRssUtils;
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
     * @return The transcoding request parameter in the form <code>key</code>=<code>value</code>.
     */
    public String getTranscodingParameter(String[] transcoderNames) throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return "tc=" + MyTunesRssUtils.getUtf8UrlEncoded(MyTunesRssWebUtils.createTranscodingParamValue(transcoderNames));
        }
        throw new IllegalAccessException("UNAUTHORIZED");
    }
}