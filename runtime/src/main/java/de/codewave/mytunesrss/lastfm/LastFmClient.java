package de.codewave.mytunesrss.lastfm;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for accessing the last.fm music social network.
 */
public class LastFmClient {
    private static final Log LOG = LogFactory.getLog(LastFmClient.class);

    private HttpClient myHttpClient;

    public LastFmClient() {
        myHttpClient = new HttpClient();
        myHttpClient.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
        if (MyTunesRss.CONFIG.isProxyServer()) {
            HostConfiguration hostConfig = new HostConfiguration();
            hostConfig.setProxy(MyTunesRss.CONFIG.getProxyHost(), MyTunesRss.CONFIG.getProxyPort());
            myHttpClient.setHostConfiguration(hostConfig);
        }
    }

    /**
     * Do the handshake an return a new session which can be used for further requests.
     *
     * @param user The mytunesrss user.
     *
     * @return The last.fm session or <code>null</code> for any hard error.
     */
    public LastFmSession doHandshake(User user) {
        LOG.debug("Handshaking with last.fm for user \"" + user.getName() + "\" using last.fm user \"" + user.getLastFmUser() + "\".");
        long timestamp = System.currentTimeMillis() / 1000L;
        try {
            String passwordHashHex = new String(Hex.encodeHex(user.getLastFmPasswordHash()));
            LOG.debug("Password hash in hex is \"" + passwordHashHex + "\".");
            String authToken = new String(Hex.encodeHex(MyTunesRss.MD5_DIGEST.digest((passwordHashHex + timestamp).getBytes("UTF-8"))));
            String uri =
                    "http://post.audioscrobbler.com/?hs=true&p=1.2&c=tst&v=1.0&u=" + user.getLastFmUser() + "&t=" + timestamp + "&a=" + authToken;
            LOG.debug("Last.fm URI is \"" + uri + "\".");
            GetMethod getMethod = new GetMethod(uri);
            try {
                if (myHttpClient.executeMethod(getMethod) == 200) {
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

    public void sendNowPlaying(LastFmSession session, Track track) {
        if (session != null) {
            if (track != null) {
                LOG.debug("Sending NOW PLAYING information to last.fm.");
                PostMethod postMethod = new PostMethod(session.getNowPlayingUrl());
                postMethod.setParameter("s", session.getSessionId());
                postMethod.setParameter("a", track.getArtist());
                postMethod.setParameter("t", track.getName());
                postMethod.setParameter("b", track.getAlbum());
                postMethod.setParameter("l", Integer.toString(track.getTime()));
                postMethod.setParameter("n", Integer.toString(track.getPosNumber()));
                postMethod.setParameter("m", "");
                try {
                    if (myHttpClient.executeMethod(postMethod) == 200) {
                        List<String> responseLines = IOUtils.readLines(postMethod.getResponseBodyAsStream());
                        if (responseLines.isEmpty()) {
                            LOG.info("No response from last.fm.");
                        } else {
                            LOG.info("Response from last.fm: \"" + responseLines.get(0) + "\".");
                        }
                    } else {
                        LOG.warn("HTTP response code from last.fm was " + postMethod.getStatusCode());
                    }
                } catch (IOException e) {
                    LOG.warn("Sending NOW PLAYING to last.fm failed.", e);
                } finally {
                    postMethod.releaseConnection();
                }
            }
        }
    }

    public void sendSubmissions(LastFmSession session) {
        if (session != null) {
            while (true) {
                int index = 0;
                PostMethod postMethod = new PostMethod(session.getSubmissionUrl());
                postMethod.setParameter("s", session.getSessionId());
                List<LastFmSubmission> submissions = new ArrayList<LastFmSubmission>();
                for (LastFmSubmission submission = session.pollSubmission(); submission != null && index < 50;
                        submission = session.pollSubmission()) {
                    submissions.add(submission);
                    postMethod.setParameter("a[" + index + "]", submission.getTrack().getArtist());
                    postMethod.setParameter("t[" + index + "]", submission.getTrack().getName());
                    postMethod.setParameter("b[" + index + "]", submission.getTrack().getAlbum());
                    LOG.debug("Adding submission for \"" + submission.getTrack().getAlbum() + " -- " + submission.getTrack().getArtist() + " -- " +
                            submission.getTrack().getName());
                    postMethod.setParameter("l[" + index + "]", Integer.toString(submission.getTrack().getTime()));
                    postMethod.setParameter("n[" + index + "]", Integer.toString(submission.getTrack().getPosNumber()));
                    postMethod.setParameter("m[" + index + "]", "");
                    postMethod.setParameter("o[" + index + "]", "P");
                    postMethod.setParameter("r[" + index + "]", "");
                    postMethod.setParameter("i[" + index + "]", Long.toString(submission.getPlaybackStartTime() / 1000L));
                    index++;
                }
                if (index > 0) {
                    LOG.debug("Sending SUBMISSION for " + index + " track(s) to last.fm.");
                    try {
                        if (myHttpClient.executeMethod(postMethod) == 200) {
                            List<String> responseLines = IOUtils.readLines(postMethod.getResponseBodyAsStream());
                            if (responseLines.isEmpty()) {
                                LOG.info("No response from last.fm.");
                            } else {
                                LOG.info("Response from last.fm: \"" + responseLines.get(0) + "\".");
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
                    break;
                }
            }
        }
    }
}