package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;

/**
 * de.codewave.mytunesrss.jmx.ErrorQueue
 */
public class ErrorQueue {
    private String myLastError;

    public ErrorQueue() {
        clearError();
    }

    public void setLastError(String lastError) {
        myLastError = lastError;
    }

    public void clear() {
        clearError();
    }

    private void clearError() {
        myLastError = MyTunesRss.BUNDLE.getString("ok");
    }

    public String popLastError() {
        try {
            return myLastError;
        } finally {
            clearError();
        }
    }
}