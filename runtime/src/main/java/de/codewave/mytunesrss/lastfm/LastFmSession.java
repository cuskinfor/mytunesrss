package de.codewave.mytunesrss.lastfm;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Container for the last.fm handshake response which identifies a session for a MyTunesRSS user.
 */
public class LastFmSession {
    private String mySessionId;
    private String myNowPlayingUrl;
    private String mySubmissionUrl;
    private Queue<LastFmSubmission> mySubmissions = new ConcurrentLinkedQueue<LastFmSubmission>();

    public String getNowPlayingUrl() {
        return myNowPlayingUrl;
    }

    public void setNowPlayingUrl(String nowPlayingUrl) {
        myNowPlayingUrl = nowPlayingUrl;
    }

    public String getSessionId() {
        return mySessionId;
    }

    public void setSessionId(String sessionId) {
        mySessionId = sessionId;
    }

    public String getSubmissionUrl() {
        return mySubmissionUrl;
    }

    public void setSubmissionUrl(String submissionUrl) {
        mySubmissionUrl = submissionUrl;
    }

    public void offerSubmission(LastFmSubmission submission) {
        mySubmissions.offer(submission);
    }

    public void offerSubmissions(List<LastFmSubmission> submissions) {
        for (LastFmSubmission submission : submissions) {
            mySubmissions.offer(submission);
        }
    }

    LastFmSubmission pollSubmission() {
        return mySubmissions.poll();
    }
}