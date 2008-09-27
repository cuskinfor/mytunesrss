package de.codewave.mytunesrss.datastore.itunes;

/**
 * Result for procession a track during database update.
 */
public class ProcessTrackInfo {
    private boolean myInserted;
    private boolean myMissing;

    public boolean isInserted() {
        return myInserted;
    }

    public void setInserted(boolean inserted) {
        myInserted = inserted;
    }

    public boolean isMissing() {
        return myMissing;
    }

    public void setMissing(boolean missing) {
        myMissing = missing;
    }
}