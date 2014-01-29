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
    private Queue<LastFmSubmission> mySubmissions = new ConcurrentLinkedQueue<>();
    private String myLastSubmittedTrackId;

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

    public String getLastSubmittedTrackId() {
        return myLastSubmittedTrackId;
    }

    public void setLastSubmittedTrackId(String lastSubmittedTrackId) {
        myLastSubmittedTrackId = lastSubmittedTrackId;
    }

    public void offerSubmission(LastFmSubmission submission) {
        if (!submission.getTrack().getId().equals(myLastSubmittedTrackId)) {
            mySubmissions.offer(submission);
            myLastSubmittedTrackId = submission.getTrack().getId();
        }
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