package de.codewave.mytunesrss.lastfm;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for accessing the last.fm music social network.
 */
public class LastFmUtils {
    private static final Logger LOG = LoggerFactory.getLogger(LastFmUtils.class);
    private static final String CLIENT_ID = "mtr";
    private static final String CLIENT_VERSION = "0.1";

    /**
     * Do the handshake an return a new session which can be used for further requests.
     *
     * @param user The mytunesrss user.
     *
     * @return The last.fm session or <code>null</code> for any hard error.
     */
    public static LastFmSession doHandshake(User user) {
        LOG.debug("Handshaking with last.fm for user \"" + user.getName() + "\" using last.fm user \"" + user.getLastFmUsername() + "\".");
        long timestamp = System.currentTimeMillis() / 1000L;
        try {
            String passwordHashHex = new String(Hex.encodeHex(user.getLastFmPasswordHash()));
            LOG.debug("Password hash in hex is \"" + passwordHashHex + "\".");
            String authToken = new String(Hex.encodeHex(MyTunesRss.MD5_DIGEST.digest((passwordHashHex + timestamp).getBytes("UTF-8"))));
            String uri = "http://post.audioscrobbler.com/?hs=true&p=1.2&c=" + CLIENT_ID + "&v=" + CLIENT_VERSION + "&u=" + user.getLastFmUsername() +
                    "&t=" + timestamp + "&a=" + authToken;
            LOG.debug("Last.fm URI is \"" + uri + "\".");
            GetMethod getMethod = new GetMethod(uri);
            try {
                if (MyTunesRssUtils.createHttpClient().executeMethod(getMethod) == 200) {
                    List<String> responseLines = IOUtils.readLines(getMethod.getResponseBodyAsStream());
                    if (LOG.isDebugEnabled()) {
                        for (String line : responseLines) {
                            LOG.debug("Last.fm response line: \"" + line + "\".");
                        }
                    }
                    LastFmSession session = new LastFmSession();
                    String response = !responseLines.isEmpty() ? responseLines.get(0) : "FAILED";
                    if ("OK".equals(response) && responseLines.size() == 4) {
                        session.setSessionId(responseLines.get(1));
                        session.setNowPlayingUrl(responseLines.get(2));
                        session.setSubmissionUrl(responseLines.get(3));
                        return session;
                    } else {
                        if (responseLines.isEmpty()) {
                            LOG.warn("Last.fm handshake response was empty.");
                        } else {
                            LOG.warn("Last.fm handshake response was " + responseLines.size() + " lines with the first being \"" + response + "\".");
                        }
                    }
                } else {
                    LOG.warn("HTTP response code from last.fm was " + getMethod.getStatusCode());
                }
            } finally {
                getMethod.releaseConnection();
            }
        } catch (IOException e) {
            LOG.warn("Handshake with last.fm failed.", e);
        }
        return null;
    }

    public static boolean sendNowPlaying(LastFmSession session, Track track) {
        if (session != null && track != null && track.getMediaType() == MediaType.Audio) {
            LOG.debug("Sending NOW PLAYING information to last.fm.");
            PostMethod postMethod = new PostMethod(session.getNowPlayingUrl());
            postMethod.getParams().setContentCharset("UTF-8");
            postMethod.setParameter("s", session.getSessionId());
            postMethod.setParameter("a", track.getOriginalArtist());
            postMethod.setParameter("t", track.getName());
            postMethod.setParameter("b", track.getAlbum());
            postMethod.setParameter("l", Integer.toString(track.getTime()));
            postMethod.setParameter("n", Integer.toString(track.getPosNumber()));
            postMethod.setParameter("m", "");
            try {
                if (MyTunesRssUtils.createHttpClient().executeMethod(postMethod) == 200) {
                    List<String> responseLines = IOUtils.readLines(postMethod.getResponseBodyAsStream());
                    if (responseLines.isEmpty()) {
                        LOG.info("No response from last.fm.");
                    } else {
                        LOG.info("Response from last.fm: \"" + responseLines.get(0) + "\".");
                        return "OK".equals(responseLines.get(0));
                    }
                } else {
                    LOG.warn("HTTP response code from last.fm was " + postMethod.getStatusCode());
                }
            } catch (IOException e) {
                LOG.warn("Sending NOW PLAYING to last.fm failed.", e);
            } finally {
                postMethod.releaseConnection();
            }
            return false;
        }
        // nothing sent, so no error here
        return true;
    }

    public static boolean sendSubmissions(LastFmSession session) {
        if (session != null) {
            while (true) {
                int index = 0;
                PostMethod postMethod = new PostMethod(session.getSubmissionUrl());
                postMethod.getParams().setContentCharset("UTF-8");
                postMethod.setParameter("s", session.getSessionId());
                List<LastFmSubmission> submissions = new ArrayList<LastFmSubmission>();
                for (LastFmSubmission submission = session.pollSubmission(); submission != null && index < 50;
                        submission = session.pollSubmission()) {
                    if (submission.getTrack().getTime() >= 30 && submission.getTrack().getMediaType() == MediaType.Audio) {// only track with at least 30 seconds
                        submissions.add(submission);
                        postMethod.setParameter("a[" + index + "]", submission.getTrack().getOriginalArtist());
                        postMethod.setParameter("t[" + index + "]", submission.getTrack().getName());
                        postMethod.setParameter("b[" + index + "]", submission.getTrack().getAlbum());
                        LOG.debug("Adding submission for \"" + submission.getTrack().getAlbum() + " -- " + submission.getTrack().getOriginalArtist() +
                                " -- " + submission.getTrack().getName());
                        postMethod.setParameter("l[" + index + "]", Integer.toString(submission.getTrack().getTime()));
                        postMethod.setParameter("n[" + index + "]", Integer.toString(submission.getTrack().getPosNumber()));
                        postMethod.setParameter("m[" + index + "]", "");
                        postMethod.setParameter("o[" + index + "]", "P");
                        postMethod.setParameter("r[" + index + "]", "");
                        postMethod.setParameter("i[" + index + "]", Long.toString(submission.getPlaybackStartTime() / 1000L));
                        index++;
                    }
                }
                if (index > 0) {
                    LOG.debug("Sending SUBMISSION for " + index + " track(s) to last.fm.");
                    try {
                        if (MyTunesRssUtils.createHttpClient().executeMethod(postMethod) == 200) {
                            List<String> responseLines = IOUtils.readLines(postMethod.getResponseBodyAsStream());
                            if (responseLines.isEmpty()) {
                                LOG.info("No response from last.fm.");
                            } else {
                                LOG.info("Response from last.fm: \"" + responseLines.get(0) + "\".");
                                return "OK".equals(responseLines.get(0));
                            }
                        } else {
                            LOG.warn("HTTP response code from last.fm was " + postMethod.getStatusCode());
                            session.offerSubmissions(submissions);
                            break;
                        }
                    } catch (IOException e) {
                        LOG.warn("Sending NOW PLAYING to last.fm failed.", e);
                        session.offerSubmissions(submissions);
                        break;
                    } finally {
                        postMethod.releaseConnection();
                    }
                } else {
                    // nothing sent, so no error here
                    return true;
                }
            }
            return false;// hard error
        }
        // nothing sent, so no error here
        return true;
    }
}